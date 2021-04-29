package com.faendir.acra.security

import com.faendir.acra.service.UserService
import com.faendir.acra.ui.view.Overview
import com.faendir.acra.ui.view.login.LoginView
import com.faendir.acra.ui.view.login.SetupView
import com.vaadin.flow.component.UI
import com.vaadin.flow.router.BeforeEnterEvent
import com.vaadin.flow.router.NotFoundException
import com.vaadin.flow.server.UIInitEvent
import com.vaadin.flow.server.UIInitListener
import org.springframework.beans.factory.BeanCreationNotAllowedException
import org.springframework.beans.factory.BeanNotOfRequiredTypeException
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessor
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.stereotype.Component


@Component
class AuthenticationUIListener(private val userService: UserService) : UIInitListener, InstantiationAwareBeanPostProcessor {
    private val cache = mutableMapOf<Pair<Int, Class<*>>, BeforeEnterEvent>()

    override fun uiInit(init: UIInitEvent) {
        init.ui.addBeforeEnterListener { event ->
            when (event.navigationTarget) {
                LoginView::class.java -> {
                    if (SecurityUtils.isLoggedIn()) {
                        event.rerouteTo(Overview::class.java)
                    } else if (!userService.hasAdmin()) {
                        event.rerouteTo(SetupView::class.java)
                    }
                }
                SetupView::class.java -> {
                    if (SecurityUtils.isLoggedIn()) {
                        event.rerouteTo(Overview::class.java)
                    } else if (userService.hasAdmin()) {
                        event.rerouteTo(LoginView::class.java)
                    }
                }
                else -> if (!SecurityUtils.isLoggedIn()) {
                    event.rerouteTo(if (userService.hasAdmin()) LoginView::class.java else SetupView::class.java)
                } else if (event.navigationTarget.hasAnnotation<RequiresRole>() || event.navigationTarget.hasAnnotation<RequiresPermission>()) {
                    cache[event.ui.uiId to event.navigationTarget] = event
                }
            }
        }
        init.ui.addAfterNavigationListener {
            cache.remove(it.locationChangeEvent.ui.uiId to it.activeChain[0].javaClass)
        }
    }

    private inline fun <reified T : Annotation> Class<*>.hasAnnotation(): Boolean = AnnotationUtils.findAnnotation(this, T::class.java) != null

    override fun postProcessBeforeInstantiation(targetClass: Class<*>, beanName: String): Any? {
        val requiresRole = AnnotationUtils.findAnnotation(targetClass, RequiresRole::class.java)
        if (requiresRole != null) {
            if (!SecurityUtils.hasRole(requiresRole.value)) {
                cache[UI.getCurrent().uiId to targetClass]?.rerouteToError(NotFoundException::class.java)
                throw BeanCreationNotAllowedException(beanName, "Missing required role ${requiresRole.value}")
            }
        }
        return null
    }

    override fun postProcessAfterInitialization(target: Any, beanName: String): Any? {
        val requiresPermission = AnnotationUtils.findAnnotation(target.javaClass, RequiresPermission::class.java)
        if (requiresPermission != null) {
            if (target is HasApp) {
                if (!SecurityUtils.hasPermission(target.app, requiresPermission.value)) {
                    cache[UI.getCurrent().uiId to target.javaClass]?.rerouteToError(NotFoundException::class.java)
                    throw BeanCreationNotAllowedException(beanName, "Missing required permission ${requiresPermission.value}")
                }
            } else {
                throw BeanNotOfRequiredTypeException(beanName, HasApp::class.java, target.javaClass)
            }
        }
        return target
    }
}