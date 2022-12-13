package com.faendir.acra.persistence

import com.faendir.acra.security.BasicSecurityConfiguration
import org.apache.commons.text.RandomStringGenerator
import org.jooq.DSLContext
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import

@TestConfiguration
@ComponentScan(lazyInit = true)
@Import(BasicSecurityConfiguration::class)
class PersistenceTestConfiguration {

    @Bean
    fun testDataBuilder(jooq: DSLContext, randomStringGenerator: RandomStringGenerator) = TestDataBuilder(jooq, randomStringGenerator)
}