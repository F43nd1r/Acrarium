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
import com.faendir.acra.model.QBug;
import com.faendir.acra.model.QStacktrace;
import com.faendir.acra.model.Stacktrace;
import com.faendir.acra.model.StacktraceMatch;
import com.mysema.commons.lang.CloseableIterator;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPADeleteClause;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAUpdateClause;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.faendir.acra.model.QApp.app;
import static com.faendir.acra.model.QBug.bug;
import static com.faendir.acra.model.QStacktrace.stacktrace1;
import static com.faendir.acra.model.QStacktraceMatch.stacktraceMatch;

/**
 * @author lukas
 * @since 28.07.18
 */
@EnableAsync
@Service
public class BugMerger {
    @NonNull
    private final EntityManager entityManager;

    @Autowired
    public BugMerger(@NonNull EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @EventListener
    @Transactional
    public void checkAutoMerge(@NonNull NewReportEvent event) {
        Stacktrace stacktrace = event.getReport().getStacktrace();
        Bug b = new JPAQuery<>(entityManager).from(stacktrace1).where(stacktrace1.eq(stacktrace)).join(stacktrace1.bug, bug).join(bug.app).fetchJoin().select(bug).fetchOne();
        if (b != null) {
            CloseableIterator<Stacktrace> iterator = new JPAQuery<>(entityManager).from(stacktrace1)
                    .where(stacktrace1.ne(stacktrace)
                            .and(stacktrace1.bug.app.eq(JPAExpressions.select(app)
                                    .from(stacktrace1)
                                    .where(stacktrace1.eq(stacktrace))
                                    .join(stacktrace1.bug, bug)
                                    .join(bug.app, app)))
                            .and(stacktrace1.notIn(JPAExpressions.select(stacktraceMatch.right).from(stacktraceMatch).where(stacktraceMatch.left.eq(stacktrace))))
                            .and(stacktrace1.notIn(JPAExpressions.select(stacktraceMatch.left).from(stacktraceMatch).where(stacktraceMatch.right.eq(stacktrace)))))
                    .join(stacktrace1.bug)
                    .fetchJoin()
                    .select(stacktrace1)
                    .iterate();
            while (iterator.hasNext()) {
                Stacktrace s = iterator.next();
                StacktraceMatch match = entityManager.merge(new StacktraceMatch(s, stacktrace, FuzzySearch.ratio(s.getStacktrace(), stacktrace.getStacktrace())));
                if (match.getScore() >= b.getApp().getConfiguration().getMinScore()) {
                    b = mergeBugs(Arrays.asList(s.getBug(), b), s.getBug().getTitle());
                }
            }
            iterator.close();
        }
    }

    @EventListener
    @Async
    @Transactional
    public void changeConfiguration(@NonNull ConfigurationUpdateEvent event) {
        App app = event.getApp();
        QStacktrace stacktrace2 = new QStacktrace("stacktrace2");
        QBug bug2 = new QBug("bug2");
        CloseableIterator<Tuple> traceIterator = new JPAQuery<>(entityManager).from(stacktrace1)
                .join(stacktrace1.bug, bug)
                .join(stacktrace2).on(stacktrace1.id.lt(stacktrace2.id))
                .join(stacktrace2.bug, bug2)
                .where(bug.app.eq(app)
                        .and(bug2.app.eq(app))
                        .and(JPAExpressions.selectFrom(stacktraceMatch)
                                .where(stacktraceMatch.left.eq(stacktrace1)
                                        .and(stacktraceMatch.right.eq(stacktrace2))
                                        .or(stacktraceMatch.left.eq(stacktrace2).and(stacktraceMatch.right.eq(stacktrace1))))
                                .notExists()))
                .select(stacktrace1, stacktrace2)
                .iterate();
        while (traceIterator.hasNext()) {
            Tuple tuple = traceIterator.next();
            Stacktrace left = tuple.get(stacktrace1);
            Stacktrace right = tuple.get(stacktrace2);
            assert left != null;
            assert right != null;
            entityManager.persist(new StacktraceMatch(left, right, FuzzySearch.ratio(left.getStacktrace(), right.getStacktrace())));
        }
        traceIterator.close();
        CloseableIterator<StacktraceMatch> matchIterator = new JPAQuery<>(entityManager).from(stacktraceMatch)
                .join(stacktraceMatch.left, stacktrace1)
                .fetchJoin()
                .join(stacktrace1.bug, bug)
                .fetchJoin()
                .join(stacktraceMatch.right, stacktrace2)
                .fetchJoin()
                .join(stacktrace2.bug, bug2)
                .fetchJoin()
                .where(bug.app.eq(app))
                .select(stacktraceMatch)
                .iterate();
        while (matchIterator.hasNext()) {
            StacktraceMatch match = matchIterator.next();
            if (match.getScore() >= app.getConfiguration().getMinScore() && !match.getLeft().getBug().equals(match.getRight().getBug())) {
                new JPAUpdateClause(entityManager, stacktrace1).set(stacktrace1.bug, match.getLeft().getBug()).where(stacktrace1.bug.eq(match.getRight().getBug())).execute();
            }
        }
        matchIterator.close();
        entityManager.flush();
        deleteOrphanBugs();
    }

    @PreAuthorize("T(com.faendir.acra.security.SecurityUtils).hasPermission(#bugs[0].app, T(com.faendir.acra.model.Permission$Level).EDIT)")
    @Transactional
    public Bug mergeBugs(@NonNull @Size(min = 2) Collection<Bug> bugs, @NonNull String title) {
        List<Bug> list = new ArrayList<>(bugs);
        Bug bug = list.remove(0);
        bug.setTitle(title);
        bug = entityManager.merge(bug);
        new JPAUpdateClause(entityManager, stacktrace1).set(stacktrace1.bug, bug).where(stacktrace1.bug.in(list)).execute();
        new JPADeleteClause(entityManager, QBug.bug).where(QBug.bug.in(list)).execute();
        entityManager.flush();
        return bug;
    }

    @EventListener
    @Transactional
    public void onReportsDeleted(ReportsDeleteEvent event) {
        deleteOrphanBugs();
    }

    @Transactional
    public void deleteOrphanBugs() {
        new JPADeleteClause(entityManager, bug).where(bug.notIn(JPAExpressions.select(stacktrace1.bug).from(stacktrace1).distinct())).execute();
    }
}
