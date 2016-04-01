package com.fw.server.database;

public abstract class DBTableComparator<GenericTable> {
    
    public abstract boolean comparator (GenericTable existingTable, GenericTable genericTable);
    
}
