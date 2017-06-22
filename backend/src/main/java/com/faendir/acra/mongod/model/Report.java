package com.faendir.acra.mongod.model;

import com.faendir.acra.mongod.data.ReportUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.function.Function;

/**
 * @author Lukas
 * @since 22.03.2017
 */
@Document
public class Report implements AppScoped {
    @NotNull @Indexed private final String app;
    @NotNull private final String id;
    @NotNull private final JSONObject content;

    @PersistenceConstructor
    private Report(@NotNull String id, @NotNull String app, @NotNull JSONObject content) {
        this.id = id;
        this.app = app;
        this.content = content;
    }

    public Report(@NotNull JSONObject content, @NotNull String app) {
        this.content = content;
        this.app = app;
        id = getValueSafe("REPORT_ID", content::getString, "");
    }

    @NotNull
    public JSONObject getContent() {
        return content;
    }

    @NotNull
    public Date getDate() {
        return ReportUtils.getDateFromString(getValueSafe("USER_CRASH_DATE", content::getString, ""));
    }

    @NotNull
    public String getId() {
        return id;
    }

    @NotNull
    @Override
    public String getApp() {
        return app;
    }

    @NotNull
    public String getStacktrace() {
        return getValueSafe("STACK_TRACE", content::getString, "");
    }

    public int getVersionCode() {
        return getValueSafe("APP_VERSION_CODE", content::getInt, -1);
    }

    @NotNull
    public String getVersionName() {
        return getValueSafe("APP_VERSION_NAME", content::getString, "");
    }

    @NotNull
    public String getUserEmail() {
        return getValueSafe("USER_EMAIL", content::getString, "");
    }

    @NotNull
    public String getUserComment() {
        return getValueSafe("USER_COMMENT", content::getString, "");
    }

    @NotNull
    public String getAndroidVersion() {
        return getValueSafe("ANDROID_VERSION", content::getString, "");
    }

    @NotNull
    public String getPhoneModel() {
        return getValueSafe("PHONE_MODEL", content::getString, "");
    }

    @NotNull
    private <T> T getValueSafe(@NotNull String key, @NotNull Function<String, T> function, @NotNull T defaultValue) {
        try {
            return function.apply(key);
        } catch (JSONException e) {
            return defaultValue;
        }
    }
}
