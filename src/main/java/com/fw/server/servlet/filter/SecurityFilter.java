package com.fw.server.servlet.filter;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.apache.shiro.web.servlet.AbstractShiroFilter;

@Singleton
public class SecurityFilter extends AbstractShiroFilter {

    @Inject
    public SecurityFilter(WebSecurityManager securityManager) {      
        setSecurityManager(securityManager);        
    }
    
}
