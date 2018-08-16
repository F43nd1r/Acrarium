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

package com.faendir.acra.ui.view.bug.tabs.panels;

import com.faendir.acra.i18n.I18nButton;
import com.faendir.acra.i18n.I18nTextArea;
import com.faendir.acra.i18n.Messages;
import com.faendir.acra.model.Bug;
import com.faendir.acra.service.DataService;
import com.faendir.acra.ui.navigation.NavigationManager;
import com.vaadin.server.Sizeable;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.vaadin.spring.i18n.I18N;

/**
 * @author lukas
 * @since 19.06.18
 */
@SpringComponent
@ViewScope
public class PropertiesPanel implements AdminPanel {
    @NonNull private final DataService dataService;
    @NonNull private final I18N i18n;

    @Autowired
    public PropertiesPanel(@NonNull DataService dataService, @NonNull I18N i18n) {
        this.dataService = dataService;
        this.i18n = i18n;
    }

    @Override
    public Component createContent(@NonNull Bug bug, @NonNull NavigationManager navigationManager) {
        TextArea title = new I18nTextArea(i18n, Messages.TITLE);
        title.setValue(bug.getTitle());
        title.setSizeFull();
        Button save = new I18nButton(e -> {
            bug.setTitle(title.getValue());
            dataService.store(bug);
        }, i18n, Messages.SAVE);
        save.setWidth(100, Sizeable.Unit.PERCENTAGE);
        return new VerticalLayout(title, save);
    }

    @Override
    public String getCaption() {
        return i18n.get(Messages.PROPERTIES);
    }

    @Override
    public String getId() {
        return "properties";
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
