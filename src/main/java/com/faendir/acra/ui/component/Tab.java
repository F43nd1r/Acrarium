package com.faendir.acra.ui.component;

import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import org.springframework.lang.NonNull;

/**
 * @author lukas
 * @since 15.11.18
 */
public class Tab extends com.vaadin.flow.component.tabs.Tab implements LocaleChangeObserver {
    private final String captionId;
    private final Object[] params;

    public Tab(@NonNull String captionId, @NonNull Object... params) {

        this.captionId = captionId;
        this.params = params;
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        setLabel(getTranslation(captionId, params));
    }
}
