package com.faendir.acra.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import javax.servlet.FilterChain
import javax.servlet.http.HttpFilter
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

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