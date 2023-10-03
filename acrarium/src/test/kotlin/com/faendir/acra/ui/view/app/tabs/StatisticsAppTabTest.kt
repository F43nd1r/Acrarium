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
package com.faendir.acra.ui.view.app.tabs

import com.faendir.acra.common.UiParams
import com.faendir.acra.common.UiTest
import com.faendir.acra.persistence.TestDataBuilder
import com.faendir.acra.persistence.user.Permission
import com.faendir.acra.persistence.user.Role
import com.faendir.acra.ui.view.app.AppView
import com.github.appreciated.apexcharts.ApexCharts
import com.github.mvysny.kaributesting.v10._find
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import strikt.api.expectThat
import strikt.assertions.hasSize

class StatisticsAppTabTest(
    @Autowired private val testDataBuilder: TestDataBuilder,
) : UiTest() {
    private val appId = testDataBuilder.createApp()

    override fun setup() = UiParams(
        route = StatisticsAppTab::class,
        routeParameters = AppView.getNavigationParams(appId),
        requiredAuthorities = setOf(Role.USER, Permission(appId, Permission.Level.VIEW))
    )

    @Test
    fun `should show statistics`() {
        val charts = _find<ApexCharts>()

        expectThat(charts).hasSize(5)
    }
}