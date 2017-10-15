package com.erp.lib.server.servlet;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.erp.lib.server.config.Configuration;
import com.erp.lib.server.database.DataSource;
import com.erp.lib.server.servlet.annotations.POST_FILE;
import com.erp.lib.server.servlet.annotations.GET;
import com.erp.lib.server.servlet.annotations.DELETE;
import com.erp.lib.server.servlet.annotations.GET_WITH_PARAMS;
import com.erp.lib.server.servlet.annotations.PUT;
import com.erp.lib.server.servlet.annotations.POST;
import com.erp.lib.server.servlet.annotations.GET_WITH_ID;
import com.erp.lib.server.servlet.annotations.Path;
import com.erp.lib.server.exception.JsonException;
import com.erp.lib.server.exception.TextException;
import com.erp.lib.server.exception.UnauthenticatedException;
import com.erp.lib.server.i18n.I18n;
import com.erp.lib.server.security.annotation.Secured;
import com.erp.lib.server.exception.UnauthorizedException;
import com.erp.lib.server.json.JacksonObjectMapper;
import com.erp.lib.server.reports.PdfDocument;
import com.erp.lib.server.routing.FileRequest;
import com.erp.lib.server.routing.FileResponse;
import com.erp.lib.server.routing.RequestFormat;
import com.erp.lib.server.routing.RequestType;
import com.erp.lib.server.routing.ResponseType;
import static com.erp.lib.server.routing.ResponseType.FILE;
import static com.erp.lib.server.routing.ResponseType.PDF;
import static com.erp.lib.server.routing.ResponseType.TEXT;
import com.erp.lib.server.routing.Route;
import com.erp.lib.server.servlet.annotations.Produces;
import com.erp.lib.server.utils.PackageUtil;
import com.erp.lib.server.utils.StringUtil;
import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfWriter;
import com.erp.lib.server.exception.NotFoundException;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.ExpiredSessionException;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class RestServlet extends HttpServlet {

    @Inject
    private Configuration configuration;

    @Inject
    private Injector injector;

    @Inject
    private JacksonObjectMapper jacksonObjectMapper;

    @Inject
    private DataSource dataSource;

    @Inject
    private I18n i18n;

    private final Map<String, Route> routesGET = new HashMap();
    private final Map<String, Route> routesGET_WITH_ID = new HashMap();
    private final Map<String, Route> routesGET_WITH_PARAMS = new HashMap();

    private final Map<String, Route> routesPOST = new HashMap();
    private final Map<String, Route> routesPOST_FILE = new HashMap();

    private final Map<String, Route> routesPUT = new HashMap();

    private final Map<String, Route> routesDELETE = new HashMap();

    private final Pattern urlRegExpId = Pattern.compile(".*\\/[0-9]{1,}$");

    private final Logger logger = LoggerFactory.getLogger(RestServlet.class);

    protected String getMessage(String message) {
        return i18n.getMessage(message);
    }

    private void initializeDataSource() {
        dataSource.init(
                configuration.getDatabaseServer(),
                configuration.getDatabaseName(),
                configuration.getDatabaseUsername(),
                configuration.getDatabasePassword()
        );
    }

    private void destroyDataSource() {
        dataSource.destroy();
    }

    private void scanServicePackages() throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("Class: " + this.getClass().getName() + " - initializing routes ...");
        }

        List<Class> scannedClasses = new ArrayList();

        for (String packageName : configuration.getServicePackages()) {
            scannedClasses.addAll(PackageUtil.scanPackage(packageName));
        }

        scannedClasses.forEach((clazz) -> {
            String basePath = "";
            boolean secured = false;
            String[] classAuthorities = {};

            Object instance = injector.getInstance(clazz);
            Path classPath = (Path) clazz.getAnnotation(Path.class);

            if (classPath != null) {
                basePath = classPath.value();
            }

            Secured classSecured = (Secured) clazz.getAnnotation(Secured.class);

            if (classSecured != null) {
                secured = true;
                classAuthorities = classSecured.value();
            }

            for (Method method : clazz.getDeclaredMethods()) {
                String currentPath = basePath;
                boolean currentSecured = secured;
                String[] currentAuthorities = classAuthorities;

                Path methodPath = method.getAnnotation(Path.class);

                if (methodPath != null) {
                    currentPath += methodPath.value();
                }

                Secured methodSecured = method.getAnnotation(Secured.class);

                if (methodSecured != null) {
                    currentSecured = true;
                    currentAuthorities = methodSecured.value();
                }

                if (logger.isDebugEnabled()) {
                    logger.debug("Class: " + clazz.getName() + " - Method:" + method.getName() + ":=" + StringUtil.join(currentAuthorities, ",") + " Class:" + clazz.getName());
                }

                if (currentPath.length() > 0) {
                    ResponseType responseType;

                    if (method.getAnnotation(Produces.class) != null) {
                        responseType = method.getAnnotation(Produces.class).value();
                    } else {
                        responseType = ResponseType.JSON;
                    }

                    if (method.getAnnotation(GET.class) != null) {
                        routesGET.put(currentPath, new Route(RequestType.GET, responseType, instance, method, currentSecured, currentAuthorities));
                    } else if (method.getAnnotation(GET_WITH_ID.class) != null) {
                        routesGET_WITH_ID.put(currentPath, new Route(RequestType.GET_WITH_ID, responseType, instance, method, currentSecured, currentAuthorities));
                    } else if (method.getAnnotation(GET_WITH_PARAMS.class) != null) {
                        routesGET_WITH_PARAMS.put(currentPath, new Route(RequestType.GET_WITH_PARAMS, responseType, instance, method, currentSecured, currentAuthorities));
                    } else if (method.getAnnotation(POST.class) != null) {
                        routesPOST.put(currentPath, new Route(RequestType.POST, responseType, instance, method, currentSecured, currentAuthorities));
                    } else if (method.getAnnotation(POST_FILE.class) != null) {
                        routesPOST_FILE.put(currentPath, new Route(RequestType.POST_FILE, responseType, instance, method, currentSecured, currentAuthorities));
                    } else if (method.getAnnotation(PUT.class) != null) {
                        routesPUT.put(currentPath, new Route(RequestType.PUT, responseType, instance, method, currentSecured, currentAuthorities));
                    } else if (method.getAnnotation(DELETE.class) != null) {
                        routesDELETE.put(currentPath, new Route(RequestType.DELETE, responseType, instance, method, currentSecured, currentAuthorities));
                    }

                }

            }
        });
    }

    @Override
    public void init() throws ServletException {
        try {
            initializeDataSource();
            scanServicePackages();
        } catch (Exception ex) {
            logger.error("Class: " + this.getClass().getName() + " - Unable to initialize servlet");
            ex.printStackTrace();
        }
    }

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) {
        String pathInfo = request.getPathInfo();
        RequestFormat requestFormat = RequestFormat.OTHER;

        if (request.getContentType() != null) {
            if (request.getContentType().contains("application/json")) {
                requestFormat = RequestFormat.JSON;
            } else if (request.getContentType().contains("text/plain")) {
                requestFormat = RequestFormat.TEXT;
            } else if (request.getContentType().contains("text/html")) {
                requestFormat = RequestFormat.HTML;
            }
        }

        if (pathInfo.charAt(pathInfo.length() - 1) == '/') {
            pathInfo = pathInfo.substring(0, pathInfo.length() - 1);
        }

        Route routeFinalized = null;
        Object result = null;

        try {
            switch (request.getMethod()) {
                case "GET":
                    if (routesGET.containsKey(pathInfo)) {
                        routeFinalized = routesGET.get(pathInfo);
                        authorize(routeFinalized);
                        result = routeFinalized.execute();
                        break;
                    }

                    if (routesGET_WITH_PARAMS.containsKey(pathInfo)) {
                        routeFinalized = routesGET_WITH_PARAMS.get(pathInfo);
                        authorize(routeFinalized);
                        result = routeFinalized.executeWithMap(request.getParameterMap());
                        break;
                    }

                    if (urlRegExpId.matcher(pathInfo).matches()) {
                        int startIndex = pathInfo.lastIndexOf("/");
                        int lastIndex = pathInfo.length();

                        String url = pathInfo.substring(0, startIndex);

                        if (routesGET_WITH_ID.containsKey(url)) {
                            routeFinalized = routesGET_WITH_ID.get(url);
                            authorize(routeFinalized);
                            int id = new Integer(pathInfo.substring(startIndex + 1, lastIndex));
                            result = routeFinalized.execute(id);
                            break;
                        }
                    }
                    break;

                case "POST":
                    if (routesPOST.containsKey(pathInfo)) {
                        routeFinalized = routesPOST.get(pathInfo);
                        authorize(routeFinalized);
                        Object body = jacksonObjectMapper.readValue(request.getInputStream(), routeFinalized.getParamType());
                        result = routeFinalized.execute(body);
                    } else if (routesPOST_FILE.containsKey(pathInfo)) {
                        routeFinalized = routesPOST_FILE.get(pathInfo);
                        authorize(routeFinalized);

                        try {
                            if (ServletFileUpload.isMultipartContent(request)) {
                                FileItemFactory factory = new DiskFileItemFactory();
                                ServletFileUpload upload = new ServletFileUpload(factory);
                                List<FileItem> fileItems = upload.parseRequest(request);
                                Map<String, Object> parameters = new HashMap();
                                List<FileItem> files = new ArrayList();

                                for (FileItem fileItem : fileItems) {
                                    if (fileItem.isFormField()) {
                                        if (fileItem.getFieldName().compareTo("parameters") == 0) {
                                            parameters = jacksonObjectMapper.readValue(fileItem.getString(), Map.class);
                                        }
                                    } else {
                                        files.add(fileItem);
                                    }
                                }

                                result = routeFinalized.execute(new FileRequest(parameters, files));
                            } else {
                                throw new JsonException("file.uploading.error");
                            }
                        } catch (FileUploadException ex) {
                            throw new JsonException("file.uploading.error");
                        }
                    }

                    break;

                case "PUT":
                    if (routesPUT.containsKey(pathInfo)) {
                        routeFinalized = routesPUT.get(pathInfo);
                        authorize(routeFinalized);
                        Object body = jacksonObjectMapper.readValue(request.getInputStream(), routeFinalized.getParamType());
                        result = routeFinalized.execute(body);
                    }
                    break;

                case "DELETE":
                    if (urlRegExpId.matcher(pathInfo).matches()) {
                        int startIndex = pathInfo.lastIndexOf("/");
                        int lastIndex = pathInfo.length();
                        String url = pathInfo.substring(0, startIndex);

                        if (routesDELETE.containsKey(url)) {
                            routeFinalized = routesDELETE.get(url);
                            authorize(routeFinalized);
                            int id = new Integer(pathInfo.substring(startIndex + 1, lastIndex));
                            result = routeFinalized.execute(id);
                            break;
                        }
                    }
                    break;

            }

            if (routeFinalized != null) {
                switch (routeFinalized.getResponseType()) {
                    case JSON:
                        response.setContentType("application/json; charset=utf-8");
                        response.setStatus(HttpServletResponse.SC_OK);
                        if (result != null) {
                            jacksonObjectMapper.writeValue(response.getOutputStream(), result);
                        }
                        break;

                    case TEXT:
                        response.setContentType("text/plain; charset=utf-8");
                        response.setStatus(HttpServletResponse.SC_OK);
                        response.getOutputStream().print(result.toString());
                        break;

                    case PDF:
                        response.setContentType("application/pdf;charset=UTF-8");
                        response.setStatus(HttpServletResponse.SC_OK);
                        PdfDocument pdfDocument = (PdfDocument) result;
                        Document document = pdfDocument.getDocument();
                        PdfWriter writer = PdfWriter.getInstance(document, response.getOutputStream());
                        pdfDocument.onBeforeBuildDocument(writer, document);
                        pdfDocument.prepareWriter(writer);
                        document.open();
                        pdfDocument.buildPdfDocument(writer, document);
                        response.addHeader("Content-Disposition", "attachment; filename=\"" + pdfDocument.getFileName() + ".pdf\"");
                        document.close();
                        break;

                    case FILE:
                        FileResponse fileResponse = (FileResponse) result;
                        File file = new File(configuration.getStorageDirectory() + fileResponse.getFullFilename());

                        if (!file.exists()) {
                            throw new TextException("file.notFound.error");
                        }

                        try (ServletOutputStream os = response.getOutputStream(); DataInputStream is = new DataInputStream(new FileInputStream(file));) {
                            response.setContentType((fileResponse.getMimetype() != null) ? fileResponse.getMimetype() : "application/octet-stream");
                            response.setContentLength((int) file.length());
                            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileResponse.getOutputFilename() + "\"");

                            byte[] writeBuffer = new byte[configuration.getWriteBufferSize()];
                            int length = 0;
                            while ((is != null) && ((length = is.read(writeBuffer)) != -1)) {
                                os.write(writeBuffer, 0, length);
                            }

                            os.flush();
                        } catch (Exception ex) {
                            throw new JsonException("file.download.error");
                        }
                        break;
                }

            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (TextException ex) {
            writeTextException(response, ex);
        } catch (JsonException ex) {
            writeJsonException(routeFinalized, response, ex);
        } catch (UnauthenticatedException exception) {
            if (requestFormat == RequestFormat.JSON) {
                writeJsonException(routeFinalized, response, new JsonException(HttpServletResponse.SC_FORBIDDEN, "access.error"));
            } else {
                writeTextException(response, new TextException(HttpServletResponse.SC_FORBIDDEN, "access.error"));
            }
        } catch (ExpiredSessionException exception) {
            if (requestFormat == RequestFormat.JSON) {
                Map<String, Object> fields = new HashMap();
                fields.put("sessionExpired", true);
                writeJsonException(routeFinalized, response, new JsonException(HttpServletResponse.SC_UNAUTHORIZED, "access.error", fields));
            } else {
                writeTextException(response, new TextException(HttpServletResponse.SC_UNAUTHORIZED, "access.error"));
            }
        } catch (UnauthorizedException exception) {
            if (requestFormat == RequestFormat.JSON) {
                writeJsonException(routeFinalized, response, new JsonException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "access.error"));
            } else {
                writeTextException(response, new TextException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "access.error"));
            }
        } catch (InvocationTargetException exception) {
            Throwable targetException = exception.getTargetException();

            if (targetException instanceof JsonException) {
                writeJsonException(routeFinalized, response, (JsonException) targetException);
            } else if (targetException instanceof TextException) {
                writeTextException(response, (TextException) targetException);
            } else if (targetException instanceof NotFoundException) {
                if (requestFormat == RequestFormat.JSON) {
                    writeJsonException(routeFinalized, response, new JsonException(HttpServletResponse.SC_NOT_FOUND, targetException.getMessage()));
                } else {
                    writeTextException(response, new TextException(HttpServletResponse.SC_NOT_FOUND, targetException.getMessage()));
                }
            } else {
                if (requestFormat == RequestFormat.JSON) {
                    writeJsonException(routeFinalized, response, new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "server.error"));
                } else {
                    writeTextException(response, new TextException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "server.error"));
                }

                if (targetException instanceof BatchUpdateException) {
                    ((BatchUpdateException) targetException).getNextException().printStackTrace();
                } else {
                    exception.printStackTrace();
                }
            }
        } catch (Exception exception) {
            if (requestFormat == RequestFormat.JSON) {
                writeJsonException(routeFinalized, response, new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "server.error"));
            } else {
                writeTextException(response, new TextException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "server.error"));
            }

            exception.printStackTrace();
        }

        Object connectionAttribute = request.getAttribute("connection");

        if (connectionAttribute != null) {
            try {
                ((Connection) connectionAttribute).close();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void writeTextException(HttpServletResponse response, TextException textException) {
        response.setContentType("text/plain;charset=UTF-8");
        response.setStatus(textException.getStatus());

        try {
            response.getWriter().print(getMessage(textException.getMessage()));
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void writeJsonException(Route route, HttpServletResponse response, JsonException jsonException) {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(jsonException.getStatus());

        Map<String, Object> error = new HashMap();
        error.put("error", true);
        error.put("status", jsonException.getStatus());

        if (!jsonException.getMessage().isEmpty()) {
            error.put("message", getMessage(jsonException.getMessage()));
        }

        if (!jsonException.getFields().isEmpty()) {
            Map<String, Object> fields = new HashMap();

            jsonException.getFields().keySet().forEach((key) -> {
                Object value = jsonException.getFields().get(key);
                if (value instanceof String) {
                    fields.put(key, getMessage((String) value));
                } else {
                    fields.put(key, value);
                }
            });

            error.put("fields", fields);
        }

        try {
            jacksonObjectMapper.writeValue(response.getOutputStream(), error);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void authorize(Route route) throws Exception {
        Subject user = SecurityUtils.getSubject();
        boolean authorityFound = false;

        if (!user.isAuthenticated() && route.isSecured()) {
            throw new UnauthenticatedException();
        }

        if (route.getAuthorites().length > 0) {
            for (String authority : route.getAuthorites()) {
                if (user.hasRole(authority)) {
                    authorityFound = true;
                    break;
                }
            }

            if (!authorityFound) {
                throw new UnauthorizedException();
            }
        }
    }
}
