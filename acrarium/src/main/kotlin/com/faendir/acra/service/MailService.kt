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
package com.faendir.acra.service

import com.faendir.acra.i18n.Messages
import com.faendir.acra.model.Bug
import com.faendir.acra.model.MailSettings
import com.faendir.acra.model.QBug
import com.faendir.acra.model.QMailSettings
import com.faendir.acra.model.QReport
import com.faendir.acra.model.QStacktrace
import com.faendir.acra.model.User
import com.faendir.acra.ui.view.bug.tabs.ReportTab
import com.faendir.acra.util.PARAM_APP
import com.faendir.acra.util.PARAM_BUG
import com.faendir.acra.util.ensureTrailing
import com.querydsl.core.Tuple
import com.querydsl.jpa.impl.JPAQuery
import com.vaadin.flow.i18n.I18NProvider
import com.vaadin.flow.router.RouteConfiguration
import com.vaadin.flow.router.RouteParameters
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.event.EventListener
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.stream.LongStream
import javax.mail.MessagingException
import javax.persistence.EntityManager

/**
 * @author lukas
 * @since 07.12.18
 */
@Service
@EnableScheduling
@EnableAsync
@ConditionalOnProperty(prefix = "spring.mail", name = ["host"])
class MailService(
    private val entityManager: EntityManager,
    private val i18nProvider: I18NProvider,
    private val mailSender: JavaMailSender,
    private val routeConfiguration: RouteConfiguration
) {

    @Value("\${server.context-path}")
    private val baseUrl: String? = null

    @Transactional
    @EventListener
    @Async
    fun onNewReport(event: NewReportEvent) {
        val r = event.report
        val stacktrace = r.stacktrace
        val bug = stacktrace.bug
        val app = bug.app
        val settings = JPAQuery<Any>(entityManager).select(QMailSettings.mailSettings).from(QMailSettings.mailSettings).where(
            QMailSettings.mailSettings.app.eq(app)
                .and(QMailSettings.mailSettings.user.mail.isNotNull)
        ).fetch()
        val newBugReceiver = getUserBy(settings, MailSettings::newBug)
        val regressionReceiver = getUserBy(settings, MailSettings::regression)
        val spikeReceiver = getUserBy(settings, MailSettings::spike)
        if (newBugReceiver.isNotEmpty() && JPAQuery<Any>(entityManager).from(QReport.report).where(QReport.report.stacktrace.bug.eq(bug)).limit(2)
                .select(QReport.report.count()).fetchCount() == 1L
        ) {
            sendMessage(
                newBugReceiver, getTranslation(
                    Messages.NEW_BUG_MAIL_TEMPLATE, getBugUrl(bug), bug.title, r.brand, r.phoneModel, r.androidVersion,
                    app.name, stacktrace.version.name
                ), getTranslation(Messages.NEW_BUG_MAIL_SUBJECT, app.name)
            )
        } else if (regressionReceiver.isNotEmpty() && bug.solvedVersion != null && bug.solvedVersion!!.code <= stacktrace.version.code) {
            sendMessage(
                regressionReceiver, getTranslation(
                    Messages.REGRESSION_MAIL_TEMPLATE, getBugUrl(bug), bug.title, r.brand, bug.solvedVersion!!.name,
                    r.phoneModel, r.androidVersion, app.name, stacktrace.version.name
                ), getTranslation(Messages.REGRESSION_MAIL_SUBJECT, app.name)
            )
            bug.solvedVersion = null
            entityManager.merge(bug)
        } else if (spikeReceiver.isNotEmpty()) {
            val reportCount = fetchReportCountOnDay(bug, 0)
            val averageCount = LongStream.range(1, 3).map { subtractDays: Long -> fetchReportCountOnDay(bug, subtractDays) }.average().orElse(Double.MAX_VALUE)
            if (reportCount > 1.2 * averageCount && reportCount - 1 <= 1.2 * averageCount) {
                sendMessage(
                    regressionReceiver, getTranslation(
                        Messages.SPIKE_MAIL_TEMPLATE, getBugUrl(bug), bug.title, stacktrace.version.name,
                        reportCount
                    ), getTranslation(Messages.SPIKE_MAIL_SUBJECT, app.name)
                )
            }
        }
    }

    private fun getUserBy(list: List<MailSettings>, predicate: (MailSettings) -> Boolean): List<User> = list.filter(predicate).map(MailSettings::user)

    private fun fetchReportCountOnDay(bug: Bug, subtractDays: Long): Long {
        val today = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
        return JPAQuery<Any>(entityManager)
            .from(QReport.report)
            .where(QReport.report.stacktrace.bug.eq(bug).and(QReport.report.date.between(today.minusDays(subtractDays), today.minusDays(subtractDays - 1))))
            .select(QReport.report.count()).fetchCount()
    }

    @Scheduled(cron = "0 0 0 * * SUN")
    fun weeklyReport() {
        val settings =
            JPAQuery<Any>(entityManager).select(QMailSettings.mailSettings).from(QMailSettings.mailSettings).where(QMailSettings.mailSettings.summary.isTrue)
                .fetch()
                .groupBy { it.app }
        for ((key, value) in settings) {
            val tuples =
                JPAQuery<Any>(entityManager).from(QBug.bug).join(QStacktrace.stacktrace1).on(QBug.bug.eq(QStacktrace.stacktrace1.bug)).join(QReport.report)
                    .on(QStacktrace.stacktrace1.eq(QReport.report.stacktrace))
                    .where(QBug.bug.app.eq(key).and(QReport.report.date.after(ZonedDateTime.now().minus(1, ChronoUnit.WEEKS))))
                    .groupBy(QBug.bug)
                    .select(QBug.bug, QReport.report.count(), QReport.report.installationId.countDistinct())
                    .fetch()
            var body = tuples.joinToString("\n") { tuple: Tuple ->
                val bug = tuple.get(QBug.bug)
                getTranslation(
                    Messages.WEEKLY_MAIL_BUG_TEMPLATE, getBugUrl(bug!!), bug.title, tuple.get(QReport.report.count())!!,
                    tuple.get(QReport.report.installationId.countDistinct())!!
                )
            }
            if (body.isEmpty()) {
                body = getTranslation(Messages.WEEKLY_MAIL_NO_REPORTS)
            }
            sendMessage(value.map(MailSettings::user), body, getTranslation(Messages.WEEKLY_MAIL_SUBJECT, key.name))
        }
    }

    private fun sendMessage(users: List<User>, body: String, subject: String) {
        for (user in users) {
            user.mail?.let {
                if (it.isNotBlank()) {
                    try {
                        val message = MimeMessageHelper(mailSender.createMimeMessage(), true)
                        message.setTo(it)
                        message.setSubject(subject)
                        message.setText(body, true)
                        mailSender.send(message.mimeMessage)
                    } catch (e: MessagingException) {
                        e.printStackTrace()
                    }
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
                    PARAM_APP to bug.app.id.toString(),
                    PARAM_BUG to bug.id.toString()
                )
            )
        )
    }

}