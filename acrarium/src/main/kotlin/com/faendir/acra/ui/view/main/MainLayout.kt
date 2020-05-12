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
import com.faendir.acra.ui.component.Path
import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.ui.ext.Unit
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
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.tabs.Tab
import com.vaadin.flow.component.tabs.Tabs
import com.vaadin.flow.router.RouterLayout
import org.springframework.context.ApplicationContext
import org.springframework.security.core.context.SecurityContextHolder

class MainLayout(applicationContext: ApplicationContext) : Composite<AppLayout>(), RouterLayout {
    private val targets: LinkedHashMap<Tab, Class<out Component>> = LinkedHashMap()
    private val tabs: Tabs

    init {
        content.element.style["width"] = "100%"
        content.element.style["height"] = "100%"
        content.primarySection = AppLayout.Section.DRAWER
        targets[com.faendir.acra.ui.component.Tab(Messages.HOME)] = Overview::class.java
        targets[Path(applicationContext)] = Component::class.java
        targets[com.faendir.acra.ui.component.Tab(Messages.ACCOUNT)] = AccountView::class.java
        if (SecurityUtils.hasRole(User.Role.ADMIN)) {
            targets[com.faendir.acra.ui.component.Tab(Messages.USER_MANAGER)] = UserManager::class.java
        }
        targets[com.faendir.acra.ui.component.Tab(Messages.SETTINGS)] = SettingsView::class.java
        targets[com.faendir.acra.ui.component.Tab(Messages.ABOUT)] = AboutView::class.java
        tabs = Tabs(*targets.keys.toTypedArray())
        tabs.orientation = Tabs.Orientation.VERTICAL
        tabs.addSelectedChangeListener { event ->
            val target = targets[event.selectedTab]
            if (target != null && target != Component::class.java) {
                ui.ifPresent { it.navigate(target) }
            }
        }
        content.addToDrawer(tabs)
        val drawerToggle = DrawerToggle()
        val button = Translatable.createButton(Messages.LOGOUT) { logout() }.with {
            icon = VaadinIcon.POWER_OFF.create()
            removeThemeVariants(ButtonVariant.LUMO_PRIMARY)
            addThemeVariants(ButtonVariant.LUMO_TERTIARY)
        }
        button.setPaddingRight(10.0, Unit.PIXEL)
        val spacer = Div()
        spacer.setFlexGrow(1)
        val image = Translatable.createImage("frontend/logo.png", Messages.ACRARIUM)
        image.setHeight(32, Unit.PIXEL)
        content.addToNavbar(drawerToggle, image, spacer, button)
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
        targets.entries.firstOrNull { content.javaClass == it.value }?.let { tabs.selectedTab = it.key }
    }
}