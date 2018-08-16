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
package com.faendir.acra.ui.navigation;

import com.faendir.acra.model.App;
import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.ui.annotation.RequiresAppPermission;
import com.faendir.acra.ui.view.base.navigation.ParametrizedBaseView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Lukas
 * @since 24.03.2018
 */
public abstract class SingleParametrizedViewProvider<T, V extends ParametrizedBaseView<T>> extends SingleViewProvider<V> {
    private String lastParameter;
    private T lastT;

    public SingleParametrizedViewProvider(Class<V> clazz) {
        super(clazz);
    }

    @Override
    protected boolean isValidParameter(String parameter) {
        T t = parseParameterInternal(parameter);
        RequiresAppPermission annotation;
        return isValidParameter(t) && ((annotation = getClazz().getAnnotation(RequiresAppPermission.class)) == null || SecurityUtils.hasPermission(toApp(t), annotation.value()));
    }

    @Override
    public V getView(String viewName) {
        V view = super.getView(viewName);
        if (view != null) {
            view.setParameterParser(e -> {
                String parameters = null;
                Matcher matcher = Pattern.compile("(^|/)(" + getId() + ")($|/)").matcher(e.getViewName());
                if (matcher.find()) {
                    parameters = getParameters(e.getViewName().substring(matcher.start(2)) + (e.getParameters().isEmpty() ? "" : MyNavigator.SEPARATOR_CHAR + e.getParameters()));
                }
                return parameters != null ? parseParameterInternal(parameters) : null;
            });
        }
        return view;
    }

    @Override
    public final String getTitle(String parameter) {
        return getTitle(parseParameterInternal(parameter));
    }

    protected abstract String getTitle(T parameter);

    protected abstract boolean isValidParameter(T parameter);

    protected abstract T parseParameter(String parameter);

    protected abstract App toApp(T parameter);

    private T parseParameterInternal(String parameter) {
        if (parameter.equals(lastParameter)) {
            return lastT;
        }
        T t = parseParameter(parameter);
        lastParameter = parameter;
        lastT = t;
        return t;
    }
}
