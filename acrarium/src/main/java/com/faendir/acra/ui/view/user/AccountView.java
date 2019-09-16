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
import com.faendir.acra.ui.base.HasAcrariumTitle;
import com.faendir.acra.ui.base.TranslatableText;
import com.faendir.acra.ui.component.FlexLayout;
import com.faendir.acra.ui.component.UserEditor;
import com.faendir.acra.ui.view.MainView;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

/**
 * @author lukas
 * @since 26.02.19
 */
@UIScope
@SpringComponent
@Route(value = "account", layout = MainView.class)
public class AccountView extends Composite<FlexLayout> implements HasAcrariumTitle {
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
        UserEditor userEditor = new UserEditor(userService, user, () -> Notification.show(getTranslation(Messages.SUCCESS)));
        getContent().add(userEditor);
        getContent().setSizeFull();
        getContent().setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        getContent().setAlignItems(FlexComponent.Alignment.CENTER);
    }

    @Override
    public TranslatableText getTitle() {
        return new TranslatableText(Messages.ACCOUNT);
    }
}
