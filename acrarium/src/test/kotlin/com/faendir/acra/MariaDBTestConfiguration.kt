package com.faendir.acra

import ch.vorburger.mariadb4j.springframework.MariaDB4jSpringService
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import javax.sql.DataSource

@TestConfiguration
class MariaDBTestConfiguration {

    @Bean
    fun mariaDB4j(): MariaDB4jSpringService = MariaDB4jSpringService().apply {
        configuration.addArg("--character-set-server=utf8mb4")
        configuration.addArg("--collation-server=utf8mb4_unicode_ci")
    }

    @Bean
    fun dataSource(mariaDB4j: MariaDB4jSpringService): DataSource = DataSourceBuilder.create()
        .driverClassName("org.mariadb.jdbc.Driver")
        .url("jdbc:mariadb://localhost:${mariaDB4j.configuration.port}/test")
        .username("root")
        .password(mariaDB4j.configuration.defaultRootPassword ?: "")
        .build()
}