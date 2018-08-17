package org.vaadin.spring.i18n.support;

import com.vaadin.ui.Component;
import com.vaadin.ui.HasComponents;
import com.vaadin.ui.UI;

import java.util.Locale;

/**
 * Implementation of {@link Translatable} intended to be used as a delegate by an owning {@link UI}.
 * The {@link #updateMessageStrings(Locale)} method will traverse the entire component hierarchy of the UI and
 * update the message strings of any components that implement the {@link Translatable} interface.
 *
 * @author Petter Holmström (petter@vaadin.com)
 */
public class TranslatableSupport implements Translatable {

    private final UI ui;

    /**
     * Creates a new {@code TranslatableSupport}.
     * 
     * @param ui the UI that owns the object.
     */
    public TranslatableSupport(UI ui) {
        this.ui = ui;
    }

    @Override
    public void updateMessageStrings(Locale locale) {
        updateMessageStrings(locale, ui);
    }

    private void updateMessageStrings(Locale locale, Component component) {
        if (component instanceof Translatable) {
            ((Translatable) component).updateMessageStrings(locale);
        }
        if (component instanceof HasComponents) {
            for (Component child : (HasComponents) component) {
                updateMessageStrings(locale, child);
            }
        }
    }

}
