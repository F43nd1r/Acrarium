package com.faendir.acra.util;

import com.vaadin.ui.renderers.TextRenderer;
import elemental.json.Json;
import elemental.json.JsonValue;
import org.ocpsoft.prettytime.PrettyTime;
import org.springframework.lang.Nullable;

import java.util.Date;

/**
 * @author Lukas
 * @since 26.05.2017
 */
public class TimeSpanRenderer extends TextRenderer {
    @Override
    public JsonValue encode(@Nullable Object value) {
        if (value instanceof Date) {
            return Json.create(new PrettyTime().format(((Date) value)));
        }
        return super.encode(value);
    }
}
