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

package com.faendir.acra.ui.view.user;

import com.faendir.acra.i18n.Messages;
import com.faendir.acra.model.App;
import com.faendir.acra.model.Permission;
import com.faendir.acra.model.QUser;
import com.faendir.acra.model.User;
import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.service.DataService;
import com.faendir.acra.service.UserService;
import com.faendir.acra.ui.base.HasRoute;
import com.faendir.acra.ui.base.MyGrid;
import com.faendir.acra.ui.base.Path;
import com.faendir.acra.ui.base.popup.Popup;
import com.faendir.acra.ui.base.popup.ValidatedField;
import com.faendir.acra.ui.component.FlexLayout;
import com.faendir.acra.ui.component.Translatable;
import com.faendir.acra.ui.view.MainView;
import com.faendir.acra.ui.view.Overview;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.lang.NonNull;

import java.util.Arrays;

/**
 * @author lukas
 * @since 15.11.18
 */
@UIScope
@SpringComponent
@Route(value = "user-manager", layout = MainView.class)
public class UserManager extends Composite<FlexLayout> implements HasRoute {
    private final UserService userService;
    private final DataService dataService;

    public UserManager(@NonNull UserService userService, @NonNull DataService dataService) {
        this.userService = userService;
        this.dataService = dataService;
        getContent().setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        getContent().setWidthFull();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        getContent().removeAll();
        getContent().add(Translatable.createLabel(Messages.USERS));
        MyGrid<User> userGrid = new MyGrid<>(userService.getUserProvider());
        userGrid.setWidthFull();
        userGrid.setSelectionMode(Grid.SelectionMode.NONE);
        userGrid.addColumn(User::getUsername, QUser.user.username, Messages.USERNAME).setFlexGrow(1);
        userGrid.addColumn(new ComponentRenderer<>(user -> {
            Checkbox checkbox = new Checkbox(user.getRoles().contains(User.Role.ADMIN));
            checkbox.addValueChangeListener(e -> {
                userService.setAdmin(user, e.getValue());
                userGrid.getDataProvider().refreshAll();
            });
            checkbox.setEnabled(!user.getUsername().equals(SecurityUtils.getUsername()));
            return checkbox;
        }), Messages.ADMIN);
        userGrid.addColumn(new ComponentRenderer<>(user -> {
            Checkbox checkbox = new Checkbox(user.getRoles().contains(User.Role.API));
            checkbox.addValueChangeListener(e -> {
                userService.setApiAccess(user, e.getValue());
                userGrid.getDataProvider().refreshAll();
            });
            return checkbox;
        }), Messages.API);
        for (App app : dataService.findAllApps()) {
            userGrid.addColumn(new ComponentRenderer<>(user -> {
                Permission.Level permission = SecurityUtils.getPermission(app, user);
                ComboBox<Permission.Level> levelComboBox = new ComboBox<>(null, Arrays.asList(Permission.Level.values()));
                levelComboBox.setValue(permission);
                levelComboBox.addValueChangeListener(e -> {
                    userService.setPermission(user, app, e.getValue());
                    userGrid.getDataProvider().refreshAll();
                });
                return levelComboBox;
            }), Messages.ACCESS_PERMISSION, app.getName());
        }
        Translatable<Button> newUser = Translatable.createButton(e -> {
            Translatable<TextField> name = Translatable.createTextField("", Messages.USERNAME);
            Translatable<PasswordField> password = Translatable.createPasswordField(Messages.PASSWORD);
            new Popup().setTitle(Messages.NEW_USER)
                    .addValidatedField(ValidatedField.of(name).addValidator(s -> !s.isEmpty(), Messages.USERNAME_EMPTY))
                    .addValidatedField(ValidatedField.of(password).addValidator(s -> !s.isEmpty(), Messages.PASSWORD_EMPTY))
                    .addValidatedField(ValidatedField.of(Translatable.createPasswordField(Messages.REPEAT_PASSWORD)).addValidator(s -> s.equals(password.getContent().getValue()), Messages.PASSWORDS_NOT_MATCHING))
                    .addCreateButton(popup -> {
                        userService.createUser(name.getContent().getValue().toLowerCase(), password.getContent().getValue());
                        userGrid.getDataProvider().refreshAll();
                    }, true)
                    .show();
        }, Messages.NEW_USER);
        userGrid.appendFooterRow().getCell(userGrid.getColumns().get(0)).setComponent(newUser);
        getContent().add(userGrid);
    }

    @NonNull
    @Override
    public Path.Element<?> getPathElement() {
        return new Path.Element<>(getClass(), Messages.USER_MANAGER);
    }

    @Override
    public Parent<?> getLogicalParent() {
        return new Parent<>(Overview.class);
    }
}
