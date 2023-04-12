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
package com.faendir.acra.persistence

import com.faendir.acra.jooq.generated.tables.references.*
import com.faendir.acra.persistence.app.AppId
import com.faendir.acra.persistence.app.CustomColumn
import com.faendir.acra.persistence.bug.BugId
import com.faendir.acra.persistence.bug.BugIdentifier
import com.faendir.acra.persistence.user.Permission
import com.faendir.acra.persistence.user.Role
import com.faendir.acra.persistence.version.VersionKey
import org.apache.commons.text.RandomStringGenerator
import org.intellij.lang.annotations.Language
import org.jooq.DSLContext
import org.jooq.JSON
import java.time.Instant
import java.util.*

class TestDataBuilder(private val jooq: DSLContext, private val randomStringGenerator: RandomStringGenerator) {
    private val random = Random()

    private fun randomString(prefix: String = "string") = "$prefix-${randomStringGenerator.generate(16)}"

    fun createUser(
        username: String = randomString("test-user"),
        password: String = randomString("test-password"),
        vararg roles: Role
    ): String {
        jooq.insertInto(USER)
            .set(USER.USERNAME, username)
            .set(USER.PASSWORD, password)
            .execute()
        for (role in roles) {
            jooq.insertInto(USER_ROLES)
                .set(USER_ROLES.USER_USERNAME, username)
                .set(USER_ROLES.ROLES, role.name)
                .execute()
        }
        return username
    }

    fun createPermission(username: String, appId: AppId, level: Permission.Level) {
        jooq.insertInto(USER_PERMISSIONS)
            .set(USER_PERMISSIONS.USER_USERNAME, username)
            .set(USER_PERMISSIONS.APP_ID, appId)
            .set(USER_PERMISSIONS.LEVEL, level.name)
            .execute()
    }

    fun createApp(reporter: String = createUser(), name: String = randomString("test-app")) = jooq.insertInto(APP)
        .set(APP.REPORTER_USERNAME, reporter)
        .set(APP.NAME, name)
        .returningResult(APP.ID).fetchValue()!!

    fun createCustomColumn(
        app: AppId = createApp(),
        name: String = randomString("test-name"),
        path: String = randomString("test-path")
    ): CustomColumn {
        jooq.insertInto(APP_REPORT_COLUMNS)
            .set(APP_REPORT_COLUMNS.APP_ID, app)
            .set(APP_REPORT_COLUMNS.NAME, name)
            .set(APP_REPORT_COLUMNS.PATH, path)
            .execute()
        return CustomColumn(name, path)
    }

    fun createVersion(
        app: AppId = createApp(),
        code: Int = random.nextInt(),
        flavor: String = "",
        name: String = randomString("test-version")
    ): VersionKey {
        jooq.insertInto(VERSION)
            .set(VERSION.CODE, code)
            .set(VERSION.NAME, name)
            .set(VERSION.FLAVOR, flavor)
            .set(VERSION.APP_ID, app)
            .execute()
        return VersionKey(code, flavor)
    }

    /**
     * Use with caution! The created bug is not valid without a report!
     */
    fun createBug(app: AppId = createApp(), title: String = randomString("title")) = jooq.insertInto(BUG)
        .set(BUG.APP_ID, app)
        .set(BUG.TITLE, title)
        .returningResult(BUG.ID).fetchValue()!!

    fun createBugIdentifier(
        app: AppId = createApp(),
        bug: BugId = createBug(app),
        exceptionClass: String = randomString("exception-class"),
        message: String? = null,
        crashLine: String? = null,
        cause: String? = null
    ): BugIdentifier {
        jooq.insertInto(BUG_IDENTIFIER)
            .set(BUG_IDENTIFIER.BUG_ID, bug)
            .set(BUG_IDENTIFIER.APP_ID, app)
            .set(BUG_IDENTIFIER.EXCEPTION_CLASS, exceptionClass)
            .set(BUG_IDENTIFIER.MESSAGE, message)
            .set(BUG_IDENTIFIER.CRASH_LINE, crashLine)
            .set(BUG_IDENTIFIER.CAUSE, cause)
            .execute()
        return BugIdentifier(app, exceptionClass, message, crashLine, cause)
    }

    fun createReport(
        app: AppId = createApp(),
        bug: BugId = createBug(app),
        bugIdentifier: BugIdentifier = createBugIdentifier(app, bug),
        version: VersionKey = createVersion(app),
        date: Instant = Instant.now(),
        id: String = randomString("report"),
        installationId: String = randomString("installationId"),
        @Language("JSON")
        content: String = "{\"REPORT_ID\": \"${id}\"}",
    ): String {
        jooq.insertInto(REPORT)
            .set(REPORT.ID, id)
            .set(REPORT.APP_ID, app)
            .set(REPORT.BUG_ID, bug)
            .set(REPORT.VERSION_CODE, version.code)
            .set(REPORT.VERSION_FLAVOR, version.flavor)
            .set(REPORT.CONTENT, JSON.json(content))
            .set(REPORT.INSTALLATION_ID, installationId)
            .set(REPORT.DATE, date)
            .set(REPORT.IS_SILENT, false)
            .set(REPORT.DEVICE, randomString("device"))
            .set(REPORT.MARKETING_DEVICE, randomString("marketingDevice"))
            .set(REPORT.STACKTRACE, randomString("stacktrace"))
            .set(REPORT.EXCEPTION_CLASS, bugIdentifier.exceptionClass)
            .set(REPORT.MESSAGE, bugIdentifier.message)
            .set(REPORT.CRASH_LINE, bugIdentifier.crashLine)
            .set(REPORT.CAUSE, bugIdentifier.cause)
            .execute()
        return id
    }

    fun createAttachment(
        report: String = createReport(),
        fileName: String = randomString("fileName"),
        content: ByteArray = randomString("content").encodeToByteArray()
    ) {
        jooq.insertInto(ATTACHMENT)
            .set(ATTACHMENT.REPORT_ID, report)
            .set(ATTACHMENT.FILENAME, fileName)
            .set(ATTACHMENT.CONTENT, content)
            .execute()
    }

    fun createMailSettings(
        app: AppId = createApp(),
        user: String = createUser(),
        newBug: Boolean = false,
        regression: Boolean = false,
        spike: Boolean = false,
        summary: Boolean = false,
    ) =
        jooq.insertInto(MAIL_SETTINGS)
            .set(MAIL_SETTINGS.APP_ID, app)
            .set(MAIL_SETTINGS.USERNAME, user)
            .set(MAIL_SETTINGS.NEW_BUG, newBug)
            .set(MAIL_SETTINGS.REGRESSION, regression)
            .set(MAIL_SETTINGS.SPIKE, spike)
            .set(MAIL_SETTINGS.SUMMARY, summary)
            .execute()
}