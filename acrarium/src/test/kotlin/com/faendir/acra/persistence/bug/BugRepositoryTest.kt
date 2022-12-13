package com.faendir.acra.persistence.bug

import com.faendir.acra.annotation.PersistenceTest
import com.faendir.acra.dataprovider.AcrariumDataProvider
import com.faendir.acra.dataprovider.AcrariumSort
import com.faendir.acra.persistence.TestDataBuilder
import com.faendir.acra.persistence.app.AppId
import com.faendir.acra.persistence.report.ReportRepository
import com.faendir.acra.persistence.version.VersionRepository
import com.faendir.acra.settings.AcrariumConfiguration
import com.ninjasquad.springmockk.MockkBean
import com.vaadin.flow.data.provider.SortDirection
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import strikt.api.expect
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.containsExactlyInAnyOrder
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNotEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.properties.Delegates


@PersistenceTest
class BugRepositoryTest(
    @Autowired
    private val bugRepository: BugRepository,
    @Autowired
    private val testDataBuilder: TestDataBuilder,
    @Autowired
    @MockkBean
    private val acrariumConfiguration: AcrariumConfiguration,
    @Autowired
    @MockkBean
    private val versionRepository: VersionRepository,
    @Autowired
    private val reportRepository: ReportRepository,
) {
    private var appId by Delegates.notNull<AppId>()

    @BeforeEach
    fun setup() {
        appId = testDataBuilder.createApp()
    }

    @Nested
    inner class FindId {
        private var bugId by Delegates.notNull<BugId>()

        @BeforeEach
        fun setup() {
            bugId = testDataBuilder.createBug(appId)
        }

        @Test
        fun `should find matching bug id`() {
            val exceptionClass = "exceptionClass"
            val message = "message"
            val crashLine = "crashLine"
            val cause = "cause"
            testDataBuilder.createBugIdentifier(appId, bugId, exceptionClass, message, crashLine, cause)

            expectThat(bugRepository.findId(BugIdentifier(appId, exceptionClass, message, crashLine, cause))).isEqualTo(bugId)
        }

        @Test
        fun `should find matching bug id with null properties`() {
            val exceptionClass = "exceptionClass"
            val message = null
            val crashLine = null
            val cause = null
            testDataBuilder.createBugIdentifier(appId, bugId, exceptionClass, message, crashLine, cause)

            expectThat(bugRepository.findId(BugIdentifier(appId, exceptionClass, message, crashLine, cause))).isEqualTo(bugId)
        }

        @Test
        fun `should not find identifiers with a difference`() {
            val exceptionClass = "exceptionClass"
            val message = "message"
            val crashLine = "crashLine"
            val cause = "cause"
            testDataBuilder.createBugIdentifier(testDataBuilder.createApp(), bugId, exceptionClass, message, crashLine, cause)
            testDataBuilder.createBugIdentifier(appId, bugId, "other", message, crashLine, cause)
            testDataBuilder.createBugIdentifier(appId, bugId, exceptionClass, "other", crashLine, cause)
            testDataBuilder.createBugIdentifier(appId, bugId, exceptionClass, message, "other", cause)
            testDataBuilder.createBugIdentifier(appId, bugId, exceptionClass, message, crashLine, "other")

            expectThat(bugRepository.findId(BugIdentifier(appId, exceptionClass, message, crashLine, cause))).isNull()
        }
    }

    @Nested
    inner class Find {
        @Test
        fun `should find bug by id`() {
            val id = testDataBuilder.createBug(appId)

            expectThat(bugRepository.find(id)).isNotNull()
        }

        @Test
        fun `should return null if id does not exist`() {
            expectThat(bugRepository.find(BugId(1))).isNull()
        }
    }

    @Nested
    inner class GetAllIds {
        @Test
        fun `should return all ids for app`() {
            val bug = testDataBuilder.createBug(appId)
            testDataBuilder.createBug()

            expectThat(bugRepository.getAllIds(appId)).containsExactly(bug)
        }
    }

    @Nested
    inner class Create {
        @Test
        fun `should create bug and identifier`() {
            val identifier = BugIdentifier(appId, "exceptionClass", "message", "crashLine", "cause")

            val id = bugRepository.create(identifier, "title")

            expectThat(bugRepository.findId(identifier)).isEqualTo(id)
            expectThat(bugRepository.find(id)).isEqualTo(Bug(id, "title", appId, 0, null, null, null, null, null, 0))
        }
    }

    @Nested
    inner class SetSolved {
        @Test
        fun `should set and reset solved version`() {
            val id = testDataBuilder.createBug(appId)
            val version = testDataBuilder.createVersion(appId)

            expectThat(bugRepository.find(id)).isNotNull().and {
                get { solvedVersionCode }.isNull()
                get { solvedVersionFlavor }.isNull()
            }

            bugRepository.setSolved(appId, id, version.code to version.flavor)

            expectThat(bugRepository.find(id)).isNotNull().and {
                get { solvedVersionCode }.isEqualTo(version.code)
                get { solvedVersionFlavor }.isEqualTo(version.flavor)
            }

            bugRepository.setSolved(appId, id, null)

            expectThat(bugRepository.find(id)).isNotNull().and {
                get { solvedVersionCode }.isNull()
                get { solvedVersionFlavor }.isNull()
            }
        }
    }

    @Nested
    inner class MergeBugs {
        @Test
        fun `should update title`() {
            val bug1 = testDataBuilder.createBug(appId)
            val id1 = testDataBuilder.createBugIdentifier(appId, bug1)
            val bug2 = testDataBuilder.createBug(appId)
            val id2 = testDataBuilder.createBugIdentifier(appId, bug2)

            bugRepository.mergeBugs(appId, setOf(bug1, bug2), "newTitle")

            expectThat(bugRepository.find(bugRepository.findId(id1)!!)!!.title).isEqualTo("newTitle")
            expectThat(bugRepository.find(bugRepository.findId(id2)!!)!!.title).isEqualTo("newTitle")
        }

        @Test
        fun `should update references and remove old bug`() {
            val bug1 = testDataBuilder.createBug(appId)
            val id1 = testDataBuilder.createBugIdentifier(appId, bug1)
            val report1 = testDataBuilder.createReport(appId, bug1, id1)
            val bug2 = testDataBuilder.createBug(appId)
            val id2 = testDataBuilder.createBugIdentifier(appId, bug2)
            val report2 = testDataBuilder.createReport(appId, bug2, id2)

            expectThat(bugRepository.getAllIds(appId)).hasSize(2)

            bugRepository.mergeBugs(appId, setOf(bug1, bug2), "")

            val id = bugRepository.findId(id1)
            expectThat(bugRepository.findId(id2)).isEqualTo(id)
            expectThat(reportRepository.find(report1)!!.bugId).isEqualTo(id)
            expectThat(reportRepository.find(report2)!!.bugId).isEqualTo(id)
            expectThat(bugRepository.getAllIds(appId)).hasSize(1)
        }

        @Test
        fun `should update bug stats`() {
            val bug1 = testDataBuilder.createBug(appId)
            val id1 = testDataBuilder.createBugIdentifier(appId, bug1)
            val v1 = testDataBuilder.createVersion(appId, 1, "1")
            val d1 = OffsetDateTime.of(2020, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC).toInstant()
            testDataBuilder.createReport(appId, bug1, id1, v1, d1)
            val bug2 = testDataBuilder.createBug(appId)
            val id2 = testDataBuilder.createBugIdentifier(appId, bug2)
            val v2 = testDataBuilder.createVersion(appId, 2, "2")
            val d2 = OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC).toInstant()
            testDataBuilder.createReport(appId, bug2, id2, v2, d2)

            expectThat(bugRepository.find(bug1)!!) {
                get { latestVersionCode }.isEqualTo(v1.code)
                get { latestVersionFlavor }.isEqualTo(v1.flavor)
                get { latestReport }.isEqualTo(d1)
                get { reportCount }.isEqualTo(1)
                get { affectedInstallations }.isEqualTo(1)
            }

            bugRepository.mergeBugs(appId, setOf(bug1, bug2), "")

            expectThat(bugRepository.find(bugRepository.findId(id1)!!)!!) {
                get { latestVersionCode }.isEqualTo(v2.code)
                get { latestVersionFlavor }.isEqualTo(v2.flavor)
                get { latestReport }.isEqualTo(d2)
                get { reportCount }.isEqualTo(2)
                get { affectedInstallations }.isEqualTo(2)
            }
        }
    }

    @Nested
    inner class SplitFromBug {
        @Test
        fun `should create new bug and update references`() {
            val bug = testDataBuilder.createBug(appId)
            val id1 = testDataBuilder.createBugIdentifier(appId, bug)
            val report1 = testDataBuilder.createReport(appId, bug, id1)
            val id2 = testDataBuilder.createBugIdentifier(appId, bug)
            val report2 = testDataBuilder.createReport(appId, bug, id2)

            expectThat(bugRepository.getAllIds(appId)).hasSize(1)

            bugRepository.splitFromBug(bug, id1)

            expectThat(bugRepository.getAllIds(appId)).hasSize(2)
            expectThat(reportRepository.find(report1)!!.bugId).isNotEqualTo(bug)
            expectThat(reportRepository.find(report2)!!.bugId).isEqualTo(bug)
        }

        @Test
        fun `should update bug stats`() {
            val bug = testDataBuilder.createBug(appId)
            val id1 = testDataBuilder.createBugIdentifier(appId, bug)
            val v1 = testDataBuilder.createVersion(appId, 1, "1")
            val d1 = OffsetDateTime.of(2020, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC).toInstant()
            val report1 = testDataBuilder.createReport(appId, bug, id1, v1, d1)
            val id2 = testDataBuilder.createBugIdentifier(appId, bug)
            val v2 = testDataBuilder.createVersion(appId, 2, "2")
            val d2 = OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC).toInstant()
            val report2 = testDataBuilder.createReport(appId, bug, id2, v2, d2)

            expectThat(bugRepository.find(bug)).isNotNull().and {
                get { latestVersionCode }.isEqualTo(v2.code)
                get { latestVersionFlavor }.isEqualTo(v2.flavor)
                get { latestReport }.isEqualTo(d2)
                get { reportCount }.isEqualTo(2)
                get { affectedInstallations }.isEqualTo(2)
            }

            bugRepository.splitFromBug(bug, id2)

            expect {
                println(reportRepository.find(report1))
                println(reportRepository.find(report2))
                expectThat(bugRepository.find(bug)).isNotNull().and {
                    get { latestVersionCode }.isEqualTo(v1.code)
                    get { latestVersionFlavor }.isEqualTo(v1.flavor)
                    get { latestReport }.isEqualTo(d1)
                    get { reportCount }.isEqualTo(1)
                    get { affectedInstallations }.isEqualTo(1)
                }

                expectThat(bugRepository.find(bugRepository.findId(id2)!!)).isNotNull().and {
                    get { latestVersionCode }.isEqualTo(v2.code)
                    get { latestVersionFlavor }.isEqualTo(v2.flavor)
                    get { latestReport }.isEqualTo(d2)
                    get { reportCount }.isEqualTo(1)
                    get { affectedInstallations }.isEqualTo(1)
                }
            }
        }
    }

    @Nested
    inner class Delete {
        @Test
        fun `should delete bug`() {
            val id = testDataBuilder.createBug(appId)

            expectThat(bugRepository.find(id)).isNotNull()

            bugRepository.delete(appId, id)

            expectThat(bugRepository.find(id)).isNull()
        }

        @Test
        fun `should ignore unknown id`() {
            val id = BugId(1)

            expectThat(bugRepository.find(id)).isNull()

            bugRepository.delete(appId, id)

            expectThat(bugRepository.find(id)).isNull()
        }
    }

    @Nested
    inner class Provider {
        private lateinit var provider: AcrariumDataProvider<BugStats, BugStats.Filter, BugStats.Sort>

        @BeforeEach
        fun setup() {
            provider = bugRepository.getProvider(appId)
        }

        @Test
        fun `should return all bugs for app`() {
            val bug1 = testDataBuilder.createBug(appId, "bug1")
            val v1 = testDataBuilder.createVersion(appId, 1, "one")
            val d1 = OffsetDateTime.of(2020, 2, 1, 14, 5, 6, 0, ZoneOffset.UTC).toInstant()
            testDataBuilder.createReport(appId, bug1, version = v1, date = d1)
            val v2 = testDataBuilder.createVersion(appId, 2, "two")
            val d2 = OffsetDateTime.of(2020, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC).toInstant()
            testDataBuilder.createReport(appId, bug1, version = v2, date = d2)
            val bug2 = testDataBuilder.createBug(appId, "bug2")
            val d3 = OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC).toInstant()
            testDataBuilder.createReport(appId, bug2, version = v1, date = d3)
            bugRepository.setSolved(appId, bug2, v2.code to v2.flavor)

            expectThat(provider.size(emptySet())).isEqualTo(2)
            expectThat(provider.fetch(emptySet(), emptyList(), 0, 10).toList()).containsExactlyInAnyOrder(
                BugStats(bug1, "bug1", 2, 2, "two", d1, null, null, 2),
                BugStats(bug2, "bug2", 1, 1, "one", d3, v2.code, v2.flavor, 1)
            )
        }

        @Test
        fun `should sort returned bugs`() {
            val bug1 = testDataBuilder.createBug(appId, "bug1")
            val bug2 = testDataBuilder.createBug(appId, "bug2")

            expectThat(provider.fetch(emptySet(), listOf(AcrariumSort(BugStats.Sort.TITLE, SortDirection.ASCENDING)), 0, 10).toList().map { it.id })
                .containsExactly(bug1, bug2)
        }

        @Test
        fun `should offset and limit returned bugs`() {
            val bug1 = testDataBuilder.createBug(appId, "bug1")
            val bug2 = testDataBuilder.createBug(appId, "bug2")

            expectThat(provider.fetch(emptySet(), listOf(AcrariumSort(BugStats.Sort.TITLE, SortDirection.ASCENDING)), 0, 1).toList().map { it.id })
                .containsExactly(bug1)
            expectThat(provider.fetch(emptySet(), listOf(AcrariumSort(BugStats.Sort.TITLE, SortDirection.ASCENDING)), 1, 1).toList().map { it.id })
                .containsExactly(bug2)
        }

        @Test
        fun `should filter returned bugs`() {
            val bug1 = testDataBuilder.createBug(appId, "bug1")
            val v1 = testDataBuilder.createVersion(appId, 1, "one")
            val d1 = OffsetDateTime.of(2020, 2, 1, 14, 5, 6, 0, ZoneOffset.UTC).toInstant()
            testDataBuilder.createReport(appId, bug1, version = v1, date = d1)
            val v2 = testDataBuilder.createVersion(appId, 2, "two")
            val d2 = OffsetDateTime.of(2020, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC).toInstant()
            testDataBuilder.createReport(appId, bug1, version = v2, date = d2)
            val bug2 = testDataBuilder.createBug(appId, "bug2")
            val d3 = OffsetDateTime.of(2021, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC).toInstant()
            testDataBuilder.createReport(appId, bug2, version = v1, date = d3)
            bugRepository.setSolved(appId, bug2, v2.code to v2.flavor)
            val bug3 = testDataBuilder.createBug(appId, "bug3")
            testDataBuilder.createReport(appId, bug3, version = v1, date = d1)
            testDataBuilder.createReport(appId, bug3, version = v2, date = d2)
            bugRepository.setSolved(appId, bug3, v1.code to v1.flavor)

            expectThat(provider.size(setOf(BugStats.Filter.LATEST_VERSION(2, "two")))).isEqualTo(2)
            expectThat(provider.fetch(setOf(BugStats.Filter.LATEST_VERSION(2, "two")), emptyList(), 0, 10).toList().map { it.id })
                .containsExactly(bug1, bug3)

            expectThat(provider.size(setOf(BugStats.Filter.TITLE("bug1")))).isEqualTo(1)
            expectThat(provider.fetch(setOf(BugStats.Filter.TITLE("bug1")), emptyList(), 0, 10).toList().map { it.id })
                .containsExactly(bug1)

            expectThat(provider.size(setOf(BugStats.Filter.IS_NOT_SOLVED_OR_REGRESSION))).isEqualTo(2)
            expectThat(provider.fetch(setOf(BugStats.Filter.IS_NOT_SOLVED_OR_REGRESSION), emptyList(), 0, 10).toList().map { it.id })
                .containsExactly(bug1, bug3)
        }
    }
}