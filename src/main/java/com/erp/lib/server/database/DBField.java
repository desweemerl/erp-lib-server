package com.erp.lib.server.database;

import java.lang.reflect.Field;

public class DBField {

    private final boolean readOnly;
    private final Field field;

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
