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

import com.faendir.acra.persistence.app.App
import com.faendir.acra.persistence.app.AppId
import com.faendir.acra.persistence.app.AppRepository
import com.faendir.acra.persistence.bug.Bug
import com.faendir.acra.persistence.bug.BugId
import com.faendir.acra.persistence.bug.BugRepository
import com.faendir.acra.persistence.report.Report
import com.faendir.acra.persistence.report.ReportRepository
import com.faendir.acra.persistence.version.VersionRepository
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
import java.time.Instant

/**
 * @author lukas
 * @since 23.08.18
 */
@RestController
@RequestMapping(RestApiInterface.API_PATH)
@PreAuthorize("hasRole(T(com.faendir.acra.persistence.user.Role).API)")
class RestApiInterface(
    private val reportRepository: ReportRepository,
    private val versionRepository: VersionRepository,
    private val bugRepository: BugRepository,
    private val appRepository: AppRepository,
) {
    @RequestMapping(value = ["apps"], produces = [MediaType.APPLICATION_JSON_VALUE], method = [RequestMethod.GET])
    fun listApps(): List<AppId> = appRepository.getVisibleIds()

    @RequestMapping(value = ["apps/{id}"], produces = [MediaType.APPLICATION_JSON_VALUE], method = [RequestMethod.GET])
    fun getApp(
        @PathVariable
        id: Int
    ): App? = appRepository.find(AppId(id))

    @RequestMapping(value = ["apps/{id}/bugs"], produces = [MediaType.APPLICATION_JSON_VALUE], method = [RequestMethod.GET])
    fun listBugs(
        @PathVariable
        id: AppId
    ): List<BugId> = bugRepository.getAllIds(id)

    @RequestMapping(value = ["bugs/{id}"], produces = [MediaType.APPLICATION_JSON_VALUE], method = [RequestMethod.GET])
    fun getBug(
        @PathVariable
        id: Int
    ): Bug? = bugRepository.find(BugId(id))

    @RequestMapping(value = ["apps/{id}/reports"], produces = [MediaType.APPLICATION_JSON_VALUE], method = [RequestMethod.GET])
    fun listReportsOfApp(
        @PathVariable
        id: AppId,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        before: Instant?,
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        after: Instant?
    ): List<String> = reportRepository.listIds(id, before, after)

    @RequestMapping(value = ["reports/{id}"], produces = [MediaType.APPLICATION_JSON_VALUE], method = [RequestMethod.GET])
    fun getReport(
        @PathVariable
        id: String
    ): Report? = reportRepository.find(id)

    @RequestMapping(value = ["apps/{id}/version/upload/{code}"], consumes = [MediaType.TEXT_PLAIN_VALUE], method = [RequestMethod.POST])
    fun uploadProguardMapping(
        @PathVariable
        id: AppId,
        @PathVariable
        code: Int,
        @RequestParam(required = false)
        flavor: String?,
        @RequestParam(required = false)
        name: String?,
        @RequestBody
        content: String
    ): ResponseEntity<*> {
        versionRepository.setMappings(id, code, flavor, name, content)
        return ResponseEntity.ok().build<Any>()
    }

    companion object {
        const val API_PATH = "api"
    }

}