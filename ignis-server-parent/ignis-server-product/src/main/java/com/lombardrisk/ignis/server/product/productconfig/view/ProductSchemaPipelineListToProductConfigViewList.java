package com.lombardrisk.ignis.server.product.productconfig.view;

import com.lombardrisk.ignis.client.external.pipeline.view.PipelineView;
import com.lombardrisk.ignis.client.external.productconfig.view.ProductConfigView;
import com.lombardrisk.ignis.client.external.productconfig.view.SchemaView;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductSchemaPipeline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

public class ProductSchemaPipelineListToProductConfigViewList implements Function<List<ProductSchemaPipeline>, List<ProductConfigView>> {

    @Override
    public List<ProductConfigView> apply(final List<ProductSchemaPipeline> productSchemaPipelineList) {
        Map<Long, List<SchemaView>> productConfigSchemaMap = getProductConfigSchemaMap(productSchemaPipelineList);
        Map<Long, List<PipelineView>> productConfigPipelineMap = getProductConfigPipelineMap(productSchemaPipelineList);

        return productSchemaPipelineList.stream()
                .map(productSchemaPipeline -> ProductConfigView.builder()
                        .id(productSchemaPipeline.getId())
                        .name(productSchemaPipeline.getName())
                        .version(productSchemaPipeline.getVersion())
                        .importStatus(productSchemaPipeline.getImportStatus())
                        .schemas(productConfigSchemaMap.get(productSchemaPipeline.getId()))
                        .pipelines(productConfigPipelineMap.get(productSchemaPipeline.getId()))
                        .build()
                )
                .filter(distinctByKey(ProductConfigView::getId))
                .collect(toList());
    }

    private Map<Long, List<SchemaView>> getProductConfigSchemaMap(final List<ProductSchemaPipeline> productSchemaPipelineList) {
        Map<Long, List<SchemaView>> productConfigSchemaMap = new HashMap();
        for (ProductSchemaPipeline productSchemaPipeline : productSchemaPipelineList) {
            SchemaView schemaView = SchemaView.builder()
                    .id(productSchemaPipeline.getSchemaId())
                    .physicalTableName(productSchemaPipeline.getSchemaPhysicalTableName())
                    .displayName(productSchemaPipeline.getSchemaDisplayName())
                    .version(productSchemaPipeline.getSchemaVersion())
                    .createdTime(productSchemaPipeline.getSchemaCreatedTime())
                    .startDate(productSchemaPipeline.getSchemaStartDate())
                    .endDate(productSchemaPipeline.getSchemaEndDate())
                    .createdBy(productSchemaPipeline.getSchemaCreatedBy())
                    .hasDatasets(productSchemaPipeline.getSchemaHasDatasets())
                    .build();

            List<SchemaView> schemaViews = productConfigSchemaMap.get(productSchemaPipeline.getId());
            if (schemaViews == null) {
                schemaViews = new ArrayList<>();
            }
            if (!schemaViews.stream().anyMatch(sv -> sv.getId().equals(schemaView.getId()))) {
                schemaViews.add(schemaView);
                productConfigSchemaMap.put(productSchemaPipeline.getId(), schemaViews);
            }
        }
        return productConfigSchemaMap;
    }

    private Map<Long, List<PipelineView>> getProductConfigPipelineMap(final List<ProductSchemaPipeline> productSchemaPipelineList) {
        Map<Long, List<PipelineView>> productConfigPipelineMap = new HashMap();
        for (ProductSchemaPipeline productSchemaPipeline : productSchemaPipelineList) {
            PipelineView pipelineView = PipelineView.builder()
                    .id(productSchemaPipeline.getPipelineId())
                    .name(productSchemaPipeline.getPipelineName())
                    .build();

            List<PipelineView> pipelineViews = productConfigPipelineMap.get(productSchemaPipeline.getId());
            if (pipelineViews == null) {
                pipelineViews = new ArrayList<>();
            }
            if (!pipelineViews.stream().anyMatch(pv -> pv.getId().equals(pipelineView.getId()))) {
                pipelineViews.add(pipelineView);
                productConfigPipelineMap.put(productSchemaPipeline.getId(), pipelineViews);
            }
        }
        return productConfigPipelineMap;
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }
}
