package com.faendir.acra.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.lang.NonNull;
import proguard.retrace.ReTrace;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author Lukas
 * @since 13.05.2017
 */
public final class Utils {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.ENGLISH);
    private static final Log log = LogFactory.getLog(Utils.class);

    @NonNull
    public static Date getDateFromString(@NonNull String s) {
        try {
            return dateFormat.parse(s);
        } catch (ParseException e) {
            return new Date();
        }
    }

    public static String retrace(@NonNull String stacktrace, @NonNull String mappings) {
        try (Reader mappingsReader = new StringReader(mappings); Reader stacktraceReader = new StringReader(stacktrace); StringWriter output = new StringWriter()) {
            new ReTrace(ReTrace.STACK_TRACE_EXPRESSION, false, mappingsReader, stacktraceReader, output).execute();
            return output.toString();
        } catch (IOException e) {
            log.error("Failed to retrace stacktrace", e);
            return stacktrace;
        }
    }
}
