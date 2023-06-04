/*
 * (C) Copyright 2021 Lukas Morawietz (https://github.com/F43nd1r)
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
package com.faendir.acra.ui.component

import com.github.appreciated.papermenubutton.PaperMenuButton
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.Composite
import com.vaadin.flow.component.HasComponents
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.FlexLayout

open class PopupButton(private val icon: VaadinIcon) : Composite<PaperMenuButton>(), HasComponents {
    private val container = FlexLayout()

    override fun initContent(): PaperMenuButton {
        return PaperMenuButton(Button(Icon(icon)).apply { element.appendChild(Icon(VaadinIcon.CHEVRON_DOWN_SMALL).element) }, container).apply {
            setDynamicAlign(true)
        }
    }

    override fun add(vararg components: Component) {
        container.add(*components)
    }

    override fun add(text: String) {
        container.add(text)
    }

    override fun addComponentAtIndex(index: Int, component: Component) {
        container.addComponentAtIndex(index, component)
    }

    override fun addComponentAsFirst(component: Component) {
        container.addComponentAsFirst(component)
    }

    override fun remove(vararg components: Component) {
        container.remove(*components)
    }

    override fun removeAll() {
        container.removeAll()
    }
}