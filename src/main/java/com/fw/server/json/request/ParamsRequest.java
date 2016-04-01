/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fw.server.json.request;

import java.util.Map;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 *
 * @author root
 */
public class ParamsRequest extends SimpleRequest {
    
    private Map<String, Object> params;    

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }   
    
    @JsonIgnore
    public boolean paramExists(String parameter) {
        
        if (params != null) {
            
            return params.containsKey(parameter);
            
        }
        
        return false; 
    }
 
    @JsonIgnore
    public Object getParam(String parameter) {
        
        return params.get(parameter);
        
    }
    
}
