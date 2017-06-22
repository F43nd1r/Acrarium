package com.faendir.acra.ui.view.base;

import com.vaadin.ui.CheckBox;
import org.jetbrains.annotations.NotNull;

/**
 * @author Lukas
 * @since 31.05.2017
 */
public class MyCheckBox extends CheckBox {

    public MyCheckBox(boolean value, boolean enabled, @NotNull ValueChangeListener<Boolean> changeListener) {
        setValue(value);
        setEnabled(enabled);
        addValueChangeListener(changeListener);
    }
}
