package com.faendir.acra.persistence.device

import com.faendir.acra.annotation.PersistenceTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNull
import strikt.assertions.isTrue

@PersistenceTest
class DeviceRepositoryTest(
    @Autowired
    private val deviceRepository: DeviceRepository
) {
    @Test
    fun `should store and find marketing names for devices`() {
        expectThat(deviceRepository.findMarketingName("model", "device")).isNull()
        expectThat(deviceRepository.isEmpty()).isTrue()

        deviceRepository.store("device", "model", "name")
        deviceRepository.store("otherDevice", "otherMode", "otherName")


        expectThat(deviceRepository.findMarketingName("model", "device")).isEqualTo("name")
        expectThat(deviceRepository.isEmpty()).isFalse()
    }
}