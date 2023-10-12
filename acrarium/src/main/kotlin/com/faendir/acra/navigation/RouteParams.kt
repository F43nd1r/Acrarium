/*
 * (C) Copyright 2022-2023 Lukas Morawietz (https://github.com/F43nd1r)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.faendir.acra.navigation

import com.faendir.acra.persistence.app.AppId
import com.faendir.acra.persistence.bug.BugId
import com.faendir.acra.util.toNullable
import com.vaadin.flow.component.UI
import com.vaadin.flow.router.BeforeEnterEvent
import com.vaadin.flow.router.BeforeEnterListener
import com.vaadin.flow.router.ListenerPriority
import com.vaadin.flow.router.RouteParameters
import com.vaadin.flow.server.UIInitEvent
import com.vaadin.flow.server.UIInitListener
import org.springframework.stereotype.Component


@Component
@ListenerPriority(1000)
class RouteParams : UIInitListener, BeforeEnterListener {
    private val cache = mutableMapOf<Int, RouteParameters>()
    override fun uiInit(uiInitEvent: UIInitEvent) {
        uiInitEvent.ui.addBeforeEnterListener(this)
    }

    override fun beforeEnter(event: BeforeEnterEvent) {
        cache[event.ui.uiId] = event.routeParameters
    }

    fun appId(): AppId = AppId(parse(PARAM_APP) { it.toInt() })

    fun bugId(): BugId = BugId(parse(PARAM_BUG) { it.toInt() })

    fun reportId(): String = parse(PARAM_REPORT) { it }

    fun installationId(): String = parse(PARAM_INSTALLATION) { it }

    fun <T> parse(param: String, parse: (String) -> T?): T {
        val id = UI.getCurrent()?.uiId ?: throw IllegalStateException("No UI present")
        val value = cache[id]?.get(param)?.toNullable() ?: throw IllegalArgumentException("Parameter $param not present")
        return parse(value) ?: throw IllegalArgumentException("Parse failure for parameter $param")
    }
}

const val PARAM_APP = "app"
const val PARAM_BUG = "bug"
const val PARAM_INSTALLATION = "installation"
const val PARAM_REPORT = "report"