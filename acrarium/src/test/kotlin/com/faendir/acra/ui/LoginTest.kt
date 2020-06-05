package com.faendir.acra.ui

import com.faendir.acra.security.SecurityUtils
import com.faendir.acra.ui.selenium.BaseSeleniumTest
import com.faendir.acra.ui.selenium.KBrowserWebDriverContainer
import com.faendir.acra.ui.selenium.SeleniumTest
import com.faendir.acra.ui.selenium.getPage
import com.faendir.acra.ui.selenium.login
import com.faendir.acra.ui.selenium.registerTestUser
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import strikt.api.expectThat
import strikt.assertions.isTrue


class LoginTest : BaseSeleniumTest() {

    @SeleniumTest
    fun login(container: KBrowserWebDriverContainer, browserName: String) {
        container.webDriver.getPage(port)
        container.webDriver.registerTestUser()
        container.webDriver.login()
        runBlocking {
            delay(5000)
            expectThat(SecurityUtils.isLoggedIn()).isTrue()
        }
    }
}