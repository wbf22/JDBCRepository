package org.jdbc.repository.classscan;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class ClassScanner {

    public static List<Class<?>> scanClasses(String basePackage) {
        return scanClasses(basePackage, null);
    }

    public static List<Class<?>> scanClasses(String basePackage, Filter filter) {
        List<Class<?>> classes = new ArrayList<>();

        String basePackagePath = basePackage.replace('.', '/');
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            Enumeration<URL> enumUrl = classLoader.getResources(basePackagePath);
            while (enumUrl.hasMoreElements()) {
                URL packageUrl = enumUrl.nextElement();
                if (packageUrl != null && packageUrl.getProtocol().equals("file")) {
                    File packageDir = new File(packageUrl.getFile());
                    if (packageDir.isDirectory()) {
                        scanDirectory(basePackage, packageDir, classes, filter);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        return classes;
    }

    private static void scanDirectory(String basePackage, File packageDir, List<Class<?>> interfaces, Filter filter) {
        File[] files = packageDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    String subPackage = basePackage + "." + file.getName();
                    scanDirectory(subPackage, file, interfaces, filter);
                } else if (file.getName().endsWith(".class")) {
                    String className = basePackage + "." + file.getName().replace(".class", "");
                    Class<?> clazz = null;
                    try {
                        clazz = Class.forName(className);
                        if (filter.isWanted(clazz)) {
                            interfaces.add(clazz);
                        }
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }

                }
            }
        }
    }


}
