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
import com.faendir.acra.service.DataService;
import com.faendir.acra.ui.base.CardView;
import com.faendir.acra.ui.view.bug.BugView;
import com.faendir.acra.ui.view.bug.tabs.admincards.AdminCard;
import com.faendir.acra.ui.view.bug.tabs.admincards.DangerCard;
import com.faendir.acra.ui.view.bug.tabs.admincards.PropertiesCard;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;


/**
 * @author lukas
 * @since 18.10.18
 */
@UIScope
@SpringComponent("bugAdminTab")
@Route(value = "admin", layout = BugView.class)
public class AdminTab extends BugTab<CardView<AdminCard, Bug>> {
    @Autowired
    public AdminTab(DataService dataService) {
        super(dataService);
    }

    @PostConstruct
    public void setupContent() {
        getContent().add(PropertiesCard.class, DangerCard.class);
    }

    @Override
    public void init(Bug bug) {
        getContent().init(bug);
    }
}
