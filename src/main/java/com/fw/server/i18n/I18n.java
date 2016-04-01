package com.fw.server.i18n;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.fw.server.config.Configuration;
import java.util.Arrays;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import javax.servlet.http.HttpServletRequest;

public class I18n {    
      
    private Provider<HttpServletRequest> httpServletRequestProvider;
    
    @Inject
    private Configuration configuration;    
    
    @Inject    
    public I18n(Provider<HttpServletRequest> httpServletRequestProvider) {
        
        this.httpServletRequestProvider = httpServletRequestProvider;

    }
    
    public String getMessage(String message) {

        String language = (String)httpServletRequestProvider.get().getAttribute("language");

        return getMessage(message, language);

    }

    public String getMessage(String message, String language) {

        String finalMessage;

        try {

            if (Arrays.binarySearch(configuration.getAvailableLanguages(), language) == -1) {
                language = configuration.getDefaultLanguage();
            }

            ResourceBundle resourceBundle = resourceBundle = PropertyResourceBundle.getBundle(configuration.getI18nPackage(), new Locale(language));
            finalMessage = resourceBundle.getString(message);

        } catch (MissingResourceException ex) {
            
            finalMessage = message;
            
        }

        return finalMessage;

    }

    public String getLanguage() {

        return (String)httpServletRequestProvider.get().getAttribute("language");

    }

    public String setLanguage(String language) {

        if (Arrays.binarySearch(configuration.getAvailableLanguages() , language) == -1) {
            language = configuration.getDefaultLanguage();
        }

        httpServletRequestProvider.get().setAttribute("language", language);
        
        return language;

    }    
    
}
