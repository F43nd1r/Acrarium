/*
 * (C) Copyright 2021-2022 Lukas Morawietz (https://github.com/F43nd1r)
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
package com.faendir.acra.setup

import com.faendir.acra.persistence.user.Role
import com.faendir.acra.persistence.user.UserRepository
import com.faendir.acra.util.zip
import mu.KotlinLogging
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

private val logger = KotlinLogging.logger {}
private const val CREATE_USER_OPTION = "create-user"
private const val PASSWORD_OPTION = "password"
private const val ROLES_OPTION = "roles"

@Component
class UserSetup(private val userRepository: UserRepository) : ApplicationRunner {

    override fun run(args: ApplicationArguments) {
        if (args.containsOption(CREATE_USER_OPTION)) {
            val names = args.getOptionValues(CREATE_USER_OPTION)
            if (names.isEmpty()) {
                logger.error { "No username provided. No users created." }
                return
            }
            if (!args.containsOption(PASSWORD_OPTION)) {
                logger.error { "No password provided. No users created." }
                return
            }
            val passwords = args.getOptionValues(PASSWORD_OPTION)
            if (names.size != passwords.size) {
                logger.error { "User and password count do not match. No users created." }
                return
            }
            val rolesList = (args.getOptionValues(ROLES_OPTION)?.map { rolesString ->
                rolesString.split(",").map {
                    try {
                        Role.valueOf(it.uppercase())
                    } catch (e: Exception) {
                        logger.error { "Unknown role $it. No users created." }
                        return
                    }
                }.toMutableSet().apply { if (contains(Role.ADMIN)) add(Role.USER) }
            } ?: emptyList()).let { it + MutableList(names.size - it.size) { setOf(Role.ADMIN, Role.USER, Role.API) } }
            names.zip(passwords, rolesList).forEach { (name, password, roles) ->
                if (name.isBlank()) {
                    logger.error { "Username may not be blank." }
                    return@forEach
                }
                if (userRepository.exists(name)) {
                    logger.warn { "User $name already exists." }
                    return@forEach
                }
                if (password.isBlank()) {
                    logger.error { "Password may not be blank." }
                    return@forEach
                }
                if (roles.contains(Role.REPORTER)) {
                    logger.error { "Reporter users may not be created manually." }
                    return@forEach
                }
                userRepository.create(name, password, null, *roles.toTypedArray())
                logger.info { "Created user $name with roles $roles." }
            }
        }
    }

}