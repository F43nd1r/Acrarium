package com.faendir.acra.sql.util;

import java.util.Date;

/**
 * @author lukas
 * @since 17.05.18
 */
public class DateResult<T> {
    private final T group;
    private final Date date;

    public DateResult(T group, Date date) {
        this.group = group;
        this.date = date;
    }

    public T getGroup() {
        return group;
    }

    public Date getDate() {
        return date;
    }
}
