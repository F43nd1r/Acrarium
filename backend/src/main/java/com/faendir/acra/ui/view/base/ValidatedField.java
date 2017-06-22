package com.faendir.acra.ui.view.base;

import com.vaadin.server.UserError;
import com.vaadin.ui.AbstractField;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Lukas
 * @since 22.06.2017
 */
public class ValidatedField<V, T extends AbstractField<V>> {
    private final T field;
    private final Map<Function<V, Boolean>, String> validators;

    public ValidatedField(T field) {
        this.field = field;
        validators = new HashMap<>();
        field.addValueChangeListener(e -> validate(e.getValue()));
    }

    public ValidatedField<V, T> addValidator(Function<V, Boolean> validator, String errorMessage) {
        validators.put(validator, errorMessage);
        return this;
    }

    public T getField() {
        return field;
    }

    public boolean isValid() {
        return validate(field.getValue());
    }

    private boolean validate(V value) {
        return validators.entrySet().stream().allMatch(entry -> {
            if (entry.getKey().apply(value)) {
                field.setComponentError(null);
                return true;
            } else {
                field.setComponentError(new UserError(entry.getValue()));
                return false;
            }
        });
    }

    public V getValue() {
        return field.getValue();
    }
}
