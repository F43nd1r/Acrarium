package com.faendir.acra.ui

import com.faendir.acra.ui.selenium.BaseSeleniumTest
import com.faendir.acra.ui.selenium.KBrowserWebDriverContainer
import com.faendir.acra.ui.selenium.PASSWORD
import com.faendir.acra.ui.selenium.SeleniumTest
import com.faendir.acra.ui.selenium.USERNAME
import com.faendir.acra.ui.selenium.getPage
import com.faendir.acra.service.UserService
import com.faendir.acra.ui.component.UserEditor
import com.faendir.acra.ui.selenium.findInputById
import org.junit.platform.commons.annotation.Testable
import org.springframework.beans.factory.annotation.Autowired
import strikt.api.expectThat
import strikt.assertions.isNotNull
import strikt.assertions.isTrue

class InitialSetupTest : BaseSeleniumTest() {

    @Autowired
    private lateinit var userService: UserService

    @Testable
    @SeleniumTest
    fun setup(container: KBrowserWebDriverContainer, browserName: String) {
        container.webDriver.getPage(port)
        container.webDriver.findInputById(UserEditor.USERNAME_ID).sendKeys(USERNAME)
        container.webDriver.findInputById(UserEditor.PASSWORD_ID).sendKeys(PASSWORD)
        container.webDriver.findInputById(UserEditor.REPEAT_PASSWORD_ID).sendKeys(PASSWORD)
        container.webDriver.findElementById(UserEditor.SUBMIT_ID).click()
        explicitlyWait()
        expectThat(userService) {
            get { hasAdmin() }.isTrue()
            get { getUser(USERNAME) }.isNotNull()
            get { checkPassword(getUser(USERNAME), PASSWORD) }.isTrue()
        }
    }
}