package com.faendir.acra.ui.view.bug;

import com.faendir.acra.i18n.Messages;
import com.faendir.acra.ui.base.HasRoute;
import com.faendir.acra.ui.base.HasSecureIntParameter;
import com.faendir.acra.ui.base.Path;
import com.faendir.acra.ui.view.MainView;
import com.faendir.acra.ui.view.Overview;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.BeforeEvent;
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
public class BugView extends Div implements HasSecureIntParameter, HasRoute {
    private final Text content;
    private int bugId;

    public BugView() {
        content = new Text("");
        add(content);
    }

    @Override
    public void setParameterSecure(BeforeEvent event, Integer parameter) {
        try {
            bugId = parameter;
            content.setText("This is bug " + bugId);
        } catch (NumberFormatException ignored) {
        }
    }

    @Override
    public Path.Element<?> getPathElement() {
        return new Path.ParametrizedTextElement<>(getClass(), bugId, Messages.ONE_ARG, bugId);
    }

    @Override
    public Class<? extends HasRoute> getLogicalParent() {
        return Overview.class; //TODO: change
    }
}
