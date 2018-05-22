package com.faendir.acra.service.data;

import com.faendir.acra.config.AcraConfiguration;
import com.faendir.acra.dataprovider.BufferedDataProvider;
import com.faendir.acra.dataprovider.ObservableDataProvider;
import com.faendir.acra.model.App;
import com.faendir.acra.model.Attachment;
import com.faendir.acra.model.Bug;
import com.faendir.acra.model.Permission;
import com.faendir.acra.model.ProguardMapping;
import com.faendir.acra.model.QReport;
import com.faendir.acra.model.Report;
import com.faendir.acra.model.User;
import com.faendir.acra.model.base.BaseBug;
import com.faendir.acra.model.view.VApp;
import com.faendir.acra.model.view.VBug;
import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.service.user.UserService;
import com.faendir.acra.sql.data.AppRepository;
import com.faendir.acra.sql.data.AppViewRepository;
import com.faendir.acra.sql.data.AttachmentRepository;
import com.faendir.acra.sql.data.BugRepository;
import com.faendir.acra.sql.data.BugViewRepository;
import com.faendir.acra.sql.data.ProguardMappingRepository;
import com.faendir.acra.sql.data.ReportRepository;
import com.faendir.acra.util.PlainTextUser;
import com.faendir.acra.util.Utils;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import org.acra.ReportField;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.validation.constraints.Size;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author lukas
 * @since 16.05.18
 */
@Service
public class DataService implements Serializable {
    @NonNull private final Log log = LogFactory.getLog(getClass());
    @NonNull private final AcraConfiguration acraConfiguration;
    @NonNull private final AppRepository appRepository;
    @NonNull private final AppViewRepository appViewRepository;
    @NonNull private final BugRepository bugRepository;
    @NonNull private final BugViewRepository bugViewRepository;
    @NonNull private final ReportRepository reportRepository;
    @NonNull private final AttachmentRepository attachmentRepository;
    @NonNull private final ProguardMappingRepository mappingRepository;
    @NonNull private final UserService userService;
    @NonNull private final EntityManager entityManager;

    @Autowired
    public DataService(@NonNull AcraConfiguration acraConfiguration, @NonNull AppRepository appRepository, @NonNull AppViewRepository appViewRepository,
            @NonNull BugRepository bugRepository, @NonNull BugViewRepository bugViewRepository, @NonNull ReportRepository reportRepository,
            @NonNull AttachmentRepository attachmentRepository, @NonNull ProguardMappingRepository mappingRepository, @NonNull UserService userService,
            @NonNull EntityManager entityManager) {
        this.acraConfiguration = acraConfiguration;
        this.appRepository = appRepository;
        this.appViewRepository = appViewRepository;
        this.bugRepository = bugRepository;
        this.bugViewRepository = bugViewRepository;
        this.reportRepository = reportRepository;
        this.attachmentRepository = attachmentRepository;
        this.mappingRepository = mappingRepository;
        this.userService = userService;
        this.entityManager = entityManager;
    }

    @NonNull
    public ObservableDataProvider<VApp, Void> getAppProvider() {
        boolean isAdmin = SecurityUtils.hasRole(User.Role.ADMIN);
        return new BufferedDataProvider<>(acraConfiguration.getPaginationSize(),
                isAdmin ?
                        pageable -> appViewRepository.findAllByPermissionWithDefaultIncluded(SecurityUtils.getUsername(), Permission.Level.VIEW, pageable) :
                        pageable -> appViewRepository.findAllByPermissionWithDefaultExcluded(SecurityUtils.getUsername(), Permission.Level.VIEW, pageable),
                isAdmin ?
                        () -> appViewRepository.countByPermissionWithDefaultIncluded(SecurityUtils.getUsername(), Permission.Level.VIEW) :
                        () -> appViewRepository.countByPermissionWithDefaultExcluded(SecurityUtils.getUsername(), Permission.Level.VIEW));
    }

    @NonNull
    public ObservableDataProvider<VBug, Void> getBugProvider(@NonNull App app, boolean onlyNonSolved) {
        return new BufferedDataProvider<>(acraConfiguration.getPaginationSize(),
                onlyNonSolved ? pageable -> bugViewRepository.findAllByAppAndSolvedFalse(app, pageable) : pageable -> bugViewRepository.findAllByApp(app, pageable),
                onlyNonSolved ? () -> bugViewRepository.countAllByAppAndSolvedFalse(app) : () -> bugViewRepository.countAllByApp(app));
    }

    @NonNull
    public ObservableDataProvider<ProguardMapping, Void> getMappingProvider(@NonNull App app) {
        return new BufferedDataProvider<>(acraConfiguration.getPaginationSize(),
                pageable -> mappingRepository.findAllByApp(app, pageable),
                () -> mappingRepository.countAllByApp(app));
    }

    /**
     * Creates a new app
     *
     * @param name the name of the new app
     * @return the name of the reporter user and its password (plaintext)
     */
    @NonNull
    public PlainTextUser createNewApp(@NonNull String name) {
        PlainTextUser user = userService.createReporterUser();
        appRepository.save(new App(name, user));
        return user;
    }

    @NonNull
    public PlainTextUser recreateReporterUser(@NonNull App app) {
        PlainTextUser user = userService.createReporterUser();
        app.setReporter(user);
        appRepository.save(app);
        return user;
    }

    @Transactional
    public void mergeBugs(@NonNull @Size(min = 2) Collection<? extends BaseBug> bugs, @NonNull String title) {
        List<BaseBug> list = new ArrayList<>(bugs);
        Bug bug = bugRepository.getOne(list.remove(0).getId());
        bug.setTitle(title);
        bug.setStacktraces(bugRepository.loadStacktraces(bugs));
        bugRepository.save(bug);
        try (Stream<Report> stream = reportRepository.streamAllByBugIn(list)) {
            stream.forEach(report -> {
                report.setBug(bug);
                reportRepository.save(report);
            });
        }
        bugRepository.deleteAllByIdIn(list.stream().map(BaseBug::getId).collect(Collectors.toList()));
    }

    @NonNull
    public Optional<Date> getLatestReportDate(@NonNull Bug bug) {
        return reportRepository.maxDateByBug(bug);
    }

    @Transactional
    public void setBugSolved(@NonNull BaseBug bug, boolean solved) {
        Bug b = bugRepository.getOne(bug.getId());
        b.setSolved(solved);
        bugRepository.save(b);
    }

    @NonNull
    public Optional<App> findAppById(@NonNull String encodedId) {
        try {
            return appRepository.findById(Integer.parseInt(encodedId));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public List<App> findAllApps() {
        return appRepository.findAll();
    }

    @Transactional
    public void changeConfiguration(@NonNull App app, @NonNull App.Configuration configuration) {
        app.setConfiguration(configuration);
        App a = appRepository.save(app);
        try (Stream<Report> stream = reportRepository.streamAllByAppEager(a)) {
            stream.forEach(report -> {
                String stacktrace = Utils.generifyStacktrace(report.getStacktrace(), a.getConfiguration());
                Optional<Bug> bug = bugRepository.findBugByAppAndStacktraces(a, stacktrace);
                if (!bug.isPresent()) {
                    report.setBug(new Bug(a, stacktrace, report.getVersionCode()));
                    reportRepository.save(report);
                } else if (!report.getBug().equals(bug.get())) {
                    report.setBug(bug.get());
                    reportRepository.save(report);
                }
            });
        }
        bugRepository.deleteOrphans();
    }

    public void deleteReportsOlderThanDays(@NonNull App app, @NonNull int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -days);
        reportRepository.deleteAllByBugAppAndDateBefore(app, calendar.getTime());
    }

    public void delete(@NonNull App app) {
        appRepository.delete(app);
    }

    public void delete(@NonNull ProguardMapping mapping) {
        mappingRepository.delete(mapping);
    }

    public ProguardMapping save(@NonNull ProguardMapping entity) {
        return mappingRepository.save(entity);
    }

    public void createNewReport(@NonNull String reporterUserName, @NonNull String content, @NonNull List<MultipartFile> attachments) {
        appRepository.findByReporterUsername(reporterUserName).ifPresent(app -> {

            JSONObject jsonObject = new JSONObject(content);
            String stacktrace = Utils.generifyStacktrace(jsonObject.optString(ReportField.STACK_TRACE.name()), app.getConfiguration());
            Bug bug = bugRepository.findBugByAppAndStacktraces(app, stacktrace).orElseGet(() -> new Bug(app, stacktrace, jsonObject.optInt(ReportField.APP_VERSION_CODE.name())));
            Report report = reportRepository.save(new Report(bug, content));
            attachments.forEach(multipartFile -> {
                try {
                    attachmentRepository.save(new Attachment(report,
                            multipartFile.getName(),
                            Hibernate.getLobCreator(entityManager.unwrap(Session.class)).createBlob(multipartFile.getInputStream(), multipartFile.getSize())));
                } catch (IOException e) {
                    log.warn("Failed to load attachment with name " + multipartFile.getName(), e);
                }
            });
        });
    }

    public <T> Map<T, Long> countReports(Predicate where, Expression<?> groupBy, Expression<T> select) {
        QReport report = QReport.report;
        JPAQuery<?> query = new JPAQuery<>(entityManager);
        List<Tuple> result = query.from(report).where(where).groupBy(groupBy).select(select, report.id.count()).fetch();
        return result.stream().collect(Collectors.toMap(tuple -> tuple.get(select), tuple -> tuple.get(report.id.count())));
    }

    public <T> List<T> getFromReports(Predicate where, Expression<T> select) {
        QReport report = QReport.report;
        JPAQuery<?> query = new JPAQuery<>(entityManager);
        return query.from(report).where(where).distinct().select(select).fetch();
    }
}
