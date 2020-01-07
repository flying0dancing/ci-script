package com.lombardrisk.ignis.spark;

import com.lombardrisk.ignis.client.internal.InternalDatasetClient;
import com.lombardrisk.ignis.common.annotation.ExcludeFromTest;
import com.lombardrisk.ignis.spark.api.JobRequest;
import com.lombardrisk.ignis.spark.core.hive.InMemoryDatasetRepository;
import com.lombardrisk.ignis.spark.core.mock.JdbcDatasetRepository;
import com.lombardrisk.ignis.spark.core.mock.StatefulDatasetClientStore;
import com.lombardrisk.ignis.spark.core.mock.TestDatasetRepository;
import com.lombardrisk.ignis.spark.core.phoenix.ranged.PhoenixRowKeyedRepository;
import com.lombardrisk.ignis.spark.core.server.DatasetEntityRepository;
import com.lombardrisk.ignis.spark.core.spark.SparkFunctions;
import org.apache.commons.lang3.SystemUtils;
import org.apache.spark.sql.SparkSession;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Primary;

import java.nio.file.Paths;
import java.sql.DriverManager;
import java.sql.SQLException;

@SpringBootApplication
@ComponentScan(
        lazyInit = true,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, value = ExcludeFromTest.class))
public class TestApplication {

    static {
        if (SystemUtils.IS_OS_WINDOWS) {
            System.setProperty(
                    "hadoop.home.dir",
                    Paths.get("../../ignis-core/ignis-common/src/test/resources/hadoop")
                            .toAbsolutePath()
                            .toString());
        }
    }

    private static final long SERVICE_REQUEST_ID = 2876543L;
    private static final String jdbcUrl = "jdbc:h2:mem:sparkcore;DB_CLOSE_DELAY=-1";

    @Bean
    public JobRequest jobRequest() {
        return () -> SERVICE_REQUEST_ID;
    }

    @Bean
    public String correlationId() {
        return "correlationId";
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
    public TestDatasetRepository datasetRepository(final SparkSession sparkSession) throws SQLException {
        DriverManager.registerDriver(new org.h2.Driver());

        return new JdbcDatasetRepository(sparkSession, jobRequest(), 0, localSparkFunctions(sparkSession), jdbcUrl);
    }

    @Bean
    public SparkFunctions localSparkFunctions(final SparkSession sparkSession) {
        return new SparkFunctions(sparkSession, true, jdbcUrl);
    }

    @Bean
    public PhoenixRowKeyedRepository phoenixRowKeyedRepository(final SparkSession sparkSession) throws SQLException {
        return new PhoenixRowKeyedRepository(
                sparkSession,
                jobRequest(),
                datasetRepository(sparkSession),
                datasetEntityRepository());
    }

    @Bean
    public InMemoryDatasetRepository inMemoryDatasetRepository(final SparkSession sparkSession) {
        return new InMemoryDatasetRepository(
                internalDatasetClient(),
                sparkSession,
                datasetEntityRepository());
    }
}
