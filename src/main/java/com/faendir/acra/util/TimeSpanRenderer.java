package com.faendir.acra.util;

import com.vaadin.ui.renderers.TextRenderer;
import elemental.json.Json;
import elemental.json.JsonValue;
import org.springframework.lang.Nullable;
import org.xbib.time.pretty.PrettyTime;

import java.time.LocalDateTime;

/**
 * @author Lukas
 * @since 26.05.2017
 */
public class TimeSpanRenderer extends TextRenderer {
    @Override
    public JsonValue encode(@Nullable Object value) {
        if (value instanceof LocalDateTime) {
            return Json.create(new PrettyTime().formatUnrounded((LocalDateTime) value));
        }
        return super.encode(value);
    }
}
