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

package com.faendir.acra.ui.navigation;

import com.faendir.acra.ui.view.ErrorView;
import com.faendir.acra.ui.view.base.navigation.BaseView;
import com.faendir.acra.ui.view.base.navigation.Path;
import com.faendir.acra.util.Utils;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Deque;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Lukas
 * @since 14.05.2017
 */
@UIScope
@Component
@Configurable
public class NavigationManager implements Serializable {
    @NonNull private final Path path;
    @NonNull private final MyNavigator navigator;

    @Autowired
    public NavigationManager(@NonNull UI ui, @NonNull Panel mainView, @NonNull Path mainPath, @NonNull MyNavigator navigator) {
        this.path = mainPath;
        this.navigator = navigator;
        this.navigator.init(ui, mainView);
        this.navigator.addViewActivationListener(e -> {
            Deque<MyNavigator.HierarchyElement> hierarchy = navigator.getHierarchy();
            path.set(hierarchy);
            SingleViewProvider provider = (SingleViewProvider) hierarchy.getLast().getProvider();
            String title = provider.getTitle(provider.getParameters(hierarchy.getLast().getNavState()));
            if (hierarchy.size() > 1) {
                title += " - Acrarium";
            }
            ui.getPage().setTitle(title);
        });
        navigator.setErrorView(ErrorView.class);
        String target = Optional.ofNullable(ui.getPage().getLocation().getFragment()).orElse("").replace("!", "");
        ui.access(() -> navigateTo(target));
    }

    public void navigateTo(@NonNull Class<? extends BaseView> namedView, @Nullable String parameters, boolean newTab) {
        String target = Stream.of(path.asUrlFragment(), navigator.getViewProvider(namedView).map(SingleViewProvider::getId).orElse(""), parameters)
                .filter(s -> s != null && !s.isEmpty())
                .collect(Collectors.joining(MyNavigator.SEPARATOR));
        if (newTab) {
            navigator.getUI().getPage().open(Utils.getUrlWithFragment(target), "_blank", false);
        } else if (path.isEmpty() || !path.getLast().getId().equals(target)) {
            navigateTo(target);
        }
    }

    public void cleanNavigateTo(@NonNull Class<? extends BaseView> namedView) {
        cleanHistory();
        navigateTo(namedView, "", false);
    }

    private void navigateTo(@NonNull String fragment) {
        navigator.navigateTo(fragment);
    }

    private void cleanHistory() {
        path.clear();
    }

    public void navigateBack() {
        if (path.getSize() < 2) {
            if (path.isEmpty() || !"".equals(path.getLast().getId())) {
                path.goUp();
                navigateTo("");
            }
        } else {
            path.goUp();
            navigateTo(path.asUrlFragment());
        }
    }

    public void updatePageParameters(@Nullable String parameters) {
        String currentView = navigator.getViewProvider(navigator.getCurrentView().getClass()).map(SingleViewProvider::getId).orElse("");
        Path.Element element = path.goUp();
        String hierarchy = path.asUrlFragment();
        String target = "!" + (hierarchy.isEmpty() ? "" : hierarchy + MyNavigator.SEPARATOR_CHAR) + currentView + (parameters == null ?
                                                                                                                           "" :
                                                                                                                           MyNavigator.SEPARATOR_CHAR + parameters);
        path.goTo(element.getLabel(), currentView + (parameters == null ? "" : MyNavigator.SEPARATOR_CHAR + parameters));
        navigator.getUI().getPage().setUriFragment(target, false);
    }

    public void openInNewTab(@Nullable String parameters) {
        String currentView = navigator.getViewProvider(navigator.getCurrentView().getClass()).map(SingleViewProvider::getId).orElse("");
        Path.Element element = path.goUp();
        String hierarchy = path.asUrlFragment();
        path.goTo(element);
        String target = "!" + (hierarchy.isEmpty() ? "" : hierarchy + MyNavigator.SEPARATOR_CHAR) + currentView + (parameters == null ?
                                                                                                                           "" :
                                                                                                                           MyNavigator.SEPARATOR_CHAR + parameters);
        navigator.getUI().getPage().open(Utils.getUrlWithFragment(target), "_blank", false);
    }
}
