package com.faendir.acra.sql.util;

/**
 * @author Lukas
 * @since 14.12.2017
 */
public class AndroidVersionCount {
    private final String androidVersion;
    private final long count;

    public AndroidVersionCount(String androidVersion, long count) {
        this.androidVersion = androidVersion;
        this.count = count;
    }

    public String getAndroidVersion() {
        return androidVersion;
    }

    public long getCount() {
        return count;
    }
}
