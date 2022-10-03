package com.faendir.acra.navigation

import com.vaadin.flow.server.VaadinContext
import org.springframework.web.context.WebApplicationContext
import java.util.*
import java.util.function.Supplier

class NonVaadinContext(val applicationContext: WebApplicationContext) : VaadinContext {
    private val attributes = mutableMapOf<Class<*>, Any>()

    override fun <T : Any> getAttribute(type: Class<T>, defaultValueSupplier: Supplier<T>?): T {
        return try {
            type.cast(attributes[type])
        } catch (e: ClassCastException) {
            null
        } ?: defaultValueSupplier?.get() ?: throw NoSuchElementException()
    }

    override fun <T : Any> setAttribute(clazz: Class<T>, value: T) {
        attributes[clazz] = value
    }

    override fun removeAttribute(clazz: Class<*>?) {
        attributes.remove(clazz)
    }

    override fun getContextParameterNames(): Enumeration<String> = Collections.emptyEnumeration()

    override fun getContextParameter(name: String?): String? = null
}