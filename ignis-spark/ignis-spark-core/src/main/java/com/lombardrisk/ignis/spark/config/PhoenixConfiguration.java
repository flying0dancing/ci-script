package com.lombardrisk.ignis.spark.config;

import com.lombardrisk.ignis.spark.api.JobRequest;
import com.lombardrisk.ignis.spark.core.phoenix.PhoenixDatasetRepository;
import com.lombardrisk.ignis.spark.core.phoenix.PhoenixTableService;
import com.lombardrisk.ignis.spark.core.repository.DatasetRepository;
import com.lombardrisk.ignis.spark.core.spark.SparkFunctions;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.SparkSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;

import java.sql.DriverManager;

import static com.lombardrisk.ignis.config.VariableConstants.PHOENIX_SALT_BUCKETS;
import static com.lombardrisk.ignis.config.VariableConstants.ZOOKEEPER_URL;

@Configuration
@Slf4j
public class PhoenixConfiguration {

    private final SparkSession sparkSession;
    private final JobRequest jobRequest;
    private final Environment environment;
    private final boolean debugMode;
    private String jdbcUrl;

    public PhoenixConfiguration(
            final SparkSession sparkSession,
            final JobRequest jobRequest,
            final Environment environment) {
        this.sparkSession = sparkSession;
        this.jobRequest = jobRequest;
        this.environment = environment;

        debugMode = environment.getProperty("debug.mode", Boolean.class, false);
        jdbcUrl = environment.getRequiredProperty(ZOOKEEPER_URL);
    }

    @Bean
    public PhoenixTableService phoenixTableService() {
        return new PhoenixTableService(
                () -> DriverManager.getConnection(getJdbcUrl()), getSaltBuckets(), debugMode);
    }

    @Bean
    public DatasetRepository datasetRepository() {
        return new PhoenixDatasetRepository(
                sparkSession,
                jobRequest,
                getSaltBuckets(),
                sparkFunctions(),
                jdbcUrl);
    }

    @Bean
    @Lazy
    public SparkFunctions sparkFunctions() {
        return new SparkFunctions(
                sparkSession,
                debugMode,
                jdbcUrl);
    }

    private int getSaltBuckets() {
        return environment.getRequiredProperty(PHOENIX_SALT_BUCKETS, Integer.class);
    }

    private String getJdbcUrl() {
        if (debugMode) {
            return jdbcUrl;
        }
        return "jdbc:phoenix:" + jdbcUrl + ":/hbase";
    }
}
