package com.faendir.acra.ui.view.bug;

import com.faendir.acra.i18n.Messages;
import com.faendir.acra.model.Bug;
import com.faendir.acra.service.DataService;
import com.faendir.acra.ui.base.HasRoute;
import com.faendir.acra.ui.base.HasSecureIntParameter;
import com.faendir.acra.ui.base.Path;
import com.faendir.acra.ui.view.MainView;
import com.faendir.acra.ui.view.app.tabs.BugTab;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.annotation.SpringComponent;
import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.lang.NonNull;

import java.util.Optional;

/**
 * @author lukas
 * @since 08.09.18
 */
@UIScope
@SpringComponent
@Route(value = "bug", layout = MainView.class)
public class BugView extends Div implements HasSecureIntParameter, HasRoute {
    private final Text content;
    private final DataService dataService;
    private Bug bug;

    public BugView(@NonNull DataService dataService) {
        this.dataService = dataService;
        content = new Text("");
        add(content);
    }

    @Override
    public void setParameterSecure(BeforeEvent event, Integer parameter) {
        Optional<Bug> b = dataService.findBug(parameter);
        if (b.isPresent()) {
            bug = b.get();
        } else {
            event.rerouteToError(IllegalArgumentException.class);
        }
    }

    @NonNull
    @Override
    public Path.Element<?> getPathElement() {
        return new Path.ParametrizedTextElement<>(getClass(), bug.getId(), Messages.ONE_ARG, bug.getTitle());
    }

    @Override
    public Parent<?> getLogicalParent() {
        return new ParametrizedParent<>(BugTab.class, bug.getApp().getId());
    }
}
