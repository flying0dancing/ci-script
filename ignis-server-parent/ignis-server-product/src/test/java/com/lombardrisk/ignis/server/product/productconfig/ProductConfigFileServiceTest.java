package com.lombardrisk.ignis.server.product.productconfig;

import com.google.common.collect.ImmutableMap;
import com.lombardrisk.ignis.client.external.productconfig.export.ProductConfigExport;
import com.lombardrisk.ignis.client.external.productconfig.export.SchemaExport;
import com.lombardrisk.ignis.common.ZipUtils;
import com.lombardrisk.ignis.server.product.ProductConfigZipObjectMapper;
import com.lombardrisk.ignis.server.product.productconfig.ProductConfigFileService;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductConfigFileContents;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import io.vavr.control.Validation;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProductConfigFileServiceTest {

    @Mock
    private ProductConfigZipObjectMapper productConfigZipObjectMapper;
    @InjectMocks
    private ProductConfigFileService fileService;

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Before
    public void setUp() throws Exception {
        when(productConfigZipObjectMapper.readProductConfigView(any()))
                .thenReturn(ProductConfigExport.builder().build());

        when(productConfigZipObjectMapper.readTableView(any()))
                .thenReturn(SchemaExport.builder().build());
    }

    @Test
    public void readProductConfig_WithZippedProductConfig_CallsZipObjectMapper()
            throws Exception {

        Map<String, byte[]> files = ImmutableMap.of(
                "manifest.json", "product config json".getBytes(),
                "table_1.json", "table 1 json".getBytes(),
                "table_2.json", "table 2 json".getBytes(),
                "pipelines/pipeline.json", "pipeline json".getBytes()
        );

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipUtils.writeFiles(files, outputStream);

        MockMultipartFile file =
                new MockMultipartFile("my_product_config.zip", outputStream.toByteArray());

        fileService.readProductConfig(file);

        verify(productConfigZipObjectMapper).readProductConfigView("product config json".getBytes());
        verify(productConfigZipObjectMapper).readTableView("table 1 json".getBytes());
        verify(productConfigZipObjectMapper).readTableView("table 2 json".getBytes());
        verify(productConfigZipObjectMapper).readPipelineView("pipeline json".getBytes());
        verifyNoMoreInteractions(productConfigZipObjectMapper);
    }

    @Test
    public void readProductConfig_WithZippedProductConfig_ReturnsProductConfig()
            throws Exception {

        when(productConfigZipObjectMapper.readProductConfigView(any()))
                .thenReturn(ProductConfigExport.builder()
                        .name("my first product")
                        .version("1.0")
                        .tables(emptyList())
                        .build());

        MultipartFile file = zipFiles(ImmutableMap.of("manifest.json", new byte[]{}));

        Validation<ErrorResponse, ProductConfigFileContents> outputProduct = fileService.readProductConfig(file);

        soft.assertThat(outputProduct.get().getProductMetadata().getName()).isEqualTo("my first product");
        soft.assertThat(outputProduct.get().getProductMetadata().getVersion()).isEqualTo("1.0");
        soft.assertThat(outputProduct.get().getSchemas()).isEmpty();
    }

    @Test
    public void readProductConfig_WithZippedTables_ReturnsProductConfig()
            throws Exception {

        Date firstTableCreatedTime = new Date();
        Date secondTableCreatedTime = new Date();

        when(productConfigZipObjectMapper.readTableView(any()))
                .thenReturn(SchemaExport.builder()
                        .physicalTableName("my first table")
                        .version(1)
                        .createdBy("admin")
                        .createdTime(firstTableCreatedTime)
                        .build())
                .thenReturn(SchemaExport.builder()
                        .physicalTableName("my second table")
                        .version(2)
                        .createdBy("non-admin")
                        .createdTime(secondTableCreatedTime)
                        .build());

        MultipartFile file = zipFiles(ImmutableMap.of(
                "manifest.json", new byte[]{},
                "table_1.json", new byte[]{},
                "table_2.json", new byte[]{}
        ));

        Validation<ErrorResponse, ProductConfigFileContents> outputProduct = fileService.readProductConfig(file);

        soft.assertThat(outputProduct.get().getSchemas())
                .hasSize(2);

        soft.assertThat(outputProduct.get().getSchemas()).extracting(SchemaExport::getPhysicalTableName)
                .contains("my first table", "my second table");

        soft.assertThat(outputProduct.get().getSchemas()).extracting(SchemaExport::getVersion)
                .contains(1, 2);

        soft.assertThat(outputProduct.get().getSchemas()).extracting(SchemaExport::getCreatedBy)
                .contains("admin", "non-admin");

        soft.assertThat(outputProduct.get().getSchemas()).extracting(SchemaExport::getCreatedTime)
                .contains(firstTableCreatedTime, secondTableCreatedTime);
    }

    @Test
    public void readProductConfig_EmptyZip_ReturnsInvalidZipFailure() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
        zipOutputStream.close();
        outputStream.close();

        MockMultipartFile file =
                new MockMultipartFile("file", "empty.zip", "multipart/formdata", outputStream.toByteArray());

        Validation<ErrorResponse, ProductConfigFileContents> productConfigView = fileService.readProductConfig(file);

        assertThat(productConfigView.isInvalid())
                .isTrue();
        assertThat(productConfigView.getError().getErrorMessage())
                .isEqualTo("Product config zip [empty.zip] cannot be empty");
    }

    @Test
    public void readProductConfig_MissingManifest_ReturnsInvalidZipFailure() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
        zipOutputStream.putNextEntry(new ZipEntry("someotherfile.txt"));
        zipOutputStream.write("HELLO WORLD!".getBytes());
        zipOutputStream.close();
        outputStream.close();

        MockMultipartFile file = new MockMultipartFile("nomanifest.zip", outputStream.toByteArray());

        Validation<ErrorResponse, ProductConfigFileContents> productConfigView = fileService.readProductConfig(file);

        assertThat(productConfigView.isInvalid())
                .isTrue();
        assertThat(productConfigView.getError().getErrorMessage())
                .isEqualTo("Product config zip [nomanifest.zip] must have a manifest file");
    }

    @Test
    public void readProductConfig_ManifestJsonInvalid_ReturnsInvalidFormattingFailure() throws Exception {
        when(productConfigZipObjectMapper.readProductConfigView(any()))
                .thenThrow(new IOException("failed to read product config json"));

        MultipartFile file = zipFiles(ImmutableMap.of("manifest.json", "invalid json".getBytes()));

        Validation<ErrorResponse, ProductConfigFileContents> productConfigView = fileService.readProductConfig(file);

        assertThat(productConfigView.isInvalid())
                .isTrue();
        assertThat(productConfigView.getError().getErrorMessage())
                .isEqualTo("Files [manifest.json] are formatted incorrectly");
    }

    @Test
    public void readProductConfig_SchemaJsonInvalid_ReturnsInvalidFormattingFailure() throws Exception {
        when(productConfigZipObjectMapper.readTableView(any()))
                .thenThrow(new IOException("invalid schema json"));

        MultipartFile file = zipFiles(ImmutableMap.of(
                "manifest.json", "valid product config json".getBytes(),
                "schema.json", "invalid schema json".getBytes()
        ));

        Validation<ErrorResponse, ProductConfigFileContents> productConfigView = fileService.readProductConfig(file);

        assertThat(productConfigView.isInvalid())
                .isTrue();
        assertThat(productConfigView.getError().getErrorMessage())
                .isEqualTo("Files [schema.json] are formatted incorrectly");
    }

    @Test
    public void readProductConfig_MultipleInvalidJsonFiles_ReturnsInvalidFormattingFailure() throws Exception {
        when(productConfigZipObjectMapper.readProductConfigView(any()))
                .thenThrow(new IOException("invalid product config json"));

        when(productConfigZipObjectMapper.readTableView(any()))
                .thenThrow(new IOException("invalid schema json"));

        MultipartFile file = zipFiles(ImmutableMap.of(
                "manifest.json", "invalid json".getBytes(),
                "schema1.json", "invalid json".getBytes(),
                "schema2.json", "invalid json".getBytes()
        ));

        Validation<ErrorResponse, ProductConfigFileContents> productConfigView = fileService.readProductConfig(file);

        assertThat(productConfigView.isInvalid())
                .isTrue();
        assertThat(productConfigView.getError().getErrorMessage())
                .startsWith("Files [")
                .contains("manifest.json", "schema1.json", "schema2.json")
                .endsWith("] are formatted incorrectly");
    }

    private static MultipartFile zipFiles(final Map<String, byte[]> files) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ZipUtils.writeFiles(files, outputStream);
        return new MockMultipartFile("my_product_config.zip", outputStream.toByteArray());
    }
}
