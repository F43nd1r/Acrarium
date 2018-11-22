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

package com.faendir.acra.ui.view.app.tabs;

import com.faendir.acra.i18n.Messages;
import com.faendir.acra.model.App;
import com.faendir.acra.service.DataService;
import com.faendir.acra.ui.base.ActiveChildAware;
import com.faendir.acra.ui.base.HasRoute;
import com.faendir.acra.ui.base.HasSecureIntParameter;
import com.faendir.acra.ui.base.Path;
import com.faendir.acra.ui.view.Overview;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.router.BeforeEvent;
import org.springframework.lang.NonNull;

import java.util.Optional;

/**
 * @author lukas
 * @since 14.07.18
 */
public abstract class AppTab<T extends Component> extends Composite<T> implements HasSecureIntParameter, HasRoute {
    private final DataService dataService;
    private App app;

    public AppTab(DataService dataService) {
        this.dataService = dataService;
    }

    public DataService getDataService() {
        return dataService;
    }

    @Override
    public void setParameterSecure(BeforeEvent event, Integer parameter) {
        Optional<App> app = dataService.findApp(parameter);
        if (app.isPresent()) {
            this.app = app.get();
        } else {
            event.rerouteToError(IllegalArgumentException.class);
        }
    }

    abstract void init(App app);

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        super.onAttach(attachEvent);
        init(this.app);
        Optional<Component> parent = getParent();
        while (parent.isPresent()) {
            if (parent.get() instanceof ActiveChildAware) {
                //noinspection unchecked
                ((ActiveChildAware<AppTab<?>, App>) parent.get()).setActiveChild(this, app);
                break;
            }
            parent = parent.get().getParent();
        }
    }

    @Override
    @NonNull
    public Path.Element<?> getPathElement() {
        //noinspection unchecked
        return new Path.ParametrizedTextElement<>((Class<? extends AppTab<?>>) getClass(), app.getId(), Messages.ONE_ARG, app.getName());
    }

    @Override
    public Parent<?> getLogicalParent() {
        return new Parent<>(Overview.class);
    }
}
