package com.faendir.acra.ui.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author lukas
 * @since 15.11.18
 */
@Tag("simple-dropdown")
@HtmlImport("bower_components/simple-dropdown/simple-dropdown.html")
public class DropdownMenu extends Component implements HasComponents, HasSize, HasStyle {
    public DropdownMenu() {
    }

    public DropdownMenu(Component... components) {
        this();
        add(components);
    }

    public enum Origin {
        LEFT, TOP, RIGHT, BOTTOM, CENTER;
    }

    public void setOpen(boolean open) {
        getElement().setProperty("active", open);
    }

    public boolean isOpen() {
        return getElement().getProperty("active", false);
    }

    public void setOrigin(Origin... origin) {
        getElement().setProperty("origin", Stream.of(origin).map(Origin::name).map(String::toLowerCase).collect(Collectors.joining(" ")));
    }

    public Origin[] getOrigin() {
        return Stream.of(getElement().getProperty("origin").split(" ")).map(String::toUpperCase).map(Origin::valueOf).toArray(Origin[]::new);
    }

    public void setLabel(String label) {
        getElement().setProperty("label", label);
    }

    public String getLabel(){
        return getElement().getProperty("label");
    }
}
