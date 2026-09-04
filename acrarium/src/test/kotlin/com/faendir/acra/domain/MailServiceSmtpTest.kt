/*
 * (C) Copyright 2026 Lukas Morawietz (https://github.com/F43nd1r)
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
package com.faendir.acra.domain

import com.faendir.acra.i18n.ResourceBundleI18NProvider
import com.faendir.acra.persistence.app.AppRepository
import com.faendir.acra.persistence.bug.BugRepository
import com.faendir.acra.persistence.mailsettings.MailSettingsRepository
import com.faendir.acra.persistence.report.ReportRepository
import com.faendir.acra.persistence.user.UserRepository
import com.faendir.acra.persistence.version.VersionRepository
import com.icegreen.greenmail.util.GreenMail
import com.icegreen.greenmail.util.ServerSetupTest
import com.vaadin.flow.router.RouteConfiguration
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.mail.javamail.JavaMailSenderImpl
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class MailServiceSmtpTest {
    private lateinit var greenMail: GreenMail
    private lateinit var service: MailService

    @BeforeEach
    fun setUp() {
        greenMail = GreenMail(ServerSetupTest.SMTP).apply { start() }
        service = MailService(
            mailSettingsRepository = mockk<MailSettingsRepository>(),
            appRepository = mockk<AppRepository>(),
            bugRepository = mockk<BugRepository>(),
            versionRepository = mockk<VersionRepository>(),
            reportRepository = mockk<ReportRepository>(),
            userRepository = mockk<UserRepository>(),
            i18nProvider = ResourceBundleI18NProvider("i18n.com.faendir.acra.messages"),
            mailSender = JavaMailSenderImpl().apply {
                host = greenMail.smtp.bindTo
                port = greenMail.smtp.port
            },
            routeConfiguration = mockk<RouteConfiguration>(),
        )
    }

    @AfterEach
    fun tearDown() {
        greenMail.stop()
    }

    @Test
    fun `test message is delivered through SMTP`() {
        service.testMessage("admin@example.com")

        expectThat(greenMail.waitForIncomingEmail(5_000, 1)).isEqualTo(true)
        expectThat(greenMail.receivedMessages.single().allRecipients.single().toString()).isEqualTo("admin@example.com")
    }
}

