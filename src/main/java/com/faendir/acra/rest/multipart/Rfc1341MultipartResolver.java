package com.faendir.acra.rest.multipart;

import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.Rfc1341ServletFileUpload;
import org.springframework.lang.NonNull;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

/**
 * @author Lukas
 * @since 18.05.2017
 */
public class Rfc1341MultipartResolver extends CommonsMultipartResolver {
    @NonNull
    @Override
    protected FileUpload newFileUpload(@NonNull FileItemFactory fileItemFactory) {
        return new Rfc1341ServletFileUpload(fileItemFactory);
    }
}
