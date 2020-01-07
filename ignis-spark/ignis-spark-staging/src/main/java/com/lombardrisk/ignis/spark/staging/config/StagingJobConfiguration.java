package com.lombardrisk.ignis.spark.staging.config;

import com.lombardrisk.ignis.spark.api.staging.StagingAppConfig;
import com.lombardrisk.ignis.spark.config.ApplicationConfig;
import com.lombardrisk.ignis.spark.config.IgnisClientConfig;
import com.lombardrisk.ignis.spark.core.phoenix.ranged.PhoenixRowKeyedRepository;
import com.lombardrisk.ignis.spark.core.repository.DatasetRepository;
import com.lombardrisk.ignis.spark.core.repository.RowKeyedDatasetRepository;
import com.lombardrisk.ignis.spark.core.server.DatasetEntityRepository;
import com.lombardrisk.ignis.spark.staging.StagingJobOperator;
import com.lombardrisk.ignis.spark.staging.execution.DatasetRegister;
import com.lombardrisk.ignis.spark.staging.execution.DatasetStagingWorker;
import com.lombardrisk.ignis.spark.staging.execution.DatasetValidator;
import com.lombardrisk.ignis.spark.staging.execution.StagingListener;
import com.lombardrisk.ignis.spark.staging.execution.error.ErrorFileService;
import com.lombardrisk.ignis.spark.staging.execution.load.CsvDatasetLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StagingJobConfiguration {

    private final ApplicationConfig applicationConfig;
    private final IgnisClientConfig ignisClientConfig;
    private final StagingAppConfig stagingJobRequest;
    private final DatasetRepository datasetRepository;

    public StagingJobConfiguration(
            final ApplicationConfig applicationConfig,
            final IgnisClientConfig ignisClientConfig,
            final StagingAppConfig stagingJobRequest,
            final DatasetRepository datasetRepository) {
        this.applicationConfig = applicationConfig;
        this.ignisClientConfig = ignisClientConfig;
        this.stagingJobRequest = stagingJobRequest;
        this.datasetRepository = datasetRepository;
    }

    @Bean
    public CsvDatasetLoader csvDatasetLoader() {
        return new CsvDatasetLoader(applicationConfig.javaSparkContext());
    }

    @Bean
    public StagingListener stagingListener() {
        return new StagingListener(ignisClientConfig.stagingClient());
    }

    @Bean
    public DatasetValidator datasetValidator() {
        return new DatasetValidator(
                stagingListener(),
                applicationConfig.sparkSession());
    }

    @Bean
    public RowKeyedDatasetRepository rowKeyedDatasetRepository() {
        return new PhoenixRowKeyedRepository(
                applicationConfig.sparkSession(),
                stagingJobRequest,
                datasetRepository,
                datasetEntityRepository());
    }

    @Bean
    public DatasetEntityRepository datasetEntityRepository() {
        return new DatasetEntityRepository(stagingJobRequest, ignisClientConfig.datasetClient());
    }

    @Bean
    public DatasetRegister datasetRegister() {
        return new DatasetRegister(stagingListener(), rowKeyedDatasetRepository(), errorFileService());
    }

    @Bean
    public ErrorFileService errorFileService() {
        return new ErrorFileService(applicationConfig.sparkSession());
    }

    @Bean
    public DatasetStagingWorker datasetStagingWorker() {
        return new DatasetStagingWorker(csvDatasetLoader(), datasetValidator(), datasetRegister());
    }

    @Bean
    public StagingJobOperator stagingJobOperator() {
        return new StagingJobOperator(datasetStagingWorker(), stagingJobRequest);
    }
}
