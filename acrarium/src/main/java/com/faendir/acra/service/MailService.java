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

package com.faendir.acra.service;

import com.faendir.acra.i18n.Messages;
import com.faendir.acra.model.App;
import com.faendir.acra.model.Bug;
import com.faendir.acra.model.MailSettings;
import com.faendir.acra.model.QBug;
import com.faendir.acra.model.Report;
import com.faendir.acra.model.Stacktrace;
import com.faendir.acra.model.User;
import com.faendir.acra.ui.view.bug.tabs.ReportTab;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import com.vaadin.flow.i18n.I18NProvider;
import com.vaadin.flow.router.RouteConfiguration;
import com.vaadin.flow.server.RouteRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.lang.NonNull;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.persistence.EntityManager;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static com.faendir.acra.model.QBug.bug;
import static com.faendir.acra.model.QMailSettings.mailSettings;
import static com.faendir.acra.model.QReport.report;
import static com.faendir.acra.model.QStacktrace.stacktrace1;

/**
 * @author lukas
 * @since 07.12.18
 */
@Service
@EnableScheduling
@EnableAsync
@ConditionalOnProperty(prefix = "spring.mail", name = "host")
public class MailService {
    private final EntityManager entityManager;
    @NonNull
    private final I18NProvider i18nProvider;
    @NonNull
    private final JavaMailSender mailSender;
    @NonNull
    private final RouteConfiguration routeConfiguration;

    public MailService(@NonNull EntityManager entityManager, @NonNull I18NProvider i18nProvider, @NonNull JavaMailSender mailSender, @NonNull RouteConfiguration routeConfiguration) {
        this.entityManager = entityManager;
        this.i18nProvider = i18nProvider;
        this.mailSender = mailSender;
        this.routeConfiguration = routeConfiguration;
    }

    @Transactional
    @EventListener
    @Async
    public void onNewReport(NewReportEvent event) {
        Report r = event.getReport();
        Stacktrace stacktrace = r.getStacktrace();
        Bug bug = stacktrace.getBug();
        App app = bug.getApp();
        List<MailSettings> settings = new JPAQuery<>(entityManager).select(mailSettings).from(mailSettings).where(mailSettings.app.eq(app).and(mailSettings.user.mail.isNotNull())).fetch();
        List<User> newBugReceiver = getUserBy(settings, MailSettings::getNewBug);
        List<User> regressionReceiver = getUserBy(settings, MailSettings::getRegression);
        List<User> spikeReceiver = getUserBy(settings, MailSettings::getSpike);
        if (!newBugReceiver.isEmpty() && new JPAQuery<>(entityManager).from(report).where(report.stacktrace.bug.eq(bug)).limit(2).select(report.count()).fetchCount() == 1) {
            sendMessage(newBugReceiver, getTranslation(Messages.NEW_BUG_MAIL_TEMPLATE, routeConfiguration.getUrl(ReportTab.class, bug.getId()), bug.getTitle(), r.getBrand(), r.getPhoneModel(), r.getAndroidVersion(), app.getName(), stacktrace.getVersion().getName()), getTranslation(Messages.NEW_BUG_MAIL_SUBJECT, app.getName()));
        } else if (!regressionReceiver.isEmpty() && bug.getSolvedVersion() != null && bug.getSolvedVersion().getCode() <= stacktrace.getVersion().getCode()) {
            sendMessage(regressionReceiver, getTranslation(Messages.REGRESSION_MAIL_TEMPLATE, routeConfiguration.getUrl(ReportTab.class, bug.getId()), bug.getTitle(), r.getBrand(), bug.getSolvedVersion().getName(), r.getPhoneModel(), r.getAndroidVersion(), app.getName(), stacktrace.getVersion().getName()), getTranslation(Messages.REGRESSION_MAIL_SUBJECT, app.getName()));
            bug.setSolvedVersion(null);
            entityManager.merge(bug);
        } else if(!spikeReceiver.isEmpty()){
            long reportCount = fetchReportCountOnDay(0);
            double averageCount = LongStream.range(1, 3).map(this::fetchReportCountOnDay).average().orElse(Double.MAX_VALUE);
            if (reportCount > 1.2 * averageCount && reportCount - 1 <= 1.2 * averageCount) {
                sendMessage(regressionReceiver, getTranslation(Messages.SPIKE_MAIL_TEMPLATE, routeConfiguration.getUrl(ReportTab.class, bug.getId()), bug.getTitle(), stacktrace.getVersion().getName(), reportCount), getTranslation(Messages.SPIKE_MAIL_SUBJECT, app.getName()));
            }
        }
    }

    private List<User> getUserBy(List<MailSettings> list, Predicate<MailSettings> predicate) {
        return list.stream().filter(predicate).map(MailSettings::getUser).collect(Collectors.toList());
    }

    private long fetchReportCountOnDay(long subtractDays) {
        ZonedDateTime today = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS);
        return new JPAQuery<>(entityManager).from(report).where(report.stacktrace.bug.eq(bug).and(report.date.between(today.minus(subtractDays, ChronoUnit.DAYS), today.minus(subtractDays - 1, ChronoUnit.DAYS)))).select(report.count()).fetchCount();
    }

    @Scheduled(cron = "0 0 0 * * SUN")
    public void weeklyReport() {
        Map<App, List<MailSettings>> settings = new JPAQuery<>(entityManager).select(mailSettings).from(mailSettings).where(mailSettings.summary.isTrue()).fetch()
                .stream()
                .collect(Collectors.groupingBy(MailSettings::getApp, Collectors.toList()));
        RouteConfiguration configuration = RouteConfiguration.forApplicationScope();
        for (Map.Entry<App, List<MailSettings>> entry : settings.entrySet()) {
            List<Tuple> tuples = new JPAQuery<>(entityManager).from(bug).join(stacktrace1).on(bug.eq(stacktrace1.bug)).join(report).on(stacktrace1.eq(report.stacktrace)).where(bug.app.eq(entry.getKey()).and(report.date.after(ZonedDateTime.now().minus(1, ChronoUnit.WEEKS))))
                    .groupBy(bug)
                    .select(bug, report.count(), report.installationId.countDistinct())
                    .fetch();
            String body = tuples.stream().map(tuple -> {
                Bug bug = tuple.get(QBug.bug);
                return getTranslation(Messages.WEEKLY_MAIL_BUG_TEMPLATE, configuration.getUrl(ReportTab.class, bug.getId()), bug.getTitle(), tuple.get(report.count()), tuple.get(report.installationId.countDistinct()));
            }).collect(Collectors.joining("\n"));
            if (body.isEmpty()) {
                body = getTranslation(Messages.WEEKLY_MAIL_NO_REPORTS);
            }
            sendMessage(entry.getValue().stream().map(MailSettings::getUser).collect(Collectors.toList()), body, getTranslation(Messages.WEEKLY_MAIL_SUBJECT, entry.getKey().getName()));
        }
    }

    private void sendMessage(@NonNull List<User> users, @NonNull String body, @NonNull String subject) {
        try {
            MimeMessage template = mailSender.createMimeMessage();
            template.setContent(body, "text/html");
            template.setSubject(subject);
            for (User user : users) {
                MimeMessage message = new MimeMessage(template);
                message.setRecipients(Message.RecipientType.TO, user.getMail());
                mailSender.send(message);
            }
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private String getTranslation(String messageId, Object... params) {
        //TODO use user locale
        return i18nProvider.getTranslation(messageId, Locale.ENGLISH, params);
    }
}
