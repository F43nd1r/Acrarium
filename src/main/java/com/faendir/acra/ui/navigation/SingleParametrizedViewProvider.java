package com.faendir.acra.ui.navigation;

import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.model.App;
import com.faendir.acra.ui.annotation.RequiresAppPermission;
import com.faendir.acra.ui.view.base.ParametrizedBaseView;

/**
 * @author Lukas
 * @since 24.03.2018
 */
public abstract class SingleParametrizedViewProvider<T, V extends ParametrizedBaseView<T>> extends SingleViewProvider<V> {
    public SingleParametrizedViewProvider(Class<V> clazz) {
        super(clazz);
    }

    @Override
    protected boolean isValidParameter(String parameter) {
        T t = parseParameter(parameter);
        RequiresAppPermission annotation;
        return isValidParameter(t) && ((annotation = getClazz().getAnnotation(RequiresAppPermission.class)) == null || SecurityUtils.hasPermission(toApp(t), annotation.value()));
    }

    @Override
    public V getView(String viewName) {
        V view = super.getView(viewName);
        if (view != null) {
            view.setParameterParser(e -> {
                String parameters = getParameters(
                        e.getViewName().substring(e.getViewName().indexOf(getId())) + (e.getParameters().isEmpty() ? "" : MyNavigator.SEPARATOR_CHAR + e.getParameters()));
                return parameters != null ? parseParameter(parameters) : null;
            });
        }
        return view;
    }

    @Override
    public final String getTitle(String parameter) {
        return getTitle(parseParameter(parameter));
    }

    protected abstract String getTitle(T parameter);

    protected abstract boolean isValidParameter(T parameter);

    protected abstract T parseParameter(String parameter);

    protected abstract App toApp(T parameter);
}
