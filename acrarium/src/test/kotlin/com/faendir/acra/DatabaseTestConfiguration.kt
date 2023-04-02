package com.faendir.acra

import com.faendir.acra.persistence.jooq.JooqConfigurationCustomizer
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import javax.sql.DataSource

@TestConfiguration
@Import(JooqConfigurationCustomizer::class)
class DatabaseTestConfiguration {

    @Bean
    fun dataSource(): DataSource = DataSourceBuilder.create()
        .driverClassName("org.testcontainers.jdbc.ContainerDatabaseDriver")
        .url("jdbc:tc:mysql:8.0:////test?serverTimezone=UTC&TC_MY_CNF=mysql.conf.d")
        .build()
}