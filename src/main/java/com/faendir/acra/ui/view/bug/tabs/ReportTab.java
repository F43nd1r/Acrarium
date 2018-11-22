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
import com.faendir.acra.service.AvatarService;
import com.faendir.acra.service.DataService;
import com.faendir.acra.ui.base.ReportList;
import com.faendir.acra.ui.view.bug.BugView;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

/**
 * @author lukas
 * @since 19.11.18
 */
@UIScope
@SpringComponent("bugReportTab")
@Route(value = "report", layout = BugView.class)
public class ReportTab extends BugTab<Div> {
    @NonNull
    private final AvatarService avatarService;

    @Autowired
    public ReportTab(@NonNull DataService dataService, @NonNull AvatarService avatarService) {
        super(dataService);
        this.avatarService = avatarService;
        getContent().setSizeFull();
    }

    @Override
    void init(Bug bug) {
        getContent().removeAll();
        getContent().add(new ReportList(bug.getApp(), getDataService().getReportProvider(bug), avatarService, getDataService()::delete));
    }
}
