package com.lombardrisk.ignis.server.controller.productconfig;

import com.lombardrisk.ignis.client.external.fixture.ExternalClient;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.server.config.IntegrationTestConfig;
import com.lombardrisk.ignis.server.job.product.ProductConfigImportJobService;
import com.lombardrisk.ignis.server.product.fixture.ProductPopulated;
import com.lombardrisk.ignis.server.product.productconfig.ProductConfigFileService;
import com.lombardrisk.ignis.server.product.productconfig.ProductConfigService;
import com.lombardrisk.ignis.server.product.productconfig.model.ImportStatus;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductConfig;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductConfigFileContents;
import io.vavr.control.Either;
import io.vavr.control.Validation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;

import static com.lombardrisk.ignis.test.config.AdminUser.BASIC_AUTH;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@IntegrationTestConfig
public class ProductConfigControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductConfigFileService productConfigFileService;
    @MockBean
    private ProductConfigService productConfigService;
    @MockBean
    private ProductConfigImportJobService productConfigImporter;

    @Captor
    private ArgumentCaptor<ProductConfig> productCaptor;

    @Before
    public void setUp() throws IOException {
        when(productConfigFileService.readProductConfig(any()))
                .thenReturn(Validation.valid(ProductConfigFileContents.builder().build()));

        when(productConfigService.findView(anyLong()))
                .thenReturn(Validation.valid(ExternalClient.Populated.productConfigView().id(0L).build()));

        when(productConfigService.saveProductConfig(any()))
                .thenReturn(Either.right(ProductPopulated.productConfig().build()));
    }

    @Test
    public void deleteProductConfig_ReturnsOkResponse() throws Exception {
        when(productConfigImporter.rollbackProductConfig(any(), any()))
                .thenReturn(Validation.valid(
                        ProductPopulated.productConfig().id(3456L).build()));

        mockMvc.perform(
                delete("/api/v1/productConfigs/3456")
                        .with(BASIC_AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3456))
                .andExpect(jsonPath("$.*").value(3456));
    }

    @Test
    public void deleteProductConfig_ReturnsErrorResponse() throws Exception {

        when(productConfigImporter.rollbackProductConfig(any(), any()))
                .thenReturn(Validation.invalid(
                        ErrorResponse.valueOf("err0r", "err1")));

        mockMvc.perform(
                delete("/api/v1/productConfigs/3456")
                        .with(BASIC_AUTH))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].errorCode").value("err1"))
                .andExpect(jsonPath("$[0].errorMessage").value("err0r"));
    }

    @Test
    public void importProductConfig_ZipNotValid_ReturnsBadRequestResponse() throws Exception {
        when(productConfigFileService.readProductConfig(any()))
                .thenReturn(Validation.invalid(ErrorResponse.valueOf("product config zip is invalid", "invalid")));

        mockMvc.perform(
                multipart("/api/v1/productConfigs/file")
                        .file(new MockMultipartFile("file", "test.zip", "application/zip", new byte[]{}))
                        .with(BASIC_AUTH))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].errorCode").value("invalid"))
                .andExpect(jsonPath("$[0].errorMessage").value("product config zip is invalid"));
    }

    @Test
    public void importProductConfig_CallsProductConfigFileService() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.zip", "application/zip", new byte[]{});

        mockMvc.perform(multipart("/api/v1/productConfigs/file").file(file).with(BASIC_AUTH));

        verify(productConfigFileService).readProductConfig(file);
    }

    @Test
    public void importProductConfig_ReturnsErrorResponse() throws Exception {
        when(productConfigImporter.importProductConfig(any(), any()))
                .thenReturn(Validation.invalid(singletonList(
                        ErrorResponse.valueOf("oops", "err"))));

        mockMvc.perform(multipart("/api/v1/productConfigs/file")
                .file(new MockMultipartFile("file", "test.zip", "application/zip", new byte[]{}))
                .with(BASIC_AUTH))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].errorCode").value("err"))
                .andExpect(jsonPath("$[0].errorMessage").value("oops"));
    }

    @Test
    public void getProductConfigs_ReturnsOkResponseWithEntity() throws Exception {
        when(productConfigService.findAllViewsWithoutFieldsRulesAndSteps())
                .thenReturn(singletonList(ExternalClient.Populated.productConfigView()
                        .id(1234L)
                        .name("some name")
                        .version("some version")
                        .schemas(emptyList())
                        .importStatus(ImportStatus.ERROR.name())
                        .build()));

        mockMvc.perform(
                get("/api/v1/productConfigs")
                        .with(BASIC_AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", equalTo(1234)))
                .andExpect(jsonPath("$[0].name", equalTo("some name")))
                .andExpect(jsonPath("$[0].version", equalTo("some version")))
                .andExpect(jsonPath("$[0].schemas", equalTo(emptyList())))
                .andExpect(jsonPath("$[0].importStatus", equalTo("ERROR")));
    }
}
