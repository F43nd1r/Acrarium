/*
 * (C) Copyright 2020 Lukas Morawietz (https://github.com/F43nd1r)
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
package com.faendir.acra.ui.view.main

import com.faendir.acra.i18n.Messages
import com.faendir.acra.model.User
import com.faendir.acra.security.SecurityUtils
import com.faendir.acra.ui.component.tabs.Path
import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.ui.ext.SizeUnit
import com.faendir.acra.ui.ext.content
import com.faendir.acra.ui.ext.setFlexGrow
import com.faendir.acra.ui.ext.setHeight
import com.faendir.acra.ui.ext.setPaddingRight
import com.faendir.acra.ui.view.AboutView
import com.faendir.acra.ui.view.Overview
import com.faendir.acra.ui.view.SettingsView
import com.faendir.acra.ui.view.user.AccountView
import com.faendir.acra.ui.view.user.UserManager
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.Composite
import com.vaadin.flow.component.HasElement
import com.vaadin.flow.component.applayout.AppLayout
import com.vaadin.flow.component.applayout.DrawerToggle
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.dependency.CssImport
import com.vaadin.flow.component.dependency.JsModule
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.tabs.Tab
import com.vaadin.flow.component.tabs.Tabs
import com.vaadin.flow.router.RouterLayout
import com.vaadin.flow.router.RouterLink
import com.vaadin.flow.spring.annotation.SpringComponent
import com.vaadin.flow.spring.annotation.UIScope
import org.springframework.context.support.GenericApplicationContext
import org.springframework.security.core.context.SecurityContextHolder
import kotlin.reflect.KClass
import kotlin.streams.asSequence

/**
 * @author lukas
 * @since 13.07.18
 */
@JsModule("./styles/shared-styles.js")
@CssImport("./styles/global.css")
@UIScope
@SpringComponent
class MainView(applicationContext: GenericApplicationContext) : Composite<AppLayout>(), RouterLayout {
    private val targets: MutableMap<Class<out HasElement>, Tab> = mutableMapOf()
    private val tabs: Tabs

    init {
        tabs = Tabs().apply {
            orientation = Tabs.Orientation.VERTICAL
            add(createTab<Overview>(Messages.HOME))
            add(Path(applicationContext))
            add(createTab<AccountView>(Messages.ACCOUNT))
            if (SecurityUtils.hasRole(User.Role.ADMIN)) {
                add(createTab<UserManager>(Messages.USER_MANAGER))
            }
            add(createTab<SettingsView>(Messages.SETTINGS))
            add(createTab<AboutView>(Messages.ABOUT))
        }
        content {
            element.style["width"] = "100%"
            element.style["height"] = "100%"
            primarySection = AppLayout.Section.DRAWER
            addToDrawer(tabs)
            addToNavbar(
                DrawerToggle(),
                Translatable.createImage("frontend/logo.png", Messages.ACRARIUM).with {
                    setHeight(32, SizeUnit.PIXEL)
                },
                Div().apply { setFlexGrow(1) },
                Translatable.createButton(Messages.LOGOUT) { logout() }.with {
                    icon = VaadinIcon.POWER_OFF.create()
                    removeThemeVariants(ButtonVariant.LUMO_PRIMARY)
                    addThemeVariants(ButtonVariant.LUMO_TERTIARY)
                    setPaddingRight(10.0, SizeUnit.PIXEL)
                }
            )
        }
    }

    private inline fun <reified T : Component> createTab(captionId: String) : Tab {
        val tab = Tab(Translatable.createRouterLink(T::class, captionId))
        targets[T::class.java] = tab
        return tab
    }

    private fun logout() {
        SecurityContextHolder.clearContext()
        ui.ifPresent {
            it.page.reload()
            it.session.close()
        }
    }

    override fun showRouterLayoutContent(content: HasElement) {
        super.showRouterLayoutContent(content)
        targets[content.javaClass]?.let { tabs.selectedTab = it }
    }
}