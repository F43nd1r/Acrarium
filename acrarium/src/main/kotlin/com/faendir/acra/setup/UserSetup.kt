package com.faendir.acra.setup

import com.faendir.acra.model.User
import com.faendir.acra.service.UserService
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
class UserSetup(private val userService: UserService) : ApplicationRunner {

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
                        User.Role.valueOf(it.uppercase())
                    } catch (e: Exception) {
                        logger.error { "Unknown role $it. No users created." }
                        return
                    }
                }
            } ?: emptyList()).let { it + MutableList(names.size - it.size) { listOf(User.Role.ADMIN, User.Role.USER, User.Role.API) } }
            names.zip(passwords, rolesList).forEach { (name, password, roles) ->
                if (name.isBlank()) {
                    logger.error { "Username may not be blank." }
                    return@forEach
                }
                if (userService.getUser(name) != null) {
                    logger.warn { "User $name already exists." }
                    return@forEach
                }
                if (password.isBlank()) {
                    logger.error { "Password my not be blank." }
                    return@forEach
                }
                if (roles.contains(User.Role.REPORTER)) {
                    logger.error { "Reporter users may not be created manually." }
                    return@forEach
                }
                val user = User(name, "", roles.toMutableSet().apply { if (contains(User.Role.ADMIN)) add(User.Role.USER) }, password, null)
                userService.store(user)
                logger.info { "Created user $name with roles $roles." }
            }
        }
    }

}