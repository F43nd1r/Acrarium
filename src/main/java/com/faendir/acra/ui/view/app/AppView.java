/*
 * (C) Copyright 2018 Lukas Morawietz (https://github.com/F43nd1r)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.faendir.acra.ui.view.app;

import com.faendir.acra.model.App;
import com.faendir.acra.model.Permission;
import com.faendir.acra.service.DataService;
import com.faendir.acra.ui.annotation.RequiresAppPermission;
import com.faendir.acra.ui.navigation.MyNavigator;
import com.faendir.acra.ui.navigation.SingleParametrizedViewProvider;
import com.faendir.acra.ui.view.app.tabs.AppTab;
import com.faendir.acra.ui.view.base.layout.MyTabSheet;
import com.faendir.acra.ui.view.base.navigation.ParametrizedBaseView;
import com.vaadin.spring.annotation.SpringComponent;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.spring.annotation.ViewScope;
import com.vaadin.ui.Panel;
import com.vaadin.ui.themes.AcraTheme;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.util.Pair;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

/**
 * @author Lukas
 * @since 13.05.2017
 */
@SpringComponent
@ViewScope
@RequiresAppPermission(Permission.Level.VIEW)
public class AppView extends ParametrizedBaseView<Pair<App, String>> {
    private final List<AppTab> tabs;

    @Autowired
    public AppView(@Lazy List<AppTab> tabs) {
        this.tabs = tabs;
    }

    @Override
    protected void enter(@NonNull Pair<App, String> parameter) {
        MyTabSheet<App> tabSheet = new MyTabSheet<>(parameter.getFirst(), getNavigationManager(), tabs);
        tabSheet.setSizeFull();
        tabSheet.addSelectedTabChangeListener(e -> getNavigationManager().updatePageParameters(parameter.getFirst().getId() + "/" + e.getTabSheet().getSelectedTab().getId()));
        tabSheet.addMiddleClickListener(e -> getNavigationManager().openInNewTab(parameter.getFirst().getId() + "/" + e.getTab().getId()));
        tabSheet.guessInitialTab(parameter.getSecond());
        Panel panel = new Panel(tabSheet);
        panel.setSizeFull();
        panel.addStyleNames(AcraTheme.NO_BORDER, AcraTheme.NO_BACKGROUND, AcraTheme.NO_PADDING, AcraTheme.PADDING_LEFT, AcraTheme.PADDING_RIGHT);
        setCompositionRoot(panel);
    }

    @SpringComponent
    @UIScope
    public static class Provider extends SingleParametrizedViewProvider<Pair<App, String>, AppView> {
        @NonNull private final DataService dataService;

        @Autowired
        public Provider(@NonNull DataService dataService) {
            super(AppView.class);
            this.dataService = dataService;
        }

        @Override
        protected boolean isValidParameter(Pair<App, String> parameter) {
            return parameter != null;
        }

        @Override
        protected Pair<App, String> parseParameter(String parameter) {
            String[] parameters = parameter.split(MyNavigator.SEPARATOR);
            if (parameters.length > 0) {
                Optional<App> app = dataService.findApp(parameters[0]);
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
