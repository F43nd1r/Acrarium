package com.faendir.acra.data;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author Lukas
 * @since 22.03.2017
 */
@Component
public class ReportManager {
    private final ReportRepository reportRepository;

    @Autowired
    public ReportManager(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public Report newReport(JSONObject content) {
        return reportRepository.save(new Report(content, SecurityContextHolder.getContext().getAuthentication().getName()));
    }

    public List<Report> getReports(String app) {
        return reportRepository.findAll(Example.of(new Report(null, app)));
    }

    public Report getReport(String id) {
        return reportRepository.findOne(id);
    }
}
