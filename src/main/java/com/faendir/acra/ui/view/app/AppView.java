package com.faendir.acra.ui.view.app;

import com.faendir.acra.sql.data.AppRepository;
import com.faendir.acra.sql.model.App;
import com.faendir.acra.sql.model.Permission;
import com.faendir.acra.ui.annotation.RequiresAppPermission;
import com.faendir.acra.ui.view.app.tabs.BugTab;
import com.faendir.acra.ui.view.app.tabs.DeObfuscationTab;
import com.faendir.acra.ui.view.app.tabs.PropertiesTab;
import com.faendir.acra.ui.view.app.tabs.ReportTab;
import com.faendir.acra.ui.view.app.tabs.StatisticsTab;
import com.faendir.acra.ui.view.base.MyTabSheet;
import com.faendir.acra.ui.view.base.ParametrizedNamedView;
import com.faendir.acra.util.Style;
import com.faendir.acra.util.Utils;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.ui.VerticalLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.util.Pair;
import org.springframework.lang.NonNull;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Lukas
 * @since 13.05.2017
 */
@SpringView(name = "app")
@RequiresAppPermission(Permission.Level.VIEW)
public class AppView extends ParametrizedNamedView<Pair<App, String>> {
    @NonNull private final AppRepository appRepository;
    @NonNull private final ApplicationContext applicationContext;
    private MyTabSheet<App> tabSheet;

    @Autowired
    public AppView(@NonNull AppRepository appRepository, @NonNull ApplicationContext applicationContext) {
        super(Pair::getFirst);
        this.appRepository = appRepository;
        this.applicationContext = applicationContext;
    }

    @Override
    protected void enter(@NonNull Pair<App, String> parameter) {
        VerticalLayout content = new VerticalLayout(tabSheet);
        content.setSizeFull();
        Style.apply(content, Style.NO_PADDING, Style.PADDING_LEFT, Style.PADDING_RIGHT, Style.PADDING_BOTTOM);
        setCompositionRoot(content);
        tabSheet.setSizeFull();
        tabSheet.addSelectedTabChangeListener(e -> getNavigationManager().updatePageParameters(parameter.getFirst().getId() + "/" + e.getTabSheet().getSelectedTab().getCaption()));
        tabSheet.setInitialTab(parameter.getSecond());
    }

    @Override
    protected String getTitle(@NonNull Pair<App, String> appStringPair) {
        return appStringPair.getFirst().getName();
    }

    @Override
    public Pair<App, String> validateAndParseFragment(@NonNull String fragment) {
        String[] parameters = fragment.split("/");
        if (parameters.length > 0) {
            Optional<App> appOptional = appRepository.findByEncodedId(parameters[0]);
            if (appOptional.isPresent()) {
                App app = appOptional.get();
                tabSheet = new MyTabSheet<>(app, getNavigationManager(), Stream.of(BugTab.class, ReportTab.class, StatisticsTab.class, DeObfuscationTab.class, PropertiesTab.class)
                        .map(clazz -> Utils.getBeanIfPermissionGranted(applicationContext, app, clazz))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList()));
                if (parameters.length == 1) {
                    return Pair.of(app, tabSheet.getCaptions().get(0));
                } else if (tabSheet.getCaptions().contains(parameters[1])) {
                    return Pair.of(app, parameters[1]);
                }
            }
        }
        return null;
    }
}
