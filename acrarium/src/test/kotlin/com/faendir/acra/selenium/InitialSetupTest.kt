package com.faendir.acra.selenium

import com.faendir.acra.service.UserService
import com.faendir.acra.ui.component.UserEditor
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.Testcontainers
import strikt.api.expectThat
import strikt.assertions.isNotNull
import strikt.assertions.isTrue

@ExtendWith(ContainerExtension::class, SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("/application-mysql.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class InitialSetupTest {

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var userService: UserService

    @BeforeEach
    fun setup() {
        Testcontainers.exposeHostPorts(port)
    }

    @SeleniumTest
    fun testSetup(container: KBrowserWebDriverContainer, browserName: String) {
        container.webDriver.get("http://host.testcontainers.internal:$port")
        val user = "TestUser"
        val pass = "TestPassword12!?'#"
        container.webDriver.findInputById(UserEditor.USERNAME_ID).sendKeys(user)
        container.webDriver.findInputById(UserEditor.PASSWORD_ID).sendKeys(pass)
        container.webDriver.findInputById(UserEditor.REPEAT_PASSWORD_ID).sendKeys(pass)
        container.webDriver.findElementById(UserEditor.SUBMIT_ID).click()
        Thread.sleep(5000)
        expectThat(userService) {
            get { hasAdmin() }.isTrue()
            get { getUser(user) }.isNotNull()
            get { checkPassword(getUser(user), pass) }.isTrue()
        }
    }
}