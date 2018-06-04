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
