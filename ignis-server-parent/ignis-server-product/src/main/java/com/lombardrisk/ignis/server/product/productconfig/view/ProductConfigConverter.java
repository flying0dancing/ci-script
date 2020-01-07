package com.lombardrisk.ignis.server.product.productconfig.view;

import com.lombardrisk.ignis.client.external.productconfig.export.ProductConfigExport;
import com.lombardrisk.ignis.common.MapperUtils;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductConfig;
import com.lombardrisk.ignis.server.product.table.view.TableConverter;

import java.util.function.Function;

public class ProductConfigConverter implements Function<ProductConfig, ProductConfigExport> {

    private final TableConverter tableConverter;

    public ProductConfigConverter(final TableConverter tableConverter) {
        this.tableConverter = tableConverter;
    }

    @Override
    public ProductConfigExport apply(final ProductConfig productConfig) {
        return ProductConfigExport.builder()
                .id(productConfig.getId())
                .name(productConfig.getName())
                .version(productConfig.getVersion())
                .tables(MapperUtils.map(productConfig.getTables(), tableConverter::apply))
                .importStatus(productConfig.getImportStatus() == null ? null : productConfig.getImportStatus().name())
                .build();
    }
}
