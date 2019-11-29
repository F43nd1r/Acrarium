package com.faendir.acra.ui.component;

import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.templatemodel.TemplateModel;

/**
 * @author lukas
 * @since 27.11.19
 */
@Tag("acrarium-box")
@JsModule("./elements/box.js")
public class Box extends PolymerTemplate<Box.BoxModel> implements HasSize, HasStyle {
    public Box(HasElement title, HasElement details, HasElement action) {
        title.getElement().setAttribute("slot", "title");
        details.getElement().setAttribute("slot", "details");
        action.getElement().setAttribute("slot", "action");
        getElement().appendChild(title.getElement(), details.getElement(), action.getElement());
    }

    public interface BoxModel extends TemplateModel {
    }
}
