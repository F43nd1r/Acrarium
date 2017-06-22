package com.faendir.acra.mongod.model;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author Lukas
 * @since 22.03.2017
 */
@Document
public class App {
    @NotNull private final String id;
    @NotNull private final String name;
    @NotNull private final String password;

    @PersistenceConstructor
    private App(@NotNull String id, @NotNull String name, @NotNull String password) {
        this.id = id;
        this.name = name;
        this.password = password;
    }

    public App(@NotNull String name, @NotNull String password) {
        this("", name, password);
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public String getPassword() {
        return password;
    }

    @NotNull
    public String getId() {
        return id;
    }
}
