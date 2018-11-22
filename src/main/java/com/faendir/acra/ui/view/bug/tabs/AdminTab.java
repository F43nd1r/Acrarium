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

import com.faendir.acra.i18n.Messages;
import com.faendir.acra.model.Bug;
import com.faendir.acra.service.DataService;
import com.faendir.acra.ui.base.popup.Popup;
import com.faendir.acra.ui.component.Card;
import com.faendir.acra.ui.component.FlexLayout;
import com.faendir.acra.ui.component.HasSize;
import com.faendir.acra.ui.component.Translatable;
import com.faendir.acra.ui.view.Overview;
import com.faendir.acra.ui.view.bug.BugView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * @author lukas
 * @since 18.10.18
 */
@UIScope
@SpringComponent("bugAdminTab")
@Route(value = "admin", layout = BugView.class)
public class AdminTab extends BugTab<FlexLayout> {
    @Autowired
    public AdminTab(DataService dataService) {
        super(dataService);
    }

    @Override
    void init(Bug bug) {
        getContent().removeAll();
        getContent().setFlexWrap(FlexLayout.FlexWrap.WRAP);
        getContent().setWidthFull();
        Translatable<TextArea> title = Translatable.createTextArea(bug.getTitle(), Messages.TITLE);
        title.setWidthFull();
        Translatable<Button> save = Translatable.createButton(e -> {
            bug.setTitle(title.getContent().getValue());
            getDataService().store(bug);
        }, Messages.SAVE);
        save.setWidthFull();
        Card propertiesCard = new Card(title, save);
        propertiesCard.setHeader(Translatable.createText(Messages.PROPERTIES));
        propertiesCard.setWidth(500, HasSize.Unit.PIXEL);
        getContent().add(propertiesCard);
        getContent().expand(propertiesCard);

        Translatable<Button> unmergeButton = Translatable.createButton(e -> new Popup().setTitle(Messages.UNMERGE_BUG_CONFIRM).addYesNoButtons(p -> {
            getDataService().unmergeBug(bug);
            UI.getCurrent().navigate(Overview.class);
        }, true), Messages.UNMERGE_BUG);
        unmergeButton.setWidthFull();
        Translatable<Button> deleteButton = Translatable.createButton(e -> new Popup().setTitle(Messages.DELETE_BUG_CONFIRM).addYesNoButtons(popup -> {
            getDataService().delete(bug);
            UI.getCurrent().navigate(Overview.class);
        }, true).show(), Messages.DELETE_BUG);
        deleteButton.setWidthFull();
        Card dangerCard = new Card(unmergeButton, deleteButton);
        dangerCard.setHeader(Translatable.createText(Messages.DANGER_ZONE));
        dangerCard.setWidth(500, HasSize.Unit.PIXEL);
        getContent().add(dangerCard);
        getContent().expand(dangerCard);
    }
}
