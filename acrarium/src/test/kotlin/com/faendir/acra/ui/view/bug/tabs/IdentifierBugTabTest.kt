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
package com.faendir.acra.ui.view.bug.tabs

import com.faendir.acra.common.UiParams
import com.faendir.acra.common.UiTest
import com.faendir.acra.common.captionId
import com.faendir.acra.i18n.Messages
import com.faendir.acra.persistence.TestDataBuilder
import com.faendir.acra.persistence.bug.BugRepository
import com.faendir.acra.persistence.user.Permission
import com.faendir.acra.persistence.user.Role
import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.ui.view.bug.BugView
import com.github.mvysny.kaributesting.v10._click
import com.github.mvysny.kaributesting.v10._expectNone
import com.github.mvysny.kaributesting.v10._find
import com.vaadin.flow.component.button.Button
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import strikt.api.expectThat
import strikt.assertions.hasSize

class IdentifierBugTabTest(
    @Autowired private val testDataBuilder: TestDataBuilder,
    @Autowired private val bugRepository: BugRepository,
) : UiTest() {
    private val appId = testDataBuilder.createApp()
    private val bugId = testDataBuilder.createBug(appId)

    init {
        testDataBuilder.createReport(appId, bugId)
        testDataBuilder.createReport(appId, bugId)
    }

    override fun setup() = UiParams(
        route = IdentifierBugTab::class,
        routeParameters = BugView.getNavigationParams(appId, bugId),
        requiredAuthorities = setOf(Role.USER, Permission(appId, Permission.Level.VIEW))
    )

    @Test
    fun `should not show unlink button without edit permission`() {
        _expectNone<Translatable<Button>> { captionId = Messages.NOT_SAME_BUG }
    }

    @Test
    fun `should be able to unlink bug`() {
        withAuth(Permission(appId, Permission.Level.EDIT)) {
            expectThat(bugRepository.getIdentifiers(bugId)).hasSize(2)

            _find<Translatable<Button>> { captionId = Messages.NOT_SAME_BUG }.last().content._click()

            expectThat(bugRepository.getIdentifiers(bugId)).hasSize(1)
        }
    }
}