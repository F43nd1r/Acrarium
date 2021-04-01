package com.faendir.acra.navigation

import com.faendir.acra.ui.view.login.LoginView
import com.vaadin.flow.server.VaadinServletRequest
import com.vaadin.flow.server.VaadinServletResponse
import org.springframework.security.web.savedrequest.DefaultSavedRequest
import org.springframework.security.web.savedrequest.HttpSessionRequestCache
import com.faendir.acra.security.SecurityUtils
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


class LoginRequestCache : HttpSessionRequestCache() {

    override fun saveRequest(request: HttpServletRequest, response: HttpServletResponse) {
        if (!SecurityUtils.isFrameworkInternalRequest(request)) {
            super.saveRequest(request, response)
        }
    }

    fun resolveRedirectUrl(): String {
        val savedRequest = getRequest(VaadinServletRequest.getCurrent().httpServletRequest, VaadinServletResponse.getCurrent().httpServletResponse)
        return (savedRequest as? DefaultSavedRequest)?.requestURI?.takeIf { it.isNotBlank() && !it.contains(LoginView.ROUTE) }?.removePrefix("/") ?: ""
    }
}