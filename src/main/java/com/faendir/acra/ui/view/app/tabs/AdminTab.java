package com.faendir.acra.ui.view.app.tabs;

import com.faendir.acra.model.App;
import com.faendir.acra.model.Permission;
import com.faendir.acra.model.ProguardMapping;
import com.faendir.acra.model.QProguardMapping;
import com.faendir.acra.model.QReport;
import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.service.DataService;
import com.faendir.acra.ui.base.Card;
import com.faendir.acra.ui.base.ConfigurationLabel;
import com.faendir.acra.ui.base.MyGrid;
import com.faendir.acra.ui.base.popup.Popup;
import com.faendir.acra.ui.view.Overview;
import com.faendir.acra.ui.view.app.AppView;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import static com.faendir.acra.model.QReport.report;


/**
 * @author lukas
 * @since 18.10.18
 */
@UIScope
@SpringComponent
@Route(value = "admin", layout = AppView.class)
public class AdminTab extends AppTab<FlexLayout> {
    @Autowired
    public AdminTab(DataService dataService) {
        super(dataService);
    }

    @Override
    void init(App app) {
        getContent().getStyle().set("flex-wrap", "wrap");
        getContent().setWidth("100%");
        MyGrid<ProguardMapping> mappingGrid = new MyGrid<>(getDataService().getMappingProvider(app));
        mappingGrid.setHeightToRows();
        mappingGrid.addColumn(ProguardMapping::getVersionCode, QProguardMapping.proguardMapping.versionCode, "Version");
        Card mappingCard = new Card(mappingGrid);
        mappingCard.setHeader(new Text("Deobfuscation"));
        mappingCard.setWidth("500px");
        if (SecurityUtils.hasPermission(app, Permission.Level.EDIT)) {
            mappingGrid.addColumn(new ComponentRenderer<>(mapping -> new Button(new Icon(VaadinIcon.TRASH), e -> new Popup().addComponent(new Text("Are you sure you want to delete the mapping for version " + mapping.getVersionCode() + "?")).addYesNoButtons(p -> {
                getDataService().delete(mapping);
                mappingGrid.getDataProvider().refreshAll();
            }, true).show())), "");
            mappingCard.add(new Button("Add File", e -> {
                TextField version = new TextField("Version Code", String.valueOf(getDataService().getMaximumMappingVersion(app).map(i -> i + 1).orElse(1)));
                MemoryBuffer buffer = new MemoryBuffer();
                Upload upload = new Upload(buffer);
                new Popup()
                        .setTitle("New Mapping")
                        .addComponent(version)
                        .addComponent(upload)
                        .addCreateButton(popup -> {
                            try {
                                getDataService().store(new ProguardMapping(app, Integer.valueOf(version.getValue()), StreamUtils.copyToString(buffer.getInputStream(), Charset.defaultCharset())));
                            } catch (Exception ex) {
                                //TODO
                            }
                            mappingGrid.getDataProvider().refreshAll();
                        }, true)
                        .show();
            }));
        }
        getContent().add(mappingCard);
        getContent().expand(mappingCard);

        ComboBox<String> mailBox = new ComboBox<>("By mail", getDataService().getFromReports(app, null, QReport.report.userEmail));
        mailBox.setSizeFull();
        ComboBox<String> idBox = new ComboBox<>("By Id", getDataService().getFromReports(app, null, QReport.report.installationId));
        idBox.setSizeFull();
        Anchor download = new Anchor(new StreamResource("reports.json", () -> {
            BooleanExpression where = null;
            String name = "";
            String mail = mailBox.getValue();
            String id = idBox.getValue();
            if (mail != null && !mail.isEmpty()) {
                where = report.userEmail.eq(mail).and(where);
                name += "_" + mail;
            }
            if (id != null && !id.isEmpty()) {
                where = report.installationId.eq(id).and(where);
                name += "_" + id;
            }
            if (name.isEmpty()) {
                return new ByteArrayInputStream(new byte[0]);
            }
            return new ByteArrayInputStream(getDataService().getFromReports(app, where, report.content, report.id).stream().collect(Collectors.joining(", ", "[", "]")).getBytes(StandardCharsets.UTF_8));
        }), "");
        download.getElement().setAttribute("download", true);
        download.add(new Button("Download"));
        download.setSizeFull();
        Card exportCard = new Card(mailBox, idBox, download);
        exportCard.setHeader(new Text("Export"));
        exportCard.setWidth("500px");
        getContent().add(exportCard);
        getContent().expand(exportCard);

        Button configButton = new Button("Create new ACRA config", e -> new Popup().addComponent(new Label("Create new ACRA Configuration"))
                .addYesNoButtons(popup -> popup.clear().addComponent(new ConfigurationLabel(getDataService().recreateReporterUser(app))).addCloseButton().show())
                .show());
        configButton.setSizeFull();
        Button matchingButton = new Button("New bug config", e -> {
            App.Configuration configuration = app.getConfiguration();
            Input score = new Input();
            score.setType("range");
            score.getElement().setProperty("min", 0);
            score.getElement().setProperty("max", 100);
            score.setValue(String.valueOf(configuration.getMinScore()));
            new Popup().addComponent(score)
                    .addComponent(new Text("Confirm new bug config"))
                    .addYesNoButtons(p -> getDataService().changeConfiguration(app, new App.Configuration(Integer.parseInt(score.getValue()))), true)
                    .show();
        });
        matchingButton.setSizeFull();
        TextField age = new TextField();
        age.setValue("30");
        age.setSizeFull();
        FlexLayout purgeAge = new FlexLayout();
        purgeAge.setSizeFull();
        purgeAge.add(new Button("Purge", e -> getDataService().deleteReportsOlderThanDays(app, Integer.parseInt(age.getValue()))),
                new Text("Reports older than"),
                age);
        purgeAge.expand(age);
        ComboBox<Integer> versionBox = new ComboBox<>(null, getDataService().getFromReports(app, null, QReport.report.stacktrace.version.code));
        versionBox.setSizeFull();
        FlexLayout purgeVersion = new FlexLayout();
        purgeVersion.setSizeFull();
        purgeVersion.add(new Button("Purge", e -> {
            if (versionBox.getValue() != null) {
                getDataService().deleteReportsBeforeVersion(app, versionBox.getValue());
            }
        }), new Text("Reports before version"), versionBox);
        purgeVersion.expand(versionBox);
        Button deleteButton = new Button("Delete app", e -> new Popup().addComponent(new Text("Confirm delete")).addYesNoButtons(popup -> {
            getDataService().delete(app);
            UI.getCurrent().navigate(Overview.class);
        }, true).show());
        deleteButton.setSizeFull();
        Card dangerCard = new Card(configButton, matchingButton, purgeAge, purgeVersion, deleteButton);
        dangerCard.setHeader(new Text("Danger Zone"));
        dangerCard.setWidth("500px");
        getContent().add(dangerCard);
        getContent().expand(dangerCard);
    }
}
