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

package com.faendir.acra.ui.base.popup;

import com.faendir.acra.i18n.Messages;
import com.faendir.acra.ui.component.Translatable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.springframework.data.util.Pair;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Lukas
 * @since 19.12.2017
 */
public class Popup extends Dialog {
    private final List<Component> components;
    private final Map<ValidatedField<?, ?>, Pair<Boolean, ValidatedField.Listener>> fields;
    private final List<Translatable<Button>> buttons;

    public Popup() {
        components = new ArrayList<>();
        fields = new HashMap<>();
        buttons = new ArrayList<>();
    }

    @NonNull
    public Popup setTitle(@NonNull String titleId, Object... params) {
        components.add(0, Translatable.createText(titleId, params));
        return this;
    }

    @NonNull
    public Popup addCreateButton(@NonNull Consumer<Popup> onCreateAction) {
        return addCreateButton(onCreateAction, false);
    }

    @NonNull
    public Popup addCreateButton(@NonNull Consumer<Popup> onCreateAction, boolean closeAfter) {
        buttons.add(Translatable.createButton(event -> {
            onCreateAction.accept(this);
            if (closeAfter) {
                close();
            }
        }, Messages.CREATE));
        return this;
    }

    @NonNull
    public Popup addCloseButton() {
        buttons.add(Translatable.createButton(event -> close(), Messages.CLOSE));
        return this;
    }

    @NonNull
    public Popup addYesNoButtons(@NonNull Consumer<Popup> onYesAction) {
        return addYesNoButtons(onYesAction, false);
    }

    @NonNull
    public Popup addYesNoButtons(@NonNull Consumer<Popup> onYesAction, boolean closeAfter) {
        buttons.add(Translatable.createButton(event -> {
            onYesAction.accept(this);
            if (closeAfter) {
                close();
            }
        }, Messages.YES));
        buttons.add(Translatable.createButton(event -> close(), Messages.NO));
        return this;
    }

    @NonNull
    public Popup addComponent(@NonNull Component component) {
        components.add(component);
        return this;
    }

    public Popup addValidatedField(@NonNull ValidatedField<?, ?> validatedField) {
        return addValidatedField(validatedField, false);
    }

    public Popup addValidatedField(@NonNull ValidatedField<?, ?> validatedField, boolean isInitialValid) {
        ValidatedField.Listener listener = value -> updateField(validatedField, value);
        validatedField.addListener(listener);
        fields.put(validatedField, Pair.of(isInitialValid, listener));
        return addComponent(validatedField.getField());
    }

    private void updateField(@NonNull ValidatedField<?, ?> field, boolean value) {
        fields.put(field, Pair.of(value, fields.get(field).getSecond()));
        checkValid();
    }

    public Popup clear() {
        components.clear();
        fields.forEach((field, booleanListenerPair) -> field.removeListener(booleanListenerPair.getSecond()));
        fields.clear();
        buttons.clear();
        return this;
    }

    public void show() {
        if (buttons.size() == 1) {
            components.add(buttons.get(0));
        } else if (buttons.size() > 1) {
            HorizontalLayout buttonLayout = new HorizontalLayout();
            components.add(buttonLayout);
            buttons.forEach(buttonLayout::add);
            buttons.forEach(com.faendir.acra.ui.component.HasSize::setWidthFull);
        }
        components.forEach(component -> {
            if(component instanceof HasSize) {
                try {
                    ((HasSize)component).setWidth("100%");
                }catch (UnsupportedOperationException e) {
                }
            }
        });
        FormLayout layout = new FormLayout();
        layout.setResponsiveSteps(new FormLayout.ResponsiveStep("0px", 1));
        components.forEach(c -> layout.addFormItem(c, ""));
        checkValid();
        removeAll();
        add(layout);
        if (!isOpened()) {
            open();
        }
    }

    private void checkValid() {
        boolean valid = fields.values().stream().map(Pair::getFirst).reduce(Boolean::logicalAnd).orElse(true);
        buttons.forEach(button -> button.getContent().setEnabled(valid));
    }
}
