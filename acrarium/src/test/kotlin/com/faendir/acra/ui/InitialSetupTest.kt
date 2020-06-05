package com.faendir.acra.ui

import com.faendir.acra.ui.selenium.BaseSeleniumTest
import com.faendir.acra.ui.selenium.KBrowserWebDriverContainer
import com.faendir.acra.ui.selenium.PASSWORD
import com.faendir.acra.ui.selenium.SeleniumTest
import com.faendir.acra.ui.selenium.USERNAME
import com.faendir.acra.ui.selenium.getPage
import com.faendir.acra.ui.selenium.registerTestUser
import com.faendir.acra.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import strikt.api.expectThat
import strikt.assertions.isNotNull
import strikt.assertions.isTrue

class InitialSetupTest : BaseSeleniumTest() {

    @Autowired
    private lateinit var userService: UserService

    @SeleniumTest
    fun setup(container: KBrowserWebDriverContainer, browserName: String) {
        container.webDriver.getPage(port)
        container.webDriver.registerTestUser()
        Thread.sleep(5000)
        expectThat(userService) {
            get { hasAdmin() }.isTrue()
            get { getUser(USERNAME) }.isNotNull()
            get { checkPassword(getUser(USERNAME), PASSWORD) }.isTrue()
        }
    }
}