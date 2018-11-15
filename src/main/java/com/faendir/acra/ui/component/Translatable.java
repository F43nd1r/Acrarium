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
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.RouterLink;
import org.springframework.lang.NonNull;

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
    public static Translatable<TextField> createTextField(@NonNull String initialValue, @NonNull String captionId, @NonNull Object... params) {
        return new Translatable<>(new TextField("", initialValue, ""), textField -> textField.setLabel(textField.getTranslation(captionId, params)));
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

    public static Translatable<Paragraph> createP(@NonNull String captionId, @NonNull Object... params) {
        return create(new Paragraph(), captionId, params);
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        setter.accept(getContent());
    }
}
