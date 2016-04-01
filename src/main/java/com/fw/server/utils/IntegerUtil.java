package com.fw.server.utils;


public class IntegerUtil {
    
    public static boolean isEqual(Integer value1, Integer value2) {
        
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
