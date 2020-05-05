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
package com.faendir.acra.ui.component

import com.vaadin.flow.component.tabs.Tab
import com.vaadin.flow.i18n.LocaleChangeEvent
import com.vaadin.flow.i18n.LocaleChangeObserver
import org.springframework.lang.NonNull

/**
 * @author lukas
 * @since 15.11.18
 */
class Tab(private val captionId: String, private vararg val params: Any) : Tab(), LocaleChangeObserver {
    override fun localeChange(event: LocaleChangeEvent) {
        label = getTranslation(captionId, *params)
    }
}