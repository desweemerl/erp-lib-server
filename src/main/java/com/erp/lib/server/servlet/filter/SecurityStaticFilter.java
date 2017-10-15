package com.erp.lib.server.servlet.filter;

import com.google.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

@Singleton
public class SecurityStaticFilter implements Filter {

    private Map<String, List<String>> authorizations = new HashMap();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        List<String> roles = null;
        boolean found = false;

        for (String url : authorizations.keySet()) {
            if (request.getServletPath().startsWith(url)) {
                roles = authorizations.get(url);
                found = true;
                break;
            }
        }

        if (found) {
            HttpServletResponse response = (HttpServletResponse) servletResponse;
            Subject subject = SecurityUtils.getSubject();

            if (subject.isAuthenticated()) {
                boolean[] rolesBol = subject.hasRoles(roles);
                boolean roleFound = false;

                for (boolean roleBol : rolesBol) {
                    if (roleBol) {
                        roleFound = true;
                        break;
                    }
                }

                if (roleFound) {
                    //      response.setHeader("Expires", "Sat, 6 May 1995 12:00:00 GMT");
                    //      response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate");
                    //      response.addHeader("Cache-Control", "post-check=0, pre-check=0");
                    //      response.setHeader("Pragma", "no-cache");
                    chain.doFilter(servletRequest, servletResponse);
                    return;
                }
            }

            response.getOutputStream().print("Access forbidden");
            response.setContentType("text/plain; charset=utf-8");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        chain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void init(FilterConfig config) throws ServletException {
        for (Enumeration e = config.getInitParameterNames(); e.hasMoreElements();) {
            String url = (String) e.nextElement();
            String roles = config.getInitParameter(url);
            List<String> rolesList = new ArrayList();

            if (roles != null) {
                String[] rolesAr = roles.split(",");

                for (String role : rolesAr) {
                    rolesList.add(role.trim());
                }

                authorizations.put(url, rolesList);
            }
        }
    }

    @Override
    public void destroy() {
    }
}
