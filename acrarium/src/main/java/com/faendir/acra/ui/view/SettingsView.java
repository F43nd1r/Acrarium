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

package com.faendir.acra.ui.view;

import com.faendir.acra.i18n.Messages;
import com.faendir.acra.ui.base.HasAcrariumTitle;
import com.faendir.acra.ui.base.TranslatableText;
import com.faendir.acra.ui.component.FlexLayout;
import com.faendir.acra.ui.component.Translatable;
import com.faendir.acra.util.LocalSettings;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import com.vaadin.flow.theme.lumo.Lumo;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author lukas
 * @since 10.09.19
 */
@UIScope
@SpringComponent
@Route(value = "settings", layout = MainView.class)
public class SettingsView extends FlexLayout implements HasAcrariumTitle {
    @Autowired
    public SettingsView(LocalSettings localSettings) {
        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);
        FormLayout layout = new FormLayout();
        layout.setResponsiveSteps(new FormLayout.ResponsiveStep("0px", 1));
        layout.getStyle().set("align-self", "auto");
        layout.add(Translatable.createCheckbox(localSettings.getDarkTheme(), Messages.DARK_THEME).with(checkbox -> checkbox.addValueChangeListener(e -> {
            localSettings.setDarkTheme(e.getValue());
            VaadinSession.getCurrent().getUIs().forEach(ui -> ui.getElement().setAttribute("theme", e.getValue() ? Lumo.DARK : Lumo.LIGHT));
        })), Translatable.createSelect(LocalSettings.getI18NProvider().getProvidedLocales(), Messages.LOCALE).with(select -> {
            select.setItemLabelGenerator(locale -> locale.getDisplayName(localSettings.getLocale()));
            select.setValue(localSettings.getLocale());
            select.addValueChangeListener(e -> {
                localSettings.setLocale(e.getValue());
                VaadinSession.getCurrent().setLocale(e.getValue());
            });
        }));
        add(layout);
    }

    @Override
    public TranslatableText getTitle() {
        return new TranslatableText(Messages.SETTINGS);
    }
}
