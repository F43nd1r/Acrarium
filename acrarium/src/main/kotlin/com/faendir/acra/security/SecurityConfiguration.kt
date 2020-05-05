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
package com.faendir.acra.security

import com.faendir.acra.model.User
import com.faendir.acra.rest.RestApiInterface
import com.faendir.acra.rest.RestReportInterface
import com.faendir.acra.service.UserService
import org.apache.commons.text.CharacterPredicate
import org.apache.commons.text.RandomStringGenerator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.lang.NonNull
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.BeanIds
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.Authentication
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint
import java.security.SecureRandom

/**
 * @author Lukas
 * @since 22.03.2017
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
open class SecurityConfiguration(private val userService: UserService) : WebSecurityConfigurerAdapter() {
    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.authenticationProvider(object : AuthenticationProvider {
            @Throws(AuthenticationException::class)
            override fun authenticate(authentication: Authentication): Authentication? {
                if (authentication is UsernamePasswordAuthenticationToken) {
                    val user = userService.getUser(authentication.getName()) ?: throw UsernameNotFoundException("Username " + authentication.getName() + " not found")
                    if (userService.checkPassword(user, authentication.getCredentials() as String)) {
                        return UsernamePasswordAuthenticationToken(user.username, user.password, user.authorities)
                    }
                    throw BadCredentialsException("Password mismatch for user " + user.username)
                }
                return null
            }

            override fun supports(authentication: Class<*>): Boolean {
                return UsernamePasswordAuthenticationToken::class.java.isAssignableFrom(authentication)
            }
        })
    }

    override fun configure(http: HttpSecurity) {
        http.csrf().disable()
                .headers().disable()
                .anonymous().disable()
                .exceptionHandling()
                .authenticationEntryPoint(Http403ForbiddenEntryPoint())
                .and().sessionManagement()
                .and().regexMatcher("/(" + RestReportInterface.REPORT_PATH + "|" + RestApiInterface.API_PATH + "/.*)")
                .httpBasic()
    }

    @Bean(name = [BeanIds.AUTHENTICATION_MANAGER])
    override fun authenticationManagerBean(): AuthenticationManager = authenticationManager()

    companion object {
        @Bean
        fun secureRandom(): SecureRandom = SecureRandom()

        @Bean
        fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

        @Bean
        fun randomStringGenerator(@NonNull secureRandom: SecureRandom): RandomStringGenerator =
                RandomStringGenerator.Builder().usingRandom { secureRandom.nextInt(it) }.withinRange('0'.toInt(), 'z'.toInt())
                        .filteredBy(CharacterPredicate { Character.isLetterOrDigit(it) }).build()

        @NonNull
        @Bean
        fun grantedAuthoritiesMapper() = GrantedAuthoritiesMapper { authorities -> User.Role.values().filter { role -> authorities.any { it?.authority == role.authority } } }
    }

}