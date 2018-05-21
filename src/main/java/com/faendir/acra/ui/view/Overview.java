package com.faendir.acra.ui.view;

import com.faendir.acra.model.User;
import com.faendir.acra.model.view.VApp;
import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.service.data.DataService;
import com.faendir.acra.ui.navigation.SingleViewProvider;
import com.faendir.acra.ui.view.app.AppView;
import com.faendir.acra.ui.view.base.BaseView;
import com.faendir.acra.ui.view.base.ConfigurationLabel;
import com.faendir.acra.ui.view.base.MyGrid;
import com.faendir.acra.ui.view.base.Popup;
import com.faendir.acra.util.Style;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Button;
import com.vaadin.ui.Grid;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;

/**
 * @author Lukas
 * @since 23.03.2017
 */
@SpringComponent
@ViewScope
public class Overview extends BaseView {
    @NonNull private final DataService dataService;
    private MyGrid<VApp> grid;

    @Autowired
    public Overview(@NonNull DataService dataService) {
        this.dataService = dataService;
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        grid = new MyGrid<>("Apps", dataService.getAppProvider());
        grid.setSizeToRows();
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.addColumn(VApp::getName, "name", "Name");
        grid.addColumn(VApp::getReportCount, "reportCount", "Reports");
        grid.addOnClickNavigation(getNavigationManager(), AppView.class, e -> String.valueOf(e.getItem().getId()));
        VerticalLayout layout = new VerticalLayout(grid);
        if (SecurityUtils.hasRole(User.Role.ADMIN)) {
            Button add = new Button("New App", e -> addApp());
            layout.addComponent(add);
        }
        Style.apply(layout, Style.NO_PADDING, Style.PADDING_LEFT, Style.PADDING_RIGHT, Style.PADDING_BOTTOM);
        setCompositionRoot(layout);
    }

    private void addApp() {
        TextField name = new TextField("Name");
        new Popup().setTitle("New App").addComponent(name).addCreateButton(popup -> {
            popup.clear().addComponent(new ConfigurationLabel(dataService.createNewApp(name.getValue()))).addCloseButton().show();
            grid.getDataProvider().refreshAll();
        }).show();
    }

    @SpringComponent
    @UIScope
    public static class Provider extends SingleViewProvider<Overview> {
        protected Provider() {
            super(Overview.class);
        }

        @Override
        public String getTitle(String parameter) {
            return "Overview";
        }

        @Override
        public String getId() {
            return "";
        }
    }
}
