/*
 * (C) Copyright 2022-2023 Lukas Morawietz (https://github.com/F43nd1r)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.faendir.acra.persistence.app

import com.faendir.acra.dataprovider.AcrariumDataProvider
import com.faendir.acra.dataprovider.AcrariumSort
import com.faendir.acra.jooq.generated.tables.references.APP
import com.faendir.acra.jooq.generated.tables.references.APP_REPORT_COLUMNS
import com.faendir.acra.jooq.generated.tables.references.REPORT
import com.faendir.acra.persistence.*
import com.faendir.acra.persistence.user.Permission
import com.faendir.acra.persistence.user.Role
import com.faendir.acra.persistence.user.UserRepository
import com.faendir.acra.security.SecurityUtils
import org.apache.commons.text.RandomStringGenerator
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class AppRepository(private val jooq: DSLContext, private val userRepository: UserRepository, private val randomStringGenerator: RandomStringGenerator) {

    @PreAuthorize("hasViewPermission(#id)")
    fun find(id: AppId): App? = jooq.selectFrom(APP).where(APP.ID.eq(id)).fetchValueInto<App>()

    @PreAuthorize("hasViewPermission(#id)")
    fun findName(id: AppId): String? = jooq.select(APP.NAME).from(APP).where(APP.ID.eq(id)).fetchValue()

    fun findId(reporter: String): AppId? = jooq.select(APP.ID).from(APP).where(APP.REPORTER_USERNAME.eq(reporter)).fetchValue()

    @Transactional
    @PreAuthorize("isAdmin()")
    fun create(name: String): Reporter {
        val username = generateSequence { randomStringGenerator.generate(16) }.first { !userRepository.exists(it) }
        val password = randomStringGenerator.generate(16)
        if (!userRepository.create(username, password, null, Role.REPORTER)) throw RuntimeException("Failed to create reporter user")
        val reporter = Reporter(username, password)
        jooq.insertInto(APP)
            .set(APP.REPORTER_USERNAME, reporter.username)
            .set(APP.NAME, name)
            .execute()
        return reporter
    }

    @Transactional
    @PreAuthorize("hasAdminPermission(#id)")
    fun recreateReporter(id: AppId): Reporter {
        val reporterUsername = jooq.select(APP.REPORTER_USERNAME).from(APP).where(APP.ID.eq(id)).fetchValue()
            ?: throw IllegalArgumentException("Can't recreate reporter for unknown app")
        val password = randomStringGenerator.generate(16)
        userRepository.update(reporterUsername, password, null)
        return Reporter(reporterUsername, password)
    }

    @Transactional
    @PreAuthorize("hasAdminPermission(#id)")
    fun delete(id: AppId) {
        val reporterUsername = jooq.select(APP.REPORTER_USERNAME).from(APP).where(APP.ID.eq(id)).fetchValue() ?: return
        // app is cascade deleted
        userRepository.delete(reporterUsername)
    }

    @PreAuthorize("isUser()")
    fun getVisibleIds(): List<AppId> = jooq.select(APP.ID.NOT_NULL).from(APP).where(hasViewPermission()).fetchList()

    @PreAuthorize("isAdmin()")
    fun getAllNames(): List<AppName> = jooq.select(APP.ID, APP.NAME).from(APP).fetchListInto()

    @PreAuthorize("hasViewPermission(#id)")
    fun getCustomColumns(id: AppId): List<CustomColumn> =
        jooq.select(APP_REPORT_COLUMNS.NAME, APP_REPORT_COLUMNS.PATH).from(APP_REPORT_COLUMNS).where(APP_REPORT_COLUMNS.APP_ID.eq(id)).fetchListInto()

    @Transactional
    @PreAuthorize("hasAdminPermission(#id)")
    fun setCustomColumns(id: AppId, customColumns: List<CustomColumn>) {
        val tableMeta = jooq.meta().getTables(REPORT.name).first()
        val fields = tableMeta.fields()
        val indexes = tableMeta.indexes

        jooq.deleteFrom(APP_REPORT_COLUMNS).where(APP_REPORT_COLUMNS.APP_ID.eq(id), APP_REPORT_COLUMNS.PATH.notIn(customColumns.map { it.path })).execute()
        indexes.filter { index -> index.isCustomColumnIndex && customColumns.none { it.indexName == index.name } }.forEach {
            jooq.dropIndex(it).on(REPORT).execute()
        }
        val columnsToDelete = fields.filter { field -> field.isCustomColumn && customColumns.none { it.fieldName == field.name } }
        if (columnsToDelete.isNotEmpty()) {
            jooq.alterTable(REPORT).dropColumns(columnsToDelete).execute()
        }

        for (customColumn in customColumns) {
            jooq.insertInto(APP_REPORT_COLUMNS)
                .set(APP_REPORT_COLUMNS.APP_ID, id)
                .set(APP_REPORT_COLUMNS.PATH, customColumn.path)
                .set(APP_REPORT_COLUMNS.NAME, customColumn.name)
                .onDuplicateKeyUpdate()
                .set(APP_REPORT_COLUMNS.NAME, customColumn.name)
                .execute()
            if (fields.none { it.name == customColumn.fieldName }) {
                jooq.execute(
                    "ALTER TABLE `${REPORT.name}` ADD COLUMN `${customColumn.fieldName}` VARCHAR(255) GENERATED ALWAYS AS (JSON_UNQUOTE(JSON_VALUE(`${REPORT.name}`.`content`, '$.${customColumn.path}'))) STORED",
                )
            }
            if (indexes.none { it.name == customColumn.indexName }) {
                jooq.createIndex(customColumn.indexName).on(REPORT.name, customColumn.fieldName).execute()
            }
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