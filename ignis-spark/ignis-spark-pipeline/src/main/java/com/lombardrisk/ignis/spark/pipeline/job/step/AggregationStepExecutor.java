package com.lombardrisk.ignis.spark.pipeline.job.step;

import com.lombardrisk.ignis.pipeline.step.common.AggregateTransformation;
import com.lombardrisk.ignis.pipeline.step.common.Transformation;
import com.lombardrisk.ignis.spark.api.DatasetTableLookup;
import com.lombardrisk.ignis.spark.api.pipeline.AggregateStepAppConfig;
import com.lombardrisk.ignis.spark.core.repository.DatasetRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

@Slf4j
@AllArgsConstructor
public class AggregationStepExecutor {

    private final SparkSession sparkSession;
    private final DatasetRepository datasetRepository;

    public Dataset<Row> runAggregation(
            final AggregateStepAppConfig aggregateStepAppConfig,
            final DatasetTableLookup datasetTableLookup) {

        Dataset<Row> inputDataset = datasetRepository.readDataFrame(datasetTableLookup);
        sparkSession.sqlContext()
                .registerDataFrameAsTable(inputDataset, datasetTableLookup.getDatasetName());

        Transformation transformation = aggregateTransformation(aggregateStepAppConfig, datasetTableLookup);
        String sparkSql = transformation.toSparkSql();

        log.debug("Running Spark SQL: [{}]", sparkSql);
        return sparkSession.sql(sparkSql);
    }

    private static Transformation aggregateTransformation(
            final AggregateStepAppConfig aggregateStepAppConfig,
            final DatasetTableLookup datasetTableLookup) {

        return AggregateTransformation.builder()
                .datasetName(datasetTableLookup.getDatasetName())
                .selects(aggregateStepAppConfig.getSelects())
                .filters(aggregateStepAppConfig.getFilters())
                .groupings(aggregateStepAppConfig.getGroupings())
                .outputSchema(aggregateStepAppConfig.getOutputDataset()
                        .getStagingSchemaValidation()
                        .getPhysicalTableName())
                .build();
    }
}
