package com.faendir.acra.ui.view.base;

import com.faendir.acra.util.Style;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Composite;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Consumer;

/**
 * @author Lukas
 * @since 24.03.2018
 */
public class Path extends Composite {
    private final Deque<Element> elements;
    private final HorizontalLayout layout;

    public Path() {
        elements = new ArrayDeque<>();
        layout = new HorizontalLayout();
        layout.setSpacing(false);
        setCompositionRoot(layout);
    }

    public void goTo(String label, String id, Consumer<String> action) {
        goTo(new Element(label, id, action));
    }

    public void goTo(Element element) {
        if (elements.stream().map(Element::getId).anyMatch(element.getId()::equals)) {
            while (!getLast().getId().equals(element.getId())) {
                goUp();
            }
        } else {
            if (!elements.isEmpty()) {
                Label icon = new Label(VaadinIcons.CARET_RIGHT.getHtml(), ContentMode.HTML);
                layout.addComponent(icon);
                layout.setComponentAlignment(icon, Alignment.MIDDLE_CENTER);
            }
            Button button = new Button(element.getLabel());
            button.addClickListener(e -> element.getAction().accept(element.getId()));
            Style.BUTTON_BORDERLESS.apply(button);
            layout.addComponent(button);
            elements.addLast(element);
        }
    }

    public int getSize() {
        return elements.size();
    }

    public boolean isEmpty() {
        return elements.isEmpty();
    }

    public Element getLast() {
        return elements.getLast();
    }

    public Element goUp() {
        if (elements.isEmpty()) {
            return null;
        }
        removeLastComponent();
        if (elements.size() != 1) removeLastComponent();
        return elements.removeLast();
    }

    public void clear() {
        layout.removeAllComponents();
        elements.clear();
    }

    private void removeLastComponent() {
        layout.removeComponent(layout.getComponent(layout.getComponentCount() - 1));
    }

    public static class Element {
        private final String label;
        private final String id;
        private final Consumer<String> action;

        public Element(String label, String id, Consumer<String> action) {
            this.label = label;
            this.id = id;
            this.action = action;
        }

        public String getLabel() {
            return label;
        }

        public String getId() {
            return id;
        }

        public Consumer<String> getAction() {
            return action;
        }
    }
}
