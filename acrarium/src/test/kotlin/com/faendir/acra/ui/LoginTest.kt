package com.faendir.acra.ui

import com.faendir.acra.security.SecurityUtils
import com.faendir.acra.service.UserService
import com.faendir.acra.ui.testbench.BaseVaadinTest
import com.faendir.acra.ui.testbench.KBrowserWebDriverContainer
import com.faendir.acra.ui.testbench.PASSWORD
import com.faendir.acra.ui.testbench.USERNAME
import com.faendir.acra.annotation.VaadinTest
import com.faendir.acra.ui.testbench.createTestUser
import com.faendir.acra.ui.testbench.getPage
import com.vaadin.flow.component.login.testbench.LoginFormElement
import org.junit.platform.commons.annotation.Testable
import org.springframework.beans.factory.annotation.Autowired
import strikt.api.expectThat
import strikt.assertions.isFalse
import strikt.assertions.isTrue


class LoginTest : BaseVaadinTest() {

    @Autowired
    private lateinit var userService: UserService

    @Testable
    @VaadinTest
    fun login(container: KBrowserWebDriverContainer, browserName: String) {
        userService.createTestUser()
        driver.getPage(port)
        tryLoginWith(USERNAME, PASSWORD)
        expectThat(SecurityUtils.isLoggedIn()).isTrue()
    }

    @Testable
    @VaadinTest
    fun loginNoUserName(container: KBrowserWebDriverContainer, browserName: String) {
        userService.createTestUser()
        driver.getPage(port)
        tryLoginWith("", PASSWORD)
        expectThat(SecurityUtils.isLoggedIn()).isFalse()
    }

    @Testable
    @VaadinTest
    fun loginNoPassword(container: KBrowserWebDriverContainer, browserName: String) {
        userService.createTestUser()
        driver.getPage(port)
        tryLoginWith(USERNAME, "")
        expectThat(SecurityUtils.isLoggedIn()).isFalse()
    }

    @Testable
    @VaadinTest
    fun loginNonExistingUser(container: KBrowserWebDriverContainer, browserName: String) {
        userService.createTestUser()
        driver.getPage(port)
        tryLoginWith("ThisUsernameDoesNotExist", PASSWORD)
        expectThat(SecurityUtils.isLoggedIn()).isFalse()
    }

    @Testable
    @VaadinTest
    fun loginWrongPassword(container: KBrowserWebDriverContainer, browserName: String) {
        userService.createTestUser()
        driver.getPage(port)
        tryLoginWith(USERNAME, "WrongPassword")
        expectThat(SecurityUtils.isLoggedIn()).isFalse()
    }

    private fun tryLoginWith(username: String, password: String) {
        val loginForm = `$`(LoginFormElement::class.java).first()
        loginForm.usernameField.value = username
        loginForm.passwordField.value = password
        loginForm.submitButton.click()
        waitForVaadin()
    }
}

