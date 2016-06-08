package com.pepperonas.jxaesprefs.utils;

/**
 * @author Martin Pfeffer (pepperonas)
 */
public class NumberFormatUtils {

    public static double decimalPlaces(double value, int precision) {
        return (double) Math.round(value * Math.pow(10.0D, (double) precision)) / Math.pow(10.0D, (double) precision);
    }
}
