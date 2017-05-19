package com.faendir.acra.ui.view;

import com.faendir.acra.data.App;
import com.faendir.acra.data.AppManager;
import com.faendir.acra.data.MappingManager;
import com.faendir.acra.data.ReportManager;
import com.faendir.acra.util.Style;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Label;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Lukas
 * @since 13.05.2017
 */
@UIScope
@Component
public class AppView extends NamedView {

    private final AppManager appManager;
    private final ReportManager reportManager;
    private final MappingManager mappingManager;

    @Autowired
    public AppView(AppManager appManager, ReportManager reportManager, MappingManager mappingManager) {
        this.appManager = appManager;
        this.reportManager = reportManager;
        this.mappingManager = mappingManager;
    }

    @Override
    public String getName() {
        return "app";
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        App app = appManager.getApp(event.getParameters());
        VerticalLayout statistics = new VerticalLayout(new Label("Coming soon"));
        statistics.setCaption("Statistics");
        statistics.setSizeFull();
        TabSheet tabSheet = new TabSheet(new BugTab(app.getId(), getNavigationManager(), reportManager),
                new ReportList(app.getId(), getNavigationManager(), reportManager),
                statistics, new DeObfuscationTab(app.getId(), mappingManager), new PropertiesTab(app));
        tabSheet.setSizeFull();
        VerticalLayout content = new VerticalLayout(tabSheet);
        content.setSizeFull();
        Style.apply(content, Style.NO_PADDING, Style.PADDING_LEFT, Style.PADDING_RIGHT, Style.PADDING_BOTTOM);
        setCompositionRoot(content);
        setSizeFull();
    }
}
