package com.faendir.acra.model.view;

import com.faendir.acra.model.App;
import com.querydsl.core.annotations.QueryProjection;
import org.springframework.lang.NonNull;

/**
 * @author lukas
 * @since 29.05.18
 */
public class VApp {
    private final App app;
    private final long reportCount;
    private final long bugCount;

    @QueryProjection
    public VApp(App app, long bugCount, long reportCount) {
        this.app = app;
        this.reportCount = reportCount;
        this.bugCount = bugCount;
    }

    public App getApp() {
        return app;
    }

    public long getReportCount() {
        return reportCount;
    }

    public long getBugCount() {
        return bugCount;
    }

    public int getId() {
        return app.getId();
    }

    @NonNull
    public String getName() {
        return app.getName();
    }
}
