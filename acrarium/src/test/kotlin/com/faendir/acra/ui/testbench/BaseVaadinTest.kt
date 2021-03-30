package com.faendir.acra.ui.testbench

import ch.vorburger.mariadb4j.springboot.autoconfigure.MariaDB4jSpringConfiguration
import com.faendir.acra.MariaDBTestConfiguration
import com.vaadin.testbench.TestBenchTestCase
import com.vaadin.testbench.commands.TestBenchCommandExecutor
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.platform.commons.annotation.Testable
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.Testcontainers

@Testable
@ExtendWith(ContainerExtension::class, SpringExtension::class)
@Import(value = [GlobalPersistentSecurityContextHolderStrategy::class, MariaDBTestConfiguration::class])
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EnableAutoConfiguration(exclude = [MariaDB4jSpringConfiguration::class])
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