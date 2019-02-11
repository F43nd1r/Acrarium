/*
 * (C) Copyright 2018 Lukas Morawietz (https://github.com/F43nd1r)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.faendir.acra.service;

import com.faendir.acra.dataprovider.QueryDslDataProvider;
import com.faendir.acra.model.App;
import com.faendir.acra.model.Attachment;
import com.faendir.acra.model.Bug;
import com.faendir.acra.model.QApp;
import com.faendir.acra.model.Report;
import com.faendir.acra.model.Stacktrace;
import com.faendir.acra.model.Version;
import com.faendir.acra.model.view.Queries;
import com.faendir.acra.model.view.VApp;
import com.faendir.acra.model.view.VBug;
import com.faendir.acra.model.view.WhereExpressions;
import com.faendir.acra.util.ImportResult;
import com.faendir.acra.util.PlainTextUser;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.jpa.impl.JPADeleteClause;
import com.querydsl.jpa.impl.JPAQuery;
import org.acra.ReportField;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ektorp.CouchDbConnector;
import org.ektorp.http.HttpClient;
import org.ektorp.http.StdHttpClient;
import org.ektorp.impl.StdCouchDbConnector;
import org.ektorp.impl.StdCouchDbInstance;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.faendir.acra.model.QApp.app;
import static com.faendir.acra.model.QAttachment.attachment;
import static com.faendir.acra.model.QBug.bug;
import static com.faendir.acra.model.QReport.report;
import static com.faendir.acra.model.QStacktrace.stacktrace1;
import static com.faendir.acra.model.QVersion.version;

/**
 * @author lukas
 * @since 16.05.18
 */
@Service
public class DataService implements Serializable {
    @NonNull
    private final Log log = LogFactory.getLog(getClass());
    @NonNull
    private final UserService userService;
    @NonNull
    private final EntityManager entityManager;
    @NonNull
    private final ApplicationEventPublisher applicationEventPublisher;
    private final Object stacktraceLock = new Object();

    @Autowired
    public DataService(@NonNull UserService userService, @NonNull EntityManager entityManager, @NonNull ApplicationEventPublisher applicationEventPublisher) {
        this.userService = userService;
        this.entityManager = entityManager;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @NonNull
    public QueryDslDataProvider<VApp> getAppProvider() {
        BooleanExpression where = WhereExpressions.whereHasAppPermission();
        return new QueryDslDataProvider<>(Queries.selectVApp(entityManager).where(where), new JPAQuery<>(entityManager).from(app).where(where));
    }

    @NonNull
    public List<Integer> getAppIds() {
        return new JPAQuery<>(entityManager).from(app).where(WhereExpressions.whereHasAppPermission()).select(app.id).fetch();
    }

    @NonNull
    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasPermission(#app, T(com.faendir.acra.model.Permission$Level).VIEW)")
    public QueryDslDataProvider<VBug> getBugProvider(@NonNull App app, BooleanSupplier onlyNonSolvedProvider) {
        Supplier<BooleanExpression> whereSupplier = () -> onlyNonSolvedProvider.getAsBoolean() ? bug.app.eq(app).and(bug.solvedVersion.isNull()) : bug.app.eq(app);
        return new QueryDslDataProvider<>(() -> Queries.selectVBug(entityManager).where(whereSupplier.get()),
                () -> new JPAQuery<>(entityManager).from(bug).where(whereSupplier.get()));
    }

    @NonNull
    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasPermission(#app, T(com.faendir.acra.model.Permission$Level).VIEW)")
    public List<Integer> getBugIds(@NonNull App app) {
        return new JPAQuery<>(entityManager).from(bug).where(bug.app.eq(app)).select(bug.id).fetch();
    }

    @NonNull
    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasPermission(#bug.app, T(com.faendir.acra.model.Permission$Level).VIEW)")
    public List<Integer> getStacktraceIds(@NonNull Bug bug) {
        return new JPAQuery<>(entityManager).from(stacktrace1).where(stacktrace1.bug.eq(bug)).select(stacktrace1.id).fetch();
    }

    @NonNull
    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasPermission(#bug.app, T(com.faendir.acra.model.Permission$Level).VIEW)")
    public QueryDslDataProvider<Report> getReportProvider(@NonNull Bug bug) {
        return new QueryDslDataProvider<>(new JPAQuery<>(entityManager).from(report)
                .join(report.stacktrace, stacktrace1)
                .fetchJoin()
                .where(stacktrace1.bug.eq(bug))
                .select(report));
    }

    @NonNull
    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasPermission(#app, T(com.faendir.acra.model.Permission$Level).VIEW)")
    public QueryDslDataProvider<Report> getReportProvider(@NonNull App app) {
        return new QueryDslDataProvider<>(new JPAQuery<>(entityManager).from(report)
                .join(report.stacktrace, stacktrace1)
                .fetchJoin()
                .join(stacktrace1.bug, bug)
                .where(bug.app.eq(app))
                .select(report));
    }

    @NonNull
    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasPermission(#stacktrace.bug.app, T(com.faendir.acra.model.Permission$Level).VIEW)")
    public List<String> getReportIds(@NonNull Stacktrace stacktrace) {
        return new JPAQuery<>(entityManager).from(report).where(report.stacktrace.eq(stacktrace)).select(report.id).fetch();
    }

    @NonNull
    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasPermission(#app, T(com.faendir.acra.model.Permission$Level).VIEW)")
    public List<String> getReportIds(@NonNull App app, @Nullable ZonedDateTime before, @Nullable ZonedDateTime after) {
        BooleanExpression where = report.stacktrace.bug.app.eq(app);
        if (before != null) {
            where = where.and(report.date.before(before));
        }
        if (after != null) {
            where = where.and(report.date.after(after));
        }
        return new JPAQuery<>(entityManager).from(report).where(where).select(report.id).fetch();
    }

    @NonNull
    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasPermission(#app, T(com.faendir.acra.model.Permission$Level).VIEW)")
    public QueryDslDataProvider<Version> getVersionProvider(@NonNull App app) {
        return new QueryDslDataProvider<>(new JPAQuery<>(entityManager).from(version).where(version.app.eq(app)).select(version));
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
    @Transactional
    @NonNull
    @PreAuthorize("hasRole(T(com.faendir.acra.model.User$Role).ADMIN)")
    public PlainTextUser createNewApp(@NonNull String name) {
        PlainTextUser user = userService.createReporterUser();
        store(new App(name, user));
        return user;
    }

    @Transactional
    @NonNull
    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasPermission(#app, T(com.faendir.acra.model.Permission$Level).ADMIN)")
    public PlainTextUser recreateReporterUser(@NonNull App app) {
        PlainTextUser user = userService.createReporterUser();
        app.setReporter(user);
        store(app);
        return user;
    }

    @Transactional
    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasPermission(#bug.app, T(com.faendir.acra.model.Permission$Level).EDIT)")
    public void unmergeBug(@NonNull Bug bug) {
        getStacktraces(bug).forEach(stacktrace -> stacktrace.setBug(new Bug(bug.getApp(), stacktrace.getStacktrace())));
        delete(bug);
    }

    @Transactional
    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasPermission(#bug.app, T(com.faendir.acra.model.Permission$Level).EDIT)")
    public void setBugSolved(@NonNull Bug bug, Version solved) {
        bug.setSolvedVersion(solved);
        store(bug);
    }

    /*@NonNull
    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasPermission(#app, T(com.faendir.acra.model.Permission$Level).VIEW)")
    public Optional<ProguardMapping> findMapping(@NonNull App app, int versionCode) {
        return Optional.ofNullable(new JPAQuery<>(entityManager).from(proguardMapping)
                .where(proguardMapping.app.eq(app).and(proguardMapping.versionCode.eq(versionCode)))
                .select(proguardMapping)
                .fetchOne());
    }*/

    @NonNull
    @PostAuthorize("!returnObject.isPresent() || T(com.faendir.acra.security.SecurityUtils).hasPermission(returnObject.get().stacktrace.bug.app, T(com.faendir.acra.model.Permission$Level).VIEW)")
    public Optional<Report> findReport(@NonNull String id) {
        return Optional.ofNullable(new JPAQuery<>(entityManager).from(report)
                .join(report.stacktrace, stacktrace1)
                .fetchJoin()
                .join(stacktrace1.bug, bug)
                .fetchJoin()
                .join(bug.app, app)
                .fetchJoin()
                .where(report.id.eq(id))
                .select(report)
                .fetchOne());
    }

    @NonNull
    @PostAuthorize("!returnObject.isPresent() || T(com.faendir.acra.security.SecurityUtils).hasPermission(returnObject.get().app, T(com.faendir.acra.model.Permission$Level).VIEW)")
    public Optional<Bug> findBug(@NonNull int id) {
        return Optional.ofNullable(new JPAQuery<>(entityManager).from(bug).join(bug.app).fetchJoin().where(bug.id.eq(id)).select(bug).fetchOne());
    }

    @NonNull
    @PostAuthorize("!returnObject.isPresent() || T(com.faendir.acra.security.SecurityUtils).hasPermission(returnObject.get(), T(com.faendir.acra.model.Permission$Level).VIEW)")
    public Optional<App> findApp(@NonNull String encodedId) {
        try {
            return findApp(Integer.parseInt(encodedId));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    @NonNull
    @PostAuthorize("!returnObject.isPresent() || T(com.faendir.acra.security.SecurityUtils).hasPermission(returnObject.get(), T(com.faendir.acra.model.Permission$Level).VIEW)")
    public Optional<App> findApp(int id) {
        return Optional.ofNullable(new JPAQuery<>(entityManager).from(app).where(app.id.eq(id)).select(app).fetchOne());
    }

    @NonNull
    @PostFilter("hasRole(T(com.faendir.acra.model.User$Role).ADMIN) || T(com.faendir.acra.security.SecurityUtils).hasPermission(filterObject, T(com.faendir.acra.model.Permission$Level).VIEW)")
    public List<App> findAllApps() {
        return new JPAQuery<>(entityManager).from(app).select(app).fetch();
    }

    @NonNull
    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasPermission(#report.stacktrace.bug.app, T(com.faendir.acra.model.Permission$Level).VIEW)")
    public List<Attachment> findAttachments(@NonNull Report report) {
        return new JPAQuery<>(entityManager).from(attachment).where(attachment.report.eq(report)).select(attachment).fetch();
    }

    @PostAuthorize("!returnObject.isPresent() || T(com.faendir.acra.security.SecurityUtils).hasPermission(returnObject.get().bug.app, T(com.faendir.acra.model.Permission$Level).VIEW)")
    public Optional<Stacktrace> findStacktrace(int id) {
        return Optional.ofNullable(new JPAQuery<>(entityManager).from(stacktrace1).where(stacktrace1.id.eq(id)).select(stacktrace1).fetchOne());
    }

    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasPermission(#app, T(com.faendir.acra.model.Permission$Level).VIEW)")
    public Optional<Stacktrace> findStacktrace(@NonNull App app, @NonNull String stacktrace, int versionCode) {
        return Optional.ofNullable(new JPAQuery<>(entityManager).from(stacktrace1)
                .where(stacktrace1.stacktrace.eq(stacktrace).and(stacktrace1.version.code.eq(versionCode)).and(stacktrace1.bug.app.eq(app)))
                .select(stacktrace1)
                .fetchOne());
    }

    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasPermission(#app, T(com.faendir.acra.model.Permission$Level).VIEW)")
    private Optional<Bug> findBug(@NonNull App app, @NonNull String stacktrace) {
        return Optional.ofNullable(new JPAQuery<>(entityManager).from(stacktrace1)
                .join(stacktrace1.bug, bug)
                .where(bug.app.eq(app).and(stacktrace1.stacktrace.like(stacktrace)))
                .select(bug)
                .fetchFirst());
    }

    @NonNull
    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasPermission(#app, T(com.faendir.acra.model.Permission$Level).VIEW)")
    public List<Version> findAllVersions(@NonNull App app) {
        return new JPAQuery<>(entityManager).from(version).where(version.app.eq(app)).select(version).fetch();
    }

    @Transactional
    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasPermission(#app, T(com.faendir.acra.model.Permission$Level).EDIT)")
    public void changeConfiguration(@NonNull App app, @NonNull App.Configuration configuration) {
        app.setConfiguration(configuration);
        app = store(app);
        entityManager.flush();
        applicationEventPublisher.publishEvent(new ConfigurationUpdateEvent(this, app));

    }

    @Transactional
    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasPermission(#app, T(com.faendir.acra.model.Permission$Level).EDIT)")
    public void deleteReportsOlderThanDays(@NonNull App app, @NonNull int days) {
        new JPADeleteClause(entityManager, report).where(report.stacktrace.bug.app.eq(app).and(report.date.before(ZonedDateTime.now().minus(days, ChronoUnit.DAYS))));
        entityManager.flush();
        applicationEventPublisher.publishEvent(new ReportsDeleteEvent(this));
    }

    @Transactional
    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasPermission(#app, T(com.faendir.acra.model.Permission$Level).EDIT)")
    public void deleteReportsBeforeVersion(@NonNull App app, int versionCode) {
        new JPADeleteClause(entityManager, report).where(report.stacktrace.bug.app.eq(app).and(report.stacktrace.version.code.lt(versionCode)));
        entityManager.flush();
        applicationEventPublisher.publishEvent(new ReportsDeleteEvent(this));
    }

    @Transactional
    @PreAuthorize("hasRole(T(com.faendir.acra.model.User$Role).REPORTER)")
    public void createNewReport(@NonNull String reporterUserName, @NonNull String content, @NonNull List<MultipartFile> attachments) {
        App app = new JPAQuery<>(entityManager).from(QApp.app).where(QApp.app.reporter.username.eq(reporterUserName)).select(QApp.app).fetchOne();
        if (app != null) {
            JSONObject jsonObject = new JSONObject(content);
            String trace = jsonObject.optString(ReportField.STACK_TRACE.name());
            Version version = getVersion(app, jsonObject);
            Stacktrace stacktrace = findStacktrace(app, trace, version.getCode()).orElseGet(() -> {
                synchronized (stacktraceLock) {
                    return findStacktrace(app, trace, version.getCode()).orElseGet(() -> store(new Stacktrace(findBug(app, trace).orElseGet(() -> new Bug(app, trace)), trace, version)));
                }
            });
            Report report = store(new Report(stacktrace, content));
            attachments.forEach(multipartFile -> {
                try {
                    store(new Attachment(report,
                            multipartFile.getName(),
                            Hibernate.getLobCreator(entityManager.unwrap(Session.class)).createBlob(multipartFile.getInputStream(), multipartFile.getSize())));
                } catch (IOException e) {
                    log.warn("Failed to load attachment with name " + multipartFile.getName(), e);
                }
            });
            entityManager.flush();
            applicationEventPublisher.publishEvent(new NewReportEvent(this, report));
        }
    }

    private Version getVersion(App app, JSONObject jsonObject) {
        JSONObject buildConfig = jsonObject.optJSONObject(ReportField.BUILD_CONFIG.name());
        Integer versionCode = null;
        String versionName = null;
        if (buildConfig != null) {
            try {
                versionCode = buildConfig.getInt("VERSION_CODE");
            } catch (Exception ignored) {
            }
            try {
                versionName = buildConfig.getString("VERSION_NAME");
            } catch (Exception ignored) {
            }
        }
        if (versionCode == null) {
            versionCode = jsonObject.optInt(ReportField.APP_VERSION_CODE.name());
        }
        if (versionName == null) {
            versionName = jsonObject.optString(ReportField.APP_VERSION_NAME.name(), "N/A");
        }
        return new Version(app, versionCode, versionName);
    }

    @NonNull
    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasPermission(#app, T(com.faendir.acra.model.Permission$Level).VIEW)")
    public <T> Map<T, Long> countReports(@NonNull App app, @Nullable Predicate where, @NonNull Expression<T> select) {
        List<Tuple> result = ((JPAQuery<?>) new JPAQuery<>(entityManager)).from(report)
                .where(report.stacktrace.bug.app.eq(app).and(where))
                .groupBy(select)
                .select(select, report.id.count())
                .fetch();
        return result.stream().collect(Collectors.toMap(tuple -> tuple.get(select), tuple -> Optional.ofNullable(tuple.get(report.id.count())).orElse(0L)));
    }

    @NonNull
    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasPermission(#app, T(com.faendir.acra.model.Permission$Level).VIEW)")
    public <T extends Comparable> List<T> getFromReports(@NonNull App app, @Nullable Predicate where, @NonNull ComparableExpressionBase<T> select) {
        return getFromReports(app, where, select, select);
    }

    @NonNull
    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasPermission(#app, T(com.faendir.acra.model.Permission$Level).VIEW)")
    public <T> List<T> getFromReports(@NonNull App app, @Nullable Predicate where, @NonNull Expression<T> select, ComparableExpressionBase<?> order) {
        return ((JPAQuery<?>) new JPAQuery<>(entityManager)).from(report)
                .where(report.stacktrace.bug.app.eq(app).and(where))
                .select(select)
                .distinct()
                .orderBy(order.asc())
                .fetch();
    }

    @NonNull
    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasPermission(#bug.app, T(com.faendir.acra.model.Permission$Level).VIEW)")
    public List<Stacktrace> getStacktraces(@NonNull Bug bug) {
        return new JPAQuery<>(entityManager).from(stacktrace1).where(stacktrace1.bug.eq(bug)).select(stacktrace1).fetch();
    }

    @NonNull
    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasPermission(#app, T(com.faendir.acra.model.Permission$Level).VIEW)")
    public Optional<Integer> getMaxVersion(@NonNull App app) {
        return Optional.ofNullable(new JPAQuery<>(entityManager).from(version).where(version.app.eq(app)).select(version.code.max()).fetchOne());
    }

    @Transactional
    @PreAuthorize("hasRole(T(com.faendir.acra.model.User$Role).ADMIN)")
    public ImportResult importFromAcraStorage(String host, int port, boolean ssl, String database) {
        HttpClient httpClient = new StdHttpClient.Builder().host(host).port(port).enableSSL(ssl).build();
        CouchDbConnector db = new StdCouchDbConnector(database, new StdCouchDbInstance(httpClient));
        PlainTextUser user = createNewApp(database.replaceFirst("acra-", ""));
        int total = 0;
        int success = 0;
        for (String id : db.getAllDocIds()) {
            if (!id.startsWith("_design")) {
                total++;
                try {
                    JSONObject report = new JSONObject(IOUtils.toString(db.getAsStream(id), StandardCharsets.UTF_8));
                    fixStringIsArray(report, ReportField.STACK_TRACE);
                    fixStringIsArray(report, ReportField.LOGCAT);
                    createNewReport(user.getUsername(), report.toString(), Collections.emptyList());
                    success++;
                } catch (Exception ignored) {
                }
            }
        }
        httpClient.shutdown();
        return new ImportResult(user, total, success);
    }

    private void fixStringIsArray(JSONObject report, ReportField reportField) {
        Optional.ofNullable(report.optJSONArray(reportField.name()))
                .ifPresent(array -> report.put(reportField.name(),
                        StreamSupport.stream(array.spliterator(), false).filter(String.class::isInstance).map(String.class::cast).collect(Collectors.joining("\n"))));
    }
}
