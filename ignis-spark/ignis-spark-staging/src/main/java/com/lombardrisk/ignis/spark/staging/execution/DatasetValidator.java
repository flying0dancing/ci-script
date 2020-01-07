package com.lombardrisk.ignis.spark.staging.execution;

import com.lombardrisk.ignis.spark.api.staging.StagingDatasetConfig;
import com.lombardrisk.ignis.spark.api.staging.StagingSchemaValidation;
import com.lombardrisk.ignis.spark.staging.datafields.DataRow;
import com.lombardrisk.ignis.spark.staging.execution.converter.SchemaStructTypeConverter;
import io.vavr.control.Either;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.StructType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Optional;

@Slf4j
@Component
public class DatasetValidator implements Serializable {

    private static final long serialVersionUID = 372259429446967450L;

    private final transient StagingListener stagingListener;
    private final SparkSession sparkSession;

    @Autowired
    public DatasetValidator(
            final StagingListener stagingListener,
            final SparkSession sparkSession) {
        this.stagingListener = stagingListener;
        this.sparkSession = sparkSession;
    }

    Either<JavaRDD<String>, Dataset<Row>> validate(final StagingDatasetConfig item, final JavaRDD<DataRow> srcRDD) {
        stagingListener.onValidationStart(item.getId());
        StagingSchemaValidation stagingSchemaValidation = item.getStagingSchemaValidation();
        log.info(
                "Validate dataset using schema [{}] with id [{}]",
                stagingSchemaValidation.getPhysicalTableName(),
                stagingSchemaValidation.getSchemaId());

        DatasetSchemaValidator datasetSchemaValidator = new DatasetSchemaValidator(stagingSchemaValidation);
        JavaRDD<String> invalidRDD = srcRDD.map(datasetSchemaValidator)
                        .filter(Optional::isPresent)
                        .map(Optional::get);

        if (invalidRDD.isEmpty()) {
            JavaRDD<Row> javaRDD = srcRDD.map(dataRow -> toStructRow(dataRow, datasetSchemaValidator));
            StructType structType = new SchemaStructTypeConverter().apply(stagingSchemaValidation);

            log.trace("Convert RDD to DataFrame using {}", structType);
            Dataset<Row> validDataset = sparkSession.createDataFrame(javaRDD.rdd(), structType);

            log.info("Dataset for schema [{}] is valid", item.getStagingSchemaValidation().getPhysicalTableName());
            return Either.right(validDataset);
        } else {
            log.warn("Dataset for schema [{}] is not valid", item.getStagingSchemaValidation().getPhysicalTableName());

            return Either.left(invalidRDD);
        }
    }

    private Row toStructRow(final DataRow dataRow, final DatasetSchemaValidator datasetSchemaValidator) {
        Object[] values =
                dataRow.getFields().stream()
                        .map(datasetSchemaValidator::parseField)
                        .toArray(Object[]::new);
        if (log.isTraceEnabled()) {
            log.trace("Converted data\n  {}\n to row values {}", dataRow, Arrays.toString(values));
        }
        return RowFactory.create(values);
    }
}