package com.faendir.acra.ui.view.tabs;

import com.faendir.acra.mongod.data.DataManager;
import com.faendir.acra.mongod.model.Permission;
import com.faendir.acra.mongod.model.ProguardMapping;
import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.ui.view.base.MyGrid;
import com.faendir.acra.util.Style;
import com.vaadin.ui.Button;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

import java.io.ByteArrayOutputStream;


/**
 * @author Lukas
 * @since 19.05.2017
 */
public class DeObfuscationTab extends VerticalLayout {
    public static final String CAPTION = "De-Obfuscation";
    private final String app;
    private final DataManager dataManager;
    private final MyGrid<ProguardMapping> grid;
    private boolean validNumber;
    private boolean validFile;

    public DeObfuscationTab(String app, DataManager dataManager) {
        setCaption(CAPTION);
        grid = new MyGrid<>(null, dataManager.getMappings(app));
        this.app = app;
        this.dataManager = dataManager;
        grid.addColumn(ProguardMapping::getVersion, "Version");
        grid.setWidth(100, Unit.PERCENTAGE);
        addComponent(grid);
        setSizeFull();
        Style.NO_PADDING.apply(this);
        if (SecurityUtils.hasPermission(app, Permission.Level.EDIT)) {
            addComponent(new Button("Add File", e -> addFile()));
        }
        setExpandRatio(grid, 1);

    }

    private void addFile() {
        Window window = new Window("New Mapping Configuration");
        Button confirm = new Button("Confirm");
        confirm.setEnabled(false);
        TextField version = new TextField("Version code", e -> {
            try {
                //noinspection ResultOfMethodCallIgnored
                Integer.parseInt(e.getValue());
                validNumber = true;
                confirm.setEnabled(validFile);
            } catch (NumberFormatException ex) {
                validNumber = false;
                confirm.setEnabled(false);
            }
        });
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Upload upload = new Upload("Mapping file:", (filename, mimeType) -> out);
        ProgressBar progressBar = new ProgressBar();
        progressBar.setSizeFull();
        upload.addProgressListener((readBytes, contentLength) -> progressBar.setValue(readBytes / contentLength));
        upload.addSucceededListener(e -> {
            validFile = true;
            confirm.setEnabled(validNumber);
        });
        upload.addFailedListener(e -> {
            validFile = false;
            confirm.setEnabled(false);
        });
        upload.setSizeFull();
        confirm.addClickListener(e -> {
            dataManager.addMapping(app, Integer.parseInt(version.getValue()), out.toString());
            grid.setItems(dataManager.getMappings(app));
            window.close();
        });
        confirm.setSizeFull();
        VerticalLayout layout = new VerticalLayout(version, upload, progressBar, confirm);
        window.setContent(layout);
        window.center();
        UI.getCurrent().addWindow(window);
    }
}
