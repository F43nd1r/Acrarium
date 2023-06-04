/*
 * (C) Copyright 2022 Lukas Morawietz (https://github.com/F43nd1r)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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