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
package com.faendir.acra.model

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import javax.persistence.Column
import javax.persistence.ElementCollection
import javax.persistence.Entity
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.persistence.FetchType
import javax.persistence.Id
import javax.persistence.Transient
import javax.validation.constraints.Email

/**
 * @author Lukas
 * @since 20.05.2017
 */
@Entity
class User(@Id
           private var username: String,
           private var password: String,
           @Enumerated(EnumType.STRING)
           @Column(nullable = false)
           @ElementCollection(fetch = FetchType.EAGER)
           val roles: MutableSet<Role>,
           @Transient
           private var plainTextPassword: String? = null,
           @Email
           var mail: String? = null,
           @Column(nullable = false)
           @ElementCollection(fetch = FetchType.EAGER)
           val permissions: MutableSet<Permission> = mutableSetOf()) : UserDetails {


    constructor(other: User) : this(other.username, other.password, other.roles.toMutableSet(), other.plainTextPassword, other.mail, other.permissions)

    override fun getAuthorities(): Collection<GrantedAuthority> = permissions + roles

    override fun getPassword(): String = password

    fun setPassword(password: String) {
        this.password = password
    }

    fun hasPlainTextPassword(): Boolean = plainTextPassword != null

    fun getPlainTextPassword(): String = plainTextPassword.let { checkNotNull(it) { "Trying to access plain text password of persisted entity" }; it }

    fun setPlainTextPassword(plainTextPassword: String) {
        this.plainTextPassword = plainTextPassword
    }

    override fun getUsername(): String = username

    fun setUsername(username: String) {
        this.username = username
    }

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true

    enum class Role : GrantedAuthority {
        ADMIN, USER, REPORTER, API;

        override fun getAuthority(): String = "ROLE_$name"
    }
}