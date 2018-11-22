package com.faendir.acra.ui.base;

/**
 * @author lukas
 * @since 11.10.18
 */
public interface ActiveChildAware<C, P> {
    void setActiveChild(C child, P parameter);
}
