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
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.lang.NonNull;

import javax.persistence.CascadeType;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

/**
 * @author Lukas
 * @since 08.12.2017
 */
@Entity
public class App {
    @OneToOne(cascade = CascadeType.ALL, optional = false, orphanRemoval = true, fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User reporter;
    private Configuration configuration;
    private String name;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @PersistenceConstructor
    App() {
    }

    public App(@NonNull String name, @NonNull User reporter) {
        this.name = name;
        this.reporter = reporter;
        this.configuration = new Configuration();
    }

    @NonNull
    public User getReporter() {
        return reporter;
    }

    public void setReporter(User reporter) {
        this.reporter = reporter;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
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
        App app = (App) o;
        return id == app.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Embeddable
    public static class Configuration {
        private boolean matchByMessage;
        private boolean ignoreInstanceIds;
        private boolean ignoreAndroidLineNumbers;

        @PersistenceConstructor
        Configuration() {
            matchByMessage = true;
            ignoreInstanceIds = true;
            ignoreAndroidLineNumbers = true;
        }

        public Configuration(boolean matchByMessage, boolean ignoreInstanceIds, boolean ignoreAndroidLineNumbers) {
            this.matchByMessage = matchByMessage;
            this.ignoreInstanceIds = ignoreInstanceIds;
            this.ignoreAndroidLineNumbers = ignoreAndroidLineNumbers;
        }

        public boolean matchByMessage() {
            return matchByMessage;
        }

        public boolean ignoreInstanceIds() {
            return ignoreInstanceIds;
        }

        public boolean ignoreAndroidLineNumbers() {
            return ignoreAndroidLineNumbers;
        }
    }
}
