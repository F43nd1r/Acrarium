package com.faendir.acra.ui.view.app.tabs;

import com.faendir.acra.model.App;
import com.faendir.acra.model.Permission;
import com.faendir.acra.model.ProguardMapping;
import com.faendir.acra.model.QReport;
import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.service.data.DataService;
import com.faendir.acra.ui.annotation.RequiresAppPermission;
import com.faendir.acra.ui.navigation.NavigationManager;
import com.faendir.acra.ui.view.base.ConfigurationLabel;
import com.faendir.acra.ui.view.base.InMemoryUpload;
import com.faendir.acra.ui.view.base.MyGrid;
import com.faendir.acra.ui.view.base.OnDemandFileDownloader;
import com.faendir.acra.ui.view.base.Popup;
import com.faendir.acra.ui.view.base.ValidatedField;
import com.faendir.acra.util.Style;
import com.jarektoro.responsivelayout.ResponsiveLayout;
import com.jarektoro.responsivelayout.ResponsiveRow;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Sizeable;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.renderers.ButtonRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.lang.NonNull;
import org.springframework.web.util.UriUtils;
import org.vaadin.risto.stepper.IntStepper;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

/**
 * @author Lukas
 * @since 19.05.2017
 */
@RequiresAppPermission(Permission.Level.ADMIN)
@SpringComponent
@ViewScope
public class AdminTab implements AppTab {
    @NonNull private final DataService dataService;

    @Autowired
    public AdminTab(@NonNull DataService dataService) {
        this.dataService = dataService;
    }

    @Override
    public Component createContent(@NonNull App app, @NonNull NavigationManager navigationManager) {
        ResponsiveLayout responsiveLayout = new ResponsiveLayout();
        ResponsiveRow row = responsiveLayout.addRow().withSpacing(ResponsiveRow.SpacingSize.SMALL, true).withMargin(ResponsiveRow.MarginSize.SMALL).withAlignment(Alignment.TOP_CENTER);
        row.addColumn().withComponent(proguardPanel(app)).withDisplayRules(12, 12, 6, 4);
        row.addColumn().withComponent(exportPanel(app, navigationManager)).withDisplayRules(12, 12, 6, 4);
        row.addColumn().withComponent(dangerPanel(app, navigationManager)).withDisplayRules(12, 12, 6, 4);
        Panel root = new Panel(responsiveLayout);
        root.setSizeFull();
        Style.apply(root, Style.NO_BACKGROUND, Style.NO_BORDER);
        return root;
    }

    private Panel dangerPanel(@NonNull App app, @NonNull NavigationManager navigationManager) {
        Button configButton = new Button("Create new ACRA Configuration",
                e -> new Popup().setTitle("Confirm")
                        .addComponent(new Label("Are you sure you want to create a new ACRA configuration?<br>The existing configuration will be invalidated", ContentMode.HTML))
                        .addYesNoButtons(popup -> popup.clear().addComponent(new ConfigurationLabel(dataService.recreateReporterUser(app))).addCloseButton().show())
                        .show());
        configButton.setWidth(100, Sizeable.Unit.PERCENTAGE);
        Button matchingButton = new Button("Configure bug matching", e -> {
            App.Configuration configuration = app.getConfiguration();
            CheckBox matchByMessage = new CheckBox("Match by exception message", configuration.matchByMessage());
            CheckBox ignoreInstanceIds = new CheckBox("Ignore instance ids", configuration.ignoreInstanceIds());
            CheckBox ignoreAndroidLineNumbers = new CheckBox("Ignore android SDK line numbers", configuration.ignoreAndroidLineNumbers());
            new Popup().addValidatedField(ValidatedField.of(matchByMessage), true)
                    .addValidatedField(ValidatedField.of(ignoreInstanceIds), true)
                    .addValidatedField(ValidatedField.of(ignoreAndroidLineNumbers), true)
                    .addComponent(new Label(
                            "Are you sure you want to save this configuration? All bugs will be recalculated, which may take some time and will reset the 'solved' status"))
                    .addYesNoButtons(p -> dataService.changeConfiguration(app,
                            new App.Configuration(matchByMessage.getValue(), ignoreInstanceIds.getValue(), ignoreAndroidLineNumbers.getValue())), true)
                    .show();
        });
        matchingButton.setWidth(100, Sizeable.Unit.PERCENTAGE);
        IntStepper age = new IntStepper();
        age.setValue(30);
        age.setMinValue(0);
        age.setWidth(100, Sizeable.Unit.PERCENTAGE);
        HorizontalLayout purgeAge = new HorizontalLayout();
        purgeAge.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        purgeAge.setWidth(100, Sizeable.Unit.PERCENTAGE);
        Style.NO_MARGIN.apply(purgeAge);
        purgeAge.addComponents(new Button("Purge", e -> dataService.deleteReportsOlderThanDays(app, age.getValue())), new Label(" Reports older than "), age, new Label(" Days"));
        purgeAge.setExpandRatio(age, 1);
        ComboBox<Integer> versionBox = new ComboBox<>(null, dataService.getFromReports(QReport.report.bug.app.eq(app), QReport.report.versionCode));
        versionBox.setEmptySelectionAllowed(false);
        versionBox.setWidth(100, Sizeable.Unit.PERCENTAGE);
        HorizontalLayout purgeVersion = new HorizontalLayout();
        purgeVersion.setWidth(100, Sizeable.Unit.PERCENTAGE);
        purgeVersion.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        Style.NO_MARGIN.apply(purgeVersion);
        purgeVersion.addComponents(new Button("Purge", e -> {
            if (versionBox.getValue() != null) {
                dataService.deleteReportsBeforeVersion(app, versionBox.getValue());
            }
        }), new Label(" Reports before Version "), versionBox);
        purgeVersion.setExpandRatio(versionBox, 1);
        Button deleteButton = new Button("Delete App",
                e -> new Popup().setTitle("Confirm").addComponent(new Label("Are you sure you want to delete this app and all its associated content?")).addYesNoButtons(popup -> {
                    dataService.delete(app);
                    navigationManager.navigateBack();
                }, true).show());
        VerticalLayout layout = new VerticalLayout(configButton, matchingButton, purgeAge, purgeVersion, deleteButton);
        deleteButton.setWidth(100, Sizeable.Unit.PERCENTAGE);
        layout.setSizeFull();
        Style.NO_PADDING.apply(layout);
        Panel danger = new Panel(layout);
        danger.setSizeFull();
        danger.setCaption("Danger Zone");
        danger.setIcon(VaadinIcons.EXCLAMATION);
        Style.apply(danger, Style.NO_BACKGROUND, Style.RED_PANEL_HEADER, Style.NO_MARGIN);
        return danger;
    }

    private Panel proguardPanel(@NonNull App app) {
        VerticalLayout layout = new VerticalLayout();
        MyGrid<ProguardMapping> grid = new MyGrid<>(null, dataService.getMappingProvider(app));
        grid.setSizeToRows();
        grid.addColumn(ProguardMapping::getVersionCode, "Version");
        if (SecurityUtils.hasPermission(app, Permission.Level.EDIT)) {
            grid.addColumn(report -> "Delete",
                    new ButtonRenderer<>(e -> new Popup().setTitle("Confirm")
                            .addComponent(new Label("Are you sure you want to delete the mapping for version " + e.getItem().getVersionCode() + "?"))
                            .addYesNoButtons(p -> dataService.delete(e.getItem()), true))).setSortable(false);
        }
        layout.addComponent(grid);
        Style.NO_PADDING.apply(layout);
        if (SecurityUtils.hasPermission(app, Permission.Level.EDIT)) {
            layout.addComponent(new Button("Add File", e -> {
                IntStepper version = new IntStepper("Version code");
                version.setValue(1);
                InMemoryUpload upload = new InMemoryUpload("Mapping file:");
                ProgressBar progressBar = new ProgressBar();
                upload.addProgressListener((readBytes, contentLength) -> layout.getUI().access(() -> progressBar.setValue((float) readBytes / contentLength)));
                new Popup().setTitle("New Mapping Configuration")
                        .addComponent(version)
                        .addValidatedField(ValidatedField.of(upload, () -> upload, consumer -> upload.addFinishedListener(event -> consumer.accept(upload)))
                                .addValidator(InMemoryUpload::isUploaded, "Upload failed"))
                        .addComponent(progressBar)
                        .addCreateButton(popup -> {
                            dataService.save(new ProguardMapping(app, version.getValue(), upload.getUploadedString()));
                            grid.getDataProvider().refreshAll();
                        }, true)
                        .show();
            }));
        }
        Panel panel = new Panel(layout);
        panel.setSizeFull();
        panel.setCaption("De-Obfuscation");
        Style.apply(panel, Style.NO_BACKGROUND, Style.NO_MARGIN);
        return panel;
    }

    private Panel exportPanel(@NonNull App app, @NonNull NavigationManager navigationManager) {
        ComboBox<String> mailBox = new ComboBox<>("By Email Address", dataService.getFromReports(QReport.report.bug.app.eq(app), QReport.report.userEmail));
        mailBox.setEmptySelectionAllowed(false);
        mailBox.setSizeFull();
        Button download = new Button("Download");
        new OnDemandFileDownloader(() -> Pair.of(new ByteArrayInputStream(dataService.getFromReports(QReport.report.bug.app.eq(app)
                        .and(QReport.report.userEmail.eq(mailBox.getValue())), QReport.report.content, QReport.report.id).stream().collect(Collectors.joining(", ", "[", "]")).getBytes()),
                "reports-" + UriUtils.encode(mailBox.getValue(), StandardCharsets.UTF_8) + ".json")).extend(download);
        download.setSizeFull();
        VerticalLayout layout = new VerticalLayout(mailBox, download);
        Panel panel = new Panel(layout);
        panel.setSizeFull();
        panel.setCaption("Export");
        Style.apply(panel, Style.NO_BACKGROUND, Style.NO_MARGIN);
        return panel;
    }

    @Override
    public String getCaption() {
        return "Admin";
    }

    @Override
    public int getOrder() {
        return 4;
    }
}
