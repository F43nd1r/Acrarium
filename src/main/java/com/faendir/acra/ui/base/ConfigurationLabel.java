package com.faendir.acra.ui.base;

import com.faendir.acra.i18n.Messages;
import com.faendir.acra.rest.RestReportInterface;
import com.faendir.acra.ui.view.Overview;
import com.faendir.acra.util.PlainTextUser;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.i18n.LocaleChangeEvent;
import com.vaadin.flow.i18n.LocaleChangeObserver;
import org.springframework.lang.NonNull;

/**
 * @author lukas
 * @since 09.11.18
 */
public class ConfigurationLabel extends Text implements LocaleChangeObserver {
    private final PlainTextUser user;

    public ConfigurationLabel(@NonNull PlainTextUser user) {
        super("");
        this.user = user;
    }

    @Override
    public void localeChange(LocaleChangeEvent event) {
        setText(getTranslation(Messages.CONFIGURATION_LABEL, UI.getCurrent().getRouter().getUrl(Overview.class), RestReportInterface.REPORT_PATH, user.getUsername(), user.getPlaintextPassword()));
    }
}
