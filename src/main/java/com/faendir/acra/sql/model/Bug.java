package com.faendir.acra.sql.model;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.lang.NonNull;

import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Lukas
 * @since 08.12.2017
 */
@Entity
public class Bug {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH}, optional = false, fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private App app;
    private boolean solved;
    @ElementCollection(fetch = FetchType.LAZY)
    @Type(type = "text")
    private List<String> stacktraces;
    @Type(type = "text") private String title;
    private Date lastReport;
    private int versionCode;

    @PersistenceConstructor
    Bug() {
    }

    public Bug(@NonNull App app, @NonNull String stacktrace, int versionCode, @NonNull Date lastReport) {
        this.app = app;
        this.stacktraces = new ArrayList<>();
        this.stacktraces.add(stacktrace);
        this.title = stacktrace.split("\n", 2)[0];
        this.versionCode = versionCode;
        this.lastReport = lastReport;
        this.solved = false;
    }

    public int getId() {
        return id;
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
    public List<String> getStacktraces() {
        return stacktraces != null ? stacktraces : new ArrayList<>();
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    public void setTitle(@NonNull String title) {
        this.title = title;
    }

    @NonNull
    public Date getLastReport() {
        return lastReport;
    }

    public void setLastReport(@NonNull Date lastReport) {
        if (this.lastReport == null || this.lastReport.before(lastReport)) this.lastReport = lastReport;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bug bug = (Bug) o;
        return id == bug.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
