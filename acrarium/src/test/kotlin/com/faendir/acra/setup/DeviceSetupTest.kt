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

        expectThat(jooq.fetchCount(DEVICE)).isEqualTo(36317)
    }
}