package com.faendir.acra.model.base;

import com.faendir.acra.model.App;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.lang.NonNull;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lukas
 * @since 17.05.18
 */
@MappedSuperclass
public abstract class BaseBug {
    @Type(type = "text") protected String title;
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.REFRESH}, optional = false, fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private App app;
    private boolean solved;
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "bug_stacktraces", joinColumns = @JoinColumn(name = "bug_id", referencedColumnName = "id"))
    @Type(type = "text")
    private List<String> stacktraces;
    private int versionCode;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @PersistenceConstructor
    protected BaseBug() {
        stacktraces = new ArrayList<>();
    }

    protected BaseBug(App app, int versionCode) {
        this();
        this.app = app;
        this.versionCode = versionCode;
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

    public void setStacktraces(@NonNull List<String> stacktraces) {
        this.stacktraces = stacktraces;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    public void setTitle(@NonNull String title) {
        this.title = title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseBug bug = (BaseBug) o;
        return id == bug.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
