package com.lombardrisk.ignis.spark.pipeline.job.step;

import com.lombardrisk.ignis.pipeline.step.common.JoinTransformation;
import com.lombardrisk.ignis.pipeline.step.common.Transformation;
import com.lombardrisk.ignis.spark.api.DatasetTableLookup;
import com.lombardrisk.ignis.spark.api.pipeline.JoinStepAppConfig;
import com.lombardrisk.ignis.spark.core.repository.DatasetRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.util.List;

@Slf4j
@AllArgsConstructor
public class JoinStepExecutor {

    private final SparkSession sparkSession;
    private final DatasetRepository datasetRepository;

    public Dataset<Row> runJoin(
            final JoinStepAppConfig joinTransformation,
            final List<DatasetTableLookup> datasetTableLookups) {

        for (DatasetTableLookup datasetTableLookup : datasetTableLookups) {
            Dataset<Row> inputDataset = datasetRepository.readDataFrame(datasetTableLookup);
            sparkSession.sqlContext()
                    .registerDataFrameAsTable(inputDataset, datasetTableLookup.getDatasetName());
        }

        Transformation transformation = joinTransformation(joinTransformation);
        String sparkSql = transformation.toSparkSql();

        log.debug("Running Spark SQL: [{}]", sparkSql);
        return sparkSession.sql(sparkSql);
    }

    private static Transformation joinTransformation(final JoinStepAppConfig joinStepAppConfig) {
        return JoinTransformation.builder()
                .selects(joinStepAppConfig.getSelects())
                .joins(joinStepAppConfig.getJoinAppConfigs())
                .outputSchema(joinStepAppConfig.getOutputDataset().getStagingSchemaValidation().getPhysicalTableName())
                .build();
    }
}
