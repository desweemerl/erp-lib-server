package com.fw.server.routing;

public class FileResponse {
    
    private String subDirectory;
    private String filename;
    private String outputFilename;
    private String mimetype;
    
    public FileResponse(String subDirectory, String filename, String mimetype) {
        this.subDirectory = subDirectory;
        this.filename = filename;
        this.outputFilename = filename;
        this.mimetype = mimetype;
    }
    
    public FileResponse(String subDirectory, String filename, String outputFilename, String mimetype) {
        this.subDirectory = subDirectory;
        this.filename = filename;
        this.outputFilename = outputFilename;
        this.mimetype = mimetype;
    }

    public String getFullFilename() {
        
        String sd  = subDirectory;
        if (sd.charAt(sd.length() - 1) != '/') {
            sd += "/";
        }
        
        return sd + filename;
        
    }

    public String getSubDirectory() {
        return subDirectory;
    }

    public String getFilename() {
        return filename;
    }

    public String getOutputFilename() {
        return outputFilename;
    }    
    
    public String getMimetype() {
        return mimetype;
    }
    
}
