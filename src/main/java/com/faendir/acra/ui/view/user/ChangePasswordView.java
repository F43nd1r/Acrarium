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
import com.faendir.acra.i18n.Messages;
import com.faendir.acra.model.User;
import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.service.UserService;
import com.faendir.acra.ui.BackendUI;
import com.faendir.acra.ui.navigation.SingleViewProvider;
import com.faendir.acra.ui.view.base.navigation.BaseView;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.server.UserError;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.VerticalLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.vaadin.spring.i18n.I18N;

/**
 * @author Lukas
 * @since 21.05.2017
 */
@SpringComponent
@ViewScope
public class ChangePasswordView extends BaseView {
    @NonNull private final UserService userService;
    @NonNull private final BackendUI backendUI;
    @NonNull private final I18N i18n;

    @Autowired
    public ChangePasswordView(@NonNull UserService userService, @NonNull BackendUI backendUI, @NonNull I18N i18n) {
        this.userService = userService;
        this.backendUI = backendUI;
        this.i18n = i18n;
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        PasswordField oldPassword = new I18nPasswordField(i18n, Messages.OLD_PASSWORD);
        PasswordField newPassword = new I18nPasswordField(i18n, Messages.NEW_PASSWORD);
        PasswordField repeatPassword = new I18nPasswordField(i18n, Messages.REPEAT_PASSWORD);
        Button confirm = new I18nButton(e -> {
            User user = userService.getUser(SecurityUtils.getUsername());
            assert user != null;
            if (newPassword.getValue().equals(repeatPassword.getValue())) {
                if (userService.changePassword(user, oldPassword.getValue(), newPassword.getValue())) {
                    Notification.show(i18n.get(Messages.SUCCESS));
                    getNavigationManager().navigateBack();
                    backendUI.logout();
                } else {
                    oldPassword.setComponentError(new UserError(i18n.get(Messages.INCORRECT_PASSWORD)));
                }
            } else {
                repeatPassword.setComponentError(new UserError(i18n.get(Messages.PASSWORDS_NOT_MATCHING)));
            }
        }, i18n, Messages.CONFIRM);
        confirm.setSizeFull();
        VerticalLayout layout = new VerticalLayout(oldPassword, newPassword, repeatPassword, confirm);
        layout.setSizeUndefined();
        VerticalLayout root = new VerticalLayout(layout);
        root.setSizeFull();
        root.setComponentAlignment(layout, Alignment.MIDDLE_CENTER);
        setCompositionRoot(root);
    }

    @SpringComponent
    @UIScope
    public static class Provider extends SingleViewProvider<ChangePasswordView> {
        @NonNull private final I18N i18n;

        protected Provider(@NonNull I18N i18n) {
            super(ChangePasswordView.class);
            this.i18n = i18n;
        }

        @Override
        public String getTitle(String parameter) {
            return i18n.get(Messages.CHANGE_PASSWORD);
        }

        @Override
        public String getId() {
            return "password-editor";
        }
    }
}
