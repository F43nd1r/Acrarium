package com.faendir.acra.model.view;

import com.faendir.acra.model.App;
import com.querydsl.core.annotations.QueryProjection;

/**
 * @author lukas
 * @since 29.05.18
 */
public class VApp {
    private final App app;
    private final long reportCount;

    @QueryProjection
    public VApp(App app, long reportCount) {
        this.app = app;
        this.reportCount = reportCount;
    }

    public App getApp() {
        return app;
    }

    public long getReportCount() {
        return reportCount;
    }
}
