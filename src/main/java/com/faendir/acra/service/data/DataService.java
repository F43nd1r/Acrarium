package com.faendir.acra.service.data;

import com.faendir.acra.dataprovider.QueryDslDataProvider;
import com.faendir.acra.model.App;
import com.faendir.acra.model.Attachment;
import com.faendir.acra.model.Bug;
import com.faendir.acra.model.Permission;
import com.faendir.acra.model.ProguardMapping;
import com.faendir.acra.model.QApp;
import com.faendir.acra.model.QBug;
import com.faendir.acra.model.Report;
import com.faendir.acra.model.User;
import com.faendir.acra.model.view.Queries;
import com.faendir.acra.model.view.VApp;
import com.faendir.acra.model.view.VBug;
import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.service.user.UserService;
import com.faendir.acra.util.PlainTextUser;
import com.faendir.acra.util.Utils;
import com.mysema.commons.lang.CloseableIterator;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPADeleteClause;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.faendir.acra.model.QApp.app;
import static com.faendir.acra.model.QAttachment.attachment;
import static com.faendir.acra.model.QBug.bug;
import static com.faendir.acra.model.QPermission.permission;
import static com.faendir.acra.model.QProguardMapping.proguardMapping;
import static com.faendir.acra.model.QReport.report;
import static com.faendir.acra.model.QUser.user;

/**
 * @author lukas
 * @since 16.05.18
 */
@Service
public class DataService implements Serializable {
    @NonNull private final Log log = LogFactory.getLog(getClass());
    @NonNull private final UserService userService;
    @NonNull private final EntityManager entityManager;

    @Autowired
    public DataService(@NonNull UserService userService, @NonNull EntityManager entityManager) {
        this.userService = userService;
        this.entityManager = entityManager;
    }

    @NonNull
    public QueryDslDataProvider<VApp> getAppProvider() {
        boolean isAdmin = SecurityUtils.hasRole(User.Role.ADMIN);
        Function<JPQLQuery<?>, BooleanExpression> existenceFunction = isAdmin ? JPQLQuery::notExists : JPQLQuery::exists;
        BiFunction<EnumPath<Permission.Level>, Permission.Level, BooleanExpression> compareFunction = isAdmin ? EnumPath::lt : EnumPath::goe;
        BooleanExpression where = existenceFunction.apply(JPAExpressions.select(permission)
                .from(user)
                .join(user.permissions, permission)
                .where(user.username.eq(SecurityUtils.getUsername()).and(permission.app.eq(app)).and(compareFunction.apply(permission.level, Permission.Level.VIEW))));
        return new QueryDslDataProvider<>(Queries.selectVApp(entityManager).where(where), new JPAQuery<>(entityManager).from(app).where(where));
    }

    @NonNull
    public QueryDslDataProvider<VBug> getBugProvider(@NonNull App app, BooleanSupplier onlyNonSolvedProvider) {
        Supplier<BooleanExpression> whereSupplier = () -> onlyNonSolvedProvider.getAsBoolean() ? bug.app.eq(app).and(bug.solved.eq(false)) : bug.app.eq(app);
        return new QueryDslDataProvider<>(() -> Queries.selectVBug(entityManager).where(whereSupplier.get()),
                () -> new JPAQuery<>(entityManager).from(bug).where(whereSupplier.get()));
    }

    @NonNull
    public QueryDslDataProvider<Report> getReportProvider(@NonNull Bug bug) {
        return new QueryDslDataProvider<>(new JPAQuery<>(entityManager).from(report).where(report.bug.eq(bug)).select(report));
    }

    @NonNull
    public QueryDslDataProvider<Report> getReportProvider(@NonNull App app) {
        return new QueryDslDataProvider<>(new JPAQuery<>(entityManager).from(report).join(report.bug).where(report.bug.app.eq(app)).select(report));
    }

    @NonNull
    public QueryDslDataProvider<ProguardMapping> getMappingProvider(@NonNull App app) {
        return new QueryDslDataProvider<>(new JPAQuery<>(entityManager).from(proguardMapping).where(proguardMapping.app.eq(app)).select(proguardMapping));
    }

    @Transactional
    public <T> T store(T entity) {
        return entityManager.merge(entity);
    }

    @Transactional
    public void delete(Object entity) {
        entityManager.remove(entityManager.contains(entity) ? entity : entityManager.merge(entity));
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
        store(new App(name, user));
        return user;
    }

    @NonNull
    public PlainTextUser recreateReporterUser(@NonNull App app) {
        PlainTextUser user = userService.createReporterUser();
        app.setReporter(user);
        store(app);
        return user;
    }

    @Transactional
    public void mergeBugs(@NonNull @Size(min = 2) Collection<Bug> bugs, @NonNull String title) {
        List<Bug> list = new ArrayList<>(bugs);
        Bug bug = list.remove(0);
        bug.setTitle(title);
        StringPath stringPath = Expressions.stringPath("trace");
        bug.setStacktraces(new JPAQuery<>(entityManager).from(QBug.bug).join(QBug.bug.stacktraces, stringPath).where(QBug.bug.in(bugs)).select(stringPath).fetch());
        bug = store(bug);
        CloseableIterator<Report> iterator = new JPAQuery<>(entityManager).from(report).where(report.bug.in(list)).select(report).iterate();
        while (iterator.hasNext()) {
            Report report = iterator.next();
            report.setBug(bug);
            store(report);
        }
        iterator.close();
        list.forEach(this::delete);
    }

    @Transactional
    public void setBugSolved(@NonNull Bug bug, boolean solved) {
        bug.setSolved(solved);
        store(bug);
    }

    @NonNull
    public Optional<ProguardMapping> findMapping(@NonNull App app, int versionCode) {
        return Optional.ofNullable(new JPAQuery<>(entityManager).from(proguardMapping)
                .where(proguardMapping.app.eq(app).and(proguardMapping.versionCode.eq(versionCode)))
                .select(proguardMapping)
                .fetchOne());
    }

    @NonNull
    public Optional<Report> findReport(@NonNull String id) {
        return Optional.ofNullable(new JPAQuery<>(entityManager).from(report)
                .join(report.bug, bug)
                .fetchJoin()
                .join(bug.app, app)
                .fetchJoin()
                .where(report.id.eq(id))
                .select(report)
                .fetchOne());
    }

    @NonNull
    public Optional<VBug> findBug(@NonNull String encodedId) {
        try {
            return Optional.ofNullable(Queries.selectVBug(entityManager).where(bug.id.eq(Integer.parseInt(encodedId))).fetchOne());
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    @NonNull
    public Optional<App> findApp(@NonNull String encodedId) {
        try {
            return Optional.ofNullable(new JPAQuery<>(entityManager).from(app).where(app.id.eq(Integer.parseInt(encodedId))).select(app).fetchOne());
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    @NonNull
    public List<App> findAllApps() {
        return new JPAQuery<>(entityManager).from(app).select(app).fetch();
    }

    @NonNull
    public List<Attachment> findAttachments(@NonNull Report report) {
        return new JPAQuery<>(entityManager).from(attachment).where(attachment.report.eq(report)).select(attachment).fetch();
    }

    private void deleteOrphanBugs() {
        new JPADeleteClause(entityManager, bug).where(JPAExpressions.selectFrom(report).where(report.bug.eq(bug)).notExists()).execute();
    }

    private Optional<Bug> findBug(@NonNull App app, @NonNull String stacktrace) {
        return Optional.ofNullable(new JPAQuery<>(entityManager).from(bug).where(bug.app.eq(app).and(bug.stacktraces.any().eq(stacktrace))).select(bug).fetchFirst());
    }

    @Transactional
    public void changeConfiguration(@NonNull App app, @NonNull App.Configuration configuration) {
        app.setConfiguration(configuration);
        app = store(app);
        CloseableIterator<Report> iterator = new JPAQuery<>(entityManager).from(report).join(report.bug, bug).where(bug.app.eq(app)).select(report).iterate();
        while (iterator.hasNext()) {
            Report report = iterator.next();
            String stacktrace = Utils.generifyStacktrace(report.getStacktrace(), app.getConfiguration());
            Optional<Bug> bug = findBug(app, stacktrace);
            if (!bug.isPresent()) {
                report.setBug(new Bug(app, stacktrace, report.getVersionCode()));
                store(report);
            } else if (!bug.get().equals(report.getBug())) {
                report.setBug(bug.get());
                store(report);
            }
        }
        iterator.close();
        deleteOrphanBugs();
    }

    @Transactional
    public void deleteReportsOlderThanDays(@NonNull App app, @NonNull int days) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -days);
        new JPADeleteClause(entityManager, report).where(report.bug.app.eq(app).and(report.date.before(calendar.getTime())));
        deleteOrphanBugs();
    }

    @Transactional
    public void deleteReportsBeforeVersion(@NonNull App app, int versionCode) {
        new JPADeleteClause(entityManager, report).where(report.bug.app.eq(app).and(report.versionCode.lt(versionCode)));
        deleteOrphanBugs();
    }

    @Transactional
    public void createNewReport(@NonNull String reporterUserName, @NonNull String content, @NonNull List<MultipartFile> attachments) {
        App app = new JPAQuery<>(entityManager).from(QApp.app).where(QApp.app.reporter.username.eq(reporterUserName)).select(QApp.app).fetchOne();
        if (app != null) {
            JSONObject jsonObject = new JSONObject(content);
            String stacktrace = Utils.generifyStacktrace(jsonObject.optString(ReportField.STACK_TRACE.name()), app.getConfiguration());
            Bug bug = findBug(app, stacktrace).orElseGet(() -> new Bug(app, stacktrace, jsonObject.optInt(ReportField.APP_VERSION_CODE.name())));
            Report report = store(new Report(bug, content));
            attachments.forEach(multipartFile -> {
                try {
                    store(new Attachment(report,
                            multipartFile.getName(),
                            Hibernate.getLobCreator(entityManager.unwrap(Session.class)).createBlob(multipartFile.getInputStream(), multipartFile.getSize())));
                } catch (IOException e) {
                    log.warn("Failed to load attachment with name " + multipartFile.getName(), e);
                }
            });
        }
    }

    public <T> Map<T, Long> countReports(@NonNull Predicate where, Expression<?> groupBy, @NonNull Expression<T> select) {
        List<Tuple> result = ((JPAQuery<?>) new JPAQuery<>(entityManager)).from(report).where(where).groupBy(groupBy).select(select, report.id.count()).fetch();
        return result.stream().collect(Collectors.toMap(tuple -> tuple.get(select), tuple -> tuple.get(report.id.count())));
    }

    @NonNull
    public <T extends Comparable> List<T> getFromReports(@NonNull Predicate where, @NonNull ComparableExpressionBase<T> select) {
        return getFromReports(where, select, select);
    }

    @NonNull
    public <T> List<T> getFromReports(@NonNull Predicate where, @NonNull Expression<T> select, ComparableExpressionBase<?> order) {
        return ((JPAQuery<?>) new JPAQuery<>(entityManager)).from(report).where(where).select(select).distinct().orderBy(order.asc()).fetch();
    }
}
