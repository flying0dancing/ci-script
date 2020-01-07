package com.lombardrisk.ignis.spark;

import com.lombardrisk.ignis.client.internal.InternalDatasetClient;
import com.lombardrisk.ignis.common.annotation.ExcludeFromTest;
import com.lombardrisk.ignis.spark.api.JobRequest;
import com.lombardrisk.ignis.spark.api.validation.DatasetValidationJobRequest;
import com.lombardrisk.ignis.spark.core.hive.InMemoryDatasetRepository;
import com.lombardrisk.ignis.spark.core.mock.HiveDatasetRepositoryFixture;
import com.lombardrisk.ignis.spark.core.mock.StatefulDatasetClientStore;
import com.lombardrisk.ignis.spark.core.mock.TestDatasetRepository;
import com.lombardrisk.ignis.spark.core.server.DatasetEntityRepository;
import org.apache.commons.lang3.SystemUtils;
import org.apache.spark.sql.SparkSession;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Primary;

import java.nio.file.Paths;

@SpringBootApplication
@ComponentScan(
        lazyInit = true,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, value = ExcludeFromTest.class))
public class TestValidationApplication {

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
    public DatasetValidationJobRequest datasetValidationJobRequest() {
        return DatasetValidationJobRequest.builder()
                .name("test validation")
                .build();
    }

    @Bean
    public String correlationId() {
        return "coriolanus birk";
    }

    @Bean
    @Primary
    public InternalDatasetClient internalDatasetClient() {
        return new StatefulDatasetClientStore();
    }

    @Bean
    public DatasetEntityRepository datasetEntityRepository() {
        return new DatasetEntityRepository(jobRequest(), internalDatasetClient());
    }

    @Bean
    public JobRequest jobRequest() {
        return () -> 100L;
    }

    @Bean
    @Primary
    public InMemoryDatasetRepository rowKeyedDatasetRepository(final SparkSession sparkSession) {
        return new InMemoryDatasetRepository(
                internalDatasetClient(),
                sparkSession,
                datasetEntityRepository());
    }

    @Bean
    @Primary
    public TestDatasetRepository datasetRepository(final SparkSession sparkSession) {
        return new HiveDatasetRepositoryFixture(sparkSession);
    }
}
