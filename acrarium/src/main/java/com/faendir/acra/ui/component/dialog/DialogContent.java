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

package com.faendir.acra.ui.component.dialog;

import com.faendir.acra.ui.component.Card;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;

/**
 * @author lukas
 * @since 24.04.19
 */
@Tag("acrarium-dialog-content")
@JsModule("./elements/dialog-content.js")
public class DialogContent extends PolymerTemplate<Card.CardModel> implements com.faendir.acra.ui.component.HasSize, HasStyle, HasOrderedComponents {

    public void setHeader(HasElement header) {
        header.getElement().setAttribute("slot", "header");
        getElement().appendChild(header.getElement());
    }

    public void setNegative(HasElement negative) {
        negative.getElement().setAttribute("slot", "negative");
        getElement().appendChild(negative.getElement());
    }

    public void setPositive(HasElement positive) {
        positive.getElement().setAttribute("slot", "positive");
        getElement().appendChild(positive.getElement());
    }
}
