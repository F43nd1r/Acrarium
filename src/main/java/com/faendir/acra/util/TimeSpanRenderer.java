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

package com.faendir.acra.util;

import com.vaadin.ui.renderers.TextRenderer;
import elemental.json.Json;
import elemental.json.JsonValue;
import org.springframework.lang.Nullable;
import org.xbib.time.pretty.PrettyTime;

import java.time.LocalDateTime;
import java.util.Locale;

/**
 * @author Lukas
 * @since 26.05.2017
 */
public class TimeSpanRenderer extends TextRenderer {
    @Override
    public JsonValue encode(@Nullable Object value) {
        if (value instanceof LocalDateTime) {
            return Json.create(new PrettyTime(Locale.US).formatUnrounded((LocalDateTime) value));
        }
        return super.encode(value);
    }
}
