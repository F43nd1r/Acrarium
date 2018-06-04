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
