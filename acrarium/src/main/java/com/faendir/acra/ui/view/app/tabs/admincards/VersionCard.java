/*
 * (C) Copyright 2019 Lukas Morawietz (https://github.com/F43nd1r)
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

package com.faendir.acra.ui.view.app.tabs.admincards;

import com.faendir.acra.i18n.Messages;
import com.faendir.acra.model.App;
import com.faendir.acra.model.Permission;
import com.faendir.acra.model.QVersion;
import com.faendir.acra.model.Version;
import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.service.DataService;
import com.faendir.acra.ui.component.Grid;
import com.faendir.acra.ui.component.HasSize;
import com.faendir.acra.ui.component.Translatable;
import com.faendir.acra.ui.component.dialog.FluentDialog;
import com.faendir.acra.ui.component.dialog.VersionEditorDialog;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.renderer.IconRenderer;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;

@UIScope
@SpringComponent
public class VersionCard extends AdminCard {
    public VersionCard(DataService dataService) {
        super(dataService);
    }

    @Override
    public void init(App app) {
        Grid<Version> versionGrid = new Grid<>(getDataService().getVersionProvider(app));
        versionGrid.setMinHeight(280, HasSize.Unit.PIXEL);
        versionGrid.setHeight(100, HasSize.Unit.PERCENTAGE);
        versionGrid.addColumn(Version::getCode, QVersion.version.code, Messages.VERSION_CODE).setFlexGrow(1);
        versionGrid.addColumn(Version::getName, QVersion.version.name, Messages.VERSION).setFlexGrow(1);
        versionGrid.addColumn(new IconRenderer<>(v -> new Icon(v.getMappings() != null ? VaadinIcon.CHECK : VaadinIcon.CLOSE), v -> ""), QVersion.version.mappings.isNotNull(), Messages.PROGUARD_MAPPINGS);
        if (SecurityUtils.hasPermission(app, Permission.Level.EDIT)) {
            versionGrid.addColumn(new ComponentRenderer<>(v -> new Button(new Icon(VaadinIcon.EDIT), e -> new VersionEditorDialog(getDataService(), app, () -> versionGrid.getDataProvider().refreshAll(), v).open())));
            versionGrid.addColumn(new ComponentRenderer<>(v -> new Button(new Icon(VaadinIcon.TRASH), e -> new FluentDialog()
                    .addComponent(Translatable.createText(Messages.DELETE_VERSION_CONFIRM, v.getCode()))
                    .addConfirmButtons(p -> {
                        getDataService().delete(v);
                        versionGrid.getDataProvider().refreshAll();
                    }).show())));
            versionGrid.appendFooterRow().getCell(versionGrid.getColumns().get(0)).setComponent(Translatable.createButton(e -> new VersionEditorDialog(getDataService(), app, () -> versionGrid.getDataProvider().refreshAll(), null).open(), Messages.NEW_VERSION));
        }
        add(versionGrid);
        setHeader(Translatable.createLabel(Messages.VERSIONS));
    }
}
