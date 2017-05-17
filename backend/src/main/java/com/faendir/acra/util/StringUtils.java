package com.faendir.acra.util;

import org.ocpsoft.prettytime.PrettyTime;

import java.util.Date;

/**
 * @author Lukas
 * @since 14.05.2017
 */
public class StringUtils {

    public static String distanceFromNowAsString(Date dateTime){
        return new PrettyTime().format(dateTime);
    }
}
