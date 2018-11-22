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
import org.springframework.lang.NonNull;

/**
 * @author lukas
 * @since 14.11.18
 */
public class FlexLayout extends com.vaadin.flow.component.orderedlayout.FlexLayout implements HasSize, HasStyle {
    public FlexLayout(Component... components) {
        super(components);
    }

    public FlexLayout() {
    }

    public void setFlexDirection(FlexDirection flexDirection) {
        getStyle().set("flex-direction", flexDirection.value);
    }

    public void setFlexWrap(FlexWrap flexWrap) {
        getStyle().set("flex-wrap", flexWrap.value);
    }

    public enum FlexWrap {
        WRAP("wrap");
        private final String value;

        FlexWrap(@NonNull String value) {
            this.value = value;
        }
    }

    public enum FlexDirection {
        COLUMN("column"),
        ROW("row");
        private final String value;

        FlexDirection(@NonNull String value) {
            this.value = value;
        }
    }
}
