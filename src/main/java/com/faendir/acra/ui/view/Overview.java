package com.faendir.acra.ui.view;

import com.faendir.acra.dataprovider.BufferedDataProvider;
import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.sql.data.AppRepository;
import com.faendir.acra.sql.data.ReportRepository;
import com.faendir.acra.sql.model.App;
import com.faendir.acra.sql.model.Permission;
import com.faendir.acra.sql.model.User;
import com.faendir.acra.sql.user.UserManager;
import com.faendir.acra.ui.view.app.AppView;
import com.faendir.acra.ui.view.base.BaseView;
import com.faendir.acra.ui.view.base.ConfigurationLabel;
import com.faendir.acra.ui.view.base.MyGrid;
import com.faendir.acra.ui.view.base.Popup;
import com.faendir.acra.ui.view.base.SingleViewProvider;
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
import org.springframework.data.util.Pair;
import org.springframework.lang.NonNull;

/**
 * @author Lukas
 * @since 23.03.2017
 */
@SpringComponent
@ViewScope
public class Overview extends BaseView {
    @NonNull private final AppRepository appRepository;
    @NonNull private final ReportRepository reportRepository;
    @NonNull private final UserManager userManager;
    @NonNull private final BufferedDataProvider.Factory factory;
    private MyGrid<App> grid;

    @Autowired
    public Overview(@NonNull AppRepository appRepository, @NonNull ReportRepository reportRepository, @NonNull UserManager userManager,
            @NonNull BufferedDataProvider.Factory factory) {
        this.appRepository = appRepository;
        this.reportRepository = reportRepository;
        this.userManager = userManager;
        this.factory = factory;
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        grid = new MyGrid<>("Apps", factory.create(SecurityUtils.hasRole(UserManager.ROLE_ADMIN), (admin, pageable) -> admin ?
                appRepository.findAllByPermissionWithDefaultIncluded(SecurityUtils.getUsername(), Permission.Level.VIEW, pageable) :
                appRepository.findAllByPermissionWithDefaultExcluded(SecurityUtils.getUsername(), Permission.Level.VIEW, pageable), admin -> admin ?
                appRepository.countByPermissionWithDefaultIncluded(SecurityUtils.getUsername(), Permission.Level.VIEW) :
                appRepository.countByPermissionWithDefaultExcluded(SecurityUtils.getUsername(), Permission.Level.VIEW)));
        grid.setSizeToRows();
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.addColumn(App::getName, "Name");
        grid.addColumn(reportRepository::countAllByBugApp, "Reports");
        grid.addOnClickNavigation(getNavigationManager(), AppView.class, e -> String.valueOf(e.getItem().getId()));
        VerticalLayout layout = new VerticalLayout(grid);
        if (SecurityUtils.hasRole(UserManager.ROLE_ADMIN)) {
            Button add = new Button("New App", e -> addApp());
            layout.addComponent(add);
        }
        Style.apply(layout, Style.NO_PADDING, Style.PADDING_LEFT, Style.PADDING_RIGHT, Style.PADDING_BOTTOM);
        setCompositionRoot(layout);
    }

    private void addApp() {
        TextField name = new TextField("Name");
        new Popup().setTitle("New App").addComponent(name).addCreateButton(popup -> {
            Pair<User, String> userPasswordPair = userManager.createReporterUser();
            appRepository.save(new App(name.getValue(), userPasswordPair.getFirst()));
            grid.getDataProvider().refreshAll();
            popup.clear().addComponent(new ConfigurationLabel(userPasswordPair.getFirst().getUsername(), userPasswordPair.getSecond())).addCloseButton().show();
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
