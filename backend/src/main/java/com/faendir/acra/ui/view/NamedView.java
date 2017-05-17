package com.faendir.acra.ui.view;

import com.faendir.acra.annotation.AutoDiscoverView;
import com.faendir.acra.ui.NavigationManager;
import com.vaadin.navigator.View;
import com.vaadin.ui.CustomComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * @author Lukas
 * @since 14.05.2017
 */
@AutoDiscoverView
@Component
public abstract class NamedView extends CustomComponent implements View {
    private NavigationManager navigationManager;

    public abstract String getName();

    NavigationManager getNavigationManager() {
        return navigationManager;
    }

    @Lazy
    @Autowired
    public void setNavigationManager(NavigationManager navigationManager) {
        this.navigationManager = navigationManager;
    }
}
