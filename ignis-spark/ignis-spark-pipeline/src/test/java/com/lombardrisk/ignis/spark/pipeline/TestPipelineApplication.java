package com.lombardrisk.ignis.spark.pipeline;

import com.lombardrisk.ignis.client.internal.PipelineStatusClient;
import com.lombardrisk.ignis.common.annotation.ExcludeFromTest;
import com.lombardrisk.ignis.spark.api.JobRequest;
import com.lombardrisk.ignis.spark.config.ApplicationConfig;
import com.lombardrisk.ignis.spark.config.PhoenixConfiguration;
import com.lombardrisk.ignis.spark.core.hive.HiveDatasetRepository;
import com.lombardrisk.ignis.spark.core.hive.InMemoryDatasetRepository;
import com.lombardrisk.ignis.spark.core.mock.HiveDatasetRepositoryFixture;
import com.lombardrisk.ignis.spark.core.mock.StatefulDatasetClientStore;
import com.lombardrisk.ignis.spark.core.server.DatasetEntityRepository;
import com.lombardrisk.ignis.spark.pipeline.mock.MockPipelineStatusClient;
import org.apache.commons.lang3.SystemUtils;
import org.apache.spark.sql.SparkSession;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import java.nio.file.Paths;

@SpringBootApplication
@Import(ApplicationConfig.class)
@ComponentScan(
        lazyInit = true,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ANNOTATION, value = ExcludeFromTest.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = PhoenixConfiguration.class)
        })
public class TestPipelineApplication {

    static {
        if (SystemUtils.IS_OS_WINDOWS) {
            System.setProperty(
                    "hadoop.home.dir",
                    Paths.get("../../ignis-core/ignis-common/src/test/resources/hadoop")
                            .toAbsolutePath()
                            .toString());
        }
    }

    @Bean
    public JobRequest jobRequest() {
        return () -> 1L;
    }

    @Bean
    public String correlationId() {
        return "coriolanus birk";
    }

    @Bean
    @Primary
    public StatefulDatasetClientStore internalDatasetClient() {
        return new StatefulDatasetClientStore();
    }

    @Bean
    public DatasetEntityRepository datasetEntityRepository() {
        return new DatasetEntityRepository(jobRequest(), internalDatasetClient());
    }

    @Bean
    @Primary
    public InMemoryDatasetRepository inMemoryDatasetRepository(final SparkSession sparkSession) {
        return new InMemoryDatasetRepository(
                internalDatasetClient(),
                sparkSession,
                datasetEntityRepository());
    }

    @Bean
    @Primary
    public HiveDatasetRepository hiveDatasetRepository(final SparkSession sparkSession) {
        return new HiveDatasetRepositoryFixture(sparkSession);
    }

    @Bean
    public PipelineStatusClient pipelineStatusClient() {
        return new MockPipelineStatusClient();
    }
}
