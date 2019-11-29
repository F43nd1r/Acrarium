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
import com.faendir.acra.model.MailSettings;
import com.faendir.acra.model.Permission;
import com.faendir.acra.model.QReport;
import com.faendir.acra.model.QVersion;
import com.faendir.acra.model.User;
import com.faendir.acra.model.Version;
import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.service.DataService;
import com.faendir.acra.service.UserService;
import com.faendir.acra.ui.base.ConfigurationLabel;
import com.faendir.acra.ui.base.MyGrid;
import com.faendir.acra.ui.base.popup.Popup;
import com.faendir.acra.ui.component.Box;
import com.faendir.acra.ui.component.Card;
import com.faendir.acra.ui.component.CssGrid;
import com.faendir.acra.ui.component.DownloadButton;
import com.faendir.acra.ui.component.FlexLayout;
import com.faendir.acra.ui.component.HasSize;
import com.faendir.acra.ui.component.RangeField;
import com.faendir.acra.ui.component.Translatable;
import com.faendir.acra.ui.component.VersionEditorDialog;
import com.faendir.acra.ui.view.Overview;
import com.faendir.acra.ui.view.app.AppView;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.IconRenderer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

import java.io.ByteArrayInputStream;
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
public class AdminTab extends AppTab<Div> {
    @NonNull
    private final UserService userService;
    private final FlexLayout layout;

    @Autowired
    public AdminTab(DataService dataService, @NonNull UserService userService) {
        super(dataService);
        this.userService = userService;
        getContent().setSizeFull();
        layout = new FlexLayout();
        layout.setWidthFull();
        layout.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        layout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        getContent().add(layout);
    }

    @Override
    protected void init(App app) {
        layout.removeAll();
        MyGrid<Version> versionGrid = new MyGrid<>(getDataService().getVersionProvider(app));
        versionGrid.setHeightToRows();
        versionGrid.setHeight("");
        versionGrid.addColumn(Version::getCode, QVersion.version.code, Messages.VERSION_CODE).setFlexGrow(1);
        versionGrid.addColumn(Version::getName, QVersion.version.name, Messages.VERSION).setFlexGrow(1);
        versionGrid.addColumn(new IconRenderer<>(v -> new Icon(v.getMappings() != null ? VaadinIcon.CHECK : VaadinIcon.CLOSE), v -> ""), QVersion.version.mappings.isNotNull(), Messages.PROGUARD_MAPPINGS);
        if (SecurityUtils.hasPermission(app, Permission.Level.EDIT)) {
            versionGrid.addColumn(new ComponentRenderer<>(v -> new Button(new Icon(VaadinIcon.EDIT), e -> new VersionEditorDialog(getDataService(), app, () -> versionGrid.getDataProvider().refreshAll(), v).open())));
            versionGrid.addColumn(new ComponentRenderer<>(v -> new Button(new Icon(VaadinIcon.TRASH), e -> new Popup().addComponent(Translatable.createText(Messages.DELETE_VERSION_CONFIRM, v.getCode())).addYesNoButtons(p -> {
                getDataService().delete(v);
                versionGrid.getDataProvider().refreshAll();
            }, true).show())));
            versionGrid.appendFooterRow().getCell(versionGrid.getColumns().get(0)).setComponent(Translatable.createButton(e -> new VersionEditorDialog(getDataService(), app, () -> versionGrid.getDataProvider().refreshAll(), null).open(), Messages.NEW_VERSION));
        }

        Card versionCard = createCard(versionGrid);
        versionCard.setHeader(Translatable.createLabel(Messages.VERSIONS));

        CssGrid notificationLayout = new CssGrid();
        notificationLayout.setTemplateColumns("auto max-content");
        notificationLayout.setWidthFull();
        User user = userService.getUser(SecurityUtils.getUsername());
        MailSettings settings = getDataService().findMailSettings(app, user).orElse(new MailSettings(app, user));
        notificationLayout.add(Translatable.createLabel(Messages.NEW_BUG_MAIL_LABEL), new Checkbox("", event -> {
            settings.setNewBug(event.getValue());
            getDataService().store(settings);
        }));
        notificationLayout.add(Translatable.createLabel(Messages.REGRESSION_MAIL_LABEL), new Checkbox("", event -> {
            settings.setRegression(event.getValue());
            getDataService().store(settings);
        }));
        notificationLayout.add(Translatable.createLabel(Messages.SPIKE_MAIL_LABEL), new Checkbox("", event -> {
            settings.setSpike(event.getValue());
            getDataService().store(settings);
        }));
        notificationLayout.add(Translatable.createLabel(Messages.WEEKLY_MAIL_LABEL), new Checkbox("", event -> {
            settings.setSummary(event.getValue());
            getDataService().store(settings);
        }));
        if (user.getMail() == null) {
            Icon icon = VaadinIcon.WARNING.create();
            icon.getStyle().set("height", "var(--lumo-font-size-m)");
            Div div = new Div(icon, Translatable.createText(Messages.NO_MAIL_SET));
            div.getStyle().set("color", "var(--lumo-error-color)");
            div.getStyle().set("font-style", "italic");
            notificationLayout.add(div);
        }
        Card notificationCard = createCard(notificationLayout);
        notificationCard.setHeader(Translatable.createLabel(Messages.NOTIFICATIONS));

        Translatable<ComboBox<String>> mailBox = Translatable.createComboBox(getDataService().getFromReports(app, null, QReport.report.userEmail), Messages.BY_MAIL);
        mailBox.setWidthFull();
        Translatable<ComboBox<String>> idBox = Translatable.createComboBox(getDataService().getFromReports(app, null, QReport.report.installationId), Messages.BY_ID);
        idBox.setWidthFull();
        DownloadButton download = new DownloadButton(new StreamResource("reports.json", () -> {
            BooleanExpression where = null;
            String mail = mailBox.getContent().getValue();
            String id = idBox.getContent().getValue();
            if (mail != null && !mail.isEmpty()) {
                where = report.userEmail.eq(mail);
            }
            if (id != null && !id.isEmpty()) {
                where = report.installationId.eq(id).and(where);
            }
            if (where == null) {
                return new ByteArrayInputStream(new byte[0]);
            }
            return new ByteArrayInputStream(getDataService().getFromReports(app, where, report.content, report.id).stream().collect(Collectors.joining(", ", "[", "]")).getBytes(StandardCharsets.UTF_8));
        }), Messages.DOWNLOAD);
        download.setSizeFull();
        Card exportCard = createCard(mailBox, idBox, download);
        exportCard.setHeader(Translatable.createLabel(Messages.EXPORT));

        Box configBox = new Box(Translatable.createLabel(Messages.NEW_ACRA_CONFIG), Translatable.createLabel(Messages.NEW_ACRA_CONFIG_DETAILS),
                Translatable.createButton(e -> new Popup().setTitle(Messages.NEW_ACRA_CONFIG_CONFIRM)
                .addYesNoButtons(popup -> popup.clear().addComponent(new ConfigurationLabel(getDataService().recreateReporterUser(app))).addCloseButton().show())
                .show(), Messages.CREATE));
        Box matchingBox = new Box(Translatable.createLabel(Messages.NEW_BUG_CONFIG), Translatable.createLabel(Messages.NEW_BUG_CONFIG_DETAILS),
                Translatable.createButton(e -> {
            App.Configuration configuration = app.getConfiguration();
            Translatable.Value<RangeField, Double> score = Translatable.createRangeField(Messages.SCORE).with(it -> {
                it.setMin(0);
                it.setMax(100);
                it.setValue((double) configuration.getMinScore());
            });
            new Popup().addComponent(score)
                    .setTitle(Messages.NEW_BUG_CONFIG_CONFIRM)
                    .addYesNoButtons(p -> getDataService().changeConfiguration(app, new App.Configuration(score.getValue().intValue())), true)
                    .show();
        }, Messages.CONFIGURE));
        NumberField age = new NumberField();
        age.setStep(1d);
        age.setMin(1d);
        age.setValue(30d);
        age.setPreventInvalidInput(true);
        age.setHasControls(true);
        age.setWidthFull();
        age.setSuffixComponent(Translatable.createLabel(Messages.REPORTS_OLDER_THAN2));
        FlexLayout purgeAge = new FlexLayout();
        purgeAge.setWidthFull();
        purgeAge.preventWhiteSpaceBreaking();
        purgeAge.setAlignItems(FlexComponent.Alignment.CENTER);
        purgeAge.add(Translatable.createButton(e -> getDataService().deleteReportsOlderThanDays(app, age.getValue().intValue()), Messages.PURGE),
                Translatable.createLabel(Messages.REPORTS_OLDER_THAN1),
                age);
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
        Card dangerCard = createCard(configBox, matchingBox, purgeAge, purgeVersion, deleteButton);
        dangerCard.setHeader(Translatable.createLabel(Messages.DANGER_ZONE));
        dangerCard.enableDivider();
        dangerCard.setHeaderColor("var(----lumo-error-text-color)", "var(--lumo-error-color)");
    }

    private Card createCard(Component... content) {
        Card card = new Card(content);
        card.setWidth(500, HasSize.Unit.PIXEL);
        card.setMaxWidth(1000, HasSize.Unit.PIXEL);
        layout.add(card);
        layout.expand(card);
        return card;
    }
}
