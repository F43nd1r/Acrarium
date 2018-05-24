package com.faendir.acra.ui.view.base;

import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import org.springframework.data.util.Pair;
import org.springframework.lang.NonNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

/**
 * @author lukas
 * @since 24.05.18
 */

public class OnDemandFileDownloader extends FileDownloader {
    @NonNull
    private final Supplier<Pair<InputStream, String>> provider;

    public OnDemandFileDownloader(@NonNull Supplier<Pair<InputStream, String>> provider) {
        super(new StreamResource(() -> new ByteArrayInputStream(new byte[0]), ""));
        this.provider = provider;
    }

    @Override
    public boolean handleConnectorRequest(VaadinRequest request, VaadinResponse response, String path)
            throws IOException {
        Pair<InputStream, String> source = provider.get();
        if (source != null) {
            getResource().setStreamSource(source::getFirst);
            getResource().setFilename(source.getSecond());
            return super.handleConnectorRequest(request, response, path);
        }
        return false;
    }

    @NonNull
    private StreamResource getResource() {
        return (StreamResource) getFileDownloadResource();
    }

}
