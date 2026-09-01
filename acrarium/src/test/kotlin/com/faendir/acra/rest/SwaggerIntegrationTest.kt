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
import com.faendir.acra.persistence.user.Role
import com.faendir.acra.persistence.user.UserRepository
import com.faendir.acra.withAuth
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.resttestclient.TestRestTemplate
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.test.context.TestPropertySource
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull

@AcrariumTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = ["springdoc.swagger-ui.enabled=true"])
class SwaggerIntegrationTest(
    @Autowired private val userRepository: UserRepository,
    @LocalServerPort private val port: Int,
) {
    private val username = "swagger-admin"
    private val password = "swagger-password"
    private val restTemplate = TestRestTemplate()

    @BeforeEach
    fun setup() {
        withAuth(Role.ADMIN) {
            userRepository.create(username, password, null, Role.ADMIN, Role.USER)
        }
    }

    @Test
    fun `should require admin authentication for swagger ui`() {
        val result = restTemplate.getForEntity("http://localhost:$port/swagger-ui.html", String::class.java)

        expectThat(result.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
    }

    @Test
    fun `should serve swagger ui and openapi document to admins`() {
        val swaggerUi = restTemplate.withBasicAuth(username, password).getForEntity("http://localhost:$port/swagger-ui/index.html", String::class.java)

        expectThat(swaggerUi.statusCode).isEqualTo(HttpStatus.OK)
        expectThat(swaggerUi.body).isNotNull().contains("Swagger UI")
    }
}
