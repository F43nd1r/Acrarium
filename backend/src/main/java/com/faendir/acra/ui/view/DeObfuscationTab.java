package com.faendir.acra.ui.view;

import com.faendir.acra.data.MappingManager;
import com.faendir.acra.data.ProguardMapping;
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
public class DeObfuscationTab extends MyGrid<ProguardMapping> {
    private final String app;
    private final MappingManager mappingManager;
    private boolean validNumber;
    private boolean validFile;

    public DeObfuscationTab(String app, MappingManager mappingManager) {
        super("De-Obfuscation", mappingManager.getMappings(app));
        this.app = app;
        this.mappingManager = mappingManager;
        Column column = addColumn(mapping -> String.valueOf(mapping.getVersion()), "Version");
        setSizeFull();
        Button add = new Button("Add File", e -> addFile());
        add.setSizeFull();
        appendFooterRow().getCell(column).setComponent(add);
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
            mappingManager.addMapping(app, Integer.parseInt(version.getValue()), out.toString());
            setItems(mappingManager.getMappings(app));
            window.close();
        });
        confirm.setSizeFull();
        VerticalLayout layout = new VerticalLayout(version, upload, progressBar, confirm);
        window.setContent(layout);
        window.center();
        UI.getCurrent().addWindow(window);
    }
}
