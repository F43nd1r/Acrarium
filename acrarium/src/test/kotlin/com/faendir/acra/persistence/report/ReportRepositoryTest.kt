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
package com.faendir.acra.persistence.report

import com.faendir.acra.annotation.PersistenceTest
import com.faendir.acra.dataprovider.AcrariumSort
import com.faendir.acra.jooq.generated.tables.references.REPORT
import com.faendir.acra.persistence.TestDataBuilder
import com.faendir.acra.persistence.app.AppId
import com.faendir.acra.persistence.bug.BugId
import com.faendir.acra.persistence.version.VersionKey
import com.vaadin.flow.data.provider.SortDirection
import org.jooq.JSON
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.dao.DuplicateKeyException
import strikt.api.expectThat
import strikt.api.expectThrows
import strikt.assertions.*
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlin.properties.Delegates

@PersistenceTest
internal class ReportRepositoryTest(
    @Autowired
    private val reportRepository: ReportRepository,
    @Autowired
    private val testDataBuilder: TestDataBuilder,
) {
    private var appId by Delegates.notNull<AppId>()

    @BeforeEach
    fun setup() {
        appId = testDataBuilder.createApp()
    }

    @Nested
    inner class Find {
        @Test
        fun `should find report by id`() {
            val report = testDataBuilder.createReport()

            expectThat(reportRepository.find(report)).isNotNull().and { get { id }.isEqualTo(report) }
        }

        @Test
        fun `should return null for unknown id`() {
            expectThat(reportRepository.find("report")).isNull()
        }
    }

    @Nested
    inner class Attachment {
        @Test
        fun `should find report attachment names`() {
            val report = testDataBuilder.createReport()
            testDataBuilder.createAttachment(report, fileName = "test")
            testDataBuilder.createAttachment(fileName = "other")

            expectThat(reportRepository.findAttachmentNames(report)).containsExactly("test")
        }

        @Test
        fun `should load attachment content`() {
            val report = testDataBuilder.createReport()
            val fileName = "test"
            val content = byteArrayOf(0, 1, 2, 3)
            testDataBuilder.createAttachment(report, fileName = fileName, content = content)
            testDataBuilder.createAttachment(fileName = "other")

            expectThat(reportRepository.loadAttachment(report, fileName)).isEqualTo(content)
        }
    }

    @Nested
    inner class ListIds {
        @Test
        fun `should return report ids in window`() {
            val now = Instant.now()
            testDataBuilder.createReport(appId, date = now.minus(Duration.ofDays(5)))
            val reportIn1 = testDataBuilder.createReport(appId, date = now)
            val reportIn2 = testDataBuilder.createReport(appId, date = now.plus(Duration.ofHours(23)))
            testDataBuilder.createReport(appId, date = now.plus(Duration.ofHours(25)))

            expectThat(reportRepository.listIds(appId, now.minus(Duration.ofDays(1)), now.plus(Duration.ofDays(1)))).containsExactlyInAnyOrder(
                reportIn1,
                reportIn2
            )
        }

        @Test
        fun `should return all report ids without window`() {
            val now = Instant.now()
            val report1 = testDataBuilder.createReport(appId, date = now.minus(Duration.ofDays(5)))
            val report2 = testDataBuilder.createReport(appId, date = now)
            val report3 = testDataBuilder.createReport(appId, date = now.plus(Duration.ofHours(23)))
            val report4 = testDataBuilder.createReport(appId, date = now.plus(Duration.ofHours(25)))

            expectThat(reportRepository.listIds(appId, null, null)).containsExactlyInAnyOrder(report1, report2, report3, report4)
        }
    }

    @Nested
    inner class Get {
        @Test
        fun `should return field from reports`() {
            testDataBuilder.createReport(appId, installationId = "1")
            testDataBuilder.createReport(appId, installationId = "3")
            testDataBuilder.createReport(appId, installationId = "2")
            testDataBuilder.createReport(appId, installationId = "1")

            expectThat(reportRepository.get(appId, REPORT.INSTALLATION_ID, sorted = true)).containsExactly("1", "2", "3")
        }

        @Test
        fun `should return field from filtered reports`() {
            val bug = testDataBuilder.createBug(appId)
            testDataBuilder.createReport(appId, installationId = "1", bug = bug)
            testDataBuilder.createReport(appId, installationId = "3")
            testDataBuilder.createReport(appId, installationId = "2", bug = bug)
            testDataBuilder.createReport(appId, installationId = "1", bug = bug)

            expectThat(reportRepository.get(appId, REPORT.INSTALLATION_ID, REPORT.BUG_ID.eq(bug), sorted = true)).containsExactly("1", "2")
        }
    }

    @Nested
    inner class CountGroupedBy {
        @Test
        fun `should return count grouped from reports`() {
            testDataBuilder.createReport(appId, installationId = "1")
            testDataBuilder.createReport(appId, installationId = "3")
            testDataBuilder.createReport(appId, installationId = "2")
            testDataBuilder.createReport(appId, installationId = "1")

            expectThat(reportRepository.countGroupedBy(appId, REPORT.INSTALLATION_ID, null)).isEqualTo(mapOf("1" to 2, "2" to 1, "3" to 1))
        }

        @Test
        fun `should return count grouped from filtered reports`() {
            val bug = testDataBuilder.createBug(appId)
            testDataBuilder.createReport(appId, installationId = "1", bug = bug)
            testDataBuilder.createReport(appId, installationId = "3")
            testDataBuilder.createReport(appId, installationId = "2", bug = bug)
            testDataBuilder.createReport(appId, installationId = "1", bug = bug)
            testDataBuilder.createReport(appId, installationId = "1")

            expectThat(reportRepository.countGroupedBy(appId, REPORT.INSTALLATION_ID, REPORT.BUG_ID.eq(bug))).isEqualTo(mapOf("1" to 2, "2" to 1))
        }
    }

    @Nested
    inner class CountInRange {
        @Test
        fun `should count reports in time range`() {
            val now = Instant.now().truncatedTo(ChronoUnit.SECONDS)
            val bug = testDataBuilder.createBug(appId)
            testDataBuilder.createReport(appId, bug, date = now.minus(5, ChronoUnit.DAYS))
            testDataBuilder.createReport(appId, bug, date = now.minus(4, ChronoUnit.DAYS))
            testDataBuilder.createReport(appId, bug, date = now.minus(3, ChronoUnit.DAYS))
            testDataBuilder.createReport(appId, date = now.minus(3, ChronoUnit.DAYS))
            testDataBuilder.createReport(appId, bug, date = now.minus(2, ChronoUnit.DAYS))
            testDataBuilder.createReport(appId, bug, date = now.minus(1, ChronoUnit.DAYS))


            expectThat(reportRepository.countInRange(appId, bug, now.minus(4, ChronoUnit.DAYS)..now.minus(2, ChronoUnit.DAYS))).isEqualTo(3)
        }
    }

    @Nested
    inner class Create {
        @Test
        fun `should store full report`() {
            val bugId = testDataBuilder.createBug(appId)
            val version = testDataBuilder.createVersion(appId)
            val report = Report(
                id = "id",
                androidVersion = "androidVersion",
                content = JSON.json("{\"REPORT_ID\": \"id\"}"),
                date = Instant.now().truncatedTo(ChronoUnit.SECONDS),
                phoneModel = "phoneModel",
                userComment = "userComment",
                userEmail = "userEmail",
                brand = "brand",
                installationId = "installationId",
                isSilent = true,
                device = "device",
                marketingDevice = "marketingDevice",
                bugId = bugId,
                appId = appId,
                stacktrace = "stacktrace",
                exceptionClass = "exceptionClass",
                message = "message",
                crashLine = "crashLine",
                cause = "cause",
                versionKey = version
            )

            reportRepository.create(report, emptyMap())

            expectThat(reportRepository.find(report.id)).isEqualTo(report)
        }

        @Test
        fun `should store minimal report`() {
            val bugId = testDataBuilder.createBug(appId)
            val version = testDataBuilder.createVersion(appId)
            val report = Report(
                id = "id",
                androidVersion = null,
                content = JSON.json("{\"REPORT_ID\": \"id\"}"),
                date = Instant.now().truncatedTo(ChronoUnit.SECONDS),
                phoneModel = null,
                userComment = null,
                userEmail = null,
                brand = null,
                installationId = "installationId",
                isSilent = false,
                device = "device",
                marketingDevice = "marketingDevice",
                bugId = bugId,
                appId = appId,
                stacktrace = "stacktrace",
                exceptionClass = "exceptionClass",
                message = null,
                crashLine = null,
                cause = null,
                versionKey = version
            )

            reportRepository.create(report, emptyMap())

            expectThat(reportRepository.find(report.id)).isEqualTo(report)
        }

        @Test
        fun `should throw for duplicate report id`() {
            val bugId = testDataBuilder.createBug(appId)
            val version = testDataBuilder.createVersion(appId)
            val report = testDataBuilder.createReport(appId)

            expectThrows<DuplicateKeyException> {
                reportRepository.create(
                    Report(
                        id = report,
                        androidVersion = null,
                        content = JSON.json("{\"REPORT_ID\": \"id\"}"),
                        date = Instant.now(),
                        phoneModel = null,
                        userComment = null,
                        userEmail = null,
                        brand = null,
                        installationId = "installationId",
                        isSilent = false,
                        device = "device",
                        marketingDevice = "marketingDevice",
                        bugId = bugId,
                        appId = appId,
                        stacktrace = "stacktrace",
                        exceptionClass = "exceptionClass",
                        message = null,
                        crashLine = null,
                        cause = null,
                        versionKey = version
                    ), emptyMap()
                )
            }
        }

        @Test
        fun `should throw for missing version`() {
            val bugId = testDataBuilder.createBug(appId)

            expectThrows<DataIntegrityViolationException> {
                reportRepository.create(
                    Report(
                        id = "id",
                        androidVersion = null,
                        content = JSON.json("{\"REPORT_ID\": \"id\"}"),
                        date = Instant.now(),
                        phoneModel = null,
                        userComment = null,
                        userEmail = null,
                        brand = null,
                        installationId = "installationId",
                        isSilent = false,
                        device = "device",
                        marketingDevice = "marketingDevice",
                        bugId = bugId,
                        appId = appId,
                        stacktrace = "stacktrace",
                        exceptionClass = "exceptionClass",
                        message = null,
                        crashLine = null,
                        cause = null,
                        versionKey = VersionKey(0, "")
                    ), emptyMap()
                )
            }
        }

        @Test
        fun `should throw for missing bug`() {
            val bugId = testDataBuilder.createBug(appId)
            val version = testDataBuilder.createVersion(appId)

            expectThrows<DataIntegrityViolationException> {
                reportRepository.create(
                    Report(
                        id = "id",
                        androidVersion = null,
                        content = JSON.json("{\"REPORT_ID\": \"id\"}"),
                        date = Instant.now(),
                        phoneModel = null,
                        userComment = null,
                        userEmail = null,
                        brand = null,
                        installationId = "installationId",
                        isSilent = false,
                        device = "device",
                        marketingDevice = "marketingDevice",
                        bugId = bugId,
                        appId = AppId(0),
                        stacktrace = "stacktrace",
                        exceptionClass = "exceptionClass",
                        message = null,
                        crashLine = null,
                        cause = null,
                        versionKey = version
                    ), emptyMap()
                )
            }
        }

        @Test
        fun `should throw for missing app`() {
            val version = testDataBuilder.createVersion(appId)

            expectThrows<DataIntegrityViolationException> {
                reportRepository.create(
                    Report(
                        id = "id",
                        androidVersion = null,
                        content = JSON.json("{\"REPORT_ID\": \"id\"}"),
                        date = Instant.now(),
                        phoneModel = null,
                        userComment = null,
                        userEmail = null,
                        brand = null,
                        installationId = "installationId",
                        isSilent = false,
                        device = "device",
                        marketingDevice = "marketingDevice",
                        bugId = BugId(0),
                        appId = AppId(0),
                        stacktrace = "stacktrace",
                        exceptionClass = "exceptionClass",
                        message = null,
                        crashLine = null,
                        cause = null,
                        versionKey = version
                    ), emptyMap()
                )
            }
        }

        @Test
        fun `should store attachments`() {
            val bugId = testDataBuilder.createBug(appId)
            val version = testDataBuilder.createVersion(appId)
            val report = Report(
                id = "id",
                androidVersion = null,
                content = JSON.json("{\"REPORT_ID\": \"id\"}"),
                date = Instant.now().truncatedTo(ChronoUnit.SECONDS),
                phoneModel = null,
                userComment = null,
                userEmail = null,
                brand = null,
                installationId = "installationId",
                isSilent = false,
                device = "device",
                marketingDevice = "marketingDevice",
                bugId = bugId,
                appId = appId,
                stacktrace = "stacktrace",
                exceptionClass = "exceptionClass",
                message = null,
                crashLine = null,
                cause = null,
                versionKey = version
            )

            reportRepository.create(report, mapOf("attachment" to byteArrayOf(0, 1, 2), "attachment2" to byteArrayOf(1, 2)))

            expectThat(reportRepository.findAttachmentNames(report.id)).containsExactlyInAnyOrder("attachment", "attachment2")
            expectThat(reportRepository.loadAttachment(report.id, "attachment")).isEqualTo(byteArrayOf(0, 1, 2))
        }
    }

    @Nested
    inner class Delete {
        @Test
        fun `should delete report`() {
            val report = testDataBuilder.createReport(appId)

            expectThat(reportRepository.find(report)).isNotNull()

            reportRepository.delete(appId, report)

            expectThat(reportRepository.find(report)).isNull()
        }

        @Test
        fun `should delete reports before time`() {
            val now = Instant.now()
            testDataBuilder.createReport(appId, date = now.minus(5, ChronoUnit.DAYS))
            testDataBuilder.createReport(appId, date = now.minus(2, ChronoUnit.DAYS))
            testDataBuilder.createReport(appId, date = now.minus(1, ChronoUnit.DAYS))
            val keep = testDataBuilder.createReport(appId, date = now)

            expectThat(reportRepository.get(appId, REPORT.ID)).hasSize(4)

            reportRepository.deleteBefore(appId, now.minus(1, ChronoUnit.MINUTES))


            expectThat(reportRepository.get(appId, REPORT.ID)).containsExactly(keep)
        }

        @Test
        fun `should delete reports before version`() {
            val version1 = testDataBuilder.createVersion(appId, code = 1)
            val version2 = testDataBuilder.createVersion(appId, code = 2)
            val version3 = testDataBuilder.createVersion(appId, code = 3)
            val version4 = testDataBuilder.createVersion(appId, code = 4)
            testDataBuilder.createReport(appId, version = version1)
            testDataBuilder.createReport(appId, version = version2)
            testDataBuilder.createReport(appId, version = version2)
            val keep1 = testDataBuilder.createReport(appId, version = version3)
            val keep2 = testDataBuilder.createReport(appId, version = version4)

            expectThat(reportRepository.get(appId, REPORT.ID)).hasSize(5)

            reportRepository.deleteBefore(appId, 3)


            expectThat(reportRepository.get(appId, REPORT.ID)).containsExactlyInAnyOrder(keep1, keep2)
        }
    }

    @Nested
    inner class Provider {

        @Test
        fun `should return all reports from app`() {
            val provider = reportRepository.getProvider(appId, emptyList())
            val r1 = testDataBuilder.createReport(appId)
            val r2 = testDataBuilder.createReport(appId)
            val otherAppId = testDataBuilder.createApp()
            testDataBuilder.createReport(otherAppId)

            expectThat(provider.size(emptySet())).isEqualTo(2)
            expectThat(provider.fetch(emptySet(), emptyList(), 0, 10).toList().map { it.id }).containsExactlyInAnyOrder(r1, r2)
        }

        @Test
        fun `should return all reports from bug`() {
            val bugId = testDataBuilder.createBug(appId)
            val provider = reportRepository.getProvider(appId, bugId, emptyList())
            val r1 = testDataBuilder.createReport(appId, bugId)
            val r2 = testDataBuilder.createReport(appId, bugId)
            val otherBugId = testDataBuilder.createBug(appId)
            testDataBuilder.createReport(appId, otherBugId)

            expectThat(provider.size(emptySet())).isEqualTo(2)
            expectThat(provider.fetch(emptySet(), emptyList(), 0, 10).toList().map { it.id }).containsExactlyInAnyOrder(r1, r2)
        }

        @Test
        fun `should return all reports from installation`() {
            val installationId = "installation-id"
            val provider = reportRepository.getProvider(appId, installationId, emptyList())
            val r1 = testDataBuilder.createReport(appId, installationId = installationId)
            val r2 = testDataBuilder.createReport(appId, installationId = installationId)
            testDataBuilder.createReport(appId, installationId = "other")

            expectThat(provider.size(emptySet())).isEqualTo(2)
            expectThat(provider.fetch(emptySet(), emptyList(), 0, 10).toList().map { it.id }).containsExactlyInAnyOrder(r1, r2)
        }

        @Test
        fun `should return all data including custom columns`() {
            val bugId = testDataBuilder.createBug(appId)
            val version = testDataBuilder.createVersion(appId)
            val report = Report(
                id = "id",
                androidVersion = "androidVersion",
                content = JSON.json("{\"CUSTOM_FIELD\": \"customField\", \"NESTED_CUSTOM_FIELD\": {\"foo\": \"bar\"}}"),
                date = Instant.now().truncatedTo(ChronoUnit.SECONDS),
                phoneModel = "phoneModel",
                userComment = "userComment",
                userEmail = "userEmail",
                brand = "brand",
                installationId = "installationId",
                isSilent = true,
                device = "device",
                marketingDevice = "marketingDevice",
                bugId = bugId,
                appId = appId,
                stacktrace = "stacktrace",
                exceptionClass = "exceptionClass",
                message = "message",
                crashLine = "crashLine",
                cause = "cause",
                versionKey = version
            )
            reportRepository.create(report, emptyMap())
            val provider = reportRepository.getProvider(appId, listOf("NESTED_CUSTOM_FIELD", "CUSTOM_FIELD"))

            expectThat(provider.fetch(emptySet(), emptyList(), 0, 1).toList().first()).isEqualTo(
                ReportRow(
                    id = "id",
                    androidVersion = "androidVersion",
                    phoneModel = "phoneModel",
                    date = report.date,
                    marketingDevice = "marketingDevice",
                    installationId = "installationId",
                    isSilent = true,
                    exceptionClass = "exceptionClass",
                    message = "message",
                    versionKey = version,
                    bugId = bugId,
                    customColumns = listOf("{\"foo\": \"bar\"}", "customField")
                )
            )
        }

        @Test
        fun `should not fail for missing custom columns`() {
            val version = testDataBuilder.createVersion(appId)
            testDataBuilder.createReport(appId, version = version, content = "{\"CUSTOM_FIELD\": \"customField\"}")
            val provider = reportRepository.getProvider(appId, listOf("NESTED_CUSTOM_FIELD", "CUSTOM_FIELD"))

            expectThat(provider.fetch(emptySet(), emptyList(), 0, 1).toList().first().customColumns).isEqualTo(listOf(null, "customField"))
        }

        @Test
        fun `should sort returned reports`() {
            val provider = reportRepository.getProvider(appId, emptyList())
            val r1 = testDataBuilder.createReport(appId, installationId = "a")
            val r2 = testDataBuilder.createReport(appId, installationId = "b")

            expectThat(
                provider.fetch(emptySet(), listOf(AcrariumSort(ReportRow.Sort.INSTALLATION_ID, SortDirection.ASCENDING)), 0, 10).toList()
                    .map { it.id }
            ).isEqualTo(listOf(r1, r2))
        }

        @Test
        fun `should offset and limit returned reports`() {
            val provider = reportRepository.getProvider(appId, emptyList())
            val r1 = testDataBuilder.createReport(appId, installationId = "a")
            val r2 = testDataBuilder.createReport(appId, installationId = "b")

            expectThat(
                provider.fetch(emptySet(), listOf(AcrariumSort(ReportRow.Sort.INSTALLATION_ID, SortDirection.ASCENDING)), 0, 1).toList()
                    .map { it.id })
                .isEqualTo(listOf(r1))
            expectThat(
                provider.fetch(emptySet(), listOf(AcrariumSort(ReportRow.Sort.INSTALLATION_ID, SortDirection.ASCENDING)), 1, 1).toList()
                    .map { it.id })
                .isEqualTo(listOf(r2))
        }
    }

    @Nested
    inner class InstallationProvider {
        @Test
        fun `should return all installations from app`() {
            val provider = reportRepository.getInstallationProvider(appId)
            testDataBuilder.createReport(appId, installationId = "1")
            testDataBuilder.createReport(appId, installationId = "2")
            val otherAppId = testDataBuilder.createApp()
            testDataBuilder.createReport(otherAppId, installationId = "3")

            expectThat(provider.size(emptySet())).isEqualTo(2)
            expectThat(provider.fetch(emptySet(), emptyList(), 0, 10).toList().map { it.id }).containsExactlyInAnyOrder("1", "2")
        }

        @Test
        fun `should sort returned installations`() {
            val provider = reportRepository.getInstallationProvider(appId)
            testDataBuilder.createReport(appId, installationId = "b")
            testDataBuilder.createReport(appId, installationId = "a")

            expectThat(
                provider.fetch(emptySet(), listOf(AcrariumSort(Installation.Sort.ID, SortDirection.ASCENDING)), 0, 10).toList()
                    .map { it.id }
            ).containsExactly("a", "b")
        }

        @Test
        fun `should offset and limit returned installations`() {
            val provider = reportRepository.getInstallationProvider(appId)
            testDataBuilder.createReport(appId, installationId = "b")
            testDataBuilder.createReport(appId, installationId = "a")

            expectThat(
                provider.fetch(emptySet(), listOf(AcrariumSort(Installation.Sort.ID, SortDirection.ASCENDING)), 0, 1).toList()
                    .map { it.id })
                .containsExactly("a")
            expectThat(
                provider.fetch(emptySet(), listOf(AcrariumSort(Installation.Sort.ID, SortDirection.ASCENDING)), 1, 1).toList()
                    .map { it.id })
                .containsExactly("b")
        }
    }
}