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

package com.faendir.acra.ui.view.base.layout;

import com.faendir.acra.ui.navigation.NavigationManager;
import com.vaadin.ui.Component;
import org.springframework.core.Ordered;
import org.springframework.lang.NonNull;

/**
 * @author lukas
 * @since 17.06.18
 */
public interface ComponentFactory<T> extends Ordered {
    Component createContent(@NonNull T t, @NonNull NavigationManager navigationManager);

    String getCaption();

    String getId();
}
