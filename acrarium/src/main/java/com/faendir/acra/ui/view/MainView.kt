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
package com.faendir.acra.ui.view

import com.faendir.acra.i18n.Messages
import com.faendir.acra.model.User
import com.faendir.acra.security.SecurityUtils
import com.faendir.acra.service.UserService
import com.faendir.acra.ui.base.ParentLayout
import com.faendir.acra.ui.component.Path
import com.faendir.acra.ui.component.Translatable
import com.faendir.acra.ui.component.UserEditor
import com.faendir.acra.ui.ext.FlexDirection
import com.faendir.acra.ui.ext.Unit
import com.faendir.acra.ui.ext.setFlexDirection
import com.faendir.acra.ui.ext.setHeight
import com.faendir.acra.ui.ext.setPaddingBottom
import com.faendir.acra.ui.ext.setPaddingRight
import com.faendir.acra.ui.ext.setPaddingTop
import com.faendir.acra.ui.ext.setWidth
import com.faendir.acra.ui.view.user.AccountView
import com.faendir.acra.ui.view.user.UserManager
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasElement
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.applayout.AppLayout
import com.vaadin.flow.component.applayout.DrawerToggle
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.dependency.JsModule
import com.vaadin.flow.component.html.Div
import com.vaadin.flow.component.html.Label
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.login.LoginForm
import com.vaadin.flow.component.login.LoginI18n
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode
import com.vaadin.flow.component.orderedlayout.FlexLayout
import com.vaadin.flow.component.tabs.Tab
import com.vaadin.flow.component.tabs.Tabs
import com.vaadin.flow.server.VaadinService
import com.vaadin.flow.spring.annotation.SpringComponent
import com.vaadin.flow.spring.annotation.UIScope
import org.springframework.context.ApplicationContext
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.InsufficientAuthenticationException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder

/**
 * @author lukas
 * @since 13.07.18
 */
@JsModule("./styles/shared-styles.js")
@UIScope
@SpringComponent
class MainView(private val authManager: AuthenticationManager, private val appContext: ApplicationContext, private val userService: UserService) : ParentLayout() {
    private val layout: AppLayout = AppLayout()
    private lateinit var targets: LinkedHashMap<Tab, Class<out Component>>
    private lateinit var tabs: Tabs

    init {
        alignItems = FlexComponent.Alignment.CENTER
        justifyContentMode = JustifyContentMode.CENTER
        setSizeFull()
        layout.element.style["width"] = "100%"
        layout.element.style["height"] = "100%"
        layout.primarySection = AppLayout.Section.DRAWER
        setRouterRoot(layout)
        when {
            SecurityUtils.isLoggedIn() -> showMain()
            userService.hasAdmin() -> showLogin()
            else -> showFirstTimeSetup()
        }
    }

    override fun showRouterLayoutContent(content: HasElement) {
        super.showRouterLayoutContent(content)
        if (::targets.isInitialized) {
            targets.entries.firstOrNull { content.javaClass == it.value }?.let { tabs.selectedTab = it.key }
        }
    }

    private fun showMain() {
        targets = LinkedHashMap()
        targets[com.faendir.acra.ui.component.Tab(Messages.HOME)] = Overview::class.java
        targets[Path(appContext)] = Component::class.java
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
        layout.addToDrawer(tabs)
        val drawerToggle = DrawerToggle()
        val button = Translatable.createButton(Messages.LOGOUT) { logout() }.with {
            icon = VaadinIcon.POWER_OFF.create()
            removeThemeVariants(ButtonVariant.LUMO_PRIMARY)
            addThemeVariants(ButtonVariant.LUMO_TERTIARY)
        }
        button.setPaddingRight(10.0, Unit.PIXEL)
        val spacer = Div()
        expand(spacer)
        val image = Translatable.createImage("frontend/logo.png", Messages.ACRARIUM)
        image.setHeight(32, Unit.PIXEL)
        layout.addToNavbar(drawerToggle, image, spacer, button)
        content = layout
    }

    private fun showLogin() {
        val logo = Translatable.createImage("frontend/logo.png", Messages.ACRARIUM)
        logo.setWidth(0, Unit.PIXEL)
        val logoWrapper = FlexLayout(logo)
        logoWrapper.expand(logo)
        val loginI18n = LoginI18n.createDefault()
        loginI18n.form.title = ""
        val loginForm = LoginForm(loginI18n)
        loginForm.isForgotPasswordButtonVisible = false
        loginForm.element.style["padding"] = "0"
        loginForm.addLoginListener {
            if (!login(it.username, it.password)) {
                it.source.isError = true
            }
        }
        val layout = FlexLayout(logoWrapper, loginForm)
        layout.setFlexDirection(FlexDirection.COLUMN)
        layout.setSizeUndefined()
        content = layout
    }

    private fun showFirstTimeSetup() {
        val logo = Translatable.createImage("frontend/logo.png", Messages.ACRARIUM)
        logo.setWidthFull()
        logo.setPaddingTop(0.5, Unit.REM)
        logo.setPaddingBottom(1.0, Unit.REM)
        val welcomeLabel = Translatable.createLabel(Messages.WELCOME)
        welcomeLabel.style["font-size"] = "var(--lumo-font-size-xxl)"
        val header = FlexLayout(welcomeLabel, logo, Translatable.createLabel(Messages.CREATE_ADMIN))
        header.setFlexDirection(FlexDirection.COLUMN)
        header.setAlignSelf(FlexComponent.Alignment.CENTER, welcomeLabel)
        header.setWidth(0, Unit.PIXEL)
        val wrapper = FlexLayout(header)
        wrapper.expand(header)
        val userEditor = UserEditor(userService, null) { UI.getCurrent().page.reload() }
        val layout = FlexLayout(wrapper, userEditor)
        layout.setFlexDirection(FlexDirection.COLUMN)
        layout.setSizeUndefined()
        content = layout
    }

    private fun login(username: String, password: String): Boolean {
        return try {
            val token = authManager.authenticate(UsernamePasswordAuthenticationToken(username.toLowerCase(), password))
            if (!token.authorities.contains(User.Role.USER)) {
                throw InsufficientAuthenticationException("Missing required role")
            }
            VaadinService.reinitializeSession(VaadinService.getCurrentRequest())
            SecurityContextHolder.getContext().authentication = token
            UI.getCurrent().page.reload()
            true
        } catch (ex: AuthenticationException) {
            false
        }
    }

    private fun logout() {
        SecurityContextHolder.clearContext()
        ui.ifPresent {
            it.page.reload()
            it.session.close()
        }
    }
}