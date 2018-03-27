package com.faendir.acra.ui.navigation;

import com.faendir.acra.security.SecurityUtils;
import com.faendir.acra.ui.annotation.RequiresRole;
import com.faendir.acra.ui.view.base.BaseView;
import com.vaadin.navigator.ViewProvider;
import com.vaadin.spring.internal.ViewCache;
import com.vaadin.spring.internal.ViewScopeImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.lang.NonNull;

/**
 * @author Lukas
 * @since 24.03.2018
 */
public abstract class SingleViewProvider<V extends BaseView> implements ViewProvider {
    private final Class<V> clazz;
    private ApplicationContext applicationContext;

    protected SingleViewProvider(Class<V> clazz) {
        this.clazz = clazz;
    }

    @Autowired
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public String getViewName(String viewAndParameters) {
        return getParameters(viewAndParameters) != null ? viewAndParameters : null;
    }

    public String getParameters(String viewAndParameters) {
        String result = null;
        String id = getId();
        if (viewAndParameters.startsWith(id)) {
            String params = viewAndParameters.substring(id.length());
            RequiresRole annotation = getClazz().getAnnotation(RequiresRole.class);
            if (annotation == null || SecurityUtils.hasRole(annotation.value())) {
                if (params.isEmpty()) {
                    result = "";
                } else if (params.charAt(0) == MyNavigator.SEPARATOR_CHAR && isValidParameter(params.substring(1))) {
                    result = params.substring(1);
                }
            }
        }
        return result;
    }

    public Class<V> getClazz() {
        return clazz;
    }

    protected boolean isValidParameter(String parameter) {
        return false;
    }

    @Override
    public V getView(String viewName) {
        V view = null;
        String id = getId();
        if (viewName.startsWith(id)) {
            String params = viewName.substring(id.length());
            if (params.isEmpty() || params.charAt(0) == MyNavigator.SEPARATOR_CHAR && isValidParameter(params.substring(1))) {
                RequiresRole annotation = getClazz().getAnnotation(RequiresRole.class);
                if (annotation == null || SecurityUtils.hasRole(annotation.value())) {
                    if (((BeanDefinitionRegistry) applicationContext.getAutowireCapableBeanFactory()).getBeanDefinition(applicationContext.getBeanNamesForType(getClazz())[0])
                            .getScope()
                            .equals(ViewScopeImpl.VAADIN_VIEW_SCOPE_NAME)) {
                        final ViewCache viewCache = ViewScopeImpl.getViewCacheRetrievalStrategy().getViewCache(applicationContext);
                        viewCache.creatingView(viewName);
                        try {
                            view = applicationContext.getBean(getClazz());
                        } finally {
                            viewCache.viewCreated(viewName, view);
                        }
                    } else {
                        view = applicationContext.getBean(getClazz());
                    }
                }
            }
        }
        return view;
    }

    public abstract String getTitle(String parameter);

    public abstract String getId();
}
