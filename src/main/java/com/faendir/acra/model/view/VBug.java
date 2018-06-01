package com.faendir.acra.model.view;

import com.faendir.acra.model.Bug;
import com.querydsl.core.annotations.QueryProjection;

import java.time.LocalDateTime;

/**
 * @author lukas
 * @since 30.05.18
 */
public class VBug {
    private final Bug bug;
    private final LocalDateTime lastReport;
    private final long reportCount;

    @QueryProjection
    public VBug(Bug bug, LocalDateTime lastReport, long reportCount) {
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

    public LocalDateTime getLastReport() {
        return lastReport;
    }
}
