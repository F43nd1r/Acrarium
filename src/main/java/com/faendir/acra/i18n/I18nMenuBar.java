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

import com.vaadin.server.Resource;
import com.vaadin.ui.MenuBar;
import org.springframework.lang.Nullable;
import org.vaadin.spring.i18n.I18N;
import org.vaadin.spring.i18n.support.Translatable;

import java.util.Locale;

/**
 * @author lukas
 * @since 16.08.18
 */
public class I18nMenuBar extends MenuBar implements Translatable {
    private final I18N i18n;

    public I18nMenuBar(I18N i18n) {
        this.i18n = i18n;
    }

    public I18nMenuItem addItem(Resource icon) {
        I18nMenuItem item = new I18nMenuItem(icon, null, i18n, Messages.BLANK);
        getItems().add(item);
        return item;
    }

    @Override
    public void updateMessageStrings(Locale locale) {
        getItems().stream().filter(Translatable.class::isInstance).map(Translatable.class::cast).forEach(translatable -> translatable.updateMessageStrings(locale));
    }

    public class I18nMenuItem extends MenuItem implements Translatable {
        private final I18N i18n;
        private final String captionId;
        private final Object[] params;

        private I18nMenuItem(@Nullable Resource icon, Command command, I18N i18n, String captionId, Object... params) {
            //noinspection ConstantConditions
            super(i18n.get(captionId, params), icon, command);
            this.i18n = i18n;
            this.captionId = captionId;
            this.params = params;
        }

        public I18nMenuItem addItem(Command command, String captionId, Object... params) {
            I18nMenuItem item = new I18nMenuItem(null, command, i18n, captionId, params);
            getChildren().add(item);
            return item;
        }

        @Override
        public void updateMessageStrings(Locale locale) {
            setText(i18n.get(captionId, params));
            if(getChildren() != null) {
                getChildren().stream().filter(Translatable.class::isInstance).map(Translatable.class::cast).forEach(translatable -> translatable.updateMessageStrings(locale));
            }
        }
    }
}
