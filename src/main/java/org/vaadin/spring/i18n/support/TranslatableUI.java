package org.vaadin.spring.i18n.support;

import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;

import java.util.Locale;

/**
 * Base class intended to make it easier to write UIs that needs to support changing the locale on the fly.
 * You are not required to extend this class to be able to use {@link Translatable} components. You can
 * easily plug in the {@link TranslatableSupport} into your existing UIs.
 * 
 * @author Petter Holmström (petter@vaadin.com)
 */
public abstract class TranslatableUI extends UI {

    private final TranslatableSupport translatableSupport = new TranslatableSupport(this);

    /**
     * {@inheritDoc}
     * <p>
     * This method will also update the message strings of all {@link Translatable} components currently attached
     * to the UI.
     * 
     * @see #updateMessageStrings()
     */
    @Override
    public void setLocale(Locale locale) {
        super.setLocale(locale);
        updateMessageStrings();
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation will delegate the UI initialization to {@link #initUI(VaadinRequest)}, then update
     * the message strings of all {@link Translatable} components.
     * 
     * @see #updateMessageStrings()
     */
    @Override
    protected void init(VaadinRequest request) {
        initUI(request);
        updateMessageStrings();
    }

    /**
     * Called by {@link #init(VaadinRequest)} to actually initialize the UI.
     * 
     * @param request
     */
    protected abstract void initUI(VaadinRequest request);

    /**
     * Returns the {@link TranslatableSupport} delegate owned by this UI.
     */
    protected TranslatableSupport getTranslatableSupport() {
        return translatableSupport;
    }

    /**
     * Updates the message strings of all {@link Translatable} components attached to this UI.
     */
    protected void updateMessageStrings() {
        getTranslatableSupport().updateMessageStrings(getLocale());
    }
}
