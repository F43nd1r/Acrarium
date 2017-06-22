package com.faendir.acra.mongod.model;

import org.jetbrains.annotations.NotNull;
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
    @NotNull private final String id;
    @NotNull @Indexed private final String app;
    @NotNull private final String stacktrace;
    private final int versionCode;
    @NotNull private final List<String> reportIds;
    private boolean solved;

    @PersistenceConstructor
    private Bug(@NotNull String id, @NotNull String app, boolean solved, @NotNull String stacktrace, int versionCode, @NotNull List<String> reportIds) {
        this.id = id;
        this.app = app;
        this.solved = solved;
        this.stacktrace = stacktrace;
        this.versionCode = versionCode;
        this.reportIds = reportIds;
    }

    public Bug(@NotNull String app, @NotNull String stacktrace, int versionCode){
        this("", app, false, stacktrace, versionCode, new ArrayList<>());
    }

    public boolean isSolved() {
        return solved;
    }

    public void setSolved(boolean solved) {
        this.solved = solved;
    }

    @NotNull
    public String getStacktrace() {
        return stacktrace;
    }

    public int getVersionCode() {
        return versionCode;
    }

    @NotNull
    @Override
    public String getApp() {
        return app;
    }

    @NotNull
    public List<String> getReportIds() {
        return reportIds;
    }

    @NotNull
    public String getId() {
        return id;
    }
}
