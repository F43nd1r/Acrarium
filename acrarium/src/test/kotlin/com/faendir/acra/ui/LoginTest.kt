package com.faendir.acra.ui

import com.faendir.acra.security.SecurityUtils
import com.faendir.acra.service.UserService
import com.faendir.acra.ui.testbench.BaseVaadinTest
import com.faendir.acra.ui.testbench.HasChrome
import com.faendir.acra.ui.testbench.HasContainer
import com.faendir.acra.ui.testbench.HasFirefox
import com.faendir.acra.ui.testbench.PASSWORD
import com.faendir.acra.ui.testbench.USERNAME
import com.faendir.acra.ui.testbench.createTestUser
import com.vaadin.flow.component.login.testbench.LoginFormElement
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import strikt.api.expectThat
import strikt.assertions.isFalse
import strikt.assertions.isTrue


abstract class LoginTest : BaseVaadinTest() {

    @Autowired
    private lateinit var userService: UserService

    @Test
    fun login()  {
        userService.createTestUser()
        getPage(port)
        tryLoginWith(USERNAME, PASSWORD)
        expectThat(SecurityUtils.isLoggedIn()).isTrue()
    }

    @Test
    fun loginNoUserName()  {
        userService.createTestUser()
        getPage(port)
        tryLoginWith("", PASSWORD)
        expectThat(SecurityUtils.isLoggedIn()).isFalse()
    }

    @Test
    fun loginNoPassword()  {
        userService.createTestUser()
        getPage(port)
        tryLoginWith(USERNAME, "")
        expectThat(SecurityUtils.isLoggedIn()).isFalse()
    }

    @Test
    fun loginNonExistingUser()  {
        userService.createTestUser()
        getPage(port)
        tryLoginWith("ThisUsernameDoesNotExist", PASSWORD)
        expectThat(SecurityUtils.isLoggedIn()).isFalse()
    }

    @Test
    fun loginWrongPassword() {
        userService.createTestUser()
        getPage(port)
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

class FirefoxLoginTest : LoginTest(), HasContainer by HasFirefox()
class ChromeLoginTest : LoginTest(), HasContainer by HasChrome()

