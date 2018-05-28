package com.faendir.acra.ui.view.base;

import com.faendir.acra.util.Style;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;

/**
 * @author lukas
 * @since 28.05.18
 */
public class FlexLayout extends CssLayout {
    public FlexLayout(Component... children) {
        super(children);
        setResponsive(true);
        Style.FLEX_LAYOUT.apply(this);
    }

    @Override
    public void addComponent(Component c) {
        super.addComponent(c);
        //c.setSizeUndefined();
        Style.FLEX_ITEM.apply(c);
    }
}
