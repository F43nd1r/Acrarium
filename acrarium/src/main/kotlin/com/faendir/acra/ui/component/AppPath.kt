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

import com.faendir.acra.i18n.Messages
import com.faendir.acra.i18n.TranslatableText
import com.faendir.acra.navigation.LogicalParent
import com.faendir.acra.ui.component.tabs.SubTabs
import com.vaadin.flow.component.AttachEvent
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.DetachEvent
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.tabs.Tab
import com.vaadin.flow.router.*
import com.vaadin.flow.shared.Registration
import org.springframework.context.support.GenericApplicationContext
import org.springframework.core.annotation.AnnotationUtils
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

/**
 * @author lukas
 * @since 18.10.18
 */
class AppPath(private val applicationContext: GenericApplicationContext) : SubTabs(), AfterNavigationListener, BeforeEnterObserver {
    private var registration: Registration? = null

    private lateinit var parameters: RouteParameters

    override fun beforeEnter(event: BeforeEnterEvent) {
        parameters = event.routeParameters
    }

    override fun afterNavigation(event: AfterNavigationEvent) {
        removeAll()
        for (element in event.activeChain.filterIsInstance<Component>().flatMap { findPathElements(it::class) }.reversed()) {
            val tab = element.toTab()
            add(tab)
            selectedTab = tab
        }
    }

    private fun findPathElements(hasElement: KClass<out Component>): List<Element<*>> {
        val routeAnnotation = AnnotationUtils.findAnnotation(hasElement.java, Route::class.java)
        if (routeAnnotation != null && routeAnnotation.value.isNotEmpty()) {
            val layouts = if (routeAnnotation.layout != UI::class) {
                generateSequence(routeAnnotation.layout) { AnnotationUtils.findAnnotation(it.java, ParentLayout::class.java)?.value }.toList()
            } else emptyList()
            if (routeAnnotation.value.startsWith("app") || layouts
                    .mapNotNull { AnnotationUtils.findAnnotation(it.java, RoutePrefix::class.java) }.lastOrNull()?.value?.startsWith("app") == true
            ) {
                val classes = listOf(hasElement) + layouts
                val availableParameters = routeAnnotation.value.getAvailablePathParams() +
                        layouts.mapNotNull { AnnotationUtils.findAnnotation(it.java, RoutePrefix::class.java) }
                            .flatMap { it.value.getAvailablePathParams() }
                val parameters = availableParameters.associateWith { parameters[it].orElse("") }
                val title = classes.find {
                    it.isSubclassOf(HasAcrariumTitle::class) && AnnotationUtils.findAnnotation(
                        it.java,
                        org.springframework.stereotype.Component::class.java
                    ) != null
                }?.let { (applicationContext.getBean(it.java) as HasAcrariumTitle).title } ?: TranslatableText(Messages.ONE_ARG, "MISSING TITLE")
                val logicalParent =
                    classes.firstNotNullOfOrNull {
                        AnnotationUtils.findAnnotation(it.java, LogicalParent::class.java)
                    }?.value?.takeIf { it.isSubclassOf(Component::class) }
                return listOf(Element(hasElement, parameters, title.id, *title.params)) +
                        logicalParent?.let { @Suppress("UNCHECKED_CAST") findPathElements(it as KClass<out Component>) }.orEmpty()
            }
        }
        return emptyList()

    }

    private fun String.getAvailablePathParams() = split("/").filter { it.startsWith(':') }.map { it.drop(1) }

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