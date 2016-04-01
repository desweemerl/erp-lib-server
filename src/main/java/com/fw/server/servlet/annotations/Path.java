/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fw.server.servlet.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Path {
    
    String value();
    
}
