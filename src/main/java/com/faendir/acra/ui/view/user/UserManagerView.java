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

import com.faendir.acra.i18n.I18nButton;
import com.faendir.acra.i18n.I18nPasswordField;
import com.faendir.acra.i18n.I18nTextField;
import com.faendir.acra.i18n.Messages;
import com.faendir.acra.model.App;
import com.faendir.acra.model.Permission;
import com.faendir.acra.model.QUser;
import com.faendir.acra.model.User;
import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.service.DataService;
import com.faendir.acra.service.UserService;
import com.faendir.acra.ui.annotation.RequiresRole;
import com.faendir.acra.ui.navigation.SingleViewProvider;
import com.faendir.acra.ui.view.base.MyCheckBox;
import com.faendir.acra.ui.view.base.layout.MyGrid;
import com.faendir.acra.ui.view.base.navigation.BaseView;
import com.faendir.acra.ui.view.base.popup.Popup;
import com.faendir.acra.ui.view.base.popup.ValidatedField;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Grid;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.renderers.ComponentRenderer;
import com.vaadin.ui.themes.AcraTheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.vaadin.spring.i18n.I18N;

import java.util.Arrays;

/**
 * @author Lukas
 * @since 20.05.2017
 */
@SpringComponent
@ViewScope
@RequiresRole(User.Role.ADMIN)
public class UserManagerView extends BaseView {
    @NonNull private final UserService userService;
    @NonNull private final DataService dataService;
    @NonNull private final I18N i18n;
    private MyGrid<User> userGrid;

    @Autowired
    public UserManagerView(@NonNull UserService userService, @NonNull DataService dataService, @NonNull I18N i18n) {
        this.userService = userService;
        this.dataService = dataService;
        this.i18n = i18n;
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        userGrid = new MyGrid<>(userService.getUserProvider(), i18n, Messages.USERS);
        userGrid.setSelectionMode(Grid.SelectionMode.NONE);
        userGrid.setBodyRowHeight(42);
        userGrid.setSizeToRows();
        userGrid.addColumn(User::getUsername, QUser.user.username, Messages.USERNAME);
        userGrid.addColumn(user -> new MyCheckBox(user.getRoles().contains(User.Role.ADMIN), !user.getUsername().equals(SecurityUtils.getUsername()), e -> {
            userService.setAdmin(user, e.getValue());
            userGrid.getDataProvider().refreshAll();
        }), new ComponentRenderer(), Messages.ADMIN);
        userGrid.addColumn(user -> new MyCheckBox(user.getRoles().contains(User.Role.API), !user.getUsername().equals(SecurityUtils.getUsername()), e -> {
            userService.setApiAccess(user, e.getValue());
            userGrid.getDataProvider().refreshAll();
        }), new ComponentRenderer(), Messages.API);
        for (App app : dataService.findAllApps()) {
            userGrid.addColumn(user -> {
                Permission.Level permission = SecurityUtils.getPermission(app, user);
                ComboBox<Permission.Level> levelComboBox = new ComboBox<>(null, Arrays.asList(Permission.Level.values()));
                levelComboBox.setEmptySelectionAllowed(false);
                levelComboBox.setValue(permission);
                levelComboBox.addValueChangeListener(e -> userService.setPermission(user, app, e.getValue()));
                return levelComboBox;
            }, new ComponentRenderer(), Messages.ACCESS_PERMISSION, app.getName());
        }
        Button newUser = new I18nButton( e -> newUser(), i18n, Messages.NEW_USER);
        VerticalLayout layout = new VerticalLayout(userGrid, newUser);
        layout.addStyleName(AcraTheme.NO_PADDING);
        setCompositionRoot(layout);
        addStyleNames(AcraTheme.PADDING_LEFT, AcraTheme.PADDING_RIGHT, AcraTheme.PADDING_BOTTOM);
    }

    private void newUser() {
        I18nTextField name = new I18nTextField(i18n, Messages.USERNAME);
        I18nPasswordField password = new I18nPasswordField(i18n, Messages.PASSWORD);
        new Popup(i18n, Messages.NEW_USER)
                .addValidatedField(ValidatedField.of(name).addValidator(s -> !s.isEmpty(), Messages.USERNAME_EMPTY))
                .addValidatedField(ValidatedField.of(password).addValidator(s -> !s.isEmpty(), Messages.PASSWORD_EMPTY))
                .addValidatedField(ValidatedField.of(new I18nPasswordField(i18n, Messages.REPEAT_PASSWORD)).addValidator(s -> s.equals(password.getValue()), Messages.PASSWORDS_NOT_MATCHING))
                .addCreateButton(popup -> {
                    userService.createUser(name.getValue().toLowerCase(), password.getValue());
                    userGrid.getDataProvider().refreshAll();
                }, true)
                .show();
    }

    @SpringComponent
    @UIScope
    public static class Provider extends SingleViewProvider<UserManagerView> {
        @NonNull private final I18N i18n;

        protected Provider(@NonNull I18N i18n) {
            super(UserManagerView.class);
            this.i18n = i18n;
        }

        @Override
        public String getTitle(String parameter) {
            return i18n.get(Messages.USER_MANAGER);
        }

        @Override
        public String getId() {
            return "user-manager";
        }
    }
}
