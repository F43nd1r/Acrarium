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
import com.faendir.acra.service.DataService
import com.faendir.acra.ui.component.Path
import com.faendir.acra.ui.component.Path.ParametrizedTextElement
import com.faendir.acra.ui.component.SpringComposite
import com.faendir.acra.ui.component.Tab
import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.util.indexOfFirstOrNull
import com.vaadin.flow.component.AttachEvent
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.orderedlayout.FlexLayout
import com.vaadin.flow.component.tabs.Tabs
import com.vaadin.flow.router.BeforeEvent
import com.vaadin.flow.router.HasUrlParameter
import java.io.Serializable

/**
 * @author lukas
 * @since 03.12.18
 */
open class TabView<T, P>(private vararg val tabs: TabInfo<out T, P>) : ParentLayout() where T : Component, T : HasUrlParameter<P> {
    private val header: Tabs = Tabs()
    private var parameter: P? = null

    init {
        header.add(*tabs.map { Tab(it.labelId) }.toTypedArray())
        header.addSelectedChangeListener { ui.ifPresent { ui: UI -> ui.navigate(tabs[it.source.selectedIndex].tabClass, parameter) } }
        setSizeFull()
        val content = FlexLayout()
        content.setWidthFull()
        expand(content)
        content.style["overflow"] = "auto"
        setRouterRoot(content)
        style["flex-direction"] = "column"
        removeAll()
        add(header, content)
    }

    private fun setActiveChild(child: T, parameter: P) {
        this.parameter = parameter
        tabs.indexOfFirstOrNull { it.tabClass == child.javaClass }?.let { header.selectedIndex = it }
    }

    class TabInfo<T : HasUrlParameter<P>, P>(val tabClass: Class<T>, val labelId: String) : Serializable

    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    abstract class Tab<T : Component, P : Any>(val dataService: DataService, private val getParameter: DataService.(Int) -> P?, private val getId: P.() -> Int,
                                               private val getTitle: P.() -> String, private val getParent: P.() -> HasRoute.Parent<*>?) :
            SpringComposite<T>(), HasSecureParameter<Int>, HasRoute, Init<P> {
        private lateinit var parameter: P

        override fun setParameterSecure(event: BeforeEvent?, parameter: Int) {
            dataService.getParameter(parameter)?.also { this.parameter = it } ?: event?.rerouteToError(IllegalArgumentException::class.java) ?: throw IllegalArgumentException()
        }

        override fun onAttach(attachEvent: AttachEvent) {
            super.onAttach(attachEvent)
            init(parameter)
            var parent = parent
            while (parent.isPresent) {
                if (parent.get() is TabView<*, *>) {
                    @Suppress("UNCHECKED_CAST")
                    (parent.get() as TabView<Tab<*, *>, Int>).setActiveChild(this, parameter.getId())
                    break
                }
                parent = parent.get().parent
            }
        }

        override val pathElement: Path.Element<*>
            get() = ParametrizedTextElement<Tab<*, *>, Int>(javaClass, parameter.getId(), Messages.ONE_ARG, parameter.getTitle())

        override val logicalParent: HasRoute.Parent<*>?
            get() {
                return parameter.getParent()
            }
    }
}