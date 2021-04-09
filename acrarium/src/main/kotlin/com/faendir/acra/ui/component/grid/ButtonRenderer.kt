package com.faendir.acra.ui.component.grid

import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.data.renderer.ComponentRenderer

class ButtonRenderer<T>(icon: VaadinIcon, onClick: (T) -> Unit) : ComponentRenderer<Button, T>({ t ->
    Button(Icon(icon)) { onClick(t) }
})