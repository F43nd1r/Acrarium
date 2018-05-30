package com.faendir.acra.model.view;

import com.faendir.acra.model.QApp;
import com.faendir.acra.model.QBug;
import com.faendir.acra.model.QReport;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.lang.NonNull;

import javax.persistence.EntityManager;

/**
 * @author lukas
 * @since 30.05.18
 */
public abstract class Queries {
    private static final QApp APP = QApp.app;
    private static final QReport REPORT = QReport.report;
    private static final QBug BUG = QBug.bug;
    private static final JPAQuery<VBug> V_BUG = new JPAQuery<>().from(BUG)
            .join(REPORT)
            .on(REPORT.bug.eq(BUG))
            .select(new QVBug(BUG, REPORT.date.max(), REPORT.count()))
            .groupBy(BUG);
    private static final JPAQuery<VApp> V_APP = new JPAQuery<>().from(APP)
            .join(BUG)
            .on(BUG.app.eq(APP))
            .join(REPORT)
            .on(REPORT.bug.eq(BUG))
            .select(new QVApp(APP, REPORT.count()))
            .groupBy(APP);

    public static JPAQuery<VBug> selectVBug(@NonNull EntityManager entityManager) {
        return V_BUG.clone(entityManager);
    }

    public static JPAQuery<VApp> selectVApp(@NonNull EntityManager entityManager) {
        return V_APP.clone(entityManager);
    }
}
