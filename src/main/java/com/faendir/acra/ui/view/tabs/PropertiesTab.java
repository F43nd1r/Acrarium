package com.faendir.acra.ui.view.tabs;

import com.faendir.acra.sql.data.AppRepository;
import com.faendir.acra.sql.data.BugRepository;
import com.faendir.acra.sql.data.ReportRepository;
import com.faendir.acra.sql.model.App;
import com.faendir.acra.sql.model.Permission;
import com.faendir.acra.sql.model.User;
import com.faendir.acra.sql.user.UserManager;
import com.faendir.acra.ui.NavigationManager;
import com.faendir.acra.ui.annotation.RequiresAppPermission;
import com.faendir.acra.ui.view.base.ConfigurationLabel;
import com.faendir.acra.ui.view.base.MyTabSheet;
import com.faendir.acra.ui.view.base.Popup;
import com.faendir.acra.ui.view.base.ValidatedField;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
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
@RequiresAppPermission(Permission.Level.ADMIN)
@SpringComponent
@ViewScope
public class PropertiesTab implements MyTabSheet.Tab {
    public static final String CAPTION = "Properties";
    @NonNull private final AppRepository appRepository;
    @NonNull private final BugRepository bugRepository;
    @NonNull private final ReportRepository reportRepository;
    @NonNull private final UserManager userManager;

    @Autowired
    public PropertiesTab(@NonNull AppRepository appRepository, @NonNull BugRepository bugRepository, @NonNull ReportRepository reportRepository, @NonNull UserManager userManager) {
        this.appRepository = appRepository;
        this.bugRepository = bugRepository;
        this.reportRepository = reportRepository;
        this.userManager = userManager;
    }

    @Override
    public Component createContent(@NonNull App app, @NonNull NavigationManager navigationManager) {
        VerticalLayout layout = new VerticalLayout();
        layout.addComponent(new Button("Create new ACRA Configuration", e -> new Popup().setTitle("Confirm")
                .addComponent(new Label("Are you sure you want to create a new ACRA configuration?<br>The existing configuration will be invalidated", ContentMode.HTML))
                .addYesNoButtons(popup -> {
                    Pair<User, String> userPasswordPair = userManager.createReporterUser();
                    app.setReporter(userPasswordPair.getFirst());
                    appRepository.save(app);
                    popup.clear().addComponent(new ConfigurationLabel(userPasswordPair.getFirst().getUsername(), userPasswordPair.getSecond())).addCloseButton().show();
                })
                .show()));
        layout.addComponent(new Button("Delete App",
                e -> new Popup().setTitle("Confirm").addComponent(new Label("Are you sure you want to delete this app and all its associated content?")).addYesNoButtons(popup -> {
                    appRepository.delete(app);
                    popup.close();
                    navigationManager.navigateBack();
                }).show()));
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
        layout.addComponent(purgeAge);
        layout.addComponent(new Button("Configure bug matching", e -> {
            App.Configuration configuration = app.getConfiguration();
            CheckBox matchByMessage = new CheckBox("Match by exception message", configuration.matchByMessage());
            CheckBox ignoreInstanceIds = new CheckBox("Ignore instance ids", configuration.ignoreInstanceIds());
            CheckBox ignoreAndroidLineNumbers = new CheckBox("Ignore android SDK line numbers", configuration.ignoreAndroidLineNumbers());
            new Popup().addValidatedField(ValidatedField.of(matchByMessage), true)
                    .addValidatedField(ValidatedField.of(ignoreInstanceIds), true)
                    .addValidatedField(ValidatedField.of(ignoreAndroidLineNumbers), true)
                    .addComponent(new Label(
                            "Are you sure you want to save this configuration? All bugs will be recalculated, which may take some time and will reset the 'solved' status"))
                    .addYesNoButtons(p -> {
                        app.setConfiguration(new App.Configuration(matchByMessage.getValue(), ignoreInstanceIds.getValue(), ignoreAndroidLineNumbers.getValue()));
                        appRepository.save(app);
                        reportRepository.reassignBugs(app);
                        bugRepository.deleteOrphans();
                        p.close();
                    })
                    .show();
        }));
        layout.setSizeUndefined();
        return layout;
    }

    @Override
    public String getCaption() {
        return CAPTION;
    }
}
