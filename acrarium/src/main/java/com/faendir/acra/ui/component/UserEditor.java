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

package com.faendir.acra.ui.component;

import com.faendir.acra.i18n.Messages;
import com.faendir.acra.model.User;
import com.faendir.acra.service.UserService;
import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.ElementFactory;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Arrays;

/**
 * @author lukas
 * @since 28.02.19
 */
public class UserEditor extends Composite<FlexLayout> {
    @NonNull
    private User user;

    public UserEditor(@NonNull UserService userService, @Nullable User u, Runnable onSuccess) {
        boolean newUser = u == null;
        this.user = newUser ? new User("", "", Arrays.asList(User.Role.ADMIN, User.Role.USER)) : u;
        Binder<User> binder = new Binder<>();
        Translatable.Value<TextField, String> username = Translatable.createTextField(user.getUsername(), Messages.USERNAME);
        exposeInput(username);
        username.setWidthFull();
        Binder.BindingBuilder<User, String> usernameBindingBuilder = binder.forField(username);
        if (newUser) {
            usernameBindingBuilder.asRequired(getTranslation(Messages.USERNAME_REQUIRED));
        }
        usernameBindingBuilder.withValidator(uName -> uName.equals(user.getUsername()) || userService.getUser(uName) == null, getTranslation(Messages.USERNAME_TAKEN))
                .bind(User::getUsername, newUser ? User::setUsername : null);
        getContent().add(username);
        Translatable.Value<TextField, String> mail = Translatable.createTextField(user.getMail(), Messages.EMAIL);
        mail.setWidthFull();
        EmailValidator emailValidator = new EmailValidator(getTranslation(Messages.INVALID_MAIL));
        binder.forField(mail).withValidator((m, c) -> {
            if (m.isEmpty()) {
                return ValidationResult.ok();
            }
            return emailValidator.apply(m, c);
        }).bind(User::getMail, User::setMail);
        getContent().add(mail);
        Translatable.Value<PasswordField, String> newPassword = Translatable.createPasswordField(Messages.NEW_PASSWORD);
        exposeInput(newPassword);
        newPassword.setWidthFull();
        Translatable.Value<PasswordField, String> repeatPassword = Translatable.createPasswordField(Messages.REPEAT_PASSWORD);
        exposeInput(repeatPassword);
        repeatPassword.setWidthFull();
        if (!newUser) {
            Translatable.Value<PasswordField, String> oldPassword = Translatable.createPasswordField(Messages.OLD_PASSWORD);
            exposeInput(oldPassword);
            oldPassword.setWidthFull();
            Binder.Binding<User, String> oldPasswordBinding = binder.forField(oldPassword).withValidator(p -> {
                if (!newPassword.getValue().isEmpty() || !oldPassword.getValue().isEmpty()) {
                    return userService.checkPassword(user, p);
                }
                return true;
            }, getTranslation(Messages.INCORRECT_PASSWORD))
                    .bind(user1 -> "", (user1, s) -> doNothing());
            getContent().add(oldPassword);
            newPassword.addValueChangeListener(e -> oldPasswordBinding.validate());
            repeatPassword.addValueChangeListener(e -> oldPasswordBinding.validate());
        }
        Binder.BindingBuilder<User, String> newPasswordBindingBuilder = binder.forField(newPassword);
        Binder.BindingBuilder<User, String> repeatPasswordBindingBuilder = binder.forField(repeatPassword);
        if (newUser) {
            newPasswordBindingBuilder.asRequired(getTranslation(Messages.PASSWORD_REQUIRED));
            repeatPasswordBindingBuilder.asRequired(getTranslation(Messages.PASSWORD_REQUIRED));
        }
        newPasswordBindingBuilder.bind(user1 -> "", (user2, plainTextPassword) -> {
            if(plainTextPassword != null && !"".equals(plainTextPassword)) {
                user2.setPlainTextPassword(plainTextPassword);
            }
        });
        getContent().add(newPassword);
        Binder.Binding<User, String> repeatPasswordBinding = repeatPasswordBindingBuilder.withValidator(p -> p.equals(newPassword.getValue()), getTranslation(Messages.PASSWORDS_NOT_MATCHING)).bind(user1 -> "", (user1, s) -> doNothing());
        newPassword.addValueChangeListener(e -> {
            if (!repeatPassword.getValue().isEmpty()) {
                repeatPasswordBinding.validate();
            }
        });
        getContent().add(repeatPassword);
        binder.readBean(this.user);
        Translatable<Button> button = Translatable.createButton(e -> {
            if (binder.writeBeanIfValid(user)) {
                user = userService.store(user);
                binder.readBean(user);
                onSuccess.run();
            }
        }, Messages.CONFIRM);
        button.setWidthFull();
        button.getContent().setEnabled(false);
        binder.addStatusChangeListener(e -> button.getContent().setEnabled(e.getBinder().hasChanges()));
        getContent().add(button);
        getContent().setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        getContent().setMaxWidthFull();
        getContent().setWidth("calc(var(--lumo-size-m) * 10)");
    }

    /**
     * password managers need an input outside the shadow dom, which we add here.
     * @param field the field which should be visible to password managers
     * @param <T> the type of the field
     */
    private <T extends Component & HasValue<AbstractField.ComponentValueChangeEvent<T, V>, V> & com.vaadin.flow.component.HasValidation, V> void exposeInput(Translatable.Value<T, V> field) {
        Element input = ElementFactory.createInput();
        input.setAttribute("slot", "input");
        field.getElement().appendChild(input);
    }

    private void doNothing() {
    }
}
