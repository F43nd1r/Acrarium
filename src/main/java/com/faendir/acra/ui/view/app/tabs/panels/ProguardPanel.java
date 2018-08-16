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

package com.faendir.acra.ui.view.app.tabs.panels;

import com.faendir.acra.i18n.I18nButton;
import com.faendir.acra.i18n.I18nIntStepper;
import com.faendir.acra.i18n.I18nLabel;
import com.faendir.acra.i18n.Messages;
import com.faendir.acra.model.App;
import com.faendir.acra.model.Permission;
import com.faendir.acra.model.ProguardMapping;
import com.faendir.acra.model.QProguardMapping;
import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.service.DataService;
import com.faendir.acra.ui.navigation.NavigationManager;
import com.faendir.acra.ui.view.base.InMemoryUpload;
import com.faendir.acra.ui.view.base.layout.MyGrid;
import com.faendir.acra.ui.view.base.popup.Popup;
import com.faendir.acra.ui.view.base.popup.ValidatedField;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Component;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.themes.AcraTheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.vaadin.risto.stepper.IntStepper;
import org.vaadin.spring.i18n.I18N;

/**
 * @author lukas
 * @since 17.06.18
 */
@SpringComponent
@ViewScope
public class ProguardPanel implements AdminPanel{
    @NonNull private final DataService dataService;
    @NonNull private final I18N i18n;

    @Autowired
    public ProguardPanel(@NonNull DataService dataService, @NonNull I18N i18n) {
        this.dataService = dataService;
        this.i18n = i18n;
    }
    @Override
    public Component createContent(@NonNull App app, @NonNull NavigationManager navigationManager) {
        VerticalLayout layout = new VerticalLayout();
        MyGrid<ProguardMapping> grid = new MyGrid<>(dataService.getMappingProvider(app), i18n);
        grid.setSizeToRows();
        grid.sort(grid.addColumn(ProguardMapping::getVersionCode, QProguardMapping.proguardMapping.versionCode, Messages.VERSION), SortDirection.ASCENDING);
        if (SecurityUtils.hasPermission(app, Permission.Level.EDIT)) {
            ButtonRenderer<ProguardMapping> renderer = new ButtonRenderer<>(e -> new Popup(i18n, Messages.CONFIRM)
                    .addComponent(new I18nLabel(i18n, Messages.DELETE_MAPPING_CONFIRM, e.getItem().getVersionCode()))
                    .addYesNoButtons(p -> {
                        dataService.delete(e.getItem());
                        grid.getDataProvider().refreshAll();
                    }, true)
                    .show());
            renderer.setHtmlContentAllowed(true);
            grid.addColumn(report -> VaadinIcons.TRASH.getHtml(),
                    renderer);
        }
        layout.addComponent(grid);
        layout.addStyleName(AcraTheme.NO_PADDING);
        if (SecurityUtils.hasPermission(app, Permission.Level.EDIT)) {
            layout.addComponent(new I18nButton(e -> {
                IntStepper version = new I18nIntStepper(dataService.getMaximumMappingVersion(app).map(i -> i + 1).orElse(1), i18n, Messages.VERSION_CODE);
                InMemoryUpload upload = new InMemoryUpload(i18n, Messages.MAPPING_FILE);
                ProgressBar progressBar = new ProgressBar();
                upload.addProgressListener((readBytes, contentLength) -> layout.getUI().access(() -> progressBar.setValue((float) readBytes / contentLength)));
                new Popup(i18n, Messages.NEW_MAPPING)
                        .addComponent(version)
                        .addValidatedField(ValidatedField.of(upload, () -> upload, consumer -> upload.addFinishedListener(event -> consumer.accept(upload)))
                                .addValidator(InMemoryUpload::isUploaded, Messages.ERROR_UPLOAD))
                        .addComponent(progressBar)
                        .addCreateButton(popup -> {
                            dataService.store(new ProguardMapping(app, version.getValue(), upload.getUploadedString()));
                            grid.getDataProvider().refreshAll();
                        }, true)
                        .show();
            }, i18n, Messages.NEW_FILE));
        }
        return layout;
    }

    @Override
    public String getCaption() {
        return i18n.get(Messages.DE_OBFUSCATION);
    }

    @Override
    public String getId() {
        return "proguard";
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
