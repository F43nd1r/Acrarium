package com.faendir.acra.ui.view.bug.tabs;

import com.faendir.acra.i18n.Messages;
import com.faendir.acra.model.Bug;
import com.faendir.acra.service.DataService;
import com.faendir.acra.ui.base.ActiveChildAware;
import com.faendir.acra.ui.base.HasRoute;
import com.faendir.acra.ui.base.HasSecureIntParameter;
import com.faendir.acra.ui.base.Path;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.router.BeforeEvent;
import org.springframework.lang.NonNull;

import java.util.Optional;

/**
 * @author lukas
 * @since 19.11.18
 */
public abstract class BugTab <T extends Component> extends Composite<T> implements HasSecureIntParameter, HasRoute {
    private final DataService dataService;
    private Bug bug;

    public BugTab(DataService dataService) {
        this.dataService = dataService;
    }

    public DataService getDataService() {
        return dataService;
    }

    @Override
    public void setParameterSecure(BeforeEvent event, Integer parameter) {
        Optional<Bug> bug = dataService.findBug(parameter);
        if (bug.isPresent()) {
            this.bug = bug.get();
        } else {
            event.rerouteToError(IllegalArgumentException.class);
        }
    }

    abstract void init(Bug bug);

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        init(this.bug);
        Optional<Component> parent = getParent();
        while (parent.isPresent()) {
            if (parent.get() instanceof ActiveChildAware) {
                //noinspection unchecked
                ((ActiveChildAware<BugTab<?>, Bug>) parent.get()).setActiveChild(this, bug);
                break;
            }
            parent = parent.get().getParent();
        }
    }

    @Override
    @NonNull
    public Path.Element<?> getPathElement() {
        //noinspection unchecked
        return new Path.ParametrizedTextElement<>((Class<? extends BugTab<?>>) getClass(), bug.getId(), Messages.ONE_ARG, bug.getTitle());
    }

    @Override
    public Parent<?> getLogicalParent() {
        return new ParametrizedParent<>(com.faendir.acra.ui.view.app.tabs.BugTab.class, bug.getApp().getId());
    }
}
