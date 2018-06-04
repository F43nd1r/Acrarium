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
