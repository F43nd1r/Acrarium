package com.faendir.acra.mongod.data;

import com.faendir.acra.mongod.model.App;
import com.faendir.acra.mongod.model.ProguardMapping;
import com.faendir.acra.mongod.model.Report;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.gridfs.GridFSDBFile;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Lukas
 * @since 21.05.2017
 */
@Component
public class DataManager {
    private final MappingRepository mappingRepository;
    private final ReportRepository reportRepository;
    private final List<ReportChangeListener> listeners;
    private final GridFsTemplate gridFsTemplate;
    private final Logger logger;
    private final SecureRandom secureRandom;
    private final AppRepository appRepository;

    @Autowired
    public DataManager(SecureRandom secureRandom, AppRepository appRepository, GridFsTemplate gridFsTemplate, MappingRepository mappingRepository, ReportRepository reportRepository) {
        this.secureRandom = secureRandom;
        this.appRepository = appRepository;
        logger = LoggerFactory.getLogger(DataManager.class);
        this.gridFsTemplate = gridFsTemplate;
        this.mappingRepository = mappingRepository;
        this.reportRepository = reportRepository;
        this.listeners = new ArrayList<>();
    }

    public void createNewApp(String name){
        byte[] bytes = new byte[12];
        secureRandom.nextBytes(bytes);
        appRepository.save(new App(name, Base64Utils.encodeToString(bytes)));
    }

    public List<App> getApps(){
        return appRepository.findAll();
    }

    public App getApp(String id){
        return appRepository.findOne(id);
    }

    public void deleteApp(String id){
        appRepository.delete(id);
        getReports(id).forEach(this::remove);
        mappingRepository.delete(getMappings(id));
    }

    public void saveAttachments(String report, List<MultipartFile> attachments) {
        for (MultipartFile a : attachments) {
            try {
                gridFsTemplate.store(a.getInputStream(), a.getOriginalFilename(), a.getContentType(), new BasicDBObjectBuilder().add("reportId", report).get());
            } catch (IOException e) {
                logger.warn("Failed to load attachment", e);
            }
        }
    }

    public List<GridFSDBFile> getAttachments(String report) {
        return gridFsTemplate.find(new Query(Criteria.where("metadata.reportId").is(report)));
    }

    public void removeAttachments(String report) {
        gridFsTemplate.delete(new Query(Criteria.where("metadata.reportId").is(report)));
    }

    public void addMapping(String app, int version, String mappings) {
        mappingRepository.save(new ProguardMapping(app, version, mappings));
    }

    public ProguardMapping getMapping(String app, int version) {
        return mappingRepository.findOne(new ProguardMapping.MetaData(app, version));
    }

    public List<ProguardMapping> getMappings(String app) {
        return mappingRepository.findAll(Example.of(new ProguardMapping(app, -1, null), ExampleMatcher.matchingAny()));
    }

    public void newReport(JSONObject content) {
        newReport(content, Collections.emptyList());
    }

    public void newReport(JSONObject content, List<MultipartFile> attachments) {
        Report report = reportRepository.save(new Report(content, SecurityContextHolder.getContext().getAuthentication().getName()));
        saveAttachments(report.getId(), attachments);
        listeners.forEach(ReportChangeListener::onChange);
    }

    public List<Report> getReports(String app) {
        return reportRepository.findAll(Example.of(new Report(null, app)));
    }

    public Report getReport(String id) {
        return reportRepository.findOne(id);
    }

    public void remove(Report report){
        reportRepository.delete(report);
        removeAttachments(report.getId());
        listeners.forEach(ReportChangeListener::onChange);
    }

    public boolean addListener(ReportChangeListener reportChangeListener) {
        return listeners.add(reportChangeListener);
    }

    public boolean removeListener(ReportChangeListener reportChangeListener) {
        return listeners.remove(reportChangeListener);
    }

    public interface ReportChangeListener {
        void onChange();
    }
}
