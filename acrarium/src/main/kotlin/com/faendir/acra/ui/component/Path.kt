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
package com.faendir.acra.ui.component

import com.faendir.acra.ui.base.HasRoute
import com.faendir.acra.ui.base.HasSecureParameter
import com.faendir.acra.ui.base.HasSecureParameter.Companion.PARAM
import com.faendir.acra.ui.base.TranslatableText
import com.vaadin.flow.component.AttachEvent
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.DetachEvent
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.tabs.Tab
import com.vaadin.flow.component.tabs.Tabs.SelectedChangeEvent
import com.vaadin.flow.router.AfterNavigationEvent
import com.vaadin.flow.router.AfterNavigationListener
import com.vaadin.flow.router.HasUrlParameter
import com.vaadin.flow.router.RouteParameters
import com.vaadin.flow.shared.Registration
import org.springframework.context.ApplicationContext

/**
 * @author lukas
 * @since 18.10.18
 */
class Path(private val applicationContext: ApplicationContext) : SubTabs(), AfterNavigationListener {
    private val actions: MutableMap<Tab, () -> Unit> = mutableMapOf()
    private var registration: Registration? = null

    init {
        addSelectedChangeListener { actions[it.selectedTab]?.invoke() }
    }

    override fun afterNavigation(event: AfterNavigationEvent) {
        removeAll()
        actions.clear()
        event.activeChain.filterIsInstance<HasRoute>().flatMap { it.getPathElements(applicationContext, event) }.reversed().forEach { element ->
            val tab = element.toTab()
            add(tab)
            selectedTab = tab
            actions[tab] = element.action
        }
    }

    override fun onAttach(attachEvent: AttachEvent) {
        super.onAttach(attachEvent)
        registration = attachEvent.ui.addAfterNavigationListener(this)
    }

    override fun onDetach(detachEvent: DetachEvent) {
        registration?.remove()
        super.onDetach(detachEvent)
    }

    open class Element<T : Component>(val target: Class<T>, titleId: String, vararg params: Any) : TranslatableText(titleId, *params) {
        fun toTab(): Tab {
            val div = Translatable.createDiv(id, *params)
            div.addTranslatedListener { div.content.text?.let { div.content.element.setProperty("innerHTML", it.replace(".", ".<wbr>")) } }
            div.style["overflow-wrap"] = "break-word"
            div.setWidthFull()
            return Tab(div)
        }

        open val action: () -> Unit = { UI.getCurrent().navigate(target) }

    }

    class ParametrizedTextElement<T, P>(target: Class<T>, private val parameter: P, titleId: String, vararg params: Any) : Element<T>(target, titleId, *params)
            where T : Component, T : HasSecureParameter<P> {
        override val action: () -> Unit = { UI.getCurrent().navigate(target, RouteParameters(PARAM, parameter.toString())) }
    }
}