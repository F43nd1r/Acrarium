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
import com.faendir.acra.model.Permission;
import com.faendir.acra.model.ProguardMapping;
import com.faendir.acra.model.QApp;
import com.faendir.acra.model.Report;
import com.faendir.acra.model.Stacktrace;
import com.faendir.acra.model.User;
import com.faendir.acra.model.view.Queries;
import com.faendir.acra.model.view.VApp;
import com.faendir.acra.model.view.VBug;
import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.util.PlainTextUser;
import com.faendir.acra.util.Utils;
import com.mysema.commons.lang.CloseableIterator;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.core.types.dsl.EnumPath;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPADeleteClause;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAUpdateClause;
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
import javax.persistence.Query;
import javax.validation.constraints.Size;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.faendir.acra.model.QApp.app;
import static com.faendir.acra.model.QAttachment.attachment;
import static com.faendir.acra.model.QBug.bug;
import static com.faendir.acra.model.QPermission.permission;
import static com.faendir.acra.model.QProguardMapping.proguardMapping;
import static com.faendir.acra.model.QReport.report;
import static com.faendir.acra.model.QStacktrace.stacktrace1;
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
        return new QueryDslDataProvider<>(new JPAQuery<>(entityManager).from(report).join(report.stacktrace, stacktrace1).where(stacktrace1.bug.eq(bug)).select(report));
    }

    @NonNull
    public QueryDslDataProvider<Report> getReportProvider(@NonNull App app) {
        return new QueryDslDataProvider<>(new JPAQuery<>(entityManager).from(report)
                .join(report.stacktrace, stacktrace1)
                .join(stacktrace1.bug, bug)
                .where(bug.app.eq(app))
                .select(report));
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
    @Transactional
    @NonNull
    public PlainTextUser createNewApp(@NonNull String name) {
        PlainTextUser user = userService.createReporterUser();
        store(new App(name, user));
        return user;
    }

    @Transactional
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
        bug = store(bug);
        new JPAUpdateClause(entityManager, stacktrace1).set(stacktrace1.bug, bug).where(stacktrace1.bug.in(list)).execute();
        list.forEach(this::delete);
    }

    @Transactional
    public void unmergeBug(@NonNull Bug bug) {
        getStacktraces(bug).forEach(stacktrace -> stacktrace.setBug(new Bug(bug.getApp(), stacktrace.getStacktrace())));
        delete(bug);
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
    public Optional<Bug> findBug(@NonNull String encodedId) {
        try {
            return Optional.ofNullable(new JPAQuery<>(entityManager).from(bug).join(bug.app).fetchJoin().where(bug.id.eq(Integer.parseInt(encodedId))).select(bug).fetchOne());
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    @NonNull
    public Optional<Bug> findBug(int id) {
        return Optional.ofNullable(new JPAQuery<>(entityManager).from(bug).where(bug.id.eq(id)).select(bug).fetchOne());
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

    public Optional<Stacktrace> findStacktrace(@NonNull String stacktrace) {
        return Optional.ofNullable(new JPAQuery<>(entityManager).from(stacktrace1).where(stacktrace1.stacktrace.eq(stacktrace)).select(stacktrace1).fetchOne());
    }

    private void deleteOrphanBugs() {
        new JPADeleteClause(entityManager, bug).where(bug.notIn(JPAExpressions.select(stacktrace1.bug).from(stacktrace1).distinct())).execute();
    }

    private Optional<Bug> findBug(@NonNull App app, @NonNull String stacktrace) {
        return Optional.ofNullable(new JPAQuery<>(entityManager).from(stacktrace1)
                .join(stacktrace1.bug, bug)
                .where(bug.app.eq(app).and(stacktrace1.stacktrace.like(stacktrace)))
                .select(bug)
                .fetchFirst());
    }

    @Transactional
    public void changeConfiguration(@NonNull App app, @NonNull App.Configuration configuration) {
        app.setConfiguration(configuration);
        app = store(app);
        CloseableIterator<Stacktrace> iterator = new JPAQuery<>(entityManager).from(stacktrace1).join(stacktrace1.bug, bug).where(bug.app.eq(app)).select(stacktrace1).iterate();
        while (iterator.hasNext()) {
            Stacktrace stacktrace = iterator.next();
            String generifiedStacktrace = Utils.generifyStacktrace(stacktrace.getStacktrace(), app.getConfiguration());
            Optional<Bug> bug = findBug(app, generifiedStacktrace);
            if (!bug.isPresent()) {
                stacktrace.setBug(new Bug(app, generifiedStacktrace));
                store(generifiedStacktrace);
                entityManager.flush();
            } else if (!bug.get().equals(stacktrace.getBug())) {
                stacktrace.setBug(bug.get());
                store(generifiedStacktrace);
            }
        }
        iterator.close();
        entityManager.flush();
        deleteOrphanBugs();
    }

    @Transactional
    public void deleteReportsOlderThanDays(@NonNull App app, @NonNull int days) {
        new JPADeleteClause(entityManager, report).where(report.stacktrace.bug.app.eq(app).and(report.date.before(LocalDateTime.now().minus(days, ChronoUnit.DAYS))));
        entityManager.flush();
        deleteOrphanBugs();
    }

    @Transactional
    public void deleteReportsBeforeVersion(@NonNull App app, int versionCode) {
        new JPADeleteClause(entityManager, report).where(report.stacktrace.bug.app.eq(app).and(report.stacktrace.versionCode.lt(versionCode)));
        entityManager.flush();
        deleteOrphanBugs();
    }

    @Transactional
    public void createNewReport(@NonNull String reporterUserName, @NonNull String content, @NonNull List<MultipartFile> attachments) {
        App app = new JPAQuery<>(entityManager).from(QApp.app).where(QApp.app.reporter.username.eq(reporterUserName)).select(QApp.app).fetchOne();
        if (app != null) {
            JSONObject jsonObject = new JSONObject(content);
            String generifiedStacktrace = Utils.generifyStacktrace(jsonObject.optString(ReportField.STACK_TRACE.name()), app.getConfiguration());
            Stacktrace stacktrace = findStacktrace(generifiedStacktrace).orElseGet(() -> new Stacktrace(findBug(app, generifiedStacktrace).orElseGet(() -> new Bug(app,
                    generifiedStacktrace)),
                    generifiedStacktrace,
                    jsonObject.optInt(ReportField.APP_VERSION_CODE.name()),
                    jsonObject.optString(ReportField.APP_VERSION_NAME.name())));
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
        }
    }

    public <T> Map<T, Long> countReports(@NonNull Predicate where, @NonNull Expression<T> select) {
        List<Tuple> result = ((JPAQuery<?>) new JPAQuery<>(entityManager)).from(report).where(where).groupBy(select).select(select, report.id.count()).fetch();
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

    @NonNull
    public List<Stacktrace> getStacktraces(@NonNull Bug bug) {
        return new JPAQuery<>(entityManager).from(stacktrace1).where(stacktrace1.bug.eq(bug)).select(stacktrace1).fetch();
    }

    @NonNull
    public Optional<Integer> getMaximumMappingVersion(@NonNull App app) {
        return Optional.ofNullable(new JPAQuery<>(entityManager).from(proguardMapping).where(proguardMapping.app.eq(app)).select(proguardMapping.versionCode.max()).fetchOne());
    }
}
