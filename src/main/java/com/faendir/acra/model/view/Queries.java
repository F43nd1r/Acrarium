package com.faendir.acra.model.view;

import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.lang.NonNull;

import javax.persistence.EntityManager;

import static com.faendir.acra.model.QApp.app;
import static com.faendir.acra.model.QBug.bug;
import static com.faendir.acra.model.QReport.report;

/**
 * @author lukas
 * @since 30.05.18
 */
public abstract class Queries {
    private static final JPAQuery<VBug> V_BUG = new JPAQuery<>().from(bug)
            .join(report)
            .on(report.bug.eq(bug))
            .select(new QVBug(bug, report.date.max(), report.count()))
            .groupBy(bug);
    private static final JPAQuery<VApp> V_APP = new JPAQuery<>().from(app)
            .join(bug)
            .on(bug.app.eq(app))
            .join(report)
            .on(report.bug.eq(bug))
            .select(new QVApp(app, bug.countDistinct(), report.count()))
            .groupBy(app);

    public static JPAQuery<VBug> selectVBug(@NonNull EntityManager entityManager) {
        return V_BUG.clone(entityManager);
    }

    public static JPAQuery<VApp> selectVApp(@NonNull EntityManager entityManager) {
        return V_APP.clone(entityManager);
    }
}
