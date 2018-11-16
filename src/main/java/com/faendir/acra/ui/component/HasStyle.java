package com.faendir.acra.ui.component;

/**
 * @author lukas
 * @since 15.11.18
 */
public interface HasStyle extends com.vaadin.flow.component.HasStyle {
    default void preventWhiteSpaceBreaking() {
        getStyle().set("white-space", "nowrap");
    }

    default void setMargin(int value, HasSize.Unit unit) {
        getStyle().set("margin", value + unit.getText());
    }

    default void setDefaultTextStyle() {
        getStyle().set("text-decoration","none");
        getStyle().set("color","inherit");
    }

    default void setPadding(int value, HasSize.Unit unit) {
        getStyle().set("padding", value + unit.getText());
    }
}
