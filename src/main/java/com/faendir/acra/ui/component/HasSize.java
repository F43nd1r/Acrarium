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

import com.vaadin.flow.component.HasStyle;

/**
 * @author lukas
 * @since 14.11.18
 */
public interface HasSize extends com.vaadin.flow.component.HasSize, HasStyle {
    enum Unit {
        PERCENTAGE("%"),
        PIXEL("px"),
        REM("rem"),
        EM("em");
        private final String text;

        Unit(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }

    default void setWidthFull() {
        setWidth(100, Unit.PERCENTAGE);
    }

    default void setWidth(int value, Unit unit) {
        setWidth(value + unit.getText());
    }

    default void setMaxWidth(int value, Unit unit) {
        getStyle().set("max-width", value + unit.getText());
    }

    default void setMaxWidthFull(){
        setMaxWidth(100, Unit.PERCENTAGE);
    }

    default void setMinWidth(int value, Unit unit) {
        getStyle().set("min-width", value + unit.getText());
    }

    default void setMinWidthFull(){
        setMinWidth(100, Unit.PERCENTAGE);
    }

    default void setHeightFull() {
        setHeight(100, Unit.PERCENTAGE);
    }

    default void setHeight(int value, Unit unit) {
        setHeight(value + unit.getText());
    }

    default void setMaxHeight(int value, Unit unit) {
        getStyle().set("max-height", value + unit.getText());
    }

    default void setMaxHeightFull(){
        setMaxHeight(100, Unit.PERCENTAGE);
    }

    default void setMinHeight(int value, Unit unit) {
        getStyle().set("min-height", value + unit.getText());
    }

    default void setMinHeightFull(){
        setMinHeight(100, Unit.PERCENTAGE);
    }
}
