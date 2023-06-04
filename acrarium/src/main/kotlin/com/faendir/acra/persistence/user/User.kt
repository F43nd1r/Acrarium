/*
 * (C) Copyright 2022 Lukas Morawietz (https://github.com/F43nd1r)
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
