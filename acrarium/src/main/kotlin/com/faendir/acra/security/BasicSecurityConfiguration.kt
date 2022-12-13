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
            .filteredBy(CharacterPredicate { Character.isLetterOrDigit(it) }).build()

    @Bean
    fun grantedAuthoritiesMapper() =
        GrantedAuthoritiesMapper { authorities -> Role.values().filter { role -> authorities.any { it?.authority == role.authority } } }
}