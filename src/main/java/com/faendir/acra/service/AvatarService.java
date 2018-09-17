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
package com.faendir.acra.service;

import com.faendir.acra.model.Report;
import com.talanlabs.avatargenerator.Avatar;
import com.talanlabs.avatargenerator.IdenticonAvatar;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.server.StreamResource;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;

/**
 * @author lukas
 * @since 29.07.18
 */
@Service
@EnableCaching
public class AvatarService {
    private final Avatar avatar;

    public AvatarService() {
        avatar = IdenticonAvatar.newAvatarBuilder().size(32, 32).build();
    }

    //@Cacheable(cacheNames = "avatars", key = "#report.installationId")
    public Component getAvatar(@NonNull Report report) {
        byte[] bytes = avatar.createAsPngBytes(report.getInstallationId().hashCode());
        return new Image(new StreamResource("", () -> new ByteArrayInputStream(bytes)), "");
    }
}
