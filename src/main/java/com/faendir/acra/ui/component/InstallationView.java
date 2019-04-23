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
import com.vaadin.flow.component.html.Image;
import org.springframework.lang.NonNull;

/**
 * @author lukas
 * @since 23.04.19
 */
public class InstallationView extends FlexLayout {
    private final Image image;
    private final Label label;
    private final AvatarService avatarService;

    public InstallationView(@NonNull AvatarService avatarService) {
        this.avatarService = avatarService;
        image = new Image();
        label = new Label();
        add(image, label);
        label.setPaddingLeft(5, Unit.PIXEL);
        setAlignItems(Alignment.CENTER);
    }

    public void setInstallationId(String installationId) {
        image.setSrc(avatarService.getAvatar(installationId));
        label.setText(installationId);
    }
}
