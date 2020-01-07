package com.lombardrisk.ignis.spark.staging.execution;

import com.lombardrisk.ignis.spark.api.staging.StagingDatasetConfig;
import com.lombardrisk.ignis.spark.staging.datafields.DataRow;
import com.lombardrisk.ignis.spark.staging.execution.load.CsvDatasetLoader;
import io.vavr.control.Either;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DatasetStagingWorker {

    private CsvDatasetLoader csvDatasetLoader;
    private DatasetValidator datasetValidator;
    private DatasetRegister datasetRegister;

    public DatasetStagingWorker(
            final CsvDatasetLoader csvDatasetLoader,
            final DatasetValidator datasetValidator,
            final DatasetRegister datasetRegister) {
        this.csvDatasetLoader = csvDatasetLoader;
        this.datasetValidator = datasetValidator;
        this.datasetRegister = datasetRegister;
    }

    public void execute(final long rowKeySeed, final StagingDatasetConfig itemRequest) {
        log.debug("Running staging job for, {}", itemRequest);

        JavaRDD<DataRow> srcRDD = csvDatasetLoader.load(itemRequest);
        Either<JavaRDD<String>, Dataset<Row>> validateResult = datasetValidator.validate(itemRequest, srcRDD);
        datasetRegister.register(rowKeySeed, itemRequest, validateResult);

        log.debug("Finished staging job for, {}", itemRequest);
    }
}