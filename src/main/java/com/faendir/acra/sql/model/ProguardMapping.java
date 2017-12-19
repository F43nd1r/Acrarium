package com.faendir.acra.sql.model;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.lang.NonNull;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.ManyToOne;
import java.io.Serializable;
import java.util.Objects;

/**
 * @author Lukas
 * @since 11.12.2017
 */
@Entity
@IdClass(ProguardMapping.MetaData.class)
public class ProguardMapping {
    @Id
    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private App app;
    @Id private int versionCode;
    @Type(type = "text") private String mappings;

    @PersistenceConstructor
    ProguardMapping() {
    }

    public ProguardMapping(App app, int versionCode, @NonNull String mappings) {
        this.app = app;
        this.versionCode = versionCode;
        this.mappings = mappings;
    }

    @NonNull
    public String getMappings() {
        return mappings;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public static class MetaData implements Serializable {
        private int app;
        private int versionCode;

        @PersistenceConstructor
        MetaData() {
        }

        public MetaData(App app, int versionCode) {
            this.app = app.getId();
            this.versionCode = versionCode;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MetaData metaData = (MetaData) o;
            return versionCode == metaData.versionCode && Objects.equals(app, metaData.app);
        }

        @Override
        public int hashCode() {
            return Objects.hash(app, versionCode);
        }
    }
}
