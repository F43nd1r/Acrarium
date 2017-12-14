package com.faendir.acra.ui.view.base;

import com.faendir.acra.sql.model.App;
import com.vaadin.navigator.ViewChangeListener;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.function.Function;

/**
 * @author Lukas
 * @since 12.12.2017
 */
public abstract class ParametrizedNamedView<T> extends NamedView {
    private final Function<T, App> converter;
    private T t;

    protected ParametrizedNamedView(Function<T, App> converter) {
        this.converter = converter;
    }

    @Nullable
    protected abstract T validateAndParseFragment(@NonNull String fragment);

    @NonNull
    public final App getApp() {
        return converter.apply(t);
    }

    public final boolean validate(@Nullable String fragment) {
        if (fragment == null) return false;
        t = validateAndParseFragment(fragment);
        return t != null;
    }

    @Override
    public final void enter(ViewChangeListener.ViewChangeEvent event) {
        enter(t);
    }

    protected abstract void enter(@NonNull T t);
}
