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

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.shared.Registration;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author lukas
 * @since 14.11.18
 */
public class Translatable<T extends Component> extends Composite<T> implements LocaleChangeObserver, HasSize, HasStyle {
    private final T t;
    private final Consumer<T> setter;

    protected Translatable(@NonNull T t, @NonNull Consumer<T> setter) {
        this.t = t;
        this.setter = setter;
    }

    @NonNull
    public static Translatable<Text> createText(@NonNull String captionId, @NonNull Object... params) {
        return create(new Text(""), captionId, params);
    }

    private static <T extends Component & HasText> Translatable<T> create(T component, @NonNull String captionId, @NonNull Object... params) {
        return new Translatable<>(component, text -> text.setText(text.getTranslation(captionId, params)));
    }

    @NonNull
    public static Translatable<Button> createButton(@NonNull String captionId, @NonNull Object... params) {
        return create(new Button(), captionId, params);
    }

    @NonNull
    public static Translatable<Button> createButton(@NonNull ComponentEventListener<ClickEvent<Button>> clickListener, @NonNull String captionId, @NonNull Object... params) {
        Button button = new Button("", clickListener);
        button.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        return create(button, captionId, params);
    }

    @NonNull
    public static ValidatedValue<TextField, ?, String> createTextField(@Nullable String initialValue, @NonNull String captionId, @NonNull Object... params) {
        return new ValidatedValue<>(new TextField("", initialValue == null ? "" : initialValue, ""), textField -> textField.setLabel(textField.getTranslation(captionId, params)));
    }

    @NonNull
    public static ValidatedValue<TextField, AbstractField.ComponentValueChangeEvent<TextField, String>, String> createTextFieldWithHint(@NonNull String captionId, @NonNull Object... params) {
        return new ValidatedValue<>(new TextField("", "",  ""), textField -> textField.setPlaceholder(textField.getTranslation(captionId, params)));
    }

    @NonNull
    public static ValidatedValue<PasswordField, ?, String> createPasswordField(@NonNull String captionId, @NonNull Object... params) {
        return new ValidatedValue<>(new PasswordField(), textField -> textField.setLabel(textField.getTranslation(captionId, params)));
    }

    @NonNull
    public static Translatable<TextArea> createTextArea(@NonNull String initialValue, @NonNull String captionId, @NonNull Object... params) {
        return new Translatable<>(new TextArea("", initialValue, ""), textField -> textField.setLabel(textField.getTranslation(captionId, params)));
    }

    public static <T> ValidatedValue<ComboBox<T>,?,  T> createComboBox(@NonNull Collection<T> items, @NonNull String captionId, @NonNull Object... params) {
        return new ValidatedValue<>(new ComboBox<>("", items), tComboBox -> tComboBox.setLabel(tComboBox.getTranslation(captionId, params)));
    }

    public static <T> Translatable<Select<T>> createSelect(@NonNull Collection<T> items, @NonNull String captionId, @NonNull Object... params) {
        Select<T> s = new Select<>();
        s.setItems(items);
        return new Translatable<>(s, select -> {
            select.setLabel(select.getTranslation(captionId, params));
            select.setItemLabelGenerator(select.getItemLabelGenerator());
        });
    }

    @NonNull
    public static Value<Checkbox, AbstractField.ComponentValueChangeEvent<Checkbox, Boolean>, Boolean> createCheckbox(boolean initialValue, @NonNull String captionId, @NonNull Object... params) {
        return new Value<>(new Checkbox(initialValue), checkbox -> checkbox.setLabel(checkbox.getTranslation(captionId, params)));
    }

    public static Translatable<RouterLink> createRouterLink(@NonNull Class<? extends Component> target, @NonNull String captionId, @NonNull Object... params) {
        return create(new RouterLink("", target), captionId, params);
    }

    public static <T extends Component & HasUrlParameter<P>, P> Translatable<RouterLink> createRouterLink(@NonNull Class<T> target, @NonNull P parameter, @NonNull String captionId, @NonNull Object... params) {
        return create(new RouterLink("", target, parameter), captionId, params);
    }

    public static Translatable<Label> createLabel(@NonNull String captionId, @NonNull Object... params) {
        return create(new Label(), captionId, params);
    }

    public static Translatable<H3> createH3(@NonNull String captionId, @NonNull Object... params) {
        return create(new H3(), captionId, params);
    }

    public static Translatable<Image> createImage(@NonNull String src, @NonNull String captionId, @NonNull Object... params) {
        return new Translatable<>(new Image(src, ""), image -> image.setAlt(image.getTranslation(captionId, params)));
    }

    public static ValidatedValue<NumberField, ?, Double> createNumberField(double initialValue, @NonNull String captionId, @NonNull Object... params) {
        return new ValidatedValue<>(new NumberField(null, initialValue, null), numberField -> numberField.setLabel(numberField.getTranslation(captionId, params)));
    }

    public static ValidatedValue<NumberField, AbstractField.ComponentValueChangeEvent<NumberField, Double>, Double> createNumberFieldWithHint(@NonNull String captionId, @NonNull Object... params) {
        return new ValidatedValue<>(new NumberField(), numberField -> numberField.setPlaceholder(numberField.getTranslation(captionId, params)));
    }

    public static ValidatedValue<UploadField, ?, String> createUploadField(@NonNull String captionId, @NonNull Object... params) {
        return new ValidatedValue<>(new UploadField(), uploadField -> uploadField.setLabel(uploadField.getTranslation(captionId, params)));
    }

    public static Translatable<Div> createDiv(@NonNull String captionId, @NonNull Object... params) {
        return new Translatable<>(new Div(), div -> {
            String translation = div.getTranslation(captionId, params);
            div.setText(translation);
        });
    }

    public static ValidatedValue<RangeField, ?, Double> createRangeField(@NonNull String captionId, @NonNull Object... params) {
        return new ValidatedValue<>(new RangeField(), rangeField -> rangeField.setLabel(rangeField.getTranslation(captionId, params)));
    }

    @Override
    protected T initContent() {
        return t;
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        setter.accept(getContent());
        fireEvent(new TranslatedEvent(this, false));
    }

    public Translatable<T> with(Consumer<T> consumer) {
        consumer.accept(t);
        return this;
    }

    public Registration addTranslatedListener(ComponentEventListener<TranslatedEvent> listener) {
        return addListener(TranslatedEvent.class, listener);
    }

    public static class Value<T extends Component & HasValue<E, V>, E extends AbstractField.ComponentValueChangeEvent<? super T, V>, V> extends Translatable<T> implements HasValue<E, V> {
        protected Value(T t, Consumer<T> setter) {
            super(t, setter);
        }

        public V getValue() {
            return getContent().getValue();
        }

        public void setValue(V value) {
            getContent().setValue(value);
        }

        public Registration addValueChangeListener(HasValue.ValueChangeListener<? super E> listener) {
            return getContent().addValueChangeListener(listener);
        }

        public V getEmptyValue() {
            return getContent().getEmptyValue();
        }

        public Optional<V> getOptionalValue() {
            return getContent().getOptionalValue();
        }

        public boolean isEmpty() {
            return getContent().isEmpty();
        }

        public void clear() {
            getContent().clear();
        }

        public boolean isReadOnly() {
            return getContent().isReadOnly();
        }

        public void setReadOnly(boolean readOnly) {
            getContent().setReadOnly(readOnly);
        }

        public boolean isRequiredIndicatorVisible() {
            return getContent().isRequiredIndicatorVisible();
        }

        public void setRequiredIndicatorVisible(boolean requiredIndicatorVisible) {
            getContent().setRequiredIndicatorVisible(requiredIndicatorVisible);
        }

        @Override
        public Value<T, E, V> with(Consumer<T> consumer) {
            return (Value<T, E, V>) super.with(consumer);
        }
    }

    public static class ValidatedValue<T extends Component & HasValue<E, V> & com.vaadin.flow.component.HasValidation, E extends AbstractField.ComponentValueChangeEvent<? super T, V>, V> extends Translatable.Value<T, E, V> implements HasValidation<T, E, V> {

        protected ValidatedValue(T t, Consumer<T> setter) {
            super(t, setter);
        }

        public String getErrorMessage() {
            return getContent().getErrorMessage();
        }

        public void setErrorMessage(String errorMessage) {
            getContent().setErrorMessage(errorMessage);
        }

        public boolean isInvalid() {
            return getContent().isInvalid();
        }

        public void setInvalid(boolean invalid) {
            getContent().setInvalid(invalid);
        }

        @Override
        public ValidatedValue<T, E, V> with(Consumer<T> consumer) {
            return (ValidatedValue<T, E, V>) super.with(consumer);
        }
    }

    public static class TranslatedEvent extends ComponentEvent<Translatable<?>> {

        public TranslatedEvent(Translatable<?> source, boolean fromClient) {
            super(source, fromClient);
        }
    }
}
