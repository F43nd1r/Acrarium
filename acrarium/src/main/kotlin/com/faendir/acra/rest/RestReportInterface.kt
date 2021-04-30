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

import com.faendir.acra.model.QReport
import com.faendir.acra.service.DataService
import com.querydsl.core.types.dsl.BooleanExpression
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.util.StreamUtils
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartHttpServletRequest
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.security.Principal

/**
 * @author Lukas
 * @since 22.03.2017
 */
@RestController
class RestReportInterface(private val dataService: DataService) {
    @PreAuthorize("hasRole(T(com.faendir.acra.model.User\$Role).REPORTER)")
    @RequestMapping(value = [REPORT_PATH], consumes = [MediaType.APPLICATION_JSON_VALUE], method = [RequestMethod.POST])
    fun report(@RequestBody content: String, principal: Principal) {
        if (content.isNotBlank()) dataService.createNewReport(principal.name, content, emptyList())
    }

    @PreAuthorize("hasRole(T(com.faendir.acra.model.User\$Role).REPORTER)")
    @RequestMapping(value = [REPORT_PATH], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE], method = [RequestMethod.POST])
    @Throws(IOException::class)
    fun report(request: MultipartHttpServletRequest, principal: Principal): ResponseEntity<*> {
        val fileMap = request.multiFileMap
        val report = fileMap[REPORT]
        if (report == null || report.isEmpty()) {
            return ResponseEntity.badRequest().build<Any>()
        }
        val content = StreamUtils.copyToString(report[0].inputStream, StandardCharsets.UTF_8)
        val attachments = fileMap[ATTACHMENT] ?: emptyList()
        dataService.createNewReport(principal.name, content, attachments)
        return ResponseEntity.ok().build<Any>()
    }

    companion object {
        const val PARAM_APP = "app_id"
        const val PARAM_ID = "id"
        const val PARAM_MAIL = "mail"
        const val REPORT_PATH = "report"
        const val REPORT = "ACRA_REPORT"
        const val ATTACHMENT = "ACRA_ATTACHMENT"
    }

}