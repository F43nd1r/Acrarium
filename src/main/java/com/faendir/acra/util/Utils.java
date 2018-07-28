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
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * @author Lukas
 * @since 13.05.2017
 */
public final class Utils {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    private static final Log log = LogFactory.getLog(Utils.class);

    @NonNull
    public static ZonedDateTime getDateFromString(@NonNull String s) {
        try {
            return ZonedDateTime.parse(s, formatter);
        } catch (DateTimeParseException e) {
            return ZonedDateTime.now();
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
}
