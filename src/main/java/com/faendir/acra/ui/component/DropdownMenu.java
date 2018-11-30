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

package com.faendir.acra.ui.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.PropertyDescriptor;
import com.vaadin.flow.component.PropertyDescriptors;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author lukas
 * @since 15.11.18
 */
@Tag("simple-dropdown")
@HtmlImport("bower_components/simple-dropdown/simple-dropdown.html")
public class DropdownMenu extends Component implements HasComponents, HasSize, HasStyle {
    private static final String ORIGIN = "origin";
    private static PropertyDescriptor<Boolean, Boolean> ACTIVE_DESCRIPTOR = PropertyDescriptors.propertyWithDefault("active", false);
    private static PropertyDescriptor<String, String> LABEL_DESCRIPTOR = PropertyDescriptors.propertyWithDefault("label", "");

    public DropdownMenu() {
    }

    public DropdownMenu(Component... components) {
        this();
        add(components);
    }

    public enum Origin {
        LEFT, TOP, RIGHT, BOTTOM, CENTER;
    }

    public void setOpen(boolean open) {
        set(ACTIVE_DESCRIPTOR, open);
    }

    public boolean isOpen() {
        return get(ACTIVE_DESCRIPTOR);
    }

    public void setOrigin(Origin... origin) {
        getElement().setProperty(ORIGIN, Stream.of(origin).map(Origin::name).map(String::toLowerCase).collect(Collectors.joining(" ")));
    }

    public Origin[] getOrigin() {
        return Stream.of(getElement().getProperty(ORIGIN).split(" ")).map(String::toUpperCase).map(Origin::valueOf).toArray(Origin[]::new);
    }

    public void setLabel(String label) {
        set(LABEL_DESCRIPTOR, label);
    }

    public String getLabel() {
        return get(LABEL_DESCRIPTOR);
    }
}
