package com.faendir.acra.service;

import com.faendir.acra.data.ReportManager;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * @author Lukas
 * @since 22.03.2017
 */
@RestController
public class ReportService {
    private final ReportManager reportManager;

    @Autowired
    public ReportService(ReportManager reportManager) {
        this.reportManager = reportManager;
    }

    @PreAuthorize("hasRole('REPORTER')")
    @RequestMapping(value = "/report", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void report(@RequestBody String content) throws IOException {
        JSONObject jsonObject = new JSONObject(content);
        reportManager.newReport(jsonObject);
    }
}
