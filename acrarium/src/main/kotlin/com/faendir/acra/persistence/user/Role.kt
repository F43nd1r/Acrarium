package com.faendir.acra.persistence.user

import org.springframework.security.core.GrantedAuthority

enum class Role : GrantedAuthority {
    ADMIN,
    USER,
    REPORTER,
    API;

    override fun getAuthority(): String = "ROLE_$name"
}