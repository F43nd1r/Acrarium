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

package com.faendir.acra.ui.component;

import com.faendir.acra.service.AvatarService;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.server.StreamRegistration;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.StreamResourceRegistry;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.templatemodel.TemplateModel;
import org.springframework.lang.NonNull;

/**
 * @author lukas
 * @since 23.04.19
 */
@Tag("acrarium-image-with-label")
@JsModule("./elements/image-with-label.js")
public class InstallationView extends PolymerTemplate<InstallationView.InstallationModel> {
    private final AvatarService avatarService;
    private StreamResource resource;
    private StreamRegistration registration;

    public InstallationView(@NonNull AvatarService avatarService) {
        this.avatarService = avatarService;
    }

    public void setInstallationId(String installationId) {
        resource = avatarService.getAvatar(installationId);
        getModel().setImage(StreamResourceRegistry.getURI(resource).toASCIIString());
        getModel().setLabel(installationId);
        if(getParent().isPresent()) {
            register();
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        if(resource != null) {
            register();
        }
        super.onAttach(attachEvent);
    }

    private void register() {
        registration = VaadinSession.getCurrent().getResourceRegistry().registerResource(resource);
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        super.onDetach(detachEvent);
        if(registration != null) {
            registration.unregister();
        }
    }

    public interface InstallationModel extends TemplateModel {
        void setImage(String image);
        String getImage();

        void setLabel(String label);
        String getLabel();
    }
}
