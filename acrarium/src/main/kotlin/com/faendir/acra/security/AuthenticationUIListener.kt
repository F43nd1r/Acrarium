package com.faendir.acra.security

import com.faendir.acra.service.UserService
import com.faendir.acra.ui.view.Overview
import com.faendir.acra.ui.view.login.LoginView
import com.faendir.acra.ui.view.login.SetupView
import com.vaadin.flow.server.UIInitEvent
import com.vaadin.flow.server.UIInitListener
import org.springframework.stereotype.Component


@Component
class AuthenticationUIListener(private val userService: UserService) : UIInitListener {

    override fun uiInit(event: UIInitEvent) {
        event.ui.addBeforeEnterListener {
            when (it.navigationTarget) {
                LoginView::class.java -> {
                    if (SecurityUtils.isLoggedIn()) {
                        it.rerouteTo(Overview::class.java)
                    } else if (!userService.hasAdmin()) {
                        it.rerouteTo(SetupView::class.java)
                    }
                }
                SetupView::class.java -> {
                    if (SecurityUtils.isLoggedIn()) {
                        it.rerouteTo(Overview::class.java)
                    } else if (userService.hasAdmin()) {
                        it.rerouteTo(LoginView::class.java)
                    }
                }
                else -> if (!SecurityUtils.isLoggedIn()) {
                    it.rerouteTo(if (userService.hasAdmin()) LoginView::class.java else SetupView::class.java)
                }
            }
        }
    }
}