package com.faendir.acra.ui.testbench

import com.faendir.acra.model.User
import com.faendir.acra.service.UserService
import org.openqa.selenium.WebDriver
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken

const val USERNAME = "TestUser"
const val PASSWORD = "TestPassword12#'\"?"

fun UserService.createTestUser() {
    store(User(USERNAME, "", mutableSetOf(User.Role.ADMIN, User.Role.USER), PASSWORD))
}