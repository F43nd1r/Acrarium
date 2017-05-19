package com.faendir.acra.data;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Lukas
 * @since 22.03.2017
 */
@Component
public class ReportManager {
    private final ReportRepository reportRepository;
    private final AttachmentManager attachmentManager;
    private final List<ChangeListener> listeners;

    @Autowired
    public ReportManager(ReportRepository reportRepository, AttachmentManager attachmentManager) {
        this.reportRepository = reportRepository;
        this.attachmentManager = attachmentManager;
        listeners = new ArrayList<>();
    }

    public void newReport(JSONObject content) {
        newReport(content, Collections.emptyList());
    }

    public void newReport(JSONObject content, List<MultipartFile> attachments) {
        Report report = reportRepository.save(new Report(content, SecurityContextHolder.getContext().getAuthentication().getName()));
        attachmentManager.saveAttachments(report.getId(), attachments);
        listeners.forEach(ChangeListener::onChange);
    }

    public List<Report> getReports(String app) {
        return reportRepository.findAll(Example.of(new Report(null, app)));
    }

    public Report getReport(String id) {
        return reportRepository.findOne(id);
    }

    public void remove(Report report){
        reportRepository.delete(report);
        attachmentManager.removeAttachments(report.getId());
        listeners.forEach(ChangeListener::onChange);
    }

    public boolean addListener(ChangeListener changeListener) {
        return listeners.add(changeListener);
    }

    public boolean removeListener(ChangeListener changeListener) {
        return listeners.remove(changeListener);
    }

    public interface ChangeListener {
        void onChange();
    }
}
