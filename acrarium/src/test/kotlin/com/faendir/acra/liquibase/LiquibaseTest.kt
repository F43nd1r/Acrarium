/*
 * (C) Copyright 2018 Lukas Morawietz (https://github.com/F43nd1r)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.faendir.acra.liquibase

import com.faendir.acra.BackendApplication
import com.faendir.acra.annotation.AcrariumTest
import com.faendir.acra.security.SecurityConfiguration
import com.ninjasquad.springmockk.MockkBean
import com.vaadin.flow.spring.security.VaadinDefaultRequestCache
import liquibase.integration.spring.SpringLiquibase
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.EmbeddedDataSourceConfiguration
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.FilterType
import org.springframework.core.io.ResourceLoader
import org.springframework.security.config.annotation.web.WebSecurityConfigurer
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringExtension
import javax.sql.DataSource
import javax.validation.Validation
import javax.validation.Validator

/**
 * @author lukas
 * @since 25.06.18
 */
@DataJpaTest
@AcrariumTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ComponentScan(basePackageClasses = [ChangeAwareSpringLiquibase::class],
        excludeFilters = [ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = [SpringLiquibase::class])])
@ImportAutoConfiguration(exclude = [LiquibaseAutoConfiguration::class, EmbeddedDataSourceConfiguration::class])
@EnableConfigurationProperties(LiquibaseProperties::class)
class LiquibaseTest {
    @Autowired
    lateinit var resourceLoader: ResourceLoader

    @Autowired
    lateinit var dataSource: DataSource

    @Autowired(required = false)
    var processors: List<LiquibaseChangePostProcessor>? = null

    @Autowired
    lateinit var properties: LiquibaseProperties

    @Test
    fun setupTest() {
        val liquibase = ChangeAwareSpringLiquibase(properties, dataSource)
        processors?.let { liquibase.setProcessors(it) }
        liquibase.resourceLoader = resourceLoader
        liquibase.afterPropertiesSet()
    }

    @Configuration
    class Config {
        @Bean
        fun validator(): Validator {
            return Validation.buildDefaultValidatorFactory().validator
        }
    }
}