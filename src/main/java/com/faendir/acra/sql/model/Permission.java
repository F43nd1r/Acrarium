package com.faendir.acra.sql.model;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.Embeddable;
import javax.persistence.ManyToOne;

/**
 * @author Lukas
 * @since 20.05.2017
 */
@Embeddable
public class Permission implements GrantedAuthority {
    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private App app;
    private Level level;

    @PersistenceConstructor
    Permission() {
    }

    public Permission(@NonNull App app, @NonNull Level level) {
        this.level = level;
        this.app = app;
    }

    @NonNull
    public Level getLevel() {
        return level;
    }

    public void setLevel(@NonNull Level level) {
        this.level = level;
    }

    @NonNull
    public App getApp() {
        return app;
    }

    @NonNull
    @Override
    public String getAuthority() {
        return "PERMISSION_" + level.name() + "_" + app;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Permission that = (Permission) o;

        return app.equals(that.app);
    }

    @Override
    public int hashCode() {
        return app.hashCode();
    }

    public enum Level {
        NONE,
        VIEW,
        EDIT,
        ADMIN
    }
}
