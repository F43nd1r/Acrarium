package com.faendir.acra.data;

import org.apache.commons.io.FileUtils;
import proguard.retrace.ReTrace;

import java.io.File;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
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

    static String retrace(String stacktrace, ProguardMapping mapping) throws IOException {
        File file = File.createTempFile("mapping", ".txt");
        FileUtils.writeStringToFile(file, mapping.getMappings());
        StringWriter writer = new StringWriter();
        new ReTrace(ReTrace.STACK_TRACE_EXPRESSION, false, file).retrace(new LineNumberReader(new StringReader(stacktrace)), new PrintWriter(writer));
        return writer.toString();
    }

}
