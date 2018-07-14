package com.faendir.acra.ui.base;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.router.RouterLayout;

/**
 * @author lukas
 * @since 13.07.18
 */
public class ParentLayout extends FlexLayout implements RouterLayout {
    private Component content;
    private HasElement routerRoot;

    public ParentLayout(HasElement routerRoot) {
        this();
        this.routerRoot = routerRoot;
    }

    public ParentLayout() {
        this.routerRoot = this;
        setSizeFull();
    }

    private void setContent(HasElement content, HasElement root) {
        if (root == this) {
            this.content = content instanceof Component ? (Component) content : null;
        }
        root.getElement().removeAllChildren();
        root.getElement().appendChild(content.getElement());
    }

    public HasElement getContent() {
        return content;
    }

    public void setContent(HasElement content) {
        setContent(content, this);
    }

    public void setRouterRoot(HasElement routerRoot) {
        this.routerRoot = routerRoot;
    }

    @Override
    public void showRouterLayoutContent(HasElement content) {
        setContent(content, routerRoot);
    }
}
