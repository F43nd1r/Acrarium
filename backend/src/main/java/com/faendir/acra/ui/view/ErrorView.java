package com.faendir.acra.ui.view;


import com.vaadin.navigator.View;
import com.vaadin.navigator.ViewChangeListener;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * @author Lukas
 * @since 21.05.2017
 */
public class ErrorView extends CustomComponent implements View {
    @Override
    public void enter(ViewChangeListener.ViewChangeEvent event) {
        Label label = new Label("This page does not exist or you do not have the permission to view it.");
        VerticalLayout layout = new VerticalLayout(label);
        layout.setComponentAlignment(label, Alignment.MIDDLE_CENTER);
        layout.setSizeFull();
        setSizeFull();
        setCompositionRoot(layout);
    }
}
