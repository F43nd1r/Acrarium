package com.faendir.acra.ui.testbench

import com.faendir.acra.model.User
import com.faendir.acra.service.UserService
import org.openqa.selenium.WebDriver

const val USERNAME = "TestUser"
const val PASSWORD = "TestPassword12#'\"?"

fun WebDriver.getPage(port: Int, page: String = "") {
    get("http://host.testcontainers.internal:$port/$page")
}

fun UserService.createTestUser() {
    store(User(USERNAME, "", mutableSetOf(User.Role.ADMIN, User.Role.USER), PASSWORD))
}