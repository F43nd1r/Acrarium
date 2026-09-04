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

import com.faendir.acra.annotation.AcrariumTest
import com.faendir.acra.persistence.TestDataBuilder
import com.faendir.acra.persistence.report.Report
import com.faendir.acra.persistence.user.Role
import com.faendir.acra.withAuth
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import jakarta.mail.Session
import jakarta.mail.internet.MimeMessage
import org.jooq.JSON
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.test.context.TestPropertySource
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.time.Instant
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@AcrariumTest
@TestPropertySource(properties = ["spring.mail.host=localhost", "management.health.mail.enabled=false"])
class MailServiceIntegrationTest(
    @Autowired private val mailService: MailService,
    @Autowired private val testDataBuilder: TestDataBuilder,
    @Autowired @MockkBean private val mailSender: JavaMailSender,
) {
    @Test
    fun `should send new bug notification`() {
        val appId = testDataBuilder.createApp()
        val bugId = testDataBuilder.createBug(appId)
        val version = testDataBuilder.createVersion(appId)
        testDataBuilder.createReport(appId, bugId, version = version)
        val username = testDataBuilder.createUser(username = "recipient", mail = "recipient@example.com", roles = arrayOf(Role.ADMIN))
        val mailSent = CountDownLatch(1)
        testDataBuilder.createMailSettings(appId, username, newBug = true)
        every { mailSender.createMimeMessage() } returns MimeMessage(Session.getInstance(Properties()))
        every { mailSender.send(any<MimeMessage>()) } answers { mailSent.countDown() }

        withAuth(Role.REPORTER) {
            mailService.onNewReport(
                Report(
                    id = "report",
                    androidVersion = null,
                    content = JSON.json("{}"),
                    date = Instant.now(),
                    phoneModel = null,
                    userComment = null,
                    userEmail = null,
                    brand = null,
                    installationId = "installation",
                    isSilent = false,
                    device = "device",
                    marketingDevice = "device",
                    bugId = bugId,
                    appId = appId,
                    stacktrace = "stacktrace",
                    exceptionClass = "Exception",
                    message = null,
                    crashLine = null,
                    cause = null,
                    versionCode = version.code,
                    versionFlavor = version.flavor,
                )
            )
        }

        expectThat(mailSent.await(5, TimeUnit.SECONDS)).isEqualTo(true)
    }

    @Test
    fun `should send weekly report`() {
        val appId = testDataBuilder.createApp()
        val username = testDataBuilder.createUser(username = "recipient", mail = "recipient@example.com", roles = arrayOf(Role.ADMIN))
        val mailSent = CountDownLatch(1)
        testDataBuilder.createMailSettings(appId, username, summary = true)
        every { mailSender.createMimeMessage() } returns MimeMessage(Session.getInstance(Properties()))
        every { mailSender.send(any<MimeMessage>()) } answers { mailSent.countDown() }

        mailService.weeklyReport()

        expectThat(mailSent.await(5, TimeUnit.SECONDS)).isEqualTo(true)
    }

}