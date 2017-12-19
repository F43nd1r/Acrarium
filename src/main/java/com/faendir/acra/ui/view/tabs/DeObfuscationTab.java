package com.faendir.acra.ui.view.tabs;

import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.sql.data.ProguardMappingRepository;
import com.faendir.acra.sql.model.App;
import com.faendir.acra.sql.model.Permission;
import com.faendir.acra.sql.model.ProguardMapping;
import com.faendir.acra.ui.NavigationManager;
import com.faendir.acra.ui.view.base.MyGrid;
import com.faendir.acra.ui.view.base.MyTabSheet;
import com.faendir.acra.util.Style;
import com.vaadin.server.UserError;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import org.springframework.lang.NonNull;

import java.io.ByteArrayOutputStream;

/**
 * @author Lukas
 * @since 19.05.2017
 */
@SpringComponent
@ViewScope
public class DeObfuscationTab extends VerticalLayout implements MyTabSheet.Tab{
    public static final String CAPTION = "De-Obfuscation";
    @NonNull private final ProguardMappingRepository mappingRepository;
    private boolean validNumber;
    private boolean validFile;

    public DeObfuscationTab(@NonNull ProguardMappingRepository mappingRepository) {
        this.mappingRepository = mappingRepository;
        setCaption(CAPTION);
    }

    @Override
    public Component createContent(@NonNull App app, @NonNull NavigationManager navigationManager) {
        MyGrid<ProguardMapping> grid = new MyGrid<>(null, mappingRepository.findAllByApp(app));
        grid.addColumn(ProguardMapping::getVersionCode, "Version");
        grid.setWidth(100, Unit.PERCENTAGE);
        addComponent(grid);
        setSizeFull();
        Style.NO_PADDING.apply(this);
        if (SecurityUtils.hasPermission(app, Permission.Level.EDIT)) {
            addComponent(new Button("Add File", e -> {
                Window window = new Window("New Mapping Configuration");
                Button confirm = new Button("Confirm");
                confirm.setEnabled(false);
                TextField version = new TextField("Version code", e1 -> {
                    try {
                        //noinspection ResultOfMethodCallIgnored
                        Integer.parseInt(e1.getValue());
                        validNumber = true;
                        confirm.setEnabled(validFile);
                        ((AbstractComponent) e1.getComponent()).setComponentError(null);
                    } catch (NumberFormatException ex) {
                        validNumber = false;
                        confirm.setEnabled(false);
                        ((AbstractComponent) e1.getComponent()).setComponentError(new UserError("Not a number"));
                    }
                });
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                Upload upload = new Upload("Mapping file:", (filename, mimeType) -> out);
                ProgressBar progressBar = new ProgressBar();
                progressBar.setSizeFull();
                upload.addProgressListener((readBytes, contentLength) -> getUI().access(() -> progressBar.setValue((float) readBytes / contentLength)));
                upload.addSucceededListener(e1 -> {
                    validFile = true;
                    confirm.setEnabled(validNumber);
                });
                upload.addFailedListener(e1 -> {
                    validFile = false;
                    confirm.setEnabled(false);
                });
                upload.setSizeFull();
                confirm.addClickListener(e1 -> {
                    mappingRepository.save(new ProguardMapping(app, Integer.parseInt(version.getValue()), out.toString()));
                    grid.setItems(mappingRepository.findAllByApp(app));
                    window.close();
                });
                confirm.setSizeFull();
                VerticalLayout layout = new VerticalLayout(version, upload, progressBar, confirm);
                window.setContent(layout);
                window.center();
                UI.getCurrent().addWindow(window);
            }));
        }
        setExpandRatio(grid, 1);
        return this;
    }
}
