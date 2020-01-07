package com.lombardrisk.ignis.server.eraser.service;

import com.lombardrisk.ignis.server.product.pipeline.model.Pipeline;
import com.lombardrisk.ignis.server.product.productconfig.ProductConfigRepository;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductConfig;
import com.lombardrisk.ignis.server.product.table.model.Table;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@AllArgsConstructor
public class ProductService {

    private final ProductConfigRepository productConfigRepository;
    private final PipelineService pipelineService;
    private final SchemaService schemaService;
    private final JdbcTemplate phoenixJdbcTemplate;

    @Transactional
    public void deleteProduct(final Long productId) {
        ProductConfig productConfig = productConfigRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Could not find product by id " + productId));

        for (Pipeline pipeline : productConfig.getPipelines()) {
            pipelineService.deletePipeline(pipeline);
        }

        for (Table schema : productConfig.getTables()) {
            schemaService.deleteSchema(schema);
        }

        productConfigRepository.deleteById(productConfig.getId());
    }
}
