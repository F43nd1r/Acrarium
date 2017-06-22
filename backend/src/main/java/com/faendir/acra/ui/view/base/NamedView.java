package com.faendir.acra.ui.view.base;

import com.faendir.acra.ui.NavigationManager;
import com.vaadin.navigator.View;
import com.vaadin.ui.CustomComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * @author Lukas
 * @since 14.05.2017
 */
@Component
public abstract class NamedView extends CustomComponent implements View {
    private NavigationManager navigationManager;

    protected NavigationManager getNavigationManager() {
        return navigationManager;
    }

    @Lazy
    @Autowired
    public void setNavigationManager(NavigationManager navigationManager) {
        this.navigationManager = navigationManager;
    }

    @Nullable
    public String getApp(@NotNull String fragment) {
        return null;
    }

    public boolean validate(@Nullable String fragment) {
        return true;
    }
}
