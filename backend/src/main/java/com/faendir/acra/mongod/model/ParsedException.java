package com.faendir.acra.mongod.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.annotation.PersistenceConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Lukas
 * @since 17.06.2017
 */
public class ParsedException {
    @NotNull private final String exceptionClass;
    @NotNull private final String message;
    @NotNull private final List<StackTraceElement> stacktraceElements;
    @Nullable private final ParsedException cause;

    @PersistenceConstructor
    private ParsedException(@NotNull String exceptionClass, @NotNull String message, @NotNull List<StackTraceElement> stacktraceElements, @Nullable ParsedException cause) {
        this.exceptionClass = exceptionClass;
        this.message = message;
        this.stacktraceElements = stacktraceElements;
        this.cause = cause;
    }

    public ParsedException(@NotNull String stacktrace) {
        List<String> lines = new ArrayList<>(Arrays.asList(stacktrace.split("\r?\n")));
        Pattern headLinePattern = Pattern.compile("^([\\w.]+)(:(.*))?$");
        Pattern tracePattern = Pattern.compile("^\\s*at\\s+([\\w.$_]+)\\.([\\w$_]+)\\((.*)\\)$");
        Pattern sourcePattern = Pattern.compile("^(.*):(\\d+)$");
        Matcher headLineMatcher = headLinePattern.matcher(lines.remove(0));
        if (headLineMatcher.find()) {
            exceptionClass = headLineMatcher.group(1);
            String m = headLineMatcher.group(3);
            if (m == null) m = "";
            else m = m.trim();
            message = m;
        } else {
            exceptionClass = "";
            message = "";
        }
        stacktraceElements = new ArrayList<>();
        Matcher lineMatcher;
        while (lines.size() > 0 && (lineMatcher = tracePattern.matcher(lines.remove(0))).find()) {
            String clazz = lineMatcher.group(1);
            String method = lineMatcher.group(2);
            String source = lineMatcher.group(3);
            int lineNumber = -1;
            Matcher sourceMatcher = sourcePattern.matcher(source);
            if (sourceMatcher.find()) {
                source = sourceMatcher.group(1);
                lineNumber = Integer.parseInt(sourceMatcher.group(2));
            } else if (source.equals("Unknown Source")) {
                source = null;
            } else if (source.equals("Native Method")) {
                source = null;
                lineNumber = -2;
            }
            stacktraceElements.add(new StackTraceElement(clazz, method, source, lineNumber));
        }
        while (lines.size() > 0 && !headLinePattern.matcher(lines.get(0)).find()) {
            lines.remove(0);
        }
        if (lines.size() > 0) {
            cause = new ParsedException(lines.stream().collect(Collectors.joining("\n")));
        } else {
            cause = null;
        }
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ParsedException that = (ParsedException) o;

        return exceptionClass.equals(that.exceptionClass) && message.equals(that.message) && stacktraceElements.equals(that.stacktraceElements)
                && (cause != null ? cause.equals(that.cause) : that.cause == null);
    }

    @Override
    public int hashCode() {
        int result = exceptionClass.hashCode();
        result = 31 * result + message.hashCode();
        result = 31 * result + stacktraceElements.hashCode();
        result = 31 * result + (cause != null ? cause.hashCode() : 0);
        return result;
    }
}
