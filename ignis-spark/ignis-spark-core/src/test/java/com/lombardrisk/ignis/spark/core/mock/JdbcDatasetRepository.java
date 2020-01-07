package com.lombardrisk.ignis.spark.core.mock;

import com.lombardrisk.ignis.spark.api.JobRequest;
import com.lombardrisk.ignis.spark.core.phoenix.PhoenixDatasetRepository;
import com.lombardrisk.ignis.spark.core.spark.SparkFunctions;
import org.apache.spark.sql.SparkSession;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class JdbcDatasetRepository extends PhoenixDatasetRepository implements TestDatasetRepository {

    private final String jdbcUrl;

    public JdbcDatasetRepository(
            final SparkSession sparkSession,
            final JobRequest jobRequest,
            final int saltBucketsSize,
            final SparkFunctions sparkFunctions,
            final String jdbcUrl) {

        super(sparkSession, jobRequest, saltBucketsSize, sparkFunctions, jdbcUrl);
        this.jdbcUrl = jdbcUrl;
    }

    @Override
    public void dropTable(final String tableName) throws SQLException {
        try (Connection connection = DriverManager.getConnection(jdbcUrl);
             Statement statement = connection.createStatement()) {

            statement.executeUpdate("DROP TABLE IF EXISTS " + tableName);
        }
    }
}
