package com.lombardrisk.ignis.design.server.productconfig.converter;

import com.lombardrisk.ignis.client.design.productconfig.ProductConfigDto;
import com.lombardrisk.ignis.common.MapperUtils;
import com.lombardrisk.ignis.design.server.productconfig.ProductConfig;
import io.vavr.Function1;
import io.vavr.control.Option;

public class ProductConfigConverter implements Function1<ProductConfig, ProductConfigDto> {

    private static final long serialVersionUID = -9196482262917664114L;
    private final SchemaConverter schemaConverter = new SchemaConverter();

    @Override
    public ProductConfigDto apply(final ProductConfig productConfig) {
        return ProductConfigDto.builder()
                .id(productConfig.getId())
                .name(productConfig.getName())
                .version(productConfig.getVersion())
                .importStatus(Option.of(productConfig.getImportStatus())
                        .map(status -> ProductConfigDto.ImportStatus.valueOf(status.name()))
                        .getOrNull())
                .schemas(MapperUtils.mapOrEmpty(productConfig.getTables(), schemaConverter))
                .build();
    }
}
