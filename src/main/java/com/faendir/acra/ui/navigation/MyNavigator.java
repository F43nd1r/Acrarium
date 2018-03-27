package com.faendir.acra.ui.navigation;

import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.navigator.ViewProvider;
import com.vaadin.shared.util.SharedUtil;
import com.vaadin.spring.annotation.UIScope;
import com.vaadin.spring.navigator.SpringNavigator;
import com.vaadin.spring.navigator.ViewActivationListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
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
    @NonNull private final List<SingleViewProvider<?>> providers;
    @NonNull private final List<ActivationListenerWrapper> activationListenerWrappers;
    @NonNull private String navState;
    @Nullable private ViewProvider errorProvider;

    @Autowired
    public MyNavigator(@NonNull List<SingleViewProvider<?>> providers) {
        this.providers = providers;
        this.activationListenerWrappers = new ArrayList<>();
        navState = "";
        providers.forEach(this::addProvider);
    }

    public Optional<SingleViewProvider<?>> getViewProvider(@NonNull Class<? extends View> clazz) {
        return providers.stream().filter(p -> p.getClazz().equals(clazz)).findAny();
    }

    @Override
    public void navigateTo(@Nullable String navigationState) {
        if (navigationState == null) {
            return;
        }
        String oldNavState = navState;
        navState = navigationState;
        View view = null;
        String viewName = "";
        HierarchyElement findResult = findViewProvider(navigationState);
        if (findResult != null) {
            viewName = findResult.getProvider().getViewName(findResult.getNavState());
            view = findResult.getProvider().getView(viewName);
        }
        if (view == null) {
            if (errorProvider != null) {
                view = errorProvider.getView(navigationState);
            }
            if (view == null) {
                throw new IllegalArgumentException("Trying to navigate to an unknown state '" + navigationState + "' and an error view provider not present");
            }
        }
        String parameters = "";
        if (navState.length() > navState.indexOf(viewName) + viewName.length() + 1) {
            parameters = navState.substring(navState.indexOf(viewName) + viewName.length() + 1);
        } else if (navState.endsWith("/")) {
            navState = navState.substring(0, navState.length() - 1);
        }
        if (getCurrentView() == null || !SharedUtil.equals(getCurrentView(), view) || !SharedUtil.equals(oldNavState, navState)) {
            navigateTo(view, navState, parameters);
        } else {
            updateNavigationState(new ViewChangeListener.ViewChangeEvent(this, getCurrentView(), view, navState, parameters));
        }
    }

    @Nullable
    private HierarchyElement findViewProvider(String navigationState) {
        List<String> fragments = Arrays.asList(navigationState.split(SEPARATOR));
        if(fragments.isEmpty()) return null;
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
        if (viewProvider != null) return new HierarchyElement(newNavState, viewProvider);
        return null;
    }

    @Override
    public void setErrorProvider(ViewProvider provider) {
        super.setErrorProvider(provider);
        errorProvider = provider;
    }

    @Override
    public void addViewActivationListener(ViewActivationListener listener) {
        ActivationListenerWrapper wrapper = new ActivationListenerWrapper(listener);
        activationListenerWrappers.add(wrapper);
        super.addViewActivationListener(wrapper);
    }

    @Override
    public void removeViewActivationListener(ViewActivationListener listener) {
        activationListenerWrappers.stream().filter(w -> w.getListener().equals(listener)).findAny().ifPresent(super::removeViewActivationListener);
    }

    public Deque<HierarchyElement> getHierarchy() {
        Deque<HierarchyElement> hierarchy = new ArrayDeque<>();
        String navPart = navState;
        while (!navPart.isEmpty()) {
            HierarchyElement element = findViewProvider(navPart);
            if (element == null) break;
            hierarchy.addFirst(element);
            if (navPart.length() == element.getNavState().length()) break;
            navPart = navPart.substring(0, navPart.length() - element.getNavState().length() - 1);
        }
        HierarchyElement top = findViewProvider("");
        if (top != null) hierarchy.addFirst(top);
        return hierarchy;
    }

    public static class HierarchyElement {
        @NonNull private final String navState;
        @NonNull private final ViewProvider provider;

        private HierarchyElement(@NonNull String navState, @NonNull ViewProvider provider) {
            this.navState = navState;
            this.provider = provider;
        }

        @NonNull
        public String getNavState() {
            return navState;
        }

        @NonNull
        public ViewProvider getProvider() {
            return provider;
        }
    }

    private class ActivationListenerWrapper implements ViewActivationListener {
        private final ViewActivationListener listener;

        private ActivationListenerWrapper(ViewActivationListener listener) {
            this.listener = listener;
        }

        @Override
        public void viewActivated(ViewActivationEvent event) {
            HierarchyElement hierarchyElement = findViewProvider(event.getViewName());
            if (hierarchyElement != null) {
                listener.viewActivated(new ViewActivationEvent(MyNavigator.this, event.isActivated(), hierarchyElement.getNavState()));
            }
        }

        public ViewActivationListener getListener() {
            return listener;
        }
    }
}
