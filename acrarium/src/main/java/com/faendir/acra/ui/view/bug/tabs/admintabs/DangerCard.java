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

package com.faendir.acra.ui.view.bug.tabs.admintabs;

import com.faendir.acra.i18n.Messages;
import com.faendir.acra.model.Bug;
import com.faendir.acra.service.DataService;
import com.faendir.acra.ui.component.Card;
import com.faendir.acra.ui.component.HasSize;
import com.faendir.acra.ui.component.Translatable;
import com.faendir.acra.ui.component.dialog.FluentDialog;
import com.faendir.acra.ui.view.Overview;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;

@UIScope
@SpringComponent("bugDangerCard")
public class DangerCard extends AdminCard {
    public DangerCard(DataService dataService) {
        super(dataService);
        setHeader(Translatable.createLabel(Messages.DANGER_ZONE));
        setHeaderColor("var(--lumo-error-contrast-color)", "var(--lumo-error-color)");
    }

    @Override
    public void init(Bug bug) {
        removeContent();
        Translatable<Button> unmergeButton = Translatable.createButton(e -> new FluentDialog().addText(Messages.UNMERGE_BUG_CONFIRM).addConfirmButtons(p -> {
            getDataService().unmergeBug(bug);
            UI.getCurrent().navigate(Overview.class);
        }), Messages.UNMERGE_BUG);
        unmergeButton.setWidthFull();
        Translatable<Button> deleteButton = Translatable.createButton(e -> new FluentDialog().addText(Messages.DELETE_BUG_CONFIRM).addConfirmButtons(popup -> {
            getDataService().delete(bug);
            UI.getCurrent().navigate(Overview.class);
        }).show(), Messages.DELETE_BUG);
        deleteButton.setWidthFull();
        add(unmergeButton, deleteButton);
    }
}
