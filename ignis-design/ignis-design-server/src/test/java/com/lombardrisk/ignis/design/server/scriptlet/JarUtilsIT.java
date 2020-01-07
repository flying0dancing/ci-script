package com.lombardrisk.ignis.design.server.scriptlet;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.apache.commons.io.FileUtils.copyDirectory;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.assertj.core.api.Assertions.assertThat;

public class JarUtilsIT {

    private static final String JAR_FILE = "ignis-spark-script-demo.jar";
    private String scriptletPath;

    private JarUtils jarUtils;

    @Before
    public void setUp() throws IOException {
        jarUtils = new JarUtils();

        scriptletPath = "target/scriptlets";
        File scriptletJarsDir = new File(scriptletPath).getAbsoluteFile();

        deleteQuietly(scriptletJarsDir);
        copyDirectory(new File("src/test/resources/scriptlets"), scriptletJarsDir);
    }

    @Test
    public void getJarFiles_invalidPath_returnsOptionalEmpty() {
        Optional<List<String>> result = jarUtils.getJarFiles("INVALID_PATH");

        assertThat(result).isEmpty();
    }

    @Test
    public void getJarFiles_validScriptletPath_returnsJarFiles() {
        Optional<List<String>> result = jarUtils.getJarFiles(scriptletPath);

        assertThat(result).isNotEmpty();
        assertThat(result.get().size()).isEqualTo(1);
        assertThat(result.get().get(0)).isEqualTo("ignis-spark-script-demo.jar");
    }

    @Test
    public void getJarClasses_withValidJarFileAndValidPath_returnsJarClassList() {
        Optional<List<String>> result = jarUtils.getJarClasses(scriptletPath + File.separator + JAR_FILE);

        assertThat(result).isNotEmpty();
        assertThat(result.get().size()).isEqualTo(5);
        assertThat(result).contains(asList(
                "com.lombardrisk.ignis.spark.script.demo.AggregateUsersScriptlet",
                "com.lombardrisk.ignis.spark.script.demo.Test1",
                "com.lombardrisk.ignis.spark.script.demo.Test2",
                "com.lombardrisk.ignis.spark.script.demo.TotalTradesByTrader",
                "com.lombardrisk.ignis.spark.script.api.Scriptlet"));
    }

    @Test
    public void getJarClasses_withInvalidJarFile_returnsOptionalEmpty() {
        Optional<List<String>> result = jarUtils.getJarClasses(scriptletPath + File.separator + "myJar.jar");

        assertThat(result).isEmpty();
    }

    @Test
    public void getJarClasses_withInvalidPath_returnsOptionalEmpty() {
        Optional<List<String>> result = jarUtils.getJarClasses("INVALID_PATH" + File.separator + JAR_FILE);

        assertThat(result).isEmpty();
    }

    @Test
    public void loadClassByJAR_loadsClass() {
        Optional<Class<?>> result = jarUtils.loadClassByJAR(
                JAR_FILE,
                scriptletPath,
                "com.lombardrisk.ignis.spark.script.api.Scriptlet");

        assertThat(result).isNotEmpty();
        assertThat(result.get()).isInstanceOf(Class.class);
        assertThat(result.get().getName()).isEqualTo("com.lombardrisk.ignis.spark.script.api.Scriptlet");
    }

    @Test
    public void loadClassByJAR_classNotFound_returnsOptionalEmpty() {
        Optional<Class<?>> result = jarUtils.loadClassByJAR(
                JAR_FILE,
                scriptletPath,
                "not.exist.Scriptlet");

        assertThat(result).isEmpty();
    }

    @Test
    public void loadClassByClassLoader_returnsLoadedClass() {
        Optional<Class<?>> mainClass = Optional.of(Number.class);

        Optional<Class<?>> result = jarUtils.loadClassByClassLoader(
                "java.lang.Long",
                mainClass.get().getClassLoader());

        assertThat(result).isNotEmpty();
        assertThat(result.get()).isInstanceOf(Class.class);
        assertThat(result.get().getName()).isEqualTo("java.lang.Long");
    }

    @Test
    public void findMethodByName_returnsMethod() {
        Optional<Class<?>> classLoader = Optional.of(Long.class);
        Optional<Method> result = jarUtils.findMethodByName("min", classLoader.get());

        assertThat(result).isNotEmpty();
        assertThat(result.get().getName()).isEqualTo("min");
    }

    @Test
    public void getMethodMetadata_errorFound_returnsOptionalEmpty() {
        Optional<Class<?>> classLoader = Optional.of(Long.class);
        Optional<Method> method = Arrays.stream(classLoader.get().getMethods()).findFirst();
        Optional<Object> result = jarUtils.getMethodMetadata(classLoader.get(), method.get());
        assertThat(result).isEmpty();
    }

    @Test
    public void getMethodMetadata_returnsMethodMetadata() {
        Map<String, List<String>> map = new HashMap<>();
        map.put("A", asList("A", "B"));

        Method method = map.getClass().getMethods()[4];
        Optional<Object> result = jarUtils.getMethodMetadata(map.getClass(), method);
        assertThat(result).isNotEmpty();
    }
}