package com.lombardrisk.ignis.design.server.controller;

import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.design.server.DesignStudioTestConfig;
import com.lombardrisk.ignis.design.server.scriptlet.ScriptletMetadataView;
import com.lombardrisk.ignis.design.server.scriptlet.ScriptletService;
import com.lombardrisk.ignis.design.server.scriptlet.StructTypeFieldView;
import io.vavr.control.Validation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@DesignStudioTestConfig
public class ScriptletControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ScriptletService scriptletService;

    @Test
    public void getScriptletJars_invokesScriptletService() throws Exception {
        mockMvc.perform(
                get("/api/v1/scriptlets/jars"));

        verify(scriptletService).getScriptletJars();
    }

    @Test
    public void getScriptletJars_returnsOkResponseWithListOfJars() throws Exception {
        when(scriptletService.getScriptletJars()).thenReturn(Validation.valid(asList("file1", "file2")));

        mockMvc.perform(
                get("/api/v1/scriptlets/jars"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]", equalTo("file1")))
                .andExpect(jsonPath("$[1]", equalTo("file2")));
    }

    @Test
    public void getScriptletJars_noJarFileFound_returnsOkResponseWithEmptyList() throws Exception {
        when(scriptletService.getScriptletJars()).thenReturn(Validation.valid(Collections.emptyList()));

        mockMvc.perform(
                get("/api/v1/scriptlets/jars"))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }

    @Test
    public void getScriptletClasses_delegatesJarFileToScriptletService() throws Exception {
        mockMvc.perform(
                get("/api/v1/scriptlets/jars/jar_file_name/classes"));

        verify(scriptletService).getScriptletClasses(eq("jar_file_name"));
    }

    @Test
    public void getScriptletClasses_scriptletServiceReturnsErrorResponse_ReturnsBadRequestError() throws Exception {
        when(scriptletService.getScriptletClasses(anyString())).thenReturn(Validation.invalid(ErrorResponse.valueOf(
                "Any error message", "any error code")));

        mockMvc.perform(
                get("/api/v1/scriptlets/jars/jar_file_name/classes"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage", equalTo("Any error message")))
                .andExpect(jsonPath("$.errorCode", equalTo("any error code")));
    }

    @Test
    public void getScriptletClasses_returnsOkResponseWithClassNameList() throws Exception {
        when(scriptletService.getScriptletClasses(anyString())).thenReturn(Validation.valid(asList(
                "class1",
                "class2")));

        mockMvc.perform(
                get("/api/v1/scriptlets/jars/jar_file_name/classes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]", equalTo("class1")))
                .andExpect(jsonPath("$[1]", equalTo("class2")));
    }

    @Test
    public void getScriptletClasses_noClassesFound_returnsOkResponseWithEmptyList() throws Exception {
        when(scriptletService.getScriptletClasses(anyString())).thenReturn(Validation.valid(Collections.emptyList()));

        mockMvc.perform(
                get("/api/v1/scriptlets/jars/jar_file_name/classes"))
                .andExpect(status().isOk())
                .andExpect(content().string("[]"));
    }

    @Test
    public void getScriptletClassMetadata_delegatesJarFileNameToScriptletService() throws Exception {
        mockMvc.perform(
                get("/api/v1/scriptlets/jars/jar_file_name/classes/classname/metadata?type=all"));

        verify(scriptletService).getScriptletMetadataStructTypes(eq("jar_file_name"), anyString());
    }

    @Test
    public void getScriptletClassMetadata_delegatesClassNameToScriptletService() throws Exception {
        mockMvc.perform(
                get("/api/v1/scriptlets/jars/jar_file_name/classes/classname/metadata?type=all"));

        verify(scriptletService).getScriptletMetadataStructTypes(anyString(), eq("classname"));
    }

    @Test
    public void getScriptletClassMetadata_scriptletServiceReturnsErrorResponse_ReturnsBadRequestError() throws Exception {
        when(scriptletService.getScriptletMetadataStructTypes(anyString(), anyString())).thenReturn(Validation.invalid(
                ErrorResponse.valueOf(
                        "Any error message", "any error code")));

        mockMvc.perform(
                get("/api/v1/scriptlets/jars/jar_file_name/classes/classname/metadata?type=all"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage", equalTo("Any error message")))
                .andExpect(jsonPath("$.errorCode", equalTo("any error code")));
    }

    @Test
    public void getScriptletClassMetadata_scriptletServiceReturnsScriptletMetadataObject_returnsOkResponseWithMetadata() throws Exception {
        Map<String, List<StructTypeFieldView>> scriptletInputs = new HashMap<>();
        scriptletInputs.put(
                "Traders",
                asList(
                        anyStructTypeField("TRADER_ID", "Integer"),
                        anyStructTypeField("Name", "String"),
                        anyStructTypeField("TRADE_DATE", "Date")
                ));

        scriptletInputs.put(
                "Trades",
                asList(
                        anyStructTypeField("TRADE_DATE", "Integer"),
                        anyStructTypeField("STOCK", "String"),
                        anyStructTypeField("TradeType", "String")
                ));

        List<StructTypeFieldView> scriptletOutput = asList(
                anyStructTypeField("TRADER_ID", "Integer"),
                anyStructTypeField("Name", "String"),
                anyStructTypeField("STOCK", "String"),
                anyStructTypeField("TradeType", "String"),
                anyStructTypeField("Total", "Long"));

        when(scriptletService.getScriptletMetadataStructTypes(anyString(), anyString())).thenReturn(Validation.valid(
                ScriptletMetadataView.builder()
                        .inputs(scriptletInputs)
                        .outputs(scriptletOutput)
                        .build()));

        String expectedJsonString = "{"
                + "'inputs':{"
                + "'Trades':[{"
                + "'field':'TRADE_DATE',"
                + "'type':'Integer'"
                + "},{"
                + "'field':'STOCK',"
                + "'type':'String'"
                + "},{"
                + "'field':'TradeType',"
                + "'type':'String'"
                + "}],"
                + "'Traders':[{"
                + "'field':'TRADER_ID',"
                + "'type':'Integer'"
                + "},{"
                + "'field':'Name',"
                + "'type':'String'"
                + "},{"
                + "'field':'TRADE_DATE',"
                + "'type':'Date'"
                + "}]},"
                + "'outputs':[{'"
                + "field':'TRADER_ID',"
                + "'type':'Integer'"
                + "},{"
                + "'field':'Name',"
                + "'type':'String'"
                + "},{"
                + "'field':'STOCK',"
                + "'type':'String'"
                + "},{"
                + "'field':'TradeType',"
                + "'type':'String'"
                + "},{"
                + "'field':'Total',"
                + "'type':'Long'"
                + "}]}";

        mockMvc.perform(
                get("/api/v1/scriptlets/jars/jar_file_name/classes/classname/metadata?type=all"))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedJsonString.replace("'", "\"")));
    }

    @Test
    public void getScriptletClassInputMetadata_delegatesJarFileNameToScriptletService() throws Exception {
        mockMvc.perform(
                get("/api/v1/scriptlets/jars/jar_file_name/classes/classname/metadata?type=input"));

        verify(scriptletService).getScriptletInputMetadata(eq("jar_file_name"), anyString());
    }

    @Test
    public void getScriptletClassInputMetadata_delegatesClassNameToScriptletService() throws Exception {
        mockMvc.perform(
                get("/api/v1/scriptlets/jars/jar_file_name/classes/classname/metadata?type=input"));

        verify(scriptletService).getScriptletInputMetadata(anyString(), eq("classname"));
    }

    @Test
    public void getScriptletClassInputMetadata_scriptletServiceReturnsErrorResponse_ReturnsBadRequestError() throws Exception {
        when(scriptletService.getScriptletInputMetadata(anyString(), anyString())).thenReturn(Validation.invalid(
                ErrorResponse.valueOf(
                        "Any error message", "any error code")));

        mockMvc.perform(
                get("/api/v1/scriptlets/jars/jar_file_name/classes/classname/metadata?type=input"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage", equalTo("Any error message")))
                .andExpect(jsonPath("$.errorCode", equalTo("any error code")));
    }

    @Test
    public void getScriptletClassInputMetadata_scriptletServiceReturnsScriptletMetadataObject_returnsOkResponseWithMetadata() throws Exception {
        Map<String, List<StructTypeFieldView>> scriptletInputs = new HashMap<>();
        scriptletInputs.put(
                "Traders",
                asList(
                        anyStructTypeField("TRADER_ID", "Integer"),
                        anyStructTypeField("Name", "String"),
                        anyStructTypeField("TRADE_DATE", "Date")
                ));

        scriptletInputs.put(
                "Trades",
                asList(
                        anyStructTypeField("TRADE_DATE", "Integer"),
                        anyStructTypeField("STOCK", "String"),
                        anyStructTypeField("TradeType", "String")
                ));

        when(scriptletService.getScriptletInputMetadata(anyString(), anyString())).thenReturn(
                Validation.valid(scriptletInputs));

        String expectedJsonString = "{"
                + "'Trades':[{"
                + "'field':'TRADE_DATE',"
                + "'type':'Integer'"
                + "},{"
                + "'field':'STOCK',"
                + "'type':'String'"
                + "},{"
                + "'field':'TradeType',"
                + "'type':'String'"
                + "}],"
                + "'Traders':[{"
                + "'field':'TRADER_ID',"
                + "'type':'Integer'"
                + "},{"
                + "'field':'Name',"
                + "'type':'String'"
                + "},{"
                + "'field':'TRADE_DATE',"
                + "'type':'Date'"
                + "}]}";

        mockMvc.perform(
                get("/api/v1/scriptlets/jars/jar_file_name/classes/classname/metadata?type=input"))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedJsonString.replace("'", "\"")));
    }

    @Test
    public void getScriptletClassOutputMetadata_delegatesJarFileNameToScriptletService() throws Exception {
        mockMvc.perform(
                get("/api/v1/scriptlets/jars/jar_file_name/classes/classname/metadata?type=output"));

        verify(scriptletService).getScriptletOutputMetadata(eq("jar_file_name"), anyString());
    }

    @Test
    public void getScriptletClassOutputMetadata_delegatesClassNameToScriptletService() throws Exception {
        mockMvc.perform(
                get("/api/v1/scriptlets/jars/jar_file_name/classes/classname/metadata?type=output"));

        verify(scriptletService).getScriptletOutputMetadata(anyString(), eq("classname"));
    }

    @Test
    public void getScriptletClassOutputMetadata_scriptletServiceReturnsErrorResponse_ReturnsBadRequestError() throws Exception {
        when(scriptletService.getScriptletOutputMetadata(anyString(), anyString())).thenReturn(Validation.invalid(
                ErrorResponse.valueOf(
                        "Any error message", "any error code")));

        mockMvc.perform(
                get("/api/v1/scriptlets/jars/jar_file_name/classes/classname/metadata?type=output"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorMessage", equalTo("Any error message")))
                .andExpect(jsonPath("$.errorCode", equalTo("any error code")));
    }

    @Test
    public void getScriptletClassOutputMetadata_scriptletServiceReturnsScriptletMetadataObject_returnsOkResponseWithMetadata() throws Exception {
        List<StructTypeFieldView> scriptletOutput = asList(
                anyStructTypeField("TRADER_ID", "Integer"),
                anyStructTypeField("Name", "String"),
                anyStructTypeField("STOCK", "String"),
                anyStructTypeField("TradeType", "String"),
                anyStructTypeField("Total", "Long"));

        when(scriptletService.getScriptletOutputMetadata(anyString(), anyString())).thenReturn(
                Validation.valid(scriptletOutput));

        String expectedJsonString = "[{'"
                + "field':'TRADER_ID',"
                + "'type':'Integer'"
                + "},{"
                + "'field':'Name',"
                + "'type':'String'"
                + "},{"
                + "'field':'STOCK',"
                + "'type':'String'"
                + "},{"
                + "'field':'TradeType',"
                + "'type':'String'"
                + "},{"
                + "'field':'Total',"
                + "'type':'Long'"
                + "}]";

        mockMvc.perform(
                get("/api/v1/scriptlets/jars/jar_file_name/classes/classname/metadata?type=output"))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedJsonString.replace("'", "\"")));
    }

    StructTypeFieldView anyStructTypeField(final String fieldName, final String fieldType) {
        return StructTypeFieldView.builder()
                .field(fieldName)
                .type(fieldType)
                .build();
    }
}