package com.faendir.acra.mongod.model;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * @author Lukas
 * @since 19.05.2017
 */
@Document
public class ProguardMapping {
    @NotNull private final MetaData id;
    @NotNull private final String mappings;

    @PersistenceConstructor
    private ProguardMapping(@NotNull MetaData id, @NotNull String mappings) {
        this.mappings = mappings;
        this.id = id;
    }

    public ProguardMapping(@NotNull String app, int version, @NotNull String mappings) {
        this(new MetaData(app, version), mappings);
    }

    @NotNull
    public String getApp() {
        return id.app;
    }

    public int getVersion() {
        return id.version;
    }

    @NotNull
    public String getMappings() {
        return mappings;
    }

    public static class MetaData implements Serializable {
        @NotNull private final String app;
        private final int version;

        @PersistenceConstructor
        public MetaData(@NotNull String app, int version) {
            this.app = app;
            this.version = version;
        }
    }
}
