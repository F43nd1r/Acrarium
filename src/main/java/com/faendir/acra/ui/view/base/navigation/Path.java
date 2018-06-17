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

package com.faendir.acra.ui.view.base.navigation;

import com.faendir.acra.ui.navigation.MyNavigator;
import com.faendir.acra.ui.navigation.SingleViewProvider;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Composite;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.themes.AcraTheme;
import com.vaadin.ui.themes.ValoTheme;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.stream.Collectors;

/**
 * @author Lukas
 * @since 24.03.2018
 */
public class Path extends Composite {
    private final Deque<Element> elements;
    private final CssLayout layout;

    public Path() {
        elements = new ArrayDeque<>();
        layout = new CssLayout();
        layout.addStyleName(AcraTheme.BASIC_FLEX);
        setCompositionRoot(layout);
    }

    public void set(Deque<MyNavigator.HierarchyElement> hierarchy) {
        elements.clear();
        layout.removeAllComponents();
        hierarchy.forEach(e -> {
            SingleViewProvider provider = (SingleViewProvider) e.getProvider();
            goTo(provider.getTitle(provider.getParameters(e.getNavState())), e.getNavState());
        });
    }

    public void goTo(String label, String id) {
        goTo(new Element(label, id));
    }

    public void goTo(Element element) {
        if (elements.stream().map(Element::getId).anyMatch(element.getId()::equals)) {
            while (!getLast().getId().equals(element.getId())) {
                goUp();
            }
        } else {
            if (!elements.isEmpty()) {
                Label icon = new Label(VaadinIcons.CARET_RIGHT.getHtml(), ContentMode.HTML);
                layout.addComponent(icon);
            }
            Button button = new Button(element.getLabel());
            button.addClickListener(e -> {
                while (getLast() != element) goUp();
                getUI().getNavigator().navigateTo(asUrlFragment());
            });
            button.addStyleNames(ValoTheme.BUTTON_BORDERLESS, AcraTheme.PATH_ELEMENT);
            layout.addComponent(button);
            elements.addLast(element);
        }
    }

    public int getSize() {
        return elements.size();
    }

    public boolean isEmpty() {
        return elements.isEmpty();
    }

    public Element getLast() {
        return elements.getLast();
    }

    public Element goUp() {
        if (elements.isEmpty()) {
            return null;
        }
        removeLastComponent();
        if (elements.size() != 1) removeLastComponent();
        return elements.removeLast();
    }

    public void clear() {
        layout.removeAllComponents();
        elements.clear();
    }

    public String asUrlFragment() {
        return elements.stream().map(Element::getId).collect(Collectors.joining(MyNavigator.SEPARATOR));
    }

    private void removeLastComponent() {
        layout.removeComponent(layout.getComponent(layout.getComponentCount() - 1));
    }

    public static class Element {
        private final String label;
        private final String id;

        public Element(String label, String id) {
            this.label = label;
            this.id = id;
        }

        public String getLabel() {
            return label;
        }

        public String getId() {
            return id;
        }
    }
}
