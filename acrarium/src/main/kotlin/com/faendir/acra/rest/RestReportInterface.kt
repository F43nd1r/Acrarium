/*
 * (C) Copyright 2018-2025 Lukas Morawietz (https://github.com/F43nd1r)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.faendir.acra.rest

import com.faendir.acra.domain.ReportService
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.security.Principal

@RestController
@PreAuthorize("isReporter()")
class RestReportInterface(private val reportService: ReportService) {
    @RequestMapping(value = [REPORT_PATH], consumes = [MediaType.APPLICATION_JSON_VALUE], method = [RequestMethod.POST])
    fun report(@RequestBody content: String, principal: Principal) {
        if (content.isNotBlank()) {
            reportService.create(principal.name, content, emptyList())
        }
    }

    @RequestMapping(value = [REPORT_PATH], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE], method = [RequestMethod.POST])
    fun report(@RequestParam(REPORT) content: String, @RequestParam(ATTACHMENT) attachments: List<MultipartFile>, principal: Principal): ResponseEntity<*> {
        reportService.create(principal.name, content, attachments)
        return ResponseEntity.ok().build<Any>()
    }

    companion object {
        const val REPORT_PATH = "report"
        const val REPORT = "ACRA_REPORT"
        const val ATTACHMENT = "ACRA_ATTACHMENT"
    }

}