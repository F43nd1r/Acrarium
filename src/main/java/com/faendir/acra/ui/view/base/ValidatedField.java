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

package com.faendir.acra.ui.view.base;

import com.vaadin.server.UserError;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.AbstractField;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Lukas
 * @since 22.06.2017
 */
public class ValidatedField<V, T extends AbstractComponent> {
    private final T field;
    private final Supplier<V> valueSupplier;
    private final Map<Function<V, Boolean>, String> validators;
    private final List<Listener> listeners;
    private boolean valid;

    private ValidatedField(T field, Supplier<V> valueSupplier, Consumer<Consumer<V>> listenerRegistration) {
        this.field = field;
        this.valueSupplier = valueSupplier;
        this.validators = new HashMap<>();
        this.listeners = new ArrayList<>();
        this.valid = false;
        listenerRegistration.accept(this::validate);
    }

    public static <V, T extends AbstractField<V>> ValidatedField<V, T> of(T field) {
        return new ValidatedField<>(field, field::getValue, vConsumer -> field.addValueChangeListener(event -> vConsumer.accept(event.getValue())));
    }

    public static <V, T extends AbstractComponent> ValidatedField<V, T> of(T field, Supplier<V> valueSupplier, Consumer<Consumer<V>> listenerRegistration) {
        return new ValidatedField<>(field, valueSupplier, listenerRegistration);
    }

    public ValidatedField<V, T> addValidator(Function<V, Boolean> validator, String errorMessage) {
        validators.put(validator, errorMessage);
        return this;
    }

    public T getField() {
        return field;
    }

    public boolean isValid() {
        return validate(valueSupplier.get());
    }

    private boolean validate(V value) {
        boolean valid = validators.entrySet().stream().allMatch(entry -> {
            if (entry.getKey().apply(value)) {
                field.setComponentError(null);
                return true;
            } else {
                field.setComponentError(new UserError(entry.getValue()));
                return false;
            }
        });
        if (this.valid != valid) {
            this.valid = valid;
            listeners.forEach(listener -> listener.onValidationChanged(valid));
        }
        return valid;
    }

    public void addListener(@NonNull Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(@NonNull Listener listener) {
        listeners.remove(listener);
    }

    public interface Listener {
        void onValidationChanged(boolean value);
    }
}
