package com.faendir.acra.ui.component;

/**
 * @author lukas
 * @since 15.11.18
 */
public interface HasStyle extends com.vaadin.flow.component.HasStyle {
    default void preventWhiteSpaceBreaking() {
        getStyle().set("white-space", "nowrap");
    }
}
