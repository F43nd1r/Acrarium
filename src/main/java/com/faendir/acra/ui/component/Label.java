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
 * @since 19.11.18
 */
public class Label extends com.vaadin.flow.component.html.Label {
    public Label() {
    }

    public Label(String text) {
        super(text);
    }

    public Label secondary() {
        getStyle().set("color", "var(--lumo-secondary-text-color)");
        return this;
    }

    public Label honorWhitespaces() {
        getStyle().set("white-space","pre");
        return this;
    }
}
