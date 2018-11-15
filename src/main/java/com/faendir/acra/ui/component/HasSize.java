package com.faendir.acra.ui.component;

import com.vaadin.flow.component.HasStyle;

/**
 * @author lukas
 * @since 14.11.18
 */
public interface HasSize extends com.vaadin.flow.component.HasSize, HasStyle {
    enum Unit {
        PERCENTAGE("%"),
        PIXEL("px");
        private final String text;

        Unit(String text) {
            this.text = text;
        }
    }

    default void setWidthFull() {
        setWidth(100, Unit.PERCENTAGE);
    }

    default void setWidth(int value, Unit unit) {
        setWidth(value + unit.text);
    }

    default void setMaxWidth(int value, Unit unit) {
        getStyle().set("max-width", value + unit.text);
    }

    default void setMaxWidthFull(){
        setMaxWidth(100, Unit.PERCENTAGE);
    }

    default void setHeightFull() {
        setHeight(100, Unit.PERCENTAGE);
    }

    default void setHeight(int value, Unit unit) {
        setHeight(value + unit.text);
    }
}
