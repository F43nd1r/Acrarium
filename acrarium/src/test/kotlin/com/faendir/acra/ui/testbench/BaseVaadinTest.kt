package com.faendir.acra.ui.testbench

import com.vaadin.testbench.TestBenchTestCase
import com.vaadin.testbench.commands.TestBenchCommandExecutor
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
@ExtendWith(ContainerExtension::class, SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("/application-mysql.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
abstract class BaseVaadinTest : TestBenchTestCase() {
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

    fun waitForVaadin() {
        (testBench() as TestBenchCommandExecutor).waitForVaadin()
    }
}