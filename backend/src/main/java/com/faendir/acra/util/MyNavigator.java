package com.faendir.acra.util;

import com.vaadin.spring.annotation.UIScope;
import com.vaadin.spring.navigator.SpringNavigator;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

/**
 * @author Lukas
 * @since 21.06.2017
 */
@Component
@UIScope
public class MyNavigator extends SpringNavigator {
    @Nullable private String parameters;

    @Override
    public void navigateTo(String navigationState) {
        String[] split = navigationState.split("/", 2);
        parameters = split.length == 2 ? split[1] : null;
        super.navigateTo(navigationState);
    }

    @Nullable
    public String getParameters() {
        return parameters;
    }
}
