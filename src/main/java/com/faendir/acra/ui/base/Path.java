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

import com.faendir.acra.ui.component.HasSize;
import com.faendir.acra.ui.component.HasStyle;
import com.faendir.acra.ui.component.SubTab;
import com.faendir.acra.ui.component.Translatable;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationListener;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.shared.Registration;
import org.springframework.context.ApplicationContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author lukas
 * @since 18.10.18
 */
public class Path extends SubTab implements AfterNavigationListener, HasStyle, HasSize {
    private final ApplicationContext applicationContext;
    private final Map<Tab, Runnable> actions = new HashMap<>();
    private Registration registration;

    public Path(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        addSelectedChangeListener(e -> {
            Runnable action = actions.get(e.getSelectedTab());
            if(action != null) {
                action.run();
            }
        });
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        removeAll();
        actions.clear();
        List<Element<?>> elements = event.getActiveChain().stream().filter(HasRoute.class::isInstance).map(HasRoute.class::cast).flatMap(e -> e.getPathElements(applicationContext, event).stream()).collect(Collectors.toList());
        Collections.reverse(elements);
        for (Element<?> element : elements) {
                Tab tab = element.toTab();
                add(tab);
                setSelectedTab(tab);
                actions.put(tab, element.getAction());

        }
    }


    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        registration = attachEvent.getUI().addAfterNavigationListener(this);
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        registration.remove();
        super.onDetach(detachEvent);
    }

    public static class Element<T extends Component> extends TranslatableText{
        final Class<T> target;

        public Element(Class<T> target, String titleId, Object... params) {
            super(titleId, params);
            this.target = target;
        }

        public Tab toTab() {
            Translatable<Div> div = Translatable.createDiv(getId(), getParams());
            div.getStyle().set("overflow-wrap", "break-word");
            div.getStyle().set("width", "100%");
            return new Tab(div);
        }

        public Runnable getAction() {
            return () -> UI.getCurrent().navigate(target);
        }

    }

    public static class ParametrizedTextElement<T extends Component & HasUrlParameter<P>, P> extends Element<T> {
        final P parameter;

        public ParametrizedTextElement(Class<T> target, P parameter, String titleId, Object... params) {
            super(target, titleId, params);
            this.parameter = parameter;
        }

        @Override
        public Runnable getAction() {
            return () -> UI.getCurrent().navigate(target, parameter);
        }
    }

}
