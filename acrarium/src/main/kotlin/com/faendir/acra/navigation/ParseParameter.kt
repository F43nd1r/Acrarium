package com.faendir.acra.navigation

import com.faendir.acra.model.App
import com.faendir.acra.model.Bug
import com.faendir.acra.model.Report
import com.faendir.acra.service.DataService
import java.lang.annotation.Inherited
import kotlin.reflect.KClass

@Inherited
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ParseParameter(val value: KClass<out ParameterParser<*>>)

interface ParameterParser<T : Any> {
    fun parse(dataService: DataService, string: String) : T?
    val type: KClass<T>
}

class AppParser : ParameterParser<App> {
    override fun parse(dataService: DataService, string: String): App? {
        return dataService.findApp(string)
    }

    override val type: KClass<App> = App::class
}

class BugParser : ParameterParser<Bug> {
    override fun parse(dataService: DataService, string: String): Bug? {
        return dataService.findBug(string.toInt())
    }

    override val type: KClass<Bug> = Bug::class
}

class ReportParser : ParameterParser<Report> {
    override fun parse(dataService: DataService, string: String): Report? {
        return dataService.findReport(string)
    }

    override val type: KClass<Report> = Report::class
}
