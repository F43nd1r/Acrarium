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

import com.faendir.acra.model.App
import com.faendir.acra.rest.RestReportInterface.Companion.ATTACHMENT
import com.faendir.acra.rest.RestReportInterface.Companion.REPORT
import com.faendir.acra.rest.RestReportInterface.Companion.REPORT_PATH
import com.faendir.acra.service.DataService
import com.faendir.acra.service.UserService
import com.ninjasquad.springmockk.MockkBean
import com.vaadin.flow.server.auth.ViewAccessChecker
import com.vaadin.flow.spring.security.RequestUtil
import com.vaadin.flow.spring.security.VaadinDefaultRequestCache
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.http.MediaType.*
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

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
    lateinit var userService: UserService

    @MockkBean
    lateinit var dataService: DataService

    @Autowired
    lateinit var restReportInterface: RestReportInterface

    @Autowired
    lateinit var mvc: MockMvc

    @BeforeEach
    fun setUp() {
        val app = mockk<App>()
        every { dataService.findApp(TEST_STRING) } returns app
        every { dataService.createNewReport(any(), any(), any()) } just runs
        every { requestCache.getMatchingRequest(any(), any()) } returns null
    }

    @Test
    fun report() {
        mvc.perform(post("/$REPORT_PATH").contentType(APPLICATION_JSON).content(TEST_STRING)).andExpect(status().isOk)
        verify(exactly = 1) { dataService.createNewReport(any(), any(), any()) }
    }

    @Test
    fun report2() {
        mvc.perform(multipart("/$REPORT_PATH").file(MockMultipartFile(REPORT, TEST_STRING, APPLICATION_JSON_VALUE, ByteArray(0)))
                .file(MockMultipartFile(ATTACHMENT, TEST_STRING, APPLICATION_OCTET_STREAM_VALUE, ByteArray(0)))
                .contentType(MULTIPART_FORM_DATA)).andExpect(status().isOk)
        verify(exactly = 1) { dataService.createNewReport(any(), any(), any()) }
    }

    companion object {
        private const val TEST_STRING = "TEST"
    }
}