package com.faendir.acra.ui.component;

import org.springframework.lang.NonNull;

/**
 * @author lukas
 * @since 14.11.18
 */
public class FlexLayout extends com.vaadin.flow.component.orderedlayout.FlexLayout implements HasSize, HasStyle {
    public enum FLEX_WRAP {
        WRAP("wrap");
        private final String value;

        FLEX_WRAP(@NonNull String value) {
            this.value = value;
        }
    }

    public enum ALIGN_CONTENT {
        CENTER("center");
        private final String value;

        ALIGN_CONTENT(String value) {
            this.value = value;
        }
    }

    public void setFlexWrap(FLEX_WRAP flexWrap) {
        getStyle().set("flex-wrap", flexWrap.value);
    }

    public void setAlignContent(ALIGN_CONTENT alignContent){
        getStyle().set("align-content", alignContent.value);
    }
}
