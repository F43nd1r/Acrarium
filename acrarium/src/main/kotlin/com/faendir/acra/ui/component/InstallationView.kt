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
package com.faendir.acra.ui.component

import com.faendir.acra.service.AvatarService
import com.faendir.acra.ui.ext.stringProperty
import com.vaadin.flow.component.AttachEvent
import com.vaadin.flow.component.DetachEvent
import com.vaadin.flow.component.Tag
import com.vaadin.flow.component.dependency.JsModule
import com.vaadin.flow.component.littemplate.LitTemplate
import com.vaadin.flow.server.StreamRegistration
import com.vaadin.flow.server.StreamResource
import com.vaadin.flow.server.StreamResourceRegistry
import com.vaadin.flow.server.VaadinSession

/**
 * @author lukas
 * @since 23.04.19
 */
@Tag("acrarium-image-with-label")
@JsModule("./elements/image-with-label.ts")
class InstallationView(private val avatarService: AvatarService) : LitTemplate() {
    private var resource: StreamResource? = null
    private var registration: StreamRegistration? = null

    private var image by stringProperty("image")
    private var label by stringProperty("label")

    fun setInstallationId(installationId: String) {
        resource = avatarService.getAvatar(installationId)
        image = StreamResourceRegistry.getURI(resource).toASCIIString()
        label = installationId
        if (parent.isPresent) {
            register()
        }
    }

    override fun onAttach(attachEvent: AttachEvent) {
        if (resource != null) {
            register()
        }
        super.onAttach(attachEvent)
    }

    private fun register() {
        registration = VaadinSession.getCurrent().resourceRegistry.registerResource(resource)
    }

    override fun onDetach(detachEvent: DetachEvent) {
        super.onDetach(detachEvent)
        registration?.unregister()
    }
}