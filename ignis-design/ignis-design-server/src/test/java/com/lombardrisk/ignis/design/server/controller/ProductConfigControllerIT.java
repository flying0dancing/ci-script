package com.lombardrisk.ignis.design.server.controller;

import com.lombardrisk.ignis.client.design.productconfig.NewProductConfigRequest;
import com.lombardrisk.ignis.client.design.productconfig.ProductConfigDto;
import com.lombardrisk.ignis.client.design.productconfig.UpdateProductConfig;
import com.lombardrisk.ignis.client.design.productconfig.validation.ProductConfigTaskList;
import com.lombardrisk.ignis.client.external.fixture.ExternalClient;
import com.lombardrisk.ignis.data.common.Identifiable;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.design.server.DesignStudioTestConfig;
import com.lombardrisk.ignis.design.server.productconfig.ProductConfig;
import com.lombardrisk.ignis.design.server.productconfig.ProductConfigImportService;
import com.lombardrisk.ignis.design.server.productconfig.ProductConfigService;
import com.lombardrisk.ignis.design.server.productconfig.export.ImportProductRequest;
import com.lombardrisk.ignis.design.server.productconfig.export.ProductConfigExportFileService;
import com.lombardrisk.ignis.design.server.productconfig.export.ProductConfigExportFileService.ProductZipOutputStream;
import com.lombardrisk.ignis.design.server.productconfig.export.ProductConfigImportFileService;
import com.lombardrisk.ignis.design.server.productconfig.validation.ProductConfigValidator;
import io.vavr.control.Validation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import static com.lombardrisk.ignis.common.json.MapperWrapper.MAPPER;
import static com.lombardrisk.ignis.design.server.fixtures.Design.Populated;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@DesignStudioTestConfig
public class ProductConfigControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductConfigService productConfigService;
    @MockBean
    private ProductConfigImportService productConfigImportService;
    @MockBean
    private ProductConfigExportFileService productConfigExportService;
    @MockBean
    private ProductConfigImportFileService productConfigImportFileService;
    @MockBean
    private ProductConfigValidator productConfigValidator;

    @Before
    public void setUp() throws Exception {
        when(productConfigService.findWithValidation(anyLong()))
                .thenReturn(Validation.valid(Populated.productConfig().build()));
        when(productConfigExportService.exportProduct(anyLong(), any(), any()))
                .thenReturn(Validation.valid(
                        new ProductZipOutputStream<>("zipName.zip", new ByteArrayOutputStream())));

        ImportProductRequest successfulImport = ImportProductRequest.builder()
                .newProductConfigRequest(Populated.newProductRequest().build())
                .schemaExports(singletonList(ExternalClient.Populated.schemaExport().build()))
                .build();

        Identifiable productId = () -> 192L;
        when(productConfigImportService.importProductConfig(any(), any()))
                .thenReturn(Validation.valid(productId));
    }

    @Test
    public void getProductConfigs_ReturnsOkResponseWithEntity() throws Exception {
        ProductConfigDto productConfig = Populated.productConfigDto()
                .id(1234L)
                .name("name")
                .version("version")
                .build();

        when(productConfigService.findAllProductConfigs())
                .thenReturn(singletonList(productConfig));

        mockMvc.perform(
                get("/api/v1/productConfigs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1234))
                .andExpect(jsonPath("$[0].name").value("name"))
                .andExpect(jsonPath("$[0].version").value("version"));
    }

    @Test
    public void getOneProductConfig_ProductExistsReturns_OkResponseWithEntity() throws Exception {
        ProductConfigDto productConfig = Populated.productConfigDto()
                .id(1234L)
                .name("name")
                .version("version")
                .build();

        when(productConfigService.findOne(anyLong()))
                .thenReturn(Validation.valid(productConfig));

        mockMvc.perform(
                get("/api/v1/productConfigs/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1234))
                .andExpect(jsonPath("$.name").value("name"))
                .andExpect(jsonPath("$.version").value("version"));
    }

    @Test
    public void getOneProductConfig_ProductDoesNotExist_ReturnsBadRequestFailure() throws Exception {
        ProductConfigDto productConfig = Populated.productConfigDto()
                .id(1234L)
                .name("name")
                .version("version")
                .build();

        when(productConfigService.findOne(anyLong()))
                .thenReturn(Validation.invalid(CRUDFailure.notFoundIds("Product", 123L)));

        mockMvc.perform(
                get("/api/v1/productConfigs/123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].errorCode").value("NOT_FOUND"))
                .andExpect(jsonPath("$[0].errorMessage").value("Could not find Product for ids [123]"));
    }

    @Test
    public void saveProductConfig_ServiceReturnsProductConfig_ReturnsOkResponse() throws Exception {
        ProductConfig productConfig = Populated.productConfig()
                .id(123456789L)
                .name("new product config")
                .build();

        when(productConfigService.createProductConfig(any(NewProductConfigRequest.class)))
                .thenReturn(Validation.valid(productConfig));

        mockMvc.perform(
                post("/api/v1/productConfigs")
                        .contentType(APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(Populated.newProductRequest().build())))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.id").value("123456789"))
                .andExpect(jsonPath("$.name").value("new product config"));
    }

    @Test
    public void saveProductConfig_CallsProductConfigService() throws Exception {
        NewProductConfigRequest productConfigRequest = NewProductConfigRequest.builder()
                .name("my product config")
                .version("my version")
                .build();

        mockMvc.perform(
                post("/api/v1/productConfigs")
                        .contentType(APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(productConfigRequest)));

        ArgumentCaptor<NewProductConfigRequest> productConfigRequestCaptor
                = ArgumentCaptor.forClass(NewProductConfigRequest.class);
        verify(productConfigService).createProductConfig(productConfigRequestCaptor.capture());

        assertThat(productConfigRequestCaptor.getValue())
                .isEqualTo(productConfigRequest);
    }

    @Test
    public void deleteProductConfig_ProductConfigFound_ReturnsOkResponse() throws Exception {
        ProductConfig productConfig = Populated.productConfig()
                .id(123456789L)
                .build();

        when(productConfigService.deleteById(anyLong()))
                .thenReturn(Validation.valid(productConfig));

        mockMvc.perform(
                delete("/api/v1/productConfigs/123456789"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("123456789"));
    }

    @Test
    public void deleteProductConfig_ProductConfigNotFound_ReturnsBadRequestResponse() throws Exception {
        when(productConfigService.deleteById(anyLong()))
                .thenReturn(Validation.invalid(CRUDFailure.notFoundIds(
                        "PRODUCT_CONFIGURATION",
                        singletonList(123456789L))));

        mockMvc.perform(
                delete("/api/v1/productConfigs/123456789"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].errorCode").value("NOT_FOUND"))
                .andExpect(jsonPath("$[0].errorMessage")
                        .value("Could not find PRODUCT_CONFIGURATION for ids [123456789]"));
    }

    @Test
    public void exportProductConfig_CallsProductConfigExportFileService() throws Exception {
        mockMvc.perform(
                get("/api/v1/productConfigs/123/file/1"));

        verify(productConfigExportService).exportProduct(eq(123L), eq(Arrays.asList(1L)),
                any(ByteArrayOutputStream.class));
    }

    @Test
    public void exportProductConfigUsingRequiredPipelineIds_CallsProductConfigExportFileService() throws Exception {
        mockMvc.perform(
                get("/api/v1/productConfigs/123/file/1,2,3"));

        verify(productConfigExportService).exportProduct(eq(123L), eq(Arrays.asList(1L, 2L, 3L)),
                any(ByteArrayOutputStream.class));
    }

    @Test
    public void exportProductConfig_ProductConfigNotFound_ReturnsBadRequestResponse() throws Exception {
        when(productConfigExportService.exportProduct(anyLong(), any(), any()))
                .thenReturn(Validation.invalid(CRUDFailure.notFoundIds("ProductConfig", 123L)));

        mockMvc.perform(
                get("/api/v1/productConfigs/123/file"))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$[0].errorCode").value("NOT_FOUND"))
                .andExpect(jsonPath("$[0].errorMessage")
                        .value("Could not find ProductConfig for ids [123]"));
    }

    @Test
    public void exportProductConfig_FileServiceReturnsErrorMessage_ReturnsBadRequestResponse() throws Exception {
        when(productConfigExportService.exportProduct(anyLong(), any(), any()))
                .thenThrow(new IOException());

        mockMvc.perform(
                get("/api/v1/productConfigs/123/file"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$[0].errorMessage").value(
                        "An unexpected error occurred. If it persists please contact your FCR administrator"));
    }

    @Test
    public void exportProductConfig_FileServiceReturnsNoErrorMessage_ReturnsOkResponse() throws Exception {
        ByteArrayOutputStream zipByteArray = new ByteArrayOutputStream();
        zipByteArray.write("abcd".getBytes());

        when(productConfigExportService.exportProduct(anyLong(), any(), any()))
                .thenReturn(Validation.valid(
                        new ProductZipOutputStream<>("zipName.zip", zipByteArray)));

        MvcResult mvcResult = mockMvc.perform(
                get("/api/v1/productConfigs/123/file"))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();

        assertThat(response.getHeader(HttpHeaders.CONTENT_DISPOSITION))
                .isEqualTo("attachment; filename=\"zipName.zip\"");
        assertThat(response.getHeader("Cache-Control"))
                .isEqualTo("no-cache, no-store, must-revalidate");
        assertThat(response.getContentType())
                .isEqualTo("application/zip");
        assertThat(response.getContentLength())
                .isEqualTo(4);
        assertThat(response.getContentAsString())
                .isEqualTo("abcd");
    }

    @Test
    public void getProductConfigTaskList_ProductConfigNotFound_ReturnsBadRequestResponse() throws Exception {
        when(productConfigValidator.productConfigTaskList(anyLong()))
                .thenReturn(Validation.invalid(CRUDFailure.notFoundIds("ProductConfig", 123L)));

        mockMvc.perform(
                get("/api/v1/productConfigs/123/tasks"))
                .andExpect(status().isBadRequest())
                .andDo(print())
                .andExpect(jsonPath("$[0].errorCode").value("NOT_FOUND"))
                .andExpect(jsonPath("$[0].errorMessage")
                        .value("Could not find ProductConfig for ids [123]"));
    }

    @Test
    public void getProductConfigTaskList_ReturnsOkResponseWithEntity() throws Exception {
        when(productConfigValidator.productConfigTaskList(anyLong()))
                .thenReturn(Validation.valid(ProductConfigTaskList
                        .builder()
                        .productId(1234L)
                        .pipelineTasks(emptyMap())
                        .build()));

        mockMvc.perform(
                get("/api/v1/productConfigs/123/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(1234))
                .andExpect(jsonPath("$.pipelineTasks").value(""));
    }

    @Test
    public void updateProduct_ProductNotFound_ReturnsError() throws Exception {
        when(productConfigService.updateProduct(anyLong(), any()))
                .thenReturn(Validation.invalid(CRUDFailure.notFoundIds("ProductConfig", 123L)));

        mockMvc.perform(
                patch("/api/v1/productConfigs/123?type=product")
                        .contentType(APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(UpdateProductConfig.builder().build())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].errorCode").value("NOT_FOUND"))
                .andExpect(jsonPath("$[0].errorMessage")
                        .value("Could not find ProductConfig for ids [123]"));
    }

    @Test
    public void updateProduct_CallsServiceWithUpdate() throws Exception {
        UpdateProductConfig updateProductConfig = UpdateProductConfig.builder()
                .name("newName")
                .version("newVersion")
                .build();

        mockMvc.perform(
                patch("/api/v1/productConfigs/123?type=product")
                        .contentType(APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(updateProductConfig)));

        verify(productConfigService).updateProduct(123L, updateProductConfig);
    }

    @Test
    public void importProductConfig_ReturnsOkResponse() throws Exception {
        when(productConfigImportService.importProductConfig(any(), any()))
                .thenReturn(Validation.valid(() -> 100L));

        mockMvc.perform(
                multipart("/api/v1/productConfigs/file")
                        .file(new MockMultipartFile("file", "test.zip", "application/zip", new byte[]{})))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("100"));
    }

    @Test
    public void importProductConfig_ProductConfigNotValid_ReturnsBadRequestResponse() throws Exception {
        when(productConfigImportService.importProductConfig(any(), any()))
                .thenReturn(Validation.invalid(singletonList(
                        CRUDFailure.constraintFailure("Name exists").toErrorResponse())));

        mockMvc.perform(
                multipart("/api/v1/productConfigs/file")
                        .file(new MockMultipartFile("file", "test.zip", "application/zip", new byte[]{})))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].errorCode").value("CONSTRAINT_VIOLATION"))
                .andExpect(jsonPath("$[0].errorMessage").value("Name exists"));
    }
}
