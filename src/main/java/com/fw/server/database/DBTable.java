package com.fw.server.database;

import com.fw.server.database.annotation.Field;
import com.fw.server.database.annotation.Table;
import com.fw.server.table.GenericTable;

import com.fw.server.json.request.ListRequest;
import com.fw.server.json.response.ListResponse;
import com.fw.server.utils.StringUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DBTable {

    private Class genericTableClass;
    private String name;
    private Map<String, DBField> dbFields = new HashMap();
    private Map<String, String> dbFieldNameClass = new HashMap();
    private List<String> fieldsNames = new ArrayList();
    private Pattern namedCriteriaPattern = Pattern.compile("\\%\\([a-zA-Z]{1,}\\)s");

    public DBTable(Class genericTableClass, String name) {
        
        String fieldName;       
        
        this.genericTableClass = genericTableClass;
        this.name = name;
        
        Class currentClass = genericTableClass;

        while (currentClass != null) {

            for (java.lang.reflect.Field field :  currentClass.getDeclaredFields()) {
                
                field.setAccessible(true);
                Field fieldAnnotation = field.getAnnotation(Field.class);
                
                if (fieldAnnotation != null) {
                    fieldName = fieldAnnotation.value();
                    dbFields.put( fieldName, new DBField( field, fieldAnnotation.readOnly() ));
                    dbFieldNameClass.put(field.getName(), fieldName);
                    fieldsNames.add(fieldName);
                }
                
            }            

            currentClass = currentClass.getSuperclass();

        }        

    }

    public Class getGenericTableClass() {
        return genericTableClass;
    }

    public String getName() {
        return name;
    }

    
    public boolean hasFieldName(String fieldName) {

        for (String name : this.fieldsNames) {
            if (name.compareTo(fieldName) == 0) {
                return true;
            }
        }
        
        return false;
        
    }

    public Map<String, DBField> getDBFields() {
        return dbFields;
    }
    
    public Object getDBField(String fieldName) {
        return dbFields.get(fieldName);
    }    

    private Object mapFirstRow(ResultSet rs) throws Exception {

        Object result = null;

        if (rs.next()) {

            result = genericTableClass.newInstance();
            for (String fieldName : dbFields.keySet())  {
                
                    DBField dbField = dbFields.get(fieldName);
                    dbField.getField().set(result, rs.getObject(fieldName));
            }            

        }

        return result;

    }

    private List mapAllRows(ResultSet rs) throws Exception {

        List result = new ArrayList();

        while (rs.next()) {

            Object genericTable = genericTableClass.newInstance();
            for (String fieldName : dbFields.keySet()) {
                
                DBField dbField = dbFields.get(fieldName);
                dbField.getField().set(genericTable, rs.getObject(fieldName));
            }            

            result.add(genericTable);

        }

        return result;

    }

    public GenericTable select(Connection connection, int id) throws Exception {

        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM " + name + " WHERE id = ?")) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            
            GenericTable genericTable = (GenericTable)mapFirstRow(rs);

            return genericTable;

        }

    }

    public List select(Connection connection, String condition, Object[] values) throws Exception {

        StringBuilder SQL = new StringBuilder("SELECT * FROM ").append(name);
        boolean withCriteria = false;
        
        if (condition != null) {
            if (condition.length() > 0) {
                withCriteria = true;
                SQL.append(" WHERE ").append(condition);
            }
        }

        try ( PreparedStatement ps = connection.prepareStatement(SQL.toString()) ) {

            if (withCriteria) {
                for (int n = 0; n < values.length; n++) {
                    ps.setObject(n + 1, values[n]);
                }
            }

            ResultSet rs = ps.executeQuery();
            return mapAllRows(rs);

        }

    }
    
    public long count(Connection connection, String condition, Object[] values) throws Exception {

        StringBuilder SQL = new StringBuilder("SELECT COUNT(*) FROM ").append(name);
        boolean withCriteria = false;

        if (condition != null) {
            if (condition.length() > 0) {
                withCriteria = true;
                SQL.append(" WHERE ").append(condition);
            }
        }

        try (PreparedStatement ps = connection.prepareStatement(SQL.toString())) {
            
            long count = 0L;

            if (withCriteria) {
                for (int n = 0; n < values.length; n++) {
                    ps.setObject(n + 1, values[n]);
                }
            }

            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                count = rs.getLong(1);
            }
            
            return count;

        }        

    }    

    public List select(Connection connection, String condition, Object[] values, String order, boolean orderAsc) throws Exception {       

        StringBuilder SQL = new StringBuilder("SELECT * FROM ").append(name);
        boolean withCriteria = false;        
        String orderFieldName;
        
        if (condition != null) {
            if (condition.length() > 0) {
                withCriteria = true;
                SQL.append(" WHERE ").append(condition);
            }
        }       
        
        if (order != null) {
            
            orderFieldName = this.dbFieldNameClass.get(order);                
            if ( orderFieldName == null ) throw new Exception("ERROR on select arguments: order field name '" + order + "' is not registered");              
            SQL.append(" ORDER BY ").append(orderFieldName).append(orderAsc ? " ASC" : " DESC");
            
        }
       
        try ( PreparedStatement ps = connection.prepareStatement(SQL.toString()) ){

            if (withCriteria) {
                for (int n = 0; n < values.length; n++) {
                    ps.setObject(n + 1, values[n]);
                }
            }

            ResultSet rs = ps.executeQuery();
            
            return mapAllRows(rs);

        }

    } 
    
    public List select(Connection connection, String condition, Object[] values, String[] orders, boolean[] ordersAsc) throws Exception {
        
        int n, l;
        StringBuilder SQL = new StringBuilder("SELECT * FROM ").append(name);
        boolean withCriteria = false;
        boolean withOrder = false;
        List<String> ordersStr = new ArrayList();
        String orderFieldName;        

        if (condition != null) {
            if (condition.length() > 0) {
                withCriteria = true;
                SQL.append(" WHERE ").append(condition);
            }
        }   
        
        if ( (l = orders.length) != ordersAsc.length) throw new Exception("ERROR on select arguments: orders and ordersAsc must be of the same size");
        
        for (n = 0; n < l; n++) {
            
            if (orders[n] != null) {
                
                withOrder = true;
                orderFieldName = this.dbFieldNameClass.get(orders[n]);     
                if ( orderFieldName == null ) throw new Exception("ERROR on select arguments: order field name '" + orders[n] + "' is not registered");              
                ordersStr.add( orderFieldName + (ordersAsc[n] ? " ASC": " DESC") );
                
            }   
            
        }
        
        if (withOrder) {
            SQL.append(" ORDER BY ");
            SQL.append(StringUtil.join(ordersStr, ", "));
        }

        try (PreparedStatement ps = connection.prepareStatement(SQL.toString())) {

            if (withCriteria) {
                for (n = 0; n < values.length; n++) {
                    ps.setObject(n + 1, values[n]);
                }
            }

            ResultSet rs = ps.executeQuery();
            
            return mapAllRows(rs);

        }

    }       
    

    public List selectForUpdate(Connection connection, String condition, Object[] values) throws Exception {

        StringBuilder SQL = new StringBuilder("SELECT * FROM ").append(name);
        boolean withCriteria = false;

        if (condition != null) {
            if (condition.length() > 0) {
                withCriteria = true;
                SQL.append(" WHERE ").append(condition);
            }
        }   

        SQL.append(" FOR UPDATE");

        try (PreparedStatement ps = connection.prepareStatement( SQL.toString()) ) {

            if (withCriteria) {
                for (int n = 0; n < values.length; n++) {
                    ps.setObject(n + 1, values[n]);
                }
            }

            ResultSet rs = ps.executeQuery();
            
            return mapAllRows(rs);

        }

    } 
    
    public Object selectFirst(Connection connection) throws Exception {

        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM " + name)) {

            ps.setFetchSize(1);
            ResultSet rs = ps.executeQuery();
            
            return mapFirstRow(rs);            

        }

    }    

    public Object selectFirst(Connection connection, String condition, Object[] values) throws Exception {

        StringBuilder SQL = new StringBuilder("SELECT * FROM ").append(name);
        boolean withCriteria = false;

        if (condition != null) {
            if (condition.length() > 0) {
                withCriteria = true;
                SQL.append(" WHERE ").append(condition);
            }
        }   

        try (PreparedStatement ps = connection.prepareStatement(SQL.toString())) {

            if (withCriteria) {
                for (int n = 0; n < values.length; n++) {
                    ps.setObject(n + 1, values[n]);
                }
            }

            ps.setFetchSize(1);
            ResultSet rs = ps.executeQuery();
            
            return mapFirstRow(rs);            

        }

    }

    public ListResponse select(Connection connection, String condition, ListRequest request) throws Exception {

        int n, l;
        StringBuilder countSQL = new StringBuilder("SELECT COUNT(*) FROM ").append(name);
        StringBuilder querySQL = new StringBuilder("SELECT * FROM ").append(name);
        boolean withCriteria = false;
        ListResponse response = new ListResponse();
        Object[] valuesArray = null;

        if (condition != null) {

            if (condition.length() > 0) {

                withCriteria = true;
                List values = new ArrayList();

                Matcher matcher = namedCriteriaPattern.matcher(condition);

                while ( matcher.find() ) {

                    String group = matcher.group();
                    String paramName = group.substring(2, group.length() - 2);

                    if (request.getParams().containsKey(paramName)) {
                        values.add(request.getParams().get(paramName));
                    } else {
                        values.add(null);
                    }

                }       

                String criteria = matcher.replaceAll("?");
                countSQL.append(" WHERE ").append(criteria);
                querySQL.append(" WHERE ").append(criteria);

                valuesArray = values.toArray();

            }

        }

        Integer size = request.getSize();

        if (size > 100) {
            size = 100;
        }
        response.setSize(size);

        try ( PreparedStatement ps1 = connection.prepareStatement( countSQL.toString() ) ) {

            long count = 0L;

            if (withCriteria) {
                for (n = 0; n < valuesArray.length; n++) {
                    ps1.setObject(n + 1, valuesArray[n]);
                }
            }   

            ResultSet rs1 = ps1.executeQuery();

            if (rs1.next()) {
                count = rs1.getLong(1);
            }
            rs1.close();

            if (count > 0) {

                boolean withOrder = false;
                Integer offset = 0,
                        page = request.getPage(),
                        maxPages = ((Double) (Math.ceil(new Float(count) / new Float(size)))).intValue();
                String order;
                Boolean orderAsc = null;
                List<String> orders = null;
                List<Boolean> ordersAsc = null;
                List<String> ordersStr = new ArrayList();
                String orderFieldName = null;
                

                if ( (size * (page - 1)) >= count ) {
                    page = maxPages;
                }
                
                offset = (page - 1) * size;

                order = request.getOrder();
                orders = request.getOrders();
                
                if (order != null) {
                    
                    orderFieldName = this.dbFieldNameClass.get(order);                
                    if ( orderFieldName == null ) throw new Exception("ERROR on select arguments: order field name '" + order + "' is not registered");                                                        
                    
                    orderAsc = request.getOrderAsc();
                    if (orderAsc == null) {
                        orderAsc = true;
                    }
                    
                    querySQL.append(" ORDER BY ").append(orderFieldName).append(orderAsc ? " ASC" : " DESC");
                    
                } else if ( ( l = orders.size() ) > 0 ) {
                    
                    ordersAsc = request.getOrdersAsc();
                    if ( ordersAsc.size() != l ) throw new Exception("ERROR on select arguments: orders and ordersAsc must be of the same size");
                    
                    for (n = 0; n < l; n++) {
            
                        if ( ( order = orders.get(n) ) != null ) {
                            withOrder = true;
                            orderFieldName = this.dbFieldNameClass.get(order);                     
                            if ( orderFieldName == null ) throw new Exception("ERROR on select arguments: order field name '" + order + "' is not registered");                             
                            ordersStr.add( orderFieldName + ( ordersAsc.get(n) ? " ASC": " DESC") );
                        }
                        
                    }
                    
                    if (withOrder) {
                        querySQL.append(" ORDER BY ");
                        querySQL.append(StringUtil.join(ordersStr, ", "));
                    }
                    
                }

                querySQL.append(" OFFSET ").append(String.valueOf(offset)).append(" LIMIT ").append(String.valueOf(size));

                try ( PreparedStatement ps2 = connection.prepareStatement( querySQL.toString() ) ) {

                    if (withCriteria) {
                        for (n = 0; n < valuesArray.length; n++) {
                            ps2.setObject(n + 1, valuesArray[n]);
                        }
                    }

                    ResultSet rs2 = ps2.executeQuery();
                    List<GenericTable> list = mapAllRows(rs2);
                    response.setList(list);
                    response.setCount(count);
                    response.setMaxPages(maxPages);
                    response.setOrder(order);
                    response.setOrderAsc(orderAsc);
                    response.setPage(page);

                    return response;

                }

            } else {

                return response;

            }

        }

    }

    public List selectAll(Connection connection) throws Exception {

        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM " + name)) {

            ResultSet rs = ps.executeQuery();
            
            return mapAllRows(rs);

        }

    }    
   
    public void setValue(GenericTable genericTable, String fieldName, Object value) throws Exception {
        
        
        DBField dbField = dbFields.get(fieldName);   
        dbField.getField().set(genericTable, value);
        
    }
    
    public Object getValue(GenericTable genericTable, String fieldName) throws Exception {
        
        DBField dbField = dbFields.get(fieldName);   
        return dbField.getField().get(genericTable);
        
    } 

    public List selectAll(Connection connection, String order, boolean orderAsc) throws Exception {
        
        StringBuilder SQL = new StringBuilder("SELECT * FROM ").append(name);
        String orderFieldName;
        
        if (order != null) {
            
            orderFieldName = this.dbFieldNameClass.get(order);                 
            if ( orderFieldName == null ) throw new Exception("ERROR on select arguments: order field name '" + order + "' is not registered");              
            SQL.append(" ORDER BY ").append(orderFieldName).append(orderAsc ? " ASC" : " DESC");
                        
        }
        
        try (PreparedStatement ps = connection.prepareStatement(SQL.toString())) {

            ResultSet rs = ps.executeQuery();
            
            return mapAllRows(rs);            

        }
        
    }
    
    public GenericTable update(Connection connection, GenericTable genericTable) throws Exception {

        Iterator it;
        GenericTable result = null;
        List<String> parameters = new ArrayList();
        List<Object> values = new ArrayList();   

        it = dbFields.keySet().iterator();

        while (it.hasNext()) {

            String fieldName = (String)it.next();
            DBField dbField = dbFields.get(fieldName);

            if ( ( !dbField.isReadOnly() ) && ( !fieldName.equals("id") ) ) {

                parameters.add(fieldName + "= ?");
                values.add(dbField.getField().get(genericTable));

            }

        }

        StringBuilder SQL = new StringBuilder("UPDATE ")
                .append(name).append(" SET ")
                .append( StringUtil.join(parameters, ",") )
                .append(" WHERE id = ?")
                .append(" RETURNING ").append( StringUtil.join(fieldsNames, ",") );
        
        values.add(genericTable.getId());

        try (PreparedStatement ps = connection.prepareStatement(SQL.toString())) {

            it = values.iterator();
            int n = 1;
            
            while (it.hasNext()) {
                ps.setObject(n, it.next());
                n++;
            }

            ps.execute();   
            result = (GenericTable)mapFirstRow( ps.getResultSet() );

        }
        
        return result;

    }
    
    public int[] batchUpdate(Connection connection, List<GenericTable> genericTables) throws Exception {

        String fieldName;
        Iterator<String> it;        
        DBField dbField;
        
        List<String> parameters = new ArrayList();
        it = dbFields.keySet().iterator();

        while (it.hasNext()) {

            fieldName = it.next();
            dbField = dbFields.get(fieldName);
            
            if ( ( !dbField.isReadOnly() ) && ( !fieldName.equals("id") ) ) {
                parameters.add(fieldName + "= ?");
            }

        }

        StringBuilder SQL = new StringBuilder("UPDATE ").append(name).append(" SET ").append(StringUtil.join(parameters, ",")).append(" WHERE id = ?");

        try (PreparedStatement ps = connection.prepareStatement(SQL.toString())) {
            
            for (GenericTable genericTable : genericTables) {            

                it = dbFields.keySet().iterator();
                int n = 1;
                Integer id = null;

                while (it.hasNext()) {
                    
                    fieldName = it.next();
                    dbField = dbFields.get(fieldName);
                    
                    if (fieldName.equals("id"))  {
                        id = (Integer)dbField.getField().get(genericTable);
                    } else if (!dbField.isReadOnly()) {
                        ps.setObject(n, dbField.getField().get(genericTable));
                        n++;
                    }
                    
                }
                
                ps.setInt(n, id);
                ps.addBatch();

            }
            
            return ps.executeBatch();
            
        }

    }
    

    public GenericTable insert(Connection connection, GenericTable genericTable) throws Exception {

        Iterator it;
        GenericTable result = null;
        List<String> parameters = new ArrayList();
        List<Object> values = new ArrayList();

        it = dbFields.keySet().iterator();

        while (it.hasNext()) {

            String fieldName =(String)it.next();
            DBField dbField = dbFields.get(fieldName);

            if ( ( !dbField.isReadOnly() ) && ( !fieldName.equals("id") ) ) {

                parameters.add(fieldName);
                values.add(dbField.getField().get(genericTable));

            }

        }

        StringBuilder SQL = new StringBuilder("INSERT INTO ")
                .append(name).append(" (")
                .append( StringUtil.join(parameters, ",") )
                .append(") VALUES (")
                .append( StringUtil.repeat("?", ",", parameters.size()) )
                .append(")")
                .append(" RETURNING ").append( StringUtil.join(fieldsNames, ",") );

        try (PreparedStatement ps = connection.prepareCall(SQL.toString())) {

            it = values.iterator();
            int n = 1;
            
            while (it.hasNext()) {
                ps.setObject(n, it.next());
                n++;
            }

            ps.execute();
            result = (GenericTable)mapFirstRow( ps.getResultSet() );

        }
        
        return result;

    }
    
    public int[] batchInsert(Connection connection, List<GenericTable> genericTables) throws Exception {

        String fieldName;
        Iterator<String> it;
        DBField dbField;

        
        boolean autoCommit = connection.getAutoCommit();
        
        if (autoCommit) {
            connection.setAutoCommit(false);
        }

        List<String> parameters = new ArrayList();
        it = dbFields.keySet().iterator();

        while (it.hasNext()) {

            fieldName = it.next();
            dbField = dbFields.get(fieldName);

            if (!dbField.isReadOnly() && (!fieldName.equals("id"))) {
                parameters.add(fieldName);
            }
            
        }

        StringBuilder SQL = new StringBuilder("INSERT INTO ").append(name).append(" (").append(StringUtil.join(parameters, ",")).append(") VALUES (").append(StringUtil.repeat("?", ",", parameters.size())).append(")");

        try (PreparedStatement ps = connection.prepareStatement(SQL.toString())) {
                
            for (GenericTable genericTable : genericTables) {
                
                it = dbFields.keySet().iterator();  
                int n = 1;

                while (it.hasNext()) {

                    fieldName = it.next();
                    dbField = dbFields.get(fieldName);

                    if (!dbField.isReadOnly() && (!fieldName.equals("id"))) {
                        ps.setObject(n, dbField.getField().get(genericTable));
                        n++;
                    }

                }
                
                ps.addBatch();
                
            }
                
            return ps.executeBatch();

        } finally {

            connection.setAutoCommit(autoCommit);

        }
        

    }    

    public void delete(Connection connection, GenericTable genericTable) throws Exception {

        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM " + name + " WHERE id =  ?")) {

            ps.setInt(1, genericTable.getId());            
            ps.executeUpdate();            

        }

    }

    public int[] batchDelete(Connection connection, List<GenericTable> genericTables) throws Exception {

        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM " + name + " WHERE id =  ?")) {

            for (GenericTable genericTable : genericTables) {
                ps.setInt(1, genericTable.getId());            
                ps.addBatch();            
            }
            
            return ps.executeBatch();

        }

    }    

    public void delete(Connection connection, String condition, Object[] values) throws Exception {
        
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM " + name + " WHERE " + condition)) {

            for (int n = 0; n < values.length; n++) {
                ps.setObject(n + 1, values[n]);
            }

            ps.executeUpdate();

        }        

    }
    
}
