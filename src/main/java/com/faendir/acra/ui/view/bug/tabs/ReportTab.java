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
import com.faendir.acra.ui.navigation.NavigationManager;
import com.faendir.acra.ui.view.base.ReportList;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

/**
 * @author Lukas
 * @since 21.03.2018
 */
@SpringComponent("bugReportTab")
@ViewScope
public class ReportTab implements BugTab {
    @NonNull private final DataService dataService;
    @NonNull private final AvatarService avatarService;

    @Autowired
    public ReportTab(@NonNull DataService dataService, @NonNull AvatarService avatarService) {
        this.dataService = dataService;
        this.avatarService = avatarService;
    }

    @Override
    public Component createContent(@NonNull Bug bug, @NonNull NavigationManager navigationManager) {
        Component content = new ReportList(bug.getApp(), navigationManager, avatarService, dataService::delete, dataService.getReportProvider(bug));
        content.setSizeFull();
        return content;
    }

    @Override
    public String getCaption() {
        return ReportList.CAPTION;
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
