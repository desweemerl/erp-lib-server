package com.fw.server.database;

import java.lang.reflect.Field;

public class DBField {

    private boolean readOnly;
    private Field field;

    public DBField(Field field, boolean readOnly) {

        this.field = field;
        this.readOnly = readOnly;

    }

    public boolean isReadOnly() {
        return readOnly;
    }
    
    public Field getField() {
        return field;
    }

}
