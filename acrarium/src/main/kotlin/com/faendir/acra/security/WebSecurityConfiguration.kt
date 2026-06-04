/*
 * (C) Copyright 2022-2026 Lukas Morawietz (https://github.com/F43nd1r)
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

import com.faendir.acra.persistence.user.Role
import com.faendir.acra.persistence.user.UserRepository
import com.faendir.acra.rest.RestApiInterface.Companion.API_PATH
import com.faendir.acra.rest.RestReportInterface.Companion.REPORT_PATH
import com.faendir.acra.ui.view.login.LoginView
import com.vaadin.flow.spring.SpringSecurityAutoConfiguration
import com.vaadin.flow.spring.security.VaadinSecurityConfigurer
import org.springframework.boot.security.autoconfigure.actuate.web.servlet.EndpointRequest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.core.annotation.Order
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.BeanIds
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.Authentication
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher
import org.springframework.security.web.util.matcher.OrRequestMatcher

@Configuration
@EnableWebSecurity
@Import(SpringSecurityAutoConfiguration::class)
class WebSecurityConfiguration(private val userRepository: UserRepository) {
    @Bean(name = [BeanIds.AUTHENTICATION_MANAGER])
    fun authenticationManager(http: HttpSecurity): AuthenticationManager =
        http.getSharedObject(AuthenticationManagerBuilder::class.java)
            .parentAuthenticationManager(null)
            .authenticationProvider(object : AuthenticationProvider {
                override fun authenticate(authentication: Authentication): Authentication? {
                    if (authentication is UsernamePasswordAuthenticationToken) {
                        val username = authentication.name
                        if (userRepository.checkPassword(username, authentication.credentials as String)) {
                            return UsernamePasswordAuthenticationToken(username, null, userRepository.getAuthorities(username))
                        }
                        throw BadCredentialsException("Bad username/password combination for $username")
                    }
                    return null
                }

                override fun supports(authentication: Class<*>): Boolean =
                    UsernamePasswordAuthenticationToken::class.java.isAssignableFrom(authentication)
            }).build()

    @Bean
    @Order(1)
    fun reportSecurityChain(http: HttpSecurity): SecurityFilterChain =
        http.securityMatcher("/$REPORT_PATH")
            .csrf { it.disable() }
            .exceptionHandling { it.authenticationEntryPoint(Http403ForbiddenEntryPoint()) }
            .authorizeHttpRequests { it.anyRequest().hasRole(Role.REPORTER.name) }
            .httpBasic(Customizer.withDefaults())
            .build()

    @Bean
    @Order(2)
    fun apiSecurityChain(http: HttpSecurity): SecurityFilterChain =
        http.securityMatcher("/$API_PATH/**")
            .csrf { it.disable() }
            .headers { it.disable() }
            .anonymous { it.disable() }
            .exceptionHandling { it.authenticationEntryPoint(Http403ForbiddenEntryPoint()) }
            .authorizeHttpRequests { it.anyRequest().hasRole(Role.API.name) }
            .httpBasic(Customizer.withDefaults())
            .build()

    @Bean
    @Order(3)
    fun actuatorSecurityChain(http: HttpSecurity): SecurityFilterChain =
        http.securityMatcher(OrRequestMatcher(EndpointRequest.toAnyEndpoint(), PathPatternRequestMatcher.withDefaults().matcher("/swagger-ui/**")))
            .csrf { it.disable() }
            .headers { it.disable() }
            .anonymous { it.disable() }
            .exceptionHandling { it.authenticationEntryPoint(Http403ForbiddenEntryPoint()) }
            .authorizeHttpRequests { it.anyRequest().hasRole(Role.ADMIN.name) }
            .httpBasic(Customizer.withDefaults())
            .build()

    @Bean("VaadinSecurityFilterChainBean")
    @Order(4)
    fun vaadinSecurityChain(http: HttpSecurity): SecurityFilterChain {
        http.with(VaadinSecurityConfigurer.vaadin()) { vaadin ->
            vaadin.loginView(LoginView::class.java)
        }
        return http.build()
    }
}
