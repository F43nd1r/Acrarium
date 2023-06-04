/*
 * (C) Copyright 2018-2022 Lukas Morawietz (https://github.com/F43nd1r)
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

import com.faendir.acra.i18n.Messages
import com.faendir.acra.navigation.PARAM_APP
import com.faendir.acra.navigation.PARAM_BUG
import com.faendir.acra.persistence.app.AppRepository
import com.faendir.acra.persistence.bug.Bug
import com.faendir.acra.persistence.bug.BugRepository
import com.faendir.acra.persistence.mailsettings.MailSettings
import com.faendir.acra.persistence.mailsettings.MailSettingsRepository
import com.faendir.acra.persistence.report.Report
import com.faendir.acra.persistence.report.ReportRepository
import com.faendir.acra.persistence.user.User
import com.faendir.acra.persistence.user.UserRepository
import com.faendir.acra.persistence.version.VersionRepository
import com.faendir.acra.ui.view.bug.tabs.ReportTab
import com.faendir.acra.util.ensureTrailing
import com.vaadin.flow.i18n.I18NProvider
import com.vaadin.flow.router.RouteConfiguration
import com.vaadin.flow.router.RouteParameters
import jakarta.mail.MessagingException
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit
import java.util.*

/**
 * @author lukas
 * @since 07.12.18
 */
@Service
@EnableScheduling
@EnableAsync
@ConditionalOnProperty(prefix = "spring.mail", name = ["host"])
class MailService(
    private val mailSettingsRepository: MailSettingsRepository,
    private val appRepository: AppRepository,
    private val bugRepository: BugRepository,
    private val versionRepository: VersionRepository,
    private val reportRepository: ReportRepository,
    private val userRepository: UserRepository,
    private val i18nProvider: I18NProvider,
    private val mailSender: JavaMailSender,
    private val routeConfiguration: RouteConfiguration
) {

    @Value("\${server.context-path}")
    private val baseUrl: String? = null

    @Value("\${spring.mail.sender}")
    private val sender: String? = null

    @Transactional
    @Async
    fun onNewReport(report: Report) {
        val settings = mailSettingsRepository.findAll(report.appId)
        val newBugReceiver = getMailsBy(settings, MailSettings::newBug)
        val regressionReceiver = getMailsBy(settings, MailSettings::regression)
        val spikeReceiver = getMailsBy(settings, MailSettings::spike)
        val bug by lazy { bugRepository.find(report.bugId)!! }
        val app by lazy { appRepository.find(report.appId)!! }
        val version by lazy { versionRepository.find(report.appId, report.versionKey)!! }
        if (newBugReceiver.isNotEmpty() && bug.reportCount == 1
        ) {
            sendMessage(
                newBugReceiver, getTranslation(
                    Messages.NEW_BUG_MAIL_TEMPLATE,
                    getBugUrl(bug),
                    bug.title,
                    report.brand ?: "",
                    report.phoneModel ?: "",
                    report.androidVersion ?: "",
                    app.name,
                    version.name
                ), getTranslation(Messages.NEW_BUG_MAIL_SUBJECT, app.name)
            )
        } else if (regressionReceiver.isNotEmpty() && bug.solvedVersionKey != null && bug.solvedVersionKey!!.code <= version.code) {
            sendMessage(
                regressionReceiver, getTranslation(
                    Messages.REGRESSION_MAIL_TEMPLATE,
                    getBugUrl(bug),
                    bug.title,
                    report.brand ?: "",
                    versionRepository.find(bug.appId, bug.solvedVersionKey!!)!!.name,
                    report.phoneModel ?: "",
                    report.androidVersion ?: "",
                    app.name,
                    version.name
                ), getTranslation(Messages.REGRESSION_MAIL_SUBJECT, app.name)
            )
        } else if (spikeReceiver.isNotEmpty()) {
            val reportCount = fetchReportCountOnDay(bug, 0)
            val averageCount = (1L..3L).map { subtractDays -> fetchReportCountOnDay(bug, subtractDays) }.average()
            val spikeBarrier = 1.2 * averageCount
            if (reportCount >= spikeBarrier && reportCount - 1 < spikeBarrier) {
                sendMessage(
                    regressionReceiver, getTranslation(
                        Messages.SPIKE_MAIL_TEMPLATE,
                        getBugUrl(bug),
                        bug.title,
                        version.name,
                        reportCount
                    ), getTranslation(Messages.SPIKE_MAIL_SUBJECT, app.name)
                )
            }
        }
    }

    private fun getMailsBy(list: List<MailSettings>, predicate: (MailSettings) -> Boolean): List<String> =
        list.filter(predicate).map(MailSettings::username).mapNotNull(userRepository::find).mapNotNull(User::mail)

    private fun fetchReportCountOnDay(bug: Bug, subtractDays: Long): Int {
        val today = OffsetDateTime.now().truncatedTo(ChronoUnit.DAYS)
        return reportRepository.countInRange(bug.appId, bug.id, today.minusDays(subtractDays).toInstant()..today.minusDays(subtractDays - 1).toInstant())
    }

    @Scheduled(cron = "0 0 0 * * SUN")
    fun weeklyReport() {
        val settings = mailSettingsRepository.getAll().filter { it.summary }.groupBy { it.appId }
        for ((appId, mailSettings) in settings) {
            val today = OffsetDateTime.now().truncatedTo(ChronoUnit.DAYS)
            val bugs = bugRepository.findInRange(appId, today.minusWeeks(1L).toInstant()..today.toInstant())
            var body = bugs.joinToString("\n") { bug -> getTranslation(Messages.WEEKLY_MAIL_BUG_TEMPLATE, getBugUrl(bug), bug.title, bug.reportCount, bug.affectedInstallations) }
            if (body.isEmpty()) {
                body = getTranslation(Messages.WEEKLY_MAIL_NO_REPORTS)
            }
            sendMessage(
                mailSettings.map(MailSettings::username).mapNotNull(userRepository::find).mapNotNull(User::mail),
                body,
                getTranslation(Messages.WEEKLY_MAIL_SUBJECT, appRepository.find(appId)!!.name)
            )
        }
    }

    fun testMessage(mail: String) {
        sendMessage(listOf(mail), getTranslation(Messages.TEST_MAIL_TEMPLATE), getTranslation(Messages.TEST_MAIL_SUBJECT))
    }

    private fun sendMessage(mails: List<String>, body: String, subject: String) {
        for (mailAddress in mails) {
            if (mailAddress.isNotBlank()) {
                try {
                    val message = MimeMessageHelper(mailSender.createMimeMessage(), true)
                    message.setTo(mailAddress)
                    sender?.takeIf { it.isNotBlank() }?.let { message.setFrom(it) }
                    message.setSubject(subject)
                    message.setText(body, true)
                    mailSender.send(message.mimeMessage)
                } catch (e: MessagingException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun getTranslation(messageId: String, vararg params: Any): String {
        //TODO use user locale
        return i18nProvider.getTranslation(messageId, Locale.ENGLISH, *params)
    }

    private fun getBugUrl(bug: Bug): String {
        return baseUrl?.ensureTrailing("/") + routeConfiguration.getUrl(
            ReportTab::class.java, RouteParameters(
                mapOf(
                    PARAM_APP to bug.appId.toString(),
                    PARAM_BUG to bug.id.toString()
                )
            )
        )
    }

}
