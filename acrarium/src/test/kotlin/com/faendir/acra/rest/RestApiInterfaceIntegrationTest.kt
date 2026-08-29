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
package com.faendir.acra.rest

import com.faendir.acra.annotation.AcrariumTest
import com.faendir.acra.persistence.TestDataBuilder
import com.faendir.acra.persistence.app.AppId
import com.faendir.acra.persistence.bug.BugId
import com.faendir.acra.persistence.user.Permission
import com.faendir.acra.persistence.user.Role
import com.faendir.acra.persistence.user.UserRepository
import com.faendir.acra.withAuth
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.TestRestTemplate
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.startsWith
import tools.jackson.databind.JsonNode
import tools.jackson.databind.node.ArrayNode
import kotlin.properties.Delegates

@AcrariumTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class RestApiInterfaceIntegrationTest(
    @Autowired private val testDataBuilder: TestDataBuilder,
    @Autowired private val userRepository: UserRepository,
    @LocalServerPort private val port: Int,
) {
    private val username = "test-username"
    private val password = "test-password"
    private val restTemplate = TestRestTemplate().withBasicAuth(username, password)
    private var appId by Delegates.notNull<AppId>()
    private var bugId by Delegates.notNull<BugId>()
    private lateinit var reportId: String

    @BeforeEach
    fun setup() {
        withAuth(Role.ADMIN) {
            userRepository.create(username, password, null, Role.USER, Role.API)
            appId = testDataBuilder.createApp()
            bugId = testDataBuilder.createBug(appId)
            reportId = testDataBuilder.createReport(appId, bugId)
            testDataBuilder.createPermission(username, appId, Permission.Level.VIEW)
        }
    }

    @Test
    fun `should return 200 with report for known report id`() {
        val result = restTemplate.getForEntity("http://localhost:$port/api/reports/$reportId", JsonNode::class.java)

        expectThat(result.statusCode).isEqualTo(HttpStatus.OK)
        expectThat(result.body).isNotNull().and {
            get { this["id"].asString() }.isEqualTo(reportId)
            get { this["bugId"].asInt() }.isEqualTo(bugId.value)
            get { this["appId"].asInt() }.isEqualTo(appId.value)
            get { this["content"]["REPORT_ID"].asString() }.isEqualTo(reportId)
        }
    }

    @Test
    fun `should return 200 with bug for known bug id`() {
        val result = restTemplate.getForEntity("http://localhost:$port/api/bugs/$bugId", JsonNode::class.java)

        expectThat(result.statusCode).isEqualTo(HttpStatus.OK)
        expectThat(result.body).isNotNull().and {
            get { this["id"].asInt() }.isEqualTo(bugId.value)
            get { this["appId"].asInt() }.isEqualTo(appId.value)
            get { this["title"].asString() }.startsWith("title-")
            get { this["reportCount"].asInt() }.isEqualTo(1)
        }
    }

    @Test
    fun `should return 200 with app for known app id`() {
        val result = restTemplate.getForEntity("http://localhost:$port/api/apps/$appId", JsonNode::class.java)

        expectThat(result.statusCode).isEqualTo(HttpStatus.OK)
        expectThat(result.body).isNotNull().and {
            get { this["id"].asInt() }.isEqualTo(appId.value)
            get { this["name"].asString() }.startsWith("test-app-")
        }
    }

    @Test
    fun `should list report ids`() {
        val result = restTemplate.getForEntity("http://localhost:$port/api/apps/$appId/reports", ArrayNode::class.java)

        expectThat(result.statusCode).isEqualTo(HttpStatus.OK)
        expectThat(result.body).isNotNull().and {
            get { size() }.isEqualTo(1)
            get { first().asString() }.isEqualTo(reportId)
        }
    }

    @Test
    fun `should list bug ids`() {
        val result = restTemplate.getForEntity("http://localhost:$port/api/apps/$appId/bugs", ArrayNode::class.java)

        expectThat(result.statusCode).isEqualTo(HttpStatus.OK)
        expectThat(result.body).isNotNull().and {
            get { size() }.isEqualTo(1)
            get { first().asInt() }.isEqualTo(bugId.value)
        }
    }

    @Test
    fun `should list app ids`() {
        val result = restTemplate.getForEntity("http://localhost:$port/api/apps", ArrayNode::class.java)

        expectThat(result.statusCode).isEqualTo(HttpStatus.OK)
        expectThat(result.body).isNotNull().and {
            get { size() }.isEqualTo(1)
            get { first().asInt() }.isEqualTo(appId.value)
        }
    }

    @Nested
    inner class User {
        @Test
        fun `should return 403 for unknown report id`() {
            val result = restTemplate.getForEntity("http://localhost:$port/api/reports/unknown", JsonNode::class.java)

            expectThat(result.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
        }

        @Test
        fun `should return 403 for unknown bug id`() {
            val result = restTemplate.getForEntity("http://localhost:$port/api/bugs/999", JsonNode::class.java)

            expectThat(result.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
        }

        @Test
        fun `should return 403 for unknown app id for users`() {
            val result = restTemplate.getForEntity("http://localhost:$port/api/apps/999", String::class.java)

            expectThat(result.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
        }
    }

    @Nested
    inner class Admin {
        @BeforeEach
        fun setup() {
            withAuth(Role.ADMIN) {
                userRepository.setRole(username, Role.ADMIN, enabled = true)
            }
        }

        @Test
        fun `should return 404 for unknown report id`() {
            val result = restTemplate.getForEntity("http://localhost:$port/api/reports/unknown", JsonNode::class.java)

            expectThat(result.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        }

        @Test
        fun `should return 404 for unknown bug id`() {
            val result = restTemplate.getForEntity("http://localhost:$port/api/bugs/999", JsonNode::class.java)

            expectThat(result.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        }

        @Test
        fun `should return 404 for unknown app id for users`() {
            val result = restTemplate.getForEntity("http://localhost:$port/api/apps/999", String::class.java)

            expectThat(result.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
        }
    }
}
