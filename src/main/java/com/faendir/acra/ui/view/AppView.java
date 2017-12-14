package com.faendir.acra.ui.view;

import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.sql.data.DataManager;
import com.faendir.acra.sql.model.App;
import com.faendir.acra.sql.model.Permission;
import com.faendir.acra.ui.view.annotation.RequiresAppPermission;
import com.faendir.acra.ui.view.base.MyTabSheet;
import com.faendir.acra.ui.view.base.ParametrizedNamedView;
import com.faendir.acra.ui.view.tabs.BugTab;
import com.faendir.acra.ui.view.tabs.DeObfuscationTab;
import com.faendir.acra.ui.view.tabs.PropertiesTab;
import com.faendir.acra.ui.view.tabs.ReportTab;
import com.faendir.acra.ui.view.tabs.StatisticsTab;
import com.faendir.acra.util.Style;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.VerticalLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.lang.NonNull;

import java.util.Optional;

/**
 * @author Lukas
 * @since 13.05.2017
 */
@SpringView(name = "app")
@RequiresAppPermission(Permission.Level.VIEW)
public class AppView extends ParametrizedNamedView<Pair<App, String>> {

    @NonNull private final DataManager dataManager;
    private MyTabSheet tabSheet;

    @Autowired
    public AppView(@NonNull DataManager dataManager) {
        super(Pair::getFirst);
        this.dataManager = dataManager;
    }

    @Override
    protected void enter(@NonNull Pair<App, String> parameter) {
        VerticalLayout content = new VerticalLayout(tabSheet);
        content.setSizeFull();
        Style.apply(content, Style.NO_PADDING, Style.PADDING_LEFT, Style.PADDING_RIGHT, Style.PADDING_BOTTOM);
        setCompositionRoot(content);
        setSizeFull();
        tabSheet.setSizeFull();
        tabSheet.addSelectedTabChangeListener(e -> getNavigationManager().updatePageParameters(parameter.getFirst().getId() + "/" + e.getTabSheet().getSelectedTab().getCaption()));
        tabSheet.setInitialTab(parameter.getSecond());
    }

    @Override
    public Pair<App, String> validateAndParseFragment(@NonNull String fragment) {
        String[] parameters = fragment.split("/");
        if(parameters.length > 0) {
            Optional<App> appOptional = dataManager.getApp(parameters[0]);
            if (appOptional.isPresent()) {
                App app = appOptional.get();
                tabSheet = new MyTabSheet(app, dataManager, getNavigationManager(), new BugTab(), new ReportTab(), new StatisticsTab(), new DeObfuscationTab());
                if (SecurityUtils.hasPermission(app, Permission.Level.ADMIN)) {
                    tabSheet.addTab((MyTabSheet.Tab) new PropertiesTab());
                }
                if (parameters.length == 1) {
                    return Pair.of(app, tabSheet.getCaptions().get(0));
                } else if (tabSheet.getCaptions().contains(parameters[1])){
                    return Pair.of(app, parameters[1]);
                }
            }
        }
        return null;
    }
}
