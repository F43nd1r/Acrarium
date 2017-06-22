package com.faendir.acra.ui.view;

import com.faendir.acra.mongod.data.DataManager;
import com.faendir.acra.mongod.model.App;
import com.faendir.acra.mongod.model.Permission;
import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.ui.view.annotation.RequiresAppPermission;
import com.faendir.acra.ui.view.base.NamedView;
import com.faendir.acra.ui.view.tabs.BugTab;
import com.faendir.acra.ui.view.tabs.DeObfuscationTab;
import com.faendir.acra.ui.view.tabs.PropertiesTab;
import com.faendir.acra.ui.view.tabs.ReportTab;
import com.faendir.acra.ui.view.tabs.StatisticsTab;
import com.faendir.acra.util.Style;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

/**
 * @author Lukas
 * @since 13.05.2017
 */
@SpringView(name = AppView.NAME)
@RequiresAppPermission(Permission.Level.VIEW)
public class AppView extends NamedView {
    static final String NAME = "app";
    private static final List<String> CAPTIONS = Arrays.asList(BugTab.CAPTION, ReportTab.CAPTION, StatisticsTab.CAPTION,
            DeObfuscationTab.CAPTION);

    @NotNull private final DataManager dataManager;
    @Nullable private App app;

    @Autowired
    public AppView(@NotNull DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    public void enter(@NotNull ViewChangeListener.ViewChangeEvent event) {
        String[] parameters = event.getParameters().split("/");
        app = dataManager.getApp(parameters[0]);
        if(app != null) {
            TabSheet tabSheet = new TabSheet(new BugTab(app.getId(), getNavigationManager(), dataManager), new ReportTab(app.getId(), getNavigationManager(), dataManager), new StatisticsTab(app.getId(), dataManager), new DeObfuscationTab(app.getId(), dataManager));
            if (SecurityUtils.hasPermission(app.getId(), Permission.Level.ADMIN)) {
                tabSheet.addComponent(new PropertiesTab(app, dataManager, getNavigationManager()));
            }
            tabSheet.setSizeFull();
            VerticalLayout content = new VerticalLayout(tabSheet);
            content.setSizeFull();
            Style.apply(content, Style.NO_PADDING, Style.PADDING_LEFT, Style.PADDING_RIGHT, Style.PADDING_BOTTOM);
            setCompositionRoot(content);
            setSizeFull();
            if (parameters.length >= 2) {
                for (com.vaadin.ui.Component component : tabSheet) {
                    if (component.getCaption().equals(parameters[1])) {
                        tabSheet.setSelectedTab(component);
                        break;
                    }
                }
            }
            tabSheet.addSelectedTabChangeListener(e -> getNavigationManager().updatePageParameters(app.getId() + "/" + e.getTabSheet().getSelectedTab().getCaption()));
        }
    }

    @Override
    public String getApp(@NotNull String fragment) {
        return fragment.split("/")[0];
    }

    @Override
    public boolean validate(@Nullable String fragment) {
        if (fragment == null) return false;
        String[] split = fragment.split("/");
        return split.length >= 1 && split.length <= 2 && dataManager.getApp(split[0]) != null
                && (split.length != 2 || CAPTIONS.contains(split[1]) || PropertiesTab.CAPTION.equals(split[1]) && SecurityUtils.hasPermission(split[0], Permission.Level.ADMIN));
    }
}
