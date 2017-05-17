package com.faendir.acra.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Lukas
 * @since 13.05.2017
 */
public class Bug {
    private List<Report> reports;
    private String trace;
    private int versionCode;

    public Bug(Report report){
        reports = new ArrayList<>();
        this.trace = report.getContent().getString("STACK_TRACE");
        this.versionCode = report.getContent().getInt("APP_VERSION_CODE");
    }

    public boolean is(Report report){
        return report.getContent().getString("STACK_TRACE").equals(trace) && report.getContent().getInt("APP_VERSION_CODE") == versionCode;
    }

    public List<Report> getReports() {
        return reports;
    }

    public Date getLastDate(){
        return reports.stream().map(Report::getDate).reduce((d1, d2) -> d1.after(d2) ? d1 : d2).orElse(new Date());
    }

    public String getTrace() {
        return trace;
    }

    public int getVersionCode() {
        return versionCode;
    }
}
