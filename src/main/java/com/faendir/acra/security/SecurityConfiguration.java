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
package com.faendir.acra.security;

import com.faendir.acra.model.User;
import com.faendir.acra.rest.RestReportInterface;
import com.faendir.acra.service.UserService;
import org.apache.commons.text.RandomStringGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;

import java.security.SecureRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Lukas
 * @since 22.03.2017
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    static {
        SecurityContextHolder.setStrategyName(VaadinSessionSecurityContextHolderStrategy.class.getName());
    }

    @NonNull private final UserService userService;

    @Autowired
    public SecurityConfiguration(@NonNull UserService userService) {
        this.userService = userService;
    }

    @NonNull
    @Bean
    public static SecureRandom secureRandom() {
        return new SecureRandom();
    }

    @NonNull
    @Bean
    public static PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @NonNull
    @Bean
    public static RandomStringGenerator randomStringGenerator(@NonNull SecureRandom secureRandom) {
        return new RandomStringGenerator.Builder().usingRandom(secureRandom::nextInt).withinRange('0', 'z').filteredBy(Character::isLetterOrDigit).build();
    }

    @NonNull
    @Bean
    public static GrantedAuthoritiesMapper grantedAuthoritiesMapper() {
        return authorities -> Stream.of(User.Role.values())
                .filter(role -> authorities.stream().anyMatch(auth -> auth.getAuthority().equals(role.getAuthority())))
                .collect(Collectors.toList());
    }

    @Override
    protected void configure(@NonNull AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(new AuthenticationProvider() {
            @Nullable
            @Override
            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                if (authentication instanceof UsernamePasswordAuthenticationToken) {
                    User user = userService.getUser(authentication.getName());
                    if (user == null) {
                        throw new UsernameNotFoundException("Username " + authentication.getName() + " not found");
                    }
                    if (userService.checkPassword(user, (String) authentication.getCredentials())) {
                        return new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword(), user.getAuthorities());
                    }
                    throw new BadCredentialsException("Password mismatch for user " + user.getUsername());
                }
                return null;
            }

            @Override
            public boolean supports(@NonNull Class<?> authentication) {
                return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
            }
        });
    }

    @Override
    protected void configure(@NonNull HttpSecurity http) throws Exception {
        // @formatter:off
        http
                .csrf().disable()
                .headers().disable()
                .anonymous().disable()
                .exceptionHandling().authenticationEntryPoint(new Http403ForbiddenEntryPoint())
                .and()
                .sessionManagement()
                .and()
                .antMatcher("/"+RestReportInterface.REPORT_PATH).httpBasic();
        // @formatter:on
    }

    @NonNull
    @Bean(name = BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return authenticationManager();
    }
}
