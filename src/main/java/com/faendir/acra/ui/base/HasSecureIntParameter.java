package com.faendir.acra.ui.base;

import com.faendir.acra.security.SecurityUtils;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;

/**
 * @author lukas
 * @since 19.11.18
 */
public
interface HasSecureIntParameter extends HasUrlParameter<Integer> {
    @Override
    default void setParameter(BeforeEvent event, Integer parameter) {
        if (SecurityUtils.isLoggedIn()) {
            setParameterSecure(event, parameter);
        }
    }

    void setParameterSecure(BeforeEvent event, Integer parameter);
}
