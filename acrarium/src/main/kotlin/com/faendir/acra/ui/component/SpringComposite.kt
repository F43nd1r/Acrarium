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

import com.googlecode.gentyref.GenericTypeReflector
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.Composite
import com.vaadin.flow.internal.ReflectTools
import com.vaadin.flow.spring.annotation.SpringComponent
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.core.annotation.AnnotationUtils
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable

@SpringComponent
abstract class SpringComposite<T : Component> : Composite<T>() {
    private lateinit var applicationContext: ApplicationContext

    @Suppress("UNCHECKED_CAST")
    override fun initContent(): T {
        val contentType = findContentType(javaClass as Class<out Composite<*>?>)
        return if (AnnotationUtils.findAnnotation(contentType, org.springframework.stereotype.Component::class.java) != null) {
            if (::applicationContext.isInitialized) {
                applicationContext.getBean(contentType) as T
            } else {
                throw IllegalStateException("Cannot access Composite content before bean initialization")
            }
        } else ReflectTools.createInstance(contentType) as T
    }

    @Autowired
    fun setApplicationContext(applicationContext: ApplicationContext) {
        this.applicationContext = applicationContext
    }

    companion object {
        /**
         * adapted from Composite#findContentType(Class)
         */
        private fun findContentType(
                compositeClass: Class<out Composite<*>?>): Class<out Component> {
            val type = GenericTypeReflector.getTypeParameter(compositeClass.genericSuperclass, Composite::class.java.typeParameters[0])
            if (type is Class<*> || type is ParameterizedType) return GenericTypeReflector.erase(type).asSubclass(Component::class.java)
            throw IllegalStateException(getExceptionMessage(type))
        }

        /**
         * adapted from Composite#getExceptionMessage(Type)
         */
        private fun getExceptionMessage(type: Type?): String {
            return when (type) {
                null -> "Composite is used as raw type: either add type information or override initContent()."
                is TypeVariable<*> -> String.format("Could not determine the composite content type for TypeVariable '%s'. Either specify exact type or override initContent().", type.getTypeName())
                else -> String.format("Could not determine the composite content type for %s. Override initContent().", type.typeName)
            }
        }
    }
}