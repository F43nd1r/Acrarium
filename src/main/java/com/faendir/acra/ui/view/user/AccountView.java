/*
 * (C) Copyright 2019 Lukas Morawietz (https://github.com/F43nd1r)
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
import com.faendir.acra.model.User;
import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.service.UserService;
import com.faendir.acra.ui.base.HasRoute;
import com.faendir.acra.ui.base.Path;
import com.faendir.acra.ui.component.FlexLayout;
import com.faendir.acra.ui.component.Translatable;
import com.faendir.acra.ui.view.MainView;
import com.faendir.acra.ui.view.Overview;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

import java.util.Objects;

/**
 * @author lukas
 * @since 26.02.19
 */
@UIScope
@SpringComponent
@Route(value = "account", layout = MainView.class)
public class AccountView extends Composite<FlexLayout> implements HasRoute {
    @NonNull
    private final UserService userService;

    @Autowired
    public AccountView(@NonNull UserService userService) {
        this.userService = userService;
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        getContent().removeAll();
        User user = userService.getUser(SecurityUtils.getUsername());
        assert user != null;
        Translatable<TextField> username = Translatable.createTextField(user.getUsername(), Messages.USERNAME);
        username.getContent().setEnabled(false);
        Translatable<TextField> email = Translatable.createTextField(user.getMail(), Messages.EMAIL);
        Translatable<PasswordField> oldPassword = Translatable.createPasswordField(Messages.OLD_PASSWORD);
        getContent().add(oldPassword);
        Translatable<PasswordField> newPassword = Translatable.createPasswordField(Messages.NEW_PASSWORD);
        getContent().add(newPassword);
        Translatable<PasswordField> repeatPassword = Translatable.createPasswordField(Messages.REPEAT_PASSWORD);
        getContent().add(repeatPassword);
        FlexLayout layout = new FlexLayout(username, email, oldPassword, newPassword, repeatPassword, Translatable.createButton(e -> {
            boolean success = true;
            String mail = email.getContent().getValue();
            if(mail.isEmpty()) mail = null;
            if(!Objects.equals(mail, user.getMail())) {
                if(!userService.changeMail(user, mail)) {
                    success = false;
                    email.getContent().setErrorMessage(getTranslation(Messages.INVALID_MAIL));
                    email.getContent().setInvalid(true);
                    email.getContent().addValueChangeListener(event -> {
                        email.getContent().setInvalid(false);
                        event.unregisterListener();
                    });
                }
            }
            if (!oldPassword.getContent().getValue().isEmpty()) {
                if (newPassword.getContent().getValue().equals(repeatPassword.getContent().getValue())) {
                    if (!userService.changePassword(user, oldPassword.getContent().getValue(), newPassword.getContent().getValue())) {
                        success = false;
                        oldPassword.getContent().setErrorMessage(getTranslation(Messages.INCORRECT_PASSWORD));
                        oldPassword.getContent().setInvalid(true);
                        oldPassword.getContent().addValueChangeListener(event -> {
                            oldPassword.getContent().setInvalid(false);
                            event.unregisterListener();
                        });
                    }
                } else {
                    success = false;
                    repeatPassword.getContent().setErrorMessage(getTranslation(Messages.PASSWORDS_NOT_MATCHING));
                    repeatPassword.getContent().setInvalid(true);
                    repeatPassword.getContent().addValueChangeListener(event -> {
                        repeatPassword.getContent().setInvalid(false);
                        event.unregisterListener();
                    });
                }
            }
            if(success) {
                Notification.show(getTranslation(Messages.SUCCESS));
            }
        }, Messages.CONFIRM));
        layout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        getContent().add(layout);
        getContent().setSizeFull();
        getContent().setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        getContent().setAlignItems(FlexComponent.Alignment.CENTER);
    }

    @NonNull
    @Override
    public Path.Element<?> getPathElement() {
        return new Path.Element<>(getClass(), Messages.ACCOUNT);
    }

    @Override
    public Parent<?> getLogicalParent() {
        return new Parent<>(Overview.class);
    }
}
