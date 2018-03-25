package com.faendir.acra.ui;

import com.faendir.acra.ui.view.ErrorView;
import com.faendir.acra.ui.view.base.BaseView;
import com.faendir.acra.ui.view.base.Path;
import com.faendir.acra.ui.view.base.SingleViewProvider;
import com.faendir.acra.util.MyNavigator;
import com.faendir.acra.util.Utils;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;
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
    @NonNull private final List<SingleViewProvider<?>> providers;

    @Autowired
    public NavigationManager(@NonNull UI ui, @NonNull Panel mainView, @NonNull Path mainPath, @NonNull MyNavigator navigator, @NonNull List<SingleViewProvider<?>> providers) {
        this.path = mainPath;
        this.navigator = navigator;
        this.providers = providers;
        this.navigator.init(ui, mainView);
        providers.forEach(navigator::addProvider);
        navigator.setErrorView(ErrorView.class);
        String target = Optional.ofNullable(ui.getPage().getLocation().getFragment()).orElse("").replace("!", "");
        ui.access(() -> navigateTo(target));
    }

    public void navigateTo(@NonNull Class<? extends BaseView> namedView, @Nullable String parameters, boolean newTab) {
        String target = Stream.of(path.isEmpty() ? null : path.getLast().getId(),
                providers.stream().filter(p -> namedView.equals(p.getClazz())).findAny().map(SingleViewProvider::getId).orElse(""), parameters)
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
            navigateTo(path.getLast().getId());
        }
    }

    public void updatePageParameters(@Nullable String parameters) {
        String target = navigator.getCurrentView().getClass().getAnnotation(SpringView.class).name() + (parameters == null ? "" : MyNavigator.SEPARATOR_CHAR + parameters);
        Path.Element element = path.goUp();
        path.goTo(element.getLabel(), target, this::navigateTo);
        navigator.getUI().getPage().setUriFragment(target, false);
    }
}
