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
package com.faendir.acra.ui.view.base.layout;

import com.faendir.acra.ui.navigation.NavigationManager;
import com.vaadin.server.Sizeable;
import com.vaadin.ui.Component;
import com.vaadin.ui.Panel;
import com.vaadin.ui.themes.AcraTheme;
import org.springframework.lang.NonNull;

import java.util.List;

/**
 * @author lukas
 * @since 17.06.18
 */
public abstract class PanelFlexTab<T> implements ComponentFactory<T> {
    @NonNull private final List<? extends PanelContentFactory<T>> factories;

    public PanelFlexTab(@NonNull List<? extends PanelContentFactory<T>> factories) {
        this.factories = factories;
    }

    @Override
    public Component createContent(@NonNull T t, @NonNull NavigationManager navigationManager) {
        FlexLayout layout = new FlexLayout(factories.stream().map(factory -> {
            Panel panel = new Panel(factory.createContent(t, navigationManager));
            panel.setCaption(factory.getCaption());
            panel.setIcon(factory.getIcon());
            panel.addStyleName(AcraTheme.NO_BACKGROUND);
            if (factory.getStyleName() != null) {
                panel.addStyleName(factory.getStyleName());
            }
            return panel;
        }).toArray(Component[]::new));
        layout.setWidth(100, Sizeable.Unit.PERCENTAGE);
        Panel root = new Panel(layout);
        root.setSizeFull();
        root.addStyleNames(AcraTheme.NO_BACKGROUND, AcraTheme.NO_BORDER);
        return root;
    }
}
