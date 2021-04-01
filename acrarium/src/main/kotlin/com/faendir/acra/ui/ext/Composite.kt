package com.faendir.acra.ui.ext

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.Composite
import kotlin.Unit

fun <T : Component?> Composite<T>.content(initializer: T.() -> Unit) : Unit = content.run(initializer)