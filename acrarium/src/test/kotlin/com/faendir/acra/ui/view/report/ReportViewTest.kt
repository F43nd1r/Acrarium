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
package com.faendir.acra.ui.view.report

import com.faendir.acra.common.UiParams
import com.faendir.acra.common.UiTest
import com.faendir.acra.persistence.TestDataBuilder
import com.faendir.acra.persistence.app.AppId
import com.faendir.acra.persistence.bug.BugId
import com.faendir.acra.persistence.user.Permission
import com.faendir.acra.persistence.user.Role
import org.springframework.beans.factory.annotation.Autowired

class ReportViewTest(
    @Autowired private val testDataBuilder: TestDataBuilder,
) : UiTest() {
    private val appId: AppId = testDataBuilder.createApp()
    private val bugId: BugId = testDataBuilder.createBug(appId)
    private val reportId: String = testDataBuilder.createReport(appId, bugId)

    override fun setup() = UiParams(
        route = ReportView::class,
        routeParameters = ReportView.getNavigationParams(appId, bugId, reportId),
        requiredAuthorities = setOf(Role.USER, Permission(appId, Permission.Level.VIEW))
    )
}