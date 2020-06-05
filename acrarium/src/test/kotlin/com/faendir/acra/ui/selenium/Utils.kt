package com.faendir.acra.ui.selenium

import com.faendir.acra.model.User
import com.faendir.acra.service.UserService
import com.faendir.acra.ui.component.UserEditor
import com.vaadin.flow.server.VaadinService
import com.vaadin.flow.server.VaadinSession
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.remote.RemoteWebDriver


fun RemoteWebDriver.findInputById(id: String): WebElement = findElementById(id).findElement(By.tagName("input"))

const val USERNAME = "TestUser"
const val PASSWORD = "TestPassword12#'\"?"

fun RemoteWebDriver.getPage(port: Int, page: String = "") {
    get("http://host.testcontainers.internal:$port/$page")
}

fun UserService.createTestUser() {
    store(User(USERNAME, "", mutableSetOf(User.Role.ADMIN, User.Role.USER), PASSWORD))
}