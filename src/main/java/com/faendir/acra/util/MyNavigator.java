package com.faendir.acra.util;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.navigator.ViewProvider;
import com.vaadin.shared.util.SharedUtil;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.spring.navigator.SpringNavigator;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lukas
 * @since 21.06.2017
 */
@Component
@UIScope
public class MyNavigator extends SpringNavigator {
    public static final char SEPARATOR_CHAR = '/';
    public static final String SEPARATOR = "" + SEPARATOR_CHAR;
    @NonNull private String navState;
    @NonNull private String parameters;
    @Nullable private ViewProvider errorProvider;

    public MyNavigator() {
        navState = "";
        parameters = "";
    }

    @Override
    public void navigateTo(@Nullable String navigationState) {
        if (navigationState == null) {
            return;
        }
        String oldNavState = navState;
        navState = navigationState;
        List<String> fragments = Arrays.asList(navigationState.split(SEPARATOR));
        int size = fragments.size();
        int fragmentIndex;
        String newNavState;
        ViewProvider viewProvider;
        fragmentIndex = size;
        do {
            fragmentIndex--;
            newNavState = fragments.subList(fragmentIndex, size).stream().collect(Collectors.joining(SEPARATOR));
            viewProvider = getViewProvider(newNavState);
        } while (fragmentIndex > 0 && viewProvider == null);
        View view = null;
        if (viewProvider != null) {
            view = viewProvider.getView(viewProvider.getViewName(newNavState));
        }
        if (view == null) {
            if (errorProvider != null) {
                view = errorProvider.getView(newNavState);
            }
            if (view == null) {
                throw new IllegalArgumentException("Trying to navigate to an unknown state '" + navigationState + "' and an error view provider not present");
            }
        }
        int slashIndex = newNavState.indexOf(SEPARATOR_CHAR);
        if (slashIndex != -1) {
            parameters = newNavState.substring(slashIndex + 1);
        } else {
            parameters = "";
            if (navigationState.endsWith(SEPARATOR)) {
                navigationState = navigationState.substring(0, navigationState.length() - 1);
            }
        }
        if (getCurrentView() == null || !SharedUtil.equals(getCurrentView(), view) || !SharedUtil.equals(oldNavState, navigationState)) {
            navigateTo(view, navigationState, parameters);
        } else {
            updateNavigationState(new ViewChangeListener.ViewChangeEvent(this, getCurrentView(), view, navigationState, parameters));
        }
    }

    @NonNull
    public String getParameters() {
        return parameters;
    }

    @NonNull
    public String getNavState() {
        return navState;
    }

    @Override
    public void setErrorProvider(ViewProvider provider) {
        super.setErrorProvider(provider);
        errorProvider = provider;
    }
}
