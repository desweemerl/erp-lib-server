package com.fw.server.exception;

import javax.servlet.http.HttpServletResponse;

public class TextException extends RuntimeException {

    private String message;
    private int status = HttpServletResponse.SC_BAD_REQUEST;
    
    public TextException(String message) {
        this.message = message;
    }

    public TextException(int status, String message) {
        this.status = status;
        this.message = message;
    }
    
    
    public void setMessage(String message) {
        this.message = message;
    }    
    
    public String getMessage() {
        return this.message;
    }
    
    public void setStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }    

}
