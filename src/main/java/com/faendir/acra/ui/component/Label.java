package com.faendir.acra.ui.component;

/**
 * @author lukas
 * @since 19.11.18
 */
public class Label extends com.vaadin.flow.component.html.Label {
    public Label() {
    }

    public Label(String text) {
        super(text);
    }

    public Label secondary() {
        getStyle().set("color", "var(--lumo-secondary-text-color)");
        return this;
    }

    public Label honorWhitespaces() {
        getStyle().set("white-space","pre");
        return this;
    }
}
