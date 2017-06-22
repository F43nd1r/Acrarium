package com.faendir.acra.mongod.data;

import com.faendir.acra.mongod.model.ReportInfo;
import org.jetbrains.annotations.NotNull;

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

    @NotNull
    public static Date getDateFromString(@NotNull String s) {
        try {
            return dateFormat.parse(s);
        } catch (ParseException e) {
            return new Date();
        }
    }

    @NotNull
    public static Date getLastReportDate(@NotNull List<ReportInfo> reports) {
        return reports.stream().map(ReportInfo::getDate).reduce((d1, d2) -> d1.after(d2) ? d1 : d2).orElse(new Date());
    }

}
