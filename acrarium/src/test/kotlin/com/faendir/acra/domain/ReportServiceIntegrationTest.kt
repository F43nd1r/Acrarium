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
import com.faendir.acra.jooq.generated.tables.references.BUG
import com.faendir.acra.jooq.generated.tables.references.REPORT
import com.faendir.acra.jooq.generated.tables.references.VERSION
import com.faendir.acra.persistence.TestDataBuilder
import com.faendir.acra.persistence.report.Report
import com.faendir.acra.persistence.user.Role
import com.faendir.acra.persistence.version.VersionRepository
import com.faendir.acra.withAuth
import com.ninjasquad.springmockk.MockkBean
import com.ninjasquad.springmockk.MockkSpyBean
import io.mockk.every
import org.jooq.DSLContext
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@AcrariumTest
class ReportServiceIntegrationTest(
    @Autowired private val reportService: ReportService,
    @Autowired @MockkSpyBean private val versionRepository: VersionRepository,
    @Autowired private val testDataBuilder: TestDataBuilder,
    @Autowired private val jooq: DSLContext,
    @Autowired @MockkBean(relaxed = true) private val mailService: MailService,
) {
    @Test
    fun `should return existing report and roll back duplicate report transaction`() {
        val reporter = testDataBuilder.createUser(username = "reporter", roles = arrayOf(Role.REPORTER))
        testDataBuilder.createApp(reporter)
        val reportId = "duplicate-report"
        val stacktrace = "IllegalStateException: duplicate report"
        val losingInstallationId = "losing-installation"
        val winningInstallationId = "winning-installation"
        val transactionStarted = CountDownLatch(1)
        val continueReportInsert = CountDownLatch(1)
        withAuth(Role.REPORTER) {
            every { versionRepository.ensureExists(any(), any(), any(), any()) } answers {
                transactionStarted.countDown()
                continueReportInsert.await()
                callOriginal()
            } andThenAnswer {
                callOriginal()
            }
        }

        val executor = Executors.newSingleThreadExecutor()
        try {
            val duplicate = executor.submit<Report> {
                withAuth(Role.REPORTER) {
                    reportService.create(
                        reporter,
                        """{"REPORT_ID":"$reportId","STACK_TRACE":"$stacktrace","USER_CRASH_DATE":"2026-01-01T00:00:00Z","INSTALLATION_ID":"$losingInstallationId"}""",
                        emptyList()
                    )
                }
            }

            expectThat(transactionStarted.await(10, TimeUnit.SECONDS)).isEqualTo(true)
            withAuth(Role.REPORTER) {
                reportService.create(
                    reporter,
                    """{"REPORT_ID":"$reportId","STACK_TRACE":"$stacktrace","USER_CRASH_DATE":"2026-01-01T00:00:00Z","INSTALLATION_ID":"$winningInstallationId"}""",
                    emptyList()
                )
            }
            continueReportInsert.countDown()

            expectThat(duplicate.get(10, TimeUnit.SECONDS).installationId).isEqualTo(winningInstallationId)
            expectThat(jooq.fetchCount(BUG)).isEqualTo(1)
            expectThat(jooq.fetchCount(VERSION)).isEqualTo(1)
            expectThat(jooq.fetchCount(REPORT)).isEqualTo(1)
        } finally {
            executor.shutdownNow()
        }
    }

}
