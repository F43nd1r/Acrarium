package com.faendir.acra.ui.component;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.server.AbstractStreamResource;
import org.springframework.lang.NonNull;

/**
 * @author lukas
 * @since 15.11.18
 */
public class DownloadButton extends Anchor {
    public DownloadButton(@NonNull AbstractStreamResource href, @NonNull String captionId, @NonNull Object... params) {
        super(href, "");
        getElement().setAttribute("download", true);
        add(Translatable.createButton(captionId, params));
    }
}
