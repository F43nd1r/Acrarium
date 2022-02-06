/*
 * (C) Copyright 2018 Lukas Morawietz (https://github.com/F43nd1r)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.faendir.acra.ui.view.user

import com.faendir.acra.i18n.Messages
import com.faendir.acra.i18n.TranslatableText
import com.faendir.acra.model.Permission
import com.faendir.acra.model.QUser
import com.faendir.acra.model.User
import com.faendir.acra.navigation.View
import com.faendir.acra.security.RequiresRole
import com.faendir.acra.security.SecurityUtils
import com.faendir.acra.service.DataService
import com.faendir.acra.service.UserService
import com.faendir.acra.ui.component.HasAcrariumTitle
import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.ui.component.dialog.ValidatedField
import com.faendir.acra.ui.component.dialog.confirmButtons
import com.faendir.acra.ui.component.dialog.createButton
import com.faendir.acra.ui.component.dialog.showFluentDialog
import com.faendir.acra.ui.component.grid.ButtonRenderer
import com.faendir.acra.ui.component.grid.column
import com.faendir.acra.ui.ext.content
import com.faendir.acra.ui.ext.forEach
import com.faendir.acra.ui.ext.queryDslAcrariumGrid
import com.faendir.acra.ui.ext.translatableText
import com.faendir.acra.ui.view.main.MainView
import com.vaadin.flow.component.Composite
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.FlexLayout
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.vaadin.flow.router.Route
import java.util.*

/**
 * @author lukas
 * @since 15.11.18
 */
@View
@Route(value = "user-manager", layout = MainView::class)
@RequiresRole(User.Role.ADMIN)
class UserManager(private val userService: UserService, private val dataService: DataService) : Composite<FlexLayout>(), HasAcrariumTitle {
    init {
        content {
            setFlexDirection(FlexLayout.FlexDirection.COLUMN)
            setSizeFull()
            queryDslAcrariumGrid(userService.getUserProvider()) {
                setWidthFull()
                setSelectionMode(Grid.SelectionMode.NONE)
                column({ it.username }) {
                    setSortable(QUser.user.username)
                    setCaption(Messages.USERNAME)
                    flexGrow = 1
                }
                column(ComponentRenderer { user: User ->
                    Checkbox(user.roles.contains(User.Role.ADMIN)).apply {
                        addValueChangeListener {
                            userService.setAdmin(user, it.value)
                            dataProvider.refreshAll()
                        }
                        isEnabled = user.username != SecurityUtils.getUsername()
                    }
                }) {
                    setCaption(Messages.ADMIN)
                }
                column(ComponentRenderer { user: User ->
                    Checkbox(user.roles.contains(User.Role.API)).apply {
                        addValueChangeListener {
                            userService.setApiAccess(user, it.value)
                            dataProvider.refreshAll()
                        }
                    }
                }) {
                    setCaption(Messages.API)
                }
                forEach(dataService.findAllApps()) { app ->
                    column(ComponentRenderer { user: User ->
                        ComboBox(null, *Permission.Level.values()).apply {
                            value = SecurityUtils.getPermission(app, user)
                            addValueChangeListener {
                                userService.setPermission(user, app, it.value)
                                dataProvider.refreshAll()
                            }
                        }
                    }) {
                        setCaption(Messages.ACCESS_PERMISSION, app.name)
                    }
                }
                column(ButtonRenderer(VaadinIcon.TRASH, { user ->
                    isEnabled = user.username != SecurityUtils.getUsername()
                }, {
                    showFluentDialog {
                        translatableText(Messages.DELETE_USER_CONFIRM, it.username)
                        confirmButtons {
                            userService.delete(it)
                            dataProvider.refreshAll()
                        }
                    }
                })) {
                    width = "50px"
                    isAutoWidth = false
                }
                appendFooterRow().getCell(columns[0]).setComponent(Translatable.createButton(Messages.NEW_USER) {
                    showFluentDialog {
                        val name = Translatable.createTextField(Messages.USERNAME)
                        val password = Translatable.createPasswordField(Messages.PASSWORD)
                        header(Messages.NEW_USER)
                        validatedField(ValidatedField.of(name).addValidator({ it.isNotEmpty() }, Messages.USERNAME_EMPTY))
                        validatedField(ValidatedField.of(password).addValidator({ it.isNotEmpty() }, Messages.PASSWORD_EMPTY))
                        validatedField(
                            ValidatedField.of(Translatable.createPasswordField(Messages.REPEAT_PASSWORD))
                                .addValidator({ it == password.content.value }, Messages.PASSWORDS_NOT_MATCHING)
                        )
                        createButton {
                            userService.createUser(name.content.value.lowercase(Locale.getDefault()), password.content.value)
                            dataProvider.refreshAll()
                        }
                    }
                })
            }
        }
    }

    override val title = TranslatableText(Messages.USER_MANAGER)
}