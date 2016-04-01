package com.fw.server.exception;

import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;

public class JsonException extends RuntimeException {

    private Throwable throwable;
    private static final long serialVersionUID = 1L;
    private int status = HttpServletResponse.SC_BAD_REQUEST;
    private String message = "";
    private Map<String, Object> fields = new HashMap();
    

    public JsonException() {

        super();

    }

    public JsonException(Throwable throwable) {

        super();
        throwable.printStackTrace();
        this.throwable = throwable;

    }

    public JsonException(Throwable throwable, String message) {

        super();
        throwable.printStackTrace();
        this.message = message;

    }

    public JsonException(Throwable throwable, int status, String message) {

        super();
        throwable.printStackTrace();
        this.status = status;
        this.message = message;

    }

    public JsonException(int status, String message, Map<String, Object> fields) {

        super();
        this.status = status;
        this.message = message;
        this.fields = fields;

    }

    public JsonException(int status, String message) {

        super();
        this.status = status;
        this.message = message;

    }

    public JsonException(int status, Map<String, Object> fields) {

        super();
        this.status = status;
        this.fields = fields;

    }

    public JsonException(String message) {

        super();
        this.message = message;

    }

    public JsonException(String message, Map<String, Object> fields) {

        super();
        this.message = message;
        this.fields = fields;

    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void addField(String key, String value) {
        this.fields.put(key, value);
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, Object> getFields() {
        return fields;
    }
}
