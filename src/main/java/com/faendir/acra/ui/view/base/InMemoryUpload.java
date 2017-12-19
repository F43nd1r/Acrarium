package com.faendir.acra.ui.view.base;

import com.vaadin.ui.Upload;

import java.io.ByteArrayOutputStream;

/**
 * @author Lukas
 * @since 19.12.2017
 */
public class InMemoryUpload extends Upload {
    private final ByteArrayOutputStream outputStream;
    private boolean finished;

    public InMemoryUpload(String caption) {
        super();
        outputStream = new ByteArrayOutputStream();
        finished = false;
        setCaption(caption);
        setReceiver((filename, mimeType) -> outputStream);
        addSucceededListener(event -> finished = true);
        addFailedListener(event -> finished = false);
    }

    public boolean isUploaded() {
        return finished;
    }

    public String getUploadedString() {
        return outputStream.toString();
    }
}
