package com.faendir.acra.ui.selenium

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.platform.commons.annotation.Testable
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.Testcontainers

@Testable
@ExtendWith(
        ContainerExtension::class, SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("/application-mysql.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
abstract class BaseSeleniumTest {
    @LocalServerPort
    protected var port: Int = 0

    @BeforeEach
    fun setup() {
        //host port needs to be visible from containers
        Testcontainers.exposeHostPorts(port)
    }

    @AfterEach
    fun teardown() {
        //we're ignoring context clears, so set null manually after the test
        SecurityContextHolder.setContext(null)
    }

    fun explicitlyWait() {
        Thread.sleep(WAIT_TIME_MS)
    }

    companion object {
        const val WAIT_TIME_S: Long = 20
        const val WAIT_TIME_MS = WAIT_TIME_S * 1000
    }
}