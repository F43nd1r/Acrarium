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
package com.faendir.acra.service

import com.faendir.acra.dataprovider.QueryDslDataProvider
import com.faendir.acra.model.App
import com.faendir.acra.model.Permission
import com.faendir.acra.model.QUser
import com.faendir.acra.model.User
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.jpa.impl.JPAQuery
import org.apache.commons.text.RandomStringGenerator
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.Serializable
import javax.persistence.EntityManager
import javax.validation.Validator

/**
 * @author Lukas
 * @since 20.05.2017
 */
@Service
class UserService(private val passwordEncoder: PasswordEncoder, private val randomStringGenerator: RandomStringGenerator, private val entityManager: EntityManager,
                  private val validator: Validator) : Serializable {

    fun getUser(username: String): User? = JPAQuery<Any>(entityManager).from(USER)
            .leftJoin(USER.roles).fetchJoin()
            .leftJoin(USER.permissions).fetchJoin()
            .where(USER.username.eq(username)).select(USER).fetchOne()

    @CacheEvict(hasAdminCache)
    @Transactional
    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasRole(T(com.faendir.acra.model.User\$Role).ADMIN)")
    fun createUser(username: String, password: String) {
        require(JPAQuery<Any?>(entityManager).from(USER).where(USER.username.eq(username)).fetchFirst() == null) { "Username already exists" }
        entityManager.persist(User(username, passwordEncoder.encode(password), mutableSetOf(User.Role.USER)))
    }

    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasRole(T(com.faendir.acra.model.User\$Role).ADMIN)")
    fun createReporterUser(): User {
        var username: String
        do {
            username = randomStringGenerator.generate(16)
        } while (JPAQuery<Any?>(entityManager).from(USER).where(USER.username.eq(username)).fetchFirst() != null)
        val password = randomStringGenerator.generate(16)
        return User(username, passwordEncoder.encode(password), mutableSetOf(User.Role.REPORTER), password)
    }

    fun checkPassword(user: User?, password: String?): Boolean = user != null && passwordEncoder.matches(password, user.password)

    @CacheEvict(cacheNames = [hasAdminCache], allEntries = true)
    @Transactional
    fun store(user: User): User = entityManager.merge(user.apply { if (hasPlainTextPassword()) password = passwordEncoder.encode(user.getPlainTextPassword()) })

    @Cacheable(hasAdminCache)
    fun hasAdmin(): Boolean = JPAQuery<Any>(entityManager).from(USER).where(USER.roles.contains(User.Role.ADMIN)).select(Expressions.ONE).fetchFirst() != null

    @CacheEvict(cacheNames = [hasAdminCache], allEntries = true)
    @Transactional
    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasRole(T(com.faendir.acra.model.User\$Role).ADMIN)")
    fun setAdmin(user: User, admin: Boolean) {
        if (admin) {
            user.roles.add(User.Role.ADMIN)
        } else {
            user.roles.remove(User.Role.ADMIN)
        }
        entityManager.merge(user)
    }

    @Transactional
    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasRole(T(com.faendir.acra.model.User\$Role).ADMIN)")
    fun setApiAccess(user: User, access: Boolean) {
        if (access) {
            user.roles.add(User.Role.API)
        } else {
            user.roles.remove(User.Role.API)
        }
        entityManager.merge(user)
    }

    @Transactional
    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasRole(T(com.faendir.acra.model.User\$Role).ADMIN)")
    fun setPermission(user: User, app: App, level: Permission.Level?) {
        val permission = user.permissions.firstOrNull { p: Permission -> p.app == app }
        when {
            permission != null -> if (level != null) permission.level = level else user.permissions.remove(permission)
            level != null -> user.permissions.add(Permission(app, level))
        }
        entityManager.merge(user)
    }

    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasRole(T(com.faendir.acra.model.User\$Role).ADMIN)")
    fun getUserProvider() = QueryDslDataProvider(JPAQuery<Any>(entityManager).from(USER).where(USER.roles.any().eq(User.Role.USER)).select(USER))

    companion object {
        private val USER = QUser.user

        private const val hasAdminCache = "hasAdmin"
    }

}