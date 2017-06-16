package com.faendir.acra.mongod.model;

import java.util.Date;

/**
 * @author Lukas
 * @since 16.06.2017
 */
public class ReportInfo implements AppScoped {
    private final Date date;
    private final String id;
    private final String app;
    private final String stacktrace;
    private final int versionCode;
    private final String versionName;
    private final String androidVersion;
    private final String phoneModel;

    public ReportInfo(Report report) {
        this.date = report.getDate();
        this.id = report.getId();
        this.app = report.getApp();
        this.stacktrace = report.getStacktrace();
        this.versionCode = report.getVersionCode();
        this.versionName = report.getVersionName();
        this.androidVersion = report.getAndroidVersion();
        this.phoneModel = report.getPhoneModel();
    }

    public Date getDate() {
        return date;
    }

    public String getId() {
        return id;
    }

    @Override
    public String getApp() {
        return app;
    }

    public String getStacktrace() {
        return stacktrace;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public String getAndroidVersion() {
        return androidVersion;
    }

    public String getPhoneModel() {
        return phoneModel;
    }
}
