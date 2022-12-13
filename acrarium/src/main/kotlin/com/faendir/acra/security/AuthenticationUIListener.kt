package com.faendir.acra.security

import com.faendir.acra.navigation.RouteParams
import com.faendir.acra.persistence.user.UserRepository
import com.faendir.acra.ui.view.Overview
import com.faendir.acra.ui.view.login.LoginView
import com.faendir.acra.ui.view.login.SetupView
import com.vaadin.flow.router.NotFoundException
import com.vaadin.flow.server.UIInitEvent
import com.vaadin.flow.server.UIInitListener
import org.springframework.stereotype.Component


@Component
class AuthenticationUIListener(private val userRepository: UserRepository, private val routeParams: RouteParams) : UIInitListener {
    override fun uiInit(init: UIInitEvent) {
        init.ui.addBeforeEnterListener { event ->
            when (event.navigationTarget) {
                LoginView::class.java -> {
                    if (SecurityUtils.isLoggedIn()) {
                        event.rerouteTo(Overview::class.java)
                    } else if (!userRepository.hasAnyAdmin()) {
                        event.rerouteTo(SetupView::class.java)
                    }
                }
                SetupView::class.java -> {
                    if (SecurityUtils.isLoggedIn()) {
                        event.rerouteTo(Overview::class.java)
                    } else if (userRepository.hasAnyAdmin()) {
                        event.rerouteTo(LoginView::class.java)
                    }
                }
                else -> if (!SecurityUtils.isLoggedIn()) {
                    event.rerouteTo(if (userRepository.hasAnyAdmin()) LoginView::class.java else SetupView::class.java)
                } else if (!SecurityUtils.hasAccess(routeParams::appId, event.navigationTarget)) {
                    event.rerouteToError(NotFoundException::class.java)
                }
            }
        }
    }
}