package com.faendir.acra.data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author Lukas
 * @since 13.05.2017
 */
public final class ReportUtils {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.ENGLISH);

    public static List<Bug> getBugs(List<Report> reports) {
        List<Bug> bugs = new ArrayList<>();
        for (Report report : reports) {
            bugs.stream().filter(bug -> bug.is(report)).findAny().orElseGet(() -> {
                Bug bug = new Bug(report);
                bugs.add(bug);
                return bug;
            }).getReports().add(report);
        }
        return bugs;
    }

    static Date getDateFromString(String s) {
        try {
            return dateFormat.parse(s);
        } catch (ParseException e) {
            return new Date();
        }
    }

}
