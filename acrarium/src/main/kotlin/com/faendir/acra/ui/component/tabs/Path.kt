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
package com.faendir.acra.ui.component.tabs

import com.faendir.acra.i18n.TranslatableText
import com.faendir.acra.ui.component.Translatable
import com.vaadin.flow.component.AttachEvent
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.DetachEvent
import com.vaadin.flow.component.tabs.Tab
import com.vaadin.flow.router.AfterNavigationEvent
import com.vaadin.flow.router.AfterNavigationListener
import com.vaadin.flow.shared.Registration
import org.springframework.context.support.GenericApplicationContext
import kotlin.reflect.KClass

/**
 * @author lukas
 * @since 18.10.18
 */
class Path(private val applicationContext: GenericApplicationContext) : SubTabs(), AfterNavigationListener {
    private var registration: Registration? = null

    override fun afterNavigation(event: AfterNavigationEvent) {
        removeAll()
        for (element in event.activeChain.filterIsInstance<HasRoute>().flatMap { it.getPathElements(applicationContext, event) }.reversed()) {
            val tab = element.toTab()
            add(tab)
            selectedTab = tab
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

    open class Element<T : Component>(val target: KClass<T>, val targetParams: Map<String, String>, titleId: String, vararg params: Any) :
        TranslatableText(titleId, *params) {
        fun toTab(): Tab {
            val link = Translatable.createRouterLink(target, targetParams, id, *params)
            link.addTranslatedListener { link.content.text?.let { link.content.element.setProperty("innerHTML", it.replace(".", ".<wbr>")) } }
            link.style["display"] = "block"
            link.style["overflow-wrap"] = "break-word"
            link.setWidthFull()
            return Tab(link)
        }
    }
}