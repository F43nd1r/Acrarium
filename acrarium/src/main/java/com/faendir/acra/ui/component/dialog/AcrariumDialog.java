package com.faendir.acra.ui.component.dialog;

import com.faendir.acra.ui.component.Translatable;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.dom.Element;

import java.util.Optional;

public class AcrariumDialog extends Dialog {
    private final DialogContent content;

    public AcrariumDialog() {
        this.content = new DialogContent();
        super.add(content);
    }

    public void setHeader(String captionId, Object... params) {
        content.setHeader(Translatable.createH3(captionId, params));
    }

    public void setPositive(ComponentEventListener<ClickEvent<Button>> clickListener, String captionId, Object... params) {
        Translatable<Button> button = Translatable.createButton(event -> {
            close();
            clickListener.onComponentEvent(event);
        }, captionId, params);
        content.setPositive(button);
    }

    public Optional<Translatable<Button>> getPositive() {
        //noinspection unchecked
        return content.getElement().getChildren().filter(e -> "positive".equals(e.getAttribute("slot"))).findAny().flatMap(Element::getComponent).map(c -> (Translatable<Button>) c);
    }

    public void setNegative(String captionId, Object... params) {
        setNegative(e -> {
        }, captionId, params);
    }

    public void setNegative(ComponentEventListener<ClickEvent<Button>> clickListener, String captionId, Object... params) {
        Translatable<Button> button = Translatable.createButton(event -> {
            close();
            clickListener.onComponentEvent(event);
        }, captionId, params).with(b -> {
            b.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
            b.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        });
        content.setNegative(button);
    }

    @Override
    public void add(Component... components) {
        content.add(components);
    }

    @Override
    public void add(String text) {
        content.add(text);
    }

    @Override
    public void remove(Component... components) {
        content.remove(components);
    }

    @Override
    public void removeAll() {
        content.removeAll();
    }

    @Override
    public void addComponentAtIndex(int index, Component component) {
        content.addComponentAtIndex(index, component);
    }

    @Override
    public void addComponentAsFirst(Component component) {
        content.addComponentAsFirst(component);
    }
}
