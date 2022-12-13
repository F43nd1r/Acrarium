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
package com.faendir.acra.ui.component

import com.faendir.acra.navigation.RouteParams
import com.faendir.acra.navigation.View
import com.faendir.acra.security.SecurityUtils
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.Composite
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.FlexLayout
import org.springframework.context.ApplicationContext
import kotlin.reflect.KClass

@View
class CardView<C : Composite<Card>>(private val context: ApplicationContext, private val routeParams: RouteParams) : FlexLayout() {

    init {
        setWidthFull()
        flexWrap = FlexWrap.WRAP
        justifyContentMode = FlexComponent.JustifyContentMode.CENTER
    }

    @SafeVarargs
    fun add(vararg cards: KClass<out C>) {
        add(*cards.map { it.java }.filter { SecurityUtils.hasAccess(routeParams::appId, it) }.map { context.getBean(it) }.toTypedArray<Component>())
    }
}