package com.faendir.acra.ui.view.tabs;

import com.faendir.acra.mongod.data.DataManager;
import com.faendir.acra.mongod.model.App;
import com.faendir.acra.ui.NavigationManager;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import org.vaadin.risto.stepper.IntStepper;

import java.util.Calendar;
import java.util.Date;

/**
 * @author Lukas
 * @since 19.05.2017
 */
public class PropertiesTab extends VerticalLayout {
    public static final String CAPTION = "Properties";
    private final App app;
    private final DataManager dataManager;
    private final NavigationManager navigationManager;
    private final IntStepper age;

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
        age = new IntStepper();
        age.setValue(30);
        age.setMinValue(0);
        HorizontalLayout purgeAge = new HorizontalLayout(new Button("Purge", e -> purge()), new Label("Reports older than "), age, new Label("Days"));
        purgeAge.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        addComponent(purgeAge);
        addComponent(new Button("Rebuild bugs", e -> rebuildBugs()));
        setCaption(CAPTION);
        setSizeUndefined();
    }

    private void rebuildBugs(){
        dataManager.rebuildBugs(app.getId());
    }

    private void purge() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -age.getValue());
        Date keepAfter = calendar.getTime();
        dataManager.getReportsForApp(app.getId()).stream().filter(report -> report.getDate().before(keepAfter)).forEach(dataManager::deleteReport);
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
