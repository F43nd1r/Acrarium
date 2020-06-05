package com.faendir.acra.ui

import com.faendir.acra.security.SecurityUtils
import com.faendir.acra.service.UserService
import com.faendir.acra.ui.selenium.BaseSeleniumTest
import com.faendir.acra.ui.selenium.KBrowserWebDriverContainer
import com.faendir.acra.ui.selenium.PASSWORD
import com.faendir.acra.ui.selenium.SeleniumTest
import com.faendir.acra.ui.selenium.USERNAME
import com.faendir.acra.ui.selenium.createTestUser
import com.faendir.acra.ui.selenium.findInputById
import com.faendir.acra.ui.selenium.getPage
import org.junit.platform.commons.annotation.Testable
import org.openqa.selenium.remote.RemoteWebDriver
import org.springframework.beans.factory.annotation.Autowired
import strikt.api.expectThat
import strikt.assertions.isFalse
import strikt.assertions.isTrue


class LoginTest : BaseSeleniumTest() {

    @Autowired
    private lateinit var userService: UserService

    @Testable
    @SeleniumTest
    fun login(container: KBrowserWebDriverContainer, browserName: String) {
        userService.createTestUser()
        container.webDriver.getPage(port)
        container.webDriver.tryLoginWith(USERNAME, PASSWORD)
        Thread.sleep(5000)
        expectThat(SecurityUtils.isLoggedIn()).isTrue()
    }

    @Testable
    @SeleniumTest
    fun loginNoUserName(container: KBrowserWebDriverContainer, browserName: String) {
        userService.createTestUser()
        container.webDriver.getPage(port)
        container.webDriver.tryLoginWith("", PASSWORD)
        Thread.sleep(5000)
        expectThat(SecurityUtils.isLoggedIn()).isFalse()
    }

    @Testable
    @SeleniumTest
    fun loginNoPassword(container: KBrowserWebDriverContainer, browserName: String) {
        userService.createTestUser()
        container.webDriver.getPage(port)
        container.webDriver.tryLoginWith(USERNAME, "")
        Thread.sleep(5000)
        expectThat(SecurityUtils.isLoggedIn()).isFalse()
    }

    @Testable
    @SeleniumTest
    fun loginNonExistingUser(container: KBrowserWebDriverContainer, browserName: String) {
        userService.createTestUser()
        container.webDriver.getPage(port)
        container.webDriver.tryLoginWith("ThisUsernameDoesNotExist", PASSWORD)
        Thread.sleep(5000)
        expectThat(SecurityUtils.isLoggedIn()).isFalse()
    }

    @Testable
    @SeleniumTest
    fun loginWrongPassword(container: KBrowserWebDriverContainer, browserName: String) {
        userService.createTestUser()
        container.webDriver.getPage(port)
        container.webDriver.tryLoginWith(USERNAME, "WrongPassword")
        Thread.sleep(5000)
        expectThat(SecurityUtils.isLoggedIn()).isFalse()
    }

    private fun RemoteWebDriver.tryLoginWith(username: String, password: String) {
        findInputById("vaadinLoginUsername").sendKeys(username)
        findInputById("vaadinLoginPassword").sendKeys(password)
        findElementByCssSelector("""[part="vaadin-login-submit"]""").click()
    }
}