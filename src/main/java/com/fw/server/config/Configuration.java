package com.fw.server.config;


public interface Configuration {
    
    String getDatabaseServer();    
    String getDatabaseUsername();    
    String getDatabasePassword(); 
    String getDatabaseName();
    
    String getI18nPackage();
    String getI18nReportPackage();    
    String[] getServicePackages();    
    String[] getModelPackages();    
    
    String getStorageDirectory();
    String getDefaultLanguage();    
    String[] getAvailableLanguages();    
    Integer getWriteBufferSize();
    
}