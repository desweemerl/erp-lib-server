package com.fw.server.routing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.fileupload.FileItem;

public class FileRequest {
    
    private Map<String, Object> parameters;
    private List<FileItem> files;
    
    public FileRequest() {        
        this(new HashMap(), new ArrayList());        
    }    
    
    public FileRequest(Map<String, Object> parameters) {        
        this(parameters, new ArrayList());        
    }
    
    public FileRequest(Map<String, Object> parameters, List<FileItem> fileItems) {        
        this.parameters = parameters;
        this.files = fileItems;        
    }
    
    public Map<String, Object> getParameters() {
        return parameters;        
    }
    
    public Object getParameter(String parameter) {
        return parameters.get(parameter);
    }
    
    public List<FileItem> getFiles() {
        return files;
    }
}
