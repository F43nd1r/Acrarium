package com.faendir.acra.sql.util;

import java.util.Date;

/**
 * @author Lukas
 * @since 14.12.2017
 */
public class DayCount {
    private final Date day;
    private final long count;

    public DayCount(Date day, long count) {
        this.day = day;
        this.count = count;
    }

    public Date getDay() {
        return day;
    }

    public long getCount() {
        return count;
    }
}
