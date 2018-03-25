package com.faendir.acra.ui.view.base;

import com.faendir.acra.ui.NavigationManager;
import com.vaadin.navigator.View;
import com.vaadin.ui.Composite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

/**
 * Non-abstract subclasses must be annotated with {@link com.vaadin.spring.annotation.SpringView}
 *
 * @author Lukas
 * @since 14.05.2017
 */
public abstract class BaseView extends Composite implements View {
    private NavigationManager navigationManager;

    protected NavigationManager getNavigationManager() {
        return navigationManager;
    }

    @Lazy
    @Autowired
    public void setNavigationManager(NavigationManager navigationManager) {
        this.navigationManager = navigationManager;
    }
}
