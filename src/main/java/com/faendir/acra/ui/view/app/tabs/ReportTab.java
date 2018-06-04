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

package com.faendir.acra.ui.view.app.tabs;

import com.faendir.acra.model.App;
import com.faendir.acra.service.data.DataService;
import com.faendir.acra.ui.navigation.NavigationManager;
import com.faendir.acra.ui.view.base.ReportList;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

/**
 * @author Lukas
 * @since 29.05.2017
 */
@SpringComponent
@ViewScope
public class ReportTab implements AppTab {
    @NonNull private final DataService dataService;

    @Autowired
    public ReportTab(@NonNull DataService dataService) {
        this.dataService = dataService;
    }

    @Override
    public Component createContent(@NonNull App app, @NonNull NavigationManager navigationManager) {
        Component content = new ReportList(app, navigationManager, dataService::delete, dataService.getReportProvider(app));
        content.setSizeFull();
        return content;
    }

    @Override
    public String getCaption() {
        return ReportList.CAPTION;
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
