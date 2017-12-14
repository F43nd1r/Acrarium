package com.faendir.acra.ui.view;

import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.sql.data.DataManager;
import com.faendir.acra.sql.model.App;
import com.faendir.acra.sql.model.Permission;
import com.faendir.acra.sql.user.UserManager;
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
import org.springframework.lang.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lukas
 * @since 23.03.2017
 */
@SpringView(name = "")
public class Overview extends NamedView {

    @NonNull private final DataManager dataManager;
    @NonNull private final MyGrid<App> grid;

    @Autowired
    public Overview(@NonNull DataManager dataManager) {
        this.dataManager = dataManager;
        grid = new MyGrid<>("Apps", Collections.emptyList());
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        grid.setItems(getApps());
        grid.setWidth(100, Unit.PERCENTAGE);
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.addColumn(App::getName, "Name");
        grid.addColumn(dataManager::reportCountForApp, "Reports");
        grid.addItemClickListener(e -> getNavigationManager().navigateTo(AppView.class, String.valueOf(e.getItem().getId())));
        VerticalLayout layout = new VerticalLayout(grid);
        if(SecurityUtils.hasRole(UserManager.ROLE_ADMIN)){
            Button add = new Button("New App", e -> addApp());
            layout.addComponent(add);
        }
        Style.apply(layout, Style.NO_PADDING, Style.PADDING_LEFT, Style.PADDING_RIGHT, Style.PADDING_BOTTOM);
        setCompositionRoot(layout);
    }

    @NonNull
    private List<App> getApps(){
        return dataManager.getApps().stream().filter(app -> SecurityUtils.hasPermission(app, Permission.Level.VIEW)).collect(Collectors.toList());
    }

    private void addApp() {
        Window window = new Window("New App");
        TextField name = new TextField("Name");
        Button create = new Button("Create");
        create.addClickListener(e -> {
            dataManager.newApp(name.getValue());
            window.close();
            grid.setItems(getApps());

        });
        VerticalLayout layout = new VerticalLayout(name, create);
        window.setContent(layout);
        window.center();
        UI.getCurrent().addWindow(window);
    }

    @Nullable
    public App parseFragment(@NonNull String fragment) {
        return null;
    }

    public boolean validate(@Nullable String fragment) {
        return true;
    }
}
