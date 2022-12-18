package com.faendir.acra.persistence.report

import com.faendir.acra.annotation.PersistenceTest
import com.faendir.acra.dataprovider.AcrariumSort
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
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull
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

        }
    }

    @Nested
    inner class Delete {
        // TODO
    }

    @Nested
    inner class CountInRange {
        // TODO
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
            val bugId = testDataBuilder.createBug(appId)
            val version = testDataBuilder.createVersion(appId)
            val report = testDataBuilder.createReport(appId, version = version, content = "{\"CUSTOM_FIELD\": \"customField\"}")
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
}