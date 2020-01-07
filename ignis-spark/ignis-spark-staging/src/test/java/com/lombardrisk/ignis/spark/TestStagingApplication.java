package com.lombardrisk.ignis.spark;

import com.lombardrisk.ignis.client.internal.InternalDatasetClient;
import com.lombardrisk.ignis.common.annotation.ExcludeFromTest;
import com.lombardrisk.ignis.spark.api.staging.DatasetProperties;
import com.lombardrisk.ignis.spark.api.staging.StagingAppConfig;
import com.lombardrisk.ignis.spark.api.staging.StagingDatasetConfig;
import com.lombardrisk.ignis.spark.api.staging.StagingErrorOutput;
import com.lombardrisk.ignis.spark.api.staging.StagingSchemaValidation;
import com.lombardrisk.ignis.spark.api.staging.datasource.HdfsCsvDataSource;
import com.lombardrisk.ignis.spark.config.PhoenixConfiguration;
import com.lombardrisk.ignis.spark.core.hive.InMemoryDatasetRepository;
import com.lombardrisk.ignis.spark.core.mock.JdbcDatasetRepository;
import com.lombardrisk.ignis.spark.core.mock.StatefulDatasetClientStore;
import com.lombardrisk.ignis.spark.core.server.DatasetEntityRepository;
import com.lombardrisk.ignis.spark.core.spark.SparkFunctions;
import com.lombardrisk.ignis.spark.staging.config.StagingJobConfiguration;
import org.apache.commons.lang3.SystemUtils;
import org.apache.spark.sql.SparkSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Primary;
import org.togglz.core.manager.FeatureManager;

import java.nio.file.Paths;
import java.sql.DriverManager;
import java.sql.SQLException;

import static com.google.common.collect.Sets.newHashSet;

@SpringBootApplication
@ComponentScan(
        lazyInit = true,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ANNOTATION, value = ExcludeFromTest.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = PhoenixConfiguration.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = StagingJobConfiguration.class)
        })
public class TestStagingApplication {

    static {
        if (SystemUtils.IS_OS_WINDOWS) {
            System.setProperty(
                    "hadoop.home.dir",
                    Paths.get("../../ignis-core/ignis-common/src/test/resources/hadoop")
                            .toAbsolutePath()
                            .toString());
        }
    }

    private static final String jdbcUrl = "jdbc:h2:mem:staging;DB_CLOSE_DELAY=-1";

    @Autowired
    private FeatureManager featureManager;

    @Bean
    public StagingAppConfig stagingJobRequest() {
        return StagingAppConfig.builder()
                .name("test staging")
                .serviceRequestId(1)
                .items(newHashSet(
                        StagingDatasetConfig.builder()
                                .id(123121)
                                .source(HdfsCsvDataSource.builder().localPath("1_employee").build())
                                .stagingErrorOutput(StagingErrorOutput.builder()
                                        .errorFilePath("target/datasets/errors/120/somefile.csv")
                                        .build())
                                .datasetProperties(DatasetProperties.builder()
                                        .build())
                                .stagingSchemaValidation(StagingSchemaValidation.builder()
                                        .physicalTableName("employee")
                                        .build())
                                .build()))
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
        return new DatasetEntityRepository(stagingJobRequest(), internalDatasetClient());
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
    public JdbcDatasetRepository datasetRepository(final SparkSession sparkSession) throws SQLException {
        DriverManager.registerDriver(new org.h2.Driver());

        return new JdbcDatasetRepository(
                sparkSession, stagingJobRequest(), 0, localSparkFunctions(sparkSession), jdbcUrl);
    }

    @Bean
    public SparkFunctions localSparkFunctions(final SparkSession sparkSession) {
        return new SparkFunctions(sparkSession, true, jdbcUrl);
    }
}
