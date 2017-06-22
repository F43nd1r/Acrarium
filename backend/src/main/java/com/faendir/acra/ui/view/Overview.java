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
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lukas
 * @since 23.03.2017
 */
@SpringView(name = "")
public class Overview extends NamedView {

    @NotNull private final DataManager dataManager;
    @NotNull private final MyGrid<App> grid;

    @Autowired
    public Overview(@NotNull DataManager dataManager) {
        this.dataManager = dataManager;
        grid = new MyGrid<>("Apps", Collections.emptyList());
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        grid.setItems(getApps());
        grid.setWidth(100, Unit.PERCENTAGE);
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.addColumn(App::getName, "Name");
        grid.addColumn(app -> dataManager.reportCountForApp(app.getId()), "Reports");
        grid.addItemClickListener(e -> getNavigationManager().navigateTo(AppView.class, e.getItem().getId()));
        VerticalLayout layout = new VerticalLayout(grid);
        if(SecurityUtils.hasRole(UserManager.ROLE_ADMIN)){
            Button add = new Button("New App", e -> addApp());
            layout.addComponent(add);
        }
        Style.apply(layout, Style.NO_PADDING, Style.PADDING_LEFT, Style.PADDING_RIGHT, Style.PADDING_BOTTOM);
        setCompositionRoot(layout);
    }

    @NotNull
    private List<App> getApps(){
        return dataManager.getApps().stream().filter(app -> SecurityUtils.hasPermission(app.getId(), Permission.Level.VIEW)).collect(Collectors.toList());
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
}
