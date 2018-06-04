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

package com.faendir.acra.rest;

import com.faendir.acra.service.data.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Lukas
 * @since 22.03.2017
 */
@RestController
public class RestReportInterface {
    private static final String REPORT_PATH = "/report";
    @NonNull private final DataService dataService;

    @Autowired
    public RestReportInterface(@NonNull DataService dataService) {
        this.dataService = dataService;
    }

    @PreAuthorize("hasRole(T(com.faendir.acra.model.User$Role).REPORTER)")
    @RequestMapping(value = REPORT_PATH, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void report(@NonNull @RequestBody String content, @NonNull Principal principal) {
        if (!"".equals(content)) {
            dataService.createNewReport(principal.getName(), content, Collections.emptyList());
        }
    }

    @PreAuthorize("hasRole(T(com.faendir.acra.model.User$Role).REPORTER)")
    @RequestMapping(value = REPORT_PATH, consumes = "multipart/mixed")
    public ResponseEntity report(@NonNull MultipartHttpServletRequest request, @NonNull Principal principal) throws IOException {
        String content = null;
        List<MultipartFile> attachments = new ArrayList<>();
        for (MultipartFile file : request.getMultiFileMap().get(null)) {
            String filename = file.getName();
            if (filename.isEmpty()) {
                content = StreamUtils.copyToString(file.getInputStream(), StandardCharsets.UTF_8);
            } else {
                attachments.add(file);
            }
        }
        if (content != null) {
            dataService.createNewReport(principal.getName(), content, attachments);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
}
