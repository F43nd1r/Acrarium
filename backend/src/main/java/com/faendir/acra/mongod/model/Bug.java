package com.faendir.acra.mongod.model;

import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lukas
 * @since 13.05.2017
 */
@Document
public class Bug implements AppScoped {
    private String id;
    @Indexed
    private String app;
    private boolean solved;
    private String stacktrace;
    private int versionCode;
    private List<String> reportIds;

    @PersistenceConstructor
    public Bug() {
        reportIds = new ArrayList<>();
    }

    public Bug(String app, String stacktrace, int versionCode){
        this();
        this.app = app;
        this.stacktrace = stacktrace;
        this.versionCode = versionCode;
    }

    public boolean isSolved() {
        return solved;
    }

    public void setSolved(boolean solved) {
        this.solved = solved;
    }

    public String getStacktrace() {
        return stacktrace;
    }

    public int getVersionCode() {
        return versionCode;
    }

    @Override
    public String getApp() {
        return app;
    }

    public List<String> getReportIds() {
        return reportIds;
    }

    public String getId() {
        return id;
    }
}
