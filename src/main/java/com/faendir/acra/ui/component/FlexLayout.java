package com.faendir.acra.ui.component;

import com.vaadin.flow.component.Component;
import org.springframework.lang.NonNull;

/**
 * @author lukas
 * @since 14.11.18
 */
public class FlexLayout extends com.vaadin.flow.component.orderedlayout.FlexLayout implements HasSize, HasStyle {
    public FlexLayout(Component... components) {
        super(components);
    }

    public FlexLayout() {
    }

    public void setFlexDirection(FlexDirection flexDirection) {
        getStyle().set("flex-direction", flexDirection.value);
    }

    public void setFlexWrap(FlexWrap flexWrap) {
        getStyle().set("flex-wrap", flexWrap.value);
    }

    public enum FlexWrap {
        WRAP("wrap");
        private final String value;

        FlexWrap(@NonNull String value) {
            this.value = value;
        }
    }

    public enum FlexDirection {
        COLUMN("column");
        private final String value;

        FlexDirection(@NonNull String value) {
            this.value = value;
        }
    }
}
