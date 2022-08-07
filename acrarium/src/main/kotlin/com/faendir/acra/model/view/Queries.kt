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
package com.faendir.acra.model.view

import com.faendir.acra.model.*
import com.querydsl.core.types.Expression
import com.querydsl.core.types.Projections
import com.querydsl.core.types.dsl.Expressions
import com.querydsl.core.types.dsl.StringTemplate
import com.querydsl.jpa.impl.JPAQuery
import javax.persistence.EntityManager

/**
 * @author lukas
 * @since 30.05.18
 */
object Queries {
    private val V_BUG = JPAQuery<Any>().from(QBug.bug)
        .innerJoin(QStacktrace.stacktrace1)
        .on(QStacktrace.stacktrace1.bug.eq(QBug.bug))
        .leftJoin(QReport.report)
        .on(QReport.report.stacktrace.eq(QStacktrace.stacktrace1))
        .leftJoin(QBug.bug.solvedVersion).fetchJoin()
        .select(
            QVBug(
                QBug.bug,
                QReport.report.date.max(),
                QReport.report.count(),
                QStacktrace.stacktrace1.version.code.max(),
                QReport.report.installationId.countDistinct()
            )
        )
        .groupBy(QBug.bug)
    private val V_APP = JPAQuery<Any>().from(QApp.app)
        .leftJoin(QBug.bug)
        .on(QBug.bug.app.eq(QApp.app))
        .leftJoin(QStacktrace.stacktrace1)
        .on(QStacktrace.stacktrace1.bug.eq(QBug.bug))
        .leftJoin(QReport.report)
        .on(QReport.report.stacktrace.eq(QStacktrace.stacktrace1))
        .select(QVApp(QApp.app, QBug.bug.countDistinct(), QReport.report.count()))
        .groupBy(QApp.app)
    private val V_REPORT = JPAQuery<Any>().from(QReport.report)
        .leftJoin(QStacktrace.stacktrace1).on(QStacktrace.stacktrace1.eq(QReport.report.stacktrace))
        .join(QStacktrace.stacktrace1.bug, QBug.bug).fetchJoin()
        .join(QBug.bug.app, QApp.app).fetchJoin()
        .leftJoin(QDevice.device1).on(QReport.report.phoneModel.eq(QDevice.device1.model).and(QReport.report.device.eq(QDevice.device1.device)))
    private val V_INSTALLATION = JPAQuery<Any>().from(QReport.report)
        .select(QVInstallation(QReport.report.installationId, QReport.report.count(), QReport.report.date.max()))
        .groupBy(QReport.report.installationId)


    fun selectVBug(entityManager: EntityManager): JPAQuery<VBug> {
        return V_BUG.clone(entityManager)
    }

    fun selectVApp(entityManager: EntityManager): JPAQuery<VApp> {
        return V_APP.clone(entityManager)
    }

    @Suppress("UNCHECKED_CAST")
    fun selectVReport(entityManager: EntityManager, app: App): JPAQuery<VReport> {
        return V_REPORT.clone(entityManager).select(
            QVReport(
                QStacktrace.stacktrace1,
                QReport.report.id,
                QReport.report.date,
                QReport.report.androidVersion,
                QReport.report.phoneModel,
                QReport.report.installationId,
                QReport.report.isSilent,
                QDevice.device1.marketingName.coalesce(QReport.report.phoneModel),
                Projections.list(*app.configuration.customReportColumns.map { customReportColumnExpression(it) }.toTypedArray()) as Expression<out List<String>>
            )
        )
    }

    fun selectVInstallation(entityManager: EntityManager) = V_INSTALLATION.clone(entityManager)

    fun customReportColumnExpression(customColumn: String): StringTemplate = Expressions.stringTemplate(
        "COALESCE(JSON_UNQUOTE(JSON_EXTRACT(content, {0})), '')",
        "$.$customColumn"
    )
}