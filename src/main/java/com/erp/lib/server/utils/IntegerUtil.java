package com.erp.lib.server.utils;

public class IntegerUtil {

    public static boolean isEqual(Integer value1, Integer value2) {
        if (value1 == null) {
            return value2 == null;
        } else if (value2 == null) {
            return false;
        }

        return value1.compareTo(value2) == 0;
    }
}
