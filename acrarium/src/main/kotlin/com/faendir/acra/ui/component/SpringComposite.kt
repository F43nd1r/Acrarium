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

package com.faendir.acra.ui.component;

import com.googlecode.gentyref.GenericTypeReflector;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.internal.ReflectTools;
import com.vaadin.flow.spring.annotation.SpringComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

@SpringComponent
public abstract class SpringComposite<T extends Component> extends Composite<T> {
    private ApplicationContext applicationContext;

    @SuppressWarnings("unchecked")
    @Override
    protected T initContent() {
        Class<? extends Component> contentType = findContentType((Class<? extends Composite<?>>) getClass());
        if (AnnotationUtils.findAnnotation(contentType, org.springframework.stereotype.Component.class) != null)
            if (applicationContext != null) {
                return (T) applicationContext.getBean(contentType);
            } else {
                throw new IllegalStateException("Cannot access Composite content before bean initialization");
            }
        return (T) ReflectTools.createInstance(contentType);
    }

    @Autowired
    public final void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /*
     * copied from Composite#findContentType(Class)
     */
    private static Class<? extends Component> findContentType(
            Class<? extends Composite<?>> compositeClass) {
        Type type = GenericTypeReflector.getTypeParameter(
                compositeClass.getGenericSuperclass(),
                Composite.class.getTypeParameters()[0]);
        if (type instanceof Class || type instanceof ParameterizedType) {
            return GenericTypeReflector.erase(type).asSubclass(Component.class);
        }
        throw new IllegalStateException(getExceptionMessage(type));
    }

    /*
     * copied from Composite#getExceptionMessage(Type)
     */
    private static String getExceptionMessage(Type type) {
        if (type == null) {
            return "Composite is used as raw type: either add type information or override initContent().";
        }

        if (type instanceof TypeVariable) {
            return String.format(
                    "Could not determine the composite content type for TypeVariable '%s'. "
                            + "Either specify exact type or override initContent().",
                    type.getTypeName());
        }
        return String.format(
                "Could not determine the composite content type for %s. Override initContent().",
                type.getTypeName());
    }
}
