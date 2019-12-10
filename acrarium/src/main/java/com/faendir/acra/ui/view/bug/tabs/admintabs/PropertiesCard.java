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
import com.faendir.acra.model.App;
import com.faendir.acra.model.Bug;
import com.faendir.acra.service.DataService;
import com.faendir.acra.ui.component.Card;
import com.faendir.acra.ui.component.FlexLayout;
import com.faendir.acra.ui.component.HasSize;
import com.faendir.acra.ui.component.Translatable;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;

@UIScope
@SpringComponent
public class PropertiesCard extends AdminCard {

    public PropertiesCard(DataService dataService) {
        super(dataService);
    }

    @Override
    public void init(Bug bug) {
        Translatable<TextArea> title = Translatable.createTextArea(bug.getTitle(), Messages.TITLE);
        title.setWidthFull();
        Translatable<Button> save = Translatable.createButton(e -> {
            bug.setTitle(title.getContent().getValue());
            getDataService().store(bug);
        }, Messages.SAVE);
        FlexLayout layout = new FlexLayout();
        layout.add(title, save);
        layout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        layout.setAlignItems(FlexComponent.Alignment.END);
        add(layout);
        setHeader(Translatable.createLabel(Messages.PROPERTIES));
    }
}
