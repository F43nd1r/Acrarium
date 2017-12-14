package com.faendir.acra.sql.data;

import com.faendir.acra.sql.model.App;
import com.faendir.acra.sql.model.Attachment;
import com.faendir.acra.sql.model.Bug;
import com.faendir.acra.sql.model.ProguardMapping;
import com.faendir.acra.sql.model.Report;
import com.faendir.acra.sql.util.AndroidVersionCount;
import com.faendir.acra.sql.util.DayCount;
import com.faendir.acra.util.BufferedDataProvider;
import com.faendir.acra.util.Utils;
import com.vaadin.data.provider.DataProvider;
import org.acra.ReportField;
import org.apache.commons.io.FileUtils;
import org.hibernate.Hibernate;
import org.hibernate.SessionFactory;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;
import org.springframework.web.multipart.MultipartFile;
import proguard.retrace.ReTrace;

import javax.persistence.EntityManagerFactory;
import java.io.File;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.SecureRandom;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author Lukas
 * @since 11.12.2017
 */
@Component
public class DataManager {
    private final SecureRandom secureRandom;
    private final AppRepository appRepository;
    private final ProguardMappingRepository mappingRepository;
    private final ReportRepository reportRepository;
    private final BugRepository bugRepository;
    private final SessionFactory sessionFactory;
    private final AttachmentRepository attachmentRepository;
    @NonNull private final Logger logger;

    @Autowired
    public DataManager(@NonNull SecureRandom secureRandom, @NonNull AppRepository appRepository, @NonNull ProguardMappingRepository mappingRepository,
                       @NonNull ReportRepository reportRepository, @NonNull BugRepository bugRepository, @NonNull EntityManagerFactory entityManagerFactory,
                       @NonNull AttachmentRepository attachmentRepository) {
        this.secureRandom = secureRandom;
        this.appRepository = appRepository;
        this.mappingRepository = mappingRepository;
        this.reportRepository = reportRepository;
        this.bugRepository = bugRepository;
        this.sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        this.logger = LoggerFactory.getLogger(DataManager.class);
        this.attachmentRepository = attachmentRepository;
    }

    public App newApp(@NonNull String name) {
        byte[] bytes = new byte[12];
        secureRandom.nextBytes(bytes);
        return appRepository.save(new App(name, Base64Utils.encodeToString(bytes)));
    }

    public List<App> getApps() {
        return appRepository.findAll();
    }

    @NonNull
    public Optional<App> getApp(int id) {
        return appRepository.findById(id);
    }

    public Optional<App> getApp(String encodedId) {
        try {
            return getApp(Integer.parseInt(encodedId));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public void deleteApp(@NonNull App app) {
        appRepository.delete(app);
    }

    public void addMapping(@NonNull App app, int version, @NonNull String mappings) {
        mappingRepository.save(new ProguardMapping(app, version, mappings));
    }

    @NonNull
    private Optional<ProguardMapping> getMapping(@NonNull App app, int version) {
        return mappingRepository.findById(new ProguardMapping.MetaData(app, version));
    }

    @NonNull
    public List<ProguardMapping> getMappings(@NonNull App app) {
        return mappingRepository.findAllByApp(app);
    }

    public void newReport(@NonNull App app, @NonNull String content, @NonNull List<MultipartFile> attachments) {
        JSONObject jsonObject = new JSONObject(content);
        String stacktrace = jsonObject.optString(ReportField.STACK_TRACE.name());
        Date crashDate = Utils.getDateFromString(jsonObject.optString(ReportField.USER_CRASH_DATE.name()));
        Bug bug = bugRepository.findBugByAppAndStacktrace(app, stacktrace)
                .orElseGet(() -> new Bug(app, stacktrace, jsonObject.optInt(ReportField.APP_VERSION_CODE.name()), crashDate));
        bug.setLastReport(crashDate);
        Report report = reportRepository.save(new Report(bug, content));
        attachments.forEach(multipartFile -> {
            try {
                attachmentRepository.save(new Attachment(report, multipartFile.getName(), Hibernate.getLobCreator(sessionFactory.getCurrentSession())
                        .createBlob(multipartFile.getInputStream(), multipartFile.getSize())));
            } catch (IOException e) {
                logger.warn("Failed to load attachment with name " + multipartFile.getName(), e);
            }
        });
    }

    public void purgeReportsBefore(@NonNull App app, @NonNull Date before){
        reportRepository.deleteAllByBugAppAndDateBefore(app, before);
    }

    public List<DayCount> getReportCountByDayAfter(@NonNull App app, @NonNull Date after){
        return reportRepository.countAllByDayAfter(app, after);
    }

    public List<AndroidVersionCount> getReportCountByAndroidVersion(@NonNull App app){
        return reportRepository.countAllByAndroidVersion(app);
    }

    @NonNull
    public DataProvider<Report, Void> lazyGetReportsForApp(@NonNull App app) {
        return new BufferedDataProvider<>(app, reportRepository::findAllByBugApp, reportRepository::countAllByBugApp);
    }

    public int reportCountForApp(@NonNull App app) {
        return reportRepository.countAllByBugApp(app);
    }

    @NonNull
    public Optional<Report> getReport(@NonNull String id) {
        return reportRepository.findById(id);
    }

    public void deleteReport(@NonNull Report report) {
        reportRepository.delete(report);
    }

    @NonNull
    public String retrace(@NonNull Report report) {
        Optional<ProguardMapping> mapping = getMapping(report.getBug().getApp(), report.getVersionCode());
        if (mapping.isPresent()) {
            try {
                File file = File.createTempFile("mapping", ".txt");
                FileUtils.writeStringToFile(file, mapping.get().getMappings());
                StringWriter writer = new StringWriter();
                new ReTrace(ReTrace.STACK_TRACE_EXPRESSION, false, file).retrace(new LineNumberReader(new StringReader(report.getStacktrace())), new PrintWriter(writer));
                //noinspection ResultOfMethodCallIgnored
                file.delete();
                return writer.toString();
            } catch (IOException ignored) {
            }
        }
        return report.getStacktrace();
    }

    @NonNull
    public List<Bug> getBugs(@NonNull App app) {
        return bugRepository.findAllByApp(app);
    }

    public DataProvider<Bug, Void> lazyGetBugs(@NonNull App app, boolean hideSolved) {
        return new BufferedDataProvider<>(app, hideSolved ? bugRepository::findAllByAppAndSolvedFalse : bugRepository::findAllByApp,
                                          hideSolved ? bugRepository::countAllByAppAndSolvedFalse : bugRepository::countAllByApp);
    }

    @NonNull
    public List<Report> getReportsForBug(@NonNull Bug bug) {
        return reportRepository.findAllByBug(bug);
    }

    @NonNull
    public DataProvider<Report, Void> lazyGetReportsForBug(@NonNull Bug bug) {
        return new BufferedDataProvider<>(bug, reportRepository::findAllByBug, reportRepository::countAllByBug);
    }

    public int reportCountForBug(@NonNull Bug bug) {
        return reportRepository.countAllByBug(bug);
    }

    public void setBugSolved(@NonNull Bug bug, boolean solved) {
        bug.setSolved(solved);
        bugRepository.save(bug);
    }

    public List<Attachment> getAttachments(@NonNull Report report) {
        return attachmentRepository.findAllByReport(report);
    }
}
