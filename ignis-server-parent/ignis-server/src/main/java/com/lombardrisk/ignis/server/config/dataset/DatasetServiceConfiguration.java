package com.lombardrisk.ignis.server.config.dataset;

import com.lombardrisk.ignis.common.time.TimeSource;
import com.lombardrisk.ignis.server.config.product.ProductRepositoryConfiguration;
import com.lombardrisk.ignis.server.dataset.DatasetService;
import com.lombardrisk.ignis.server.dataset.rule.ValidationResultsSummaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatasetServiceConfiguration {

    private final ProductRepositoryConfiguration productRepositoryConfiguration;
    private final DatasetRepositoryConfiguration datasetRepositoryConfiguration;
    private final TimeSource timeSource;

    @Autowired
    public DatasetServiceConfiguration(
            final ProductRepositoryConfiguration productRepositoryConfiguration,
            final DatasetRepositoryConfiguration datasetRepositoryConfiguration,
            final TimeSource timeSource) {
        this.productRepositoryConfiguration = productRepositoryConfiguration;
        this.datasetRepositoryConfiguration = datasetRepositoryConfiguration;
        this.timeSource = timeSource;
    }

    @Bean
    public DatasetService datasetService() {
        return new DatasetService(
                datasetRepositoryConfiguration.datasetRepository(),
                productRepositoryConfiguration.tableRepository(),
                timeSource);
    }

    @Bean
    public ValidationResultsSummaryService validationResultsSummaryService() {
        return new ValidationResultsSummaryService(
                productRepositoryConfiguration.ruleRepository(),
                productRepositoryConfiguration.validationResultsSummaryRepository(),
                timeSource);
    }
}
