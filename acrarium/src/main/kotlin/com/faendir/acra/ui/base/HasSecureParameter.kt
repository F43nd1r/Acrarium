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
package com.faendir.acra.ui.base

import com.faendir.acra.security.SecurityUtils.isLoggedIn
import com.faendir.acra.util.toNullable
import com.vaadin.flow.router.BeforeEnterEvent
import com.vaadin.flow.router.BeforeEnterObserver
import com.vaadin.flow.router.BeforeEvent

/**
 * @author lukas
 * @since 19.11.18
 */
interface HasSecureParameter<T> : BeforeEnterObserver {
    override fun beforeEnter(enterEvent: BeforeEnterEvent?) {
        enterEvent?.let { event ->
            if (isLoggedIn()) {
                event.routeParameters[PARAM].toNullable()?.let { setParameterSecure(event, parseParameter(it))}
            }
        }
    }

    fun parseParameter(parameter: String) : T

    fun setParameterSecure(event: BeforeEvent?, parameter: T)

    companion object {
        const val PARAM = "param"
    }
}