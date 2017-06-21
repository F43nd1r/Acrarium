package com.faendir.acra.ui;

import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.ui.view.ErrorView;
import com.faendir.acra.ui.view.annotation.RequiresAppPermission;
import com.faendir.acra.ui.view.annotation.RequiresRole;
import com.faendir.acra.ui.view.base.NamedView;
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
    private final MyNavigator navigator;
    private final ApplicationContext applicationContext;
    private final List<String> backStack;

    @Autowired
    public NavigationManager(UI ui, VerticalLayout mainView, MyNavigator navigator, ApplicationContext applicationContext) {
        this.navigator = navigator;
        this.applicationContext = applicationContext;
        this.navigator.init(ui, mainView);
        backStack = new ArrayList<>();
        this.navigator.setErrorView(ErrorView.class);
        String target = Optional.ofNullable(ui.getPage().getLocation().getFragment()).orElse("").replace("!", "");
        backStack.add(target);
        ui.access(() -> navigateTo(target));
    }

    public void navigateTo(Class<? extends NamedView> namedView, String parameters) {
        String target = namedView.getAnnotation(SpringView.class).name() + (parameters == null ? "" : "/" + parameters);
        if (!backStack.get(0).equals(target)) {
            backStack.add(0, target);
            navigateTo(target);
        }
    }

    private void navigateTo(String fragment) {
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

    public void updatePageParameters(String parameters) {
        String target = navigator.getCurrentView().getClass().getAnnotation(SpringView.class).name() + (parameters == null ? "" : "/" + parameters);
        backStack.set(0, target);
        navigator.getUI().getPage().setUriFragment(target, false);
    }

    @Override
    public boolean isAccessGranted(UI ui, String beanName) {
        RequiresRole requiresRole = applicationContext.findAnnotationOnBean(beanName, RequiresRole.class);
        return requiresRole == null || SecurityUtils.hasRole(requiresRole.value());
    }

    @Override
    public boolean isAccessGranted(UI ui, String beanName, View view) {
        RequiresAppPermission requiresAppPermission = applicationContext.findAnnotationOnBean(beanName, RequiresAppPermission.class);
        String parameters = navigator.getParameters();
        if (requiresAppPermission != null && parameters != null) {
            String app = ((NamedView) view).getApp(parameters);
            if (app != null) {
                if (!SecurityUtils.hasPermission(app, requiresAppPermission.value())) {
                    return false;
                }
            }
        }
        return ((NamedView) view).validate(parameters);
    }
}
