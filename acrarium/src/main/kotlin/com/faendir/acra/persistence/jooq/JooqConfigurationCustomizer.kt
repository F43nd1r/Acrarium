package com.faendir.acra.persistence.jooq

import org.jooq.SQLDialect
import org.jooq.impl.DefaultConfiguration
import org.springframework.boot.autoconfigure.jooq.DefaultConfigurationCustomizer
import org.springframework.stereotype.Component

@Component
class JooqConfigurationCustomizer(private val customConverterProvider: CustomConverterProvider) : DefaultConfigurationCustomizer {
    init {
        System.setProperty("org.jooq.no-logo", "true")
        System.setProperty("org.jooq.no-tips", "true")
    }

    override fun customize(configuration: DefaultConfiguration) {
        configuration.settings().apply {
            isRenderSchema = false
            isExecuteLogging = true
        }
        configuration.setSQLDialect(SQLDialect.MYSQL)
        configuration.set(customConverterProvider)
    }
}