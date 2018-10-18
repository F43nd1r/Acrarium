package com.faendir.acra.ui.base;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationListener;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.shared.Registration;
import org.springframework.context.ApplicationContext;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lukas
 * @since 18.10.18
 */
public class Path extends Composite<FlexLayout> implements AfterNavigationListener {
    private final ApplicationContext applicationContext;
    private Registration registration;

    public Path(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        getContent().removeAll();
        List<Element<?>> elements = event.getActiveChain().stream().filter(HasRoute.class::isInstance).map(HasRoute.class::cast).flatMap(e -> e.getPathElements(applicationContext).stream()).collect(Collectors.toList());
        if(!elements.isEmpty()) {
            Collections.reverse(elements);
            getContent().add(elements.remove(0).toComponent());
            for (Element<?> element : elements) {
                getContent().add(VaadinIcon.CARET_RIGHT.create(), element.toComponent());
            }
        }
    }


    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        registration = attachEvent.getUI().addAfterNavigationListener(this);
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        registration.remove();
        super.onDetach(detachEvent);
    }

    public static class Element<T extends Component> {
        final Class<T> target;
        final String title;

        public Element(Class<T> target, String title) {
            this.target = target;
            this.title = title;
        }

        public Component toComponent() {
            return new RouterLink(title, target);
        }
    }

    public static class ParametrizedElement<T extends Component & HasUrlParameter<P>, P> extends Element<T> {
        final P parameter;

        public ParametrizedElement(Class<T> target, String title, P parameter) {
            super(target, title);
            this.parameter = parameter;
        }

        public Component toComponent() {
            return new RouterLink(title, target, parameter);
        }
    }
}
