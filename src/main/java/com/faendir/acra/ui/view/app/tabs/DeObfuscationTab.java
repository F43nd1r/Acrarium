package com.faendir.acra.ui.view.app.tabs;

import com.faendir.acra.model.App;
import com.faendir.acra.model.Permission;
import com.faendir.acra.model.ProguardMapping;
import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.service.data.DataService;
import com.faendir.acra.ui.navigation.NavigationManager;
import com.faendir.acra.ui.view.base.InMemoryUpload;
import com.faendir.acra.ui.view.base.MyGrid;
import com.faendir.acra.ui.view.base.Popup;
import com.faendir.acra.ui.view.base.ValidatedField;
import com.faendir.acra.util.Style;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.renderers.ButtonRenderer;
import org.springframework.lang.NonNull;
import org.vaadin.risto.stepper.IntStepper;

/**
 * @author Lukas
 * @since 19.05.2017
 */
@SpringComponent
@ViewScope
public class DeObfuscationTab implements AppTab {
    @NonNull private final DataService dataService;

    public DeObfuscationTab(@NonNull DataService dataService) {
        this.dataService = dataService;
    }

    @Override
    public Component createContent(@NonNull App app, @NonNull NavigationManager navigationManager) {
        VerticalLayout layout = new VerticalLayout();
        MyGrid<ProguardMapping> grid = new MyGrid<>(null, dataService.getMappingProvider(app));
        grid.setSizeToRows();
        grid.addColumn(ProguardMapping::getVersionCode, "Version");
        if (SecurityUtils.hasPermission(app, Permission.Level.EDIT)) {
            grid.addColumn(report -> "Delete", new ButtonRenderer<>(e -> new Popup().setTitle("Confirm")
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
        return layout;
    }

    @Override
    public String getCaption() {
        return "De-Obfuscation";
    }

    @Override
    public int getOrder() {
        return 3;
    }
}
