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

/**
 * @author lukas
 * @since 19.06.18
 */
@SpringComponent("bugDangerPanel")
@ViewScope
public class DangerPanel implements AdminPanel {
    @NonNull private final DataService dataService;

    @Autowired
    public DangerPanel(@NonNull DataService dataService) {
        this.dataService = dataService;
    }

    @Override
    public Component createContent(@NonNull Bug bug, @NonNull NavigationManager navigationManager) {
        Button unmerge = new Button("Disjoin Bug",
                e -> new Popup().setTitle("Confirm").addComponent(new Label("Are you sure you want to revert merging this bug group?")).addYesNoButtons(p -> {
            dataService.unmergeBug(bug);
            navigationManager.navigateBack();
        }, true).show());
        unmerge.setWidth(100, Sizeable.Unit.PERCENTAGE);
        Button delete = new Button("Delete Bug",
                e -> new Popup().setTitle("Confirm").addComponent(new Label("Are you sure you want to delete this bug and all its reports?")).addYesNoButtons(p -> {
                    dataService.delete(bug);
                    navigationManager.navigateBack();
                }, true).show());
        delete.setWidth(100, Sizeable.Unit.PERCENTAGE);
        return new VerticalLayout(unmerge, delete);
    }

    @Override
    public String getCaption() {
        return "Danger Zone";
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
