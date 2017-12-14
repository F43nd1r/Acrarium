package com.faendir.acra.service;

import com.faendir.acra.sql.data.DataManager;
import com.faendir.acra.sql.model.App;
import org.springframework.lang.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
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
import java.util.Optional;

/**
 * @author Lukas
 * @since 22.03.2017
 */
@RestController
public class ReportService {
    @NonNull private final DataManager dataManager;

    @Autowired
    public ReportService(@NonNull DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @PreAuthorize("hasRole('REPORTER')")
    @RequestMapping(value = "/report", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void report(@NonNull @RequestBody String content, @NonNull Principal principal) {
        if (!"".equals(content)) {
            Optional<App> app = dataManager.getApp(principal.getName());
            app.ifPresent(a -> dataManager.newReport(a, content, Collections.emptyList()));
        }
    }

    @PreAuthorize("hasRole('REPORTER')")
    @RequestMapping(value = "/report", consumes = "multipart/mixed")
    public ResponseEntity report(@NonNull MultipartHttpServletRequest request, @NonNull Principal principal) throws IOException {
        MultiValueMap<String, MultipartFile> fileMap = request.getMultiFileMap();
        List<MultipartFile> files = fileMap.get(null);
        String content = null;
        List<MultipartFile> attachments = new ArrayList<>();
        for (MultipartFile file : files) {
            String filename = file.getOriginalFilename();
            if (filename.isEmpty()) {
                content = StreamUtils.copyToString(file.getInputStream(), StandardCharsets.UTF_8);
            } else {
                attachments.add(file);
            }
        }
        if (content != null) {
            Optional<App> app = dataManager.getApp(principal.getName());
            String finalContent = content;
            app.ifPresent(a -> dataManager.newReport(a, finalContent, attachments));
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
}
