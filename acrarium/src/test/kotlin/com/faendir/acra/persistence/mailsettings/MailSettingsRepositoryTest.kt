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
package com.faendir.acra.persistence.mailsettings

import com.faendir.acra.annotation.PersistenceTest
import com.faendir.acra.persistence.TestDataBuilder
import com.faendir.acra.persistence.app.AppId
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isNotNull
import strikt.assertions.isTrue
import kotlin.properties.Delegates

@PersistenceTest
class MailSettingsRepositoryTest(
    @Autowired
    private val testDataBuilder: TestDataBuilder,
    @Autowired
    private val mailSettingsRepository: MailSettingsRepository,
) {
    var app: AppId by Delegates.notNull()
    lateinit var user: String

    @BeforeEach
    fun setup() {
        app = testDataBuilder.createApp()
        user = testDataBuilder.createUser()
    }

    @Test
    fun `should find by app and username`() {
        testDataBuilder.createMailSettings(app, user, spike = true)
        testDataBuilder.createMailSettings(app)
        testDataBuilder.createMailSettings(user = user)

        expectThat(mailSettingsRepository.find(app, user)).isNotNull().get { spike }.isTrue()
    }

    @Test
    fun `should get all`() {
        testDataBuilder.createMailSettings(app, user, spike = true)
        testDataBuilder.createMailSettings(app)
        testDataBuilder.createMailSettings(user = user)

        expectThat(mailSettingsRepository.getAll()).hasSize(3)
    }

    @Test
    fun `should find all by app`() {
        testDataBuilder.createMailSettings(app, user, spike = true)
        testDataBuilder.createMailSettings(app)
        testDataBuilder.createMailSettings(user = user)

        expectThat(mailSettingsRepository.findAll(app)).hasSize(2)
    }

    @Test
    fun `should store new settings`() {
        mailSettingsRepository.store(MailSettings(app, user, spike = true))

        expectThat(mailSettingsRepository.find(app, user)).isNotNull().get { spike }.isTrue()
        expectThat(mailSettingsRepository.getAll()).hasSize(1)
    }

    @Test
    fun `should override existing settings`() {
        testDataBuilder.createMailSettings(app, user)

        mailSettingsRepository.store(MailSettings(app, user, spike = true))

        expectThat(mailSettingsRepository.find(app, user)).isNotNull().get { spike }.isTrue()
        expectThat(mailSettingsRepository.getAll()).hasSize(1)
    }
}