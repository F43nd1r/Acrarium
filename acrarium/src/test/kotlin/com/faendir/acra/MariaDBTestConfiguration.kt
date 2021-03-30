package com.faendir.acra

import ch.vorburger.mariadb4j.springframework.MariaDB4jSpringService
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class MariaDBTestConfiguration {

    @Bean
    fun mariaDB4j(): MariaDB4jSpringService {
        return MariaDB4jSpringService().apply {
            configuration.addArg("--character-set-server=utf8mb4")
            configuration.addArg("--collation-server=utf8mb4_unicode_ci")
        }
    }
}