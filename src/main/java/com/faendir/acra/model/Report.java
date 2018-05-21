package com.faendir.acra.model;

import com.faendir.acra.util.Utils;
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
import java.util.Date;

/**
 * @author Lukas
 * @since 08.12.2017
 */
@Entity
public class Report {
    @Id private String id;
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}, optional = false, fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Bug bug;
    @Type(type = "text") private String content;
    @Transient private JSONObject jsonObject;
    private Date date;
    @Type(type = "text") private String stacktrace;
    private int versionCode;
    private String versionName;
    private String userEmail;
    @Type(type = "text") private String userComment;
    private String androidVersion;
    private String phoneModel;

    @PersistenceConstructor
    Report() {
    }

    public Report(@NonNull Bug bug, @NonNull String content) {
        this.bug = bug;
        this.content = content;
        this.id = getJsonObject().optString(ReportField.REPORT_ID.name());
        this.date = Utils.getDateFromString(getJsonObject().optString(ReportField.USER_CRASH_DATE.name()));
        this.stacktrace = getJsonObject().optString(ReportField.STACK_TRACE.name());
        this.versionCode = getJsonObject().optInt(ReportField.APP_VERSION_CODE.name());
        this.versionName = getJsonObject().optString(ReportField.APP_VERSION_NAME.name());
        this.userEmail = getJsonObject().optString(ReportField.USER_EMAIL.name());
        this.userComment = getJsonObject().optString(ReportField.USER_COMMENT.name());
        this.androidVersion = getJsonObject().optString(ReportField.ANDROID_VERSION.name());
        this.phoneModel = getJsonObject().optString(ReportField.PHONE_MODEL.name());
    }

    @NonNull
    public final JSONObject getJsonObject() {
        if (jsonObject == null) {
            jsonObject = new JSONObject(content);
        }
        return jsonObject;
    }

    @NonNull
    public Bug getBug() {
        return bug;
    }

    public void setBug(Bug bug) {
        this.bug = bug;
    }

    @NonNull
    public String getId() {
        return id;
    }

    @NonNull
    public Date getDate() {
        return date;
    }

    @NonNull
    public String getStacktrace() {
        return stacktrace;
    }

    public int getVersionCode() {
        return versionCode;
    }

    @NonNull
    public String getVersionName() {
        return versionName;
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
}
