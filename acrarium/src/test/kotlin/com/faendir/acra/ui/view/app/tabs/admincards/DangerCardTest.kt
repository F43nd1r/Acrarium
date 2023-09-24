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
package com.faendir.acra.ui.view.app.tabs.admincards

import com.faendir.acra.common.UiParams
import com.faendir.acra.common.UiTest
import com.faendir.acra.common.captionId
import com.faendir.acra.i18n.Messages
import com.faendir.acra.persistence.TestDataBuilder
import com.faendir.acra.persistence.app.AppRepository
import com.faendir.acra.persistence.report.ReportRepository
import com.faendir.acra.persistence.user.Role
import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.ui.view.Overview
import com.faendir.acra.ui.view.app.AppView
import com.faendir.acra.ui.view.app.tabs.AdminTab
import com.github.mvysny.kaributesting.v10.*
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.html.Label
import com.vaadin.flow.component.textfield.NumberField
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import strikt.api.expectThat
import strikt.assertions.isNotEqualTo
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import java.time.Instant
import java.time.temporal.ChronoUnit

class DangerCardTest(
    @Autowired private val testDataBuilder: TestDataBuilder,
    @Autowired private val appRepository: AppRepository,
    @Autowired private val reportRepository: ReportRepository,
) : UiTest() {
    private var appId = testDataBuilder.createApp()

    override fun setup() = UiParams(
        route = AdminTab::class,
        routeParameters = AppView.getNavigationParams(appId),
        requiredAuthorities = setOf(Role.USER, Role.ADMIN)
    )

    @Test
    fun `should recreate reporter`() {
        withAuth(Role.USER, Role.ADMIN) {
            val oldReporter = appRepository.find(appId)!!.reporterUsername

            val card = _get<DangerCard>()
            card._get<Translatable<Button>> { captionId = Messages.CREATE }.content._click()
            _get<Translatable<Button>> { captionId = Messages.CONFIRM }.content._click()

            val reporter = appRepository.find(appId)!!.reporterUsername

            expectThat(reporter).isNotEqualTo(oldReporter)

            _expectOne<Label> {
                predicates.add { it.element.outerHTML?.contains(reporter) == true }
            }

            _get<Translatable<Button>> { captionId = Messages.CLOSE }.content._click()

            _expectNoDialogs()
        }
    }

    @Test
    fun `should purge old reports by date`() {
        withAuth(Role.USER, Role.ADMIN) {
            val now = Instant.now()
            val oldReport = testDataBuilder.createReport(appId, date = now.minus(2, ChronoUnit.DAYS))
            val newReport = testDataBuilder.createReport(appId, date = now)

            val card = _get<DangerCard>()
            card._find<Translatable<Button>> { captionId = Messages.PURGE }.first().content._click()

            _get<Translatable<NumberField>> { captionId = Messages.REPORTS_OLDER_THAN1 }.content._value = 1.0

            _get<Translatable<Button>> { captionId = Messages.CONFIRM }.content._click()

            expectThat(reportRepository.find(oldReport)).isNull()
            expectThat(reportRepository.find(newReport)).isNotNull()
            _expectNoDialogs()
        }
    }

    @Test
    fun `should purge old reports by version`() {
        withAuth(Role.USER, Role.ADMIN) {
            val oldReport = testDataBuilder.createReport(appId, version = testDataBuilder.createVersion(appId, code = 1))
            val newReport = testDataBuilder.createReport(appId, version = testDataBuilder.createVersion(appId, code = 2))

            val card = _get<DangerCard>()
            card._find<Translatable<Button>> { captionId = Messages.PURGE }.last().content._click()

            _get<Translatable<ComboBox<Int>>> { captionId = Messages.REPORTS_BEFORE_VERSION }.content._value = 2

            _get<Translatable<Button>> { captionId = Messages.CONFIRM }.content._click()

            expectThat(reportRepository.find(oldReport)).isNull()
            expectThat(reportRepository.find(newReport)).isNotNull()
            _expectNoDialogs()
        }
    }

    @Test
    fun `should delete app`() {
        withAuth(Role.USER, Role.ADMIN) {
            val card = _get<DangerCard>()
            card._get<Translatable<Button>> { captionId = Messages.DELETE }.content._click()
            _get<Translatable<Button>> { captionId = Messages.CONFIRM }.content._click()

            expectThat(appRepository.find(appId)).isNull()
            _expectOne<Overview>()
        }
    }
}