package com.faendir.acra.ui.ext

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.Composite
import kotlin.Unit
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
inline fun <T : Component?> Composite<T>.content(initializer: T.() -> Unit) {
    contract {
        callsInPlace(initializer, InvocationKind.EXACTLY_ONCE)
    }
    content.run(initializer)
}