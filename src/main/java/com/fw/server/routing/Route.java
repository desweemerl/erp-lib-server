/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fw.server.routing;

import com.fw.server.servlet.annotations.PARAM;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import org.codehaus.jackson.map.ObjectMapper;

public class Route {

    private Method method;
    private RequestType requestType;
    private ResponseType responseType;    
    private Class returnType;
    private Class paramType;
    private Object instance;
    private boolean secured;
    private String[] authorities;        
    private Parameter[] parameters;
    
    private class Parameter {
        Class type;
        String value;
    }
    
    

    public Route(RequestType requestType, ResponseType responseType, Object instance, Method method, boolean secured, String[] authorities) {

        this.requestType = requestType;
        this.responseType = responseType;
        this.instance = instance;
        this.method = method;       
        this.returnType = method.getReturnType();
        this.responseType = responseType;
        
        if ( (requestType == RequestType.PUT) || (requestType == RequestType.POST) ) {            
            this.paramType = method.getParameterTypes()[0];            
        }
        
        this.secured = secured;
        this.authorities = authorities;
        Class[] types = method.getParameterTypes(); 
        parameters = new Parameter[types.length];
        int n = 0;
        
        for (Annotation[] annotations :  method.getParameterAnnotations()) {   
            
            Parameter parameter = new Parameter();
            parameter.type = types[n];
            
            for (Annotation annotation : annotations) {
                if (annotation instanceof PARAM) {
                    PARAM param = ((PARAM)annotation);
                    if (!param.value().isEmpty()) parameter.value = param.value();
                    break;
                }
            }
            
            parameters[n] = parameter;
            n++;
            
        }       

    }  

    public Object execute() throws Exception {
        return method.invoke(instance);
    }

    public Object execute(int id) throws Exception {
        return method.invoke(instance, id);
    }
    
    public Object execute(Object parameter) throws Exception {
        return method.invoke(instance, parameter);
    }    
    
    public Object executeWithMap(Map<String, String[]> requestParam) throws Exception {
        
        Object[] args = new Object[this.parameters.length];
       
        for (int n = 0, l = this.parameters.length; n < l; n++) {

            args[n] = null;
            Parameter parameter = this.parameters[n];            
            
            if (requestParam.containsKey(parameter.value)) {
                
                String[] values = requestParam.get(parameter.value);

                if (values.length > 0) {
                    if (parameter.type == String.class) {
                        args[n] = values[0];
                    } else if (parameter.type == int.class) {
                        args[n] = Integer.parseInt(values[0]);
                    } else if (parameter.type == Boolean.class) {
                        args[n] = Boolean.parseBoolean(values[0]);
                    } else if (parameter.type == List.class) {
                        args[n] = new ObjectMapper().readValue(values[0], List.class);
                    }
                }
                
            }
            
        }
        
        return method.invoke(instance, args);
        
    }

    public boolean isSecured() {
        return secured;
    }    
    
    public String[] getAuthorites() {
        return authorities;
    }
    
    public ResponseType getResponseType() {
        return responseType;
    }    
    
    public RequestType getRequestType() {
        return requestType;
    }

    public Class getParamType() {
        return paramType;
    }

    public Class getReturnType() {
        return returnType;
    }
   
    public boolean hasParameters() {
        return (parameters.length > 0);
    }    
   
}