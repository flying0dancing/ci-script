package com.lombardrisk.ignis.server.controller.external.converter.request;

import com.lombardrisk.ignis.client.external.fixture.ExternalClient;
import com.lombardrisk.ignis.client.external.productconfig.export.SchemaExport;
import com.lombardrisk.ignis.server.product.fixture.ProductPopulated;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductConfig;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductConfigFileContents;
import com.lombardrisk.ignis.server.product.productconfig.view.ProductConfigExportConverter;
import com.lombardrisk.ignis.server.product.table.model.Table;
import com.lombardrisk.ignis.server.product.table.view.SchemaExportConverter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProductConfigExportConverterTest {

    @Mock
    private SchemaExportConverter schemaViewConverter;
    @InjectMocks
    private ProductConfigExportConverter productConfigViewConverter;

    @Test
    public void apply_SetsName() {
        ProductConfigFileContents productConfigView = ProductPopulated.productConfigFileContents()
                .productMetadata(ProductPopulated.productManifest()
                        .name("product name")
                        .build())
                .build();

        ProductConfig productConfig = productConfigViewConverter.apply(productConfigView);

        assertThat(productConfig.getName())
                .isEqualTo("product name");
    }

    @Test
    public void apply_SetsVersion() {
        ProductConfigFileContents productConfigView = ProductPopulated.productConfigFileContents()
                .productMetadata(ProductPopulated.productManifest()
                        .version("version")
                        .build())
                .build();

        ProductConfig productConfig = productConfigViewConverter.apply(productConfigView);

        assertThat(productConfig.getVersion())
                .isEqualTo("version");
    }

    @Test
    public void apply_SetsTables() {
        SchemaExport schemaView1 = ExternalClient.Populated.schemaExport().physicalTableName("table 1").build();
        SchemaExport schemaView2 = ExternalClient.Populated.schemaExport().physicalTableName("table 2").build();
        SchemaExport schemaView3 = ExternalClient.Populated.schemaExport().physicalTableName("table 3").build();
        SchemaExport schemaView4 = ExternalClient.Populated.schemaExport().physicalTableName("table 4").build();

        Table table1 = ProductPopulated.table().physicalTableName("table 1").build();
        Table table2 = ProductPopulated.table().physicalTableName("table 2").build();
        Table table3 = ProductPopulated.table().physicalTableName("table 2").build();
        Table table4 = ProductPopulated.table().physicalTableName("table 2").build();
        when(schemaViewConverter.apply(schemaView1))
                .thenReturn(table1);
        when(schemaViewConverter.apply(schemaView2))
                .thenReturn(table2);
        when(schemaViewConverter.apply(schemaView3))
                .thenReturn(table3);
        when(schemaViewConverter.apply(schemaView4))
                .thenReturn(table4);

        ProductConfigFileContents productConfigView = ProductPopulated.productConfigFileContents()
                .schemas(asList(schemaView1, schemaView2, schemaView3, schemaView4))
                .build();

        ProductConfig productConfig = productConfigViewConverter.apply(productConfigView);

        assertThat(productConfig.getTables())
                .contains(table1, table2, table3, table4);
    }

    @Test
    public void apply_DoesNotSetId() {
        ProductConfigFileContents productConfigView = ProductPopulated.productConfigFileContents()
                .build();

        ProductConfig productConfig = productConfigViewConverter.apply(productConfigView);

        assertThat(productConfig.getId())
                .isNull();
    }
}
