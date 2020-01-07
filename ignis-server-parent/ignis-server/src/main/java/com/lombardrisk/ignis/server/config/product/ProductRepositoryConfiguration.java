package com.lombardrisk.ignis.server.config.product;

import com.lombardrisk.ignis.server.dataset.DatasetJpaRepository;
import com.lombardrisk.ignis.server.dataset.rule.ValidationResultsSummaryRepository;
import com.lombardrisk.ignis.server.product.pipeline.PipelineJpaRepository;
import com.lombardrisk.ignis.server.product.productconfig.ProductConfigRepository;
import com.lombardrisk.ignis.server.product.rule.ValidationRuleRepository;
import com.lombardrisk.ignis.server.product.table.TableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProductRepositoryConfiguration {

    private final DatasetJpaRepository datasetRepository;
    private final TableRepository tableRepository;
    private final ValidationRuleRepository ruleRepository;
    private final ValidationResultsSummaryRepository validationResultsSummaryRepository;
    private final ProductConfigRepository productConfigRepository;
    private final PipelineJpaRepository pipelineJpaRepository;

    @Autowired
    public ProductRepositoryConfiguration(
            final DatasetJpaRepository datasetRepository,
            final TableRepository tableRepository,
            final ValidationRuleRepository ruleRepository,
            final ValidationResultsSummaryRepository validationResultsSummaryRepository,
            final ProductConfigRepository productConfigRepository,
            final PipelineJpaRepository pipelineJpaRepository) {
        this.datasetRepository = datasetRepository;
        this.tableRepository = tableRepository;
        this.ruleRepository = ruleRepository;
        this.validationResultsSummaryRepository = validationResultsSummaryRepository;
        this.productConfigRepository = productConfigRepository;
        this.pipelineJpaRepository = pipelineJpaRepository;
    }

    public DatasetJpaRepository datasetRepository() {
        return datasetRepository;
    }

    public TableRepository tableRepository() {
        return tableRepository;
    }

    public ValidationRuleRepository ruleRepository() {
        return ruleRepository;
    }

    public ValidationResultsSummaryRepository validationResultsSummaryRepository() {
        return validationResultsSummaryRepository;
    }

    public ProductConfigRepository productConfigRepository() {
        return productConfigRepository;
    }

    public PipelineJpaRepository pipelineJpaRepository() {
        return pipelineJpaRepository;
    }
}
