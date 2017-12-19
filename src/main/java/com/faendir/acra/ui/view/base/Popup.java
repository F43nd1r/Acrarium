package com.faendir.acra.ui.view.base;

import com.faendir.acra.util.Style;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
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
public class Popup extends Window {
    private final List<Component> components;
    private final Map<ValidatedField<?, ?>, Pair<Boolean, ValidatedField.Listener>> fields;
    private final List<Button> buttons;

    public Popup() {
        components = new ArrayList<>();
        fields = new HashMap<>();
        buttons = new ArrayList<>();
    }

    @NonNull
    public Popup setTitle(@NonNull String title) {
        super.setCaption(title);
        return this;
    }

    @NonNull
    public Popup addCreateButton(@NonNull Consumer<Popup> onCreateAction) {
        buttons.add(new Button("Create", event -> onCreateAction.accept(this)));
        return this;
    }

    @NonNull
    public Popup addCloseButton() {
        buttons.add(new Button("Close", event -> close()));
        return this;
    }

    @NonNull
    public Popup addYesNoButtons(@NonNull Consumer<Popup> onYesAction) {
        buttons.add(new Button("Yes", event -> onYesAction.accept(this)));
        buttons.add(new Button("No", event -> close()));
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
            buttons.forEach(buttonLayout::addComponent);
            buttons.forEach(button -> button.setWidth(100, Unit.PERCENTAGE));
        }
        components.forEach(component -> component.setWidth(100, Unit.PERCENTAGE));
        FormLayout layout = new FormLayout();
        components.forEach(layout::addComponent);
        checkValid();
        Style.apply(layout, Style.PADDING_LEFT, Style.PADDING_RIGHT);
        setContent(layout);
        center();
        if (!isAttached()) {
            UI.getCurrent().addWindow(this);
        }
    }

    private void checkValid() {
        boolean valid = fields.values().stream().map(Pair::getFirst).reduce(Boolean::logicalAnd).orElse(true);
        buttons.forEach(button -> button.setEnabled(valid));
    }
}
