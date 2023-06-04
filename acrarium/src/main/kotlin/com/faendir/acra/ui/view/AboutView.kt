/*
 * (C) Copyright 2017-2021 Lukas Morawietz (https://github.com/F43nd1r)
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
package com.faendir.acra.ui.view

import com.faendir.acra.i18n.Messages
import com.faendir.acra.i18n.TranslatableText
import com.faendir.acra.navigation.View
import com.faendir.acra.ui.component.HasAcrariumTitle
import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.ui.view.main.MainView
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode
import com.vaadin.flow.component.orderedlayout.FlexLayout
import com.vaadin.flow.router.Route
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.info.BuildProperties


/**
 * @author lukas
 * @since 06.09.19
 */
@Suppress("LeakingThis")
@View
@Route(value = "about", layout = MainView::class)
class AboutView(@Autowired val buildProperties: BuildProperties) : FlexLayout(), HasAcrariumTitle {

    init {
        setSizeFull()
        justifyContentMode = JustifyContentMode.CENTER
        alignItems = FlexComponent.Alignment.CENTER
        flexDirection = FlexDirection.COLUMN
        val info = Div()
        info.element.setProperty("innerHTML", getTranslation(Messages.FOOTER))
        val version = Translatable.createDiv(Messages.ABOUT_VERSION, buildProperties.version)
        add(info, version)
    }

    override val title = TranslatableText(Messages.ABOUT)
}