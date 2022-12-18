package com.faendir.acra.domain

import com.faendir.acra.persistence.app.AppRepository
import com.faendir.acra.persistence.bug.BugIdentifier
import com.faendir.acra.persistence.bug.BugRepository
import com.faendir.acra.persistence.device.DeviceRepository
import com.faendir.acra.persistence.report.Report
import com.faendir.acra.persistence.report.ReportRepository
import com.faendir.acra.persistence.version.VersionKey
import com.faendir.acra.persistence.version.VersionRepository
import com.faendir.acra.settings.AcrariumConfiguration
import com.faendir.acra.util.findInt
import com.faendir.acra.util.findString
import com.faendir.acra.util.toDate
import org.acra.ReportField
import org.intellij.lang.annotations.Language
import org.jooq.JSON
import org.json.JSONObject
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

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

    @Transactional
    @PreAuthorize("hasRole(T(com.faendir.acra.persistence.user.Role).REPORTER)")
    fun create(
        reporterUserName: String,
        @Language("JSON")
        content: String,
        attachments: List<MultipartFile>
    ): Report {
        val appId = appRepository.findId(reporterUserName) ?: throw IllegalArgumentException("No app for reporter $reporterUserName")

        val json = JSONObject(content)

        val stacktrace = json.getString(ReportField.STACK_TRACE.name)
        val bugIdentifier = BugIdentifier.fromStacktrace(acrariumConfiguration, appId, stacktrace)

        val date = json.getString(ReportField.USER_CRASH_DATE.name).toDate()
        val buildConfig: JSONObject? = json.optJSONObject(ReportField.BUILD_CONFIG.name)
        val versionCode: Int = buildConfig?.findInt("VERSION_CODE") ?: json.findInt(ReportField.APP_VERSION_CODE.name) ?: 0
        val versionName: String = buildConfig?.findString("VERSION_NAME") ?: json.findString(ReportField.APP_VERSION_NAME.name) ?: "N/A"
        val flavor: String? = buildConfig?.findString("FLAVOR")

        versionRepository.ensureExists(appId, versionCode, flavor, versionName)

        val bugId = bugRepository.findId(bugIdentifier) ?: bugRepository.create(bugIdentifier, stacktrace.substringBefore('\n'))

        val phoneModel = json.optString(ReportField.PHONE_MODEL.name)
        val device = json.optJSONObject(ReportField.BUILD.name)?.optString("DEVICE") ?: ""
        val reportId = json.getString(ReportField.REPORT_ID.name)
        val report = Report(
            id = reportId,
            androidVersion = json.optString(ReportField.ANDROID_VERSION.name),
            content = JSON.json(content),
            date = date,
            phoneModel = phoneModel,
            userComment = json.optString(ReportField.USER_COMMENT.name),
            userEmail = json.optString(ReportField.USER_EMAIL.name),
            brand = json.optString(ReportField.BRAND.name),
            installationId = json.getString(ReportField.INSTALLATION_ID.name),
            isSilent = json.optBoolean(ReportField.IS_SILENT.name),
            device = device,
            marketingDevice = deviceRepository.findMarketingName(phoneModel, device) ?: device,
            bugId = bugId,
            appId = appId,
            stacktrace = stacktrace,
            exceptionClass = bugIdentifier.exceptionClass,
            message = bugIdentifier.message,
            crashLine = bugIdentifier.crashLine,
            cause = bugIdentifier.cause,
            versionKey = VersionKey(versionCode, flavor ?: ""),
        )
        reportRepository.create(report, attachments.associate { (it.originalFilename ?: it.name) to it.bytes })
        mailService?.onNewReport(report)
        return report
    }
}