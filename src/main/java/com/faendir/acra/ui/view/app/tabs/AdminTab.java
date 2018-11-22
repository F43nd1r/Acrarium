/*
 * (C) Copyright 2018 Lukas Morawietz (https://github.com/F43nd1r)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.faendir.acra.ui.view.app.tabs;

import com.faendir.acra.i18n.Messages;
import com.faendir.acra.model.App;
import com.faendir.acra.model.Permission;
import com.faendir.acra.model.ProguardMapping;
import com.faendir.acra.model.QProguardMapping;
import com.faendir.acra.model.QReport;
import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.service.DataService;
import com.faendir.acra.ui.component.Card;
import com.faendir.acra.ui.base.ConfigurationLabel;
import com.faendir.acra.ui.base.MyGrid;
import com.faendir.acra.ui.base.popup.Popup;
import com.faendir.acra.ui.component.DownloadButton;
import com.faendir.acra.ui.component.FlexLayout;
import com.faendir.acra.ui.component.HasSize;
import com.faendir.acra.ui.component.Translatable;
import com.faendir.acra.ui.view.Overview;
import com.faendir.acra.ui.view.app.AppView;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Input;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
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
        getContent().removeAll();
        getContent().setFlexWrap(FlexLayout.FlexWrap.WRAP);
        getContent().setWidthFull();
        MyGrid<ProguardMapping> mappingGrid = new MyGrid<>(getDataService().getMappingProvider(app));
        mappingGrid.setHeightToRows();
        mappingGrid.addColumn(ProguardMapping::getVersionCode, QProguardMapping.proguardMapping.versionCode, Messages.VERSION);
        Card mappingCard = new Card(mappingGrid);
        mappingCard.setHeader(Translatable.createText(Messages.DE_OBFUSCATION));
        mappingCard.setWidth(500, HasSize.Unit.PIXEL);
        if (SecurityUtils.hasPermission(app, Permission.Level.EDIT)) {
            mappingGrid.addColumn(new ComponentRenderer<>(mapping -> new Button(new Icon(VaadinIcon.TRASH), e -> new Popup().addComponent(Translatable.createText(Messages.DELETE_MAPPING_CONFIRM, mapping.getVersionCode())).addYesNoButtons(p -> {
                getDataService().delete(mapping);
                mappingGrid.getDataProvider().refreshAll();
            }, true).show())));
            mappingCard.add(Translatable.createButton(e -> {
                Translatable<TextField> version = Translatable.createTextField(String.valueOf(getDataService().getMaximumMappingVersion(app).map(i -> i + 1).orElse(1)), Messages.VERSION_CODE);
                MemoryBuffer buffer = new MemoryBuffer();
                Upload upload = new Upload(buffer);
                new Popup()
                        .setTitle(Messages.NEW_MAPPING)
                        .addComponent(version)
                        .addComponent(upload)
                        .addCreateButton(popup -> {
                            try {
                                getDataService().store(new ProguardMapping(app, Integer.valueOf(version.getContent().getValue()), StreamUtils.copyToString(buffer.getInputStream(), Charset.defaultCharset())));
                            } catch (Exception ex) {
                                //TODO
                            }
                            mappingGrid.getDataProvider().refreshAll();
                        }, true)
                        .show();
            }, Messages.NEW_FILE));
        }
        getContent().add(mappingCard);
        getContent().expand(mappingCard);

        Translatable<ComboBox<String>> mailBox = Translatable.createComboBox(getDataService().getFromReports(app, null, QReport.report.userEmail), Messages.BY_MAIL);
        mailBox.setWidthFull();
        Translatable<ComboBox<String>> idBox = Translatable.createComboBox(getDataService().getFromReports(app, null, QReport.report.installationId), Messages.BY_ID);
        idBox.setWidthFull();
        DownloadButton download = new DownloadButton(new StreamResource("reports.json", () -> {
            BooleanExpression where = null;
            String name = "";
            String mail = mailBox.getContent().getValue();
            String id = idBox.getContent().getValue();
            if (mail != null && !mail.isEmpty()) {
                where = report.userEmail.eq(mail);
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
        }), Messages.DOWNLOAD);
        download.setSizeFull();
        Card exportCard = new Card(mailBox, idBox, download);
        exportCard.setHeader(Translatable.createText(Messages.EXPORT));
        exportCard.setWidth(500, HasSize.Unit.PIXEL);
        getContent().add(exportCard);
        getContent().expand(exportCard);

        Translatable<Button> configButton = Translatable.createButton(e -> new Popup().setTitle(Messages.NEW_ACRA_CONFIG_CONFIRM)
                .addYesNoButtons(popup -> popup.clear().addComponent(new ConfigurationLabel(getDataService().recreateReporterUser(app))).addCloseButton().show())
                .show(), Messages.NEW_ACRA_CONFIG);
        configButton.setWidthFull();
        Translatable<Button> matchingButton = Translatable.createButton(e -> {
            App.Configuration configuration = app.getConfiguration();
            Input score = new Input();
            score.setType("range");
            score.getElement().setProperty("min", 0);
            score.getElement().setProperty("max", 100);
            score.setValue(String.valueOf(configuration.getMinScore()));
            new Popup().addComponent(score)
                    .setTitle(Messages.NEW_BUG_CONFIG_CONFIRM)
                    .addYesNoButtons(p -> getDataService().changeConfiguration(app, new App.Configuration(Integer.parseInt(score.getValue()))), true)
                    .show();
        }, Messages.NEW_BUG_CONFIG);
        matchingButton.setWidthFull();
        TextField age = new TextField();
        age.setValue("30");
        age.setWidth("100%");
        FlexLayout purgeAge = new FlexLayout();
        purgeAge.setWidthFull();
        purgeAge.preventWhiteSpaceBreaking();
        purgeAge.setAlignItems(FlexComponent.Alignment.CENTER);
        purgeAge.add(Translatable.createButton(e -> getDataService().deleteReportsOlderThanDays(app, Integer.parseInt(age.getValue())), Messages.PURGE),
                Translatable.createLabel(Messages.REPORTS_OLDER_THAN1),
                age, Translatable.createLabel(Messages.REPORTS_OLDER_THAN2));
        purgeAge.expand(age);
        ComboBox<Integer> versionBox = new ComboBox<>(null, getDataService().getFromReports(app, null, QReport.report.stacktrace.version.code));
        versionBox.setWidth("100%");
        FlexLayout purgeVersion = new FlexLayout();
        purgeVersion.setWidthFull();
        purgeVersion.preventWhiteSpaceBreaking();
        purgeVersion.setAlignItems(FlexComponent.Alignment.CENTER);
        purgeVersion.add(Translatable.createButton(e -> {
            if (versionBox.getValue() != null) {
                getDataService().deleteReportsBeforeVersion(app, versionBox.getValue());
            }
        }, Messages.PURGE), Translatable.createLabel(Messages.REPORTS_BEFORE_VERSION), versionBox);
        purgeVersion.expand(versionBox);
        Translatable<Button> deleteButton = Translatable.createButton(e -> new Popup().setTitle(Messages.DELETE_APP_CONFIRM).addYesNoButtons(popup -> {
            getDataService().delete(app);
            UI.getCurrent().navigate(Overview.class);
        }, true).show(), Messages.DELETE_APP);
        deleteButton.setWidthFull();
        Card dangerCard = new Card(configButton, matchingButton, purgeAge, purgeVersion, deleteButton);
        dangerCard.setHeader(Translatable.createText(Messages.DANGER_ZONE));
        dangerCard.setWidth(500, HasSize.Unit.PIXEL);
        getContent().add(dangerCard);
        getContent().expand(dangerCard);
    }
}
