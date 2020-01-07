package com.lombardrisk.ignis.design.server.scriptlet;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
public class JarUtils {

    public Optional<List<String>> getJarFiles(final String jarsPath) {
        File file = new File(jarsPath);
        if (!file.exists() || !file.isDirectory()) {
            log.error("Invalid scriptlet path [{}]", jarsPath);
            return Optional.empty();
        }

        try {
            return Optional.of(Files.walk(Paths.get(jarsPath))
                    .filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".jar"))
                    .map(x -> x.getFileName().toString())
                    .collect(Collectors.toList()));
        } catch (IOException e) {
            log.error("Could not read jars repository", e);
            return Optional.empty();
        }
    }

    public Optional<List<String>> getJarClasses(final String jarPathName) {
        File file = new File(jarPathName);
        if (!file.exists() || file.isDirectory()) {
            return Optional.empty();
        }

        try {
            return Optional.of(readJarClasses(new ZipInputStream(new FileInputStream(jarPathName))));
        } catch (FileNotFoundException e) {
            log.error("Jar file not exist [{}]", jarPathName, e);
        } catch (IOException e) {
            log.error("Error when open jar file [{}]", jarPathName, e);
        }
        return Optional.empty();
    }

    public Optional<Class<?>> loadClassByJAR(
            final String jarName,
            final String jarPath,
            final String classname) {
        try {
            final URL url = new URL("jar:file:" + jarPath + "/" + jarName + "!/");
            URLClassLoader ucl = new URLClassLoader(new URL[]{ url });
            return Optional.of(ucl.loadClass(classname));
        } catch (MalformedURLException | ClassNotFoundException e) {
            log.error("Error when load class [{}] by jar [{}]", classname, jarName, e);
        }
        return Optional.empty();
    }

    public Optional<Class<?>> loadClassByClassLoader(final String classname, final ClassLoader classLoader) {
        try {
            return Optional.of(Class.forName(classname, true, classLoader));
        } catch (ClassNotFoundException e) {
            log.error("Error when load class [{}] by class loader", classname, e);
        }
        return Optional.empty();
    }

    public Optional<Method> findMethodByName(final String methodName, final Class<?> classLoader) {
        try {
            return Arrays.stream(classLoader.getMethods())
                    .filter(m -> m.getName().equals(methodName))
                    .findFirst();
        } catch (Exception e) {
            log.error("Method not found [{}]", methodName, e);
        }
        return Optional.empty();
    }

    public Optional<Object> getMethodMetadata(
            final Class<?> myMainClass,
            final Method method) {

        try {
            return Optional.of(method.invoke(myMainClass.newInstance()));
        } catch (Exception e) {
            log.error("Error when extract metadata for method [{}]", method.getName(), e);
        }
        return Optional.empty();
    }

    private static List<String> readJarClasses(final ZipInputStream zip) throws IOException {
        List<String> classNames = new ArrayList<>();
        for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
            if (!entry.isDirectory() && entry.getName().endsWith(".class")) {

                String className = entry.getName().replace('/', '.'); // including ".class"
                classNames.add(className.substring(0, className.length() - ".class".length()));
            }
        }
        return classNames;
    }
}
