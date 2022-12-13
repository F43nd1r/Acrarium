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
import com.faendir.acra.navigation.View
import com.faendir.acra.persistence.app.AppRepository
import com.faendir.acra.persistence.user.Permission
import com.faendir.acra.persistence.user.Role
import com.faendir.acra.persistence.user.UserAuthorities
import com.faendir.acra.persistence.user.UserRepository
import com.faendir.acra.security.RequiresRole
import com.faendir.acra.security.SecurityUtils
import com.faendir.acra.ui.component.HasAcrariumTitle
import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.ui.component.dialog.ValidatedField
import com.faendir.acra.ui.component.dialog.confirmButtons
import com.faendir.acra.ui.component.dialog.createButton
import com.faendir.acra.ui.component.dialog.showFluentDialog
import com.faendir.acra.ui.component.grid.column
import com.faendir.acra.ui.component.grid.renderer.ButtonRenderer
import com.faendir.acra.ui.ext.basicLayoutPersistingFilterableGrid
import com.faendir.acra.ui.ext.content
import com.faendir.acra.ui.ext.forEach
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

/**
 * @author lukas
 * @since 15.11.18
 */
@View
@Route(value = "user-manager", layout = MainView::class)
@RequiresRole(Role.ADMIN)
class UserManager(private val userRepository: UserRepository, private val appRepository: AppRepository) : Composite<FlexLayout>(), HasAcrariumTitle {
    init {
        content {
            flexDirection = FlexLayout.FlexDirection.COLUMN
            setSizeFull()
            basicLayoutPersistingFilterableGrid(userRepository.getProvider()) {
                setWidthFull()
                setSelectionMode(Grid.SelectionMode.NONE)
                column({ it.username }) {
                    setSortable(UserAuthorities.Sort.USERNAME)
                    setCaption(Messages.USERNAME)
                    flexGrow = 1
                }
                column(ComponentRenderer { user: UserAuthorities ->
                    Checkbox(user.roles.contains(Role.ADMIN)).apply {
                        addValueChangeListener {
                            userRepository.setRole(user.username, Role.ADMIN, it.value)
                            dataProvider.refreshAll()
                        }
                        isEnabled = user.username != SecurityUtils.getUsername()
                    }
                }) {
                    setCaption(Messages.ADMIN)
                }
                column(ComponentRenderer { user: UserAuthorities ->
                    Checkbox(user.roles.contains(Role.API)).apply {
                        addValueChangeListener {
                            userRepository.setRole(user.username, Role.API, it.value)
                            dataProvider.refreshAll()
                        }
                    }
                }) {
                    setCaption(Messages.API)
                }
                forEach(appRepository.getAllNames()) { app ->
                    column(ComponentRenderer { user: UserAuthorities ->
                        ComboBox(null, *Permission.Level.values()).apply {
                            value = user.getPermissionLevel(app.id)
                            addValueChangeListener {
                                userRepository.setPermission(user.username, app.id, it.value)
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
                            userRepository.delete(it.username)
                            dataProvider.refreshAll()
                        }
                    }
                })) {
                    width = "50px"
                    isAutoWidth = false
                }
                appendFooterRow().getCell(columns[0]).component = Translatable.createButton(Messages.NEW_USER) {
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
                            userRepository.create(name.content.value, password.content.value, null)
                            dataProvider.refreshAll()
                        }
                    }
                }
            }
        }
    }

    override val title = TranslatableText(Messages.USER_MANAGER)
}