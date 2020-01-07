package com.lombardrisk.ignis.spark.api.fixture;

import com.lombardrisk.ignis.spark.api.DatasetTableLookup;
import com.lombardrisk.ignis.spark.api.pipeline.MapStepAppConfig;
import com.lombardrisk.ignis.spark.api.pipeline.PipelineAppConfig;
import com.lombardrisk.ignis.spark.api.staging.DatasetProperties;
import com.lombardrisk.ignis.spark.api.staging.DownstreamPipeline;
import com.lombardrisk.ignis.spark.api.staging.StagingAppConfig;
import com.lombardrisk.ignis.spark.api.staging.StagingDatasetConfig;
import com.lombardrisk.ignis.spark.api.staging.StagingErrorOutput;
import com.lombardrisk.ignis.spark.api.staging.StagingSchemaValidation;
import com.lombardrisk.ignis.spark.api.staging.datasource.HdfsCsvDataSource;
import com.lombardrisk.ignis.spark.api.staging.field.BooleanFieldValidation;
import com.lombardrisk.ignis.spark.api.staging.field.DateFieldValidation;
import com.lombardrisk.ignis.spark.api.staging.field.DecimalFieldValidation;
import com.lombardrisk.ignis.spark.api.staging.field.DoubleFieldValidation;
import com.lombardrisk.ignis.spark.api.staging.field.FloatFieldValidation;
import com.lombardrisk.ignis.spark.api.staging.field.IntegerFieldValidation;
import com.lombardrisk.ignis.spark.api.staging.field.LongFieldValidation;
import com.lombardrisk.ignis.spark.api.staging.field.StringFieldValidation;
import com.lombardrisk.ignis.spark.api.staging.field.TimestampFieldValidation;
import com.lombardrisk.ignis.spark.api.validation.DatasetValidationJobRequest;
import com.lombardrisk.ignis.spark.api.validation.DatasetValidationRule;
import lombok.experimental.UtilityClass;

import java.time.LocalDate;

import static com.google.common.collect.Sets.newHashSet;
import static com.lombardrisk.ignis.spark.api.pipeline.PipelineStepDatasetLookup.datasetTableLookup;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;

@UtilityClass
public class Populated {

    public static StagingAppConfig.StagingAppConfigBuilder stagingAppConfig() {
        return StagingAppConfig.builder()
                .name("CQRS")
                .serviceRequestId(144)
                .items(newHashSet(stagingDatasetConfig().build()))
                .downstreamPipelines(newHashSet(downstreamPipelineRequest().build()));
    }

    public static StagingDatasetConfig.StagingDatasetConfigBuilder stagingDatasetConfig() {
        return StagingDatasetConfig.builder()
                .stagingSchemaValidation(stagingSchemaValidation().build())
                .source(csvDataSource().build())
                .stagingErrorOutput(stagingErrorOutput().build())
                .datasetProperties(datasetProperties().build());
    }

    public static DownstreamPipeline.DownstreamPipelineBuilder downstreamPipelineRequest() {
        return DownstreamPipeline.builder()
                .entityCode("some entity code")
                .referenceDate(LocalDate.now())
                .pipelineId(1L);
    }

    public static StagingErrorOutput.StagingErrorOutputBuilder stagingErrorOutput() {
        return StagingErrorOutput.builder()
                .errorFilePath("user/ubuntu/datasets/1/E_TEST.CSV")
                .errorFileSystemUri("hdfs://i-01823727231.com:9000")
                .temporaryFilePath("/tmp");
    }

    public static DatasetProperties.DatasetPropertiesBuilder datasetProperties() {
        return DatasetProperties.builder()
                .entityCode("entity123")
                .referenceDate(LocalDate.of(2018, 1, 1));
    }

    public static StagingSchemaValidation.StagingSchemaValidationBuilder stagingSchemaValidation() {
        return StagingSchemaValidation.builder()
                .schemaId(0L)
                .physicalTableName("CQRS")
                .fields(newHashSet(decimalFieldValidation().build()));
    }

    public static HdfsCsvDataSource.HdfsCsvDataSourceBuilder csvDataSource() {
        return HdfsCsvDataSource.builder()
                .header(true)
                .localPath("target/datasource.csv")
                .hdfsPath("target/datasource")
                .start(0)
                .end(10);
    }

    public static DecimalFieldValidation.DecimalFieldValidationBuilder decimalFieldValidation(final String name) {
        return decimalFieldValidation().name(name);
    }

    public static DecimalFieldValidation.DecimalFieldValidationBuilder decimalFieldValidation() {
        return DecimalFieldValidation.builder()
                .name("DECIMAL")
                .nullable(false)
                .precision(38)
                .scale(2);
    }

    public static StringFieldValidation.StringFieldValidationBuilder stringFieldValidation(final String name) {
        return stringFieldValidation().name(name);
    }

    public static StringFieldValidation.StringFieldValidationBuilder stringFieldValidation() {
        return StringFieldValidation.builder()
                .name("STRING")
                .nullable(false)
                .maxLength(38)
                .minLength(2)
                .regularExpression("[a-zA-Z0-9]");
    }

    public static BooleanFieldValidation.BooleanFieldValidationBuilder booleanFieldValidation(final String name) {
        return booleanFieldValidation().name(name);
    }

    public static BooleanFieldValidation.BooleanFieldValidationBuilder booleanFieldValidation() {
        return BooleanFieldValidation.builder()
                .name("BOOLEAN")
                .nullable(false);
    }

    public static LongFieldValidation.LongFieldValidationBuilder longFieldValidation(final String name) {
        return longFieldValidation().name(name);
    }

    public static LongFieldValidation.LongFieldValidationBuilder longFieldValidation() {
        return LongFieldValidation.builder()
                .name("LONG")
                .nullable(false);
    }

    public static IntegerFieldValidation.IntegerFieldValidationBuilder intFieldValidation(final String name) {
        return intFieldValidation().name(name);
    }

    public static IntegerFieldValidation.IntegerFieldValidationBuilder intFieldValidation() {
        return IntegerFieldValidation.builder()
                .name("INT")
                .nullable(false);
    }

    public static DoubleFieldValidation.DoubleFieldValidationBuilder doubleFieldValidation(final String name) {
        return doubleFieldValidation().name(name);
    }

    public static DoubleFieldValidation.DoubleFieldValidationBuilder doubleFieldValidation() {
        return DoubleFieldValidation.builder()
                .name("DOUBLE")
                .nullable(false);
    }

    public static FloatFieldValidation.FloatFieldValidationBuilder floatFieldValidation(final String name) {
        return floatFieldValidation().name(name);
    }

    public static FloatFieldValidation.FloatFieldValidationBuilder floatFieldValidation() {
        return FloatFieldValidation.builder()
                .name("FLOAT")
                .nullable(false);
    }

    public static DateFieldValidation.DateFieldValidationBuilder dateFieldValidation(final String name) {
        return dateFieldValidation().name(name);
    }

    public static DateFieldValidation.DateFieldValidationBuilder dateFieldValidation() {
        return DateFieldValidation.builder()
                .name("DATE")
                .nullable(false)
                .format("dd/MM/yyyy");
    }

    public static TimestampFieldValidation.TimestampFieldValidationBuilder timestampFieldValidation(final String name) {
        return timestampFieldValidation().name(name);
    }

    public static TimestampFieldValidation.TimestampFieldValidationBuilder timestampFieldValidation() {
        return TimestampFieldValidation.builder()
                .name("TIMESTAMP")
                .nullable(false)
                .format("dd/MM/yyyy");
    }

    public static DatasetValidationJobRequest.DatasetValidationJobRequestBuilder validationJobRequest() {
        return DatasetValidationJobRequest.builder()
                .name("DatasetValidationJobRequest")
                .datasetName("CQRS")
                .datasetValidationRule(datasetValidationRule().build())
                .datasetId(1L);
    }

    public static DatasetValidationRule.DatasetValidationRuleBuilder datasetValidationRule() {
        return DatasetValidationRule.builder()
                .name("ValidationRule1")
                .expression("DECIMAL == 0.01");
    }

    public static PipelineAppConfig.PipelineAppConfigBuilder pipelineAppConfig() {
        return PipelineAppConfig.builder()
                .serviceRequestId(12)
                .name("pipeline job")
                .pipelineSteps(singletonList(pipelineMapItem().build()));
    }

    public static MapStepAppConfig.MapStepAppConfigBuilder pipelineMapItem() {
        return MapStepAppConfig.builder()
                .pipelineStepDatasetLookup(datasetTableLookup(datasetLookup().build()))
                .outputDataset(stagingDatasetConfig().build())
                .selects(emptySet());
    }

    public static DatasetTableLookup.DatasetTableLookupBuilder datasetLookup() {
        return DatasetTableLookup.builder()
                .datasetName("EMPLOYEES")
                .predicate("1=1");
    }
}
