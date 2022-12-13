package com.faendir.acra.persistence.user

import com.faendir.acra.persistence.app.AppId
import org.springframework.security.core.GrantedAuthority

data class Permission(
    val appId: AppId,
    val level: Level
) : GrantedAuthority {
    override fun getAuthority(): String {
        return "PERMISSION_" + level.name + "_" + appId
    }

    enum class Level {
        NONE,
        VIEW,
        EDIT,
        ADMIN
    }
}