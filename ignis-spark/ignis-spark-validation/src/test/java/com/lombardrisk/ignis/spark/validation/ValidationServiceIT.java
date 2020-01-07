package com.lombardrisk.ignis.spark.validation;

import com.google.common.collect.ImmutableList;
import com.lombardrisk.ignis.spark.TestValidationApplication;
import com.lombardrisk.ignis.spark.api.DatasetTableLookup;
import com.lombardrisk.ignis.spark.api.validation.DatasetValidationRule;
import com.lombardrisk.ignis.spark.core.fixture.DatasetFixture;
import com.lombardrisk.ignis.spark.core.repository.RowKeyedDatasetRepository;
import com.lombardrisk.ignis.spark.core.schema.DatasetTableSchema;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static com.lombardrisk.ignis.api.rule.ValidationOutput.DATASET_ID;
import static com.lombardrisk.ignis.api.rule.ValidationOutput.DATASET_ROW_KEY;
import static com.lombardrisk.ignis.api.rule.ValidationOutput.ERROR_MESSAGE;
import static com.lombardrisk.ignis.api.rule.ValidationOutput.ResultType.SUCCESS;
import static com.lombardrisk.ignis.api.rule.ValidationOutput.VALIDATION_RESULT_TYPE;
import static com.lombardrisk.ignis.api.rule.ValidationOutput.VALIDATION_RULE_ID;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { TestValidationApplication.class })
public class ValidationServiceIT {

    @Rule
    public final JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Autowired
    private ValidationService validationService;

    @Autowired
    private SparkSession sparkSession;

    @Autowired
    private RowKeyedDatasetRepository rowKeyedDatasetRepository;

    private DatasetFixture datasetFixture;

    @Before
    public void setUp() {
        datasetFixture = new DatasetFixture();
    }

    @Test
    public void executeRule_SingleRowValidation_SavesFailingRowToOutputTable() throws Exception {
        //drop old data
        SparkSession spark = sparkSession;
        spark.sql("DROP TABLE IF EXISTS FRY14Q_95");

        //create test data
        StructField interestRateIndex =
                new StructField("InterestRateIndex", DataTypes.LongType, false, Metadata.empty());

        StructType fry14Q_95 = DatasetTableSchema.rowKeyRangedSchema(
                "FRY14Q_95", new StructType(new StructField[]{ interestRateIndex }))
                .getStructType();

        List<List<?>> rowValues = ImmutableList.of(
                ImmutableList.of(500L, 1L),
                ImmutableList.of(501L, 2L),
                ImmutableList.of(502L, 3L),
                ImmutableList.of(503L, 4L),
                ImmutableList.of(504L, 5L),
                ImmutableList.of(505L, 6L),
                ImmutableList.of(506L, 7L));

        datasetFixture.createDataset(spark, "FRY14Q_95", fry14Q_95, rowValues);

        List<Long> rowKeys = spark.sql("select ROW_KEY from FRY14Q_95")
                .distinct()
                .collectAsList()
                .stream()
                .map((Row row) -> row.getLong(0))
                .collect(toList());

        assertThat(rowKeys).hasSize(7);

        Long failingRowKey = (Long) spark.sql("select ROW_KEY from FRY14Q_95 WHERE InterestRateIndex = 7")
                .distinct()
                .first()
                .get(0);

        //run rule
        DatasetValidationRule validationRule = DatasetValidationRule.builder()
                .id(400)
                .name("Test rule")
                .expression("InterestRateIndex > 0 && InterestRateIndex < 7")
                .build();

        Dataset<Row> dataset = rowKeyedDatasetRepository.readDataFrame(DatasetTableLookup.builder()
                .datasetName("FRY14Q_95")
                .build());

        ValidationRuleStatistics validationResult = validationService.executeRule(
                validationRule, 5000L, dataset)
                .get();

        Dataset<Row> outputTableRowSet = spark.sql("select * from VALIDATION_RULE_RESULTS");
        outputTableRowSet.show();

        assertThat(validationResult.getNumberOfFailures()).isEqualTo(1);

        //verify output tables
        assertThat(outputTableRowSet.count()).isEqualTo(7);

        soft.assertThat(outputTableRowSet.select(DATASET_ROW_KEY).collectAsList())
                .extracting(row -> row.get(0))
                .containsOnlyElementsOf(rowKeys);
        soft.assertThat(outputTableRowSet.select(VALIDATION_RULE_ID).collectAsList())
                .extracting(row -> row.get(0))
                .containsOnly(400L);
        soft.assertThat(outputTableRowSet.select(DATASET_ID).collectAsList())
                .extracting(row -> row.get(0))
                .containsOnly(5000L);
        soft.assertThat(outputTableRowSet.select(VALIDATION_RESULT_TYPE)
                .where(DATASET_ROW_KEY + " = " + failingRowKey)
                .first()
                .getString(0))
                .isEqualTo("FAIL");
        soft.assertThat(outputTableRowSet.select(VALIDATION_RESULT_TYPE)
                .where(DATASET_ROW_KEY + " != " + failingRowKey)
                .first()
                .getString(0))
                .isEqualTo("SUCCESS");
    }

    @Test
    public void executeRule_SingleRowValidationExpressionErrors_SavesErroringRowToOutputTable() throws Exception {
        //drop old data
        SparkSession spark = sparkSession;
        spark.sql("DROP TABLE IF EXISTS NEIGHBOURINOS");

        //create test data
        StructField name =
                new StructField("Name", DataTypes.StringType, false, Metadata.empty());

        StructType structType = DatasetTableSchema.rowKeyRangedSchema(
                "FRY14Q_95", new StructType(new StructField[]{ name }))
                .getStructType();

        List<List<?>> rowValues = ImmutableList.of(
                ImmutableList.of(1L, "Ned"),
                ImmutableList.of(2L, "Homer"),
                ImmutableList.of(3L, "Marge"));

        datasetFixture.createDataset(spark, "NEIGHBOURINOS", structType, rowValues);

        List<Long> rowKeys = spark.sql("select ROW_KEY from NEIGHBOURINOS")
                .distinct()
                .collectAsList()
                .stream()
                .map((Row row) -> row.getLong(0))
                .collect(toList());

        assertThat(rowKeys).hasSize(3);

        Long erroringRowKey = (Long) spark.sql("select ROW_KEY from NEIGHBOURINOS WHERE Name = 'Ned'")
                .distinct()
                .first()
                .get(0);

        //run rule
        DatasetValidationRule validationRule = DatasetValidationRule.builder()
                .id(400)
                .name("Test rule")
                .expression("Name == 'Ned' ? (Name * 100) : true")
                .build();

        Dataset<Row> dataset = rowKeyedDatasetRepository.readDataFrame(DatasetTableLookup.builder()
                .datasetName("NEIGHBOURINOS")
                .build());

        ValidationRuleStatistics validationResult = validationService.executeRule(
                validationRule, 5000L, dataset)
                .get();

        assertThat(validationResult.getNumberOfErrors()).isEqualTo(1);

        Dataset<Row> outputTableRowSet = spark.sql("select * from VALIDATION_RULE_RESULTS");
        outputTableRowSet.show();

        //verify output tables
        assertThat(outputTableRowSet.count()).isEqualTo(3);

        soft.assertThat(outputTableRowSet.select(VALIDATION_RESULT_TYPE)
                .where(DATASET_ROW_KEY + " = " + erroringRowKey)
                .first()
                .getString(0))
                .isEqualTo("ERROR");

        soft.assertThat(outputTableRowSet.select(ERROR_MESSAGE)
                .where(DATASET_ROW_KEY + " = " + erroringRowKey)
                .first()
                .getString(0))
                .isEqualTo("java.lang.NumberFormatException: For input string: \"Ned\"");

        soft.assertThat(outputTableRowSet.where(VALIDATION_RESULT_TYPE + " = '" + SUCCESS + "'")
                .count())
                .isEqualTo(2);
    }
}