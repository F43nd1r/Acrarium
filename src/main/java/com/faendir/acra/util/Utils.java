/*
 * (C) Copyright 2018 Lukas Morawietz (https://github.com/F43nd1r)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.faendir.acra.util;

import com.faendir.acra.model.App;
import com.vaadin.ui.UI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.lang.NonNull;
import org.springframework.web.util.UriComponentsBuilder;
import proguard.retrace.ReTrace;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Lukas
 * @since 13.05.2017
 */
public final class Utils {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    private static final Log log = LogFactory.getLog(Utils.class);

    @NonNull
    public static LocalDateTime getDateFromString(@NonNull String s) {
        try {
            return LocalDateTime.parse(s, formatter);
        } catch (DateTimeParseException e) {
            return LocalDateTime.now();
        }
    }

    public static String retrace(@NonNull String stacktrace, @NonNull String mappings) {
        try (Reader mappingsReader = new StringReader(mappings);
             LineNumberReader stacktraceReader = new LineNumberReader(new StringReader(stacktrace));
             StringWriter output = new StringWriter()) {
            new ReTrace(ReTrace.STACK_TRACE_EXPRESSION, false, mappingsReader).retrace(stacktraceReader, new PrintWriter(output));
            return output.toString();
        } catch (IOException e) {
            log.error("Failed to retrace stacktrace", e);
            return stacktrace;
        }
    }

    public static String getUrlWithFragment(String fragment) {
        if(fragment!= null && fragment.isEmpty()) fragment = null;
        return UriComponentsBuilder.fromUri(UI.getCurrent().getPage().getLocation()).fragment(fragment).build().encode().toUri().toASCIIString();
    }

    public static String generifyStacktrace(String stacktrace, App.Configuration configuration) {
        List<String> lines = Pattern.compile("\r?\n").splitAsStream(stacktrace).collect(Collectors.toCollection(ArrayList::new));
        StringBuilder output = new StringBuilder();
        Pattern headLinePattern = Pattern.compile("^([\\w.]+)(:(.*))?$");
        Pattern tracePattern = Pattern.compile("^\\s*at\\s+([\\w.$_]+)\\.([\\w$_]+)\\((.*)\\)$");
        Pattern sourcePattern = Pattern.compile("^((android|java)\\..*:)(\\d+)$");
        Pattern instancePattern = Pattern.compile("(([a-z_$][a-z0-9_$]*\\.)+[a-zA-Z_$][a-zA-Z0-9_$]*@)([a-fA-F0-9]+)");
        while (lines.size() > 0) {
            String line = lines.remove(0);
            Matcher headLineMatcher = headLinePattern.matcher(line);
            if (!headLineMatcher.find()) {
                output.append(line);
            } else if (!configuration.matchByMessage()) {
                output.append(headLineMatcher.group(1)).append("%");
            } else if (configuration.ignoreInstanceIds()) {
                String message = headLineMatcher.group(2);
                if (message != null) {
                    output.append(headLineMatcher.group(1)).append(instancePattern.matcher(message).replaceAll("$1%"));
                } else {
                    output.append(headLineMatcher.group(1));
                }
            }
            Matcher lineMatcher;
            while (lines.size() > 0 && (lineMatcher = tracePattern.matcher(line = lines.remove(0))).find()) {
                output.append('\n');
                Matcher sourceMatcher;
                if (configuration.ignoreAndroidLineNumbers() && (sourceMatcher = sourcePattern.matcher(lineMatcher.group(3))).find()) {
                    output.append(sourceMatcher.group(1)).append("%");
                } else {
                    output.append(line);
                }
            }
            while (lines.size() > 0 && !headLinePattern.matcher(line = lines.remove(0)).find()) {
                output.append('\n').append(line);
            }
        }
        return output.toString();
    }
}
