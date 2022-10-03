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
    private val V_APP = JPAQuery<Any>().from(QApp.app)
        .leftJoin(QBug.bug)
        .on(QBug.bug.app.eq(QApp.app))
        .leftJoin(QStacktrace.stacktrace1)
        .on(QStacktrace.stacktrace1.bug.eq(QBug.bug))
        .leftJoin(QReport.report)
        .on(QReport.report.stacktrace.eq(QStacktrace.stacktrace1))
        .select(QVApp(QApp.app, QBug.bug.countDistinct(), QReport.report.count()))
        .groupBy(QApp.app)
    private val V_INSTALLATION = JPAQuery<Any>().from(QReport.report)
        .select(QVInstallation(QReport.report.installationId, QReport.report.count(), QReport.report.date.max()))
        .groupBy(QReport.report.installationId)

    fun selectVApp(entityManager: EntityManager): JPAQuery<VApp> {
        return V_APP.clone(entityManager)
    }

    fun selectVInstallation(entityManager: EntityManager) = V_INSTALLATION.clone(entityManager)
}