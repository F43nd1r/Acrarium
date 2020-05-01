/*
 * (C) Copyright 2019 Lukas Morawietz (https://github.com/F43nd1r)
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

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasValue;

/**
 * @author lukas
 * @since 01.03.19
 */
public interface HasValidation<T extends Component, E extends AbstractField.ComponentValueChangeEvent<? super T, V>, V> extends com.vaadin.flow.component.HasValidation, HasValue<E, V> {

    default void invalidateWithMessage(String errorMessageId, Object... params) {
        setErrorMessage(getTranslation(errorMessageId, params));
        setInvalid(true);
        addValueChangeListener(event -> {
            setInvalid(false);
            event.unregisterListener();
        });
    }

    String getTranslation(String key, Object... params);
}
