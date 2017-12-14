package com.faendir.acra.sql.model;

import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.lang.NonNull;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.ManyToOne;
import java.io.Serializable;
import java.sql.Blob;
import java.util.Objects;

/**
 * @author Lukas
 * @since 11.12.2017
 */
@Entity
@IdClass(Attachment.MetaData.class)
public class Attachment {
    @Id
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}, optional = false)
    private Report report;
    @Id private String filename;
    private Blob content;

    @PersistenceConstructor
    Attachment() {
    }

    public Attachment(@NonNull Report report, @NonNull String filename, @NonNull Blob content) {
        this.report = report;
        this.filename = filename;
        this.content = content;
    }

    @NonNull
    public Blob getContent() {
        return content;
    }

    @NonNull
    public String getFilename() {
        return filename;
    }

    public static class MetaData implements Serializable {
        private Report report;
        private String filename;

        @PersistenceConstructor
        MetaData() {
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MetaData metaData = (MetaData) o;
            return Objects.equals(report, metaData.report) && Objects.equals(filename, metaData.filename);
        }

        @Override
        public int hashCode() {
            return Objects.hash(report, filename);
        }
    }
}
