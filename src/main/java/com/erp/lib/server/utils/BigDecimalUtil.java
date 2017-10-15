package com.erp.lib.server.utils;

import java.math.BigDecimal;

public class BigDecimalUtil {

    public static boolean isEqual(BigDecimal value1, BigDecimal value2) {
        if (value1 == null) {
            return value2 == null;
        } else if (value2 == null) {
            return false;
        }

        return value1.compareTo(value2) == 0;
    }
}
