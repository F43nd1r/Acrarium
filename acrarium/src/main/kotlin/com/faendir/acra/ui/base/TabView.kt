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
package com.faendir.acra.ui.base

import com.faendir.acra.i18n.Messages
import com.faendir.acra.ui.component.Path
import com.faendir.acra.ui.component.Path.ParametrizedTextElement
import com.faendir.acra.ui.component.SpringComposite
import com.faendir.acra.ui.ext.flexLayout
import com.faendir.acra.ui.ext.forEach
import com.faendir.acra.ui.ext.setFlexGrow
import com.faendir.acra.ui.ext.tab
import com.faendir.acra.ui.ext.tabs
import com.faendir.acra.util.PARAM
import com.faendir.acra.util.indexOfFirstOrNull
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasElement
import com.vaadin.flow.component.tabs.Tabs
import com.vaadin.flow.router.RouteParameters
import java.io.Serializable

/**
 * @author lukas
 * @since 03.12.18
 */
@Suppress("LeakingThis")
open class TabView<T>(private vararg val tabs: TabInfo<out T>) : ParentLayout()
        where T : Component {
    private val header: Tabs

    init {
        setSizeFull()
        setFlexDirection(FlexDirection.COLUMN)
        header = tabs {
            forEach(tabs.toList()) {
                tab(it.labelId)
            }
            addSelectedChangeListener {
                ui.ifPresent { ui ->
                    ui.navigate(tabs[it.source.selectedIndex].tabClass, RouteParameters(PARAM, (content as Tab<T>).id.toString()))
                }
            }
        }
        setRouterRoot(flexLayout {
            setWidthFull()
            setFlexGrow(1)
            style["overflow"] = "auto"
        })
    }

    override fun showRouterLayoutContent(content: HasElement) {
        super.showRouterLayoutContent(content)
        tabs.indexOfFirstOrNull { it.tabClass == content.javaClass }?.let { header.selectedIndex = it }
    }

    class TabInfo<T>(val tabClass: Class<T>, val labelId: String) : Serializable

    abstract class Tab<T : Component> : SpringComposite<T>(), HasRoute {
        abstract val id: Int
        abstract val name: String

        override val pathElement: Path.Element<*>
            get() = ParametrizedTextElement<Tab<*>, Int>(javaClass, id, Messages.ONE_ARG, name)
    }
}