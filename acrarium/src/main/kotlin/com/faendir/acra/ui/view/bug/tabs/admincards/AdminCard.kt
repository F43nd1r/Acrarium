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
package com.faendir.acra.ui.view.bug.tabs.admincards

import com.faendir.acra.service.DataService
import com.faendir.acra.ui.component.Card
import com.faendir.acra.ui.ext.SizeUnit
import com.faendir.acra.ui.ext.setFlexGrow
import com.faendir.acra.ui.ext.setMaxHeight
import com.faendir.acra.ui.ext.setMaxWidth
import com.faendir.acra.ui.ext.setWidth

abstract class AdminCard(val dataService: DataService) : Card() {

    init {
        setWidth(500, SizeUnit.PIXEL)
        setMaxWidth(1000, SizeUnit.PIXEL)
        setMaxHeight(500, SizeUnit.PIXEL)
        setFlexGrow(1)
    }
}