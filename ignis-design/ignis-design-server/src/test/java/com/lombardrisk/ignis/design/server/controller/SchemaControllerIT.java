package com.lombardrisk.ignis.design.server.controller;

import com.lombardrisk.ignis.client.design.schema.CopySchemaRequest;
import com.lombardrisk.ignis.client.design.schema.NewSchemaVersionRequest;
import com.lombardrisk.ignis.client.design.schema.UpdateSchema;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.design.server.DesignStudioTestConfig;
import com.lombardrisk.ignis.design.server.fixtures.Design.Populated;
import com.lombardrisk.ignis.design.server.productconfig.ProductConfigService;
import com.lombardrisk.ignis.design.server.productconfig.schema.Schema;
import com.lombardrisk.ignis.design.server.productconfig.schema.SchemaService;
import com.lombardrisk.ignis.design.server.productconfig.schema.SchemaService.SchemaCsvOutputStream;
import io.vavr.control.Validation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;

import static com.lombardrisk.ignis.client.design.fixtures.Populated.copySchemaRequest;
import static com.lombardrisk.ignis.common.json.MapperWrapper.MAPPER;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@DesignStudioTestConfig
public class SchemaControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductConfigService productConfigService;

    @MockBean
    private SchemaService schemaService;

    @Test
    public void updateSchema_SchemaNotFound_ShouldReturnBadRequest() throws Exception {
        when(schemaService.updateWithoutFields(anyLong(), anyLong(), any(UpdateSchema.class)))
                .thenReturn(Validation.invalid(CRUDFailure.notFoundIds("Schema", 333L)));

        mockMvc.perform(
                patch("/api/v1/productConfigs/0/schemas/333")
                        .contentType(APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(Populated.schemaUpdateRequest().build())))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].errorMessage", equalTo("Could not find Schema for ids [333]")))
                .andExpect(jsonPath("$[0].errorCode", equalTo("NOT_FOUND")));
    }

    @Test
    public void updateSchemaShouldCallUpdateOnSchemaService() throws Exception {
        UpdateSchema updateRequest = Populated.schemaUpdateRequest().build();
        when(schemaService.updateWithoutFields(anyLong(), anyLong(), any(UpdateSchema.class)))
                .thenReturn(Validation.valid(Populated.schema().build()));

        mockMvc.perform(
                patch("/api/v1/productConfigs/1982/schemas/333")
                        .contentType(APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(updateRequest)));

        verify(schemaService).updateWithoutFields(eq(1982L), eq(333L), eq(updateRequest));
    }

    @Test
    public void updateSchemaShouldReturnId() throws Exception {
        UpdateSchema updateRequest = Populated.schemaUpdateRequest().build();

        when(schemaService.updateWithoutFields(anyLong(), anyLong(), any(UpdateSchema.class)))
                .thenReturn(Validation.valid(Populated.schema().id(453L).build()));

        mockMvc.perform(
                patch("/api/v1/productConfigs/0/schemas/453")
                        .contentType(APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(453)));
    }

    @Test
    public void addSchemaToProduct_ProductNotFound_ReturnsError() throws Exception {
        when(productConfigService.createNewSchemaOnProduct(anyLong(), any()))
                .thenReturn(Validation.invalid(singletonList(
                        CRUDFailure.notFoundIds("ProductConfig", 123L))));

        mockMvc.perform(
                post("/api/v1/productConfigs/123/schemas")
                        .contentType(APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(Populated.createSchemaRequest().build())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].errorCode").value("NOT_FOUND"))
                .andExpect(jsonPath("$[0].errorMessage")
                        .value("Could not find ProductConfig for ids [123]"));
    }

    @Test
    public void addSchemaToProduct_ProductFound_ReturnsNewSchema() throws Exception {
        Schema table = Populated.schema()
                .physicalTableName("table1")
                .majorVersion(1)
                .fields(emptySet())
                .validationRules(emptySet())
                .build();

        when(productConfigService.createNewSchemaOnProduct(anyLong(), any()))
                .thenReturn(Validation.valid(table));

        mockMvc.perform(
                post("/api/v1/productConfigs/123/schemas")
                        .contentType(APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(Populated.createSchemaRequest().build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.physicalTableName").value("table1"))
                .andExpect(jsonPath("$.majorVersion").value("1"));
    }

    @Test
    public void createNewSchemaVersion_NotFound_ReturnsError() throws Exception {
        when(productConfigService.createNewSchemaVersion(anyLong(), any(), any()))
                .thenReturn(Validation.invalid(CRUDFailure.notFoundIds("ProductConfig", 123L)));

        mockMvc.perform(
                post("/api/v1/productConfigs/123/schemas/100")
                        .contentType(APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(Populated.newSchemaVersionRequest().build())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].errorCode").value("NOT_FOUND"))
                .andExpect(jsonPath("$[0].errorMessage")
                        .value("Could not find ProductConfig for ids [123]"));
    }

    @Test
    public void createNewSchemaVersion_ProductFound_ReturnsNewSchema() throws Exception {
        Schema schema = Populated.schema()
                .id(100L)
                .displayName("LIQUIDITY")
                .physicalTableName("RIEG001")
                .majorVersion(1)
                .fields(emptySet())
                .validationRules(emptySet())
                .build();

        when(productConfigService.createNewSchemaVersion(anyLong(), any(), any()))
                .thenReturn(Validation.valid(schema));

        mockMvc.perform(
                post("/api/v1/productConfigs/123/schemas/100")
                        .contentType(APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(Populated.newSchemaVersionRequest().build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("100"))
                .andExpect(jsonPath("$.displayName").value("LIQUIDITY"))
                .andExpect(jsonPath("$.physicalTableName").value("RIEG001"))
                .andExpect(jsonPath("$.majorVersion").value("1"));
    }

    @Test
    public void createNewSchemaVersion_CallsServiceWithRequest() throws Exception {
        Schema schema = Populated.schema().build();

        when(productConfigService.createNewSchemaVersion(anyLong(), any(), any()))
                .thenReturn(Validation.valid(schema));

        NewSchemaVersionRequest createNewSchemaVersion = Populated.newSchemaVersionRequest()
                .startDate(LocalDate.of(2015, 1, 1))
                .build();

        mockMvc.perform(
                post("/api/v1/productConfigs/123/schemas/100")
                        .content(MAPPER.writeValueAsString(createNewSchemaVersion))
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(productConfigService).createNewSchemaVersion(123L, 100L, createNewSchemaVersion);
    }

    @Test
    public void delete_SchemaFound_ReturnsId() throws Exception {
        when(productConfigService.removeSchemaFromProduct(anyLong(), anyLong()))
                .thenReturn(Validation.valid(() -> 333L));

        mockMvc.perform(
                delete("/api/v1/productConfigs/0/schemas/333"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(333)));
    }

    @Test
    public void delete_SchemaNotFound_ReturnsError() throws Exception {
        when(productConfigService.removeSchemaFromProduct(anyLong(), anyLong()))
                .thenReturn(Validation.invalid(CRUDFailure.notFoundIds("Schema", 333L)));

        mockMvc.perform(
                delete("/api/v1/productConfigs/0/schemas/333"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].errorCode", equalTo("NOT_FOUND")))
                .andExpect(jsonPath("$[0].errorMessage", equalTo("Could not find Schema for ids [333]")));
    }

    @Test
    public void copySchema_Successful_ReturnsNewSchema() throws Exception {
        Schema schema = Populated.schema()
                .id(100L)
                .displayName("LIQUIDITY")
                .physicalTableName("RIEG001")
                .majorVersion(1)
                .fields(emptySet())
                .validationRules(emptySet())
                .build();

        when(schemaService.copySchema(anyLong(), any(), any()))
                .thenReturn(Validation.valid(schema));

        CopySchemaRequest copySchemaRequest = copySchemaRequest().build();

        mockMvc.perform(
                post("/api/v1/productConfigs/123/schemas/100/copy")
                        .contentType(APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(copySchemaRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("100"))
                .andExpect(jsonPath("$.displayName").value("LIQUIDITY"))
                .andExpect(jsonPath("$.physicalTableName").value("RIEG001"))
                .andExpect(jsonPath("$.majorVersion").value("1"));

        verify(schemaService).copySchema(123L, 100L, copySchemaRequest);
    }

    @Test
    public void copySchema_NotFound_ReturnsError() throws Exception {
        when(schemaService.copySchema(anyLong(), any(), any()))
                .thenReturn(Validation.invalid(singletonList(
                        CRUDFailure.notFoundIds("Schema", 100L))));

        mockMvc.perform(
                post("/api/v1/productConfigs/123/schemas/100/copy")
                        .contentType(APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(copySchemaRequest().build())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].errorCode").value("NOT_FOUND"))
                .andExpect(jsonPath("$[0].errorMessage")
                        .value("Could not find Schema for ids [100]"));
    }


    @Test
    public void createExampleCsv_FileServiceReturnsNoErrorMessage_ReturnsOkResponse() throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write("abcd".getBytes());

        Schema toDownload = Populated.schema("toDownload")
                .build();
        when(schemaService.createExampleCsv(anyLong(), any()))
                .thenReturn(Validation.valid(
                        new SchemaCsvOutputStream<>(toDownload, byteArrayOutputStream)));

        MvcResult mvcResult = mockMvc.perform(
                get("/api/v1/productConfigs/123/schemas/456/exampleCsv"))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();

        assertThat(response.getHeader(HttpHeaders.CONTENT_DISPOSITION))
                .isEqualTo("attachment; filename=\"toDownload-example.csv\"");
        assertThat(response.getHeader("Cache-Control"))
                .isEqualTo("no-cache, no-store, must-revalidate");
        assertThat(response.getContentType())
                .isEqualTo("text/csv");
        assertThat(response.getContentLength())
                .isEqualTo(4);
        assertThat(response.getContentAsString())
                .isEqualTo("abcd");
    }
}
