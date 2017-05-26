package com.faendir.acra.util;

import com.vaadin.ui.renderers.TextRenderer;
import elemental.json.Json;
import elemental.json.JsonValue;

import java.util.Date;

/**
 * @author Lukas
 * @since 26.05.2017
 */
public class TimeSpanRenderer extends TextRenderer {
    public TimeSpanRenderer() {
    }

    @Override
    public JsonValue encode(Object value) {
        if (value == null || !(value instanceof Date)) {
            return super.encode(null);
        }
        return Json.create(StringUtils.distanceFromNowAsString((Date) value));
    }
}
