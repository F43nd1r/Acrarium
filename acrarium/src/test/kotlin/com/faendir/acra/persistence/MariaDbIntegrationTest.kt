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
package com.faendir.acra.persistence

import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jooq.test.autoconfigure.JooqTest
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Import
import org.testcontainers.containers.MariaDBContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import strikt.api.expectThat
import strikt.assertions.isEqualTo

@JooqTest
@Testcontainers
@Import(PersistenceTestConfiguration::class)
class MariaDbIntegrationTest(
    @Autowired private val jooq: DSLContext,
) {
    @Test
    fun `should auto detect mariadb`() {
        jooq.connection {
            expectThat(it.metaData.driverName).isEqualTo("MariaDB Connector/J")
        }
        expectThat(jooq.configuration().dialect()).isEqualTo(SQLDialect.MARIADB)
    }

    companion object {
        @Container
        @ServiceConnection
        @JvmStatic
        val database: MariaDBContainer<*> = MariaDBContainer(DockerImageName.parse("mariadb:12.3"))
            .withDatabaseName("acrarium")
    }
}
