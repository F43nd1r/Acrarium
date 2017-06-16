package com.faendir.acra.mongod.data;

import com.faendir.acra.mongod.model.ProguardMapping;
import com.faendir.acra.mongod.model.ReportInfo;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author Lukas
 * @since 13.05.2017
 */
public final class ReportUtils {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.ENGLISH);

    public static Date getDateFromString(String s) {
        try {
            return dateFormat.parse(s);
        } catch (ParseException e) {
            return new Date();
        }
    }

    public static String retrace(String stacktrace, ProguardMapping mapping) throws IOException {
        File file = File.createTempFile("mapping", ".txt");
        FileUtils.writeStringToFile(file, mapping.getMappings());
        StringWriter writer = new StringWriter();
        new ReTrace(ReTrace.STACK_TRACE_EXPRESSION, false, file).retrace(new LineNumberReader(new StringReader(stacktrace)), new PrintWriter(writer));
        //noinspection ResultOfMethodCallIgnored
        file.delete();
        return writer.toString();
    }

    public static Date getLastReportDate(List<ReportInfo> reports) {
        return reports.stream().map(ReportInfo::getDate).reduce((d1, d2) -> d1.after(d2) ? d1 : d2).orElse(new Date());
    }

}
