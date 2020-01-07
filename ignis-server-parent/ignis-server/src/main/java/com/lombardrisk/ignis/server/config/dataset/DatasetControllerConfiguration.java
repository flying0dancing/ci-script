package com.lombardrisk.ignis.server.config.dataset;

import com.lombardrisk.ignis.common.time.TimeSource;
import com.lombardrisk.ignis.server.config.datasource.DatasourceFileConfiguration;
import com.lombardrisk.ignis.server.config.product.ProductServiceConfiguration;
import com.lombardrisk.ignis.server.controller.DatasetSourceFileController;
import com.lombardrisk.ignis.server.controller.datasets.DatasetController;
import com.lombardrisk.ignis.server.controller.external.ExternalDatasetController;
import com.lombardrisk.ignis.server.dataset.rule.view.ValidationResultsSummaryConverter;
import com.lombardrisk.ignis.server.dataset.view.DatasetConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatasetControllerConfiguration {

    private final DatasourceFileConfiguration datasourceFileConfiguration;
    private final DatasetServiceConfiguration datasetServices;
    private final ProductServiceConfiguration productServices;
    private final PhoenixRepositoryConfiguration phoenixRepositoryConfiguration;
    private final TimeSource timeSource;

    public DatasetControllerConfiguration(
            final DatasourceFileConfiguration datasourceFileConfiguration,
            final DatasetServiceConfiguration datasetServices,
            final ProductServiceConfiguration productServices,
            final PhoenixRepositoryConfiguration phoenixRepositoryConfiguration,
            final TimeSource timeSource) {
        this.datasourceFileConfiguration = datasourceFileConfiguration;
        this.datasetServices = datasetServices;
        this.productServices = productServices;
        this.phoenixRepositoryConfiguration = phoenixRepositoryConfiguration;
        this.timeSource = timeSource;
    }

    @Bean
    public DatasetController datasetController() {
        return new DatasetController(
                datasetServices.datasetService(),
                datasetServices.validationResultsSummaryService());
    }

    @Bean
    public ExternalDatasetController externalDatasetController() {
        return new ExternalDatasetController(
                datasetServices.datasetService(),
                datasetViewConverter(),
                datasetServices.validationResultsSummaryService(),
                validationResultsSummaryConverter(),
                phoenixRepositoryConfiguration.validationResultDetailService(),
                productServices.getConverterConfiguration().pageConverter());
    }

    @Bean
    public ValidationResultsSummaryConverter validationResultsSummaryConverter() {
        return new ValidationResultsSummaryConverter(
                productServices.getConverterConfiguration()
                        .validationRuleConverter());
    }

    @Bean
    public DatasetConverter datasetViewConverter() {
        return new DatasetConverter(timeSource);
    }

    @Bean
    public DatasetSourceFileController datasetSourceFileController() {
        return new DatasetSourceFileController(datasourceFileConfiguration.dataSourceService());
    }
}
