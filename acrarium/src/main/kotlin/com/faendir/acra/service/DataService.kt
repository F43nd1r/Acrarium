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
package com.faendir.acra.service

import com.faendir.acra.dataprovider.QueryDslDataProvider
import com.faendir.acra.model.*
import com.faendir.acra.model.view.Queries
import com.faendir.acra.model.view.VApp
import com.faendir.acra.model.view.VReport
import com.faendir.acra.model.view.WhereExpressions.whereHasAppPermission
import com.faendir.acra.util.*
import com.querydsl.core.types.Expression
import com.querydsl.core.types.Predicate
import com.querydsl.core.types.dsl.ComparableExpressionBase
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.impl.JPADeleteClause
import com.querydsl.jpa.impl.JPAQuery
import com.querydsl.jpa.impl.JPAUpdateClause
import mu.KotlinLogging
import org.acra.ReportField
import org.ektorp.CouchDbConnector
import org.ektorp.http.StdHttpClient
import org.ektorp.impl.StdCouchDbConnector
import org.ektorp.impl.StdCouchDbInstance
import org.hibernate.Hibernate
import org.hibernate.engine.spi.SessionImplementor
import org.intellij.lang.annotations.Language
import org.json.JSONObject
import org.springframework.context.ApplicationEventPublisher
import org.springframework.security.access.prepost.PostAuthorize
import org.springframework.security.access.prepost.PostFilter
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.io.Serializable
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import javax.persistence.EntityManager

private val logger = KotlinLogging.logger {}

/**
 * @author lukas
 * @since 16.05.18
 */
@Service
class DataService(
    private val userService: UserService, private val entityManager: EntityManager,
    private val applicationEventPublisher: ApplicationEventPublisher
) : Serializable {
    private val stacktraceLock = Any()

    fun getAppProvider(): QueryDslDataProvider<VApp> {
        val where = whereHasAppPermission()
        return QueryDslDataProvider(Queries.selectVApp(entityManager).where(where), JPAQuery<Any>(entityManager).from(QApp.app).where(where))
    }

    fun getAppIds(): List<Int> = JPAQuery<Any>(entityManager).from(QApp.app).where(whereHasAppPermission()).select(QApp.app.id).fetch()

    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasPermission(#app, T(com.faendir.acra.model.Permission\$Level).VIEW)")
    fun getBugProvider(app: App) = QueryDslDataProvider(Queries.selectVBug(entityManager).where(QBug.bug.app.eq(app)))

    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasPermission(#app, T(com.faendir.acra.model.Permission\$Level).VIEW)")
    fun getBugIds(app: App): List<Int> = JPAQuery<Any>(entityManager).from(QBug.bug).where(QBug.bug.app.eq(app)).select(QBug.bug.id).fetch()

    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasPermission(#bug.app, T(com.faendir.acra.model.Permission\$Level).VIEW)")
    fun getStacktraceIds(bug: Bug): List<Int> =
        JPAQuery<Any>(entityManager).from(QStacktrace.stacktrace1).where(QStacktrace.stacktrace1.bug.eq(bug)).select(QStacktrace.stacktrace1.id).fetch()

    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasPermission(#bug.app, T(com.faendir.acra.model.Permission\$Level).VIEW)")
    fun getReportProvider(bug: Bug) = QueryDslDataProvider(
        Queries.selectVReport(entityManager, bug.app)
            .where(QStacktrace.stacktrace1.bug.eq(bug))
    )

    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasPermission(#app, T(com.faendir.acra.model.Permission\$Level).VIEW)")
    fun getReportProvider(app: App) = QueryDslDataProvider(
        Queries.selectVReport(entityManager, app)
            .join(QStacktrace.stacktrace1.version).fetchJoin()
            .where(QApp.app.eq(app))
    )

    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasPermission(#stacktrace.bug.app, T(com.faendir.acra.model.Permission\$Level).VIEW)")
    fun getReportIds(stacktrace: Stacktrace): List<String> =
        JPAQuery<Any>(entityManager).from(QReport.report).where(QReport.report.stacktrace.eq(stacktrace)).select(QReport.report.id).fetch()

    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasPermission(#app, T(com.faendir.acra.model.Permission\$Level).VIEW)")
    fun getReportIds(app: App, before: ZonedDateTime?, after: ZonedDateTime?): List<String> {
        var where = QReport.report.stacktrace.bug.app.eq(app)
        before?.let { where = where.and(QReport.report.date.before(it)) }
        after?.let { where = where.and(QReport.report.date.after(it)) }
        return JPAQuery<Any>(entityManager).from(QReport.report).where(where).select(QReport.report.id).fetch()
    }

    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasPermission(#app, T(com.faendir.acra.model.Permission\$Level).VIEW)")
    fun getVersionProvider(app: App) =
        QueryDslDataProvider(JPAQuery<Any>(entityManager).from(QVersion.version).fetchAll().where(QVersion.version.app.eq(app)).select(QVersion.version))

    @Transactional
    fun <T> store(entity: T): T = entityManager.merge(entity)

    private fun delete(entity: Any) {
        entityManager.remove(if (entityManager.contains(entity)) entity else entityManager.merge(entity))
    }

    @Transactional
    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasPermission(#version.app, T(com.faendir.acra.model.Permission\$Level).EDIT)")
    fun storeVersion(version: Version): Version = store(version)

    @Transactional
    fun deleteVersion(version: Version) {
        JPAUpdateClause(entityManager, QBug.bug).set(QBug.bug.solvedVersion, null as Version?).where(QBug.bug.solvedVersion.eq(version))
        delete(version)
        applicationEventPublisher.publishEvent(ReportsDeleteEvent(this))
    }

    @Transactional
    fun deleteReport(report: VReport) {
        JPADeleteClause(entityManager, QReport.report).where(QReport.report.id.eq(report.id)).execute()
        applicationEventPublisher.publishEvent(ReportsDeleteEvent(this))
    }

    @Transactional
    fun deleteBug(bug: Bug) {
        delete(bug)
    }

    @Transactional
    fun deleteApp(app: App) {
        delete(app)
    }

    /**
     * Creates a new app
     *
     * @param name the name of the new app
     * @return the name of the reporter user and its password (plaintext)
     */
    @Transactional
    @PreAuthorize("hasRole(T(com.faendir.acra.model.User\$Role).ADMIN)")
    fun createNewApp(name: String): User {
        val user = userService.createReporterUser()
        store(App(name, user))
        return user
    }

    @Transactional
    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasPermission(#app, T(com.faendir.acra.model.Permission\$Level).ADMIN)")
    fun recreateReporterUser(app: App): User {
        val user = userService.createReporterUser()
        app.reporter = user
        store(app)
        return user
    }

    @Transactional
    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasPermission(#bug.app, T(com.faendir.acra.model.Permission\$Level).EDIT)")
    fun unmergeBug(bug: Bug) {
        getStacktraces(bug).forEach {
            it.bug = Bug(bug.app, it.stacktrace)
            store(it)
        }
        delete(bug)
    }

    @Transactional
    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasPermission(#stacktrace.bug.app, T(com.faendir.acra.model.Permission\$Level).EDIT)")
    fun unmergeStacktrace(stacktrace: Stacktrace) {
        stacktrace.bug = Bug(stacktrace.bug.app, stacktrace.stacktrace)
        store(stacktrace)
    }

    @Transactional
    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasPermission(#bug.app, T(com.faendir.acra.model.Permission\$Level).EDIT)")
    fun setBugSolved(bug: Bug, solved: Version?) {
        bug.solvedVersion = solved
        store(bug)
    }

    @PostAuthorize(
        "returnObject == null || T(com.faendir.acra.security.SecurityUtils).hasPermission(returnObject.stacktrace.bug.app, T(com.faendir.acra.model.Permission\$Level).VIEW)"
    )
    fun findReport(id: String): Report? = JPAQuery<Any>(entityManager).from(QReport.report)
        .join(QReport.report.stacktrace, QStacktrace.stacktrace1)
        .fetchJoin()
        .join(QStacktrace.stacktrace1.version, QVersion.version)
        .fetchAll()
        .fetchJoin()
        .join(QStacktrace.stacktrace1.bug, QBug.bug)
        .fetchJoin()
        .join(QBug.bug.app, QApp.app)
        .fetchJoin()
        .where(QReport.report.id.eq(id))
        .select(QReport.report)
        .fetchOne()

    @PostAuthorize(
        "returnObject == null || T(com.faendir.acra.security.SecurityUtils).hasPermission(returnObject.app, T(com.faendir.acra.model.Permission\$Level).VIEW)"
    )
    fun findBug(id: Int): Bug? =
        JPAQuery<Any>(entityManager).from(QBug.bug).join(QBug.bug.app).fetchJoin().where(QBug.bug.id.eq(id)).select(QBug.bug).fetchOne()

    @PostAuthorize(
        "returnObject == null || T(com.faendir.acra.security.SecurityUtils).hasPermission(returnObject, T(com.faendir.acra.model.Permission\$Level).VIEW)"
    )
    fun findApp(encodedId: String): App? = tryOrNull { findApp(encodedId.toInt()) }

    @PostAuthorize(
        "returnObject == null || T(com.faendir.acra.security.SecurityUtils).hasPermission(returnObject, T(com.faendir.acra.model.Permission\$Level).VIEW)"
    )
    fun findApp(id: Int): App? = JPAQuery<Any>(entityManager).from(QApp.app).where(QApp.app.id.eq(id)).select(QApp.app).fetchOne()

    @PostFilter(
        "hasRole(T(com.faendir.acra.model.User\$Role).ADMIN) || T(com.faendir.acra.security.SecurityUtils).hasPermission(filterObject, T(com.faendir.acra.model.Permission\$Level).VIEW)"
    )
    fun findAllApps(): List<App> = JPAQuery<Any>(entityManager).from(QApp.app).select(QApp.app).fetch()

    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasPermission(#report.stacktrace.bug.app, T(com.faendir.acra.model.Permission\$Level).VIEW)")
    fun findAttachments(report: Report): List<Attachment> =
        JPAQuery<Any>(entityManager).from(QAttachment.attachment).where(QAttachment.attachment.report.eq(report)).select(QAttachment.attachment).fetch()

    @PostAuthorize(
        "returnObject == null || T(com.faendir.acra.security.SecurityUtils).hasPermission(returnObject.bug.app, T(com.faendir.acra.model.Permission\$Level).VIEW)"
    )
    fun findStacktrace(id: Int): Stacktrace? =
        JPAQuery<Any>(entityManager).from(QStacktrace.stacktrace1).where(QStacktrace.stacktrace1.id.eq(id)).select(QStacktrace.stacktrace1).fetchOne()

    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasPermission(#app, T(com.faendir.acra.model.Permission\$Level).VIEW)")
    fun findStacktrace(app: App, stacktrace: String, versionCode: Int): Stacktrace? = JPAQuery<Any>(entityManager).from(QStacktrace.stacktrace1)
        .where(
            QStacktrace.stacktrace1.stacktrace.eq(stacktrace)
                .and(QStacktrace.stacktrace1.version.code.eq(versionCode))
                .and(QStacktrace.stacktrace1.bug.app.eq(app))
        )
        .select(QStacktrace.stacktrace1)
        .fetchOne()

    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasPermission(#app, T(com.faendir.acra.model.Permission\$Level).VIEW)")
    fun findVersion(app: App, versionCode: Int): Version? = JPAQuery<Any>(entityManager).from(QVersion.version)
        .where(QVersion.version.code.eq(versionCode).and(QVersion.version.app.eq(app)))
        .select(QVersion.version)
        .fetchOne()

    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasPermission(#app, T(com.faendir.acra.model.Permission\$Level).VIEW)")
    private fun findBug(app: App, stacktrace: String): Bug? = JPAQuery<Any>(entityManager).from(QStacktrace.stacktrace1)
        .join(QStacktrace.stacktrace1.bug, QBug.bug)
        .leftJoin(QBug.bug.solvedVersion).fetchJoin()
        .where(QBug.bug.app.eq(app).and(QStacktrace.stacktrace1.stacktrace.like(stacktrace)))
        .select(QBug.bug)
        .fetchFirst()

    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasPermission(#app, T(com.faendir.acra.model.Permission\$Level).VIEW)")
    fun findAllVersions(app: App): List<Version> =
        JPAQuery<Any>(entityManager).from(QVersion.version).where(QVersion.version.app.eq(app)).select(QVersion.version).orderBy(QVersion.version.code.desc())
            .fetch()

    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasPermission(#app, T(com.faendir.acra.model.Permission\$Level).VIEW)")
    fun findMailSettings(app: App, user: User): MailSettings? = JPAQuery<Any>(entityManager).from(QMailSettings.mailSettings)
        .where(QMailSettings.mailSettings.app.eq(app).and(QMailSettings.mailSettings.user.eq(user)))
        .select(QMailSettings.mailSettings).fetchOne()

    @Transactional
    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasPermission(#a, T(com.faendir.acra.model.Permission\$Level).EDIT)")
    fun changeConfiguration(a: App, configuration: App.Configuration) {
        var app = a
        app.configuration = configuration
        store(app)
    }

    @Transactional
    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasPermission(#app, T(com.faendir.acra.model.Permission\$Level).EDIT)")
    fun deleteReportsOlderThanDays(app: App, days: Int) {
        JPADeleteClause(entityManager, QReport.report).where(
            QReport.report.stacktrace.`in`(
                JPAExpressions.select(QStacktrace.stacktrace1).from(QStacktrace.stacktrace1).where(QStacktrace.stacktrace1.bug.app.eq(app))
            )
                .and(QReport.report.date.before(ZonedDateTime.now().minus(days.toLong(), ChronoUnit.DAYS)))
        ).execute()
        entityManager.flush()
        applicationEventPublisher.publishEvent(ReportsDeleteEvent(this))
    }

    @Transactional
    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasPermission(#app, T(com.faendir.acra.model.Permission\$Level).EDIT)")
    fun deleteReportsBeforeVersion(app: App, versionCode: Int) {
        JPADeleteClause(entityManager, QReport.report).where(
            QReport.report.stacktrace.`in`(
                JPAExpressions.select(QStacktrace.stacktrace1).from(QStacktrace.stacktrace1).where(
                    QStacktrace.stacktrace1.bug.app.eq(app)
                        .and(QStacktrace.stacktrace1.version.code.lt(versionCode))
                )
            )
        ).execute()
        entityManager.flush()
        applicationEventPublisher.publishEvent(ReportsDeleteEvent(this))
    }

    @Transactional
    @PreAuthorize("hasRole(T(com.faendir.acra.model.User\$Role).REPORTER)")
    fun createNewReport(reporterUserName: String, @Language("JSON") content: String, attachments: List<MultipartFile>) {
        JPAQuery<Any>(entityManager).from(QApp.app).where(QApp.app.reporter.username.eq(reporterUserName)).select(QApp.app).fetchOne()?.let { app ->
            val jsonObject = JSONObject(content)
            val trace = jsonObject.optString(ReportField.STACK_TRACE.name)
            val version = getVersion(app, jsonObject)
            val stacktrace = findStacktrace(app, trace, version.code) ?: synchronized(stacktraceLock) {
                findStacktrace(app, trace, version.code) ?: store(Stacktrace(findBug(app, trace) ?: Bug(app, trace), trace, version))
            }
            val report = store(Report(stacktrace, content))
            attachments.forEach {
                try {
                    store(
                        Attachment(
                            report, it.originalFilename ?: it.name,
                            Hibernate.getLobCreator(entityManager.unwrap(SessionImplementor::class.java)).createBlob(it.inputStream, it.size)
                        )
                    )
                } catch (e: IOException) {
                    logger.warn(e) { "Failed to load attachment with name ${it.originalFilename}" }
                }
            }
            entityManager.flush()
            applicationEventPublisher.publishEvent(NewReportEvent(this, report))
        }
    }

    private fun getVersion(app: App, jsonObject: JSONObject): Version {
        val buildConfig: JSONObject? = jsonObject.optJSONObject(ReportField.BUILD_CONFIG.name)
        val versionCode: Int = buildConfig?.findInt("VERSION_CODE") ?: jsonObject.findInt(ReportField.APP_VERSION_CODE.name) ?: 0
        val versionName: String = buildConfig?.findString("VERSION_NAME") ?: jsonObject.findString(ReportField.APP_VERSION_NAME.name) ?: "N/A"
        return findVersion(app, versionCode) ?: (Version(app, versionCode, versionName))
    }

    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasPermission(#app, T(com.faendir.acra.model.Permission\$Level).VIEW)")
    fun <T> countReports(app: App, where: Predicate?, select: Expression<T>): Map<T, Long> {
        val result = (JPAQuery<Any>(entityManager) as JPAQuery<*>).from(QReport.report)
            .where(QReport.report.stacktrace.bug.app.eq(app).and(where))
            .groupBy(select)
            .select(select, QReport.report.id.count())
            .fetch()
        return result.associate { it[select]!! to (it[QReport.report.id.count()] ?: 0L) }
    }

    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasPermission(#app, T(com.faendir.acra.model.Permission\$Level).VIEW)")
    fun <T> getFromReports(app: App, select: Expression<T>, where: Predicate? = null, sorted: Boolean = true): List<T> =
        (JPAQuery<Any>(entityManager) as JPAQuery<*>).from(QReport.report).where(QReport.report.stacktrace.bug.app.eq(app).and(where))
            .select(select).apply { if (sorted && select is ComparableExpressionBase) orderBy(select.asc()) }.distinct().fetch()

    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasPermission(#bug.app, T(com.faendir.acra.model.Permission\$Level).VIEW)")
    fun getStacktraces(bug: Bug): List<Stacktrace> =
        JPAQuery<Any>(entityManager).from(QStacktrace.stacktrace1)
            .join(QStacktrace.stacktrace1.version, QVersion.version).fetchAll().fetchJoin()
            .join(QStacktrace.stacktrace1.bug).fetchJoin()
            .where(QStacktrace.stacktrace1.bug.eq(bug)).select(QStacktrace.stacktrace1).fetch()

    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasPermission(#app, T(com.faendir.acra.model.Permission\$Level).VIEW)")
    fun getMaxVersion(app: App): Int? =
        JPAQuery<Any>(entityManager).from(QVersion.version).where(QVersion.version.app.eq(app)).select(QVersion.version.code.max()).fetchOne()

    @Transactional
    @PreAuthorize("hasRole(T(com.faendir.acra.model.User\$Role).ADMIN)")
    fun importFromAcraStorage(host: String, port: Int, ssl: Boolean, database: String): ImportResult {
        val httpClient = StdHttpClient.Builder().host(host).port(port).enableSSL(ssl).build()
        val db: CouchDbConnector = StdCouchDbConnector(database, StdCouchDbInstance(httpClient))
        val user = createNewApp(database.replaceFirst("acra-".toRegex(), ""))
        var total = 0
        var success = 0
        for (id in db.allDocIds) {
            if (!id.startsWith("_design")) {
                total++
                catching {
                    val report = JSONObject(db.getAsStream(id).reader(Charsets.UTF_8).use { it.readText() })
                    fixStringIsArray(report, ReportField.STACK_TRACE)
                    fixStringIsArray(report, ReportField.LOGCAT)
                    createNewReport(user.username, report.toString(), emptyList())
                    success++
                }
            }
        }
        httpClient.shutdown()
        return ImportResult(user, total, success)
    }

    private fun fixStringIsArray(report: JSONObject, reportField: ReportField) {
        report.optJSONArray(reportField.name)?.let { report.put(reportField.name, it.filterIsInstance<String>().joinToString("\n")) }
    }

    @Transactional
    fun updateDeviceTable(devices: List<Device>) {
        devices.forEach { store(it) }
    }

    fun hasDeviceTableEntries(): Boolean {
        return JPAQuery<Any>(entityManager).from(QDevice.device1).select(Expressions.ONE).fetchFirst() != null
    }

}