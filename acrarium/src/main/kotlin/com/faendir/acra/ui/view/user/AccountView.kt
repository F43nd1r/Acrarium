/*
 * (C) Copyright 2018-2022 Lukas Morawietz (https://github.com/F43nd1r)
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
package com.faendir.acra.ui.view.user

import com.faendir.acra.domain.MailService
import com.faendir.acra.i18n.Messages
import com.faendir.acra.i18n.TranslatableText
import com.faendir.acra.navigation.View
import com.faendir.acra.persistence.user.UserRepository
import com.faendir.acra.security.SecurityUtils
import com.faendir.acra.ui.component.HasAcrariumTitle
import com.faendir.acra.ui.component.UserEditor
import com.faendir.acra.ui.ext.content
import com.faendir.acra.ui.view.main.MainView
import com.vaadin.flow.component.Composite
import com.vaadin.flow.component.notification.Notification
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.FlexLayout
import com.vaadin.flow.router.Route
import org.springframework.beans.factory.annotation.Autowired

/**
 * @author lukas
 * @since 26.02.19
 */
@View
@Route(value = "account", layout = MainView::class)
class AccountView(
    private val userRepository: UserRepository,
    @Autowired(required = false)
    private val mailService: MailService?
) :
    Composite<FlexLayout>(),
    HasAcrariumTitle {
    init {
        content {
            setSizeFull()
            justifyContentMode = FlexComponent.JustifyContentMode.CENTER
            alignItems = FlexComponent.Alignment.CENTER
            add(UserEditor(userRepository, mailService, SecurityUtils.getUsername()) { Notification.show(getTranslation(Messages.SUCCESS)) })
        }
    }

    override val title = TranslatableText(Messages.ACCOUNT)

}