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
package com.faendir.acra.service

import com.faendir.acra.model.view.VReport
import com.talanlabs.avatargenerator.Avatar
import com.talanlabs.avatargenerator.IdenticonAvatar
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.html.Image
import com.vaadin.flow.server.InputStreamFactory
import com.vaadin.flow.server.StreamResource
import org.springframework.cache.annotation.Cacheable
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import java.io.ByteArrayInputStream
import javax.annotation.Resource

/**
 * @author lukas
 * @since 29.07.18
 */
@Service
class AvatarService {
    private val avatar: Avatar = IdenticonAvatar.newAvatarBuilder().size(32, 32).build()

    @Lazy
    @Resource
    private lateinit var self: AvatarService

    fun getAvatar(report: VReport): Component = Image(self.getAvatar(report.installationId), report.installationId)

    @Cacheable("avatars")
    fun getAvatar(installationId: String): StreamResource {
        val bytes = avatar.createAsPngBytes(installationId.hashCode().toLong())
        return StreamResource("", InputStreamFactory { ByteArrayInputStream(bytes) })
    }

}