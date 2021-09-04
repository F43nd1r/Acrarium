package com.faendir.acra.ui.component.grid

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
                addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE)
            })
        }
    })