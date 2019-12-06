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

package com.faendir.acra.ui.component.dialog;

import com.faendir.acra.i18n.Messages;
import com.faendir.acra.ui.component.FlexLayout;
import com.faendir.acra.ui.component.Translatable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
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
public class FluentDialog extends AcrariumDialog {
    private final List<Component> components;
    private final Map<ValidatedField<?, ?>, Pair<Boolean, ValidatedField.Listener>> fields;

    public FluentDialog() {
        components = new ArrayList<>();
        fields = new HashMap<>();
    }

    @NonNull
    public FluentDialog setTitle(@NonNull String titleId, Object... params) {
        setHeader(titleId, params);
        return this;
    }

    @NonNull
    public FluentDialog addCreateButton(@NonNull Consumer<FluentDialog> onCreateAction) {
        setPositive(event -> onCreateAction.accept(this), Messages.CREATE);
        setNegative(Messages.CANCEL);
        return this;
    }

    @NonNull
    public FluentDialog addCloseButton() {
        setPositive(e -> {
        }, Messages.CLOSE);
        return this;
    }

    @NonNull
    public FluentDialog addYesNoButtons(@NonNull Consumer<FluentDialog> onYesAction) {
        setPositive(event -> onYesAction.accept(this), Messages.YES);
        setNegative(Messages.NO);
        return this;
    }

    @NonNull
    public FluentDialog addConfirmButtons(@NonNull Consumer<FluentDialog> onYesAction) {
        setPositive(event -> onYesAction.accept(this), Messages.CONFIRM);
        setNegative(Messages.CANCEL);
        return this;
    }

    @NonNull
    public FluentDialog addComponent(@NonNull Component component) {
        components.add(component);
        return this;
    }

    @NonNull
    public FluentDialog addText(String captionId, Object... params) {
        components.add(Translatable.createText(captionId, params));
        return this;
    }

    public FluentDialog addValidatedField(@NonNull ValidatedField<?, ?> validatedField) {
        return addValidatedField(validatedField, false);
    }

    public FluentDialog addValidatedField(@NonNull ValidatedField<?, ?> validatedField, boolean isInitialValid) {
        ValidatedField.Listener listener = value -> updateField(validatedField, value);
        validatedField.addListener(listener);
        fields.put(validatedField, Pair.of(isInitialValid, listener));
        return addComponent(validatedField.getField());
    }

    private void updateField(@NonNull ValidatedField<?, ?> field, boolean value) {
        fields.put(field, Pair.of(value, fields.get(field).getSecond()));
        checkValid();
    }

    public void show() {
        components.forEach(component -> {
            if (component instanceof HasSize) {
                try {
                    ((HasSize) component).setWidth("100%");
                } catch (UnsupportedOperationException ignored) {
                }
            }
        });
        FlexLayout layout = new FlexLayout();
        layout.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        components.forEach(layout::add);
        checkValid();
        add(layout);
        if (!isOpened()) {
            open();
        }
    }

    private void checkValid() {
        boolean valid = fields.values().stream().map(Pair::getFirst).reduce(Boolean::logicalAnd).orElse(true);
        getPositive().ifPresent(button -> button.getContent().setEnabled(valid));
    }
}
