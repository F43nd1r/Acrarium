package com.faendir.acra.mongod.model;

import com.faendir.acra.mongod.data.ReportUtils;
import org.json.JSONException;
import org.json.JSONObject;
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
    private String id;
    @Indexed
    private String app;
    private JSONObject content;

    public Report() {
    }

    public Report(JSONObject content, String app) {
        this.content = content;
        this.app = app;
        id = content == null ? null : getValueSafe("REPORT_ID", content::getString, "");
    }

    public JSONObject getContent() {
        return content;
    }

    public Date getDate() {
        return ReportUtils.getDateFromString(getValueSafe("USER_CRASH_DATE", content::getString, ""));
    }

    public String getId() {
        return id;
    }

    @Override
    public String getApp() {
        return app;
    }

    public String getStacktrace() {
        return getValueSafe("STACK_TRACE", content::getString, "");
    }

    public int getVersionCode() {
        return getValueSafe("APP_VERSION_CODE", content::getInt, -1);
    }

    public String getVersionName() {
        return getValueSafe("APP_VERSION_NAME", content::getString, "");
    }

    public String getUserEmail() {
        return getValueSafe("USER_EMAIL", content::getString, "");
    }

    public String getUserComment() {
        return getValueSafe("USER_COMMENT", content::getString, "");
    }

    public String getAndroidVersion() {
        return getValueSafe("ANDROID_VERSION", content::getString, "");
    }

    public String getPhoneModel() {
        return getValueSafe("PHONE_MODEL", content::getString, "");
    }

    private <T> T getValueSafe(String key, Function<String, T> function, T defaultValue) {
        try {
            return function.apply(key);
        } catch (JSONException e) {
            return defaultValue;
        }
    }
}
