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
package com.faendir.acra.rest

import com.faendir.acra.model.App
import com.faendir.acra.model.Bug
import com.faendir.acra.model.Report
import com.faendir.acra.model.Stacktrace
import com.faendir.acra.model.Version
import com.faendir.acra.service.DataService
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.ZonedDateTime

/**
 * @author lukas
 * @since 23.08.18
 */
@RestController
@RequestMapping(RestApiInterface.API_PATH)
@PreAuthorize("hasRole(T(com.faendir.acra.model.User\$Role).API)")
class RestApiInterface(private val dataService: DataService) {
    @RequestMapping(value = ["apps"], produces = [MediaType.APPLICATION_JSON_VALUE], method = [RequestMethod.GET])
    fun listApps(): List<Int> = dataService.getAppIds()

    @RequestMapping(value = ["apps/{id}"], produces = [MediaType.APPLICATION_JSON_VALUE], method = [RequestMethod.GET])
    fun getApp(@PathVariable id: Int): App? = dataService.findApp(id)

    @RequestMapping(value = ["apps/{id}/bugs"], produces = [MediaType.APPLICATION_JSON_VALUE], method = [RequestMethod.GET])
    fun listBugs(@PathVariable id: Int): List<Int> = dataService.findApp(id)?.let { dataService.getBugIds(it) } ?: emptyList()

    @RequestMapping(value = ["bugs/{id}"], produces = [MediaType.APPLICATION_JSON_VALUE], method = [RequestMethod.GET])
    fun getBug(@PathVariable id: Int): Bug? = dataService.findBug(id)

    @RequestMapping(value = ["bugs/{id}/stacktraces"], produces = [MediaType.APPLICATION_JSON_VALUE], method = [RequestMethod.GET])
    fun listStacktraces(@PathVariable id: Int): List<Int> = dataService.findBug(id)?.let { dataService.getStacktraceIds(it) } ?: emptyList()

    @RequestMapping(value = ["stacktraces/{id}"], produces = [MediaType.APPLICATION_JSON_VALUE], method = [RequestMethod.GET])
    fun getStacktrace(@PathVariable id: Int): Stacktrace? = dataService.findStacktrace(id)

    @RequestMapping(value = ["stacktraces/{id}/reports"], produces = [MediaType.APPLICATION_JSON_VALUE], method = [RequestMethod.GET])
    fun listReports(@PathVariable id: Int): List<String> = dataService.findStacktrace(id)?.let { dataService.getReportIds(it) } ?: emptyList()

    @RequestMapping(value = ["apps/{id}/reports"], produces = [MediaType.APPLICATION_JSON_VALUE], method = [RequestMethod.GET])
    fun listReportsOfApp(
        @PathVariable id: Int, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) before: ZonedDateTime?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) after: ZonedDateTime?
    ): List<String> =
        dataService.findApp(id)?.let { dataService.getReportIds(it, before, after) } ?: emptyList()

    @RequestMapping(value = ["reports/{id}"], produces = [MediaType.APPLICATION_JSON_VALUE], method = [RequestMethod.GET])
    fun getReport(@PathVariable id: String): Report? = dataService.findReport(id)

    @RequestMapping(value = ["apps/{id}/version/upload/{code}"], consumes = [MediaType.TEXT_PLAIN_VALUE], method = [RequestMethod.POST])
    fun uploadProguardMapping(
        @PathVariable id: Int,
        @PathVariable code: Int,
        @RequestParam(required = false) name: String?,
        @RequestBody content: String
    ): ResponseEntity<*> {
        val app = dataService.findApp(id) ?: return ResponseEntity.notFound().build<Any>()
        dataService.storeVersion(dataService.findVersion(app, code)?.also { version ->
            name?.let { it -> version.name = it }
            version.mappings = content
        } ?: Version(app, code, name ?: "N/A", content))
        return ResponseEntity.ok().build<Any>()
    }

    companion object {
        const val API_PATH = "api"
    }

}