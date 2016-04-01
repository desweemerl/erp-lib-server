package com.fw.server.utils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class PackageUtil {

    public static List<Class> scanPackage(String packageName) throws IOException, ClassNotFoundException {

        List<Class> classes = new ArrayList();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        if (classLoader != null) {

            List<File> directories = new ArrayList();
            Enumeration<URL> resources = classLoader.getResources(packageName.replace(".", "/"));

            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                directories.add(new File(URLDecoder.decode(resource.getFile(), "UTF-8")));
            }

            for (File directory : directories) {
                classes.addAll(findClasses(directory, packageName));
            }

        }

        return classes;
    }

    private static List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {

        List<Class> classes = new ArrayList();

        if (!directory.exists()) {
            return classes;
        }

        for (File file : directory.listFiles()) {

            String fileName = file.getName();

            if (file.isDirectory()) {
                classes.addAll(findClasses(file, packageName + "." + fileName));
            } else if (fileName.endsWith(".class") && !fileName.contains("$")) {
                Class clazz = Class.forName(packageName + '.' + fileName.substring(0, fileName.length() - 6));
                classes.add(clazz);
            }

        }

        return classes;
    }
}
