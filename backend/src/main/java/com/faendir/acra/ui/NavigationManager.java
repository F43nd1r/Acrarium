package com.faendir.acra.ui;

import com.faendir.acra.gen.ViewDefinition;
import com.faendir.acra.ui.view.NamedView;
import com.vaadin.navigator.Navigator;
import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewProvider;
import com.vaadin.spring.annotation.UIScope;
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
public class NavigationManager {
    private final Navigator navigator;
    private final ApplicationContext applicationContext;
    private final List<Class<?>> views;
    private final List<String> backStack;

    @Autowired
    public NavigationManager(UI ui, VerticalLayout mainView, ApplicationContext applicationContext) {
        navigator = new Navigator(ui, mainView);
        backStack = new ArrayList<>();
        this.applicationContext = applicationContext;
        navigator.addProvider(new ViewProvider() {
            @Override
            public String getViewName(String viewAndParameters) {
                String name = viewAndParameters.split("/", 2)[0];
                if (views.stream().map(applicationContext::getBean).map(NamedView.class::cast).map(NamedView::getName).anyMatch(name::equals))
                    return name;
                return null;
            }

            @Override
            public View getView(String viewName) {
                return views.stream().map(applicationContext::getBean).map(NamedView.class::cast).filter(view -> view.getName().equals(viewName)).findAny().orElse(null);
            }
        });
        views = ViewDefinition.getViewClasses();
        String target = Optional.ofNullable(ui.getPage().getLocation().getFragment()).orElse("").replace("!", "");
        backStack.add(target);
        navigator.navigateTo(target);
    }

    public void navigateTo(Class<? extends NamedView> namedView, String contentId) {
        String target = applicationContext.getBean(namedView).getName() + (contentId == null ? "" : "/" + contentId);
        backStack.add(0, target);
        navigator.navigateTo(target);
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


}
