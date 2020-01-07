package com.lombardrisk.ignis.spark.validation.config;

import com.lombardrisk.ignis.spark.api.validation.DatasetValidationJobRequest;
import com.lombardrisk.ignis.spark.config.ApplicationConfig;
import com.lombardrisk.ignis.spark.config.IgnisClientConfig;
import com.lombardrisk.ignis.spark.core.phoenix.PhoenixTableService;
import com.lombardrisk.ignis.spark.core.phoenix.ranged.PhoenixRowKeyedRepository;
import com.lombardrisk.ignis.spark.core.repository.DatasetRepository;
import com.lombardrisk.ignis.spark.core.repository.RowKeyedDatasetRepository;
import com.lombardrisk.ignis.spark.core.server.DatasetEntityRepository;
import com.lombardrisk.ignis.spark.validation.ValidationRuleSummaryService;
import com.lombardrisk.ignis.spark.validation.ValidationService;
import com.lombardrisk.ignis.spark.validation.job.ValidationJobOperator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ValidationJobConfiguration {

    private final IgnisClientConfig ignisClientConfig;
    private final DatasetRepository datasetRepository;
    private final PhoenixTableService phoenixTableService;
    private final DatasetValidationJobRequest datasetValidationJobRequest;
    private final ApplicationConfig applicationConfig;

    public ValidationJobConfiguration(
            final IgnisClientConfig ignisClientConfig,
            final DatasetRepository datasetRepository,
            final PhoenixTableService phoenixTableService,
            final ApplicationConfig applicationConfig,
            final DatasetValidationJobRequest datasetValidationJobRequest) {
        this.ignisClientConfig = ignisClientConfig;
        this.datasetRepository = datasetRepository;
        this.phoenixTableService = phoenixTableService;
        this.datasetValidationJobRequest = datasetValidationJobRequest;
        this.applicationConfig = applicationConfig;
    }

    @Bean
    public ValidationService validationService() {
        return new ValidationService(datasetRepository);
    }

    @Bean
    public ValidationRuleSummaryService validationRuleSummaryService() {
        return new ValidationRuleSummaryService(ignisClientConfig.datasetClient());
    }

    @Bean
    public RowKeyedDatasetRepository rowKeyedDatasetRepository() {
        return new PhoenixRowKeyedRepository(
                applicationConfig.sparkSession(),
                datasetValidationJobRequest,
                datasetRepository,
                datasetEntityRepository());
    }

    @Bean
    public DatasetEntityRepository datasetEntityRepository() {
        return new DatasetEntityRepository(
                datasetValidationJobRequest,
                ignisClientConfig.datasetClient());
    }

    @Bean
    public ValidationJobOperator validationJobOperator() {
        return new ValidationJobOperator(
                validationService(),
                rowKeyedDatasetRepository(),
                validationRuleSummaryService(),
                phoenixTableService,
                datasetValidationJobRequest);
    }
}
