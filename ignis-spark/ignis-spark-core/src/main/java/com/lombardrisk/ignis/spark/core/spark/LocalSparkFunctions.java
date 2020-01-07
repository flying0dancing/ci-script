package com.lombardrisk.ignis.spark.core.spark;

import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.phoenix.spark.SparkSqlContextFunctions;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import scala.Option;
import scala.collection.Seq;

import java.sql.DriverManager;
import java.sql.SQLException;

import static org.apache.commons.lang3.ArrayUtils.EMPTY_STRING_ARRAY;

@Slf4j
public class LocalSparkFunctions extends SparkSqlContextFunctions {

    private static final long serialVersionUID = -1163767842838498578L;
    private final String jdbcUrl;

    LocalSparkFunctions(final SQLContext sqlContext, final String jdbcUrl) {
        super(sqlContext);
        this.jdbcUrl = jdbcUrl;
    }

    @Override
    public Dataset<Row> phoenixTableAsDataFrame(
            final String table,
            final Seq<String> columns,
            final Option<String> optionalPredicate,
            final Option<String> zkUrl,
            final Option<String> tenantId,
            final Configuration conf) {
        try {
            DriverManager.registerDriver(new org.h2.Driver());
        } catch (SQLException e) {
            throw new IllegalStateException("Could not register h2 driver", e);
        }

        String[] predicatePart = EMPTY_STRING_ARRAY;
        if (optionalPredicate.isDefined()) {
            predicatePart = new String[]{ optionalPredicate.get() };
        }
        log.debug("Read [{}] where [{}] from {}", table, predicatePart, jdbcUrl);

        return sqlContext()
                .jdbc(jdbcUrl, table, predicatePart);
    }
}
