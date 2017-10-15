package com.erp.lib.server.routing;

public class FileResponse {

    private final String subDirectory;
    private final String filename;
    private final String outputFilename;
    private final String mimetype;

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
        String sd = subDirectory;
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
