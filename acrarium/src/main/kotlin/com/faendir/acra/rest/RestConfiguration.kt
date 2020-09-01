package com.faendir.acra.rest

import com.github.ziplet.filter.compression.CompressingFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import javax.servlet.Filter

@Configuration
class RestConfiguration {
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    fun gzipFilter(): Filter = CompressingFilter()
}