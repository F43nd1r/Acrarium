/*
 * (C) Copyright 2023 Lukas Morawietz (https://github.com/F43nd1r)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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