package com.faendir.acra.mongod.data;

import com.faendir.acra.mongod.model.App;
import com.faendir.acra.mongod.model.Bug;
import com.faendir.acra.mongod.model.ProguardMapping;
import com.faendir.acra.mongod.model.Report;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.gridfs.GridFSDBFile;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author Lukas
 * @since 21.05.2017
 */
@Component
public class DataManager {
    private static final String APP_REPORT_CACHE = "appReport";
    private static final String APP_CACHE = "app";
    private final MappingRepository mappingRepository;
    private final ReportRepository reportRepository;
    private final AppRepository appRepository;
    private final BugRepository bugRepository;
    private final List<ReportChangeListener> listeners;
    private final GridFsTemplate gridFsTemplate;
    private final Logger logger;
    private final SecureRandom secureRandom;

    @Autowired
    public DataManager(SecureRandom secureRandom, AppRepository appRepository, GridFsTemplate gridFsTemplate, MappingRepository mappingRepository, ReportRepository reportRepository, BugRepository bugRepository) {
        this.secureRandom = secureRandom;
        this.appRepository = appRepository;
        this.bugRepository = bugRepository;
        logger = LoggerFactory.getLogger(DataManager.class);
        this.gridFsTemplate = gridFsTemplate;
        this.mappingRepository = mappingRepository;
        this.reportRepository = reportRepository;
        this.listeners = new ArrayList<>();
    }

    @CacheEvict(APP_CACHE)
    public synchronized void createNewApp(String name) {
        byte[] bytes = new byte[12];
        secureRandom.nextBytes(bytes);
        appRepository.save(new App(name, Base64Utils.encodeToString(bytes)));
    }

    @Cacheable(APP_CACHE)
    public List<App> getApps() {
        return appRepository.findAll();
    }

    public App getApp(String id) {
        return appRepository.findOne(id);
    }

    @CacheEvict(APP_CACHE)
    public synchronized void deleteApp(String id) {
        appRepository.delete(id);
        getReportsForApp(id).forEach(this::deleteReport);
        mappingRepository.delete(getMappings(id));
    }

    public List<GridFSDBFile> getAttachments(String report) {
        return gridFsTemplate.find(new Query(Criteria.where("metadata.reportId").is(report)));
    }

    public synchronized void addMapping(String app, int version, String mappings) {
        mappingRepository.save(new ProguardMapping(app, version, mappings));
    }

    public ProguardMapping getMapping(String app, int version) {
        return mappingRepository.findOne(new ProguardMapping.MetaData(app, version));
    }

    public List<ProguardMapping> getMappings(String app) {
        return mappingRepository.findByApp(app);
    }

    public void newReport(String app, JSONObject content) {
        newReport(app, content, Collections.emptyList());
    }

    @CacheEvict(value = APP_REPORT_CACHE, key = "#a0")
    public synchronized void newReport(String app, JSONObject content, List<MultipartFile> attachments) {
        Report report = reportRepository.save(new Report(content, app));
        for (MultipartFile a : attachments) {
            try {
                gridFsTemplate.store(a.getInputStream(), a.getOriginalFilename(), a.getContentType(), new BasicDBObjectBuilder().add("reportId", report.getId()).get());
            } catch (IOException e) {
                logger.warn("Failed to load attachment", e);
            }
        }
        Bug bug = bugRepository.findOne(new Bug.Identification(report.getStacktrace().hashCode(), report.getVersionCode()));
        if (bug == null) {
            bugRepository.save(new Bug(app, report.getStacktrace(), report.getVersionCode()));
        }
        listeners.forEach(ReportChangeListener::onChange);
    }

    @Cacheable(APP_REPORT_CACHE)
    public List<Report> getReportsForApp(String app) {
        return reportRepository.findByApp(app);
    }

    public Report getReport(String id) {
        return reportRepository.findOne(id);
    }

    @CacheEvict(value = APP_REPORT_CACHE, key = "#a0.app")
    public synchronized void deleteReport(Report report) {
        reportRepository.delete(report);
        gridFsTemplate.delete(new Query(Criteria.where("metadata.reportId").is(report.getId())));
        if(reportRepository.countByBug(report.getStacktrace(), report.getVersionCode()) == 0){
            Optional.ofNullable(bugRepository.findOne(new Bug.Identification(report.getStacktrace().hashCode(), report.getVersionCode()))).ifPresent(bugRepository::delete);
        }
        listeners.forEach(ReportChangeListener::onChange);
    }

    public List<Bug> getBugs(String app) {
        return bugRepository.findByApp(app);
    }

    public List<Report> getReportsForBug(Bug bug) {
        return reportRepository.findByBug(bug.getStacktrace(), bug.getVersionCode());
    }

    public int countReportsForBug(Bug bug){
        return reportRepository.countByBug(bug.getStacktrace(), bug.getVersionCode());
    }

    public String retrace(Report report){
        ProguardMapping mapping = getMapping(report.getApp(), report.getVersionCode());
        if (mapping != null) {
            try {
                return ReportUtils.retrace(report.getStacktrace(), mapping);
            } catch (IOException ignored) {
            }
        }
        return report.getStacktrace();
    }

    public void setBugSolved(Bug bug, boolean solved){
        bug.setSolved(solved);
        bugRepository.save(bug);
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
