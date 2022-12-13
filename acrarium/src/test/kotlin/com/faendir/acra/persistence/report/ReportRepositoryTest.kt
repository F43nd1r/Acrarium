package com.faendir.acra.persistence.report

import com.faendir.acra.annotation.PersistenceTest
import com.faendir.acra.persistence.TestDataBuilder
import com.faendir.acra.persistence.app.AppId
import com.faendir.acra.persistence.bug.BugId
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

            expectThat(reportRepository.listIds(appId, now.minus(Duration.ofDays(1)), now.plus(Duration.ofDays(1)))).containsExactlyInAnyOrder(reportIn1, reportIn2)
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
                versionCode = version.code,
                versionFlavor = version.flavor,
                stacktrace = "stacktrace",
                exceptionClass = "exceptionClass",
                message = "message",
                crashLine = "crashLine",
                cause = "cause"
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
                versionCode = version.code,
                versionFlavor = version.flavor,
                stacktrace = "stacktrace",
                exceptionClass = "exceptionClass",
                message = null,
                crashLine = null,
                cause = null
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
                        versionCode = version.code,
                        versionFlavor = version.flavor,
                        stacktrace = "stacktrace",
                        exceptionClass = "exceptionClass",
                        message = null,
                        crashLine = null,
                        cause = null
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
                        versionCode = 0,
                        versionFlavor = "",
                        stacktrace = "stacktrace",
                        exceptionClass = "exceptionClass",
                        message = null,
                        crashLine = null,
                        cause = null
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
                        versionCode = version.code,
                        versionFlavor = version.flavor,
                        stacktrace = "stacktrace",
                        exceptionClass = "exceptionClass",
                        message = null,
                        crashLine = null,
                        cause = null
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
                        versionCode = version.code,
                        versionFlavor = version.flavor,
                        stacktrace = "stacktrace",
                        exceptionClass = "exceptionClass",
                        message = null,
                        crashLine = null,
                        cause = null
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
        // TODO
    }
}