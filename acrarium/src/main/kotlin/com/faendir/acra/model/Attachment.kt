/*
 * (C) Copyright 2018 Lukas Morawietz (https://github.com/F43nd1r)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.faendir.acra.model;

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
