package com.faendir.acra.ui.view.app;

import com.faendir.acra.sql.data.AppRepository;
import com.faendir.acra.sql.model.App;
import com.faendir.acra.sql.model.Permission;
import com.faendir.acra.ui.annotation.RequiresAppPermission;
import com.faendir.acra.ui.view.base.MyTabSheet;
import com.faendir.acra.ui.view.base.ParametrizedBaseView;
import com.faendir.acra.ui.view.base.SingleParametrizedViewProvider;
import com.faendir.acra.util.MyNavigator;
import com.faendir.acra.util.Utils;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.spring.annotation.ViewScope;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.util.Pair;
import org.springframework.lang.NonNull;

import java.util.Optional;

/**
 * @author Lukas
 * @since 13.05.2017
 */
@SpringComponent
@ViewScope
@RequiresAppPermission(Permission.Level.VIEW)
public class AppView extends ParametrizedBaseView<Pair<App, String>> {
    @NonNull private final ApplicationContext applicationContext;

    @Autowired
    public AppView(@NonNull ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    protected void enter(@NonNull Pair<App, String> parameter) {
        MyTabSheet<App> tabSheet = new MyTabSheet<>(parameter.getFirst(), getNavigationManager(), Utils.getTabs(applicationContext, parameter.getFirst(), App.class));
        tabSheet.setSizeFull();
        tabSheet.addSelectedTabChangeListener(e -> getNavigationManager().updatePageParameters(parameter.getFirst().getId() + "/" + e.getTabSheet().getSelectedTab().getCaption()));
        if (tabSheet.getCaptions().contains(parameter.getSecond())) tabSheet.setInitialTab(parameter.getSecond());
        setCompositionRoot(tabSheet);
    }

    @SpringComponent
    @UIScope
    public static class Provider extends SingleParametrizedViewProvider<Pair<App, String>, AppView> {
        @NonNull private final AppRepository appRepository;

        @Autowired
        public Provider(@NonNull AppRepository appRepository) {
            super(AppView.class);
            this.appRepository = appRepository;
        }

        @Override
        protected boolean isValidParameter(Pair<App, String> parameter) {
            return parameter != null;
        }

        @Override
        protected Pair<App, String> parseParameter(String parameter) {
            String[] parameters = parameter.split(MyNavigator.SEPARATOR);
            if (parameters.length > 0) {
                Optional<App> app = appRepository.findByEncodedId(parameters[0]);
                if (app.isPresent()) {
                    return Pair.of(app.get(), parameters.length == 1 ? "" : parameters[1]);
                }
            }
            return null;
        }

        @Override
        protected App toApp(Pair<App, String> parameter) {
            return parameter.getFirst();
        }

        @Override
        public String getTitle(Pair<App, String> parameter) {
            return parameter.getFirst().getName();
        }

        @Override
        public String getId() {
            return "app";
        }
    }
}
