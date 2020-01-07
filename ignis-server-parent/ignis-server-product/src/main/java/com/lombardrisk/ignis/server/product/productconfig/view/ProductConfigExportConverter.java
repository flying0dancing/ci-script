package com.lombardrisk.ignis.server.product.productconfig.view;

import com.lombardrisk.ignis.common.MapperUtils;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductConfig;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductConfigFileContents;
import com.lombardrisk.ignis.server.product.table.view.SchemaExportConverter;

import java.util.function.Function;

public class ProductConfigExportConverter implements Function<ProductConfigFileContents, ProductConfig> {

    private final SchemaExportConverter schemaExportConverter;

    public ProductConfigExportConverter(final SchemaExportConverter schemaExportConverter) {
        this.schemaExportConverter = schemaExportConverter;
    }

    @Override
    public ProductConfig apply(final ProductConfigFileContents productConfigExport) {
        return ProductConfig.builder()
                .name(productConfigExport.getProductMetadata().getName())
                .version(productConfigExport.getProductMetadata().getVersion())
                .tables(MapperUtils.mapSet(productConfigExport.getSchemas(), schemaExportConverter::apply))
                .build();
    }
}
