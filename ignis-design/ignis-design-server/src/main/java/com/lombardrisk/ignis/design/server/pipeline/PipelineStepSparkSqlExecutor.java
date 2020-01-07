package com.lombardrisk.ignis.design.server.pipeline;

import com.lombardrisk.ignis.design.server.pipeline.converter.SchemaStructTypeConverter;
import com.lombardrisk.ignis.design.server.productconfig.schema.Schema;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.StructType;

import java.util.Map;

@Slf4j
public class PipelineStepSparkSqlExecutor {

    private final SparkSession sparkSession;
    private final SchemaStructTypeConverter schemaStructTypeConverter = new SchemaStructTypeConverter();

    public PipelineStepSparkSqlExecutor(final SparkSession sparkSession) {
        this.sparkSession = sparkSession;
    }

    public Dataset<Row> executeSqlNew(
            final String sql, final Map<Schema, Dataset<Row>> inputData) {

        for (Map.Entry<Schema, Dataset<Row>> schemaAndData : inputData.entrySet()) {
            Schema schemaIn = schemaAndData.getKey();
            Dataset<Row> data = schemaAndData.getValue();

            StructType schemaInStructType = schemaStructTypeConverter.apply(schemaIn.getFields());
            log.debug("Saving for schema {} dummy data {}", schemaInStructType, data.collectAsList());

            sparkSession.sqlContext()
                    .registerDataFrameAsTable(data, schemaIn.getPhysicalTableName());
        }

        return sparkSession.sql(sql);
    }
}
