/*
 * (C) Copyright 2018 Lukas Morawietz (https://github.com/F43nd1r)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.faendir.acra.ui.base;

import com.faendir.acra.ui.component.FlexLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasElement;
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
