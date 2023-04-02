package com.faendir.acra.setup

import com.faendir.acra.annotation.PersistenceTest
import com.faendir.acra.jooq.generated.tables.references.DEVICE
import com.faendir.acra.persistence.device.DeviceRepository
import com.faendir.acra.settings.AcrariumConfiguration
import org.jooq.DSLContext
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.event.ContextRefreshedEvent
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.util.regex.Pattern

@PersistenceTest
class DeviceSetupTest(
    @Autowired private val deviceRepository: DeviceRepository, @Autowired private val applicationContext: ApplicationContext, @Autowired
    private val jooq: DSLContext
) {
    private val deviceSetup = DeviceSetup(deviceRepository, AcrariumConfiguration(false, Pattern.compile("")))

    @Test
    fun `should set up devices`() {
        deviceSetup.onStartup(ContextRefreshedEvent(applicationContext))

        expectThat(jooq.fetchCount(DEVICE)).isEqualTo(35002)
    }
}