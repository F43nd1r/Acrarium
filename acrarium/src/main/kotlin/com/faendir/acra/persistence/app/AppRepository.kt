package com.faendir.acra.persistence.app

import com.faendir.acra.dataprovider.AcrariumDataProvider
import com.faendir.acra.dataprovider.AcrariumSort
import com.faendir.acra.jooq.generated.Tables.APP
import com.faendir.acra.jooq.generated.Tables.APP_REPORT_COLUMNS
import com.faendir.acra.jooq.generated.Tables.REPORT
import com.faendir.acra.persistence.fetchList
import com.faendir.acra.persistence.fetchListInto
import com.faendir.acra.persistence.fetchValue
import com.faendir.acra.persistence.fetchValueInto
import com.faendir.acra.persistence.user.Permission
import com.faendir.acra.persistence.user.Role
import com.faendir.acra.persistence.user.UserRepository
import com.faendir.acra.security.SecurityUtils
import org.apache.commons.text.RandomStringGenerator
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.security.access.prepost.PostAuthorize
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class AppRepository(private val jooq: DSLContext, private val userRepository: UserRepository, private val randomStringGenerator: RandomStringGenerator) {

    @PreAuthorize("hasViewPermission(#id)")
    fun find(id: AppId): App? = jooq.selectFrom(APP).where(APP.ID.eq(id)).fetchValueInto<App>()

    @PreAuthorize("hasViewPermission(#id)")
    fun findName(id: AppId): String? = jooq.select(APP.NAME).from(APP).where(APP.ID.eq(id)).fetchValue()

    @PostAuthorize("isReporter()")
    fun findId(reporter: String): AppId? = jooq.select(APP.ID).from(APP).where(APP.REPORTER_USERNAME.eq(reporter)).fetchValue()

    @Transactional
    @PreAuthorize("isAdmin()")
    fun create(name: String): Reporter {
        val reporter = createReporterUser()
        jooq.insertInto(APP)
            .set(APP.REPORTER_USERNAME, reporter.username)
            .set(APP.NAME, name)
            .execute()
        return reporter
    }

    @Transactional
    @PreAuthorize("hasAdminPermission(#id)")
    fun recreateReporter(id: AppId): Reporter {
        val oldReporterUsername =
            jooq.select(APP.REPORTER_USERNAME).from(APP).where(APP.ID.eq(id)).fetchValue() ?: throw IllegalArgumentException("Can't recreate reporter for unknown app")
        val newReporter = createReporterUser()
        jooq.update(APP)
            .set(APP.REPORTER_USERNAME, newReporter.username)
            .where(APP.ID.eq(id))
            .execute()
        userRepository.delete(oldReporterUsername)
        return newReporter
    }

    private fun createReporterUser(): Reporter {
        var username: String
        do {
            username = randomStringGenerator.generate(16)
        } while (userRepository.exists(username))
        val password = randomStringGenerator.generate(16)
        if (!userRepository.create(username, password, null, Role.REPORTER)) throw RuntimeException("Failed to create reporter user")
        return Reporter(username, password)
    }

    @Transactional
    @PreAuthorize("hasAdminPermission(#id)")
    fun delete(id: AppId) {
        val reporterUsername = jooq.select(APP.REPORTER_USERNAME).from(APP).where(APP.ID.eq(id)).fetchValue() ?: return
        // app is cascade deleted
        userRepository.delete(reporterUsername)
    }

    @PreAuthorize("isUser()")
    fun getVisibleIds(): List<AppId> = jooq.select(APP.ID).from(APP).where(hasViewPermission()).fetchList()

    @PreAuthorize("isAdmin()")
    fun getAllNames(): List<AppName> = jooq.select(APP.ID, APP.NAME).from(APP).fetchListInto()

    @PreAuthorize("hasViewPermission(#id)")
    fun getCustomColumns(id: AppId): List<CustomColumn> =
        jooq.select(APP_REPORT_COLUMNS.NAME, APP_REPORT_COLUMNS.PATH).from(APP_REPORT_COLUMNS).where(APP_REPORT_COLUMNS.APP_ID.eq(id)).fetchListInto()

    @Transactional
    @PreAuthorize("hasEditPermission(#id)")
    fun setCustomColumns(id: AppId, customColumns: List<CustomColumn>) {
        jooq.deleteFrom(APP_REPORT_COLUMNS).where(APP_REPORT_COLUMNS.APP_ID.eq(id)).execute()
        for (customColumn in customColumns) {
            jooq.insertInto(APP_REPORT_COLUMNS)
                .set(APP_REPORT_COLUMNS.APP_ID, id)
                .set(APP_REPORT_COLUMNS.PATH, customColumn.path)
                .set(APP_REPORT_COLUMNS.NAME, customColumn.name)
                .execute()
        }
    }

    @PreAuthorize("isUser()")
    fun getProvider() = object : AcrariumDataProvider<AppStats, Nothing, AppStats.Sort>() {
        override fun fetch(filters: Set<Nothing>, sort: List<AcrariumSort<AppStats.Sort>>, offset: Int, limit: Int) = jooq.select(
            APP.ID,
            APP.NAME,
            DSL.count(REPORT.ID).`as`("REPORT_COUNT"),
            DSL.countDistinct(REPORT.BUG_ID).`as`("BUG_COUNT"),
        )
            .from(APP)
            .leftJoin(REPORT).on(REPORT.APP_ID.eq(APP.ID))
            .where(hasViewPermission())
            .groupBy(APP.ID)
            .offset(offset)
            .limit(limit)
            .fetchListInto<AppStats>()
            .stream()

        override fun size(filters: Set<Nothing>) = jooq.selectCount()
            .from(APP)
            .where(hasViewPermission())
            .fetchValue() ?: 0
    }

    private fun hasViewPermission(): Condition {
        val permissions = SecurityUtils.getAuthorities().filterIsInstance<Permission>()
        return if (SecurityUtils.hasRole(Role.ADMIN)) {
            APP.ID.notIn(permissions.filter { it.level == Permission.Level.NONE }.map { it.appId })
        } else {
            APP.ID.`in`(permissions.filter { it.level >= Permission.Level.VIEW }.map { it.appId })
        }
    }
}

data class Reporter(val username: String, val rawPassword: String)