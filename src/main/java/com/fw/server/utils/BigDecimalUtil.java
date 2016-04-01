package com.fw.server.utils;

import java.math.BigDecimal;


public class BigDecimalUtil {
    
    public static boolean isEqual(BigDecimal value1, BigDecimal value2) {
        
        if (value1 == null){
            if (value2 == null) {
                return true;
            } else {
                return false;
            }
        } else if (value2 == null) {
            if (value1 == null) {
                return true;
            } else {
                return false;
            }            
        } 
        
        return value1.compareTo(value2) == 0;
        
    }
    
}
