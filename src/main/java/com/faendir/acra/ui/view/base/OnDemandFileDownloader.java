/*
 * (C) Copyright 2018 Lukas Morawietz (https://github.com/F43nd1r)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
