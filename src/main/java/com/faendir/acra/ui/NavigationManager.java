package com.faendir.acra.ui;

import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.ui.view.ErrorView;
import com.faendir.acra.ui.annotation.RequiresAppPermission;
import com.faendir.acra.ui.annotation.RequiresRole;
import com.faendir.acra.ui.view.base.NamedView;
import com.faendir.acra.ui.view.base.ParametrizedNamedView;
import com.faendir.acra.util.MyNavigator;
import com.vaadin.navigator.View;
import com.vaadin.spring.access.ViewAccessControl;
import com.vaadin.spring.access.ViewInstanceAccessControl;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Lukas
 * @since 14.05.2017
 */
@UIScope
@Component
@Configurable
public class NavigationManager implements ViewAccessControl, ViewInstanceAccessControl, Serializable {
    @NonNull private final MyNavigator navigator;
    @NonNull private final ApplicationContext applicationContext;
    @NonNull private final List<String> backStack;

    @Autowired
    public NavigationManager(@NonNull UI ui, @NonNull VerticalLayout mainView, @NonNull MyNavigator navigator, @NonNull ApplicationContext applicationContext) {
        this.navigator = navigator;
        this.applicationContext = applicationContext;
        this.navigator.init(ui, mainView);
        this.backStack = new ArrayList<>();
        navigator.setErrorView(ErrorView.class);
        String target = Optional.ofNullable(ui.getPage().getLocation().getFragment()).orElse("").replace("!", "");
        backStack.add(target);
        ui.access(() -> navigateTo(target));
    }

    public void navigateTo(@NonNull Class<? extends NamedView> namedView, @Nullable String parameters) {
        String target = namedView.getAnnotation(SpringView.class).name() + (parameters == null ? "" : "/" + parameters);
        if (!backStack.get(0).equals(target)) {
            backStack.add(0, target);
            navigateTo(target);
        }
    }

    private void navigateTo(@NonNull String fragment) {
        navigator.navigateTo(fragment);
    }

    public void navigateBack() {
        if (backStack.size() < 2) {
            backStack.set(0, "");
            navigateTo("");
        } else {
            backStack.remove(0);
            navigateTo(backStack.get(0));
        }
    }

    public void updatePageParameters(@Nullable String parameters) {
        String target = navigator.getCurrentView().getClass().getAnnotation(SpringView.class).name() + (parameters == null ? "" : "/" + parameters);
        backStack.set(0, target);
        navigator.getUI().getPage().setUriFragment(target, false);
    }

    @Override
    public boolean isAccessGranted(UI ui, @NonNull String beanName) {
        RequiresRole annotation = applicationContext.findAnnotationOnBean(beanName, RequiresRole.class);
        return annotation == null || SecurityUtils.hasRole(annotation.value());
    }

    @Override
    public boolean isAccessGranted(UI ui, @NonNull String beanName, @NonNull View view) {
        if (!(view instanceof ParametrizedNamedView)) {
            return true;
        }
        ParametrizedNamedView<?> v = (ParametrizedNamedView<?>) view;
        if (!v.validate(navigator.getParameters())) {
            return false;
        }
        RequiresAppPermission annotation = applicationContext.findAnnotationOnBean(beanName, RequiresAppPermission.class);
        return annotation == null || SecurityUtils.hasPermission(v.getApp(), annotation.value());
    }
}
