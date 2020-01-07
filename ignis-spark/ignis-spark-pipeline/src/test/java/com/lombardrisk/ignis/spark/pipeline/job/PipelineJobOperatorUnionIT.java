package com.lombardrisk.ignis.spark.pipeline.job;

import com.google.common.collect.ImmutableList;
import com.lombardrisk.ignis.pipeline.step.api.SelectColumn;
import com.lombardrisk.ignis.pipeline.step.api.UnionAppConfig;
import com.lombardrisk.ignis.pipeline.step.api.UnionSpec;
import com.lombardrisk.ignis.spark.api.DatasetTableLookup;
import com.lombardrisk.ignis.spark.api.fixture.Populated;
import com.lombardrisk.ignis.spark.api.pipeline.PipelineAppConfig;
import com.lombardrisk.ignis.spark.api.pipeline.UnionStepAppConfig;
import com.lombardrisk.ignis.spark.api.pipeline.spark.SparkFunctionConfig;
import com.lombardrisk.ignis.spark.core.fixture.DatasetFixture;
import com.lombardrisk.ignis.spark.core.mock.StatefulDatasetClientStore;
import com.lombardrisk.ignis.spark.core.repository.DatasetRepository;
import com.lombardrisk.ignis.spark.core.schema.DatasetTableSchema;
import com.lombardrisk.ignis.spark.pipeline.TestPipelineApplication;
import com.lombardrisk.ignis.spark.pipeline.mock.MockPipelineStatusClient;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
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

import java.time.LocalDate;
import java.util.List;

import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static com.lombardrisk.ignis.spark.api.pipeline.PipelineStepDatasetLookup.datasetTableLookup;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { TestPipelineApplication.class, })
public class PipelineJobOperatorUnionIT {

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Autowired
    private PipelineJobOperator pipelineJobOperator;

    @Autowired
    private DatasetRepository datasetRepository;

    @Autowired
    private SparkSession sparkSession;

    @Autowired
    private StatefulDatasetClientStore internalDatasetClient;

    @Autowired
    private MockPipelineStatusClient pipelineStatusClient;

    @Before
    public void setUp() {
        pipelineStatusClient.reset();
    }

    @Test
    public void runJob_SingleUnionStep() throws Exception {
        sparkSession.sqlContext().dropTempTable("BUSINESS_CUSTOMERS");
        sparkSession.sqlContext().dropTempTable("RETAIL_CUSTOMERS");

        StructType businessCustomersType = DatasetTableSchema.rowKeyRangedSchema(
                "BUSINESS_CUSTOMERS",
                DataTypes.createStructType(new StructField[]{
                        DataTypes.createStructField("Name", DataTypes.StringType, false),
                        DataTypes.createStructField("Operating Location", DataTypes.StringType, false) }))
                .getStructType();

        StructType retailCustomersType = DatasetTableSchema.rowKeyRangedSchema(
                "RETAIL_CUSTOMERS",
                DataTypes.createStructType(new StructField[]{
                        DataTypes.createStructField("First_Name", DataTypes.StringType, false),
                        DataTypes.createStructField("Last_Name", DataTypes.StringType, false) }))
                .getStructType();

        List<List<?>> businessCustomerRows = ImmutableList.of(
                ImmutableList.of(1L, "Chaos Enterprise", "The Void"),
                ImmutableList.of(2L, "Satan Industries", "Pandemonium"));
        List<List<?>> retailCustomerRows = ImmutableList.of(
                ImmutableList.of(7L, "Lucifer", "Morningstar"),
                ImmutableList.of(8L, "Chloe", "Decker"));

        new DatasetFixture()
                .createDataset(sparkSession, "BUSINESS_CUSTOMERS", businessCustomersType, businessCustomerRows);
        new DatasetFixture()
                .createDataset(sparkSession, "RETAIL_CUSTOMERS", retailCustomersType, retailCustomerRows);

        UnionStepAppConfig unionStep = UnionStepAppConfig.builder()
                .pipelineStepInvocationId(8585L)
                .pipelineStepDatasetLookups(asList(
                        datasetTableLookup(DatasetTableLookup.builder()
                                .datasetName("BUSINESS_CUSTOMERS")
                                .predicate("1=1")
                                .build()),
                        datasetTableLookup(DatasetTableLookup.builder()
                                .datasetName("RETAIL_CUSTOMERS")
                                .predicate("1=1")
                                .build())))
                .unions(newLinkedHashSet(asList(
                        UnionAppConfig.builder()
                                .schemaIn(new UnionSpec("RETAIL_CUSTOMERS", -1L))
                                .selects(newHashSet(SelectColumn.builder()
                                        .select("CONCAT(FIRST_NAME, ' ', LAST_NAME)")
                                        .as("NAME")
                                        .build()))
                                .build(),
                        UnionAppConfig.builder()
                                .schemaIn(new UnionSpec("BUSINESS_CUSTOMERS", -1L))
                                .selects(newHashSet(SelectColumn.builder()
                                        .select("NAME")
                                        .as("NAME")
                                        .build()))
                                .build())))
                .outputDataset(
                        Populated.stagingDatasetConfig()
                                .id(12312L)
                                .stagingSchemaValidation(Populated.stagingSchemaValidation()
                                        .displayName("Union Customers")
                                        .physicalTableName("UNION_CUSTOMERS")
                                        .build())
                                .build())
                .build();

        PipelineAppConfig pipelineAppConfig = PipelineAppConfig.builder()
                .name("test staging")
                .serviceRequestId(1)
                .pipelineInvocationId(8292L)
                .pipelineSteps(singletonList(unionStep))
                .sparkFunctionConfig(SparkFunctionConfig.builder()
                        .referenceDate(LocalDate.of(2019, 1, 1))
                        .build())
                .build();

        pipelineJobOperator.runJob(pipelineAppConfig);

        Dataset<Row> createdDataset = datasetRepository.readDataFrame(DatasetTableLookup.builder()
                .datasetName("UNION_CUSTOMERS")
                .predicate("1=1")
                .build());

        List<Row> rows = createdDataset.collectAsList();
        assertThat(rows)
                .extracting(
                        row -> row.getAs("NAME"),
                        row -> row.getAs("FCR_SYS__RETAIL_CUSTOMERS__ROW_KEY"),
                        row -> row.getAs("FCR_SYS__BUSINESS_CUSTOMERS__ROW_KEY"))
                .containsExactlyInAnyOrder(
                        tuple("Chaos Enterprise", null, 1L),
                        tuple("Satan Industries", null, 2L),
                        tuple("Lucifer Morningstar", 7L, null),
                        tuple("Chloe Decker", 8L, null));
    }

    @Test
    public void runJob_UnionStepWithIntermediateResults() throws Exception {
        sparkSession.sqlContext().dropTempTable("INPUT_ONE");
        sparkSession.sqlContext().dropTempTable("INPUT_TWO");
        sparkSession.sqlContext().dropTempTable("OUTPUT");

        StructType structType = DataTypes.createStructType(new StructField[]{
                DataTypes.createStructField("A", DataTypes.IntegerType, false),
                DataTypes.createStructField("B", DataTypes.IntegerType, false),
                DataTypes.createStructField("C", DataTypes.IntegerType, false) });

        StructType inputOneStruct = DatasetTableSchema.rowKeyRangedSchema("INPUT_ONE", structType).getStructType();
        StructType inputTwoStruct = DatasetTableSchema.rowKeyRangedSchema("INPUT_TWO", structType).getStructType();

        List<List<?>> inputOneData = ImmutableList.of(
                ImmutableList.of(1L, 1, 2, 3),
                ImmutableList.of(2L, 4, 5, 6));
        List<List<?>> inputTwoData = ImmutableList.of(
                ImmutableList.of(3L, 7, 8, 9),
                ImmutableList.of(4L, 10, 11, 12));

        DatasetFixture datasetFixture = new DatasetFixture();
        datasetFixture.createDataset(sparkSession, "INPUT_ONE", inputOneStruct, inputOneData);
        datasetFixture.createDataset(sparkSession, "INPUT_TWO", inputTwoStruct, inputTwoData);

        UnionStepAppConfig unionStep = UnionStepAppConfig.builder()
                .pipelineStepInvocationId(8585L)
                .pipelineStepDatasetLookups(asList(
                        datasetTableLookup(
                                DatasetTableLookup.builder().datasetName("INPUT_ONE").predicate("1=1").build()),
                        datasetTableLookup(
                                DatasetTableLookup.builder().datasetName("INPUT_TWO").predicate("1=1").build())))
                .unions(newLinkedHashSet(asList(
                        UnionAppConfig.builder()
                                .schemaIn(new UnionSpec("INPUT_ONE", -1L))
                                .selects(newLinkedHashSet(asList(
                                        SelectColumn.builder().select("A").as("A").intermediateResult(false).build(),
                                        SelectColumn.builder().select("C + 5").as("C").intermediateResult(true).build(),
                                        SelectColumn.builder().select("C * B").as("B").intermediateResult(false).build()
                                )))
                                .build(),
                        UnionAppConfig.builder()
                                .schemaIn(new UnionSpec("INPUT_TWO", -1L))
                                .selects(newLinkedHashSet(asList(
                                        SelectColumn.builder().select("C * 3").as("C").intermediateResult(true).build(),
                                        SelectColumn.builder().select("C + B").as("B").intermediateResult(true).build(),
                                        SelectColumn.builder().select("B + A").as("A").intermediateResult(false).build()
                                )))
                                .build())))
                .outputDataset(
                        Populated.stagingDatasetConfig()
                                .id(12312L)
                                .stagingSchemaValidation(Populated.stagingSchemaValidation()
                                        .displayName("Union Output")
                                        .physicalTableName("OUTPUT")
                                        .build())
                                .build())
                .build();

        PipelineAppConfig pipelineAppConfig = PipelineAppConfig.builder()
                .name("test union with intermediate results")
                .serviceRequestId(1)
                .pipelineInvocationId(8292L)
                .pipelineSteps(singletonList(unionStep))
                .sparkFunctionConfig(SparkFunctionConfig.builder()
                        .referenceDate(LocalDate.of(2019, 1, 1))
                        .build())
                .build();

        pipelineJobOperator.runJob(pipelineAppConfig);

        Dataset<Row> createdDataset = datasetRepository.readDataFrame(DatasetTableLookup.builder()
                .datasetName("OUTPUT")
                .predicate("1=1")
                .build());

        // 1 (1, 2, 3)    => (1, 16, 8)
        // 2 (4, 5, 6)    => (4, 55, 11)
        // 3 (7, 8, 9)    => (42, 35, 27)
        // 4 (10, 11, 12) => (57, 47, 36)

        List<Row> rows = createdDataset.collectAsList();
        assertThat(rows)
                .extracting(
                        row -> row.getAs("A"),
                        row -> row.getAs("B"),
                        row -> row.getAs("C"),
                        row -> row.getAs("FCR_SYS__INPUT_ONE__ROW_KEY"),
                        row -> row.getAs("FCR_SYS__INPUT_TWO__ROW_KEY"))
                .describedAs("OUTPUT(A, B, C)")
                .containsExactlyInAnyOrder(
                        tuple(1, 16, 8, 1L, null),
                        tuple(4, 55, 11, 2L, null),
                        tuple(42, 35, 27, null, 3L),
                        tuple(57, 47, 36, null, 4L));
    }
}
