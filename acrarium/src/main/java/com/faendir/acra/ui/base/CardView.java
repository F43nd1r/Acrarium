/*
 * (C) Copyright 2019 Lukas Morawietz (https://github.com/F43nd1r)
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

import com.faendir.acra.ui.component.Card;
import com.faendir.acra.ui.component.FlexLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.spring.annotation.SpringComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;

import java.util.stream.Stream;

@SpringComponent
@Scope(scopeName = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class CardView<C extends Card & Init<P>, P> extends FlexLayout implements Init<P> {
    private final ApplicationContext context;
    private P parameter;

    @Autowired
    public CardView(ApplicationContext context) {
        setWidthFull();
        setFlexWrap(FlexLayout.FlexWrap.WRAP);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        this.context = context;
    }

    @SafeVarargs
    public CardView(ApplicationContext context, Class<? extends C>... cards) {
        this(context);
        add(cards);
    }

    @SafeVarargs
    public final void add(Class<? extends C>... cards) {
        add(Stream.of(cards).map(context::getBean).peek(c -> {
            if (parameter != null) {
                c.init(parameter);
            }
        }).toArray(Component[]::new));
    }

    @Override
    public void init(P p) {
        parameter = p;
        getChildren().forEach(component -> {
            if (component instanceof Init) {
                //noinspection unchecked
                ((Init<P>) component).init(parameter);
            }
        });
    }
}
