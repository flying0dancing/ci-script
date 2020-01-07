package com.lombardrisk.ignis.spark.core.spark;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

@Slf4j
@UtilityClass
@SuppressWarnings("squid:S1157")
public class PhoenixDataFrameAdapter {

    public static Dataset<Row> handleLowerCaseColumns(final Dataset<Row> dataset) {
        Dataset<Row> renamed = dataset;
        for (String column : dataset.columns()) {
            if (!column.toUpperCase().equals(column)) {
                log.debug("Renaming column {} to {}", column, "\"" + column + "\"");
                renamed = renamed.withColumnRenamed(column, "\"" + column + "\"");
            }
        }

        log.debug("New schema {}", renamed.schema());
        return renamed;
    }
}
