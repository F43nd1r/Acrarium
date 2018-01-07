package com.faendir.acra.ui.view.base;

import com.faendir.acra.util.Utils;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Label;

/**
 * @author Lukas
 * @since 18.12.2017
 */
public class ConfigurationLabel extends Label {
    public ConfigurationLabel(String username, String password) {
        super(String.format("Take note of the following ACRA configuration. It cannot be viewed later:<br><code>"
                            + "@AcraCore(reportFormat = StringFormat.JSON)<br>"
                            + "@AcraHttpSender(uri = \"%sreport\",<br>"
                            + "basicAuthLogin = \"%s\",<br>"
                            + "basicAuthPassword = \"%s\",<br>"
                            + "httpMethod = HttpSender.Method.POST)<br></code>", Utils.getUrlWithFragment(null), username, password), ContentMode.HTML);
    }
}
