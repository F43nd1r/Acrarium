package com.faendir.acra.ui;

import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.ui.view.ErrorView;
import com.faendir.acra.ui.view.annotation.RequiresRole;
import com.faendir.acra.ui.view.base.NamedView;
import com.vaadin.spring.access.ViewAccessControl;
import com.vaadin.spring.annotation.SpringView;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.spring.navigator.SpringNavigator;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Lukas
 * @since 14.05.2017
 */
@UIScope
@Component
public class NavigationManager implements ViewAccessControl {
    private final SpringNavigator navigator;
    private final ApplicationContext applicationContext;
    private final List<String> backStack;

    @Autowired
    public NavigationManager(UI ui, VerticalLayout mainView, SpringNavigator springNavigator, ApplicationContext applicationContext) {
        navigator = springNavigator;
        this.applicationContext = applicationContext;
        navigator.init(ui, mainView);
        backStack = new ArrayList<>();
        navigator.setErrorView(ErrorView.class);
        String target = Optional.ofNullable(ui.getPage().getLocation().getFragment()).orElse("").replace("!", "");
        backStack.add(target);
        ui.access(() -> navigator.navigateTo(target));
    }

    public void navigateTo(Class<? extends NamedView> namedView, String contentId) {
        String target = namedView.getAnnotation(SpringView.class).name() + (contentId == null ? "" : "/" + contentId);
        if (!backStack.get(0).equals(target)) {
            backStack.add(0, target);
            navigator.navigateTo(target);
        }
    }

    public void navigateBack() {
        if (backStack.size() < 2) {
            backStack.set(0, "");
            navigator.navigateTo("");
        } else {
            backStack.remove(0);
            navigator.navigateTo(backStack.get(0));
        }
    }

    @Override
    public boolean isAccessGranted(UI ui, String beanName) {
        RequiresRole requiresRole = applicationContext.findAnnotationOnBean(beanName, RequiresRole.class);
        return requiresRole == null || SecurityUtils.hasRole(requiresRole.value());
    }
}
