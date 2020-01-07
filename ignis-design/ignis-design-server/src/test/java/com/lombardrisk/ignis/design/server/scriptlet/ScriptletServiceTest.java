package com.lombardrisk.ignis.design.server.scriptlet;

import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import io.vavr.control.Validation;
import org.apache.spark.sql.types.DataType;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ScriptletServiceTest {

    private JarUtils jarUtils;
    private ScriptletService scriptletService;

    @Before
    public void setUp() {
        jarUtils = mock(JarUtils.class);

        scriptletService = new ScriptletService("path", "interface",
                "inputMethodName", "outputMethodName", jarUtils);

        when(jarUtils.getJarFiles(anyString())).thenReturn(Optional.of(asList("file1.jar", "file2.jar")));
        when(jarUtils.loadClassByJAR(anyString(), anyString(), anyString())).thenReturn(Optional.of(String.class));
        when(jarUtils.findMethodByName(anyString(), any())).thenReturn(anyMethod());
    }

    @Test
    public void getScriptletJars_delegatesScriptletPathToJarUtils() {
        Validation<ErrorResponse, List<String>> result = scriptletService.getScriptletJars();
        verify(jarUtils).getJarFiles(eq("path"));
    }

    @Test
    public void getScriptletJars_jarFilesIsEmpty_returnsErrorResponse() {
        when(jarUtils.getJarFiles(anyString())).thenReturn(Optional.empty());

        Validation<ErrorResponse, List<String>> validation = scriptletService.getScriptletJars();

        assertThat(validation.isInvalid()).isTrue();
        assertThat(validation.getError())
                .extracting(ErrorResponse::getErrorCode)
                .isEqualTo("FAILED_TO_READ_SCRIPTLET_PATH");
    }

    @Test
    public void getScriptletJars_delegatesScriptletPathToJarUtilsLoadClassByJAR() {
        when(jarUtils.getJarFiles(anyString())).thenReturn(Optional.of(asList("myFile1.jar", "myFile2.jar")));

        scriptletService.getScriptletJars();
        verify(jarUtils, times(2)).loadClassByJAR(anyString(), eq("path"), anyString());
    }

    @Test
    public void getScriptletJars_delegatesScriptletInterfaceToJarUtilsLoadClassByJAR() {
        when(jarUtils.getJarFiles(anyString())).thenReturn(Optional.of(asList("myFile1.jar", "myFile2.jar")));

        scriptletService.getScriptletJars();
        verify(jarUtils, times(2)).loadClassByJAR(anyString(), anyString(), eq("interface"));
    }

    @Test
    public void getScriptletJars_delegatesAllJarFilesToJarUtilsLoadClassByJAR() {
        when(jarUtils.getJarFiles(anyString())).thenReturn(Optional.of(asList("myFile1.jar", "myFile2.jar")));

        scriptletService.getScriptletJars();

        ArgumentCaptor<String> myCaptor = ArgumentCaptor.forClass(String.class);
        verify(jarUtils, times(2)).loadClassByJAR(myCaptor.capture(), anyString(), anyString());

        List<String> values = myCaptor.getAllValues();

        assertThat(values.size()).isEqualTo(2);
        assertThat(values).containsExactlyInAnyOrder("myFile1.jar", "myFile2.jar");
    }

    @Test
    public void getScriptletJars_returnsOnlyScriptletJars() {
        when(jarUtils.getJarFiles(anyString())).thenReturn(Optional.of(asList("myFile1.jar", "myFile2.jar")));

        when(jarUtils.loadClassByJAR(eq("myFile1.jar"), anyString(), anyString()))
                .thenReturn(Optional.of(String.class));

        when(jarUtils.loadClassByJAR(eq("myFile2.jar"), anyString(), anyString()))
                .thenReturn(Optional.empty());

        Validation<ErrorResponse, List<String>> validation = scriptletService.getScriptletJars();

        assertThat(validation.isValid()).isTrue();
        assertThat(validation.get().size()).isEqualTo(1);
        assertThat(validation.get().get(0)).isEqualTo("myFile1.jar");
    }

    @Test
    public void getScriptletJars_noScriptletJar_returnsEmptyList() {
        when(jarUtils.getJarFiles(anyString())).thenReturn(Optional.of(asList("myFile1.jar", "myFile2.Jar")));

        when(jarUtils.loadClassByJAR(anyString(), anyString(), anyString())).thenReturn(Optional.empty());

        Validation<ErrorResponse, List<String>> validation = scriptletService.getScriptletJars();

        assertThat(validation.isValid()).isTrue();
        assertThat(validation.get().size()).isEqualTo(0);
    }

    @Test
    public void getScriptletClasses_delegatesJarFileNameToJarUtilsLoadClassByJar() {
        scriptletService.getScriptletClasses("myFile.jar");

        verify(jarUtils).loadClassByJAR(eq("myFile.jar"), anyString(), anyString());
    }

    @Test
    public void getScriptletClasses_delegatesScriptletPathToJarUtilsLoadClassByJar() {
        scriptletService.getScriptletClasses("myFile.jar");

        verify(jarUtils).loadClassByJAR(anyString(), eq("path"), anyString());
    }

    @Test
    public void getScriptletClasses_delegatesScriptletInterfaceNameToJarUtilsLoadClassByJar() {
        scriptletService.getScriptletClasses("myFile.jar");

        verify(jarUtils).loadClassByJAR(anyString(), anyString(), eq("interface"));
    }

    @Test
    public void getScriptletClasses_interfaceClassNotFound_returnsErrorResponse() {
        when(jarUtils.loadClassByJAR(anyString(), anyString(), anyString())).thenReturn(Optional.empty());

        Validation<ErrorResponse, List<String>> validation = scriptletService.getScriptletClasses("myFile.jar");

        assertThat(validation.isInvalid()).isTrue();
        assertThat(validation.getError())
                .extracting(ErrorResponse::getErrorCode)
                .isEqualTo("FAILED_TO_LOAD_SCRIPTLET_INTERFACE");
    }

    @Test
    public void getScriptletClasses_delegatesJarWithFullPathToJarUtils() {
        scriptletService.getScriptletClasses("myFile.jar");

        ArgumentCaptor<String> myCaptor = ArgumentCaptor.forClass(String.class);
        verify(jarUtils).getJarClasses(myCaptor.capture());
        assertThat(myCaptor.getValue()).isEqualTo("path" + File.separator + "myFile.jar");
    }

    @Test
    public void getScriptletClasses_noClassFound_returnsErrorResponse() {
        when(jarUtils.loadClassByJAR(anyString(), anyString(), anyString()))
                .thenReturn(Optional.of(String.class));

        when(jarUtils.getJarClasses(anyString())).thenReturn(Optional.empty());

        Validation<ErrorResponse, List<String>> validation = scriptletService.getScriptletClasses("myFile.jar");

        assertThat(validation.isInvalid()).isTrue();
        assertThat(validation.getError())
                .extracting(ErrorResponse::getErrorCode)
                .isEqualTo("FAILED_TO_READ_JAR_CLASSES");
    }

    @Test
    public void getScriptletClasses_neverDelegatesInterfaceClasseToJarUtilsLoadClassByClassLoader() {
        when(jarUtils.getJarClasses(anyString())).thenReturn(Optional.of(asList(
                "class1",
                "class2",
                "interface")));

        scriptletService.getScriptletClasses("myFile.jar");

        verify(jarUtils, never()).loadClassByClassLoader(eq("interface"), any());
    }

    @Test
    public void getScriptletClasses_delegatesAllOtherClasseToJarUtilsLoadClassByClassLoader() {
        when(jarUtils.getJarClasses(anyString())).thenReturn(Optional.of(asList(
                "class1",
                "class2",
                "interface")));

        scriptletService.getScriptletClasses("myFile.jar");

        ArgumentCaptor<String> myCaptor = ArgumentCaptor.forClass(String.class);
        verify(jarUtils, times(2)).loadClassByClassLoader(myCaptor.capture(), any());

        List<String> classes = myCaptor.getAllValues();

        assertThat(classes.size()).isEqualTo(2);
        assertThat(classes).containsExactlyInAnyOrder("class1", "class2");
    }

    @Test
    public void getScriptletClasses_delegatesInterfaceClassLoader() {
        Class<?> interfaceClass = Integer.class;

        when(jarUtils.loadClassByJAR(anyString(), anyString(), anyString())).thenReturn(Optional.of(interfaceClass));
        when(jarUtils.getJarClasses(anyString())).thenReturn(Optional.of(asList(
                "class1", "class2")));

        scriptletService.getScriptletClasses("myFile.jar");
        verify(jarUtils, times(2)).loadClassByClassLoader(anyString(), eq(interfaceClass.getClassLoader()));
    }

    @Test
    public void getScriptletClasses_returnsOnlyClassesAssignableFromInterface() {
        when(jarUtils.loadClassByJAR(anyString(), anyString(), anyString())).thenReturn(Optional.of(Number.class));
        when(jarUtils.getJarClasses(anyString())).thenReturn(Optional.of(asList(
                "java.lang.Long",
                "java.lang.Integer")));

        when(jarUtils.loadClassByClassLoader(eq("java.lang.Long"), any())).thenReturn(Optional.of(Long.class));
        when(jarUtils.loadClassByClassLoader(eq("java.lang.Integer"), any())).thenReturn(Optional.of(String.class));

        Validation<ErrorResponse, List<String>> validation = scriptletService.getScriptletClasses("myFile.jar");

        assertThat(validation.isValid()).isTrue();
        assertThat(validation.get().size()).isEqualTo(1);
        assertThat(validation.get().get(0)).isEqualTo("java.lang.Long");
    }

    @Test
    public void getScriptletClasses_noClassImplementInterface_returnsEmptyList() {
        when(jarUtils.loadClassByJAR(anyString(), anyString(), anyString())).thenReturn(Optional.of(Number.class));
        when(jarUtils.getJarClasses(anyString())).thenReturn(Optional.of(asList(
                "java.lang.Long",
                "java.lang.Integer")));

        when(jarUtils.loadClassByClassLoader(any(), any())).thenReturn(Optional.empty());

        Validation<ErrorResponse, List<String>> validation = scriptletService.getScriptletClasses("myFile.jar");

        assertThat(validation.isValid()).isTrue();
        assertThat(validation.get().size()).isEqualTo(0);
        assertThat(validation.get()).isEmpty();
    }

    @Test
    public void getScriptletInputMetadata_delegatesJarFileNameToJarUtils() {
        Validation<ErrorResponse, Map<String, List<StructTypeFieldView>>> validation =
                scriptletService.getScriptletInputMetadata("myFile.jar", "className");

        verify(jarUtils).loadClassByJAR(eq("myFile.jar"), anyString(), anyString());
    }

    @Test
    public void getScriptletInputMetadata_delegatesScriptletPathToJarUtils() {
        Validation<ErrorResponse, Map<String, List<StructTypeFieldView>>> validation =
                scriptletService.getScriptletInputMetadata("myFile.jar", "className");

        verify(jarUtils).loadClassByJAR(anyString(), eq("path"), anyString());
    }

    @Test
    public void getScriptletInputMetadata_delegatesClassnameToJarUtils() {
        Validation<ErrorResponse, Map<String, List<StructTypeFieldView>>> validation =
                scriptletService.getScriptletInputMetadata("myFile.jar", "className");

        verify(jarUtils).loadClassByJAR(anyString(), anyString(), eq("className"));
    }

    @Test
    public void getScriptletInputMetadata_classNotFound_ReturnsErrorResponse() {
        when(jarUtils.loadClassByJAR(anyString(), anyString(), anyString())).thenReturn(Optional.empty());

        Validation<ErrorResponse, Map<String, List<StructTypeFieldView>>> validation =
                scriptletService.getScriptletInputMetadata("myFile.jar", "className");

        assertThat(validation.isInvalid()).isTrue();
        assertThat(validation.getError())
                .extracting(ErrorResponse::getErrorCode)
                .isEqualTo("FAILED_TO_LOAD_SCRIPTLET_CLASS");
    }

    @Test
    public void getScriptletInputMetadata_delegatesInputMethodNameToJarUtils() {
        Validation<ErrorResponse, Map<String, List<StructTypeFieldView>>> validation =
                scriptletService.getScriptletInputMetadata("myFile.jar", "className");

        verify(jarUtils).findMethodByName(eq("inputMethodName"), any());
    }

    @Test
    public void getScriptletInputMetadata_delegatesLoadedClassToJarUtils() {
        Class<?> loadedClass = String.class;
        when(jarUtils.loadClassByJAR(anyString(), anyString(), anyString())).thenReturn(Optional.of(loadedClass));

        Validation<ErrorResponse, Map<String, List<StructTypeFieldView>>> validation =
                scriptletService.getScriptletInputMetadata("myFile.jar", "className");

        verify(jarUtils).findMethodByName(anyString(), eq(loadedClass));
    }

    @Test
    public void getScriptletInputMetadata_InputMethodNotFound_returnsErrorResponse() {
        when(jarUtils.findMethodByName(anyString(), any()))
                .thenReturn(Optional.empty());

        Validation<ErrorResponse, Map<String, List<StructTypeFieldView>>> validation =
                scriptletService.getScriptletInputMetadata("myFile.jar", "className");

        assertThat(validation.isInvalid()).isTrue();

        assertThat(validation.getError())
                .extracting(ErrorResponse::getErrorCode)
                .isEqualTo("METHOD_NOT_FOUND");
    }

    @Test
    public void getScriptletInputMetadata_delegatesLoadedClassToGetMethodMetadataJarUtils() {
        Class<?> loadedClass = String.class;
        when(jarUtils.loadClassByJAR(anyString(), anyString(), anyString())).thenReturn(Optional.of(loadedClass));

        Validation<ErrorResponse, Map<String, List<StructTypeFieldView>>> validation =
                scriptletService.getScriptletInputMetadata("myFile.jar", "className");

        verify(jarUtils).getMethodMetadata(eq(loadedClass), any());
    }

    @Test
    public void getScriptletInputMetadata_delegatesInputMethodJarUtils() {
        Method method = anyMethod().get();
        when(jarUtils.findMethodByName(anyString(), any())).thenReturn(Optional.of(method));

        Validation<ErrorResponse, Map<String, List<StructTypeFieldView>>> validation =
                scriptletService.getScriptletInputMetadata("myFile.jar", "className");

        verify(jarUtils).getMethodMetadata(any(), eq(method));
    }

    @Test
    public void getScriptletInputMetadata_failedToReadInputMethodMetadata_returnsErrorResponse() {
        when(jarUtils.getMethodMetadata(any(), any())).thenReturn(Optional.empty());

        Validation<ErrorResponse, Map<String, List<StructTypeFieldView>>> validation =
                scriptletService.getScriptletInputMetadata("myFile.jar", "className");

        assertThat(validation.isInvalid()).isTrue();

        assertThat(validation.getError())
                .extracting(ErrorResponse::getErrorCode)
                .isEqualTo("FAILED_TO_READ_METHOD_METADATA");
    }

    @Test
    public void getScriptletInputMetadata_returnsMapWithListOfStructTypeFieldView() {
        StructType structType = new StructType(new StructField[]{
                anyField("FieldName", DataTypes.StringType)
        });
        Map<String, StructType> metadata = new HashMap<>();
        metadata.put("Name", structType);

        when(jarUtils.getMethodMetadata(any(), any())).thenReturn(Optional.of(metadata));

        Validation<ErrorResponse, Map<String, List<StructTypeFieldView>>> validation =
                scriptletService.getScriptletInputMetadata("myFile.jar", "className");

        assertThat(validation.isValid()).isTrue();
        assertThat(validation.get()).isInstanceOf(Map.class);
        assertThat(validation.get()).containsKey("Name");
        assertThat(validation.get().get("Name").size()).isEqualTo(1);
        assertThat(validation.get().get("Name").get(0)).isInstanceOf(StructTypeFieldView.class);
    }

    @Test
    public void getScriptletOutputMetadata_delegatesJarFileNameToJarUtils() {
        Validation<ErrorResponse, List<StructTypeFieldView>> validation =
                scriptletService.getScriptletOutputMetadata("myFile.jar", "className");

        verify(jarUtils).loadClassByJAR(eq("myFile.jar"), anyString(), anyString());
    }

    @Test
    public void getScriptletOutputMetadata_delegatesScriptletPathToJarUtils() {
        Validation<ErrorResponse, List<StructTypeFieldView>> validation =
                scriptletService.getScriptletOutputMetadata("myFile.jar", "className");

        verify(jarUtils).loadClassByJAR(anyString(), eq("path"), anyString());
    }

    @Test
    public void getScriptletOutputMetadata_delegatesClassnameToJarUtils() {
        Validation<ErrorResponse, List<StructTypeFieldView>> validation =
                scriptletService.getScriptletOutputMetadata("myFile.jar", "className");

        verify(jarUtils).loadClassByJAR(anyString(), anyString(), eq("className"));
    }

    @Test
    public void getScriptletOutputMetadata_errorToLoadClass_returnsErrorResponse() {
        when(jarUtils.loadClassByJAR(anyString(), anyString(), anyString())).thenReturn(Optional.empty());

        Validation<ErrorResponse, List<StructTypeFieldView>> validation =
                scriptletService.getScriptletOutputMetadata("myFile.jar", "className");

        assertThat(validation.isInvalid()).isTrue();
        assertThat(validation.getError())
                .extracting(ErrorResponse::getErrorCode)
                .isEqualTo("FAILED_TO_LOAD_SCRIPTLET_CLASS");
    }

    @Test
    public void getScriptletOutputMetadata_delegatesOutputMethodNameToJarUtils() {
        Validation<ErrorResponse, List<StructTypeFieldView>> validation =
                scriptletService.getScriptletOutputMetadata("myFile.jar", "className");

        verify(jarUtils).findMethodByName(eq("outputMethodName"), any());
    }

    @Test
    public void getScriptletOutputMetadata_delegatesLoadedClassToJarUtils() {
        Class<?> loadedClass = String.class;
        when(jarUtils.loadClassByJAR(anyString(), anyString(), anyString())).thenReturn(Optional.of(loadedClass));

        Validation<ErrorResponse, List<StructTypeFieldView>> validation =
                scriptletService.getScriptletOutputMetadata("myFile.jar", "className");

        verify(jarUtils).findMethodByName(anyString(), eq(loadedClass));
    }

    @Test
    public void getScriptletOutputMetadata_outputMethodNotFound_returnErrorResponse() {
        when(jarUtils.findMethodByName(anyString(), any()))
                .thenReturn(Optional.empty());

        Validation<ErrorResponse, List<StructTypeFieldView>> validation =
                scriptletService.getScriptletOutputMetadata("myFile.jar", "className");

        assertThat(validation.isInvalid()).isTrue();

        assertThat(validation.getError())
                .extracting(ErrorResponse::getErrorCode)
                .isEqualTo("METHOD_NOT_FOUND");
    }

    @Test
    public void getScriptletOutputMetadata_delegatesLoadedClassToJarUtilsGetMethodMetadata() {
        Class<?> loadedClass = String.class;
        when(jarUtils.loadClassByJAR(anyString(), anyString(), anyString())).thenReturn(Optional.of(loadedClass));

        Validation<ErrorResponse, List<StructTypeFieldView>> validation =
                scriptletService.getScriptletOutputMetadata("myFile.jar", "className");

        verify(jarUtils).getMethodMetadata(eq(loadedClass), any());
    }

    @Test
    public void getScriptletOutputMetadata_delegatesOutputMethodToJarUtils() {
        Method method = anyMethod().get();
        when(jarUtils.findMethodByName(anyString(), any())).thenReturn(Optional.of(method));

        Validation<ErrorResponse, List<StructTypeFieldView>> validation =
                scriptletService.getScriptletOutputMetadata("myFile.jar", "className");

        verify(jarUtils).getMethodMetadata(any(), eq(method));
    }

    @Test
    public void getScriptletOutputMetadata_failedToHaveOutputMethodMetadata_returnsErrorResponse() {
        when(jarUtils.getMethodMetadata(any(), any())).thenReturn(Optional.empty());

        Validation<ErrorResponse, List<StructTypeFieldView>> validation =
                scriptletService.getScriptletOutputMetadata("myFile.jar", "className");

        assertThat(validation.isInvalid()).isTrue();
        assertThat(validation.getError())
                .extracting(ErrorResponse::getErrorCode)
                .isEqualTo("FAILED_TO_READ_METHOD_METADATA");
    }

    @Test
    public void getScriptletOutputMetadata_returnListOfStructTypeFieldViewObject() {
        StructType structType = new StructType(new StructField[]{
                anyField("FieldName", DataTypes.StringType)
        });

        when(jarUtils.getMethodMetadata(any(), any())).thenReturn(Optional.of(structType));

        Validation<ErrorResponse, List<StructTypeFieldView>> validation =
                scriptletService.getScriptletOutputMetadata("myFile.jar", "className");

        assertThat(validation.isValid()).isTrue();
        assertThat(validation.get()).isInstanceOf(List.class);
        assertThat(validation.get().size()).isEqualTo(1);
        assertThat(validation.get().get(0)).isInstanceOf(StructTypeFieldView.class);
    }

    @Test
    public void getScriptletMetadataStructTypes_delegatesJarFileToJarUtils() {
        scriptletService.getScriptletMetadataStructTypes("myFile.jar", "classname");

        verify(jarUtils).loadClassByJAR(eq("myFile.jar"), anyString(), anyString());
    }

    @Test
    public void getScriptletMetadataStructTypes_delegatesScriptletPathToJarUtils() {
        scriptletService.getScriptletMetadataStructTypes("myFile.jar", "classname");

        verify(jarUtils).loadClassByJAR(anyString(), eq("path"), anyString());
    }

    @Test
    public void getScriptletMetadataStructTypes_delegatesClassnameToJarUtils() {
        scriptletService.getScriptletMetadataStructTypes("myFile.jar", "classname");

        verify(jarUtils).loadClassByJAR(anyString(), anyString(), eq("classname"));
    }

    @Test
    public void getScriptletMetadataStructTypes_classNotFound_returnsErrorResponse() {
        when(jarUtils.loadClassByJAR(anyString(), anyString(), anyString()))
                .thenReturn(Optional.empty());

        Validation<ErrorResponse, ScriptletMetadataView> validation =
                scriptletService.getScriptletMetadataStructTypes("myFile.jar", "className");

        assertThat(validation.isInvalid()).isTrue();
        assertThat(validation.getError())
                .extracting(ErrorResponse::getErrorCode)
                .isEqualTo("FAILED_TO_LOAD_SCRIPTLET_CLASS");
    }

    @Test
    public void getScriptletMetadataStructTypes_delegatesInputMethodNameToJarUtils() {
        Validation<ErrorResponse, ScriptletMetadataView> validation =
                scriptletService.getScriptletMetadataStructTypes("myFile.jar", "className");

        verify(jarUtils).findMethodByName(eq("inputMethodName"), any());
    }

    @Test
    public void getScriptletMetadataStructTypes_delegatesLoadedClassToJarUtils() {
        Class<?> loadedClass = String.class;
        when(jarUtils.loadClassByJAR(anyString(), anyString(), anyString())).thenReturn(Optional.of(loadedClass));

        Validation<ErrorResponse, ScriptletMetadataView> validation =
                scriptletService.getScriptletMetadataStructTypes("myFile.jar", "className");

        verify(jarUtils).findMethodByName(anyString(), eq(loadedClass));
    }

    @Test
    public void getScriptletMetadataStructTypes_InputMethodNotFound_returnsErrorResponse() {
        when(jarUtils.findMethodByName(anyString(), any()))
                .thenReturn(Optional.empty());

        Validation<ErrorResponse, ScriptletMetadataView> validation =
                scriptletService.getScriptletMetadataStructTypes("myFile.jar", "className");

        assertThat(validation.isInvalid()).isTrue();

        assertThat(validation.getError())
                .extracting(ErrorResponse::getErrorCode)
                .isEqualTo("METHOD_NOT_FOUND");
    }

    @Test
    public void getScriptletMetadataStructTypes_delegatesLoadedClassToGetMethodMetadataJarUtils() {
        Class<?> loadedClass = String.class;
        when(jarUtils.loadClassByJAR(anyString(), anyString(), anyString())).thenReturn(Optional.of(loadedClass));

        Validation<ErrorResponse, ScriptletMetadataView> validation =
                scriptletService.getScriptletMetadataStructTypes("myFile.jar", "className");

        verify(jarUtils).getMethodMetadata(eq(loadedClass), any());
    }

    @Test
    public void getScriptletMetadataStructTypes_delegatesInputMethodJarUtils() {
        Method method = anyMethod().get();
        when(jarUtils.findMethodByName(anyString(), any())).thenReturn(Optional.of(method));

        Validation<ErrorResponse, ScriptletMetadataView> validation =
                scriptletService.getScriptletMetadataStructTypes("myFile.jar", "className");

        verify(jarUtils).getMethodMetadata(any(), eq(method));
    }

    @Test
    public void getScriptletMetadataStructTypes_failedToReadInputMethodMetadata_returnsErrorResponse() {
        when(jarUtils.getMethodMetadata(any(), any())).thenReturn(Optional.empty());

        Validation<ErrorResponse, ScriptletMetadataView> validation =
                scriptletService.getScriptletMetadataStructTypes("myFile.jar", "className");

        assertThat(validation.isInvalid()).isTrue();

        assertThat(validation.getError())
                .extracting(ErrorResponse::getErrorCode)
                .isEqualTo("FAILED_TO_READ_METHOD_METADATA");
    }

    private Optional<Method> anyMethod() {
        return Arrays.stream(String.class.getMethods()).findFirst();
    }

    private StructField anyField(final String name, final DataType dataType) {
        return new StructField(name, dataType, false, Metadata.empty());
    }
}