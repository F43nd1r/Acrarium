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

package com.faendir.acra.i18n;

import com.vaadin.ui.Accordion;
import com.vaadin.ui.Component;
import com.vaadin.ui.TabSheet;
import org.springframework.data.util.Pair;
import org.vaadin.spring.i18n.I18N;
import org.vaadin.spring.i18n.support.Translatable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author lukas
 * @since 16.08.18
 */
public class I18nAccordion extends Accordion implements Translatable {
    private final I18N i18n;
    private final Map<TabSheet.Tab, Pair<String, Object[]>> tabCaptionIds;

    public I18nAccordion(I18N i18n) {
        this.i18n = i18n;
        tabCaptionIds = new HashMap<>();
    }

    public Tab addTab(Component c, String captionId, Object... parameters) {
        Tab tab = super.addTab(c, i18n.get(captionId, parameters));
        tabCaptionIds.put(tab, Pair.of(captionId, parameters));
        return tab;
    }

    @Override
    public void updateMessageStrings(Locale locale) {
        tabCaptionIds.forEach((tab, pair) -> tab.setCaption(i18n.get(pair.getFirst(), locale, pair.getSecond())));
    }
}
