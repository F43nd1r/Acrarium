package com.faendir.acra.ui.view.base;

import com.vaadin.ui.CheckBox;

/**
 * @author Lukas
 * @since 31.05.2017
 */
public class MyCheckBox extends CheckBox {
    public MyCheckBox(boolean value, ValueChangeListener<Boolean> changeListener) {
        this(value, true, changeListener);
    }

    public MyCheckBox(boolean value, boolean enabled, ValueChangeListener<Boolean> changeListener) {
        setValue(value);
        setEnabled(enabled);
        addValueChangeListener(changeListener);
    }
}
