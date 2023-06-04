/*
 * (C) Copyright 2022-2023 Lukas Morawietz (https://github.com/F43nd1r)
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

import com.faendir.acra.annotation.PersistenceTest
import com.faendir.acra.dataprovider.AcrariumDataProvider
import com.faendir.acra.dataprovider.AcrariumSort
import com.faendir.acra.persistence.TestDataBuilder
import com.faendir.acra.persistence.app.AppId
import com.ninjasquad.springmockk.MockkBean
import com.vaadin.flow.data.provider.SortDirection
import io.mockk.every
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import strikt.api.expectThat
import strikt.assertions.*
import kotlin.properties.Delegates


@PersistenceTest
class UserRepositoryTest(
    @Autowired
    private val userRepository: UserRepository,
    @Autowired
    private val testDataBuilder: TestDataBuilder,
    @Autowired
    @MockkBean
    private val passwordEncoder: PasswordEncoder,
) {

    private val username = "username"

    @Nested
    inner class Exists {
        @Test
        fun `should return if user exists`() {
            expectThat(userRepository.exists(username)).isFalse()

            testDataBuilder.createUser(username)

            expectThat(userRepository.exists(username)).isTrue()
        }
    }

    @Nested
    inner class Find {
        @Test
        fun `should find user`() {
            expectThat(userRepository.find(username)).isNull()

            testDataBuilder.createUser(username)

            expectThat(userRepository.find(username)).isNotNull().and {
                get { this.username }.isEqualTo(username)
            }
        }

        @Test
        fun `should find user regardless of case`() {
            expectThat(userRepository.find("UsErNaMe")).isNull()

            testDataBuilder.createUser("username")

            expectThat(userRepository.find("UsErNaMe")).isNotNull()
        }
    }

    @Nested
    inner class Create {
        @Test
        fun `should encode password`() {
            every { passwordEncoder.encode(any()) } answers { "encoded-" + firstArg<String>() }

            userRepository.create(username, "password", null)

            expectThat(userRepository.find(username)?.password).isEqualTo("encoded-password")
        }

        @Test
        fun `should add roles`() {
            every { passwordEncoder.encode(any()) } returns ""

            userRepository.create(username, "password", null, Role.USER, Role.API)

            expectThat(userRepository.getAuthorities(username)).containsExactlyInAnyOrder(Role.USER, Role.API)
        }

        @Test
        fun `should enforce lowercase username`() {
            every { passwordEncoder.encode(any()) } returns ""

            userRepository.create("UsErNaMe", "password", null, Role.USER, Role.API)

            expectThat(userRepository.find("username")).isNotNull().and {
                get { this.username }.isEqualTo("username")
            }
        }

        @Test
        fun `should store mail address`() {
            every { passwordEncoder.encode(any()) } returns ""

            userRepository.create(username, "password", "mail", Role.USER, Role.API)

            expectThat(userRepository.find(username)).isNotNull().and {
                get { this.mail }.isEqualTo("mail")
            }
        }
    }

    @Nested
    inner class Update {

        @Test
        fun `should encode password if provided`() {
            testDataBuilder.createUser(username)
            every { passwordEncoder.encode(any()) } answers { "encoded-" + firstArg<String>() }

            userRepository.update(username, "password", null)

            expectThat(userRepository.find(username)?.password).isEqualTo("encoded-password")
        }

        @Test
        fun `should not change password if not provided`() {
            testDataBuilder.createUser(username, "password")
            every { passwordEncoder.encode(any()) } answers { "encoded-" + firstArg<String>() }

            userRepository.update(username, null, null)

            expectThat(userRepository.find(username)?.password).isEqualTo("password")
        }

        @Test
        fun `should store mail address`() {
            testDataBuilder.createUser(username)
            every { passwordEncoder.encode(any()) } answers { "" }

            userRepository.update(username, "password", "mail")

            expectThat(userRepository.find(username)?.mail).isEqualTo("mail")
        }
    }

    @Nested
    inner class SetRole {
        @BeforeEach
        fun setup() {
            testDataBuilder.createUser(username)
        }

        @Test
        fun `should add role`() {
            expectThat(userRepository.getAuthorities(username)).doesNotContain(Role.ADMIN)

            userRepository.setRole(username, Role.ADMIN, true)

            expectThat(userRepository.getAuthorities(username)).contains(Role.ADMIN)
        }

        @Test
        fun `should ignore existing role`() {
            userRepository.setRole(username, Role.ADMIN, true)

            expectThat(userRepository.getAuthorities(username)).contains(Role.ADMIN)

            userRepository.setRole(username, Role.ADMIN, true)

            expectThat(userRepository.getAuthorities(username)).contains(Role.ADMIN)
        }

        @Test
        fun `should ignore missing role`() {
            expectThat(userRepository.getAuthorities(username)).doesNotContain(Role.ADMIN)

            userRepository.setRole(username, Role.ADMIN, false)

            expectThat(userRepository.getAuthorities(username)).doesNotContain(Role.ADMIN)
        }

        @Test
        fun `should remove role`() {
            userRepository.setRole(username, Role.ADMIN, true)

            expectThat(userRepository.getAuthorities(username)).contains(Role.ADMIN)

            userRepository.setRole(username, Role.ADMIN, false)

            expectThat(userRepository.getAuthorities(username)).doesNotContain(Role.ADMIN)
        }
    }

    @Nested
    inner class SetPermission {
        var appId by Delegates.notNull<AppId>()

        @BeforeEach
        fun setup() {
            testDataBuilder.createUser(username)
            appId = testDataBuilder.createApp()
        }

        @Test
        fun `should set permission`() {
            expectThat(userRepository.getAuthorities(username)).doesNotContain(Permission(appId, Permission.Level.EDIT))

            userRepository.setPermission(username, appId, Permission.Level.EDIT)

            expectThat(userRepository.getAuthorities(username)).contains(Permission(appId, Permission.Level.EDIT))
        }

        @Test
        fun `should update existing permission`() {
            userRepository.setPermission(username, appId, Permission.Level.EDIT)

            expectThat(userRepository.getAuthorities(username)).contains(Permission(appId, Permission.Level.EDIT))

            userRepository.setPermission(username, appId, Permission.Level.ADMIN)

            expectThat(userRepository.getAuthorities(username)).contains(Permission(appId, Permission.Level.ADMIN))
            expectThat(userRepository.getAuthorities(username)).doesNotContain(Permission(appId, Permission.Level.EDIT))
        }
    }

    @Nested
    inner class Authorities {
        @Test
        fun `should return roles and permissions`() {
            testDataBuilder.createUser(username)
            val appId = testDataBuilder.createApp()
            userRepository.setPermission(username, appId, Permission.Level.ADMIN)
            userRepository.setRole(username, Role.ADMIN, true)

            expectThat(userRepository.getAuthorities(username)).containsExactlyInAnyOrder(Role.ADMIN, Permission(appId, Permission.Level.ADMIN))
        }
    }

    @Nested
    inner class Delete {
        @Test
        fun `should delete user`() {
            val name = testDataBuilder.createUser()

            expectThat(userRepository.find(name)).isNotNull()

            userRepository.delete(name)

            expectThat(userRepository.find(name)).isNull()
        }

        @Test
        fun `should ignore nonexistent username`() {
            val name = "does not exist"

            expectThat(userRepository.find(name)).isNull()

            userRepository.delete(name)

            expectThat(userRepository.find(name)).isNull()
        }
    }

    @Nested
    inner class CheckPassword {
        @Test
        fun `should return false if user doesn't exist`() {
            expectThat(userRepository.checkPassword(username, "password")).isFalse()
        }

        @Test
        fun `should return false if password does not match`() {
            val rawPassword = "password"
            every { passwordEncoder.matches(rawPassword, any()) } returns false
            testDataBuilder.createUser(username)

            expectThat(userRepository.checkPassword(username, rawPassword)).isFalse()
        }

        @Test
        fun `should return true if password matches`() {
            val rawPassword = "password"
            every { passwordEncoder.matches(rawPassword, any()) } returns true
            testDataBuilder.createUser(username)

            expectThat(userRepository.checkPassword(username, rawPassword)).isTrue()
        }
    }

    @Nested
    inner class HasAnyAdmin {
        @Test
        fun `should return true if an admin exists`() {
            testDataBuilder.createUser(roles = arrayOf(Role.ADMIN))
            testDataBuilder.createUser(roles = arrayOf(Role.USER, Role.ADMIN, Role.API))
            testDataBuilder.createUser(roles = arrayOf(Role.USER))

            expectThat(userRepository.hasAnyAdmin()).isTrue()
        }

        @Test
        fun `should return false if no admin exists`() {
            testDataBuilder.createUser(roles = arrayOf(Role.REPORTER))
            testDataBuilder.createUser(roles = arrayOf(Role.USER, Role.API))

            expectThat(userRepository.hasAnyAdmin()).isFalse()
        }
    }

    @Nested
    inner class Provider {
        private lateinit var provider: AcrariumDataProvider<UserAuthorities, Nothing, UserAuthorities.Sort>

        @BeforeEach
        fun setup() {
            provider = userRepository.getProvider()
        }

        @Test
        fun `should return all users with USER role including authorities`() {
            val u1 = testDataBuilder.createUser(roles = arrayOf(Role.USER))
            val appId = testDataBuilder.createApp()
            userRepository.setPermission(u1, appId, Permission.Level.EDIT)
            val u2 = testDataBuilder.createUser(roles = arrayOf(Role.USER, Role.ADMIN))
            testDataBuilder.createUser(roles = arrayOf(Role.REPORTER))

            expectThat(provider.size(emptySet())).isEqualTo(2)
            expectThat(provider.fetch(emptySet(), emptyList(), 0, 10).toList()).containsExactlyInAnyOrder(
                UserAuthorities(u1, setOf(Role.USER), setOf(Permission(appId, Permission.Level.EDIT))),
                UserAuthorities(u2, setOf(Role.USER, Role.ADMIN), emptySet())
            )
        }

        @Test
        fun `should sort returned users`() {
            val u1 = testDataBuilder.createUser(username = "u1", roles = arrayOf(Role.USER))
            val u2 = testDataBuilder.createUser(username = "u2", roles = arrayOf(Role.USER))

            expectThat(
                provider.fetch(emptySet(), listOf(AcrariumSort(UserAuthorities.Sort.USERNAME, SortDirection.ASCENDING)), 0, 10).toList()
                    .map { it.username })
                .isEqualTo(listOf(u1, u2))
        }

        @Test
        fun `should offset and limit returned users`() {
            val u1 = testDataBuilder.createUser(username = "u1", roles = arrayOf(Role.USER))
            val u2 = testDataBuilder.createUser(username = "u2", roles = arrayOf(Role.USER))

            expectThat(
                provider.fetch(emptySet(), listOf(AcrariumSort(UserAuthorities.Sort.USERNAME, SortDirection.ASCENDING)), 0, 1).toList()
                    .map { it.username })
                .isEqualTo(listOf(u1))
            expectThat(
                provider.fetch(emptySet(), listOf(AcrariumSort(UserAuthorities.Sort.USERNAME, SortDirection.ASCENDING)), 1, 1).toList()
                    .map { it.username })
                .isEqualTo(listOf(u2))
        }

        @Test
        fun `should limit correctly regardless of number of roles and permissions`() {
            val u1 = testDataBuilder.createUser(username = "u1", roles = arrayOf(Role.USER, Role.ADMIN, Role.API))
            testDataBuilder.createPermission(u1, testDataBuilder.createApp(), Permission.Level.ADMIN)
            testDataBuilder.createPermission(u1, testDataBuilder.createApp(), Permission.Level.ADMIN)
            testDataBuilder.createPermission(u1, testDataBuilder.createApp(), Permission.Level.EDIT)
            testDataBuilder.createPermission(u1, testDataBuilder.createApp(), Permission.Level.VIEW)
            testDataBuilder.createPermission(u1, testDataBuilder.createApp(), Permission.Level.NONE)

            expectThat(
                provider.fetch(emptySet(), listOf(AcrariumSort(UserAuthorities.Sort.USERNAME, SortDirection.ASCENDING)), 0, 1).toList().first()
            ) {
                get { this.username }.isEqualTo(u1)
                get { this.roles }.containsExactlyInAnyOrder(Role.USER, Role.ADMIN, Role.API)
                get { this.permissions }.hasSize(5)
            }
        }
    }
}