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

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import java.io.Serializable;
import java.util.Objects;

/**
 * @author lukas
 * @since 07.12.18
 */
@Entity
@IdClass(MailSettings.ID.class)
public class MailSettings {
    @Id
    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private App app;
    @Id
    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "username")
    private User user;
    private boolean newBug;
    private boolean regression;
    private boolean spike;
    private boolean summary;

    @PersistenceConstructor
    MailSettings() {
    }

    public MailSettings(@NonNull App app, @NonNull User user) {
        this.app = app;
        this.user = user;
    }

    public App getApp() {
        return app;
    }

    public User getUser() {
        return user;
    }

    public boolean getNewBug() {
        return newBug;
    }

    public void setNewBug(boolean newBug) {
        this.newBug = newBug;
    }

    public boolean getRegression() {
        return regression;
    }

    public void setRegression(boolean regression) {
        this.regression = regression;
    }

    public boolean getSpike() {
        return spike;
    }

    public void setSpike(boolean spike) {
        this.spike = spike;
    }

    public boolean getSummary() {
        return summary;
    }

    public void setSummary(boolean summary) {
        this.summary = summary;
    }

    static class ID implements Serializable {
        private int app;
        private String user;

        @PersistenceConstructor
        ID() {
        }

        public ID(App app, User user) {
            this.app = app.getId();
            this.user = user.getUsername();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ID id = (ID) o;
            return app == id.app &&
                    Objects.equals(user, id.user);
        }

        @Override
        public int hashCode() {
            return Objects.hash(app, user);
        }
    }
}
