package com.lombardrisk.ignis.spark.pipeline.config;

import com.lombardrisk.ignis.client.internal.InternalDatasetClient;
import com.lombardrisk.ignis.client.internal.PipelineStatusClient;
import com.lombardrisk.ignis.pipeline.step.common.MapStepExecutor;
import com.lombardrisk.ignis.pipeline.step.common.UnionStepExecutor;
import com.lombardrisk.ignis.pipeline.step.common.WindowStepExecutor;
import com.lombardrisk.ignis.spark.api.JobRequest;
import com.lombardrisk.ignis.spark.config.ApplicationConfig;
import com.lombardrisk.ignis.spark.core.phoenix.ranged.PhoenixRowKeyedRepository;
import com.lombardrisk.ignis.spark.core.repository.DatasetRepository;
import com.lombardrisk.ignis.spark.core.repository.RowKeyedDatasetRepository;
import com.lombardrisk.ignis.spark.core.server.DatasetEntityRepository;
import com.lombardrisk.ignis.spark.pipeline.job.PipelineJobOperator;
import com.lombardrisk.ignis.spark.pipeline.job.PipelineStepExecutor;
import com.lombardrisk.ignis.spark.pipeline.job.SparkConfigurationService;
import com.lombardrisk.ignis.spark.pipeline.job.step.AggregationStepExecutor;
import com.lombardrisk.ignis.spark.pipeline.job.step.JoinStepExecutor;
import com.lombardrisk.ignis.spark.pipeline.service.PipelineStatusService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PipelineJobConfiguration {

    private final JobRequest pipelineAppConfig;
    private final ApplicationConfig applicationConfig;
    private final InternalDatasetClient internalDatasetClient;
    private final PipelineStatusClient pipelineStatusClient;
    private final DatasetRepository datasetRepository;

    public PipelineJobConfiguration(
            final JobRequest pipelineAppConfig,
            final ApplicationConfig applicationConfig,
            final InternalDatasetClient internalDatasetClient,
            final PipelineStatusClient pipelineStatusClient,
            final DatasetRepository datasetRepository) {

        this.pipelineAppConfig = pipelineAppConfig;
        this.applicationConfig = applicationConfig;
        this.internalDatasetClient = internalDatasetClient;
        this.pipelineStatusClient = pipelineStatusClient;
        this.datasetRepository = datasetRepository;
    }

    @Bean
    public DatasetEntityRepository datasetEntityRepository() {
        return new DatasetEntityRepository(pipelineAppConfig, internalDatasetClient);
    }

    @Bean
    public RowKeyedDatasetRepository rowKeyedDatasetRepository() {
        return new PhoenixRowKeyedRepository(
                applicationConfig.sparkSession(),
                pipelineAppConfig,
                datasetRepository,
                datasetEntityRepository());
    }

    @Bean
    public SparkConfigurationService sparkConfigurationService() {
        return new SparkConfigurationService(applicationConfig.sparkSession());
    }

    @Bean
    public AggregationStepExecutor aggregationStepExecutor() {
        return new AggregationStepExecutor(applicationConfig.sparkSession(), datasetRepository);
    }

    @Bean
    public MapStepExecutor mapStepExecutor() {
        return new MapStepExecutor();
    }

    @Bean
    public WindowStepExecutor windowStepExecutor() {
        return new WindowStepExecutor();
    }

    @Bean
    public JoinStepExecutor joinStepExecutor() {
        return new JoinStepExecutor(applicationConfig.sparkSession(), datasetRepository);
    }

    @Bean
    public UnionStepExecutor unionStepExecutor() {
        return new UnionStepExecutor();
    }

    @Bean
    public PipelineStepExecutor pipelineStepExecutor() {
        return new PipelineStepExecutor(
                rowKeyedDatasetRepository(),
                datasetRepository,
                aggregationStepExecutor(),
                mapStepExecutor(),
                joinStepExecutor(),
                windowStepExecutor(),
                unionStepExecutor());
    }

    @Bean
    public PipelineJobOperator pipelineJobOperator() {
        return new PipelineJobOperator(
                pipelineStatusService(),
                sparkConfigurationService(),
                pipelineStepExecutor());
    }

    @Bean
    public PipelineStatusService pipelineStatusService() {
        return new PipelineStatusService(pipelineStatusClient);
    }
}
