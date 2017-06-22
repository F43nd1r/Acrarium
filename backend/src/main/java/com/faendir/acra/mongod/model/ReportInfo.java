package com.faendir.acra.mongod.model;

import org.jetbrains.annotations.NotNull;

import java.util.Date;

/**
 * @author Lukas
 * @since 16.06.2017
 */
public class ReportInfo implements AppScoped {
    @NotNull private final Date date;
    @NotNull private final String id;
    @NotNull private final String app;
    @NotNull private final String stacktrace;
    private final int versionCode;
    @NotNull private final String androidVersion;
    @NotNull private final String phoneModel;

    public ReportInfo(@NotNull Report report) {
        this.date = report.getDate();
        this.id = report.getId();
        this.app = report.getApp();
        this.stacktrace = report.getStacktrace();
        this.versionCode = report.getVersionCode();
        this.androidVersion = report.getAndroidVersion();
        this.phoneModel = report.getPhoneModel();
    }

    @NotNull
    public Date getDate() {
        return date;
    }

    @NotNull
    public String getId() {
        return id;
    }

    @NotNull
    @Override
    public String getApp() {
        return app;
    }

    @NotNull
    public String getStacktrace() {
        return stacktrace;
    }

    public int getVersionCode() {
        return versionCode;
    }

    @NotNull
    public String getAndroidVersion() {
        return androidVersion;
    }

    @NotNull
    public String getPhoneModel() {
        return phoneModel;
    }
}
