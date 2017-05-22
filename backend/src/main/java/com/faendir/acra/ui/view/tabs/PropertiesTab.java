package com.faendir.acra.ui.view.tabs;

import com.faendir.acra.mongod.data.DataManager;
import com.faendir.acra.mongod.model.App;
import com.faendir.acra.ui.NavigationManager;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * @author Lukas
 * @since 19.05.2017
 */
public class PropertiesTab extends VerticalLayout {
    private final App app;
    private final DataManager dataManager;
    private final NavigationManager navigationManager;

    public PropertiesTab(App app, DataManager dataManager, NavigationManager navigationManager) {
        this.app = app;
        this.dataManager = dataManager;
        this.navigationManager = navigationManager;
        String location = UI.getCurrent().getPage().getLocation().toASCIIString();
        location = location.substring(0, location.indexOf('#'));
        addComponent(new Label(String.format("Required ACRA configuration:<br><code>formUri = \"%sreport\",<br>" +
                        "formUriBasicAuthLogin = \"%s\",<br>formUriBasicAuthPassword = \"%s\",<br>" +
                        "httpMethod = HttpSender.Method.POST,<br>reportType = HttpSender.Type.JSON</code>",
                location, app.getId(), app.getPassword()), ContentMode.HTML));
        addComponent(new Button("Delete App", e -> deleteApp()));
        setCaption("Properties");
        setSizeUndefined();
    }

    private void deleteApp() {
        Window window = new Window("Confirm");
        Label label = new Label("Are you sure you want to delete this app and all its reports and mappings?");
        Button yes = new Button("Yes", e -> {
            dataManager.deleteApp(app.getId());
            window.close();
            navigationManager.navigateBack();
        });
        Button no = new Button("No", e -> window.close());
        VerticalLayout layout = new VerticalLayout(label, new HorizontalLayout(yes, no));
        window.setContent(layout);
        window.center();
        UI.getCurrent().addWindow(window);
    }
}
