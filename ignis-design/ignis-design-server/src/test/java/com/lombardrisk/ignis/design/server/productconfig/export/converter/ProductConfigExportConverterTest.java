package com.lombardrisk.ignis.design.server.productconfig.export.converter;

import com.lombardrisk.ignis.client.external.productconfig.export.ProductConfigExport;
import com.lombardrisk.ignis.client.external.productconfig.export.SchemaExport;
import com.lombardrisk.ignis.common.time.TimeSource;
import com.lombardrisk.ignis.design.server.productconfig.ProductConfig;
import com.lombardrisk.ignis.design.server.productconfig.schema.Schema;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;

import java.time.Clock;
import java.util.Collections;

import static com.lombardrisk.ignis.design.server.fixtures.Design.Populated;

public class ProductConfigExportConverterTest {

    private final ProductConfigExportConverter productConfigConverter = new ProductConfigExportConverter(
            new SchemaExportConverter(new TimeSource(Clock.systemUTC()))
    );

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Test
    public void apply_ConvertsProductConfigToProductConfigView() {
        Schema table = Populated.schema()
                .displayName("table1")
                .physicalTableName("tbl1")
                .build();

        ProductConfig productConfig = Populated.productConfig()
                .id(12345L)
                .tables(Collections.singleton(table))
                .build();

        ProductConfigExport view = productConfigConverter.apply(productConfig);

        soft.assertThat(view.getId()).isEqualTo(12345L);
        soft.assertThat(view.getName()).isEqualTo("default name");
        soft.assertThat(view.getVersion()).isEqualTo("default version");
        soft.assertThat(view.getTables())
                .extracting(SchemaExport::getDisplayName)
                .containsExactly("table1");
        soft.assertThat(view.getTables())
                .extracting(SchemaExport::getPhysicalTableName)
                .containsExactly("tbl1");
    }

    @Test
    public void inverse_ConvertsBackToProduct() {
        Schema table = Populated.schema().build();

        ProductConfig original = Populated.productConfig()
                .name("product1")
                .version("1.0.0-RELEASE")
                .tables(Collections.singleton(table))
                .build();

        ProductConfigExport view = productConfigConverter.apply(original);

        ProductConfig reConverted = productConfigConverter.inverse().apply(view);
        soft.assertThat(reConverted.getName())
                .isEqualTo("product1");
        soft.assertThat(reConverted.getVersion())
                .isEqualTo("1.0.0-RELEASE");
    }
}