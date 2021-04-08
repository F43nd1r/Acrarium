package com.faendir.acra.rest

import com.github.ziplet.filter.compression.CompressingFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.web.multipart.MultipartResolver
import org.springframework.web.multipart.commons.CommonsMultipartResolver
import javax.servlet.Filter

@Configuration
class RestConfiguration {
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    fun gzipFilter(): Filter = CompressingFilter()

    @Bean
    fun multipartResolver(): MultipartResolver = CommonsMultipartResolver()
}