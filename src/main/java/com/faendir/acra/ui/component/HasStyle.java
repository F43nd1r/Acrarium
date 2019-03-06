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

/**
 * @author lukas
 * @since 15.11.18
 */
public interface HasStyle extends com.vaadin.flow.component.HasStyle {
    default void preventWhiteSpaceBreaking() {
        getStyle().set("white-space", "nowrap");
    }

    default void setMargin(int value, HasSize.Unit unit) {
        getStyle().set("margin", value + unit.getText());
    }

    default void setDefaultTextStyle() {
        getStyle().set("text-decoration","none");
        getStyle().set("color","inherit");
    }

    default void setPadding(double value, HasSize.Unit unit) {
        getStyle().set("padding", value + unit.getText());
    }

    default void setPaddingLeft(double value, HasSize.Unit unit) {
        getStyle().set("padding-left", value + unit.getText());
    }

    default void setPaddingTop(double value, HasSize.Unit unit) {
        getStyle().set("padding-top", value + unit.getText());
    }

    default void setPaddingRight(double value, HasSize.Unit unit) {
        getStyle().set("padding-right", value + unit.getText());
    }

    default void setPaddingBottom(double value, HasSize.Unit unit) {
        getStyle().set("padding-bottom", value + unit.getText());
    }
}
