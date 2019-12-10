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

package com.faendir.acra.ui.view.app.tabs.admincards;

import com.faendir.acra.i18n.Messages;
import com.faendir.acra.model.App;
import com.faendir.acra.model.MailSettings;
import com.faendir.acra.model.User;
import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.service.DataService;
import com.faendir.acra.service.UserService;
import com.faendir.acra.ui.component.Card;
import com.faendir.acra.ui.component.CssGrid;
import com.faendir.acra.ui.component.Translatable;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;

@UIScope
@SpringComponent
public class NotificationCard extends AdminCard {
    @org.springframework.lang.NonNull
    private final UserService userService;

    public NotificationCard(UserService userService, DataService dataService) {
        super(dataService);
        this.userService = userService;
    }

    @Override
    public void init(App app) {
        CssGrid notificationLayout = new CssGrid();
        notificationLayout.setTemplateColumns("auto max-content");
        notificationLayout.setWidthFull();
        User user = userService.getUser(SecurityUtils.getUsername());
        MailSettings settings = getDataService().findMailSettings(app, user).orElse(new MailSettings(app, user));
        notificationLayout.add(Translatable.createLabel(Messages.NEW_BUG_MAIL_LABEL), new Checkbox("", event -> {
            settings.setNewBug(event.getValue());
            getDataService().store(settings);
        }));
        notificationLayout.add(Translatable.createLabel(Messages.REGRESSION_MAIL_LABEL), new Checkbox("", event -> {
            settings.setRegression(event.getValue());
            getDataService().store(settings);
        }));
        notificationLayout.add(Translatable.createLabel(Messages.SPIKE_MAIL_LABEL), new Checkbox("", event -> {
            settings.setSpike(event.getValue());
            getDataService().store(settings);
        }));
        notificationLayout.add(Translatable.createLabel(Messages.WEEKLY_MAIL_LABEL), new Checkbox("", event -> {
            settings.setSummary(event.getValue());
            getDataService().store(settings);
        }));
        if (user.getMail() == null) {
            Icon icon = VaadinIcon.WARNING.create();
            icon.getStyle().set("height", "var(--lumo-font-size-m)");
            Div div = new Div(icon, Translatable.createText(Messages.NO_MAIL_SET));
            div.getStyle().set("color", "var(--lumo-error-color)");
            div.getStyle().set("font-style", "italic");
            notificationLayout.add(div);
        }
        setHeader(Translatable.createLabel(Messages.NOTIFICATIONS));
        add(notificationLayout);
    }
}
