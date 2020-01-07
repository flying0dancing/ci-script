package com.lombardrisk.ignis.spark.core.repository;

import com.lombardrisk.ignis.spark.api.DatasetTableLookup;
import com.lombardrisk.ignis.spark.api.pipeline.PipelineAppConfig;
import com.lombardrisk.ignis.spark.api.pipeline.PipelineStepAppConfig;
import com.lombardrisk.ignis.spark.api.staging.StagingDatasetConfig;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

public interface RowKeyedDatasetRepository {

    /**
     * Generate and save dataset created from a staging job with row keys.
     *
     * @param rowKeySeed           Seed to use to generate row key range
     * @param dataset              Dataset to save
     * @param stagingDatasetConfig Staging configuration for saving Dataset entity
     * @return Dataset name and predicate for reading back the dataset
     */
    DatasetTableLookup generateRowKeyAndSaveDataset(
            final long rowKeySeed,
            final Dataset<Row> dataset,
            final StagingDatasetConfig stagingDatasetConfig);

    /**
     * Generate and save dataset created from pipeline job with row keys.
     *
     * @param rowKeySeed            Seed to use to generate row key range
     * @param dataset               Dataset to save
     * @param pipelineAppConfig     Pipeline configuration for saving Dataset entity
     * @param pipelineStepAppConfig Pipeline step configuration for saving Dataset entity
     * @return Dataset name and predicate for reading back the dataset
     */
    DatasetTableLookup generateRowKeyAndSaveDataset(
            final long rowKeySeed,
            final Dataset<Row> dataset,
            final PipelineAppConfig pipelineAppConfig,
            final PipelineStepAppConfig pipelineStepAppConfig);

    /**
     * Read dataset from table using predicate
     *
     * @param datasetTableLookup Dataset to read
     * @return Spark dataset
     */
    Dataset<Row> readDataFrame(DatasetTableLookup datasetTableLookup);
}