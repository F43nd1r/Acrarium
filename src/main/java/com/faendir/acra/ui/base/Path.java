package com.faendir.acra.ui.base;

import com.faendir.acra.ui.component.FlexLayout;
import com.faendir.acra.ui.component.HasSize;
import com.faendir.acra.ui.component.Label;
import com.faendir.acra.ui.component.Translatable;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
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
        getContent().setAlignItems(FlexComponent.Alignment.CENTER);
        getContent().setFlexWrap(FlexLayout.FlexWrap.WRAP);
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        getContent().removeAll();
        List<Element<?>> elements = event.getActiveChain().stream().filter(HasRoute.class::isInstance).map(HasRoute.class::cast).flatMap(e -> e.getPathElements(applicationContext).stream()).collect(Collectors.toList());
        if (!elements.isEmpty()) {
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
        final String titleId;
        final Object[] params;

        public Element(Class<T> target, String titleId, Object... params) {
            this.target = target;
            this.titleId = titleId;
            this.params = params;
        }

        protected RouterLink createRouterLink() {
            return new RouterLink("", target);
        }

        protected Component createContent() {
            Translatable<Label> p = Translatable.createLabel(titleId, params);
            p.setMargin(0, HasSize.Unit.PIXEL);
            p.getStyle().set("padding-top", "2px");
            return p;
        }

        public Component toComponent() {
            RouterLink routerLink = createRouterLink();
            Component component = createContent();
            routerLink.add(component);
            routerLink.getStyle().set("height", "32px");
            routerLink.getStyle().set("padding", "1rem");
            routerLink.getStyle().set("font-size", "130%");
            routerLink.getStyle().set("text-decoration","none");
            routerLink.getStyle().set("color","inherit");
            return routerLink;
        }

    }

    public static class ParametrizedTextElement<T extends Component & HasUrlParameter<P>, P> extends Element<T> {
        final P parameter;

        public ParametrizedTextElement(Class<T> target, P parameter, String titleId, Object... params) {
            super(target, titleId, params);
            this.parameter = parameter;
        }

        @Override
        protected RouterLink createRouterLink() {
            return new RouterLink("", target, parameter);
        }
    }

    public static class ImageElement<T extends Component> extends Element<T> {
        private final String src;

        public ImageElement(Class<T> target, String src, String titleId, Object... params) {
            super(target, titleId, params);
            this.src = src;
        }

        @Override
        protected Component createContent() {
            Translatable<Image> image = Translatable.createImage(src, titleId, params);
            image.setMaxHeightFull();
            return image;
        }
    }

}
