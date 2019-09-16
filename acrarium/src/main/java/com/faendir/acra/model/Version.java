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
import org.springframework.lang.Nullable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.util.Objects;

/**
 * @author lukas
 * @since 26.07.18
 */
@Entity
public class Version implements Comparable<Version>{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private App app;
    private int code;
    private String name;
    @Type(type = "text")
    @Nullable
    private String mappings;

    @PersistenceConstructor
    Version() {
    }

    public Version(App app, int code, String name) {
        this.app = app;
        this.code = code;
        this.name = name;
    }

    public Version(App app, int code, String name, String mappings) {
        this(app, code, name);
        this.mappings = mappings;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    @Nullable
    public String getMappings() {
        return mappings;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMappings(@Nullable String mappings) {
        this.mappings = mappings;
    }

    @Override
    public int compareTo(@NonNull Version o) {
        return Integer.compare(code, o.code);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Version version = (Version) o;
        return id == version.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
