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
package com.faendir.acra.ui.view.base.layout;

import com.faendir.acra.client.mytabsheet.TabSheetMiddleClickExtensionConnector;
import com.faendir.acra.ui.navigation.NavigationManager;
import com.vaadin.event.ConnectorEventListener;
import com.vaadin.event.MouseEvents;
import com.vaadin.server.AbstractExtension;
import com.vaadin.shared.MouseEventDetails;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import com.vaadin.util.ReflectTools;
import org.springframework.lang.NonNull;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.stream.StreamSupport;

/**
 * @author Lukas
 * @since 12.12.2017
 */
public class MyTabSheet<T> extends TabSheet {
    private final T t;
    private final NavigationManager navigationManager;

    public MyTabSheet(@NonNull T t, @NonNull NavigationManager navigationManager, Collection<? extends ComponentFactory<T>> tabs) {
        this.t = t;
        this.navigationManager = navigationManager;
        for (ComponentFactory<T> tab : tabs) {
            addTab(tab);
        }
        MiddleClickExtension.extend(this);
    }

    public void addTab(ComponentFactory<T> tab) {
        TabWrapper<T> wrapper = new TabWrapper<>(t, navigationManager, tab);
        addComponent(wrapper);
        addSelectedTabChangeListener(wrapper);
    }

    public void guessInitialTab(String id) {
        Component component = StreamSupport.stream(spliterator(), false).filter(c -> c.getId().equals(id)).findAny().orElseGet(() -> getTab(0).getComponent());
        if (getSelectedTab() == component && component instanceof SelectedTabChangeListener) {
            ((SelectedTabChangeListener) component).selectedTabChange(new SelectedTabChangeEvent(this, true));
        }
        setSelectedTab(component);
    }

    public void addMiddleClickListener(ClickListener listener) {
        addListener(ClickEvent.class, listener, ClickListener.clickMethod);
    }

    @FunctionalInterface
    public interface ClickListener extends ConnectorEventListener {
        Method clickMethod = ReflectTools.findMethod(ClickListener.class, "click", ClickEvent.class);

        void click(ClickEvent event);
    }

    private static class TabWrapper<T> extends CustomComponent implements SelectedTabChangeListener {
        private final T t;
        private final NavigationManager navigationManager;
        private final ComponentFactory<T> tab;

        private TabWrapper(@NonNull T t, @NonNull NavigationManager navigationManager, @NonNull ComponentFactory<T> tab) {
            this.t = t;
            this.navigationManager = navigationManager;
            this.tab = tab;
            setSizeFull();
        }

        @Override
        public void selectedTabChange(@NonNull SelectedTabChangeEvent event) {
            if (this == event.getTabSheet().getSelectedTab()) {
                setCompositionRoot(tab.createContent(t, navigationManager));
            } else {
                setCompositionRoot(null);
            }
        }

        @Override
        public String getCaption() {
            return tab.getCaption();
        }

        @Override
        public String getId() {
            return tab.getId();
        }
    }

    public static class MiddleClickExtension extends AbstractExtension {
        private MiddleClickExtension(MyTabSheet component) {
            super(component);
            registerRpc((tabIndex, details) -> component.fireEvent(new ClickEvent(component, ((TabWrapper) component.getTab(tabIndex).getComponent()).tab, details)),
                    TabSheetMiddleClickExtensionConnector.Rpc.class);
        }

        public static void extend(MyTabSheet component) {
            new MiddleClickExtension(component);
        }
    }

    public static class ClickEvent extends MouseEvents.ClickEvent {
        private final ComponentFactory tab;

        public ClickEvent(MyTabSheet source, ComponentFactory tab, MouseEventDetails mouseEventDetails) {
            super(source, mouseEventDetails);
            this.tab = tab;
        }

        public ComponentFactory getTab() {
            return tab;
        }
    }
}
