package org.apache.commons.fileupload;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Closeable;
import org.apache.commons.fileupload.util.LimitedInputStream;
import org.apache.commons.fileupload.util.Streams;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

import static java.lang.String.format;

/**
 * @author Lukas
 * @since 18.05.2017
 */
public class Rfc1341ServletFileUpload extends ServletFileUpload {
    public Rfc1341ServletFileUpload(@NotNull FileItemFactory fileItemFactory) {
        super(fileItemFactory);
    }

    @NotNull
    @Override
    public FileItemIterator getItemIterator(@Nullable RequestContext ctx) throws FileUploadException, IOException {
        return new Rfc1341FileItemIterator(ctx);
    }

    /**
     * Modified copy (with appropriate cast) of {@link FileUploadBase#parseRequest(RequestContext)}
     */
    @NotNull
    @Override
    public List<FileItem> parseRequest(@NotNull RequestContext ctx) throws FileUploadException {
        List<FileItem> items = new ArrayList<>();
        boolean successful = false;
        try {
            FileItemIterator iter = getItemIterator(ctx);
            FileItemFactory fac = getFileItemFactory();
            if (fac == null) {
                throw new NullPointerException("No FileItemFactory has been set.");
            }
            while (iter.hasNext()) {
                final FileItemStream item = iter.next();
                // Don't use getName() here to prevent an InvalidFileNameException.
                final String fileName = ((Rfc1341FileItemIterator.Rfc1341FileItemStream) item).name;
                FileItem fileItem = fac.createItem(item.getFieldName(), item.getContentType(), item.isFormField(), fileName);
                items.add(fileItem);
                try {
                    Streams.copy(item.openStream(), fileItem.getOutputStream(), true);
                } catch (FileUploadIOException e) {
                    throw (FileUploadException) e.getCause();
                } catch (IOException e) {
                    throw new IOFileUploadException(format("Processing of %s request failed. %s", MULTIPART_FORM_DATA, e.getMessage()), e);
                }
                final FileItemHeaders fih = item.getHeaders();
                fileItem.setHeaders(fih);
            }
            successful = true;
            return items;
        } catch (FileUploadIOException e) {
            throw (FileUploadException) e.getCause();
        } catch (IOException e) {
            throw new FileUploadException(e.getMessage(), e);
        } finally {
            if (!successful) {
                for (FileItem fileItem : items) {
                    try {
                        fileItem.delete();
                    } catch (Throwable e) {
                        // ignore it
                    }
                }
            }
        }
    }

    /**
     * Modified copy of {@link FileItemIteratorImpl}
     *
     * @author Lukas
     * @since 18.05.2017
     */
    private class Rfc1341FileItemIterator implements FileItemIterator {
        /**
         * The multi part stream to process.
         */
        @NotNull private final MultipartStream multi;
        /**
         * The notifier, which used for triggering the
         * {@link ProgressListener}.
         */
        @NotNull private final MultipartStream.ProgressNotifier notifier;
        /**
         * The boundary, which separates the various parts.
         */
        private final byte[] boundary;
        /**
         * The item, which we currently process.
         */
        @Nullable private Rfc1341FileItemStream currentItem;
        /**
         * Whether we are currently skipping the preamble.
         */
        private boolean skipPreamble;
        /**
         * Whether the current item may still be read.
         */
        private boolean itemValid;
        /**
         * Whether we have seen the end of the file.
         */
        private boolean eof;
        private final long fileSizeMax;
        /**
         * Creates a new instance.
         *
         * @param ctx The request context.
         * @throws FileUploadException An error occurred while
         *                             parsing the request.
         * @throws IOException         An I/O error occurred.
         */
        Rfc1341FileItemIterator(@Nullable RequestContext ctx) throws FileUploadException, IOException {
            this.fileSizeMax = getFileSizeMax();
            long sizeMax = getSizeMax();
            String headerEncoding = getHeaderEncoding();
            ProgressListener listener = getProgressListener();
            if (ctx == null) {
                throw new NullPointerException("ctx parameter");
            }

            String contentType = ctx.getContentType();
            if ((null == contentType) || (!contentType.toLowerCase(Locale.ENGLISH).startsWith(MULTIPART))) {
                throw new InvalidContentTypeException(
                        format("the request doesn't contain a %s or %s stream, content type header is %s", MULTIPART_FORM_DATA, MULTIPART_MIXED, contentType));
            }

            InputStream input = ctx.getInputStream();

            @SuppressWarnings("deprecation") // still has to be backward compatible
            final int contentLengthInt = ctx.getContentLength();

            final long requestSize = UploadContext.class.isAssignableFrom(ctx.getClass())
                    // Inline conditional is OK here CHECKSTYLE:OFF
                    ? ((UploadContext) ctx).contentLength() : contentLengthInt;
            // CHECKSTYLE:ON

            if (sizeMax >= 0) {
                if (requestSize != -1 && requestSize > sizeMax) {
                    throw new SizeLimitExceededException(format("the request was rejected because its size (%s) exceeds the configured maximum (%s)", requestSize, sizeMax),
                                                         requestSize, sizeMax);
                }
                input = new LimitedInputStream(input, sizeMax) {
                    @Override
                    protected void raiseError(long pSizeMax, long pCount) throws IOException {
                        FileUploadException ex = new SizeLimitExceededException(
                                format("the request was rejected because its size (%s) exceeds the configured maximum (%s)", pCount, pSizeMax), pCount, pSizeMax);
                        throw new FileUploadIOException(ex);
                    }
                };
            }

            String charEncoding = headerEncoding;
            if (charEncoding == null) {
                charEncoding = ctx.getCharacterEncoding();
            }

            boundary = getBoundary(contentType);
            if (boundary == null) {
                throw new FileUploadException("the request was rejected because no multipart boundary was found");
            }

            notifier = new MultipartStream.ProgressNotifier(listener, requestSize);
            try {
                multi = new MultipartStream(input, boundary, notifier);
            } catch (IllegalArgumentException iae) {
                throw new InvalidContentTypeException(format("The boundary specified in the %s header is too long", CONTENT_TYPE), iae);
            }
            multi.setHeaderEncoding(charEncoding);

            skipPreamble = true;
            findNextItem();
        }

        /**
         * Called for finding the next item, if any.
         *
         * @return True, if an next item was found, otherwise false.
         * @throws IOException An I/O error occurred.
         */
        private boolean findNextItem() throws IOException {
            if (eof) {
                return false;
            }
            if (currentItem != null) {
                currentItem.close();
                currentItem = null;
            }
            boolean nextPart;
            if (skipPreamble) {
                nextPart = multi.skipPreamble();
            } else {
                nextPart = multi.readBoundary();
            }
            if (!nextPart) {
                eof = true;
                return false;
            }
            FileItemHeaders headers = getParsedHeaders(multi.readHeaders());
            String fileName = getFileName(headers);
            currentItem = new Rfc1341FileItemStream(fileName, headers.getHeader(CONTENT_TYPE), getContentLength(headers));
            currentItem.setHeaders(headers);
            notifier.noteItem();
            itemValid = true;
            return true;
        }

        private long getContentLength(@NotNull FileItemHeaders pHeaders) {
            try {
                return Long.parseLong(pHeaders.getHeader(CONTENT_LENGTH));
            } catch (Exception e) {
                return -1;
            }
        }

        /**
         * Returns, whether another instance of {@link FileItemStream}
         * is available.
         *
         * @return True, if one or more additional file items
         * are available, otherwise false.
         * @throws FileUploadException Parsing or processing the
         *                             file item failed.
         * @throws IOException         Reading the file item failed.
         */
        public boolean hasNext() throws FileUploadException, IOException {
            if (eof) {
                return false;
            }
            if (itemValid) {
                return true;
            }
            try {
                return findNextItem();
            } catch (FileUploadIOException e) {
                // unwrap encapsulated SizeException
                throw (FileUploadException) e.getCause();
            }
        }

        /**
         * Returns the next available {@link FileItemStream}.
         *
         * @return FileItemStream instance, which provides
         * access to the next file item.
         * @throws java.util.NoSuchElementException No more items are
         *                                          available. Use {@link #hasNext()} to prevent this exception.
         * @throws FileUploadException              Parsing or processing the
         *                                          file item failed.
         * @throws IOException                      Reading the file item failed.
         */
        @Nullable
        public FileItemStream next() throws FileUploadException, IOException {
            if (eof || (!itemValid && !hasNext())) {
                throw new NoSuchElementException();
            }
            itemValid = false;
            return currentItem;
        }

        /**
         * Default implementation of {@link FileItemStream}.
         */
        class Rfc1341FileItemStream implements FileItemStream {
            /**
             * The file items content type.
             */
            private final String contentType;
            /**
             * The file items file name.
             */
            private final String name;
            /**
             * The file items input stream.
             */
            private final InputStream stream;
            /**
             * Whether the file item was already opened.
             */
            private boolean opened;
            /**
             * The headers, if any.
             */
            private FileItemHeaders headers;

            /**
             * Creates a new instance.
             *
             * @param pName          The items file name, or null.
             * @param pContentType   The items content type, or null.
             * @param pContentLength The items content length, if known, or -1
             * @throws IOException Creating the file item failed.
             */
            Rfc1341FileItemStream(String pName, String pContentType, long pContentLength) throws IOException {
                name = pName;
                contentType = pContentType;
                final MultipartStream.ItemInputStream itemStream = multi.newInputStream();
                InputStream istream = itemStream;
                if (fileSizeMax != -1) {
                    if (pContentLength != -1 && pContentLength > fileSizeMax) {
                        FileSizeLimitExceededException e = new FileSizeLimitExceededException(
                                format("The file %s exceeds its maximum permitted size of %s bytes.", name, fileSizeMax), pContentLength, fileSizeMax);
                        e.setFileName(pName);
                        throw new FileUploadIOException(e);
                    }
                    istream = new LimitedInputStream(istream, fileSizeMax) {
                        @Override
                        protected void raiseError(long pSizeMax, long pCount) throws IOException {
                            itemStream.close(true);
                            FileSizeLimitExceededException e = new FileSizeLimitExceededException(
                                    format("The file %s exceeds its maximum permitted size of %s bytes.", name, pSizeMax), pCount, pSizeMax);
                            e.setFileName(name);
                            throw new FileUploadIOException(e);
                        }
                    };
                }
                stream = istream;
            }

            /**
             * Returns the items content type, or null.
             *
             * @return Content type, if known, or null.
             */
            public String getContentType() {
                return contentType;
            }

            /**
             * Files are not associated to fields.
             *
             * @return null
             */
            @Nullable
            public String getFieldName() {
                return null;
            }

            /**
             * Returns the items file name.
             *
             * @return File name, if known, or null.
             * @throws InvalidFileNameException The file name contains a NUL character,
             *                                  which might be an indicator of a security attack. If you intend to
             *                                  use the file name anyways, catch the exception and use
             *                                  InvalidFileNameException#getName().
             */
            public String getName() {
                return Streams.checkFileName(name);
            }

            /**
             * Returns, whether this is a form field.
             *
             * @return True, if the item is a form field,
             * otherwise false.
             */
            public boolean isFormField() {
                return false;
            }

            /**
             * Returns an input stream, which may be used to
             * read the items contents.
             *
             * @return Opened input stream.
             * @throws IOException An I/O error occurred.
             */
            public InputStream openStream() throws IOException {
                if (opened) {
                    throw new IllegalStateException("The stream was already opened.");
                }
                if (((Closeable) stream).isClosed()) {
                    throw new ItemSkippedException();
                }
                return stream;
            }

            /**
             * Closes the file item.
             *
             * @throws IOException An I/O error occurred.
             */
            void close() throws IOException {
                stream.close();
            }

            /**
             * Returns the file item headers.
             *
             * @return The items header object
             */
            public FileItemHeaders getHeaders() {
                return headers;
            }

            /**
             * Sets the file item headers.
             *
             * @param pHeaders The items header object
             */
            public void setHeaders(FileItemHeaders pHeaders) {
                headers = pHeaders;
            }
        }
    }
}
