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

import com.faendir.acra.i18n.Messages
import com.faendir.acra.ui.component.SpringComposite
import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.ui.ext.forEach
import com.faendir.acra.ui.ext.setFlexGrow
import com.faendir.acra.ui.ext.tabs
import com.faendir.acra.util.indexOfFirstOrNull
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasElement
import com.vaadin.flow.component.HasStyle
import com.vaadin.flow.component.orderedlayout.FlexLayout
import com.vaadin.flow.component.tabs.Tab
import com.vaadin.flow.component.tabs.Tabs
import com.vaadin.flow.router.BeforeEnterEvent
import com.vaadin.flow.router.BeforeEnterObserver
import com.vaadin.flow.router.RouteParameters
import com.vaadin.flow.router.RouterLayout
import java.io.Serializable
import kotlin.reflect.KClass

/**
 * @author lukas
 * @since 03.12.18
 */
@Suppress("LeakingThis")
open class TabView(private vararg val tabs: TabInfo<out TabContent<*>>) : FlexLayout() , RouterLayout, BeforeEnterObserver {
    private val header: Tabs
    lateinit var content: TabContent<*>
    lateinit var parameters: RouteParameters

    init {
        setSizeFull()
        setFlexDirection(FlexDirection.COLUMN)
        header = tabs()
    }

    override fun beforeEnter(event: BeforeEnterEvent) {
        parameters = event.routeParameters
        header.removeAll()
        header.forEach(tabs.toList()) {
            add(Tab(Translatable.createRouterLink(it.tabClass, parameters, it.labelId)))
        }
    }

    override fun showRouterLayoutContent(content: HasElement) {
        if(content is TabContent<*>) {
            if (this::content.isInitialized) {
                remove(this.content)
            }
            this.content = content
            content.setFlexGrow(1)
            tabs.indexOfFirstOrNull { it.tabClass.java == content.javaClass }?.let { header.selectedIndex = it }
            add(content)
        }
    }

    class TabInfo<T : Any>(val tabClass: KClass<T>, val labelId: String) : Serializable

    abstract class TabContent<T : Component> : SpringComposite<T>(), HasRoute, HasStyle {
        abstract val params: Map<String, String>
        abstract val name: String

        override val pathElement: Path.Element<*>
            get() = Path.Element(this::class, params, Messages.ONE_ARG, name)
    }
}