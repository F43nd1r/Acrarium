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

import liquibase.Contexts
import liquibase.LabelExpression
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import org.junit.jupiter.api.Test
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.utility.DockerImageName
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class LiquibaseMigrationTest {
    @Test
    fun `should migrate reports with multiple devices for one model`() {
        MySQLContainer(DockerImageName.parse("mysql:8.0.39"))
            .withDatabaseName("acrarium")
            .withCommand("--log-bin-trust-function-creators=1")
            .use { container ->
                container.start()
                container.createConnection("").use { connection ->
                    val database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(JdbcConnection(connection))
                    Liquibase("db/db.changelog-master.yml", ClassLoaderResourceAccessor(), database).use { liquibase ->
                        // The first 18 changesets are the complete 1.9 schema.
                        liquibase.update(18, Contexts(), LabelExpression())
                        connection.createStatement().use { statement ->
                            statement.executeUpdate("INSERT INTO user (username, password) VALUES ('reporter', 'password')")
                            statement.executeUpdate("INSERT INTO app (name, reporter_username) VALUES ('app', 'reporter')")
                            statement.executeUpdate("INSERT INTO version (code, name, app_id) VALUES (1, 'version', 1)")
                            statement.executeUpdate("INSERT INTO bug (title, app_id) VALUES ('bug', 1)")
                            statement.executeUpdate("INSERT INTO stacktrace (bug_id, stacktrace, version_id, class) VALUES (1, 'Exception: message', 1, 'Exception')")
                            statement.executeUpdate(
                                "INSERT INTO report (id, content, date, phone_model, installation_id, stacktrace_id, device, is_silent) " +
                                        "VALUES ('report', JSON_OBJECT('STACK_TRACE', 'Exception: message'), NOW(), 'model', 'installation', 1, 'device-a', FALSE)"
                            )
                            statement.executeUpdate("INSERT INTO device (device, model, marketing_name) VALUES ('device-a', 'model', 'Correct device')")
                            statement.executeUpdate("INSERT INTO device (device, model, marketing_name) VALUES ('device-b', 'model', 'Other device')")
                        }

                        liquibase.update(Contexts(), LabelExpression())

                        connection.createStatement().use { statement ->
                            statement.executeQuery("SELECT marketing_device FROM report WHERE id = 'report'").use { result ->
                                result.next()
                                expectThat(result.getString("marketing_device")).isEqualTo("Correct device")
                            }
                            statement.executeUpdate(
                                "UPDATE DATABASECHANGELOG SET MD5SUM = '9:4ba63fc628568ef898e8c650fe6425e3' " +
                                        "WHERE id = '2.0.0-expand-report-table' AND author = 'f43nd1r'"
                            )
                        }

                        liquibase.update(Contexts(), LabelExpression())
                    }
                }
            }
    }
}
