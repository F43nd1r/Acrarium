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

package com.faendir.acra.model.view;

import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.lang.NonNull;

import javax.persistence.EntityManager;

import static com.faendir.acra.model.QApp.app;
import static com.faendir.acra.model.QBug.bug;
import static com.faendir.acra.model.QReport.report;
import static com.faendir.acra.model.QStacktrace.stacktrace1;

/**
 * @author lukas
 * @since 30.05.18
 */
public abstract class Queries {
    private static final JPAQuery<VBug> V_BUG = new JPAQuery<>().from(bug)
            .leftJoin(stacktrace1)
            .on(stacktrace1.bug.eq(bug))
            .leftJoin(report)
            .on(report.stacktrace.eq(stacktrace1))
            .select(new QVBug(bug, report.date.max(), report.count(), stacktrace1.versionCode.max(), report.installationId.countDistinct()))
            .groupBy(bug);
    private static final JPAQuery<VApp> V_APP = new JPAQuery<>().from(app)
            .leftJoin(bug)
            .on(bug.app.eq(app))
            .leftJoin(stacktrace1)
            .on(stacktrace1.bug.eq(bug))
            .leftJoin(report)
            .on(report.stacktrace.eq(stacktrace1))
            .select(new QVApp(app, bug.countDistinct(), report.count()))
            .groupBy(app);

    public static JPAQuery<VBug> selectVBug(@NonNull EntityManager entityManager) {
        return V_BUG.clone(entityManager);
    }

    public static JPAQuery<VApp> selectVApp(@NonNull EntityManager entityManager) {
        return V_APP.clone(entityManager);
    }
}
