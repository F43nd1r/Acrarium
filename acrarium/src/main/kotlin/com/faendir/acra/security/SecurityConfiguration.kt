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
import com.faendir.acra.rest.RestApiInterface.Companion.API_PATH
import com.faendir.acra.rest.RestReportInterface.Companion.REPORT_PATH
import com.faendir.acra.service.UserService
import com.faendir.acra.ui.view.login.LoginView
import com.faendir.acra.ui.view.login.SetupView
import com.vaadin.flow.spring.SpringLookupInitializer
import com.vaadin.flow.spring.VaadinScopesConfig
import com.vaadin.flow.spring.scopes.VaadinSessionScope
import com.vaadin.flow.spring.security.RequestUtil
import com.vaadin.flow.spring.security.VaadinDefaultRequestCache
import com.vaadin.flow.spring.security.VaadinWebSecurityConfigurerAdapter
import org.apache.commons.text.CharacterPredicate
import org.apache.commons.text.RandomStringGenerator
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.BeanIds
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.builders.WebSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint
import java.security.SecureRandom


/**
 * @author Lukas
 * @since 22.03.2017
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
class SecurityConfiguration(private val userService: UserService) : WebSecurityConfigurerAdapter() {

    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.authenticationProvider(object : AuthenticationProvider {
            override fun authenticate(authentication: Authentication): Authentication? {
                if (authentication is UsernamePasswordAuthenticationToken) {
                    val user =
                        userService.getUser(authentication.getName()) ?: throw UsernameNotFoundException("Username ${authentication.getName()} not found")
                    if (userService.checkPassword(user, authentication.getCredentials() as String)) {
                        return UsernamePasswordAuthenticationToken(user.username, user.password, user.authorities)
                    }
                    throw BadCredentialsException("Password mismatch for user ${user.username}")
                }
                return null
            }

            override fun supports(authentication: Class<*>): Boolean = UsernamePasswordAuthenticationToken::class.java.isAssignableFrom(authentication)
        })
    }

    @Configuration
    @Order(1)
    class ReportConfigurer : WebSecurityConfigurerAdapter() {
        override fun configure(http: HttpSecurity) {
            http.csrf().disable()
                .headers().disable()
                .anonymous().disable()
                .exceptionHandling().authenticationEntryPoint(Http403ForbiddenEntryPoint()).and()
                .regexMatcher("/$REPORT_PATH").authorizeRequests { it.anyRequest().hasRole(User.Role.REPORTER.name) }
                .httpBasic()
        }
    }

    @Configuration
    @Order(2)
    class ApiConfigurer : WebSecurityConfigurerAdapter() {
        override fun configure(http: HttpSecurity) {
            http.csrf().disable()
                .headers().disable()
                .anonymous().disable()
                .exceptionHandling().authenticationEntryPoint(Http403ForbiddenEntryPoint()).and()
                .regexMatcher("/$API_PATH/.*").authorizeRequests { it.anyRequest().hasRole(User.Role.API.name) }
                .httpBasic()
        }
    }

    @Configuration
    @Order(3)
    class ActuatorConfigurer : WebSecurityConfigurerAdapter() {
        override fun configure(http: HttpSecurity) {
            http.csrf().disable()
                .headers().disable()
                .anonymous().disable()
                .exceptionHandling().authenticationEntryPoint(BasicAuthenticationEntryPoint()).and()
                .requestMatcher(EndpointRequest.toAnyEndpoint()).authorizeRequests { it.anyRequest().hasRole(User.Role.API.name) }
                .httpBasic()
        }
    }

    @Configuration
    @Order(4)
    class VaadinConfigurer : VaadinWebSecurityConfigurerAdapter() {

        override fun configure(web: WebSecurity) {
            super.configure(web)
            web.ignoring().antMatchers("/images/**")
        }

        override fun configure(http: HttpSecurity) {
            http.csrf().disable()
                .headers().disable()
                .authorizeRequests()
                .regexMatchers("/${SetupView.ROUTE}").permitAll()
                .and().formLogin().loginPage("/${LoginView.ROUTE}").permitAll()
            super.configure(http)
        }
    }

    @Bean(name = [BeanIds.AUTHENTICATION_MANAGER])
    override fun authenticationManagerBean(): AuthenticationManager = authenticationManager()

    companion object {
        @Bean
        fun secureRandom(): SecureRandom = SecureRandom()

        @Bean
        fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

        @Bean
        fun randomStringGenerator(secureRandom: SecureRandom): RandomStringGenerator =
            RandomStringGenerator.Builder().usingRandom { secureRandom.nextInt(it) }.withinRange('0'.code, 'z'.code)
                .filteredBy(CharacterPredicate { Character.isLetterOrDigit(it) }).build()

        @Bean
        fun grantedAuthoritiesMapper() =
            GrantedAuthoritiesMapper { authorities -> User.Role.values().filter { role -> authorities.any { it?.authority == role.authority } } }
    }

}