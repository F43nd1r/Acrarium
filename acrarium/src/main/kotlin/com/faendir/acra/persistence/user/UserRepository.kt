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

import com.faendir.acra.dataprovider.AcrariumDataProvider
import com.faendir.acra.dataprovider.AcrariumSort
import com.faendir.acra.jooq.generated.tables.references.USER
import com.faendir.acra.jooq.generated.tables.references.USER_PERMISSIONS
import com.faendir.acra.jooq.generated.tables.references.USER_ROLES
import com.faendir.acra.persistence.*
import com.faendir.acra.persistence.app.AppId
import org.jooq.DSLContext
import org.jooq.exception.DataAccessException
import org.jooq.impl.DSL
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.stream.Stream

@Repository
class UserRepository(private val jooq: DSLContext, private val passwordEncoder: PasswordEncoder) {

    fun exists(username: String): Boolean = jooq.fetchExists(USER, USER.USERNAME.eq(username))

    @PreAuthorize("#username == principal.username || isAdmin()")
    fun find(username: String): User? = jooq.selectFrom(USER).where(USER.USERNAME.eq(username)).fetchValueInto<User>()

    /**
     * @return if the user was successfully created
     */
    @Transactional
    @PreAuthorize("isAdmin()")
    fun create(username: String, rawPassword: String, mail: String?, vararg roles: Role): Boolean = try {
        val name = username.lowercase()
        jooq.insertInto(USER)
            .set(USER.USERNAME, name)
            .set(USER.PASSWORD, passwordEncoder.encode(rawPassword))
            .set(USER.MAIL, mail)
            .execute()
        for (role in roles) {
            jooq.insertInto(USER_ROLES)
                .set(USER_ROLES.USER_USERNAME, name)
                .set(USER_ROLES.ROLES, role.name)
                .execute()
        }
        true
    } catch (e: DataAccessException) {
        false
    }

    @Transactional
    @PreAuthorize("#username == principal.username || isAdmin()")
    fun update(username: String, rawPassword: String?, mail: String?) {
        jooq.update(USER)
            .apply { if (rawPassword != null) set(USER.PASSWORD, passwordEncoder.encode(rawPassword)) }
            .set(USER.MAIL, mail)
            .where(USER.USERNAME.eq(username))
            .execute()
    }

    @Transactional
    @PreAuthorize("isAdmin()")
    fun setRole(username: String, role: Role, enabled: Boolean) {
        if (enabled) {
            jooq.insertInto(USER_ROLES)
                .set(USER_ROLES.USER_USERNAME, username)
                .set(USER_ROLES.ROLES, role.name)
                .onDuplicateKeyIgnore()
                .execute()
        } else {
            jooq.deleteFrom(USER_ROLES)
                .where(USER_ROLES.USER_USERNAME.eq(username), USER_ROLES.ROLES.eq(role.name))
                .execute()
        }
    }

    @Transactional
    @PreAuthorize("isAdmin()")
    fun setPermission(username: String, appId: AppId, level: Permission.Level) {
        jooq.insertInto(USER_PERMISSIONS)
            .set(USER_PERMISSIONS.USER_USERNAME, username)
            .set(USER_PERMISSIONS.APP_ID, appId)
            .set(USER_PERMISSIONS.LEVEL, level.name)
            .onDuplicateKeyUpdate()
            .set(USER_PERMISSIONS.LEVEL, level.name)
            .execute()
    }

    fun getAuthorities(username: String): List<GrantedAuthority> =
        jooq.select(USER_ROLES.ROLES).from(USER_ROLES).where(USER_ROLES.USER_USERNAME.eq(username)).fetchListInto<Role>() +
                jooq.select(USER_PERMISSIONS.APP_ID, USER_PERMISSIONS.LEVEL).from(USER_PERMISSIONS).where(USER_PERMISSIONS.USER_USERNAME.eq(username))
                    .fetchListInto<Permission>()

    @Transactional
    @PreAuthorize("#username != principal.username && isAdmin()")
    fun delete(username: String) {
        jooq.deleteFrom(USER).where(USER.USERNAME.eq(username)).execute()
    }

    fun checkPassword(username: String, rawPassword: String): Boolean {
        val encodedPassword = jooq.select(USER.PASSWORD).from(USER).where(USER.USERNAME.eq(username)).fetchValue()
        return encodedPassword != null && passwordEncoder.matches(rawPassword, encodedPassword)
    }

    fun hasAnyAdmin(): Boolean = jooq.selectOne().from(USER_ROLES).where(USER_ROLES.ROLES.eq(Role.ADMIN.name)).limit(1).fetchValue() != null

    @PreAuthorize("isAdmin()")
    fun getProvider(): AcrariumDataProvider<UserAuthorities, Nothing, UserAuthorities.Sort> =
        object : AcrariumDataProvider<UserAuthorities, Nothing, UserAuthorities.Sort>() {
            private val USERNAME_FROM_ROLE = USER_ROLES.USER_USERNAME.NOT_NULL.`as`("username_from_role")
            override fun fetch(
                filters: Set<Nothing>,
                sort: List<AcrariumSort<UserAuthorities.Sort>>,
                offset: Int,
                limit: Int
            ): Stream<UserAuthorities> =
                jooq.select(
                    USERNAME_FROM_ROLE,
                    USER_ROLES.ROLES,
                    USER_PERMISSIONS.APP_ID,
                    USER_PERMISSIONS.LEVEL,
                )
                    .from(
                        DSL.selectDistinct(USERNAME_FROM_ROLE).from(USER_ROLES).where(USER_ROLES.ROLES.eq(Role.USER.name))
                            .orderBy(sort.asOrderFields())
                            .offset(offset)
                            .limit(limit)
                    )
                    .join(USER_ROLES).on(USER_ROLES.USER_USERNAME.eq(USERNAME_FROM_ROLE))
                    .leftJoin(USER_PERMISSIONS).on(USER_PERMISSIONS.USER_USERNAME.eq(USERNAME_FROM_ROLE))
                    .orderBy(sort.asOrderFields())
                    .fetchGroups(USERNAME_FROM_ROLE)
                    .map { (username, rows) ->
                        UserAuthorities(
                            username,
                            rows.intoSet(USER_ROLES.ROLES, Role::class.java),
                            rows.intoSet {
                                val appId = it[USER_PERMISSIONS.APP_ID]
                                val level = it[USER_PERMISSIONS.LEVEL]
                                if (appId != null && level != null) Permission(appId, Permission.Level.valueOf(level)) else null
                            }.apply { remove(null) }
                        )
                    }
                    .stream()

            override fun size(filters: Set<Nothing>): Int =
                jooq.select(DSL.countDistinct(USER_ROLES.USER_USERNAME)).from(USER_ROLES).where(USER_ROLES.ROLES.eq(Role.USER.name)).fetchValue() ?: 0
        }
}