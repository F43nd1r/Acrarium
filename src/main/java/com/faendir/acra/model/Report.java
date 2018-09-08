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

import com.faendir.acra.util.Utils;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.querydsl.core.annotations.QueryInit;
import org.acra.ReportField;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Type;
import org.json.JSONObject;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.lang.NonNull;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import java.time.ZonedDateTime;

/**
 * @author Lukas
 * @since 08.12.2017
 */
@Entity
public class Report {
    @Id private String id;
    @QueryInit("*.*")
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    @JsonIdentityReference(alwaysAsId = true)
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}, optional = false, fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Stacktrace stacktrace;
    @Type(type = "text") private String content;
    @JsonIgnore
    @Transient
    private JSONObject jsonObject;
    private ZonedDateTime date;
    private String userEmail;
    @Type(type = "text") private String userComment;
    private String androidVersion;
    private String phoneModel;
    private String brand;
    private String installationId;

    @PersistenceConstructor
    Report() {
    }

    public Report(@NonNull Stacktrace stacktrace, @NonNull String content) {
        this.stacktrace = stacktrace;
        this.content = content;
        this.id = getJsonObject().getString(ReportField.REPORT_ID.name());
        this.date = Utils.getDateFromString(getJsonObject().optString(ReportField.USER_CRASH_DATE.name()));
        this.userEmail = getJsonObject().optString(ReportField.USER_EMAIL.name());
        this.userComment = getJsonObject().optString(ReportField.USER_COMMENT.name());
        this.androidVersion = getJsonObject().optString(ReportField.ANDROID_VERSION.name());
        this.phoneModel = getJsonObject().optString(ReportField.PHONE_MODEL.name());
        this.brand = getJsonObject().optString(ReportField.BRAND.name());
        this.installationId = getJsonObject().optString(ReportField.INSTALLATION_ID.name());
    }

    @NonNull
    public final JSONObject getJsonObject() {
        if (jsonObject == null) {
            jsonObject = new JSONObject(content);
        }
        return jsonObject;
    }

    @NonNull
    public String getId() {
        return id;
    }

    @NonNull
    public ZonedDateTime getDate() {
        return date;
    }

    @NonNull
    public String getUserEmail() {
        return userEmail;
    }

    @NonNull
    public String getUserComment() {
        return userComment;
    }

    @NonNull
    public String getAndroidVersion() {
        return androidVersion;
    }

    @NonNull
    public String getPhoneModel() {
        return phoneModel;
    }

    @NonNull
    public String getBrand() {
        return brand;
    }

    @NonNull
    public String getInstallationId() {
        return installationId;
    }

    public Stacktrace getStacktrace() {
        return stacktrace;
    }

    public void setStacktrace(Stacktrace stacktrace) {
        this.stacktrace = stacktrace;
    }

    public String getContent() {
        return content;
    }
}
