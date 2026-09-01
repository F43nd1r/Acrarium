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
import com.vaadin.flow.router.RouteConfiguration
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import jakarta.mail.Message
import jakarta.mail.Session
import jakarta.mail.internet.MimeMessage
import org.junit.jupiter.api.Test
import org.springframework.mail.javamail.JavaMailSender
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isEqualTo
import java.io.ByteArrayOutputStream
import java.util.*

class MailServiceTest {
    private val sentMessages = mutableListOf<MimeMessage>()
    private val mailSender = mockk<JavaMailSender> {
        every { createMimeMessage() } returns MimeMessage(Session.getInstance(Properties()))
        every { send(any<MimeMessage>()) } answers { sentMessages += invocation.args[0] as MimeMessage }
    }
    private val service = MailService(
        mailSettingsRepository = mockk<MailSettingsRepository>(),
        appRepository = mockk<AppRepository>(),
        bugRepository = mockk<BugRepository>(),
        versionRepository = mockk<VersionRepository>(),
        reportRepository = mockk<ReportRepository>(),
        userRepository = mockk<UserRepository>(),
        i18nProvider = ResourceBundleI18NProvider("i18n.com.faendir.acra.messages"),
        mailSender = mailSender,
        routeConfiguration = mockk<RouteConfiguration>(),
    )

    @Test
    fun `test message sends the localized HTML email to the requested recipient`() {
        service.testMessage("admin@example.com")

        verify(exactly = 1) { mailSender.send(any<MimeMessage>()) }
        val sentMessage = sentMessages.single()
        expectThat(sentMessage.getRecipients(Message.RecipientType.TO).single().toString()).isEqualTo("admin@example.com")
        expectThat(sentMessage.subject).isEqualTo("Acrarium Setup Test")
        val rawMessage = ByteArrayOutputStream().use { output ->
            sentMessage.writeTo(output)
            output.toString(Charsets.UTF_8)
        }
        expectThat(rawMessage).contains("your mail setup is complete")
        expectThat(rawMessage).contains("Content-Type: text/html")
    }

}
