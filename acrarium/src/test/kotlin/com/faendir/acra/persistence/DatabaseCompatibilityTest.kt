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

import com.faendir.acra.domain.ReportService
import com.faendir.acra.jooq.generated.tables.references.*
import com.faendir.acra.persistence.app.AppIdConverter
import com.faendir.acra.persistence.app.AppRepository
import com.faendir.acra.persistence.bug.BugId
import com.faendir.acra.persistence.bug.BugIdConverter
import com.faendir.acra.persistence.bug.BugIdentifier
import com.faendir.acra.persistence.bug.BugRepository
import com.faendir.acra.persistence.device.DeviceRepository
import com.faendir.acra.persistence.jooq.CustomConverterProvider
import com.faendir.acra.persistence.jooq.InstantConverter
import com.faendir.acra.persistence.jooq.JooqConfigurationCustomizer
import com.faendir.acra.persistence.report.ReportRepository
import com.faendir.acra.persistence.user.UserRepository
import com.faendir.acra.persistence.version.VersionRepository
import com.faendir.acra.security.BasicSecurityConfiguration
import com.faendir.acra.settings.AcrariumConfiguration
import com.faendir.acra.util.YamlPropertySourceFactory
import com.zaxxer.hikari.HikariDataSource
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties
import org.springframework.boot.jdbc.autoconfigure.DataSourceTransactionManagerAutoConfiguration
import org.springframework.boot.jooq.autoconfigure.JooqAutoConfiguration
import org.springframework.boot.jooq.autoconfigure.JooqProperties
import org.springframework.boot.liquibase.autoconfigure.LiquibaseAutoConfiguration
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.boot.transaction.autoconfigure.TransactionAutoConfiguration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.PropertySource
import org.springframework.core.env.StandardEnvironment
import org.springframework.core.env.SystemEnvironmentPropertySource
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import org.testcontainers.containers.JdbcDatabaseContainer
import org.testcontainers.containers.MariaDBContainer
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.utility.DockerImageName
import tools.jackson.databind.json.JsonMapper
import java.sql.SQLException
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/** Uses ordinary JDBC URLs and the shipped defaults, without the MySQL-only DatabaseTestConfiguration. */
class DatabaseCompatibilityTest {
    enum class Database(val driver: String, val dialect: SQLDialect) {
        MYSQL("com.mysql.cj.jdbc.Driver", SQLDialect.MYSQL),
        MARIADB("org.mariadb.jdbc.Driver", SQLDialect.MARIADB);

        fun container(): JdbcDatabaseContainer<*> = when (this) {
            MYSQL -> MySQLContainer(DockerImageName.parse("mysql:8.0.39"))
                .withCommand("--log-bin-trust-function-creators=1")
            MARIADB -> MariaDBContainer(DockerImageName.parse("mariadb:11.8.3"))
        }
    }

    private fun runner(container: JdbcDatabaseContainer<*>, overrides: Map<String, Any> = emptyMap()) =
        ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                DataSourceAutoConfiguration::class.java,
                DataSourceTransactionManagerAutoConfiguration::class.java,
                TransactionAutoConfiguration::class.java,
                JooqAutoConfiguration::class.java,
                LiquibaseAutoConfiguration::class.java,
            ))
            .withUserConfiguration(PersistenceConfiguration::class.java)
            .withInitializer { context ->
                // Exercise Spring's real environment-variable binding, including SQLDIALECT.
                context.environment.propertySources.replace(
                    StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                    SystemEnvironmentPropertySource(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, mapOf<String, Any>(
                        "SPRING_DATASOURCE_URL" to container.jdbcUrl,
                        "SPRING_DATASOURCE_USERNAME" to container.username,
                        "SPRING_DATASOURCE_PASSWORD" to container.password,
                    ) + overrides)
                )
            }

    @ParameterizedTest
    @EnumSource(Database::class)
    fun `select driver and dialect from URL and preserve explicit environment overrides`(database: Database) {
        database.container().use { container ->
            container.start()
            runner(container).withPropertyValues("spring.liquibase.enabled=false").run { context ->
                assertThat(context).hasNotFailed()
                assertThat(context.getBean(HikariDataSource::class.java).driverClassName).isEqualTo(database.driver)
                assertThat(context.getBean(DSLContext::class.java).dialect()).isEqualTo(database.dialect)
                assertThat(context.getBean(DataSourceProperties::class.java).driverClassName).isNull()
                assertThat(context.getBean(JooqProperties::class.java).sqlDialect).isNull()
            }
            // Also use a different dialect to prove an explicit choice wins over detection.
            for (dialect in listOf(database.dialect, SQLDialect.DEFAULT)) {
                runner(container, mapOf(
                    "SPRING_DATASOURCE_DRIVERCLASSNAME" to database.driver,
                    "SPRING_JOOQ_SQLDIALECT" to dialect.name,
                )).withPropertyValues("spring.liquibase.enabled=false").run { context ->
                    assertThat(context).hasNotFailed()
                    assertThat(context.getBean(DataSourceProperties::class.java).driverClassName).isEqualTo(database.driver)
                    assertThat(context.getBean(HikariDataSource::class.java).driverClassName).isEqualTo(database.driver)
                    assertThat(context.getBean(JooqProperties::class.java).sqlDialect).isEqualTo(dialect)
                    assertThat(context.getBean(DSLContext::class.java).dialect()).isEqualTo(dialect)
                }
            }
        }
    }

    @ParameterizedTest
    @EnumSource(Database::class)
    fun `save reports and versions atomically and reopen populated database`(database: Database) {
        database.container().use { container ->
            container.start()
            val application = runner(container)
            var savedBugId: BugId? = null
            var migrationCount = 0
            application.run { context ->
                assertThat(context).hasNotFailed()
                val jooq = context.getBean(DSLContext::class.java)
                val dataSource = context.getBean(HikariDataSource::class.java)
                assertThat(dataSource.driverClassName).isEqualTo(database.driver)
                assertThat(jooq.dialect()).isEqualTo(database.dialect)
                dataSource.connection.use { connection ->
                    println("Compatibility: ${connection.metaData.databaseProductVersion}; " +
                            "${connection.metaData.driverName} ${connection.metaData.driverVersion}; jOOQ ${org.jooq.Constants.VERSION}")
                }
                val data = context.getBean(TestDataBuilder::class.java)
                val reporter = data.createUser(username = "compatibility-reporter")
                val appId = data.createApp(reporter)
                val service = context.getBean(ReportService::class.java)
                val reports = context.getBean(ReportRepository::class.java)
                val bugs = context.getBean(BugRepository::class.java)
                val versions = context.getBean(VersionRepository::class.java)
                val json = reportJson("first")
                val first = service.create(reporter, json, emptyList())
                savedBugId = first.bugId
                assertThat(bugs.find(first.bugId)?.title).isEqualTo("IllegalStateException: compatibility")
                assertThat(reports.find(first.id)?.bugId).isEqualTo(first.bugId)
                val mapper = JsonMapper.builder().build()
                assertThat(mapper.readTree(reports.find(first.id)!!.content.data())).isEqualTo(mapper.readTree(json))
                val second = service.create(reporter, reportJson("second"), emptyList())
                assertThat(second.bugId).isEqualTo(first.bugId)
                assertThat(reports.find(second.id)?.bugId).isEqualTo(first.bugId)
                assertThat(jooq.fetchCount(BUG)).isEqualTo(1)
                assertThat(jooq.fetchCount(BUG_IDENTIFIER)).isEqualTo(1)
                assertThat(jooq.fetchCount(REPORT)).isEqualTo(2)

                versions.setMappings(appId, 7, null, "renamed", "first mappings")
                versions.setMappings(appId, 7, "", null, "updated mappings")
                assertThat(versions.find(appId, 7, "")?.name).isEqualTo("renamed")
                assertThat(versions.find(appId, 7, "")?.mappings).isEqualTo("updated mappings")
                versions.ensureExists(appId, 7, null, "must not replace")
                assertThat(versions.find(appId, 7, "")?.name).isEqualTo("renamed")
                versions.setMappings(appId, 7, "demo", "demo name", "demo mappings")
                versions.setMappings(appId, 7, "demo", "new demo name", null)
                assertThat(versions.find(appId, 7, "demo")?.name).isEqualTo("new demo name")
                assertThat(versions.find(appId, 7, "demo")?.mappings).isNull()
                versions.setMappings(appId, 8, null, null, "new version mappings")
                assertThat(versions.find(appId, 8, "")?.name).isEqualTo("8")
                assertThat(jooq.fetchCount(VERSION)).isEqualTo(3)

                // Hold both inserts open to verify generated IDs on separate connections.
                // Report inserts are checked afterwards, separately from trigger lock contention.
                val executor = Executors.newFixedThreadPool(2)
                val inserted = CountDownLatch(2)
                val transaction = TransactionTemplate(context.getBean(PlatformTransactionManager::class.java))
                val configuration = context.getBean(AcrariumConfiguration::class.java)
                try {
                    val futures = (1..2).map { index -> executor.submit(Callable {
                        val stacktrace = "IllegalArgumentException: failure-$index"
                        val id = transaction.execute {
                            val id = bugs.create(BugIdentifier.fromStacktrace(configuration, appId, stacktrace), stacktrace)
                            inserted.countDown()
                            check(inserted.await(30, TimeUnit.SECONDS)) { "Concurrent bug insert did not complete" }
                            id
                        }
                        id to stacktrace
                    }) }
                    val concurrent = futures.map { it.get(30, TimeUnit.SECONDS) }
                    assertThat(concurrent.map { it.first }.toSet()).hasSize(2).doesNotContain(first.bugId)
                    concurrent.forEachIndexed { index, (bugId, stacktrace) ->
                        val report = service.create(reporter, reportJson("concurrent-$index", stacktrace), emptyList())
                        assertThat(report.bugId).isEqualTo(bugId)
                        assertThat(bugs.find(bugId)?.title).isEqualTo(stacktrace)
                        assertThat(reports.find(report.id)?.bugId).isEqualTo(bugId)
                    }
                } finally {
                    executor.shutdownNow()
                }

                val tables = listOf(VERSION, BUG, BUG_IDENTIFIER, REPORT, ATTACHMENT)
                val counts = tables.map { jooq.fetchCount(it) }
                // Fail at the report INSERT, after ReportService has inserted a new version and bug.
                jooq.execute("""CREATE TRIGGER compatibility_reject_report BEFORE INSERT ON report FOR EACH ROW
                    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'compatibility rollback test'""")
                try {
                    assertThatThrownBy {
                        service.create(reporter, reportJson("rollback", "IOException: rollback", 99), emptyList())
                    }.rootCause().isInstanceOfSatisfying(SQLException::class.java) { cause ->
                        assertThat(cause.sqlState).isEqualTo("45000")
                        assertThat(cause.message).contains("compatibility rollback test")
                    }
                    assertThat(tables.map { jooq.fetchCount(it) }).isEqualTo(counts)
                    assertThat(reports.find("rollback")).isNull()
                } finally {
                    jooq.execute("DROP TRIGGER compatibility_reject_report")
                }
                migrationCount = jooq.fetchCount(org.jooq.impl.DSL.table("DATABASECHANGELOG"))
                assertThat(migrationCount).isPositive()
            }
            // Closing and recreating the application context also reruns Liquibase against committed data.
            application.run { context ->
                assertThat(context).hasNotFailed()
                val reports = context.getBean(ReportRepository::class.java)
                assertThat(reports.find("first")?.bugId).isEqualTo(savedBugId)
                assertThat(reports.find("second")?.bugId).isEqualTo(savedBugId)
                val third = context.getBean(ReportService::class.java)
                    .create("compatibility-reporter", reportJson("after-restart"), emptyList())
                assertThat(third.bugId).isEqualTo(savedBugId)
                val jooq = context.getBean(DSLContext::class.java)
                assertThat(jooq.fetchCount(REPORT)).isEqualTo(5)
                assertThat(jooq.fetchCount(org.jooq.impl.DSL.table("DATABASECHANGELOG"))).isEqualTo(migrationCount)
            }
        }
    }

    private fun reportJson(id: String, stacktrace: String = "IllegalStateException: compatibility", version: Int = 7) = """
        {"REPORT_ID":"$id","STACK_TRACE":"$stacktrace","USER_CRASH_DATE":"2026-01-01T00:00:00Z",
         "INSTALLATION_ID":"installation-$id","APP_VERSION_CODE":$version,"APP_VERSION_NAME":"original",
         "CUSTOM_DATA":{"unicode":"Õä 日本語 🚀","quoted":"say \"hello\"","nested":{"values":[1,true,null,{"key":"value"}]}}}
    """.trimIndent()

    @TestConfiguration(proxyBeanMethods = false)
    @PropertySource("classpath:default.yml", factory = YamlPropertySourceFactory::class)
    @EnableConfigurationProperties(AcrariumConfiguration::class)
    @Import(
        BasicSecurityConfiguration::class, JooqConfigurationCustomizer::class, CustomConverterProvider::class,
        AppIdConverter::class, BugIdConverter::class, InstantConverter::class,
        AppRepository::class, UserRepository::class, BugRepository::class, DeviceRepository::class,
        ReportRepository::class, VersionRepository::class, ReportService::class, TestDataBuilder::class,
    )
    class PersistenceConfiguration
}
