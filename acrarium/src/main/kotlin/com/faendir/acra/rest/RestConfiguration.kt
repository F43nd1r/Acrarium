package com.faendir.acra.rest

import com.github.ziplet.filter.compression.CompressingFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.web.multipart.MultipartHttpServletRequest
import org.springframework.web.multipart.MultipartResolver
import org.springframework.web.multipart.commons.CommonsMultipartResolver
import org.springframework.web.multipart.support.StandardServletMultipartResolver
import javax.servlet.Filter
import javax.servlet.http.HttpServletRequest

@Configuration
class RestConfiguration {
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    fun gzipFilter(): Filter = CompressingFilter()

    @Bean
    fun multipartResolver(): MultipartResolver = SwitchingMultipartResolver()
}

class SwitchingMultipartResolver : MultipartResolver {
    private val commonsResolver = CommonsMultipartResolver()
    private val standardResolver = StandardServletMultipartResolver()
    override fun isMultipart(request: HttpServletRequest): Boolean {
        return request.resolver().isMultipart(request)
    }

    override fun resolveMultipart(request: HttpServletRequest): MultipartHttpServletRequest {
        return request.resolver().resolveMultipart(request)
    }

    override fun cleanupMultipart(request: MultipartHttpServletRequest) {
        return request.resolver().cleanupMultipart(request)
    }

    private fun HttpServletRequest.resolver() = if (this.servletPath.startsWith("/VAADIN")) standardResolver else commonsResolver
}