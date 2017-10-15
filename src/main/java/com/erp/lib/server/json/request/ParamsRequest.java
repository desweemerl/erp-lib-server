package com.erp.lib.server.json.request;

import java.util.Map;
import org.codehaus.jackson.annotate.JsonIgnore;

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
        return params != null
                ? params.containsKey(parameter)
                : false;
    }

    @JsonIgnore
    public Object getParam(String parameter) {
        return params.get(parameter);
    }
}
