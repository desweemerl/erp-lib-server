/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fw.server.exception;

public class NotFoundException extends RuntimeException {
    
    private String message;
    
    public NotFoundException() {        
    }
    
    public NotFoundException(String message) {
        this.message = message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }    
}
