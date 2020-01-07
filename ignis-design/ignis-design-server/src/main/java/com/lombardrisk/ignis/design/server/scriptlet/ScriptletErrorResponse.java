package com.lombardrisk.ignis.design.server.scriptlet;

import com.lombardrisk.ignis.data.common.error.ErrorResponse;

public class ScriptletErrorResponse {

    public static ErrorResponse FailedToReadScriptletPath(final String path) {
        return ErrorResponse.valueOf(
                String.format(Code.FAILED_TO_READ_SCRIPTLET_PATH.errorMessage, path),
                Code.FAILED_TO_READ_SCRIPTLET_PATH.name());
    }

    public static ErrorResponse FailedToReadJarClasses(final String jarFile) {
        return ErrorResponse.valueOf(
                String.format(Code.FAILED_TO_READ_JAR_CLASSES.errorMessage, jarFile),
                Code.FAILED_TO_READ_JAR_CLASSES.name());
    }

    public static ErrorResponse FailedToLoadScriptletInterface(final String jarFile) {
        return ErrorResponse.valueOf(
                String.format(Code.FAILED_TO_LOAD_SCRIPTLET_INTERFACE.errorMessage, jarFile),
                Code.FAILED_TO_LOAD_SCRIPTLET_INTERFACE.name());
    }

    public static ErrorResponse FailedToLoadScriptletClass(final String jarFile, final String className) {
        return ErrorResponse.valueOf(
                String.format(Code.FAILED_TO_LOAD_SCRIPTLET_CLASS.errorMessage, className, jarFile),
                Code.FAILED_TO_LOAD_SCRIPTLET_CLASS.name());
    }

    public static ErrorResponse MethodNotFound(final String methodName, final String className) {
        return ErrorResponse.valueOf(
                String.format(Code.METHOD_NOT_FOUND.errorMessage, methodName, className),
                Code.METHOD_NOT_FOUND.name());
    }

    public static ErrorResponse FailedToReadMethodMetadata(final String methodName, final String className) {
        return ErrorResponse.valueOf(
                String.format(Code.FAILED_TO_READ_METHOD_METADATA.errorMessage, methodName, className),
                Code.FAILED_TO_READ_METHOD_METADATA.name());
    }

    private enum Code {
        FAILED_TO_READ_SCRIPTLET_PATH("Failed to read [%s]"),
        FAILED_TO_READ_JAR_CLASSES("Failed to read jar [%s]"),
        FAILED_TO_LOAD_SCRIPTLET_INTERFACE("Failed to load scriptlet interface jar [%s]"),
        FAILED_TO_LOAD_SCRIPTLET_CLASS("Failed to load scriptlet class [%s] for jar [%s]"),
        METHOD_NOT_FOUND("Method [%s] is not member of class [%s]"),
        FAILED_TO_READ_METHOD_METADATA("Enable to read method [%s] metadata for class [%s]");

        private final String errorMessage;

        Code(final String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }
}
