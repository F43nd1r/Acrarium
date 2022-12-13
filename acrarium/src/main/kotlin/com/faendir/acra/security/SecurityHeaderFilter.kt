package com.faendir.acra.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpFilter
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class SecurityHeaderFilter(@Value("\${security.require-ssl:false}") private val requireSsl: Boolean) : HttpFilter() {
    override fun doFilter(req: HttpServletRequest, res: HttpServletResponse, chain: FilterChain) {
        res.addHeader("X-Frame-Options", "sameorigin")
        res.addHeader("X-Robots-Tag", "noindex,nofollow")
        if (requireSsl) {
            res.addHeader("Strict-Transport-Security", "max-age=31536000")
        }
        super.doFilter(req, res, chain)
    }

    override fun destroy() {
    }
}