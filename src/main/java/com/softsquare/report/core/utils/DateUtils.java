package com.softsquare.report.core.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    public static String format(Date date, String pattern) {
        DateFormat dateFormat = new SimpleDateFormat(pattern, Locale.US);
        return dateFormat.format(date);
    }

    public static Date parse(String source, String pattern) {
        DateFormat dateFormat = new SimpleDateFormat(pattern, Locale.US);
        try {
            return dateFormat.parse(source);
        } catch (ParseException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static String format(Date date, String pattern, Locale locale) {
        DateFormat dateFormat = new SimpleDateFormat(pattern, locale);
        return dateFormat.format(date);
    }

}
