package com.faendir.acra.navigation

import com.faendir.acra.persistence.app.AppId
import com.faendir.acra.persistence.bug.BugId
import com.faendir.acra.util.*
import com.vaadin.flow.component.UI
import com.vaadin.flow.router.BeforeEnterEvent
import com.vaadin.flow.router.BeforeEnterListener
import com.vaadin.flow.router.ListenerPriority
import com.vaadin.flow.router.RouteParameters
import com.vaadin.flow.server.UIInitEvent
import com.vaadin.flow.server.UIInitListener
import org.springframework.stereotype.Component


@Component
@ListenerPriority(1000)
class RouteParams : UIInitListener, BeforeEnterListener {
    private val cache = mutableMapOf<Int, RouteParameters>()
    override fun uiInit(uiInitEvent: UIInitEvent) {
        uiInitEvent.ui.addBeforeEnterListener(this)
    }

    override fun beforeEnter(event: BeforeEnterEvent) {
        cache[event.ui.uiId] = event.routeParameters
    }

    fun appId(): AppId = AppId(parse(PARAM_APP) { it.toInt() })

    fun bugId(): BugId = BugId(parse(PARAM_BUG) { it.toInt() })

    fun reportId(): String = parse(PARAM_REPORT) { it }

    fun installationId(): String = parse(PARAM_INSTALLATION) { it }

    fun <T> parse(param: String, parse: (String) -> T?): T {
        return parse(cache[UI.getCurrent().uiId]?.get(param)?.toNullable() ?: throw IllegalArgumentException("Parameter $param not present"))
            ?: throw IllegalArgumentException("Parse failure for parameter $param")
    }
}

const val PARAM_APP = "app"
const val PARAM_BUG = "bug"
const val PARAM_INSTALLATION = "installation"
const val PARAM_REPORT = "report"