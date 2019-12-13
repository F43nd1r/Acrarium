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

package com.faendir.acra.ui.view.bug.tabs.admincards;

import com.faendir.acra.model.Bug;
import com.faendir.acra.service.DataService;
import com.faendir.acra.ui.base.Init;
import com.faendir.acra.ui.component.Card;
import com.faendir.acra.ui.component.HasSize;

public abstract class AdminCard extends Card implements Init<Bug> {

    private DataService dataService;

    public AdminCard(DataService dataService) {
        this.dataService = dataService;
        setWidth(500, HasSize.Unit.PIXEL);
        setMaxWidth(1000, HasSize.Unit.PIXEL);
        setMaxHeight(500, HasSize.Unit.PIXEL);
        getStyle().set("flexGrow", "1");
    }

    public DataService getDataService() {
        return dataService;
    }
}
