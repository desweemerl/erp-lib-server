package com.fw.server.table;

import com.fw.server.database.annotation.Field;

public class GenericTable {

    @Field(value = "id")
    private Integer id;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}
