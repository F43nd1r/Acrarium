package com.faendir.acra.service;

import com.faendir.acra.mongod.data.DataManager;
import org.json.JSONObject;
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

import javax.servlet.ServletException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Lukas
 * @since 22.03.2017
 */
@RestController
public class ReportService {
    private final DataManager dataManager;

    @Autowired
    public ReportService(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @PreAuthorize("hasRole('REPORTER')")
    @RequestMapping(value = "/report", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void report(@RequestBody String content) throws IOException {
        JSONObject jsonObject = new JSONObject(content);
        dataManager.newReport(jsonObject);
    }

    @PreAuthorize("hasRole('REPORTER')")
    @RequestMapping(value = "/report", consumes = "multipart/mixed")
    public ResponseEntity report(MultipartHttpServletRequest request) throws IOException, ServletException {
        MultiValueMap<String, MultipartFile> fileMap = request.getMultiFileMap();
        List<MultipartFile> files = fileMap.get(null);
        JSONObject jsonObject = null;
        List<MultipartFile> attachments = new ArrayList<>();
        for (MultipartFile file : files) {
            String filename = file.getOriginalFilename();
            if (filename.isEmpty()) {
                jsonObject = new JSONObject(StreamUtils.copyToString(file.getInputStream(), StandardCharsets.UTF_8));
            } else {
                attachments.add(file);
            }
        }
        if(jsonObject != null) {
            dataManager.newReport(jsonObject, attachments);
            return ResponseEntity.ok().build();
        }else {
            return ResponseEntity.badRequest().build();
        }
    }
}
