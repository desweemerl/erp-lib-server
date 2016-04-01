package com.fw.server.servlet.filter;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.fw.server.config.Configuration;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.apache.shiro.SecurityUtils;

@Singleton
public class LanguageFilter implements Filter {
    
    @Inject
    Configuration configuration;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        
        String language = configuration.getDefaultLanguage();
       
        if (SecurityUtils.getSubject().isAuthenticated()) {
            language = (String)SecurityUtils.getSubject().getSession().getAttribute("language");
        }

        request.setAttribute("language", language);        
        chain.doFilter(request, response);
        
    }

    @Override
    public void init(FilterConfig fc) throws ServletException {        
    }    

    @Override
    public void destroy() {
    }
}
