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

package com.faendir.acra.ui.base;

import com.faendir.acra.security.SecurityUtils;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;

/**
 * @author lukas
 * @since 19.11.18
 */
public
interface HasSecureStringParameter extends HasUrlParameter<String> {
    @Override
    default void setParameter(BeforeEvent event, String parameter) {
        if (SecurityUtils.isLoggedIn()) {
            setParameterSecure(event, parameter);
        }
    }

    void setParameterSecure(BeforeEvent event, String parameter);
}
