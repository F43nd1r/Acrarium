/*
 * (C) Copyright 2022-2024 Lukas Morawietz (https://github.com/F43nd1r)
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
import org.apache.commons.text.CharacterPredicate
import org.apache.commons.text.RandomStringGenerator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import java.security.SecureRandom

@Configuration
class BasicSecurityConfiguration {
    @Bean
    fun secureRandom(): SecureRandom = SecureRandom()

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun randomStringGenerator(secureRandom: SecureRandom): RandomStringGenerator =
        RandomStringGenerator.Builder().usingRandom { secureRandom.nextInt(it) }.withinRange('0'.code, 'z'.code)
            .filteredBy(CharacterPredicate { Character.isLetterOrDigit(it) }).get()

    @Bean
    fun grantedAuthoritiesMapper() =
        GrantedAuthoritiesMapper { authorities -> Role.entries.filter { role -> authorities.any { it?.authority == role.authority } } }
}