package com.faendir.acra.mongod.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

    @NotNull private final String app;
    @NotNull private Level level;

    @PersistenceConstructor
    public Permission(@NotNull String app, @NotNull Level level) {
        this.level = level;
        this.app = app;
    }

    @NotNull
    public Level getLevel() {
        return level;
    }

    public void setLevel(@NotNull Level level) {
        this.level = level;
    }

    @NotNull
    public String getApp() {
        return app;
    }

    @NotNull
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
}
