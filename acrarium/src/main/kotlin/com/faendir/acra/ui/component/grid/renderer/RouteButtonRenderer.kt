/*
 * (C) Copyright 2021-2023 Lukas Morawietz (https://github.com/F43nd1r)
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
package com.faendir.acra.ui.component.grid.renderer

import com.faendir.acra.ui.ext.SizeUnit
import com.faendir.acra.ui.ext.setMargin
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.vaadin.flow.router.RouteParameters
import com.vaadin.flow.router.RouterLink

class RouteButtonRenderer<T>(icon: VaadinIcon, target: Class<out Component>, getTargetParams: (T) -> Map<String, String>) :
    ComponentRenderer<RouterLink, T>({ t ->
        RouterLink("", target, RouteParameters(getTargetParams(t))).apply {
            element.setAttribute("target", "_blank")
            element.setAttribute("rel", "noopener noreferrer")
            add(Button(Icon(icon)).apply {
                addThemeVariants(ButtonVariant.LUMO_TERTIARY)
                setMargin(0, SizeUnit.PIXEL)
            })
        }
    }), InteractiveColumnRenderer