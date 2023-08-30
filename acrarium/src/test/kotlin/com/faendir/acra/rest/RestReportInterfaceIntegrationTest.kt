/*
 * (C) Copyright 2023 Lukas Morawietz (https://github.com/F43nd1r)
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
package com.faendir.acra.rest

import com.faendir.acra.DatabaseTestConfiguration
import com.faendir.acra.persistence.app.AppRepository
import com.faendir.acra.persistence.app.Reporter
import com.faendir.acra.persistence.user.Role
import com.faendir.acra.withAuth
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.core.io.ClassPathResource
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import strikt.api.expectThat
import strikt.assertions.isEqualTo

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(DatabaseTestConfiguration::class)
class RestReportInterfaceIntegrationTest(
    @Autowired private val restTemplate: TestRestTemplate,
    @Autowired private val appRepository: AppRepository,
    @LocalServerPort private val port: Int,
) {

    private lateinit var reporter: Reporter

    @BeforeEach
    fun setup() {
        withAuth(Role.ADMIN) {
            reporter = appRepository.create("test")
        }
    }

    @Test
    fun `should be able to submit report`() {
        val headers = HttpHeaders()
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        val result = restTemplate
            .withBasicAuth(reporter.username, reporter.rawPassword)
            .postForEntity(
                "http://localhost:$port/${RestReportInterface.REPORT_PATH}",
                HttpEntity(ClassPathResource("example.stacktrace").contentAsByteArray, headers),
                Void::class.java
            )

        expectThat(result.statusCode).isEqualTo(HttpStatus.OK)
    }
}