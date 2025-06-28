/*
 * (C) Copyright 2022-2025 Lukas Morawietz (https://github.com/F43nd1r)
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
package com.faendir.acra.domain

import com.faendir.acra.persistence.app.AppId
import com.faendir.acra.persistence.app.AppRepository
import com.faendir.acra.persistence.bug.BugIdentifier
import com.faendir.acra.persistence.bug.BugRepository
import com.faendir.acra.persistence.device.DeviceRepository
import com.faendir.acra.persistence.report.Report
import com.faendir.acra.persistence.report.ReportRepository
import com.faendir.acra.persistence.version.VersionRepository
import com.faendir.acra.settings.AcrariumConfiguration
import com.faendir.acra.util.findInt
import com.faendir.acra.util.findString
import com.faendir.acra.util.toDate
import mu.KotlinLogging
import org.acra.ReportField
import org.intellij.lang.annotations.Language
import org.jooq.JSON
import org.json.JSONException
import org.json.JSONObject
import org.springframework.dao.DuplicateKeyException
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.Instant


private val logger = KotlinLogging.logger {}

@Service
class ReportService(
    private val reportRepository: ReportRepository,
    private val deviceRepository: DeviceRepository,
    private val bugRepository: BugRepository,
    private val appRepository: AppRepository,
    private val versionRepository: VersionRepository,
    private val acrariumConfiguration: AcrariumConfiguration,
    private val mailService: MailService?,
) {

    @PreAuthorize("isReporter()")
    fun create(
        reporterUserName: String,
        @Language("JSON")
        content: String,
        attachments: List<MultipartFile>
    ): Report {
        val appId = appRepository.findId(reporterUserName) ?: throw IllegalArgumentException("No app for reporter $reporterUserName")


        val json = try {
            JSONObject(content)
        } catch (e: JSONException) {
            throw IllegalArgumentException("Invalid JSON:\n$content", e)
        }
        val reportId = json.getString(ReportField.REPORT_ID.name)

        synchronized(this) {
            reportRepository.find(reportId)?.let {
                logger.info { "Received report with id $reportId a second time, ignoring." }
                return it
            }
        }

        val stacktrace = json.getString(ReportField.STACK_TRACE.name)
        val bugIdentifier = BugIdentifier.fromStacktrace(acrariumConfiguration, appId, stacktrace)

        val date = json.getString(ReportField.USER_CRASH_DATE.name).toDate()
        val buildConfig: JSONObject? = json.optJSONObject(ReportField.BUILD_CONFIG.name)
        val versionCode: Int = buildConfig?.findInt("VERSION_CODE") ?: json.findInt(ReportField.APP_VERSION_CODE.name) ?: 0
        val versionName: String = buildConfig?.findString("VERSION_NAME") ?: json.findString(ReportField.APP_VERSION_NAME.name) ?: "N/A"
        val flavor: String? = buildConfig?.findString("FLAVOR")

        versionRepository.ensureExists(appId, versionCode, flavor, versionName)

        val report = saveReport(date, content, json, appId, bugIdentifier, versionCode, flavor, attachments)

        mailService?.onNewReport(report)
        return report
    }


    @Synchronized
    @Transactional
    @PreAuthorize("isReporter()")
    protected fun saveReport(
        date: Instant,
        content: String,
        json: JSONObject,
        appId : AppId,
        bugIdentifier: BugIdentifier,
        versionCode : Int,
        flavor : String?,
        attachments: List<MultipartFile>
    ) : Report {
        val reportId = json.getString(ReportField.REPORT_ID.name)
        val stacktrace = json.getString(ReportField.STACK_TRACE.name)
        val phoneModel = json.optString(ReportField.PHONE_MODEL.name)
        val device = json.optJSONObject(ReportField.BUILD.name)?.optString("DEVICE") ?: ""
        val androidVersion = json.optString(ReportField.ANDROID_VERSION.name)
        val content2 = JSON.json(content)
        val userComment = json.optString(ReportField.USER_COMMENT.name)
        val userEmail = json.optString(ReportField.USER_EMAIL.name)
        val brand = json.optString(ReportField.BRAND.name)
        val installationId = json.getString(ReportField.INSTALLATION_ID.name)
        val isSilent = json.optBoolean(ReportField.IS_SILENT.name)
        val marketingDevice = deviceRepository.findMarketingName(phoneModel, device) ?: device

        val bugId = bugRepository.findId(bugIdentifier) ?: bugRepository.create(bugIdentifier, stacktrace.substringBefore('\n').trim())

        val report = Report(
            id = reportId,
            androidVersion = androidVersion,
            content = content2,
            date = date,
            phoneModel = phoneModel,
            userComment = userComment,
            userEmail = userEmail,
            brand = brand,
            installationId = installationId,
            isSilent = isSilent,
            device = device,
            marketingDevice = marketingDevice,
            bugId = bugId,
            appId = appId,
            stacktrace = stacktrace,
            exceptionClass = bugIdentifier.exceptionClass,
            message = bugIdentifier.message,
            crashLine = bugIdentifier.crashLine,
            cause = bugIdentifier.cause,
            versionCode = versionCode,
            versionFlavor = flavor ?: "",
        )

        reportRepository.find(reportId)?.let {
            logger.info { "Race condition while saving $reportId, ignoring." }
            return it
        }
        try {
            reportRepository.create(report, attachments.associate { (it.originalFilename ?: it.name) to it.bytes })
        } catch (e: DuplicateKeyException) {
            logger.warn { "Failed to save the report $reportId because it already exists." }
        }

        return report
    }
}