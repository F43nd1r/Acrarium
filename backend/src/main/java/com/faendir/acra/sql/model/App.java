package com.faendir.acra.sql.model;

import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.lang.NonNull;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * @author Lukas
 * @since 08.12.2017
 */
@Entity
public class App {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;
    private String password;

    @PersistenceConstructor
    App() {
    }

    public App(@NonNull String name, @NonNull String password) {
        this.name = name;
        this.password = password;
    }

    public int getId() {
        return id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @NonNull
    public String getPassword() {
        return password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        App app = (App) o;
        return id == app.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
