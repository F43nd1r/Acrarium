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

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.RouterLink;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Collection;
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

    @Override
    protected T initContent() {
        return t;
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        setter.accept(getContent());
    }

    public Translatable<T> with(Consumer<T> consumer) {
        consumer.accept(t);
        return this;
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
        return create(new Button("", clickListener), captionId, params);
    }

    @NonNull
    public static Translatable<TextField> createTextField(@Nullable String initialValue, @NonNull String captionId, @NonNull Object... params) {
        return new Translatable<>(new TextField("", initialValue == null ? "" : initialValue, ""), textField -> textField.setLabel(textField.getTranslation(captionId, params)));
    }

    @NonNull
    public static Translatable<PasswordField> createPasswordField(@NonNull String captionId, @NonNull Object... params) {
        return new Translatable<>(new PasswordField(), textField -> textField.setLabel(textField.getTranslation(captionId, params)));
    }

    @NonNull
    public static Translatable<TextArea> createTextArea(@NonNull String initialValue, @NonNull String captionId, @NonNull Object... params) {
        return new Translatable<>(new TextArea("", initialValue, ""), textField -> textField.setLabel(textField.getTranslation(captionId, params)));
    }

    public static <T> Translatable<ComboBox<T>> createComboBox(@NonNull Collection<T> items, @NonNull String captionId, @NonNull Object... params) {
        return new Translatable<>(new ComboBox<>("", items), tComboBox -> tComboBox.setLabel(tComboBox.getTranslation(captionId, params)));
    }

    @NonNull
    public static Translatable<Checkbox> createCheckbox(boolean initialValue, @NonNull String captionId, @NonNull Object... params) {
        return new Translatable<>(new Checkbox(initialValue), checkbox -> checkbox.setLabel(checkbox.getTranslation(captionId, params)));
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

    public static Translatable<Image> createImage(@NonNull String src, @NonNull String captionId, @NonNull Object... params) {
        return new Translatable<>(new Image(src, ""), image -> image.setAlt(image.getTranslation(captionId, params)));
    }

    public static Translatable<Div> createDiv(@NonNull String captionId, @NonNull Object... params) {
        return new Translatable<>(new Div(), div -> {
            String translation = div.getTranslation(captionId, params);
            div.setText(translation);
        });
    }
}
