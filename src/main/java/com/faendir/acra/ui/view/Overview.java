package com.faendir.acra.ui.view;

import com.faendir.acra.model.QApp;
import com.faendir.acra.model.QBug;
import com.faendir.acra.model.QReport;
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
import com.vaadin.ui.Alignment;
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
        grid.setResponsive(true);
        grid.setSizeToRows();
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.addColumn(VApp::getName, QApp.app.name, "Name");
        grid.addColumn(VApp::getBugCount, QBug.bug.countDistinct(), "Bugs");
        grid.addColumn(VApp::getReportCount, QReport.report.count(), "Reports");
        grid.addOnClickNavigation(getNavigationManager(), AppView.class, e -> String.valueOf(e.getItem().getId()));
        VerticalLayout layout = new VerticalLayout();
        if (SecurityUtils.hasRole(User.Role.ADMIN)) {
            Button add = new Button("New App", e -> addApp());
            layout.addComponent(add);
        }
        layout.addComponent(grid);
        Style.apply(layout, Style.NO_PADDING, Style.PADDING_LEFT, Style.PADDING_RIGHT, Style.PADDING_BOTTOM, Style.MAX_WIDTH_728);
        VerticalLayout root = new VerticalLayout(layout);
        root.setSpacing(false);
        root.setSizeFull();
        root.setComponentAlignment(layout, Alignment.TOP_CENTER);
        Style.apply(root, Style.NO_PADDING);
        setCompositionRoot(root);
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
            return "Acrarium";
        }

        @Override
        public String getId() {
            return "";
        }
    }
}
