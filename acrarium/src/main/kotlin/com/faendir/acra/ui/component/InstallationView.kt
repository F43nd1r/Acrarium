/*
 * (C) Copyright 2018-2026 Lukas Morawietz (https://github.com/F43nd1r)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.faendir.acra.ui.component

import com.faendir.acra.domain.AvatarService
import com.vaadin.flow.component.html.Image
import com.vaadin.flow.component.html.NativeLabel
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.FlexLayout

class InstallationView(private val avatarService: AvatarService) : FlexLayout() {
    private val image = Image(ByteArray(0), "")
    private val label = NativeLabel()

    init {
        add(image, label)
        flexDirection = FlexDirection.ROW
        alignItems = FlexComponent.Alignment.CENTER
        image.width = "32px"
        image.height = "32px"
        label.style["padding-left"] = "var(--lumo-space-s)"
    }

    fun setInstallationId(installationId: String) {
        image.setSrc(avatarService.getAvatarResource(installationId))
        image.setAlt(installationId)
        label.text = installationId
    }
}