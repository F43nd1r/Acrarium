/*
 * (C) Copyright 2020-2023 Lukas Morawietz (https://github.com/F43nd1r)
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

import com.faendir.acra.i18n.Messages
import com.faendir.acra.navigation.RouteParams
import com.faendir.acra.navigation.View
import com.faendir.acra.persistence.bug.BugRepository
import com.faendir.acra.persistence.user.Permission
import com.faendir.acra.security.SecurityUtils
import com.faendir.acra.ui.component.Card
import com.faendir.acra.ui.ext.*
import com.faendir.acra.ui.view.bug.BugView
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.FlexLayout
import com.vaadin.flow.router.Route

/**
 * @author lukas
 * @since 19.11.18
 */
@View
@Route(value = "identifier", layout = BugView::class)
class IdentifierBugTab(
    private val bugRepository: BugRepository,
    routeParams: RouteParams,
) : Div() {
    private val appId = routeParams.appId()
    private val bugId = routeParams.bugId()

    init {
        setWidthFull()
        style["overflow"] = "auto"
        val identifiers = bugRepository.getIdentifiers(bugId).toMutableList()
        forEach(identifiers) { identifier ->
            card {
                allowCollapse = true
                setHeader(Span(identifier.exceptionClass + ": " + identifier.message))
                this@IdentifierBugTab.children.findAny().ifPresent { isCollapsed = true }
                flexLayout {
                    flexDirection = FlexLayout.FlexDirection.COLUMN
                    translatableSpan(Messages.IDENTIFER_BODY, identifier.crashLine ?: "", identifier.cause ?: "")
                    if (identifiers.size > 1 && SecurityUtils.hasPermission(appId, Permission.Level.EDIT)) {
                        translatableButton(Messages.NOT_SAME_BUG) {
                            bugRepository.splitFromBug(bugId, identifier)
                            this@IdentifierBugTab.remove(this@card)
                            identifiers.remove(identifier)
                            if (identifiers.size == 1) {
                                this@IdentifierBugTab.children.forEach { card ->
                                    card.children.filter { it is Button }.forEach { (card as Card).remove(it) }
                                }
                            }
                        }.with {
                            icon = Icon(VaadinIcon.UNLINK)
                            removeThemeVariants(ButtonVariant.LUMO_PRIMARY)
                            addThemeVariants(ButtonVariant.LUMO_TERTIARY)
                            setAlignSelf(Align.END)
                        }
                    }
                }
            }
        }
    }
}