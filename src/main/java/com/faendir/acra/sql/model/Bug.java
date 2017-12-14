package com.faendir.acra.sql.model;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;
import org.springframework.lang.NonNull;
import org.springframework.data.annotation.PersistenceConstructor;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.Date;

/**
 * @author Lukas
 * @since 08.12.2017
 */
@Entity
public class Bug {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private App app;
    private boolean solved;
    @Type(type = "text") private String stacktrace;
    private Date lastReport;
    private int versionCode;

    @PersistenceConstructor
    Bug() {
    }

    public Bug(@NonNull App app, @NonNull String stacktrace, int versionCode, @NonNull Date lastReport) {
        this.app = app;
        this.stacktrace = stacktrace;
        this.versionCode = versionCode;
        this.lastReport = lastReport;
        this.solved = false;
    }

    @NonNull
    public App getApp() {
        return app;
    }

    public boolean isSolved() {
        return solved;
    }

    public void setSolved(boolean solved) {
        this.solved = solved;
    }

    public int getVersionCode() {
        return versionCode;
    }

    @NonNull
    public String getStacktrace() {
        return stacktrace;
    }

    @NonNull
    public Date getLastReport() {
        return lastReport;
    }

    public void setLastReport(@NonNull Date lastReport) {
        if(this.lastReport == null || this.lastReport.before(lastReport))
        this.lastReport = lastReport;
    }
}
