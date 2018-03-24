package com.faendir.acra.ui;

import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.ui.annotation.RequiresAppPermission;
import com.faendir.acra.ui.annotation.RequiresRole;
import com.faendir.acra.ui.view.ErrorView;
import com.faendir.acra.ui.view.base.NamedView;
import com.faendir.acra.ui.view.base.ParametrizedNamedView;
import com.faendir.acra.ui.view.base.Path;
import com.faendir.acra.util.MyNavigator;
import com.faendir.acra.util.Utils;
import com.vaadin.navigator.View;
import com.vaadin.spring.access.ViewAccessControl;
import com.vaadin.spring.access.ViewInstanceAccessControl;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Optional;

/**
 * @author Lukas
 * @since 14.05.2017
 */
@UIScope
@Component
@Configurable
public class NavigationManager implements ViewAccessControl, ViewInstanceAccessControl, Serializable {
    @NonNull private final Path path;
    @NonNull private final MyNavigator navigator;
    @NonNull private final ApplicationContext applicationContext;

    @Autowired
    public NavigationManager(@NonNull UI ui, @NonNull Panel mainView, @NonNull Path mainPath, @NonNull MyNavigator navigator, @NonNull ApplicationContext applicationContext) {
        this.path = mainPath;
        this.navigator = navigator;
        this.applicationContext = applicationContext;
        this.navigator.init(ui, mainView);
        navigator.setErrorView(ErrorView.class);
        String target = Optional.ofNullable(ui.getPage().getLocation().getFragment()).orElse("").replace("!", "");
        ui.access(() -> navigateTo(target));
    }

    public void navigateTo(@NonNull Class<? extends NamedView> namedView, @Nullable String parameters, boolean newTab) {
        String target = namedView.getAnnotation(SpringView.class).name() + (parameters == null ? "" : "/" + parameters);
        if (newTab) {
            navigator.getUI().getPage().open(Utils.getUrlWithFragment(target), "_blank", false);
        } else if (path.isEmpty() || !path.getLast().getId().equals(target)) {
            navigateTo(target);
        }
    }

    public void cleanNavigateTo(@NonNull Class<? extends NamedView> namedView) {
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
            if (!"".equals(path.getLast().getId())) {
                path.goUp();
                navigateTo("");
            }
        } else {
            path.goUp();
            navigateTo(path.getLast().getId());
        }
    }

    public void updatePageParameters(@Nullable String parameters) {
        String target = navigator.getCurrentView().getClass().getAnnotation(SpringView.class).name() + (parameters == null ? "" : "/" + parameters);
        Path.Element element = path.goUp();
        path.goTo(element.getLabel(), target, this::navigateTo);
        navigator.getUI().getPage().setUriFragment(target, false);
    }

    @Override
    public boolean isAccessGranted(UI ui, @NonNull String beanName) {
        RequiresRole annotation = applicationContext.findAnnotationOnBean(beanName, RequiresRole.class);
        return annotation == null || SecurityUtils.hasRole(annotation.value());
    }

    @Override
    public boolean isAccessGranted(UI ui, @NonNull String beanName, @NonNull View view) {
        boolean result = false;
        if ((view instanceof NamedView)) {
            if ((view instanceof ParametrizedNamedView)) {
                ParametrizedNamedView<?> v = (ParametrizedNamedView<?>) view;
                if (v.validate(navigator.getParameters())) {
                    RequiresAppPermission annotation = applicationContext.findAnnotationOnBean(beanName, RequiresAppPermission.class);
                    result = annotation == null || SecurityUtils.hasPermission(v.getApp(), annotation.value());
                }
            } else {
                result = true;
            }
            if (result) {
                path.goTo(((NamedView) view).getTitle(), navigator.getNavState(), this::navigateTo);
            }
        }
        return result;
    }
}
