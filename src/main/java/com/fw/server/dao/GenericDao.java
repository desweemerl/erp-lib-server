package com.fw.server.dao;

import com.google.inject.Inject;
import com.fw.server.database.DBTable;
import com.fw.server.database.DBTableComparator;
import com.fw.server.database.DBTables;
import com.fw.server.database.DataSource;
import com.fw.server.json.request.ListRequest;
import com.fw.server.json.response.ListResponse;
import com.fw.server.table.GenericTable;
import com.fw.server.utils.IntegerUtil;
import com.fw.server.utils.StringUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class GenericDao {

    @Inject
    private DataSource dataSource;
    
    @Inject
    private DBTables DBTables;
    

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
    
    public void activateLocale(boolean enabled) throws SQLException {
        dataSource.activateLocale(enabled);
    }    
    
    public String getConnectionLocale() {
        return dataSource.getConnectionLocale();
    }
    
    public void saveTransactionState() throws SQLException {
        dataSource.saveTransactionState();
    }    

    public void restoreTransactionState() throws SQLException {
        dataSource.restoreTransactionState();
    }

    public void beginTransaction() throws SQLException {
        getConnection().setAutoCommit(false);
    }

    public void commit() throws SQLException {
        getConnection().commit();
    }

    public void rollback() throws SQLException {
        getConnection().rollback();
    }        
    
    protected Object executeFunction(String functionName) throws SQLException {

        Object result = null;

        try (PreparedStatement ps = getConnection().prepareStatement("SELECT " + functionName + "()")) {
            ps.executeQuery();
            ResultSet rs = ps.getResultSet();
            if ( rs.next() ) {
                result = rs.getObject(1);
            } 
            return result;
        }

    }

    protected Object executeFunction(String functionName, Object[] parameters) throws SQLException {

        int n, l;
        Object result = null;

        StringBuilder SQL = new StringBuilder("SELECT ").append(functionName).append("(");

        for (n = 0, l = parameters.length - 1; n <= l; n++) {
            SQL.append("?");
            if (n < l) {
                SQL.append(",");
            }
        }

        SQL.append(")");

        try (PreparedStatement ps = getConnection().prepareStatement(SQL.toString())) {
            for (n = 0; n <= l; n++) {
                ps.setObject(n + 1, parameters[n]);
            }
            ps.executeQuery();
            ResultSet rs = ps.getResultSet();
            if ( rs.next() ) {
                result = rs.getObject(1);
            }
            return result; 
        }

    }

    protected ListResponse getPaginatedList(Class tableClass, String condition, ListRequest request) throws Exception {

        DBTable table = DBTables.getDBTable(tableClass);
        return table.select(getConnection(), condition, request);

    }

    protected GenericTable getRecord(Class tableClass, Integer id) throws Exception {

        DBTable table = DBTables.getDBTable(tableClass);
        return table.select(getConnection(), id);

    }

    protected Object getRecord(Class tableClass, String condition, Object[] values) throws Exception {

        DBTable table = DBTables.getDBTable(tableClass);
        return table.selectFirst(getConnection(), condition, values);

    }

    protected List getRecords(Class tableClass, String condition, Object[] values) throws Exception {

        DBTable table = DBTables.getDBTable(tableClass);
        return table.select(getConnection(), condition, values);

    }

    protected List getRecords(Class tableClass, String condition, Object[] values, String order, boolean orderAsc) throws Exception {

        DBTable table = DBTables.getDBTable(tableClass);
        return table.select(getConnection(), condition, values, order, orderAsc);

    }

    protected List getRecords(Class tableClass, String condition, Object[] values, String[] orders, boolean[] ordersAsc) throws Exception {

        DBTable table = DBTables.getDBTable(tableClass);
        return table.select(getConnection(), condition, values, orders, ordersAsc);

    }

    protected List getRecordsForUpdate(Class tableClass, String condition, Object[] values) throws Exception {

        DBTable table = DBTables.getDBTable(tableClass);
        return table.selectForUpdate(getConnection(), condition, values);

    }
    
    protected Object getFirstRecord(Class tableClass) throws Exception {

        DBTable table = DBTables.getDBTable(tableClass);
        return table.selectFirst(getConnection());

    }    

    protected List getAllRecords(Class tableClass) throws Exception {

        DBTable table = DBTables.getDBTable(tableClass);
        return table.selectAll(getConnection());

    }

    protected List getAllRecords(Class tableClass, String order, boolean orderAsc) throws Exception {

        DBTable table = DBTables.getDBTable(tableClass);
        return table.selectAll(getConnection(), order, orderAsc);

    }

    protected long countRecords(Class tableClass, String condition, Object[] values) throws Exception {

        DBTable table = DBTables.getDBTable(tableClass);
        return table.count(getConnection(), condition, values);

    }

    protected GenericTable insertRecord(GenericTable genericTable) throws Exception {

        DBTable table = DBTables.getDBTable(genericTable.getClass());
        return table.insert(getConnection(), genericTable);

    }
    
    protected Map insertRecord(String tableName, Map<String, Object> mapInput, String[]fieldsReturned) throws Exception {
        
        Set keys = mapInput.keySet(); 
        Collection<Object> values = mapInput.values();
        int size = keys.size();    
        Map mapOutput = new HashMap<String, Object>();

        StringBuilder SQL = new StringBuilder("INSERT INTO ")
                .append(tableName).append(" (")
                .append( StringUtil.join(keys, ",") )
                .append(") VALUES (")
                .append( StringUtil.repeat("?", ",", size) )
                .append(")");       

        try (PreparedStatement ps = getConnection().prepareStatement(SQL.toString())) {

            Iterator it = values.iterator();
            int n = 1;
            
            while (it.hasNext()) {
                ps.setObject(n, it.next());
                n++;
            }

            ps.execute();
            ResultSet rs = ps.getResultSet();            
            
            if (rs.next()) {
                for (String field : fieldsReturned) {
                    mapOutput.put(field, rs.getObject(field));
                }               
            }

        }
        
        return mapOutput;

    }

    protected int[] batchInsert(List<GenericTable> genericTables) throws Exception {
        
        if (genericTables.size() > 0) {
            
            Class clazz = genericTables.get(0).getClass();

            DBTable table = DBTables.getDBTable(clazz);
            return table.batchInsert(getConnection(), genericTables);            
            
        }
        
        return null;

    }

    protected GenericTable updateRecord(GenericTable genericTable) throws Exception {

        DBTable table = DBTables.getDBTable(genericTable.getClass());
        return table.update(getConnection(), genericTable);

    }

    protected int[] batchUpdate(List<GenericTable> genericTables) throws Exception {
        
        if (genericTables.size() > 0) {
        
            Class clazz = genericTables.get(0).getClass();   

            DBTable table = DBTables.getDBTable(clazz);
            return table.batchUpdate(getConnection(), genericTables);
            
        }
        
        return null;

    }
    
    protected void saveSubRecords(Class tableClass, Map<String, Integer> conditions, List<GenericTable> genericTables) throws Exception {
        
        if (genericTables == null) return;
        
        int n, l;

        DBTable table = DBTables.getDBTable(tableClass);
        List<GenericTable> genericTablesBkp = new ArrayList(genericTables);

        List<GenericTable> tablesForUpdate = new ArrayList();
        List<GenericTable> tablesForInsert = new ArrayList();
        List<Integer> idsForDelete = new ArrayList();
        
        String[] fields = new String[conditions.size()];
        Integer[] values = new Integer[conditions.size()];
        
        n = 0;
        
        for (Map.Entry<String, Integer> entry : conditions.entrySet()) {
            fields[n] = entry.getKey();
            values[n] = entry.getValue();
            n++;
        }

        StringBuilder SQL = new StringBuilder();
        
        for (n = 0, l = fields.length; n < l; n++) {
            
            SQL.append(fields[n]);
            SQL.append(" = ?");

            if (n < (l - 1)) {
                SQL.append(" AND ");
            }   
            
        }

        List<? extends GenericTable> existingTables = getRecordsForUpdate(tableClass, SQL.toString(), values);

        for (GenericTable genericTableBkp : genericTablesBkp) {
            
            for (n = 0, l = fields.length; n < l; n++) { 
                table.setValue(genericTableBkp, fields[n], values[n]);
            }                    
        }        

        for (GenericTable existingTable : existingTables) {

            boolean found = false;

            for (GenericTable genericTableBkp : genericTablesBkp) {
                
                if (genericTableBkp.getId() != null) {
                    if (IntegerUtil.isEqual(existingTable.getId(), genericTableBkp.getId())) {                 
                        tablesForUpdate.add(genericTableBkp);
                        genericTablesBkp.remove(genericTableBkp);
                        found = true;
                        break;  
                    }
                }

            }

            if (!found) {
                idsForDelete.add(existingTable.getId());
            }

        }    

        for (GenericTable genericTableBkp : genericTablesBkp) {
            tablesForInsert.add(genericTableBkp);
        }  
        
        if (idsForDelete.size() > 0) {
            batchDelete(table.getName(), idsForDelete);
        }           

        if (tablesForUpdate.size() > 0) {
            batchUpdate(tablesForUpdate);
        }

        if (tablesForInsert.size() > 0) {
            batchInsert(tablesForInsert);
        } 
        
    }   
    
    protected void saveSubRecords(Class tableClass, Map<String, Integer> conditions, List<GenericTable> genericTables, DBTableComparator dbTableComparator) throws Exception {
        
        if (genericTables == null) return;        
        
        int n, l;

        DBTable table = DBTables.getDBTable(tableClass);
        List<GenericTable> genericTablesBkp = new ArrayList(genericTables);        

        List<GenericTable> tablesForUpdate = new ArrayList();
        List<GenericTable> tablesForInsert = new ArrayList();
        List<Integer> idsForDelete = new ArrayList();
        
        String[] fields = new String[conditions.size()];
        Integer[] values = new Integer[conditions.size()];
        
        n = 0;
        
        for (Map.Entry<String, Integer> entry : conditions.entrySet()) {
            fields[n] = entry.getKey();
            values[n] = entry.getValue();
            n++;
        }

        StringBuilder SQL = new StringBuilder();
        
        for (n = 0, l = fields.length; n < l; n++) {
            
            SQL.append(fields[n]);
            SQL.append(" = ?");

            if (n < (l - 1)) {
                SQL.append(" AND ");
            }   
            
        }

        List<GenericTable> existingTables = getRecordsForUpdate(tableClass, SQL.toString(), values);

        for (GenericTable genericTableBkp : genericTablesBkp) {
            
            for (n = 0, l = fields.length; n < l; n++) { 
                table.setValue(genericTableBkp, fields[n], values[n]);
            }                    
        }        

        for (GenericTable existingTable : existingTables) {

            boolean found = false;

            for (GenericTable genericTableBkp : genericTablesBkp) {

                if (dbTableComparator.comparator(existingTable, genericTableBkp)) {
                 
                    genericTableBkp.setId(existingTable.getId());
                    tablesForUpdate.add(genericTableBkp);
                    genericTablesBkp.remove(genericTableBkp);
                    found = true;
                    break;
                    
                }

            }

            if (!found) {
                idsForDelete.add(existingTable.getId());
            }

        }    

        for (GenericTable genericTableBkp : genericTablesBkp) {
            tablesForInsert.add(genericTableBkp);
        } 
        
        if (idsForDelete.size() > 0) {
            batchDelete(table.getName(), idsForDelete);
        }        

        if (tablesForUpdate.size() > 0) {
            batchUpdate(tablesForUpdate);
        }
        
        if (tablesForInsert.size() > 0) {
            batchInsert(tablesForInsert);
        } 
        
    }
  
    
    protected void saveSubRecords(Class conditionTableClass, Map<String, Integer> conditions, Class tableClass, List<GenericTable> genericTables, DBTableComparator dbTableComparator) throws Exception {
        
        if (genericTables == null) return;
        
        int n, l;

        DBTable table = DBTables.getDBTable(tableClass);
        List<GenericTable> genericTablesBkp = new ArrayList(genericTables);                

        List<GenericTable> tablesForUpdate = new ArrayList();
        List<GenericTable> tablesForInsert = new ArrayList();
        List<Integer> idsForDelete = new ArrayList();
 
        String[] fields = new String[conditions.size()];
        Integer[] values = new Integer[conditions.size()];
        
        List<String> fieldsToStore = new ArrayList();        
        List<Integer> valuesToStore = new ArrayList();
        
        n = 0;        
        
        for (Map.Entry<String, Integer> entry : conditions.entrySet()) {
            
            String field = entry.getKey();
            Integer value = entry.getValue();            
            fields[n] = field;
            values[n] = value;
            
            if (table.hasFieldName(field)) {
                fieldsToStore.add(field);
                valuesToStore.add(value);
            }    
            
            n++;
            
        }

        StringBuilder SQL = new StringBuilder();
        
        for (n = 0, l = fields.length; n < l; n++) {
            
            SQL.append(fields[n]);
            SQL.append(" = ?");

            if (n < (l - 1)) {
                SQL.append(" AND ");
            }   
            
        }

        List<GenericTable> existingTables = getRecordsForUpdate(conditionTableClass, SQL.toString(), values);
        
        for (GenericTable genericTableBkp : genericTablesBkp) { 
            
            for (n = 0, l = fieldsToStore.size(); n < l; n++) { 
                table.setValue(genericTableBkp, fieldsToStore.get(n), valuesToStore.get(n));
            }
            
        }                

        for (GenericTable existingTable : existingTables) {

            boolean found = false;

            for (GenericTable genericTableBkp : genericTablesBkp) {

                if (dbTableComparator.comparator(existingTable, genericTableBkp)) {
                 
                    tablesForUpdate.add(genericTableBkp);
                    genericTablesBkp.remove(genericTableBkp);
                    found = true;
                    break;
                    
                }

            }

            if (!found) {
                idsForDelete.add(existingTable.getId());
            }

        }    

        for (GenericTable genericTableBkp : genericTablesBkp) {
            tablesForInsert.add(genericTableBkp);
        }             

        if (idsForDelete.size() > 0) {
            batchDelete(table.getName(), idsForDelete);
        }        

        if (tablesForUpdate.size() > 0) {
            batchUpdate(tablesForUpdate);
        }

        if (tablesForInsert.size() > 0) {
            batchInsert(tablesForInsert);
        } 
        
    }

    protected void deleteRecord(GenericTable genericTable) throws Exception {

        DBTable table = DBTables.getDBTable(genericTable.getClass());
        table.delete(getConnection(), genericTable);

    }
    
    protected int[] batchDelete(List<GenericTable> genericTables) throws Exception {
        
        if (genericTables.size() > 0) {
            
            Class clazz = genericTables.get(0).getClass();        
            
            DBTable table = DBTables.getDBTable(clazz);
            return table.batchDelete(getConnection(), genericTables);
            
        }
        
        return null;

    } 
    
    public int[] batchDelete(String tableName, List<Integer> ids) throws SQLException {

        try (PreparedStatement ps = getConnection().prepareStatement("DELETE FROM " + tableName + " WHERE id =  ?")) {

            for (Integer id : ids) {
                if (id != null) {
                    ps.setInt(1, id);            
                    ps.addBatch();           
                }
            }
            
            return ps.executeBatch();

        }

    }       

    protected void deleteRecord(Class tableClass, int id) throws Exception {

        DBTable table = DBTables.getDBTable(tableClass);
        table.delete(getConnection(), "id = ?", new Integer[]{id});

    }

    protected void deleteRecords(Class tableClass, String condition, Object[] values) throws Exception {

        DBTable table = DBTables.getDBTable(tableClass);
        table.delete(getConnection(), condition, values);

    }

    protected void deleteRecords(String tableName, String condition, Object[] values) throws Exception {

        Connection connection = getConnection();
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM " + tableName + " WHERE " + condition)) {

            for (int n = 0; n < values.length; n++) {
                ps.setObject(n + 1, values[n]);
            }

            ps.executeUpdate();

        }

    }
}
