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

import com.faendir.acra.i18n.Messages;
import com.faendir.acra.service.DataService;
import com.faendir.acra.ui.component.FlexLayout;
import com.faendir.acra.ui.component.Path;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * @author lukas
 * @since 03.12.18
 */
public class TabView<T extends Component & HasUrlParameter<P>, P> extends ParentLayout {
    private final TabInfo<? extends T, P>[] tabs;
    private final Tabs header;
    private P parameter;

    @SafeVarargs
    public TabView(TabInfo<? extends T, P>... tabs) {
        this.tabs = tabs;
        header = new Tabs(Stream.of(tabs).map(def -> new com.faendir.acra.ui.component.Tab(def.getLabelId())).toArray(com.faendir.acra.ui.component.Tab[]::new));
        header.addSelectedChangeListener(e -> getUI().ifPresent(ui -> ui.navigate(tabs[e.getSource().getSelectedIndex()].tabClass, parameter)));
        setSizeFull();
        FlexLayout content = new FlexLayout();
        content.setWidthFull();
        expand(content);
        content.getStyle().set("overflow", "auto");
        setRouterRoot(content);
        getStyle().set("flex-direction", "column");
        removeAll();
        add(header, content);
    }

    private void setActiveChild(T child, P parameter) {
        this.parameter = parameter;
        for (int i = 0; i < tabs.length; i++) {
            if (tabs[i].getTabClass().equals(child.getClass())) {
                header.setSelectedIndex(i);
                break;
            }
        }
    }

    public static class TabInfo<T extends HasUrlParameter<P>, P> implements Serializable {
        private final Class<T> tabClass;
        private final String labelId;

        public TabInfo(Class<T> tabClass, String labelId) {
            this.tabClass = tabClass;
            this.labelId = labelId;
        }

        public Class<T> getTabClass() {
            return tabClass;
        }

        public String getLabelId() {
            return labelId;
        }
    }

    public abstract static class Tab<T extends Component, P> extends Composite<T> implements HasSecureParameter<Integer>, HasRoute {
        private final DataService dataService;
        private final BiFunction<DataService, Integer, Optional<P>> parameterGetter;
        private final Function<P, Integer> idGetter;
        private final Function<P, String> titleGetter;
        private final Function<P, Parent<?>> parentGetter;
        private P parameter;

        public Tab(DataService dataService, BiFunction<DataService, Integer, Optional<P>> parameterGetter, Function<P, Integer> idGetter, Function<P, String> titleGetter, Function<P, Parent<?>> parentGetter) {
            this.dataService = dataService;
            this.parameterGetter = parameterGetter;
            this.idGetter = idGetter;
            this.titleGetter = titleGetter;
            this.parentGetter = parentGetter;
        }

        public DataService getDataService() {
            return dataService;
        }

        @Override
        public void setParameterSecure(BeforeEvent event, Integer parameter) {
            Optional<P> p = parameterGetter.apply(dataService, parameter);
            if (p.isPresent()) {
                this.parameter = p.get();
            } else {
                event.rerouteToError(IllegalArgumentException.class);
            }
        }

        protected abstract void init(P parameter);

        @Override
        protected void onAttach(AttachEvent attachEvent) {
            super.onAttach(attachEvent);
            init(this.parameter);
            Optional<Component> parent = getParent();
            while (parent.isPresent()) {
                if (parent.get() instanceof TabView) {
                    //noinspection unchecked
                    ((TabView<Tab<?, ?>, Integer>) parent.get()).setActiveChild(this, idGetter.apply(parameter));
                    break;
                }
                parent = parent.get().getParent();
            }
        }

        @Override
        @NonNull
        public Path.Element<?> getPathElement() {
            //noinspection unchecked
            return new Path.ParametrizedTextElement<>((Class<? extends Tab<?, ?>>) getClass(), idGetter.apply(parameter), Messages.ONE_ARG, titleGetter.apply(parameter));
        }

        @Override
        public Parent<?> getLogicalParent() {
            return parentGetter.apply(parameter);
        }
    }
}
