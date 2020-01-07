package com.lombardrisk.ignis.design.server.productconfig.export.converter;

import com.lombardrisk.ignis.client.external.productconfig.export.ProductConfigExport;
import com.lombardrisk.ignis.client.external.productconfig.export.SchemaExport;
import com.lombardrisk.ignis.common.function.IsoMorphicFunction1;
import com.lombardrisk.ignis.design.server.productconfig.ProductConfig;
import com.lombardrisk.ignis.design.server.productconfig.schema.Schema;
import io.vavr.Function1;
import lombok.AllArgsConstructor;

import java.util.LinkedHashSet;

import static com.lombardrisk.ignis.common.MapperUtils.mapCollectionOrEmpty;
import static com.lombardrisk.ignis.common.MapperUtils.mapOrEmpty;

@AllArgsConstructor
public class ProductConfigExportConverter implements IsoMorphicFunction1<ProductConfig, ProductConfigExport> {

    private static final long serialVersionUID = -9196482262917664114L;
    private final SchemaExportConverter schemaExportConverter;

    @Override
    public ProductConfigExport apply(final ProductConfig productConfig) {
        return ProductConfigExport.builder()
                .id(productConfig.getId())
                .name(productConfig.getName())
                .version(productConfig.getVersion())
                .tables(mapOrEmpty(productConfig.getTables(), schemaExportConverter))
                .build();
    }

    @Override
    public Function1<ProductConfigExport, ProductConfig> inverse() {
        Function1<SchemaExport, Schema> schemaConverter = schemaExportConverter.inverse();
        return productConfigExport -> ProductConfig.builder()
                .name(productConfigExport.getName())
                .version(productConfigExport.getVersion())
                .tables(mapCollectionOrEmpty(productConfigExport.getTables(), schemaConverter, LinkedHashSet::new))
                .build();
    }
}
