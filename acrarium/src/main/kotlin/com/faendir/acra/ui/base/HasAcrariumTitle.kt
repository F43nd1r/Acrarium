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
package com.faendir.acra.ui.base

import com.faendir.acra.i18n.Messages
import com.faendir.acra.security.SecurityUtils.isLoggedIn
import com.vaadin.flow.component.UI
import com.vaadin.flow.i18n.LocaleChangeEvent
import com.vaadin.flow.i18n.LocaleChangeObserver
import com.vaadin.flow.router.HasDynamicTitle

/**
 * @author lukas
 * @since 06.09.19
 */
interface HasAcrariumTitle : HasDynamicTitle, LocaleChangeObserver {
    val title: TranslatableText
    override fun getPageTitle(): String {
        var result = TranslatableText(Messages.ACRARIUM).translate()
        if (isLoggedIn()) {
            result = title.translate() + " - " + result
        }
        return result
    }

    override fun localeChange(event: LocaleChangeEvent) {
        UI.getCurrent().page.setTitle(pageTitle)
    }
}