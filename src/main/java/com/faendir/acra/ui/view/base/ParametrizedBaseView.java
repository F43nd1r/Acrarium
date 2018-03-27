package com.faendir.acra.ui.view.base;

import com.vaadin.navigator.ViewChangeListener;
import org.springframework.lang.NonNull;

import java.util.function.Function;

/**
 * @author Lukas
 * @since 12.12.2017
 */
public abstract class ParametrizedBaseView<T> extends BaseView {
    private Function<ViewChangeListener.ViewChangeEvent, T> parameterParser;

    @Override
    public final void enter(ViewChangeListener.ViewChangeEvent event) {
        enter(parameterParser.apply(event));
    }

    protected abstract void enter(@NonNull T parameter);

    public void setParameterParser(Function<ViewChangeListener.ViewChangeEvent, T> parameterParser) {
        this.parameterParser = parameterParser;
    }
}
