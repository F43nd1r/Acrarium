package com.faendir.acra.ui.view.tabs;

import com.faendir.acra.sql.data.DataManager;
import com.faendir.acra.sql.model.App;
import com.faendir.acra.ui.NavigationManager;
import com.faendir.acra.ui.view.base.MyTabSheet;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import org.springframework.lang.NonNull;
import org.vaadin.risto.stepper.IntStepper;

import java.util.Calendar;
import java.util.Date;

/**
 * @author Lukas
 * @since 19.05.2017
 */
public class PropertiesTab extends VerticalLayout implements MyTabSheet.Tab{
    public static final String CAPTION = "Properties";

    public PropertiesTab() {
    }

    @Override
    public Component createContent(@NonNull App app, @NonNull DataManager dataManager, @NonNull NavigationManager navigationManager) {
        String location = UI.getCurrent().getPage().getLocation().toASCIIString();
        location = location.substring(0, location.indexOf('#'));
        addComponent(new Label(String.format("Required ACRA configuration:<br><code>formUri = \"%sreport\",<br>" +
                                                     "formUriBasicAuthLogin = \"%s\",<br>formUriBasicAuthPassword = \"%s\",<br>" +
                                                     "httpMethod = HttpSender.Method.POST,<br>reportType = HttpSender.Type.JSON</code>",
                                             location, app.getId(), app.getPassword()), ContentMode.HTML));
        addComponent(new Button("Delete App", e -> {
            Window window = new Window("Confirm");
            Label label = new Label("Are you sure you want to delete this app and all its reports and mappings?");
            Button yes = new Button("Yes", e1 -> {
                dataManager.deleteApp(app);
                window.close();
                navigationManager.navigateBack();
            });
            Button no = new Button("No", e1 -> window.close());
            VerticalLayout layout = new VerticalLayout(label, new HorizontalLayout(yes, no));
            window.setContent(layout);
            window.center();
            UI.getCurrent().addWindow(window);
        }));
        IntStepper age = new IntStepper();
        age.setValue(30);
        age.setMinValue(0);
        HorizontalLayout purgeAge = new HorizontalLayout(new Button("Purge", e -> {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_MONTH, -age.getValue());
            Date keepAfter = calendar.getTime();
            dataManager.purgeReportsBefore(app, keepAfter);
        }), new Label("Reports older than "), age, new Label("Days"));
        purgeAge.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        addComponent(purgeAge);
        setCaption(CAPTION);
        setSizeUndefined();
        return this;
    }
}
