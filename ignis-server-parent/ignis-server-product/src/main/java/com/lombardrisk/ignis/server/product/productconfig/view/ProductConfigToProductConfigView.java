package com.lombardrisk.ignis.server.product.productconfig.view;

import com.lombardrisk.ignis.client.external.productconfig.view.ProductConfigView;
import com.lombardrisk.ignis.server.product.table.view.TableToSchemaView;
import com.lombardrisk.ignis.server.product.pipeline.view.PipelineToPipelineView;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductConfig;

import java.util.ArrayList;
import java.util.function.Function;

import static com.lombardrisk.ignis.common.MapperUtils.mapCollectionOrEmpty;

public class ProductConfigToProductConfigView implements Function<ProductConfig, ProductConfigView> {

    private final TableToSchemaView tableToSchemaView = new TableToSchemaView();
    private final PipelineToPipelineView pipelineDetailsToPipelineView = new PipelineToPipelineView();

    @Override
    public ProductConfigView apply(final ProductConfig productConfig) {
        return ProductConfigView.builder()
                .id(productConfig.getId())
                .name(productConfig.getName())
                .version(productConfig.getVersion())
                .schemas(mapCollectionOrEmpty(productConfig.getTables(), tableToSchemaView::apply, ArrayList::new))
                .pipelines(mapCollectionOrEmpty(
                        productConfig.getPipelines(), pipelineDetailsToPipelineView::apply, ArrayList::new))
                .importStatus(productConfig.getImportStatus() == null ? null : productConfig.getImportStatus().name())
                .build();
    }
}
