/*
 * (C) Copyright 2018 Lukas Morawietz (https://github.com/F43nd1r)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.faendir.acra.rest

import com.faendir.acra.domain.ReportService
import com.faendir.acra.persistence.app.AppId
import com.faendir.acra.persistence.bug.BugId
import com.faendir.acra.persistence.report.Report
import com.faendir.acra.persistence.user.UserRepository
import com.faendir.acra.rest.RestReportInterface.Companion.ATTACHMENT
import com.faendir.acra.rest.RestReportInterface.Companion.REPORT
import com.faendir.acra.rest.RestReportInterface.Companion.REPORT_PATH
import com.ninjasquad.springmockk.MockkBean
import com.vaadin.flow.server.auth.ViewAccessChecker
import com.vaadin.flow.spring.security.RequestUtil
import com.vaadin.flow.spring.security.VaadinDefaultRequestCache
import io.mockk.every
import io.mockk.verify
import org.jooq.JSON
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE
import org.springframework.http.MediaType.MULTIPART_FORM_DATA
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant

/**
 * @author lukas
 * @since 26.06.18
 */
@WebMvcTest(controllers = [RestReportInterface::class], includeFilters = [ComponentScan.Filter(classes = [EnableWebSecurity::class])])
@WithMockUser(roles = ["REPORTER", "USER"])
class RestReportInterfaceTest {

    @MockkBean
    lateinit var requestCache: VaadinDefaultRequestCache

    @MockkBean(relaxed = true)
    lateinit var requestUtil: RequestUtil

    @MockkBean(relaxed = true)
    lateinit var viewAccessChecker: ViewAccessChecker

    @MockkBean
    lateinit var userRepository: UserRepository

    @MockkBean
    lateinit var reportService: ReportService

    @Autowired
    lateinit var restReportInterface: RestReportInterface

    @Autowired
    lateinit var mvc: MockMvc

    @BeforeEach
    fun setUp() {
        every { reportService.create(any(), any(), any()) } returns Report(
            "", null, JSON.json("{}"), Instant.now(), null, null, null, null, "", false, "", "", BugId(0), AppId(0), 0, "", "", "", null, null, null
        )
        every { requestCache.getMatchingRequest(any(), any()) } returns null
    }

    @Test
    fun `should accept report without attachments`() {
        mvc.perform(post("/$REPORT_PATH").contentType(APPLICATION_JSON).content(TEST_STRING)).andExpect(status().isOk)
        verify(exactly = 1) { reportService.create(any(), any(), emptyList()) }
    }

    @Test
    fun `should accept report with attachments`() {
        mvc.perform(
            multipart("/$REPORT_PATH").file(MockMultipartFile(REPORT, TEST_STRING, APPLICATION_JSON_VALUE, ByteArray(0)))
                .file(MockMultipartFile(ATTACHMENT, TEST_STRING, APPLICATION_OCTET_STREAM_VALUE, ByteArray(0)))
                .contentType(MULTIPART_FORM_DATA)
        ).andExpect(status().isOk)
        verify(exactly = 1) { reportService.create(any(), any(), any()) }
    }

    companion object {
        private const val TEST_STRING = "TEST"
    }
}