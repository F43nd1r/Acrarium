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
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.templatemodel.TemplateModel;

import java.util.stream.Stream;

/**
 * @author lukas
 * @since 18.10.18
 */
@Tag("acrarium-card")
@JsModule("./elements/card.js")
public class Card extends PolymerTemplate<Card.CardModel> implements HasSize, HasStyle, HasComponents {

    public Card() {
    }

    public Card(Component... components) {
        this();
        add(components);
    }

    public void setHeader(Component... components) {
        Stream.of(components).forEach(component -> component.getElement().setAttribute("slot", "header"));
        add(components);
    }

    public boolean allowsCollapse() {
        return getModel().getCanCollapse();
    }

    public void setAllowCollapse(boolean allowCollapse) {
        getModel().setCanCollapse(allowCollapse);
    }

    public void collapse() {
        getModel().setCollapse(true);
    }

    public void expand() {
        getModel().setCollapse(false);
    }

    public boolean isCollapsed() {
        return getModel().getCollapse();
    }

    public void enableDivider() {
        getModel().setDivider(true);
    }

    public void setHeaderColor(String textColor, String backgroundColor) {
        getStyle().set("--acrarium-card-header-text-color",textColor);
        getStyle().set("--acrarium-card-header-color", backgroundColor);
    }

    public void removeContent() {
        getChildren().filter(component -> component.getElement().getAttribute("slot") == null).forEach(this::remove);
    }

    public interface CardModel extends TemplateModel {
        void setCanCollapse(boolean collapse);
        boolean getCanCollapse();

        void setCollapse(boolean collapse);
        boolean getCollapse();

        void setDivider(boolean divider);
        boolean getDivider();
    }
}
