/*
 * (C) Copyright 2018 Lukas Morawietz (https://github.com/F43nd1r)
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

package com.faendir.acra.ui.base;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.router.NavigationTrigger;
import com.vaadin.flow.router.Router;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lukas
 * @since 18.10.18
 */
public interface HasRoute extends HasAcrariumTitle {
    @NonNull
    Path.Element<?> getPathElement();

    @Nullable
    default Parent<?> getLogicalParent() {
        return null;
    }

    default List<Path.Element<?>> getPathElements(ApplicationContext applicationContext, AfterNavigationEvent afterNavigationEvent) {
        List<Path.Element<?>> list = new ArrayList<>();
        list.add(getPathElement());
        Parent<?> parent = getLogicalParent();
        if (parent != null) {
            list.addAll(parent.get(applicationContext, afterNavigationEvent).getPathElements(applicationContext, afterNavigationEvent));
        }
        return list;
    }

    @Override
    default TranslatableText getTitle() {
        return getPathElement();
    }

    String getTranslation(String key, Object... params);

    class Parent<T extends HasRoute> {
        private final Class<T> parentClass;

        public Parent(Class<T> parentClass) {
            this.parentClass = parentClass;
        }

        public T get(ApplicationContext applicationContext, AfterNavigationEvent afterNavigationEvent) {
            return applicationContext.getBean(parentClass);
        }
    }

    class ParametrizedParent<T extends HasRoute & HasUrlParameter<P>, P> extends Parent<T> {
        private final P parameter;

        public ParametrizedParent(Class<T> parentClass, P parameter) {
            super(parentClass);
            this.parameter = parameter;
        }

        @Override
        public T get(ApplicationContext applicationContext, AfterNavigationEvent afterNavigationEvent) {
            T t = super.get(applicationContext, afterNavigationEvent);
            t.setParameter(new BeforeRouteEvent(afterNavigationEvent.getSource(), afterNavigationEvent.getLocation(), afterNavigationEvent.getActiveChain().get(0).getClass()), parameter);
            return t;
        }
    }

    class BeforeRouteEvent extends BeforeEvent {

        public BeforeRouteEvent(Router router, Location location, Class<?> navigationTarget) {
            super(router, NavigationTrigger.PAGE_LOAD,location, navigationTarget, UI.getCurrent());
        }
    }
}
