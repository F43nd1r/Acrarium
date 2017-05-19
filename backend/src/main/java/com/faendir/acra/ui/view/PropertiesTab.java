package com.faendir.acra.ui.view;

import com.faendir.acra.data.App;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

/**
 * @author Lukas
 * @since 19.05.2017
 */
public class PropertiesTab extends VerticalLayout {
    public PropertiesTab(App app) {
        String location = UI.getCurrent().getPage().getLocation().toASCIIString();
        location = location.substring(0, location.indexOf('#'));
        addComponent(new Label(String.format("Required ACRA configuration:<br><code>formUri = \"%sreport\",<br>" +
                        "formUriBasicAuthLogin = \"%s\",<br>formUriBasicAuthPassword = \"%s\",<br>" +
                        "httpMethod = HttpSender.Method.POST,<br>reportType = HttpSender.Type.JSON</code>",
                location, app.getId(), app.getPassword()), ContentMode.HTML));
        setCaption("Properties");
        setSizeFull();
    }
}
