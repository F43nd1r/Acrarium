package com.faendir.acra.ui.testbench

import com.faendir.acra.BackendApplication
import com.faendir.acra.annotation.AcrariumTest
import com.vaadin.testbench.TestBenchTestCase
import com.vaadin.testbench.commands.TestBenchCommandExecutor
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.firefox.FirefoxOptions
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.context.WebServerInitializedEvent
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ApplicationListener
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Import
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.Testcontainers
import org.testcontainers.containers.BrowserWebDriverContainer
import org.testcontainers.containers.VncRecordingContainer
import org.testcontainers.junit.jupiter.Container
import java.io.File

@AcrariumTest
@Import(GlobalPersistentSecurityContextHolderStrategy::class)
@SpringBootTest(classes = [BackendApplication::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class BaseVaadinTest : TestBenchTestCase(), HasContainer {
    @LocalServerPort
    protected var port: Int = 0

    private lateinit var container: KBrowserWebDriverContainer

    @BeforeEach
    fun setup() {
        Testcontainers.exposeHostPorts(port)
        container = newContainer()
        container.start()
        setDriver(container.webDriver)
    }

    @AfterEach
    fun teardown() {
        container.stop()
        //we're ignoring context clears, so set null manually after the test
        SecurityContextHolder.setContext(null)
    }

    fun getPage(port: Int, page: String = "") {
        driver.get("http://host.testcontainers.internal:$port/$page")
        waitForVaadin()
    }

    fun waitForVaadin() {
        (testBench() as TestBenchCommandExecutor).waitForVaadin()
    }
}

interface HasContainer {
    fun newContainer(): KBrowserWebDriverContainer
}

class HasFirefox : HasContainer {
    override fun newContainer(): KBrowserWebDriverContainer = KBrowserWebDriverContainer().withCapabilities(FirefoxOptions())
}

class HasChrome : HasContainer {
    override fun newContainer(): KBrowserWebDriverContainer = KBrowserWebDriverContainer().withCapabilities(ChromeOptions())
}