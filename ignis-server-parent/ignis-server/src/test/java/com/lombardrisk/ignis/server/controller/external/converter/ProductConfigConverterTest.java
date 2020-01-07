package com.lombardrisk.ignis.server.controller.external.converter;

import com.lombardrisk.ignis.client.external.fixture.ExternalClient;
import com.lombardrisk.ignis.client.external.productconfig.export.ProductConfigExport;
import com.lombardrisk.ignis.client.external.productconfig.export.SchemaExport;
import com.lombardrisk.ignis.server.product.fixture.ProductPopulated;
import com.lombardrisk.ignis.server.product.productconfig.model.ImportStatus;
import com.lombardrisk.ignis.server.product.productconfig.view.ProductConfigConverter;
import com.lombardrisk.ignis.server.product.table.model.Table;
import com.lombardrisk.ignis.server.product.table.view.TableConverter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProductConfigConverterTest {

    @Mock
    private TableConverter tableConverter;
    @InjectMocks
    private ProductConfigConverter productConfigConverter;

    @Before
    public void setup() {
        when(tableConverter.apply(any()))
                .thenReturn(ExternalClient.Populated.schemaExport().build());
    }

    @Test
    public void apply_WithId_ReturnsProductConfigViewWithId() {
        ProductConfigExport productConfigView = productConfigConverter.apply(
                ProductPopulated.productConfig()
                        .id(12345L)
                        .build());

        assertThat(productConfigView.getId()).isEqualTo(12345L);
    }

    @Test
    public void apply_WithName_ReturnsProductConfigViewWithName() {
        ProductConfigExport productConfigView = productConfigConverter.apply(
                ProductPopulated.productConfig()
                        .name("12L")
                        .build());

        assertThat(productConfigView.getName()).isEqualTo("12L");
    }

    @Test
    public void apply_WithVersion_ReturnsProductConfigViewWithVersion() {
        ProductConfigExport productConfigView = productConfigConverter.apply(
                ProductPopulated.productConfig()
                        .version("1.44")
                        .build());

        assertThat(productConfigView.getVersion()).isEqualTo("1.44");
    }

    @Test
    public void apply_WithImportStatus_ReturnsProductConfigViewWithImportStatus() {
        ProductConfigExport productConfigView = productConfigConverter.apply(
                ProductPopulated.productConfig()
                        .importStatus(ImportStatus.SUCCESS)
                        .build());

        assertThat(productConfigView.getImportStatus()).isEqualTo("SUCCESS");
    }

    @Test
    public void apply_WithNullImportStatus_ReturnsProductConfigViewWithNullImportStatus() {
        ProductConfigExport productConfigView = productConfigConverter.apply(
                ProductPopulated.productConfig()
                        .importStatus(null)
                        .build());

        assertThat(productConfigView.getImportStatus()).isNull();
    }

    @Test
    public void apply_WithTables_ReturnsProductConfigViewWithTables() {
        Table table = ProductPopulated.table().build();
        SchemaExport schemaView = ExternalClient.Populated.schemaExport().build();

        when(tableConverter.apply(table))
                .thenReturn(schemaView);

        ProductConfigExport view = productConfigConverter.apply(
                ProductPopulated.productConfig()
                        .tables(singleton(table))
                        .build());

        assertThat(view.getTables()).containsExactly(schemaView);
    }
}
