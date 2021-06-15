package com.faendir.acra.navigation

import com.faendir.acra.service.DataService
import com.faendir.acra.util.PARAM_APP
import com.faendir.acra.util.PARAM_BUG
import com.faendir.acra.util.PARAM_REPORT
import com.faendir.acra.util.toNullable
import com.vaadin.flow.component.UI
import com.vaadin.flow.router.BeforeEnterEvent
import com.vaadin.flow.router.BeforeEnterListener
import com.vaadin.flow.router.ListenerPriority
import com.vaadin.flow.router.RouteParameters
import com.vaadin.flow.server.UIInitEvent
import com.vaadin.flow.server.UIInitListener
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.lang.annotation.Inherited

@Inherited
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Value("#{@parameterParser.parseApp()}")
annotation class ParseAppParameter

@Inherited
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Value("#{@parameterParser.parseBug()}")
annotation class ParseBugParameter

@Inherited
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Value("#{@parameterParser.parseReport()}")
annotation class ParseReportParameter


@Component
@ListenerPriority(1000)
class ParameterParser(private val dataService: DataService) : UIInitListener, BeforeEnterListener {
    private val cache = mutableMapOf<Int, RouteParameters>()
    override fun uiInit(uiInitEvent: UIInitEvent) {
        uiInitEvent.ui.addBeforeEnterListener(this)
    }

    override fun beforeEnter(event: BeforeEnterEvent) {
        cache[event.ui.uiId] = event.routeParameters
    }

    fun parseApp() = parse(PARAM_APP, DataService::findApp)

    fun parseBug() = parse(PARAM_BUG) { findBug(it.toInt()) }

    fun parseReport() = parse(PARAM_REPORT, DataService::findReport)

    fun <T> parse(param: String, parse: DataService.(String) -> T?): T {
        return parse(dataService, cache[UI.getCurrent().uiId]?.get(param)?.toNullable() ?: throw IllegalArgumentException("Parameter $param not present"))
            ?: throw IllegalArgumentException("Parse failure for parameter $param")
    }
}
