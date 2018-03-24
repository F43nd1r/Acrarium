package com.faendir.acra.util;

import com.vaadin.spring.annotation.UIScope;
import com.vaadin.spring.navigator.SpringNavigator;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

/**
 * @author Lukas
 * @since 21.06.2017
 */
@Component
@UIScope
public class MyNavigator extends SpringNavigator {
    @NonNull private String navState;
    @Nullable private String parameters;

    public MyNavigator() {
        navState = "";
    }

    @Override
    public void navigateTo(@Nullable String navigationState) {
        navState = navigationState != null ? navigationState : "";
        int index;
        parameters = (index = navState.indexOf('/')) != -1 ? navState.substring(index + 1) : null;
        super.navigateTo(navState);
    }

    @Nullable
    public String getParameters() {
        return parameters;
    }

    @NonNull
    public String getNavState() {
        return navState;
    }
}
