package com.faendir.acra.mongod.model;

import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.security.core.GrantedAuthority;

/**
 * @author Lukas
 * @since 20.05.2017
 */
public class Permission implements GrantedAuthority {

    public enum Level {
        NONE,
        VIEW,
        EDIT,
        ADMIN
    }

    private Level level;
    private String app;

    @PersistenceConstructor
    public Permission() {
    }

    public Permission(String app, Level level) {
        this.level = level;
        this.app = app;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public String getApp() {
        return app;
    }

    @Override
    public String getAuthority() {
        return "PERMISSION_" + level.name() + "_" + app;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Permission that = (Permission) o;

        return app.equals(that.app);
    }

    @Override
    public int hashCode() {
        return app.hashCode();
    }
}
