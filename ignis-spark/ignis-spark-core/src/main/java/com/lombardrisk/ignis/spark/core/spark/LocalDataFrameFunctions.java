package com.lombardrisk.ignis.spark.core.spark;

import lombok.extern.slf4j.Slf4j;
import org.apache.phoenix.spark.DataFrameFunctions;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import scala.collection.immutable.Map;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

@Slf4j
public class LocalDataFrameFunctions extends DataFrameFunctions {

    private static final long serialVersionUID = -2537341782463593044L;
    private final Dataset<Row> dataset;
    private final String jdbcUrl;

    LocalDataFrameFunctions(final String jdbcUrl, final Dataset<Row> dataset) {
        super(dataset);
        this.dataset = dataset;
        this.jdbcUrl = jdbcUrl;
    }

    @Override
    public void saveToPhoenix(final Map<String, String> parameters) {
        String tableName = parameters.get("table").get();
        log.debug("Save {} in {}", tableName, jdbcUrl);
        try {
            DriverManager.registerDriver(new org.h2.Driver());
        } catch (SQLException e) {
            throw new IllegalStateException("Could not register h2 driver", e);
        }

        dataset.write()
                .mode(SaveMode.Append)
                .jdbc(jdbcUrl, tableName, new Properties());
    }
}
