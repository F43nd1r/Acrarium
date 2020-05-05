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
package com.faendir.acra.ui.base

import com.faendir.acra.ui.component.Card
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.FlexLayout
import com.vaadin.flow.spring.annotation.SpringComponent
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Scope
import kotlin.streams.asSequence

@SpringComponent
@Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class CardView<C, P>(context: ApplicationContext) : FlexLayout(), Init<P> where C : Card, C : Init<P> {
    private val context: ApplicationContext
    private var parameter: P? = null

    init {
        setWidthFull()
        wrapMode = WrapMode.WRAP
        justifyContentMode = FlexComponent.JustifyContentMode.CENTER
        this.context = context
    }

    @SafeVarargs
    fun add(vararg cards: Class<out C>) {
        add(*cards.map { context.getBean(it) }.onEach { parameter?.run { it.init(this) } }.map { it as Component }.toTypedArray())
    }

    override fun init(p: P) {
        parameter = p
        children.asSequence().filterIsInstance<Init<P>>().forEach { it.init(p) }
    }
}