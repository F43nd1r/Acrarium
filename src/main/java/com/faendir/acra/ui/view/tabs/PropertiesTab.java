package com.faendir.acra.ui.view.tabs;

import com.faendir.acra.sql.data.AppRepository;
import com.faendir.acra.sql.data.ReportRepository;
import com.faendir.acra.sql.model.App;
import com.faendir.acra.sql.model.User;
import com.faendir.acra.sql.user.UserManager;
import com.faendir.acra.ui.NavigationManager;
import com.faendir.acra.ui.view.base.ConfigurationLabel;
import com.faendir.acra.ui.view.base.MyTabSheet;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.lang.NonNull;
import org.vaadin.risto.stepper.IntStepper;

import java.util.Calendar;
import java.util.Date;

/**
 * @author Lukas
 * @since 19.05.2017
 */
@SpringComponent
@ViewScope
public class PropertiesTab extends VerticalLayout implements MyTabSheet.Tab {
    public static final String CAPTION = "Properties";
    @NonNull private final AppRepository appRepository;
    private final ReportRepository reportRepository;
    @NonNull private final UserManager userManager;

    @Autowired
    public PropertiesTab(@NonNull AppRepository appRepository, @NonNull ReportRepository reportRepository, @NonNull UserManager userManager) {
        this.appRepository = appRepository;
        this.reportRepository = reportRepository;
        this.userManager = userManager;
        setCaption(CAPTION);
    }

    @Override
    public Component createContent(@NonNull App app, @NonNull NavigationManager navigationManager) {
        addComponent(new Button("Create new ACRA Configuration", e -> {
            Window window = new Window("Confirm");
            VerticalLayout layout = new VerticalLayout();
            layout.addComponent(new Label("Are you sure you want to create a new ACRA configuration? The existing configuration will be invalidated"));
            Button yes = new Button("Yes", e1 -> {
                layout.removeAllComponents();
                Pair<User, String> userPasswordPair = userManager.createReporterUser();
                app.setReporter(userPasswordPair.getFirst());
                appRepository.save(app);
                layout.addComponent(new ConfigurationLabel(userPasswordPair.getFirst().getUsername(), userPasswordPair.getSecond()));
                layout.addComponent(new Button("Close", e2 -> window.close()));
                window.center();
            });
            Button no = new Button("No", e1 -> window.close());
            layout.addComponent(new HorizontalLayout(yes, no));
            window.setContent(layout);
            window.center();
            UI.getCurrent().addWindow(window);
        }));
        addComponent(new Button("Delete App", e -> {
            Window window = new Window("Confirm");
            Label label = new Label("Are you sure you want to delete this app and all its reports and mappings?");
            Button yes = new Button("Yes", e1 -> {
                appRepository.delete(app);
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
            reportRepository.deleteAllByBugAppAndDateBefore(app, keepAfter);
        }), new Label("Reports older than "), age, new Label("Days"));
        purgeAge.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        addComponent(purgeAge);
        setSizeUndefined();
        return this;
    }
}
