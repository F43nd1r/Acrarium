package com.faendir.acra.ui.view.base;

import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;

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
                            + "httpMethod = HttpSender.Method.POST)<br></code>", getLocation(), username, password), ContentMode.HTML);
    }

    private static String getLocation() {
        String location = UI.getCurrent().getPage().getLocation().toASCIIString();
        return location.substring(0, location.indexOf('#'));
    }
}
