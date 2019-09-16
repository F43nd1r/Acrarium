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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;

/**
 * @author lukas
 * @since 24.04.19
 */
public class CardDialog extends Dialog implements HasSize, HasStyle {
    private final Div header;
    private final Div content;

    public CardDialog() {
        header = new Div();
        header.getStyle().set("padding", "1rem");
        header.getStyle().set("box-sizing", "border-box");
        header.getStyle().set("background-color", "var(--lumo-contrast-5pct)");
        header.getStyle().set("display", "inline-block");
        header.setWidth("100%");
        content = new Div();
        content.getStyle().set("padding", "1rem");
        content.getStyle().set("box-sizing", "border-box");
        content.getStyle().set("display", "inline-block");
        content.setSizeFull();
        super.add(header, content);
    }

    public CardDialog(Component... components) {
        this();
        add(components);
    }

    public void removeAll() {
        content.removeAll();
    }

    public void addComponentAtIndex(int index, Component component) {
        content.addComponentAtIndex(index, component);
    }

    public void addComponentAsFirst(Component component) {
        content.addComponentAsFirst(component);
    }

    public void add(Component... components) {
        content.add(components);
    }

    public void remove(Component... components) {
        content.remove(components);
    }

    public void setHeader(Component... components) {
        header.removeAll();
        header.add(components);
    }

    public void setHeaderColor(String textColor, String backgroundColor) {
        header.getStyle().set("color",textColor);
        header.getStyle().set("background-color", backgroundColor);
    }
}
