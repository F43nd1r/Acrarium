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
import com.faendir.acra.i18n.I18nLabel;
import com.faendir.acra.i18n.Messages;
import com.faendir.acra.model.Bug;
import com.faendir.acra.service.DataService;
import com.faendir.acra.ui.navigation.NavigationManager;
import com.faendir.acra.ui.view.base.popup.Popup;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Resource;
import com.vaadin.server.Sizeable;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.AcraTheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.vaadin.spring.i18n.I18N;

/**
 * @author lukas
 * @since 19.06.18
 */
@SpringComponent("bugDangerPanel")
@ViewScope
public class DangerPanel implements AdminPanel {
    @NonNull private final DataService dataService;
    private final I18N i18n;

    @Autowired
    public DangerPanel(@NonNull DataService dataService, I18N i18n) {
        this.dataService = dataService;
        this.i18n = i18n;
    }

    @Override
    public Component createContent(@NonNull Bug bug, @NonNull NavigationManager navigationManager) {
        Button unmerge = new I18nButton(
                e -> new Popup(i18n, Messages.CONFIRM).addComponent(new I18nLabel(i18n, Messages.UNMERGE_BUG_CONFIRM)).addYesNoButtons(p -> {
            dataService.unmergeBug(bug);
            navigationManager.navigateBack();
        }, true).show(), i18n, Messages.UNMERGE_BUG);
        unmerge.setWidth(100, Sizeable.Unit.PERCENTAGE);
        Button delete = new I18nButton(
                e -> new Popup(i18n, Messages.CONFIRM).addComponent(new Label(Messages.DELETE_BUG_CONFIRM)).addYesNoButtons(p -> {
                    dataService.delete(bug);
                    navigationManager.navigateBack();
                }, true).show(), i18n, Messages.DELETE_BUG);
        delete.setWidth(100, Sizeable.Unit.PERCENTAGE);
        return new VerticalLayout(unmerge, delete);
    }

    @Override
    public String getCaption() {
        return i18n.get(Messages.DANGER_ZONE);
    }

    @Override
    public String getId() {
        return "danger-zone";
    }

    @Override
    public Resource getIcon() {
        return VaadinIcons.EXCLAMATION;
    }

    @Override
    public String getStyleName() {
        return AcraTheme.RED_PANEL_HEADER;
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
