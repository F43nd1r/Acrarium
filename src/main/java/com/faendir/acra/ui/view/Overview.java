package com.faendir.acra.ui.view;

import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.sql.data.AppRepository;
import com.faendir.acra.sql.data.ReportRepository;
import com.faendir.acra.sql.model.App;
import com.faendir.acra.sql.model.Permission;
import com.faendir.acra.sql.model.User;
import com.faendir.acra.sql.user.UserManager;
import com.faendir.acra.ui.view.base.ConfigurationLabel;
import com.faendir.acra.ui.view.base.MyGrid;
import com.faendir.acra.ui.view.base.NamedView;
import com.faendir.acra.ui.view.base.Popup;
import com.faendir.acra.util.BufferedDataProvider;
import com.faendir.acra.util.Style;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringView;
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
@SpringView(name = "")
public class Overview extends NamedView {
    @NonNull private final AppRepository appRepository;
    @NonNull private final ReportRepository reportRepository;
    @NonNull private final UserManager userManager;
    private MyGrid<App> grid;

    @Autowired
    public Overview(@NonNull AppRepository appRepository, @NonNull ReportRepository reportRepository, @NonNull UserManager userManager) {
        this.appRepository = appRepository;
        this.reportRepository = reportRepository;
        this.userManager = userManager;
    }

    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        grid = new MyGrid<>("Apps", new BufferedDataProvider<>(SecurityUtils.hasRole(UserManager.ROLE_ADMIN), (admin, pageable) -> admin ?
                appRepository.findAllByPermissionWithDefaultIncluded(SecurityUtils.getUsername(), Permission.Level.VIEW, pageable) :
                appRepository.findAllByPermissionWithDefaultExcluded(SecurityUtils.getUsername(), Permission.Level.VIEW, pageable), admin -> admin ?
                appRepository.countByPermissionWithDefaultIncluded(SecurityUtils.getUsername(), Permission.Level.VIEW) :
                appRepository.countByPermissionWithDefaultExcluded(SecurityUtils.getUsername(), Permission.Level.VIEW)));
        grid.setWidth(100, Unit.PERCENTAGE);
        grid.setSelectionMode(Grid.SelectionMode.NONE);
        grid.addColumn(App::getName, "Name");
        grid.addColumn(reportRepository::countAllByBugApp, "Reports");
        grid.addItemClickListener(e -> getNavigationManager().navigateTo(AppView.class, String.valueOf(e.getItem().getId())));
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
}
