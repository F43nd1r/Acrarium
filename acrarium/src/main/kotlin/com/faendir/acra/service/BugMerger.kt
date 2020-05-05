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

import com.faendir.acra.model.Bug
import com.faendir.acra.model.QApp
import com.faendir.acra.model.QBug
import com.faendir.acra.model.QReport
import com.faendir.acra.model.QStacktrace
import com.faendir.acra.model.QStacktrace.stacktrace1
import com.faendir.acra.model.QStacktraceMatch
import com.faendir.acra.model.StacktraceMatch
import com.querydsl.jpa.JPAExpressions
import com.querydsl.jpa.impl.JPADeleteClause
import com.querydsl.jpa.impl.JPAQuery
import com.querydsl.jpa.impl.JPAUpdateClause
import me.xdrop.fuzzywuzzy.FuzzySearch
import org.springframework.context.event.EventListener
import org.springframework.lang.NonNull
import org.springframework.scheduling.annotation.Async
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import javax.persistence.EntityManager
import javax.validation.constraints.Size

/**
 * @author lukas
 * @since 28.07.18
 */
@EnableAsync
@Service
class BugMerger(private val entityManager: EntityManager) {
    @EventListener
    @Transactional
    fun checkAutoMerge(event: NewReportEvent) {
        val stacktrace = event.report.stacktrace
        JPAQuery<Any>(entityManager).from(stacktrace1).where(stacktrace1.eq(stacktrace)).join(stacktrace1.bug, QBug.bug)
                .join(QBug.bug.app).fetchJoin().select(QBug.bug).fetchOne()?.let { b ->
                    var bug = b
                    JPAQuery<Any>(entityManager).from(stacktrace1).where(stacktrace1.ne(stacktrace).and(stacktrace1.bug.app.eq(
                            JPAExpressions.select(QApp.app).from(stacktrace1).where(stacktrace1.eq(stacktrace)).join(stacktrace1.bug, QBug.bug).join(QBug.bug.app, QApp.app)))
                            .and(stacktrace1.notIn(JPAExpressions.select(QStacktraceMatch.stacktraceMatch.right).from(QStacktraceMatch.stacktraceMatch)
                                    .where(QStacktraceMatch.stacktraceMatch.left.eq(stacktrace))))
                            .and(stacktrace1.notIn(JPAExpressions.select(QStacktraceMatch.stacktraceMatch.left).from(QStacktraceMatch.stacktraceMatch)
                                    .where(QStacktraceMatch.stacktraceMatch.right.eq(stacktrace)))))
                            .join(stacktrace1.bug).fetchJoin().select(stacktrace1).iterate().use {
                                it.asSequence().forEach { s ->
                                    val match = entityManager.merge(StacktraceMatch(s, stacktrace, FuzzySearch.ratio(s.stacktrace, stacktrace.stacktrace)))
                                    if (s.bug != bug && match.score >= bug.app.configuration.minScore) {
                                        entityManager.flush()
                                        bug = mergeBugs(listOf(s.bug, bug), s.bug.title)
                                    }
                                }
                            }
                }
    }

    @EventListener
    @Async
    @Transactional
    fun changeConfiguration(event: ConfigurationUpdateEvent) {
        val app = event.app
        val stacktrace2 = QStacktrace("stacktrace2")
        val bug2 = QBug("bug2")
        JPAQuery<Any>(entityManager).from(stacktrace1).join(stacktrace1.bug, QBug.bug).join(stacktrace2).on(stacktrace1.id.lt(stacktrace2.id)).join(stacktrace2.bug, bug2)
                .where(QBug.bug.app.eq(app).and(bug2.app.eq(app)).and(
                        JPAExpressions.selectFrom(QStacktraceMatch.stacktraceMatch).where(QStacktraceMatch.stacktraceMatch.left.eq(stacktrace1)
                                .and(QStacktraceMatch.stacktraceMatch.right.eq(stacktrace2)).or(QStacktraceMatch.stacktraceMatch.left.eq(stacktrace2)
                                        .and(QStacktraceMatch.stacktraceMatch.right.eq(stacktrace1)))).notExists()))
                .select(stacktrace1, stacktrace2).iterate().use {
                    it.asSequence().forEach { tuple ->
                        val left = tuple.get(stacktrace1)
                        val right = tuple.get(stacktrace2)
                        assert(left != null)
                        assert(right != null)
                        entityManager.persist(StacktraceMatch(left!!, right!!, FuzzySearch.ratio(left.stacktrace, right.stacktrace)))
                    }
                }
        JPAQuery<Any>(entityManager).from(QStacktraceMatch.stacktraceMatch)
                .join(QStacktraceMatch.stacktraceMatch.left, stacktrace1)
                .fetchJoin()
                .join(stacktrace1.bug, QBug.bug)
                .fetchJoin()
                .join(QStacktraceMatch.stacktraceMatch.right, stacktrace2)
                .fetchJoin()
                .join(stacktrace2.bug, bug2)
                .fetchJoin()
                .where(QBug.bug.app.eq(app))
                .select(QStacktraceMatch.stacktraceMatch)
                .iterate().use {
                    it.asSequence().forEach { match ->
                        if (match.score >= app.configuration.minScore && match.left.bug != match.right.bug) {
                            JPAUpdateClause(entityManager, stacktrace1).set(stacktrace1.bug, match.left.bug).where(stacktrace1.bug.eq(match.right.bug)).execute()
                        }
                    }
                }
        entityManager.flush()
        deleteOrphanBugs()
    }

    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasPermission(#bugs[0].app, T(com.faendir.acra.model.Permission\$Level).EDIT)")
    @Transactional
    fun mergeBugs(bugs: @Size(min = 2) Collection<Bug>, title: String): Bug {
        val list = ArrayList(bugs)
        var bug = list.removeAt(0)
        bug.title = title
        bug = entityManager.merge(bug)
        JPAUpdateClause(entityManager, stacktrace1).set(stacktrace1.bug, bug).where(stacktrace1.bug.`in`(list)).execute()
        JPADeleteClause(entityManager, QBug.bug).where(QBug.bug.`in`(list)).execute()
        entityManager.flush()
        return bug
    }

    @EventListener
    @Transactional
    fun onReportsDeleted(event: ReportsDeleteEvent?) {
        deleteOrphanStacktraces()
        deleteOrphanBugs()
    }

    @Transactional
    fun deleteOrphanStacktraces() {
        JPADeleteClause(entityManager, stacktrace1).where(stacktrace1.notIn(JPAExpressions.select(QReport.report.stacktrace).from(QReport.report).distinct())).execute()
    }

    @Transactional
    fun deleteOrphanBugs() {
        JPADeleteClause(entityManager, QBug.bug).where(QBug.bug.notIn(JPAExpressions.select(stacktrace1.bug).from(stacktrace1).distinct())).execute()
    }

}