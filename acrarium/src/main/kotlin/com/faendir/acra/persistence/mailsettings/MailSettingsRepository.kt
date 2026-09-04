/*
 * (C) Copyright 2022-2026 Lukas Morawietz (https://github.com/F43nd1r)
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
package com.faendir.acra.persistence.mailsettings

import com.faendir.acra.jooq.generated.tables.references.MAIL_SETTINGS
import com.faendir.acra.persistence.app.AppId
import org.jooq.DSLContext
import org.jooq.Records
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Repository

@Repository
class MailSettingsRepository(private val jooq: DSLContext) {

    @PreAuthorize("isCurrentUser(#username)")
    fun find(appId: AppId, username: String): MailSettings? =
        jooq.selectMailSettings().where(MAIL_SETTINGS.APP_ID.eq(appId), MAIL_SETTINGS.USERNAME.eq(username)).fetchOne(Records.mapping(::MailSettings))

    fun getAll(): List<MailSettings> = jooq.selectMailSettings().fetch(Records.mapping(::MailSettings))

    fun findAll(appId: AppId): List<MailSettings> =
        jooq.selectMailSettings().where(MAIL_SETTINGS.APP_ID.eq(appId)).fetch(Records.mapping(::MailSettings))

    @PreAuthorize("isCurrentUser(#mailSettings.username)")
    fun store(mailSettings: MailSettings) {
        jooq.insertInto(MAIL_SETTINGS)
            .set(MAIL_SETTINGS.APP_ID, mailSettings.appId)
            .set(MAIL_SETTINGS.USERNAME, mailSettings.username)
            .set(MAIL_SETTINGS.NEW_BUG, mailSettings.newBug)
            .set(MAIL_SETTINGS.REGRESSION, mailSettings.regression)
            .set(MAIL_SETTINGS.SPIKE, mailSettings.spike)
            .set(MAIL_SETTINGS.SUMMARY, mailSettings.summary)
            .onDuplicateKeyUpdate()
            .set(MAIL_SETTINGS.NEW_BUG, mailSettings.newBug)
            .set(MAIL_SETTINGS.REGRESSION, mailSettings.regression)
            .set(MAIL_SETTINGS.SPIKE, mailSettings.spike)
            .set(MAIL_SETTINGS.SUMMARY, mailSettings.summary)
            .execute()
    }
}

private fun DSLContext.selectMailSettings() = select(
    MAIL_SETTINGS.APP_ID,
    MAIL_SETTINGS.USERNAME,
    MAIL_SETTINGS.NEW_BUG,
    MAIL_SETTINGS.REGRESSION,
    MAIL_SETTINGS.SPIKE,
    MAIL_SETTINGS.SUMMARY,
).from(MAIL_SETTINGS)
