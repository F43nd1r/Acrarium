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
