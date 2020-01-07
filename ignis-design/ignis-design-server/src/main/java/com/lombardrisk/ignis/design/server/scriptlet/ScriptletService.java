package com.lombardrisk.ignis.design.server.scriptlet;

import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import io.vavr.control.Validation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.types.StructType;
import org.springframework.stereotype.Service;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class ScriptletService {

    final private String scriptletPath;
    final private String scriptletInterface;
    final private String inputMethodName;
    final private String outputMethodName;
    final private JarUtils jarUtils;

    public Validation<ErrorResponse, List<String>> getScriptletJars() {
        Optional<List<String>> jarFiles = jarUtils.getJarFiles(scriptletPath);

        if (!jarFiles.isPresent()) {
            return Validation.invalid(ScriptletErrorResponse.FailedToReadScriptletPath(scriptletPath));
        }

        return Validation.valid(jarFiles.get().stream()
                .filter(jar -> jarUtils.loadClassByJAR(jar, scriptletPath, scriptletInterface).isPresent())
                .collect(Collectors.toList()));
    }

    public Validation<ErrorResponse, List<String>> getScriptletClasses(final String jarFile) {
        Optional<Class<?>> interfaceClass = jarUtils.loadClassByJAR(jarFile, scriptletPath, scriptletInterface);
        if (!interfaceClass.isPresent()) {
            return Validation.invalid(ScriptletErrorResponse.FailedToLoadScriptletInterface(jarFile));
        }

        Optional<List<String>> classes = jarUtils.getJarClasses(scriptletPath + File.separator + jarFile);
        if (!classes.isPresent()) {
            return Validation.invalid(ScriptletErrorResponse.FailedToReadJarClasses("jarFile"));
        }

        List<String> classNames = new ArrayList<>();
        for (String classname : classes.get()) {
            if (!classname.equals(scriptletInterface)) {
                final Optional<Class<?>> loadedClass = jarUtils.loadClassByClassLoader(
                        classname,
                        interfaceClass.get().getClassLoader());

                if (loadedClass.isPresent() &&
                        interfaceClass.get().isAssignableFrom(loadedClass.get())) {
                    classNames.add(classname);
                }
            }
        }
        return Validation.valid(classNames);
    }

    public Validation<ErrorResponse, Map<String, List<StructTypeFieldView>>> getScriptletInputMetadata(
            final String jarFile, final String className) {

        final Optional<Class<?>> loadedClass = jarUtils.loadClassByJAR(jarFile, scriptletPath, className);
        if (!loadedClass.isPresent()) {
            return Validation.invalid(ScriptletErrorResponse.FailedToLoadScriptletClass(jarFile, className));
        }

        Validation<ErrorResponse, Map<String, StructType>> inputMethodMetadata =
                getInputMethodMetadataStructType(loadedClass.get());
        if (inputMethodMetadata.isInvalid()) {
            return Validation.invalid(inputMethodMetadata.getError());
        }

        return Validation.valid(ScriptletMetadataConverter.toScriptletInputView(inputMethodMetadata.get()));
    }

    public Validation<ErrorResponse, List<StructTypeFieldView>> getScriptletOutputMetadata(
            final String jarFile, final String className) {

        final Optional<Class<?>> loadedClass = jarUtils.loadClassByJAR(jarFile, scriptletPath, className);
        if (!loadedClass.isPresent()) {
            return Validation.invalid(ScriptletErrorResponse.FailedToLoadScriptletClass(jarFile, className));
        }

        Validation<ErrorResponse, StructType> outputMethodMetadata =
                getOutputMethodMetadataStructType(loadedClass.get());
        if (outputMethodMetadata.isInvalid()) {
            return Validation.invalid(outputMethodMetadata.getError());
        }

        return Validation.valid(ScriptletMetadataConverter.toStructTypeView(outputMethodMetadata.get()));
    }

    public Validation<ErrorResponse, ScriptletMetadataView> getScriptletMetadataStructTypes(
            final String jarFile, final String className) {

        final Optional<Class<?>> loadedClass = jarUtils.loadClassByJAR(jarFile, scriptletPath, className);
        if (!loadedClass.isPresent()) {
            return Validation.invalid(ScriptletErrorResponse.FailedToLoadScriptletClass(jarFile, className));
        }

        Validation<ErrorResponse, Map<String, StructType>> inputMethodMetadata =
                getInputMethodMetadataStructType(loadedClass.get());
        if (inputMethodMetadata.isInvalid()) {
            return Validation.invalid(inputMethodMetadata.getError());
        }

        Validation<ErrorResponse, StructType> outputMethodMetadata =
                getOutputMethodMetadataStructType(loadedClass.get());
        if (outputMethodMetadata.isInvalid()) {
            return Validation.invalid(outputMethodMetadata.getError());
        }

        return Validation.valid(ScriptletMetadataView.builder()
                .inputs(ScriptletMetadataConverter.toScriptletInputView(inputMethodMetadata.get()))
                .outputs(ScriptletMetadataConverter.toStructTypeView(outputMethodMetadata.get()))
                .build());
    }

    private Validation<ErrorResponse, Map<String, StructType>> getInputMethodMetadataStructType(final Class<?> loadedClass) {
        Optional<Method> method = jarUtils.findMethodByName(inputMethodName, loadedClass);
        if (!method.isPresent()) {
            return Validation.invalid(ScriptletErrorResponse.MethodNotFound(
                    inputMethodName,
                    loadedClass.getName()));
        }

        Optional<Object> metadata =
                jarUtils.getMethodMetadata(loadedClass, method.get());
        if (!metadata.isPresent()) {
            return Validation.invalid(ScriptletErrorResponse.FailedToReadMethodMetadata(
                    inputMethodName,
                    loadedClass.getName()));
        }
        return Validation.valid(((Map<String, StructType>) metadata.get()));
    }

    private Validation<ErrorResponse, StructType> getOutputMethodMetadataStructType(final Class<?> loadedClass) {
        Optional<Method> method = jarUtils.findMethodByName(outputMethodName, loadedClass);
        if (!method.isPresent()) {
            return Validation.invalid(ScriptletErrorResponse.MethodNotFound(
                    outputMethodName,
                    loadedClass.getName()));
        }

        Optional<Object> metadata =
                jarUtils.getMethodMetadata(loadedClass, method.get());
        if (!metadata.isPresent()) {
            return Validation.invalid(ScriptletErrorResponse.FailedToReadMethodMetadata(
                    outputMethodName,
                    loadedClass.getName()));
        }
        return Validation.valid(((StructType) metadata.get()));
    }
}
