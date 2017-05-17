package com.faendir.acra.data;

import org.json.JSONObject;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * @author Lukas
 * @since 22.03.2017
 */
@Document
public class Report {
    private String id;
    @Indexed
    private String app;
    private JSONObject content;

    public Report() {
    }

    public Report(JSONObject content, String app) {
        id = content == null ? null : content.getString("REPORT_ID");
        this.content = content;
        this.app = app;
    }

    public JSONObject getContent() {
        return content;
    }

    public Date getDate() {
        return ReportUtils.getDateFromString(content.getString("USER_CRASH_DATE"));
    }

    public String getId() {
        return id;
    }
}
