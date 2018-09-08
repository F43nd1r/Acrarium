package com.faendir.acra.ui.view.bug;

import com.faendir.acra.ui.view.MainView;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;

/**
 * @author lukas
 * @since 08.09.18
 */
@UIScope
@SpringComponent
@Route(value = "bug", layout = MainView.class)
public class BugView extends Div implements HasUrlParameter<Integer> {
    private final Text content;
    private int bugId;

    public BugView() {
        content = new Text("");
        add(content);
    }

    @Override
    public void setParameter(BeforeEvent event, Integer parameter) {
        try {
            bugId = parameter;
            content.setText("This is bug " + bugId);
        } catch (NumberFormatException ignored) {
        }
    }
}
