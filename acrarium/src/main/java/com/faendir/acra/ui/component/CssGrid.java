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
import com.vaadin.flow.component.html.Div;
import org.springframework.lang.NonNull;

/**
 * @author lukas
 * @since 19.11.18
 */
public class CssGrid extends Div implements HasSize, HasStyle {
    public CssGrid() {
        getStyle().set("display", "grid");
    }

    public CssGrid(Component... components) {
        this();
        add(components);
    }

    public void setTemplateColumns(String template) {
        getStyle().set("grid-template-columns", template);
    }

    public void setColumnGap(int size, Unit unit) {
        getStyle().set("grid-column-gap", size + unit.getText());
    }

    public void setJustifyItems(JustifyMode justifyMode) {
        getStyle().set("justify-items", justifyMode.value);
    }

    public void setAlignItems(AlignMode alignMode) {
        getStyle().set("align-items", alignMode.value);
    }

    public void alignItems(AlignMode alignMode, com.vaadin.flow.component.HasStyle... components) {
        for (com.vaadin.flow.component.HasStyle component : components) {
            component.getStyle().set("align-self", alignMode.value);
        }
    }

    public enum JustifyMode {
        START("start");
        private final String value;

        JustifyMode(@NonNull String value) {
            this.value = value;
        }
    }

    public enum AlignMode {
        CENTER("center"),
        FIRST_BASELINE("first baseline");
        private final String value;

        AlignMode(@NonNull String value) {
            this.value = value;
        }
    }
}
