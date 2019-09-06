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

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.Synchronize;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.shared.Registration;

/**
 * @author lukas
 * @since 06.09.19
 */
public class SubTab extends Tab implements HasComponents {
    private final Tabs content;
    private Registration registration;

    public SubTab(Tab... tabs) {
        content = new Tabs(false, tabs);
        content.setOrientation(Tabs.Orientation.VERTICAL);
        content.addSelectedChangeListener(e -> getParent().ifPresent(parent -> {
            if (e.getSelectedTab() != null && parent instanceof Tabs) {
                ((Tabs) parent).setSelectedIndex(-1);
            }
        }));
        content.setWidthFull();
        super.add(content);
        hideIfEmpty();
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        getParent().ifPresent(parent -> {
            if (parent instanceof Tabs) {
                registration = ((Tabs) parent).addSelectedChangeListener(e -> {
                    if (e.getSelectedTab() == this) {
                        content.setSelectedIndex(0);
                    } else if (e.getSelectedTab() != null) {
                        content.setSelectedIndex(-1);
                    }
                });
            }
        });
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        if(registration != null) {
            registration.remove();
        }
    }

    public void add(Tab... tabs) {
        content.add(tabs);
        hideIfEmpty();
    }

    @Override
    public void add(Component... components) {
        content.add(components);
        hideIfEmpty();
    }

    @Override
    public void remove(Component... components) {
        content.remove(components);
        hideIfEmpty();
    }

    @Override
    public void removeAll() {
        content.removeAll();
        hideIfEmpty();
    }

    @Override
    public void addComponentAtIndex(int index, Component component) {
        content.addComponentAtIndex(index, component);
        hideIfEmpty();
    }

    public void replace(Component oldComponent, Component newComponent) {
        content.replace(oldComponent, newComponent);
        hideIfEmpty();
    }

    public Registration addSelectedChangeListener(ComponentEventListener<Tabs.SelectedChangeEvent> listener) {
        return content.addSelectedChangeListener(listener);
    }

    @Synchronize(property = "selected", value = {"selected-changed"})
    public int getSelectedIndex() {
        return content.getSelectedIndex();
    }

    public void setSelectedIndex(int selectedIndex) {
        content.setSelectedIndex(selectedIndex);
    }

    public Tab getSelectedTab() {
        return content.getSelectedTab();
    }

    public void setSelectedTab(Tab selectedTab) {
        content.setSelectedTab(selectedTab);
    }

    private void hideIfEmpty() {
        setVisible(content.getComponentCount() > 0);
    }
}
