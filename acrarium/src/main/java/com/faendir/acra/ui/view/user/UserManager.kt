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
import com.faendir.acra.model.Permission
import com.faendir.acra.model.QUser
import com.faendir.acra.model.User
import com.faendir.acra.security.SecurityUtils
import com.faendir.acra.service.DataService
import com.faendir.acra.service.UserService
import com.faendir.acra.ui.base.HasAcrariumTitle
import com.faendir.acra.ui.base.TranslatableText
import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.ui.component.dialog.FluentDialog
import com.faendir.acra.ui.component.dialog.ValidatedField
import com.faendir.acra.ui.component.grid.AcrariumGrid
import com.faendir.acra.ui.ext.FlexDirection
import com.faendir.acra.ui.ext.setFlexDirection
import com.faendir.acra.ui.view.MainView
import com.vaadin.flow.component.AttachEvent
import com.vaadin.flow.component.Composite
import com.vaadin.flow.component.checkbox.Checkbox
import com.vaadin.flow.component.combobox.ComboBox
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.orderedlayout.FlexLayout
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.vaadin.flow.router.Route
import com.vaadin.flow.spring.annotation.SpringComponent
import com.vaadin.flow.spring.annotation.UIScope

/**
 * @author lukas
 * @since 15.11.18
 */
@UIScope
@SpringComponent
@Route(value = "user-manager", layout = MainView::class)
class UserManager(private val userService: UserService, private val dataService: DataService) : Composite<FlexLayout>(), HasAcrariumTitle {

    init {
        content.setFlexDirection(FlexDirection.COLUMN)
        content.setSizeFull()
    }

    override fun onAttach(attachEvent: AttachEvent) {
        super.onAttach(attachEvent)
        content.removeAll()
        val userGrid = AcrariumGrid(userService.userProvider)
        userGrid.setWidthFull()
        userGrid.setSelectionMode(Grid.SelectionMode.NONE)
        userGrid.addColumn { it.username }.setSortable(QUser.user.username).setCaption(Messages.USERNAME).flexGrow = 1
        userGrid.addColumn(ComponentRenderer { user: User ->
            Checkbox(user.roles.contains(User.Role.ADMIN)).apply {
                addValueChangeListener {
                    userService.setAdmin(user, it.value)
                    userGrid.dataProvider.refreshAll()
                }
                isEnabled = user.username != SecurityUtils.username
            }
        }).setCaption(Messages.ADMIN)
        userGrid.addColumn(ComponentRenderer { user: User ->
            Checkbox(user.roles.contains(User.Role.API)).apply {
                addValueChangeListener {
                    userService.setApiAccess(user, it.value)
                    userGrid.dataProvider.refreshAll()
                }
            }
        }).setCaption(Messages.API)
        for (app in dataService.findAllApps()) {
            userGrid.addColumn(ComponentRenderer { user: User ->
                ComboBox(null, *Permission.Level.values()).apply {
                    value = SecurityUtils.getPermission(app, user)
                    addValueChangeListener {
                        userService.setPermission(user, app, it.value)
                        userGrid.dataProvider.refreshAll()
                    }
                }
            }).setCaption(Messages.ACCESS_PERMISSION, app.name)
        }
        val newUser = Translatable.createButton(Messages.NEW_USER) {
            val name = Translatable.createTextField(Messages.USERNAME)
            val password = Translatable.createPasswordField(Messages.PASSWORD)
            FluentDialog().setTitle(Messages.NEW_USER)
                    .addValidatedField(ValidatedField.of(name).addValidator({ it.isNotEmpty() }, Messages.USERNAME_EMPTY))
                    .addValidatedField(ValidatedField.of(password).addValidator({ it.isNotEmpty() }, Messages.PASSWORD_EMPTY))
                    .addValidatedField(ValidatedField.of(Translatable.createPasswordField(Messages.REPEAT_PASSWORD))
                            .addValidator({ it == password.content.value }, Messages.PASSWORDS_NOT_MATCHING))
                    .addCreateButton {
                        userService.createUser(name.content.value.toLowerCase(), password.content.value)
                        userGrid.dataProvider.refreshAll()
                    }
                    .show()
        }
        userGrid.appendFooterRow().getCell(userGrid.columns[0]).setComponent(newUser)
        content.add(userGrid)
    }

    override fun getTitle(): TranslatableText = TranslatableText(Messages.USER_MANAGER)
}