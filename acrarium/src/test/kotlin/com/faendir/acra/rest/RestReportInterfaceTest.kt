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
import com.faendir.acra.rest.RestReportInterface.Companion.EXPORT_PATH
import com.faendir.acra.rest.RestReportInterface.Companion.PARAM_APP
import com.faendir.acra.rest.RestReportInterface.Companion.PARAM_ID
import com.faendir.acra.rest.RestReportInterface.Companion.PARAM_MAIL
import com.faendir.acra.rest.RestReportInterface.Companion.REPORT
import com.faendir.acra.rest.RestReportInterface.Companion.REPORT_PATH
import com.faendir.acra.service.DataService
import com.faendir.acra.service.UserService
import com.ninjasquad.springmockk.MockkBean
import com.querydsl.core.types.Expression
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.json.JSONArray
import org.json.JSONObject
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import strikt.api.expectThat
import strikt.assertions.isA
import strikt.assertions.isEqualTo

/**
 * @author lukas
 * @since 26.06.18
 */
@WebMvcTest(controllers = [RestReportInterface::class], includeFilters = [ComponentScan.Filter(classes = [EnableWebSecurity::class])])
@WithMockUser(roles = ["REPORTER", "USER"])
class RestReportInterfaceTest {

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
        every { dataService.getFromReports(any(), any(), any<Expression<Any>>()) } returns listOf("{\"name\":\"a\"}", "{\"name\":\"b\"}")
        every { dataService.createNewReport(any(), any(), any()) } just runs
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

    @Test
    fun exportWithId() {
        mvc.perform(get("/$EXPORT_PATH").param(PARAM_APP, TEST_STRING).param(PARAM_ID, TEST_STRING)).andExpect(status().isOk).andExpect {
            val array = JSONArray(it.response.contentAsString)
            expectThat(array) {
                get(JSONArray::length).isEqualTo(2)
                get { get(0) }.isA<JSONObject>()
            }
        }
    }

    @Test
    fun exportWithMail() {
        mvc.perform(get("/$EXPORT_PATH").param(PARAM_APP, TEST_STRING).param(PARAM_MAIL, TEST_STRING)).andExpect(status().isOk).andExpect {
            expectThat(JSONArray(it.response.contentAsString)) {
                get(JSONArray::length).isEqualTo(2)
                get { get(0) }.isA<JSONObject>()
            }
        }
    }

    @Test
    fun exportInvalid() {
        mvc.perform(get("/$EXPORT_PATH").param(PARAM_APP, TEST_STRING)).andExpect(status().is4xxClientError)
        mvc.perform(get("/$EXPORT_PATH").param(PARAM_ID, TEST_STRING)).andExpect(status().is4xxClientError)
        mvc.perform(get("/$EXPORT_PATH").param(PARAM_MAIL, TEST_STRING)).andExpect(status().is4xxClientError)
        mvc.perform(get("/$EXPORT_PATH")).andExpect(status().is4xxClientError)
    }

    companion object {
        private const val TEST_STRING = "TEST"
    }
}