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
package com.faendir.acra.ui.view.bug.tabs;

import com.faendir.acra.model.Bug;
import com.faendir.acra.model.Permission;
import com.faendir.acra.service.DataService;
import com.faendir.acra.ui.annotation.RequiresAppPermission;
import com.faendir.acra.ui.navigation.NavigationManager;
import com.faendir.acra.ui.view.base.Popup;
import com.vaadin.server.Sizeable;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.AcraTheme;
import com.vaadin.ui.themes.ValoTheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

/**
 * @author lukas
 * @since 04.06.18
 */
@RequiresAppPermission(Permission.Level.ADMIN)
@SpringComponent("bugAdminTab")
@ViewScope
public class AdminTab implements BugTab {
    @NonNull private final DataService dataService;

    @Autowired
    public AdminTab(@NonNull DataService dataService) {
        this.dataService = dataService;
    }

    @Override
    public Component createContent(@NonNull Bug bug, @NonNull NavigationManager navigationManager) {
        TextArea title = new TextArea("Title");
        title.setValue(bug.getTitle());
        title.setSizeFull();
        HorizontalLayout titleLayout = new HorizontalLayout();
        titleLayout.setDefaultComponentAlignment(Alignment.BOTTOM_CENTER);
        titleLayout.addComponents(title, new Button("Save", e -> {
            bug.setTitle(title.getValue());
            dataService.store(bug);
        }));
        titleLayout.setExpandRatio(title, 1);
        titleLayout.setSizeFull();
        Button unmerge = new Button("Unmerge Bug", e -> {
            dataService.unmergeBug(bug);
            navigationManager.navigateBack();
        });
        unmerge.setWidth(100, Sizeable.Unit.PERCENTAGE);
        Button delete = new Button("Delete Bug",
                e -> new Popup().setTitle("Confirm").addComponent(new Label("Are you sure you want to delete this bug and all its reports?")).addYesNoButtons(p -> {
                    dataService.delete(bug);
                    navigationManager.navigateBack();
                }, true).show());
        delete.addStyleName(ValoTheme.BUTTON_DANGER);
        delete.setWidth(100, Sizeable.Unit.PERCENTAGE);
        VerticalLayout layout = new VerticalLayout(titleLayout, unmerge, delete);
        layout.addStyleNames(AcraTheme.NO_PADDING, AcraTheme.MAX_WIDTH_728);
        VerticalLayout root = new VerticalLayout(layout);
        root.setSpacing(false);
        root.setSizeFull();
        root.setComponentAlignment(layout, Alignment.TOP_CENTER);
        root.addStyleName(AcraTheme.NO_PADDING);
        return root;
    }

    @Override
    public String getCaption() {
        return "Admin";
    }

    @Override
    public int getOrder() {
        return 3;
    }
}
