package com.faendir.acra.model.view;

import com.faendir.acra.model.Bug;
import com.querydsl.core.annotations.QueryProjection;
import com.querydsl.jpa.impl.JPAQuery;

import java.util.Date;

/**
 * @author lukas
 * @since 30.05.18
 */
public class VBug {
    private final Bug bug;
    private final Date lastReport;
    private final long reportCount;

    @QueryProjection
    public VBug(Bug bug, Date lastReport, long reportCount) {
        this.bug = bug;
        this.lastReport = lastReport;
        this.reportCount = reportCount;
    }

    public Bug getBug() {
        return bug;
    }

    public long getReportCount() {
        return reportCount;
    }

    public Date getLastReport() {
        return lastReport;
    }
}
