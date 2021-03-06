package com.faendir.acra.service

import com.faendir.acra.annotation.AcrariumTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEmpty

@AcrariumTest
@SpringBootTest
@ContextConfiguration
@WithMockUser(roles = ["REPORTER", "USER", "ADMIN"])
class VersionDeleteTest {

    @Autowired
    private lateinit var dataService: DataService

    @Test
    fun testVersionDelete() {
        val reporter = dataService.createNewApp("TEST")
        dataService.createNewReport(reporter.username, javaClass.classLoader.getResourceAsStream("example.stacktrace")!!.bufferedReader().readText(),
                emptyList())
        expectThat(dataService.findAllApps()).hasSize(1)
        val app = dataService.findAllApps().first()
        expectThat(dataService.getBugIds(app)).hasSize(1)
        val bug = dataService.findBug(dataService.getBugIds(app).first())!!
        expectThat(dataService.getStacktraceIds(bug)).hasSize(1)
        val stacktrace = dataService.getStacktraces(bug).first()
        expectThat(dataService.getReportIds(stacktrace)).hasSize(1)
        expectThat(dataService.findAllVersions(app)).hasSize(1)
        val version = dataService.findAllVersions(app).first()
        dataService.deleteVersion(version)
        expectThat(dataService.findAllApps()).hasSize(1)
        expectThat(dataService.getBugIds(app)).hasSize(1)
        expectThat(dataService.getStacktraceIds(bug)).isEmpty()
        expectThat(dataService.getReportIds(stacktrace)).isEmpty()
        expectThat(dataService.findAllVersions(app)).isEmpty()
    }

}