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

package com.faendir.acra.ui.view.base;

import com.faendir.acra.ui.navigation.NavigationManager;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.TabSheet;
import org.springframework.core.Ordered;
import org.springframework.lang.NonNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author Lukas
 * @since 12.12.2017
 */
public class MyTabSheet<T> extends TabSheet {
    private final T t;
    private final NavigationManager navigationManager;

    @SafeVarargs
    public MyTabSheet(@NonNull T t, @NonNull NavigationManager navigationManager, Tab<T>... tabs) {
        this(t, navigationManager, Arrays.asList(tabs));
    }

    public MyTabSheet(@NonNull T t, @NonNull NavigationManager navigationManager, Collection<? extends Tab<T>> tabs) {
        this.t = t;
        this.navigationManager = navigationManager;
        for (Tab<T> tab : tabs) {
            addTab(tab);
        }
    }

    public List<String> getCaptions() {
        return StreamSupport.stream(Spliterators.spliterator(iterator(), getComponentCount(), Spliterator.ORDERED), false).map(Component::getCaption).collect(Collectors.toList());
    }

    public void addTab(Tab<T> tab) {
        TabWrapper<T> wrapper = new TabWrapper<>(t, navigationManager, tab);
        addComponent(wrapper);
        addSelectedTabChangeListener(wrapper);
    }

    public void setInitialTab(String caption) {
        StreamSupport.stream(spliterator(), false).filter(c -> c.getCaption().equals(caption)).findAny().ifPresent(component -> {
            if (getSelectedTab() == component && component instanceof SelectedTabChangeListener) {
                ((SelectedTabChangeListener) component).selectedTabChange(new SelectedTabChangeEvent(this, true));
            }
            setSelectedTab(component);
        });
    }

    public void setFirstTabAsInitialTab() {
        Component component = getTab(0).getComponent();
        if (getSelectedTab() == component && component instanceof SelectedTabChangeListener) {
            ((SelectedTabChangeListener) component).selectedTabChange(new SelectedTabChangeEvent(this, true));
        }
        setSelectedTab(component);
    }

    public interface Tab<T> extends Ordered {
        Component createContent(@NonNull T t, @NonNull NavigationManager navigationManager);

        String getCaption();
    }

    private static class TabWrapper<T> extends CustomComponent implements SelectedTabChangeListener {
        private final T t;
        private final NavigationManager navigationManager;
        private final Tab<T> tab;

        private TabWrapper(@NonNull T t, @NonNull NavigationManager navigationManager, @NonNull Tab<T> tab) {
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
    }
}
