package com.lombardrisk.ignis.spark.core.spark;

import org.apache.phoenix.spark.DataFrameFunctions;
import org.apache.phoenix.spark.SparkSqlContextFunctions;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.h2.jdbcx.JdbcDataSource;

import javax.sql.DataSource;

public class SparkFunctions {

    private final SparkSession sparkSession;
    private final boolean debugMode;
    private final String jdbcUrl;

    public SparkFunctions(
            final SparkSession sparkSession,
            final boolean debugMode,
            final String jdbcUrl) {
        this.sparkSession = sparkSession;
        this.debugMode = debugMode;
        this.jdbcUrl = jdbcUrl;
    }

    public SparkSqlContextFunctions sparkSqlFunctions() {
        if (debugMode) {
            return new LocalSparkFunctions(sparkSession.sqlContext(), jdbcUrl);
        }
        return new SparkSqlContextFunctions(sparkSession.sqlContext());
    }

    public DataFrameFunctions dataFrameFunctions(final Dataset<Row> dfWithRowKey) {
        if (debugMode) {
            return new LocalDataFrameFunctions(jdbcUrl, dfWithRowKey);
        }
        return new DataFrameFunctions(PhoenixDataFrameAdapter.handleLowerCaseColumns(dfWithRowKey));
    }

    private DataSource localDataSource() {
        JdbcDataSource jdbcDataSource = new JdbcDataSource();
        jdbcDataSource.setUrl(jdbcUrl);
        return jdbcDataSource;
    }
}
