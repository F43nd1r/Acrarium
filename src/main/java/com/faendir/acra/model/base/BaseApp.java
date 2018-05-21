package com.faendir.acra.model.base;

import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.lang.NonNull;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 * @author lukas
 * @since 20.05.18
 */
@MappedSuperclass
public abstract class BaseApp {
    private String name;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @PersistenceConstructor
    protected BaseApp(){
    }

    protected BaseApp(@NonNull String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseApp app = (BaseApp) o;
        return id == app.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
