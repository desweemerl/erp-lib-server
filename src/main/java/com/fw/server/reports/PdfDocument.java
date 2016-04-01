
package com.fw.server.reports;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfWriter;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public abstract class PdfDocument {    
    
    private String fileName = "download";
    private String language = "en";
    
    private Map model;     
    
    public abstract Document getDocument();    
    public abstract String getPropertyBundle();
    
    public void buildPdfDocument(PdfWriter writer, Document document) throws Exception  {
        
    }
    
    public void setLanguage(String language) {        
        this.language = language;        
    }       
    
    public String getLanguage() {
        return this.language;
    }
       
    public void setModel(Map model) {
        this.model = model;
    }    
    
    public Map getModel() {
        return model;
    }   
   
    public void prepareWriter(PdfWriter writer) {
        writer.setViewerPreferences(PdfWriter.ALLOW_PRINTING | PdfWriter.PageLayoutSinglePage);
    }  
    
    public void onBeforeBuildDocument(PdfWriter writer, Document document) throws Exception {
        
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }   
    
    public String getMessage(String message) {        
        
        String finalMessage;
        
        try {
            
            ResourceBundle resourceBundle = PropertyResourceBundle.getBundle(getPropertyBundle(), new Locale(this.language));
            finalMessage = resourceBundle.getString(message);
            
        } catch (MissingResourceException ex) {
            
            finalMessage = message;      
            
        }
        
        return finalMessage;
        
    }       
    
}
