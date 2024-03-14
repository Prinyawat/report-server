package com.softsquare.report.core.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

public class NumberUtils {

    public static String format(Number number, String pattern) {
        NumberFormat nf = new DecimalFormat(pattern, new DecimalFormatSymbols(Locale.US));
        return nf.format(number);
    }

    public static <T extends Number> T parse(String text, Class<T> targetClass) {
        return org.springframework.util.NumberUtils.parseNumber(text, targetClass);
    }

}
