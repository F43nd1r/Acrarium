package com.faendir.acra

import ch.vorburger.mariadb4j.springframework.MariaDB4jSpringService
import com.faendir.acra.persistence.jooq.JooqConfigurationCustomizer
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import javax.sql.DataSource

@TestConfiguration
@Import(JooqConfigurationCustomizer::class)
class MariaDBTestConfiguration {

    @Bean
    fun mariaDB4j(): MariaDB4jSpringService = object : MariaDB4jSpringService() {
        @PostConstruct
        override fun postConstruct() {
            super.postConstruct()
        }

        @PreDestroy
        override fun preDestroy() {
            super.preDestroy()
        }
    }.apply {
        configuration.addArg("--character-set-server=utf8mb4")
        configuration.addArg("--collation-server=utf8mb4_unicode_ci")
    }

    @Bean
    fun dataSource(mariaDB4j: MariaDB4jSpringService): DataSource = DataSourceBuilder.create()
        .driverClassName("org.mariadb.jdbc.Driver")
        .url("jdbc:mariadb://localhost:${mariaDB4j.configuration.port}/test?serverTimezone=UTC")
        .username("root")
        .password(mariaDB4j.configuration.defaultRootPassword ?: "")
        .build()
}