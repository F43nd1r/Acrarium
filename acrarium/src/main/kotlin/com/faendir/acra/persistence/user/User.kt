package com.faendir.acra.persistence.user

import com.faendir.acra.jooq.generated.tables.references.USER_ROLES
import com.faendir.acra.persistence.SortDefinition
import com.faendir.acra.persistence.app.AppId
import org.jooq.Field

data class User(val username: String, val password: String, val mail: String?)

data class UserAuthorities(val username: String, val roles: Set<Role>, val permissions: Set<Permission>) {
    enum class Sort(override val field: Field<*>) : SortDefinition {
        USERNAME(USER_ROLES.USER_USERNAME)
    }

    fun getPermissionLevel(appId: AppId): Permission.Level {
        return permissions.find { it.appId == appId }?.level ?: if (roles.contains(Role.ADMIN)) Permission.Level.ADMIN else Permission.Level.NONE
    }
}
