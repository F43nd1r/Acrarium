package com.faendir.acra.ui.view;

import com.faendir.acra.mongod.data.DataManager;
import com.faendir.acra.mongod.model.App;
import com.faendir.acra.mongod.model.Permission;
import com.faendir.acra.mongod.user.UserManager;
import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.ui.view.base.MyGrid;
import com.faendir.acra.ui.view.base.NamedView;
import com.faendir.acra.util.Style;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lukas
 * @since 23.03.2017
 */
@UIScope
@Component
public class Overview extends NamedView {

    private final DataManager dataManager;
    private MyGrid<App> grid;

    @Autowired
    public Overview(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    private void addApp() {
        Window window = new Window("New App");
        TextField name = new TextField("Name");
        Button create = new Button("Create");
        create.addClickListener(e -> {
            dataManager.createNewApp(name.getValue());
            window.close();
            grid.setItems(getApps());

        });
        VerticalLayout layout = new VerticalLayout(name, create);
        window.setContent(layout);
        window.center();
        UI.getCurrent().addWindow(window);
    }

    private List<App> getApps(){
        return dataManager.getApps().stream().filter(app -> SecurityUtils.hasPermission(app.getId(), Permission.Level.VIEW)).collect(Collectors.toList());
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        grid = new MyGrid<>("Apps", getApps());
        grid.setSizeFull();
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.addColumn(App::getName, "Name");
        grid.addColumn(app -> dataManager.getReports(app.getId()).size(), "Reports");
        VerticalLayout layout = new VerticalLayout(grid);
        if(SecurityUtils.hasRole(UserManager.ROLE_ADMIN)){
            Button add = new Button("New App", e -> addApp());
            layout.addComponent(add);
        }
        Style.apply(layout, Style.NO_PADDING, Style.PADDING_LEFT, Style.PADDING_RIGHT, Style.PADDING_BOTTOM);
        setCompositionRoot(layout);
        grid.addItemClickListener(e -> getNavigationManager().navigateTo(AppView.class, e.getItem().getId()));
    }

    @Override
    public String getName() {
        return "";
    }
}
