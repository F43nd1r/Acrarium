package com.faendir.acra.ui.view;

import com.faendir.acra.data.App;
import com.faendir.acra.data.AppManager;
import com.faendir.acra.data.ReportManager;
import com.faendir.acra.util.Style;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.components.grid.FooterCell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author Lukas
 * @since 23.03.2017
 */
@UIScope
@Component
public class Overview extends NamedView {

    private final AppManager appManager;
    private final ReportManager reportManager;
    private MyGrid<App> grid;

    @Autowired
    public Overview(AppManager appManager, ReportManager reportManager) {
        this.appManager = appManager;
        this.reportManager = reportManager;
    }

    private void addApp() {
        Window window = new Window("New App");
        TextField name = new TextField("Name");
        Button create = new Button("Create");
        create.addClickListener(e -> {
            appManager.createNewApp(name.getValue());
            window.close();
            grid.setItems(appManager.getApps());

        });
        VerticalLayout layout = new VerticalLayout(name, create);
        window.setContent(layout);
        window.center();
        UI.getCurrent().addWindow(window);
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        grid = new MyGrid<>("Apps", appManager.getApps());
        grid.setSizeFull();
        Grid.Column column = grid.addColumn(App::getName, "Name");
        grid.addColumn(app -> String.valueOf(reportManager.getReports(app.getId()).size()), "Reports");
        FooterCell footerCell = grid.appendFooterRow().getCell(column.getId());
        Button add = new Button("New App");
        add.setSizeFull();
        add.addClickListener(e -> addApp());
        footerCell.setComponent(add);
        VerticalLayout layout = new VerticalLayout(grid);
        Style.apply(layout, Style.NO_PADDING, Style.PADDING_LEFT, Style.PADDING_RIGHT, Style.PADDING_BOTTOM);
        setCompositionRoot(layout);
        grid.addItemClickListener(e -> getNavigationManager().navigateTo(AppView.class, e.getItem().getId()));
    }

    @Override
    public String getName() {
        return "";
    }
}
