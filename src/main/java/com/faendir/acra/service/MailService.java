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

import com.faendir.acra.model.App;
import com.faendir.acra.model.Bug;
import com.faendir.acra.model.MailSettings;
import com.faendir.acra.model.Report;
import com.faendir.acra.model.Stacktrace;
import com.faendir.acra.model.Version;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.context.event.EventListener;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.faendir.acra.model.QMailSettings.mailSettings;
import static com.faendir.acra.model.QReport.report;

/**
 * @author lukas
 * @since 07.12.18
 */
@Service
@EnableScheduling
@EnableAsync
public class MailService {
    private final EntityManager entityManager;

    public MailService(@NonNull EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @EventListener
    @Async
    public void onNewReport(NewReportEvent event) {

    }

    @Scheduled(cron = "0 0 * * * *")
    public void checkHourly() {
        check(MailSettings.SendMode.HOURLY);
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void checkDaily() {
        check(MailSettings.SendMode.DAILY);
    }

    @Scheduled(cron = "0 0 0 * * SUN")
    public void checkWeekly() {
        check(MailSettings.SendMode.WEEKLY);
    }

    private void check(MailSettings.SendMode sendMode) {
        List<MailSettings> settings = new JPAQuery<>(entityManager).select(mailSettings).from(mailSettings).where(mailSettings.sendMode.eq(sendMode)).fetch();
        Map<App, List<MailSettings>> appMap = settings.stream().collect(Collectors.groupingBy(MailSettings::getApp));
        for (Map.Entry<App, List<MailSettings>> appEntry : appMap.entrySet()) {
            List<Report> reports = new JPAQuery<>(entityManager).select(report).where(report.date.after(ZonedDateTime.now().minus(1, sendMode.getUnit())).and(report.stacktrace.bug.app.eq(appEntry.getKey()))).fetch();
            if(!reports.isEmpty()) {
                Map<Bug, List<Report>> bugMap = reports.stream().collect(Collectors.groupingBy(r -> r.getStacktrace().getBug()));
                for (Map.Entry<Bug, List<Report>> bugEntry : bugMap.entrySet()) {
                    boolean newBug = new JPAQuery<>().select(report).where(report.stacktrace.bug.eq(bugEntry.getKey()).and(report.notIn(bugEntry.getValue()))).fetchFirst() == null;
                    if (!newBug && bugEntry.getKey().getSolvedVersion() != null) {
                        int maxNewVersion = reports.stream().map(Report::getStacktrace).map(Stacktrace::getVersion).mapToInt(Version::getCode).max().orElseThrow(IllegalStateException::new);
                        boolean regression = new JPAQuery<>().select(report).where(report.stacktrace.bug.eq(bugEntry.getKey()).and(report.notIn(bugEntry.getValue())).and(report.stacktrace.version.code.goe(maxNewVersion))).fetchFirst() == null;
                    }
                }
            }
        }
    }
}
