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
package com.faendir.acra.ui.view.bug.tabs

import com.faendir.acra.model.Bug
import com.faendir.acra.model.QReport
import com.faendir.acra.navigation.ParseBugParameter
import com.faendir.acra.navigation.View
import com.faendir.acra.service.DataService
import com.faendir.acra.ui.component.statistics.Statistics
import com.faendir.acra.ui.view.bug.BugView
import com.vaadin.flow.router.Route

/**
 * @author lukas
 * @since 11.10.18
 */
@View("bugStatisticsTab")
@Route(value = "statistics", layout = BugView::class)
class StatisticsTab(private val dataService: DataService, @ParseBugParameter private val bug: Bug) : BugTab<Statistics>(bug) {

    override fun initContent(): Statistics {
        return Statistics(bug.app, QReport.report.stacktrace.bug.eq(bug), dataService)
    }
}