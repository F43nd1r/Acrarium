package com.faendir.acra.service;

import com.faendir.acra.sql.data.AppRepository;
import com.faendir.acra.sql.data.AttachmentRepository;
import com.faendir.acra.sql.data.BugRepository;
import com.faendir.acra.sql.data.ReportRepository;
import com.faendir.acra.sql.model.App;
import com.faendir.acra.sql.model.Attachment;
import com.faendir.acra.sql.model.Bug;
import com.faendir.acra.sql.model.Report;
import com.faendir.acra.util.Utils;
import org.acra.ReportField;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.SessionFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.persistence.EntityManagerFactory;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author Lukas
 * @since 22.03.2017
 */
@RestController
public class ReportService {
    @NonNull private final AppRepository appRepository;
    @NonNull private final BugRepository bugRepository;
    @NonNull private final ReportRepository reportRepository;
    @NonNull private final AttachmentRepository attachmentRepository;
    @NonNull private final SessionFactory sessionFactory;
    @NonNull private final Log log;

    @Autowired
    public ReportService(@NonNull AppRepository appRepository, @NonNull BugRepository bugRepository, @NonNull ReportRepository reportRepository,
            @NonNull AttachmentRepository attachmentRepository, @NonNull EntityManagerFactory entityManagerFactory) {
        this.appRepository = appRepository;
        this.bugRepository = bugRepository;
        this.reportRepository = reportRepository;
        this.attachmentRepository = attachmentRepository;
        this.sessionFactory = entityManagerFactory.unwrap(SessionFactory.class);
        this.log = LogFactory.getLog(getClass());
    }

    @PreAuthorize("hasRole('REPORTER')")
    @RequestMapping(value = "/report", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void report(@NonNull @RequestBody String content, @NonNull Principal principal) {
        if (!"".equals(content)) {
            Optional<App> app = appRepository.findByReporterUsername(principal.getName());
            app.ifPresent(a -> newReport(a, content, Collections.emptyList()));
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
            String filename = file.getName();
            if (filename.isEmpty()) {
                content = StreamUtils.copyToString(file.getInputStream(), StandardCharsets.UTF_8);
            } else {
                attachments.add(file);
            }
        }
        if (content != null) {
            Optional<App> app = appRepository.findByReporterUsername(principal.getName());
            String finalContent = content;
            app.ifPresent(a -> newReport(a, finalContent, attachments));
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    private void newReport(@NonNull App app, @NonNull String content, @NonNull List<MultipartFile> attachments) {
        JSONObject jsonObject = new JSONObject(content);
        String stacktrace = jsonObject.optString(ReportField.STACK_TRACE.name());
        Date crashDate = Utils.getDateFromString(jsonObject.optString(ReportField.USER_CRASH_DATE.name()));
        Bug bug = bugRepository.findBugByAppAndStacktrace(app, stacktrace)
                .orElseGet(() -> new Bug(app, stacktrace, jsonObject.optInt(ReportField.APP_VERSION_CODE.name()), crashDate));
        bug.setLastReport(crashDate);
        Report report = reportRepository.save(new Report(bug, content));
        attachments.forEach(multipartFile -> {
            try {
                attachmentRepository.save(new Attachment(report, multipartFile.getName(),
                        Hibernate.getLobCreator(sessionFactory.getCurrentSession()).createBlob(multipartFile.getInputStream(), multipartFile.getSize())));
            } catch (IOException e) {
                log.warn("Failed to load attachment with name " + multipartFile.getName(), e);
            }
        });
    }
}
