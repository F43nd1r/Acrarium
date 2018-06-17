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
import com.vaadin.shared.data.sort.SortDirection;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.renderers.ButtonRenderer;
import com.vaadin.ui.themes.AcraTheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.vaadin.risto.stepper.IntStepper;

/**
 * @author lukas
 * @since 17.06.18
 */
@SpringComponent
@ViewScope
public class ProguardPanel implements AdminPanel{
    @NonNull private final DataService dataService;

    @Autowired
    public ProguardPanel(@NonNull DataService dataService) {
        this.dataService = dataService;
    }
    @Override
    public Component createContent(@NonNull App app, @NonNull NavigationManager navigationManager) {
        VerticalLayout layout = new VerticalLayout();
        MyGrid<ProguardMapping> grid = new MyGrid<>(null, dataService.getMappingProvider(app));
        grid.setSizeToRows();
        grid.sort(grid.addColumn(ProguardMapping::getVersionCode, QProguardMapping.proguardMapping.versionCode, "Version"), SortDirection.ASCENDING);
        if (SecurityUtils.hasPermission(app, Permission.Level.EDIT)) {
            grid.addColumn(report -> "Delete",
                    new ButtonRenderer<>(e -> new Popup().setTitle("Confirm")
                            .addComponent(new Label("Are you sure you want to delete the mapping for version " + e.getItem().getVersionCode() + "?"))
                            .addYesNoButtons(p -> {
                                dataService.delete(e.getItem());
                                grid.getDataProvider().refreshAll();
                            }, true)
                            .show()));
        }
        layout.addComponent(grid);
        layout.addStyleName(AcraTheme.NO_PADDING);
        if (SecurityUtils.hasPermission(app, Permission.Level.EDIT)) {
            layout.addComponent(new Button("Add File", e -> {
                IntStepper version = new IntStepper("Version code");
                version.setValue(dataService.getMaximumMappingVersion(app).map(i -> i + 1).orElse(1));
                InMemoryUpload upload = new InMemoryUpload("Mapping file:");
                ProgressBar progressBar = new ProgressBar();
                upload.addProgressListener((readBytes, contentLength) -> layout.getUI().access(() -> progressBar.setValue((float) readBytes / contentLength)));
                new Popup().setTitle("New Mapping Configuration")
                        .addComponent(version)
                        .addValidatedField(ValidatedField.of(upload, () -> upload, consumer -> upload.addFinishedListener(event -> consumer.accept(upload)))
                                .addValidator(InMemoryUpload::isUploaded, "Upload failed"))
                        .addComponent(progressBar)
                        .addCreateButton(popup -> {
                            dataService.store(new ProguardMapping(app, version.getValue(), upload.getUploadedString()));
                            grid.getDataProvider().refreshAll();
                        }, true)
                        .show();
            }));
        }
        return layout;
    }

    @Override
    public String getCaption() {
        return "De-Obfuscation";
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
