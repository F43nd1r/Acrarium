package com.faendir.acra.ui.testbench

import ch.vorburger.mariadb4j.springboot.autoconfigure.MariaDB4jSpringConfiguration
import com.faendir.acra.MariaDBTestConfiguration
import com.faendir.acra.annotation.AcrariumTest
import com.faendir.acra.setup.DeviceSetup
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
@AcrariumTest
@ExtendWith(ContainerExtension::class)
@Import(GlobalPersistentSecurityContextHolderStrategy::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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