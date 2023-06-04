/*
 * (C) Copyright 2022 Lukas Morawietz (https://github.com/F43nd1r)
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
import com.faendir.acra.persistence.fetchListInto
import com.faendir.acra.persistence.fetchValueInto
import org.jooq.DSLContext
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Repository

@Repository
class MailSettingsRepository(private val jooq: DSLContext) {

    @PreAuthorize("#username == principal.username")
    fun find(appId: AppId, username: String): MailSettings? =
        jooq.selectFrom(MAIL_SETTINGS).where(MAIL_SETTINGS.APP_ID.eq(appId), MAIL_SETTINGS.USERNAME.eq(username)).fetchValueInto()

    fun getAll(): List<MailSettings> = jooq.selectFrom(MAIL_SETTINGS).fetchListInto()

    fun findAll(appId: AppId): List<MailSettings> =
        jooq.selectFrom(MAIL_SETTINGS).where(MAIL_SETTINGS.APP_ID.eq(appId)).fetchListInto()

    @PreAuthorize("#mailSettings.username == principal.username")
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