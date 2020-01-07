package com.lombardrisk.ignis.spark.pipeline.job;

import com.google.common.collect.ImmutableList;
import com.lombardrisk.ignis.client.external.pipeline.view.PipelineStepStatus;
import com.lombardrisk.ignis.client.internal.CreateDatasetCall;
import com.lombardrisk.ignis.client.internal.UpdatePipelineStepStatusRequest;
import com.lombardrisk.ignis.pipeline.step.api.JoinAppConfig;
import com.lombardrisk.ignis.pipeline.step.api.JoinFieldConfig;
import com.lombardrisk.ignis.pipeline.step.api.SelectColumn;
import com.lombardrisk.ignis.pipeline.step.api.WindowSpec;
import com.lombardrisk.ignis.spark.api.DatasetTableLookup;
import com.lombardrisk.ignis.spark.api.fixture.Populated;
import com.lombardrisk.ignis.spark.api.pipeline.AggregateStepAppConfig;
import com.lombardrisk.ignis.spark.api.pipeline.JoinStepAppConfig;
import com.lombardrisk.ignis.spark.api.pipeline.MapStepAppConfig;
import com.lombardrisk.ignis.spark.api.pipeline.PipelineAppConfig;
import com.lombardrisk.ignis.spark.api.pipeline.PipelineStepDatasetLookup;
import com.lombardrisk.ignis.spark.api.pipeline.WindowStepAppConfig;
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
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static com.lombardrisk.ignis.pipeline.step.api.OrderSpec.Direction.DESC;
import static com.lombardrisk.ignis.pipeline.step.api.OrderSpec.column;
import static com.lombardrisk.ignis.spark.api.pipeline.PipelineStepDatasetLookup.datasetTableLookup;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

@RunWith(Enclosed.class)
public class PipelineJobOperatorIT {

    @RunWith(SpringRunner.class)
    @SpringBootTest(classes = { TestPipelineApplication.class, })
    public static class MapTransformationTest {

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
        public void runJob_SingleMapStep() throws Exception {
            sparkSession.sqlContext().dropTempTable("EMPLOYEES");

            StructType employeesStructType = DatasetTableSchema.rowKeyRangedSchema(
                    "EMPLOYEES",
                    DataTypes.createStructType(new StructField[]{
                            DataTypes.createStructField("Name", DataTypes.StringType, false),
                            DataTypes.createStructField("Department", DataTypes.StringType, false) }))
                    .getStructType();

            List<List<?>> rowValues = ImmutableList.of(
                    ImmutableList.of(1L, "Matt", "Development"),
                    ImmutableList.of(2L, "Jaya", "QA"));

            new DatasetFixture()
                    .createDataset(sparkSession, "EMPLOYEES", employeesStructType, rowValues);

            MapStepAppConfig mapStep = MapStepAppConfig.builder()
                    .pipelineStepInvocationId(8585L)
                    .pipelineStepDatasetLookup(datasetTableLookup(DatasetTableLookup.builder()
                            .datasetName("EMPLOYEES")
                            .predicate("1=1")
                            .rowKeySeed(1L)
                            .build()))
                    .selects(newLinkedHashSet(Arrays.asList(
                            SelectColumn.builder()
                                    .select("get_reference_date()")
                                    .intermediateResult(true)
                                    .as("RefDate")
                                    .build(),
                            SelectColumn.builder()
                                    .select("CONCAT(NAME, DEPARTMENT, RefDate)")
                                    .as("ND")
                                    .build())))
                    .outputDataset(
                            Populated.stagingDatasetConfig()
                                    .id(12312L)
                                    .stagingSchemaValidation(Populated.stagingSchemaValidation()
                                            .displayName("Mapped Employees")
                                            .physicalTableName("MAPPED_EMPLOYEES")
                                            .build())
                                    .build())
                    .build();

            PipelineAppConfig pipelineAppConfig = PipelineAppConfig.builder()
                    .name("test staging")
                    .sparkFunctionConfig(SparkFunctionConfig.builder()
                            .referenceDate(LocalDate.of(2019, 1, 1))
                            .build())
                    .serviceRequestId(1)
                    .pipelineInvocationId(8292L)
                    .pipelineSteps(singletonList(mapStep))
                    .build();

            pipelineJobOperator.runJob(pipelineAppConfig);

            Dataset<Row> createdDataset = datasetRepository.readDataFrame(DatasetTableLookup.builder()
                    .datasetName("MAPPED_EMPLOYEES")
                    .predicate("1=1")
                    .build());

            List<Row> rows = createdDataset.collectAsList();
            assertThat(rows)
                    .extracting(
                            row -> row.<Long>getAs("ROW_KEY"),
                            row -> row.<Date>getAs("RefDate"),
                            row -> row.<String>getAs("ND"),
                            row -> row.<Long>getAs("FCR_SYS__EMPLOYEES__ROW_KEY"))
                    .containsOnly(
                            tuple(4294967296L, Date.valueOf("2019-01-01"), "MattDevelopment2019-01-01", 1L),
                            tuple(4294967297L, Date.valueOf("2019-01-01"), "JayaQA2019-01-01", 2L));

            CreateDatasetCall createDatasetCall = internalDatasetClient.getCachedDatasetCall();

            soft.assertThat(createDatasetCall.getStagingJobId())
                    .isNull();
            soft.assertThat(createDatasetCall.getPipelineJobId())
                    .isEqualTo(1);
            soft.assertThat(createDatasetCall.getPipelineInvocationId())
                    .isEqualTo(8292L);
            soft.assertThat(createDatasetCall.getPredicate())
                    .isEqualTo("ROW_KEY >= 4294967296 and ROW_KEY <= 8589934591");

            soft.assertThat(pipelineStatusClient.getRequests())
                    .containsOnlyKeys(8292L);

            soft.assertThat(pipelineStatusClient.getRequests().get(8292L))
                    .containsExactly(
                            new UpdatePipelineStepStatusRequest(8585L, PipelineStepStatus.RUNNING),
                            new UpdatePipelineStepStatusRequest(8585L, PipelineStepStatus.SUCCESS));
        }

        @Test
        public void runJob_SingleMapStepWithFilter() throws Exception {
            sparkSession.sqlContext().dropTempTable("EMPLOYEES");

            StructType employeesStructType = DatasetTableSchema.rowKeyRangedSchema(
                    "EMPLOYEES",
                    DataTypes.createStructType(new StructField[]{
                            DataTypes.createStructField("Name", DataTypes.StringType, false),
                            DataTypes.createStructField("Department", DataTypes.StringType, false) }))
                    .getStructType();

            List<List<?>> rowValues = ImmutableList.of(
                    ImmutableList.of(1L, "Eeny", "Development"),
                    ImmutableList.of(2L, "Meeny", "QA"),
                    ImmutableList.of(3L, "Miney", "Development"),
                    ImmutableList.of(4L, "Moe", "QA"));

            new DatasetFixture()
                    .createDataset(sparkSession, "EMPLOYEES", employeesStructType, rowValues);

            MapStepAppConfig mapStep = MapStepAppConfig.builder()
                    .pipelineStepDatasetLookup(datasetTableLookup(DatasetTableLookup.builder()
                            .datasetName("EMPLOYEES")
                            .predicate("1=1")
                            .rowKeySeed(1L)
                            .build()))
                    .selects(newHashSet(SelectColumn.builder().select("CONCAT(NAME, DEPARTMENT)").as("ND").build()))
                    .filters(newHashSet("Department='Development'"))
                    .outputDataset(
                            Populated.stagingDatasetConfig()
                                    .id(12312L)
                                    .stagingSchemaValidation(Populated.stagingSchemaValidation()
                                            .displayName("Mapped Employees")
                                            .physicalTableName("MAPPED_EMPLOYEES")
                                            .build())
                                    .build())
                    .build();

            PipelineAppConfig pipelineAppConfig = PipelineAppConfig.builder()
                    .name("test staging")
                    .serviceRequestId(1)
                    .pipelineInvocationId(8292L)
                    .pipelineSteps(singletonList(mapStep))
                    .sparkFunctionConfig(SparkFunctionConfig.builder().build())
                    .build();

            pipelineJobOperator.runJob(pipelineAppConfig);

            Dataset<Row> createdDataset = datasetRepository.readDataFrame(DatasetTableLookup.builder()
                    .datasetName("MAPPED_EMPLOYEES")
                    .predicate("1=1")
                    .build());

            List<Row> rows = createdDataset.collectAsList();
            assertThat(rows)
                    .extracting(
                            row -> row.<Long>getAs("ROW_KEY"),
                            row -> row.<String>getAs("ND"),
                            row -> row.<Long>getAs("FCR_SYS__EMPLOYEES__ROW_KEY"))
                    .containsOnly(
                            tuple(4294967296L, "EenyDevelopment", 1L),
                            tuple(4294967297L, "MineyDevelopment", 3L));

            CreateDatasetCall createDatasetCall = internalDatasetClient.getCachedDatasetCall();

            assertThat(createDatasetCall.getRecordsCount())
                    .isEqualTo(2L);
        }
    }

    @RunWith(SpringRunner.class)
    @SpringBootTest(classes = { TestPipelineApplication.class, })
    public static class AggregateTransformationTest {

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

        @Test
        public void runJob_SingleAggregationStep() throws Exception {
            sparkSession.sqlContext().dropTempTable("EMPLOYEE");
            sparkSession.sqlContext().dropTempTable("DEPARTMENT_SALARY");

            AggregateStepAppConfig aggregationStep = AggregateStepAppConfig.builder()
                    .pipelineStepDatasetLookup(datasetTableLookup(DatasetTableLookup.builder()
                            .datasetName("EMPLOYEE")
                            .predicate("1=1")
                            .rowKeySeed(1L)
                            .build()))
                    .selects(newLinkedHashSet(newHashSet(
                            SelectColumn.builder().select("FIRST(Department)").as("Department").build(),
                            SelectColumn.builder().select("FIRST(DepartmentCode)").as("DepartmentCode").build(),
                            SelectColumn.builder().select("SUM(Salary)").as("TotalSalary").build(),
                            SelectColumn.builder().select("AVG(Salary)").as("AverageSalary").build())))
                    .groupings(newHashSet("Department", "DepartmentCode"))
                    .outputDataset(
                            Populated.stagingDatasetConfig()
                                    .id(12312L)
                                    .stagingSchemaValidation(Populated.stagingSchemaValidation()
                                            .displayName("Department Salary")
                                            .physicalTableName("DEPARTMENT_SALARY")
                                            .build())
                                    .build())
                    .build();

            PipelineAppConfig pipelineAppConfig = PipelineAppConfig.builder()
                    .name("test aggregating dataset")
                    .serviceRequestId(1)
                    .pipelineInvocationId(8292L)
                    .pipelineSteps(singletonList(aggregationStep))
                    .sparkFunctionConfig(SparkFunctionConfig.builder().build())
                    .build();

            StructType employeeStructType = DatasetTableSchema.rowKeyRangedSchema(
                    "EMPLOYEE",
                    DataTypes.createStructType(new StructField[]{
                            DataTypes.createStructField("Name", DataTypes.StringType, false),
                            DataTypes.createStructField("Department", DataTypes.StringType, false),
                            DataTypes.createStructField("DepartmentCode", DataTypes.StringType, false),
                            DataTypes.createStructField("Salary", DataTypes.IntegerType, false) }))
                    .getStructType();

            List<List<?>> rowValues = ImmutableList.of(
                    ImmutableList.of(1L, "Eeny", "Development", "DEPT01", 11000),
                    ImmutableList.of(2L, "Meeny", "QA", "DEPT02", 12000),
                    ImmutableList.of(3L, "Miney", "HR", "DEPT03", 13500),
                    ImmutableList.of(4L, "Moe", "Development", "DEPT01", 16000)
            );

            new DatasetFixture()
                    .createDataset(sparkSession, "EMPLOYEE", employeeStructType, rowValues);

            pipelineJobOperator.runJob(pipelineAppConfig);

            Dataset<Row> aggregatedDataset = datasetRepository.readDataFrame(DatasetTableLookup.builder()
                    .datasetName("DEPARTMENT_SALARY")
                    .predicate("1=1")
                    .build());
            aggregatedDataset.show();

            List<Row> rows = aggregatedDataset.collectAsList();

            assertThat(rows)
                    .extracting(row -> row.<Long>getAs("ROW_KEY"))
                    .containsExactlyInAnyOrder(4294967296L, 4294967297L, 4294967298L);

            assertThat(rows)
                    .extracting(
                            row -> row.<String>getAs("Department"),
                            row -> row.<String>getAs("DepartmentCode"),
                            row -> row.<Long>getAs("TotalSalary"),
                            row -> row.<Double>getAs("AverageSalary"),
                            row -> row.<String>getAs("FCR_SYS__EMPLOYEE__Department"),
                            row -> row.<String>getAs("FCR_SYS__EMPLOYEE__DepartmentCode"))
                    .containsExactlyInAnyOrder(
                            tuple("Development", "DEPT01", 27000L, 13500D, "Development", "DEPT01"),
                            tuple("QA", "DEPT02", 12000L, 12000D, "QA", "DEPT02"),
                            tuple("HR", "DEPT03", 13500L, 13500D, "HR", "DEPT03")
                    );

            CreateDatasetCall createDatasetCall = internalDatasetClient.getCachedDatasetCall();

            assertThat(createDatasetCall.getRecordsCount())
                    .isEqualTo(3L);

            assertThat(createDatasetCall.getPredicate())
                    .isEqualTo("ROW_KEY >= 4294967296 and ROW_KEY <= 8589934591");
        }

        @Test
        public void runJob_SingleAggregationStepWithFilter() throws Exception {
            sparkSession.sqlContext().dropTempTable("CURRENCY");
            sparkSession.sqlContext().dropTempTable("LEAST_USED_CURRENCIES");

            AggregateStepAppConfig aggregationStep = AggregateStepAppConfig.builder()
                    .pipelineStepDatasetLookup(datasetTableLookup(DatasetTableLookup.builder()
                            .datasetName("CURRENCY")
                            .predicate("1=1")
                            .rowKeySeed(1L)
                            .build()))
                    .selects(newHashSet(SelectColumn.builder()
                            .select("CONCAT_WS(',', COLLECT_SET(Code))")
                            .as("LeastUsed")
                            .build()))
                    .filters(newHashSet("Rank=9"))
                    .groupings(emptySet())
                    .outputDataset(
                            Populated.stagingDatasetConfig()
                                    .id(12312L)
                                    .stagingSchemaValidation(Populated.stagingSchemaValidation()
                                            .displayName("The least used currencies")
                                            .physicalTableName("LEAST_USED_CURRENCIES")
                                            .build())
                                    .build())
                    .build();

            PipelineAppConfig pipelineAppConfig = PipelineAppConfig.builder()
                    .name("test filtering dataset")
                    .serviceRequestId(1)
                    .pipelineInvocationId(8292L)
                    .pipelineSteps(singletonList(aggregationStep))
                    .sparkFunctionConfig(SparkFunctionConfig.builder().build())
                    .build();

            StructType currencyStructType = DatasetTableSchema.rowKeyRangedSchema(
                    "CURRENCY",
                    DataTypes.createStructType(new StructField[]{
                            DataTypes.createStructField("Code", DataTypes.StringType, false),
                            DataTypes.createStructField("Rank", DataTypes.IntegerType, false) }))
                    .getStructType();

            List<List<?>> rowValues = ImmutableList.of(
                    ImmutableList.of(1L, "EUR", 9),
                    ImmutableList.of(2L, "USD", 2),
                    ImmutableList.of(3L, "GBP", 1),
                    ImmutableList.of(4L, "AUD", 9),
                    ImmutableList.of(5L, "JPY", 9)
            );

            new DatasetFixture()
                    .createDataset(sparkSession, "CURRENCY", currencyStructType, rowValues);

            pipelineJobOperator.runJob(pipelineAppConfig);

            List<Row> rows = datasetRepository.readDataFrame(DatasetTableLookup.builder()
                    .datasetName("LEAST_USED_CURRENCIES")
                    .predicate("1=1")
                    .build())
                    .collectAsList();

            assertThat(rows)
                    .extracting(row -> row.getString(1))
                    .containsExactly("EUR,AUD,JPY");
        }
    }

    @RunWith(SpringRunner.class)
    @SpringBootTest(classes = { TestPipelineApplication.class, })
    public static class JoinTransformationTest {

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

        @Test
        public void runJob_SingleJoinStep() throws Exception {
            sparkSession.sqlContext().dropTempTable("STOCK_QUOTES");
            sparkSession.sqlContext().dropTempTable("STOCK_NAMES");
            sparkSession.sqlContext().dropTempTable("TRADES_RAW");

            JoinStepAppConfig joinStep = JoinStepAppConfig.builder()
                    .pipelineStepDatasetLookups(asList(
                            datasetTableLookup(Populated.datasetLookup()
                                    .datasetName("STOCK_QUOTES")
                                    .build()),
                            datasetTableLookup(Populated.datasetLookup()
                                    .datasetName("STOCK_NAMES")
                                    .build()),
                            datasetTableLookup(Populated.datasetLookup()
                                    .datasetName("TRADES_RAW")
                                    .build())))
                    .selects(newLinkedHashSet(newHashSet(
                            SelectColumn.builder().select("TRADES_RAW.TRADE_DATE").as("TRADE_DATE").build(),
                            SelectColumn.builder().select("STOCK_NAMES.STOCK_NAME").as("STOCK").build(),
                            SelectColumn.builder().select("STOCK_QUOTES.RATE").as("RATE").build())))
                    .joinAppConfigs(newHashSet(
                            JoinAppConfig.builder()
                                    .joinType(JoinAppConfig.JoinType.INNER)
                                    .leftSchemaName("TRADES_RAW")
                                    .rightSchemaName("STOCK_NAMES")
                                    .joinFields(asList(
                                            JoinFieldConfig.builder()
                                                    .leftJoinField("STOCK_ID")
                                                    .rightJoinField("STOCK_ID")
                                                    .build(),
                                            JoinFieldConfig.builder()
                                                    .leftJoinField("STOCK_ALTERNATE_ID")
                                                    .rightJoinField("STOCK_ALTERNATE_ID")
                                                    .build()))
                                    .build(),
                            JoinAppConfig.builder()
                                    .joinType(JoinAppConfig.JoinType.FULL_OUTER)
                                    .leftSchemaName("TRADES_RAW")
                                    .rightSchemaName("STOCK_QUOTES")
                                    .joinFields(asList(JoinFieldConfig.builder()
                                            .leftJoinField("STOCK_ID")
                                            .rightJoinField("STOCK_ID")
                                            .build()))
                                    .build()))
                    .outputDataset(
                            Populated.stagingDatasetConfig()
                                    .stagingSchemaValidation(Populated.stagingSchemaValidation()
                                            .displayName("Joined Trades")
                                            .physicalTableName("TRADES_JOIN")
                                            .build())
                                    .build())
                    .build();

            PipelineAppConfig pipelineAppConfig = Populated.pipelineAppConfig()
                    .pipelineInvocationId(8292L)
                    .pipelineSteps(singletonList(joinStep))
                    .sparkFunctionConfig(SparkFunctionConfig.builder().build())
                    .build();

            StructType stockNameStructType = DatasetTableSchema.rowKeyRangedSchema(
                    "STOCK_NAMES",
                    DataTypes.createStructType(new StructField[]{
                            DataTypes.createStructField("STOCK_ID", DataTypes.LongType, false),
                            DataTypes.createStructField("STOCK_NAME", DataTypes.StringType, false),
                            DataTypes.createStructField("STOCK_ALTERNATE_ID", DataTypes.LongType, false) }))
                    .getStructType();

            StructType stockQuotesStructType = DatasetTableSchema.rowKeyRangedSchema(
                    "STOCK_QUOTES",
                    DataTypes.createStructType(new StructField[]{
                            DataTypes.createStructField("STOCK_ID", DataTypes.LongType, false),
                            DataTypes.createStructField("RATE", DataTypes.DoubleType, false) }))
                    .getStructType();
            StructType tradesRawStructType = DatasetTableSchema.rowKeyRangedSchema(
                    "TRADES_RAW",
                    DataTypes.createStructType(new StructField[]{
                            DataTypes.createStructField("TRADE_DATE", DataTypes.LongType, false),
                            DataTypes.createStructField("STOCK_ID", DataTypes.LongType, false),
                            DataTypes.createStructField("STOCK_ALTERNATE_ID", DataTypes.LongType, false) }))
                    .getStructType();

            List<List<?>> stockNames = ImmutableList.of(
                    ImmutableList.of(1L, 10L, "APPLE", 60L),
                    ImmutableList.of(2L, 20L, "GOOGLE", 70L),
                    ImmutableList.of(3L, 30L, "FACEBOOK", 80L),
                    ImmutableList.of(4L, 40L, "EVIL_INC", 90L),
                    ImmutableList.of(5L, 50L, "GOOD_SAMARITANS", 100L));

            List<List<?>> stockQuotes = ImmutableList.of(
                    ImmutableList.of(6L, 10L, 50.1),
                    ImmutableList.of(7L, 20L, 55.12),
                    ImmutableList.of(8L, 30L, 43.41),
                    ImmutableList.of(9L, 40L, 1023992.12));

            List<List<?>> tradesRaw = ImmutableList.of(
                    ImmutableList.of(10L, 101L, 10L, 60L),
                    ImmutableList.of(11L, 102L, 20L, 70L),
                    ImmutableList.of(12L, 103L, 30L, 80L),
                    ImmutableList.of(13L, 104L, 40L, 90L),
                    ImmutableList.of(14L, 105L, 40L, 90L),
                    ImmutableList.of(15L, 106L, 40L, 90L),
                    ImmutableList.of(16L, 107L, 50L, 100L),
                    ImmutableList.of(17L, 108L, 60L, 110L));

            new DatasetFixture()
                    .createDataset(sparkSession, "STOCK_QUOTES", stockQuotesStructType, stockQuotes);
            new DatasetFixture()
                    .createDataset(sparkSession, "STOCK_NAMES", stockNameStructType, stockNames);
            new DatasetFixture()
                    .createDataset(sparkSession, "TRADES_RAW", tradesRawStructType, tradesRaw);

            pipelineJobOperator.runJob(pipelineAppConfig);

            List<Row> rows = datasetRepository.readDataFrame(DatasetTableLookup.builder()
                    .datasetName("TRADES_JOIN")
                    .predicate("1=1")
                    .build())
                    .collectAsList();

            assertThat(rows)
                    .extracting(
                            row -> row.<Long>getAs("TRADE_DATE"),
                            row -> row.<String>getAs("STOCK"),
                            row -> row.<Double>getAs("RATE"),
                            row -> row.<Long>getAs("FCR_SYS__STOCK_NAMES__ROW_KEY"),
                            row -> row.<Long>getAs("FCR_SYS__STOCK_QUOTES__ROW_KEY"),
                            row -> row.<Long>getAs("FCR_SYS__TRADES_RAW__ROW_KEY"))
                    .containsExactlyInAnyOrder(
                            tuple(101L, "APPLE", 50.1, 1L, 6L, 10L),
                            tuple(102L, "GOOGLE", 55.12, 2L, 7L, 11L),
                            tuple(103L, "FACEBOOK", 43.41, 3L, 8L, 12L),
                            tuple(104L, "EVIL_INC", 1023992.12, 4L, 9L, 13L),
                            tuple(105L, "EVIL_INC", 1023992.12, 4L, 9L, 14L),
                            tuple(106L, "EVIL_INC", 1023992.12, 4L, 9L, 15L),
                            //should be null due to full outer join
                            tuple(107L, "GOOD_SAMARITANS", null, 5L, null, 16L)
                            //108 is missing due to inner join
                    );

            CreateDatasetCall createDatasetCall = internalDatasetClient.getCachedDatasetCall();

            assertThat(createDatasetCall.getRecordsCount())
                    .isEqualTo(7L);
        }
    }

    @RunWith(SpringRunner.class)
    @SpringBootTest(classes = { TestPipelineApplication.class })
    public static class WindowTransformationTest {

        @Rule
        public JUnitSoftAssertions soft = new JUnitSoftAssertions();

        @Autowired
        private PipelineJobOperator pipelineJobOperator;

        @Autowired
        private DatasetRepository datasetRepository;

        @Autowired
        private SparkSession sparkSession;

        @Before
        public void setUp() throws Exception {
            sparkSession.sqlContext().dropTempTable("THE_FELLOWSHIP");

            StructType employeesStructType = DatasetTableSchema.rowKeyRangedSchema(
                    "THE_FELLOWSHIP",
                    DataTypes.createStructType(new StructField[]{
                            DataTypes.createStructField("Name", DataTypes.StringType, false),
                            DataTypes.createStructField("Race", DataTypes.StringType, false),
                            DataTypes.createStructField("Salary", DataTypes.IntegerType, false),
                            DataTypes.createStructField("Rating", DataTypes.IntegerType, false)
                    }))
                    .getStructType();

            List<List<?>> rowValues = ImmutableList.of(
                    ImmutableList.of(1L, "Frodo", "Hobbit", 50000, 1),
                    ImmutableList.of(2L, "Sam", "Hobbit", 29000, 1),
                    ImmutableList.of(3L, "Merry", "Hobbit", 29000, 3),
                    ImmutableList.of(4L, "Pippin", "Hobbit", 25000, 4),
                    ImmutableList.of(5L, "Aragorn", "Man", 40000, 1),
                    ImmutableList.of(6L, "Gandalf", "Wizard", 35000, 1),
                    ImmutableList.of(7L, "Boromir", "Man", 35000, 2),
                    ImmutableList.of(8L, "Gimli", "Dwarf", 19500, 2),
                    ImmutableList.of(9L, "Legolas", "Elf", 23500, 2),
                    ImmutableList.of(10L, "Elrond", "Elf", 0, 1)
            );

            new DatasetFixture()
                    .createDataset(sparkSession, "THE_FELLOWSHIP", employeesStructType, rowValues);
        }

        @Test
        public void runJob_SingleWindowStepWithRankingFunction() {
            sparkSession.sqlContext().dropTempTable("THE_FELLOWSHIP_SALARIES");

            WindowStepAppConfig windowStep = WindowStepAppConfig.builder()
                    .pipelineStepInvocationId(132432L)
                    .pipelineStepDatasetLookup(datasetTableLookup(DatasetTableLookup.builder()
                            .datasetName("THE_FELLOWSHIP")
                            .predicate("ROW_KEY >= 1 AND ROW_KEY <= 9")
                            .rowKeySeed(1L)
                            .build()))
                    .selects(newHashSet(
                            SelectColumn.builder().select("Name").as("Name").build(),
                            SelectColumn.builder().select("Race").as("Race").build(),
                            SelectColumn.builder().select("Salary").as("Salary").build(),
                            SelectColumn.builder()
                                    .select("rank()")
                                    .over(WindowSpec.builder()
                                            .partitionBy(singleton("Race"))
                                            .orderBy(singletonList(column("Salary", DESC)))
                                            .build())
                                    .as("Rank")
                                    .build(),
                            SelectColumn.builder()
                                    .select("dense_rank()")
                                    .over(WindowSpec.builder()
                                            .partitionBy(singleton("Race"))
                                            .orderBy(singletonList(column("Salary", DESC)))
                                            .build())
                                    .as("DenseRank")
                                    .build()))
                    .outputDataset(Populated.stagingDatasetConfig()
                            .id(123123L)
                            .stagingSchemaValidation(Populated.stagingSchemaValidation()
                                    .physicalTableName("THE_FELLOWSHIP_SALARIES")
                                    .build())
                            .build())
                    .build();

            pipelineJobOperator.runJob(PipelineAppConfig.builder()
                    .name("test window functions")
                    .serviceRequestId(1)
                    .pipelineInvocationId(2135153L)
                    .pipelineSteps(singletonList(windowStep))
                    .sparkFunctionConfig(SparkFunctionConfig.builder().build())
                    .build());

            Dataset<Row> createdDataset = sparkSession.sql("SELECT * FROM THE_FELLOWSHIP_SALARIES");

            assertThat(createdDataset.collectAsList())
                    .extracting(row -> tuple(
                            row.<Long>getAs("FCR_SYS__THE_FELLOWSHIP__ROW_KEY"),
                            row.<String>getAs("Name"),
                            row.<String>getAs("Race"),
                            row.<Integer>getAs("Salary"),
                            row.<Integer>getAs("Rank"),
                            row.<Integer>getAs("DenseRank"),
                            row.<String>getAs("FCR_SYS__THE_FELLOWSHIP__Race")))
                    .containsExactlyInAnyOrder(
                            tuple(1L, "Frodo", "Hobbit", 50000, 1, 1, "Hobbit"),
                            tuple(2L, "Sam", "Hobbit", 29000, 2, 2, "Hobbit"),
                            tuple(3L, "Merry", "Hobbit", 29000, 2, 2, "Hobbit"),
                            tuple(4L, "Pippin", "Hobbit", 25000, 4, 3, "Hobbit"),
                            tuple(5L, "Aragorn", "Man", 40000, 1, 1, "Man"),
                            tuple(7L, "Boromir", "Man", 35000, 2, 2, "Man"),
                            tuple(6L, "Gandalf", "Wizard", 35000, 1, 1, "Wizard"),
                            tuple(8L, "Gimli", "Dwarf", 19500, 1, 1, "Dwarf"),
                            tuple(9L, "Legolas", "Elf", 23500, 1, 1, "Elf"));
        }

        @Test
        public void runJob_SingleWindowStepWithAggregateFunction() {
            sparkSession.sqlContext().dropTempTable("THE_FELLOWSHIP_SALARIES");

            WindowStepAppConfig windowStep = WindowStepAppConfig.builder()
                    .pipelineStepInvocationId(132432L)
                    .pipelineStepDatasetLookup(datasetTableLookup(DatasetTableLookup.builder()
                            .datasetName("THE_FELLOWSHIP")
                            .predicate("ROW_KEY >= 1 AND ROW_KEY <= 9")
                            .rowKeySeed(1L)
                            .build()))
                    .selects(newHashSet(
                            SelectColumn.builder().select("Name").as("Name").build(),
                            SelectColumn.builder().select("Race").as("Race").build(),
                            SelectColumn.builder().select("Salary").as("Salary").build(),
                            SelectColumn.builder()
                                    .select("avg(Salary)")
                                    .over(WindowSpec.builder().partitionBy(singleton("Race")).build())
                                    .as("AverageSalary")
                                    .build()))
                    .outputDataset(Populated.stagingDatasetConfig()
                            .id(123123L)
                            .stagingSchemaValidation(Populated.stagingSchemaValidation()
                                    .physicalTableName("THE_FELLOWSHIP_SALARIES")
                                    .build())
                            .build())
                    .build();

            pipelineJobOperator.runJob(PipelineAppConfig.builder()
                    .name("test window functions")
                    .serviceRequestId(1)
                    .pipelineInvocationId(2135153L)
                    .pipelineSteps(singletonList(windowStep))
                    .sparkFunctionConfig(SparkFunctionConfig.builder().build())
                    .build());

            Dataset<Row> createdDataset = sparkSession.sql("SELECT * FROM THE_FELLOWSHIP_SALARIES");

            assertThat(createdDataset.collectAsList())
                    .extracting(row -> tuple(
                            row.<Long>getAs("FCR_SYS__THE_FELLOWSHIP__ROW_KEY"),
                            row.<String>getAs("Name"),
                            row.<String>getAs("Race"),
                            row.<Integer>getAs("Salary"),
                            row.<Double>getAs("AverageSalary"),
                            row.<String>getAs("FCR_SYS__THE_FELLOWSHIP__Race")))
                    .containsExactlyInAnyOrder(
                            tuple(1L, "Frodo", "Hobbit", 50000, 33250.0, "Hobbit"),
                            tuple(2L, "Sam", "Hobbit", 29000, 33250.0, "Hobbit"),
                            tuple(3L, "Merry", "Hobbit", 29000, 33250.0, "Hobbit"),
                            tuple(4L, "Pippin", "Hobbit", 25000, 33250.0, "Hobbit"),
                            tuple(5L, "Aragorn", "Man", 40000, 37500.0, "Man"),
                            tuple(7L, "Boromir", "Man", 35000, 37500.0, "Man"),
                            tuple(6L, "Gandalf", "Wizard", 35000, 35000.0, "Wizard"),
                            tuple(8L, "Gimli", "Dwarf", 19500, 19500.0, "Dwarf"),
                            tuple(9L, "Legolas", "Elf", 23500, 23500.0, "Elf"));
        }

        @Test
        public void runJob_SingleWindowStepWithNoPartitionsOrOrdering() throws Exception {
            sparkSession.sqlContext().dropTempTable("SOME_INPUT_TABLE");
            sparkSession.sqlContext().dropTempTable("SOME_OUTPUT_TABLE");


            StructType structType = DatasetTableSchema.rowKeyRangedSchema(
                    "SOME_INPUT_TABLE",
                    DataTypes.createStructType(new StructField[]{
                            DataTypes.createStructField("FieldA", DataTypes.StringType, false),
                            DataTypes.createStructField("FieldB", DataTypes.StringType, false)
                    }))
                    .getStructType();

            List<List<?>> rowValues = ImmutableList.of(
                    Arrays.asList(1L, "A1", "B1"),
                    Arrays.asList(2L, "A2", "B2"),
                    Arrays.asList(3L, "A3", "B3"),
                    Arrays.asList(4L, "A4", "B4"),
                    Arrays.asList(5L, "A5", "B5"),
                    Arrays.asList(7L, "A6", "B6"));

            new DatasetFixture()
                    .createDataset(sparkSession, "SOME_INPUT_TABLE", structType, rowValues);

            WindowStepAppConfig windowStep = WindowStepAppConfig.builder()
                    .pipelineStepInvocationId(132432L)
                    .pipelineStepDatasetLookup(datasetTableLookup(DatasetTableLookup.builder()
                            .datasetName("SOME_INPUT_TABLE")
                            .predicate("ROW_KEY >= 1 AND ROW_KEY <= 7")
                            .rowKeySeed(1L)
                            .build()))
                    .selects(newHashSet(
                            SelectColumn.builder().select("FieldA").as("FieldA").build(),
                            SelectColumn.builder().select("FieldB").as("FieldB").build(),
                            SelectColumn.builder()
                                    .select("first_value(FieldB)")
                                    .over(WindowSpec.builder()
                                            .partitionBy(emptySet())
                                            .orderBy(emptyList())
                                            .build())
                                    .as("FieldC")
                                    .build()))
                    .outputDataset(Populated.stagingDatasetConfig()
                            .id(123123L)
                            .stagingSchemaValidation(Populated.stagingSchemaValidation()
                                    .physicalTableName("SOME_OUTPUT_TABLE")
                                    .build())
                            .build())
                    .build();

            pipelineJobOperator.runJob(PipelineAppConfig.builder()
                    .name("test window functions")
                    .serviceRequestId(1)
                    .pipelineInvocationId(2135153L)
                    .pipelineSteps(singletonList(windowStep))
                    .sparkFunctionConfig(SparkFunctionConfig.builder().build())
                    .build());

            Dataset<Row> createdDataset = sparkSession.sql("SELECT * FROM SOME_OUTPUT_TABLE");

            assertThat(createdDataset.collectAsList())
                    .extracting(row -> tuple(
                            row.<Long>getAs("ROW_KEY"),
                            row.<String>getAs("FieldA"),
                            row.<String>getAs("FieldB"),
                            row.<Integer>getAs("FieldC"),
                            row.<Integer>getAs("FCR_SYS__SOME_INPUT_TABLE__ROW_KEY")))
                    .containsExactlyInAnyOrder(
                            tuple(4294967296L, "A1", "B1", "B1", 1L),
                            tuple(4294967297L, "A2", "B2", "B1", 2L),
                            tuple(4294967298L, "A3", "B3", "B1", 3L),
                            tuple(4294967299L, "A4", "B4", "B1", 4L),
                            tuple(4294967300L, "A5", "B5", "B1", 5L),
                            tuple(4294967301L, "A6", "B6", "B1", 7L));
        }
    }

    @RunWith(SpringRunner.class)
    @SpringBootTest(classes = { TestPipelineApplication.class, })
    public static class MultipleStepsTest {

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

        @SuppressWarnings("unchecked")
        @Test
        public void runJob_PipelineWithMultipleSteps() throws Exception {
            sparkSession.sqlContext().dropTempTable("EMPLOYEES");
            sparkSession.sqlContext().dropTempTable("DEPARTMENTS");

            StructType employeesStructType = DatasetTableSchema.rowKeyRangedSchema(
                    "EMPLOYEES",
                    DataTypes.createStructType(new StructField[]{
                            DataTypes.createStructField("FirstName", DataTypes.StringType, false),
                            DataTypes.createStructField("LastName", DataTypes.StringType, false),
                            DataTypes.createStructField("Salary", DataTypes.LongType, false),
                            DataTypes.createStructField("DepartmentCode", DataTypes.StringType, false) }))
                    .getStructType();

            StructType departmentStructType = DatasetTableSchema.rowKeyRangedSchema(
                    "DEPARTMENTS",
                    DataTypes.createStructType(new StructField[]{
                            DataTypes.createStructField("DepartmentName", DataTypes.StringType, false),
                            DataTypes.createStructField("DepartmentCode", DataTypes.StringType, false) }))
                    .getStructType();

            List<List<?>> employees = ImmutableList.of(
                    ImmutableList.of(1L, "Homer", "Simpson", 10000L, "DEPT1"),
                    ImmutableList.of(2L, "Lenny", "Leonard", 15000L, "DEPT1"),
                    ImmutableList.of(3L, "Carl", "Carlson", 19500L, "DEPT1"),
                    ImmutableList.of(4L, "Wayland", "Smithers", 20000L, "DEPT2"),
                    ImmutableList.of(5L, "Montgomery", "Burns", 99999999L, "DEPT3"),
                    ImmutableList.of(999L, "Another", "Dataset", 123456L, "DEPTZ"));

            List<List<?>> departments = ImmutableList.of(
                    ImmutableList.of(1L, "Safety", "DEPT1"),
                    ImmutableList.of(2L, "PA", "DEPT2"),
                    ImmutableList.of(3L, "Management", "DEPT3"),
                    ImmutableList.of(999L, "ZZZ", "DEPTZ"));

            new DatasetFixture().createDataset(sparkSession, "EMPLOYEES", employeesStructType, employees);
            new DatasetFixture().createDataset(sparkSession, "DEPARTMENTS", departmentStructType, departments);

            JoinStepAppConfig joinStep = JoinStepAppConfig.builder()
                    .pipelineStepInvocationId(1L)
                    .selects(newLinkedHashSet(newHashSet(
                            SelectColumn.builder().select("EMPLOYEES.FirstName").as("FirstName").build(),
                            SelectColumn.builder().select("EMPLOYEES.LastName").as("LastName").build(),
                            SelectColumn.builder().select("EMPLOYEES.Salary").as("Salary").build(),
                            SelectColumn.builder().select("DEPARTMENTS.DepartmentName").as("DepartmentName").build(),
                            SelectColumn.builder().select("DEPARTMENTS.DepartmentCode").as("DepartmentCode").build())))
                    .joinAppConfigs(newHashSet(
                            JoinAppConfig.builder()
                                    .joinType(JoinAppConfig.JoinType.FULL_OUTER)
                                    .leftSchemaName("EMPLOYEES")
                                    .rightSchemaName("DEPARTMENTS")
                                    .joinFields(asList(JoinFieldConfig.builder()
                                            .leftJoinField("DepartmentCode")
                                            .rightJoinField("DepartmentCode")
                                            .build()))
                                    .build()))
                    .outputDataset(
                            Populated.stagingDatasetConfig()
                                    .stagingSchemaValidation(Populated.stagingSchemaValidation()
                                            .displayName("Employee To Departments")
                                            .physicalTableName("EMPLOYEE_DEPARTMENT")
                                            .build())
                                    .build()).pipelineStepDatasetLookups(asList(
                            datasetTableLookup(Populated.datasetLookup()
                                    .datasetName("EMPLOYEES")
                                    .predicate("ROW_KEY >= 1 AND ROW_KEY <= 5")
                                    .build()),
                            datasetTableLookup(Populated.datasetLookup()
                                    .datasetName("DEPARTMENTS")
                                    .predicate("ROW_KEY >= 1 AND ROW_KEY <=3")
                                    .build())))
                    .build();

            MapStepAppConfig mapStep = MapStepAppConfig.builder()
                    .pipelineStepInvocationId(2L)
                    .pipelineStepDatasetLookup(PipelineStepDatasetLookup.pipelineStepInvocationId(1L))
                    .selects(newHashSet(
                            SelectColumn.builder().select("CONCAT_WS(' ', FirstName, LastName)").as("FullName").build(),
                            SelectColumn.builder().select("Salary").as("Salary").build(),
                            SelectColumn.builder().select("DepartmentName").as("DepartmentName").build()))
                    .outputDataset(
                            Populated.stagingDatasetConfig()
                                    .id(12312L)
                                    .stagingSchemaValidation(Populated.stagingSchemaValidation()
                                            .displayName("Mapped Employees")
                                            .physicalTableName("MAPPED_EMPLOYEES")
                                            .build())
                                    .build())
                    .build();

            AggregateStepAppConfig aggregationStep = AggregateStepAppConfig.builder()
                    .pipelineStepInvocationId(3L)
                    .pipelineStepDatasetLookup(PipelineStepDatasetLookup.pipelineStepInvocationId(2L))
                    .selects(newLinkedHashSet(newHashSet(
                            SelectColumn.builder().select("DepartmentName").as("Department").build(),
                            SelectColumn.builder().select("SUM(Salary)").as("TotalSalary").build(),
                            SelectColumn.builder().select("ROUND(AVG(Salary), 3)").as("AverageSalary").build(),
                            SelectColumn.builder().select("COUNT(*)").as("TotalEmployees").build())))
                    .groupings(newHashSet("DepartmentName"))
                    .outputDataset(
                            Populated.stagingDatasetConfig()
                                    .id(12312L)
                                    .stagingSchemaValidation(Populated.stagingSchemaValidation()
                                            .displayName("Department Salary")
                                            .physicalTableName("DEPARTMENT_SALARY")
                                            .build())
                                    .build())
                    .build();

            PipelineAppConfig pipelineAppConfig = PipelineAppConfig.builder()
                    .name("First pipeline run")
                    .serviceRequestId(1)
                    .pipelineInvocationId(8292L)
                    .pipelineSteps(asList(joinStep, mapStep, aggregationStep))
                    .sparkFunctionConfig(SparkFunctionConfig.builder().build())
                    .build();

            pipelineJobOperator.runJob(pipelineAppConfig);

            Dataset<Row> joinedDataset = datasetRepository.readDataFrame(DatasetTableLookup.builder()
                    .datasetName("EMPLOYEE_DEPARTMENT")
                    .predicate("1=1")
                    .build());

            Dataset<Row> mappedDataset = datasetRepository.readDataFrame(DatasetTableLookup.builder()
                    .datasetName("MAPPED_EMPLOYEES")
                    .predicate("1=1")
                    .build());

            Dataset<Row> aggregatedDataset = datasetRepository.readDataFrame(DatasetTableLookup.builder()
                    .datasetName("DEPARTMENT_SALARY")
                    .predicate("1=1")
                    .build());

            soft.assertThat(joinedDataset.collectAsList())
                    .extracting(
                            row -> row.<String>getAs("FirstName"),
                            row -> row.<String>getAs("LastName"),
                            row -> row.<Long>getAs("Salary"),
                            row -> row.<String>getAs("DepartmentName"),
                            row -> row.<String>getAs("DepartmentCode"))
                    .containsExactlyInAnyOrder(
                            tuple("Homer", "Simpson", 10000L, "Safety", "DEPT1"),
                            tuple("Lenny", "Leonard", 15000L, "Safety", "DEPT1"),
                            tuple("Carl", "Carlson", 19500L, "Safety", "DEPT1"),
                            tuple("Wayland", "Smithers", 20000L, "PA", "DEPT2"),
                            tuple("Montgomery", "Burns", 99999999L, "Management", "DEPT3"));

            soft.assertThat(mappedDataset.collectAsList())
                    .extracting(
                            row -> row.<String>getAs("FullName"),
                            row -> row.<Long>getAs("Salary"),
                            row -> row.<String>getAs("DepartmentName"))
                    .containsExactlyInAnyOrder(
                            tuple("Homer Simpson", 10000L, "Safety"),
                            tuple("Lenny Leonard", 15000L, "Safety"),
                            tuple("Carl Carlson", 19500L, "Safety"),
                            tuple("Wayland Smithers", 20000L, "PA"),
                            tuple("Montgomery Burns", 99999999L, "Management"));

            soft.assertThat(aggregatedDataset.collectAsList())
                    .extracting(
                            row -> row.<String>getAs("Department"),
                            row -> row.<Long>getAs("TotalSalary"),
                            row -> row.<Double>getAs("AverageSalary"),
                            row -> row.<Long>getAs("TotalEmployees"))
                    .containsExactlyInAnyOrder(
                            tuple("Safety", 44500L, 14833.333D, 3L),
                            tuple("PA", 20000L, 20000D, 1L),
                            tuple("Management", 99999999L, 99999999D, 1L));

            soft.assertThat(pipelineStatusClient.getRequests())
                    .containsOnlyKeys(8292L);

            soft.assertThat(pipelineStatusClient.getRequestsByPipelineInvocationId(8292L))
                    .containsExactly(
                            new UpdatePipelineStepStatusRequest(1L, PipelineStepStatus.RUNNING),
                            new UpdatePipelineStepStatusRequest(1L, PipelineStepStatus.SUCCESS),
                            new UpdatePipelineStepStatusRequest(2L, PipelineStepStatus.RUNNING),
                            new UpdatePipelineStepStatusRequest(2L, PipelineStepStatus.SUCCESS),
                            new UpdatePipelineStepStatusRequest(3L, PipelineStepStatus.RUNNING),
                            new UpdatePipelineStepStatusRequest(3L, PipelineStepStatus.SUCCESS));
        }

        @Test
        public void runJob_PipelineStepFails_UpdatesPipelineStepStatus() throws Exception {
            sparkSession.sqlContext().dropTempTable("EMPLOYEES");
            sparkSession.sqlContext().dropTempTable("DEPARTMENTS");

            StructType employeesStructType = DatasetTableSchema.rowKeyRangedSchema(
                    "EMPLOYEES",
                    DataTypes.createStructType(new StructField[]{
                            DataTypes.createStructField("FirstName", DataTypes.StringType, false),
                            DataTypes.createStructField("LastName", DataTypes.StringType, false),
                            DataTypes.createStructField("Salary", DataTypes.LongType, false),
                            DataTypes.createStructField("DepartmentCode", DataTypes.StringType, false) }))
                    .getStructType();

            StructType departmentStructType = DatasetTableSchema.rowKeyRangedSchema(
                    "DEPARTMENTS",
                    DataTypes.createStructType(new StructField[]{
                            DataTypes.createStructField("DepartmentName", DataTypes.StringType, false),
                            DataTypes.createStructField("DepartmentCode", DataTypes.StringType, false) }))
                    .getStructType();

            List<List<?>> employees = ImmutableList.of(
                    ImmutableList.of(1L, "Homer", "Simpson", 10000L, "DEPT1"),
                    ImmutableList.of(2L, "Lenny", "Leonard", 15000L, "DEPT1"),
                    ImmutableList.of(3L, "Carl", "Carlson", 19500L, "DEPT1"),
                    ImmutableList.of(4L, "Wayland", "Smithers", 20000L, "DEPT2"),
                    ImmutableList.of(5L, "Montgomery", "Burns", 99999999L, "DEPT3"),
                    ImmutableList.of(999L, "Another", "Dataset", 123456L, "DEPTZ"));

            List<List<?>> departments = ImmutableList.of(
                    ImmutableList.of(1L, "Safety", "DEPT1"),
                    ImmutableList.of(2L, "PA", "DEPT2"),
                    ImmutableList.of(3L, "Management", "DEPT3"),
                    ImmutableList.of(999L, "ZZZ", "DEPTZ"));

            new DatasetFixture().createDataset(sparkSession, "EMPLOYEES", employeesStructType, employees);
            new DatasetFixture().createDataset(sparkSession, "DEPARTMENTS", departmentStructType, departments);

            JoinStepAppConfig joinStep = JoinStepAppConfig.builder()
                    .pipelineStepInvocationId(1111L)
                    .selects(newLinkedHashSet(newHashSet(
                            SelectColumn.builder().select("EMPLOYEES.FirstName").as("FirstName").build(),
                            SelectColumn.builder().select("EMPLOYEES.LastName").as("LastName").build(),
                            SelectColumn.builder().select("EMPLOYEES.Salary").as("Salary").build(),
                            SelectColumn.builder().select("DEPARTMENTS.DepartmentName").as("DepartmentName").build(),
                            SelectColumn.builder().select("DEPARTMENTS.DepartmentCode").as("DepartmentCode").build())))
                    .joinAppConfigs(newHashSet(
                            JoinAppConfig.builder()
                                    .joinType(JoinAppConfig.JoinType.FULL_OUTER)
                                    .leftSchemaName("EMPLOYEES")
                                    .rightSchemaName("DEPARTMENTS")
                                    .joinFields(asList(JoinFieldConfig.builder()
                                            .leftJoinField("DepartmentCode")
                                            .rightJoinField("DepartmentCode")
                                            .build()))
                                    .build()))
                    .outputDataset(
                            Populated.stagingDatasetConfig()
                                    .stagingSchemaValidation(Populated.stagingSchemaValidation()
                                            .displayName("Employee To Departments")
                                            .physicalTableName("EMPLOYEE_DEPARTMENT")
                                            .build())
                                    .build()).pipelineStepDatasetLookups(asList(
                            datasetTableLookup(Populated.datasetLookup()
                                    .datasetName("EMPLOYEES")
                                    .predicate("ROW_KEY >= 1 AND ROW_KEY <= 5")
                                    .build()),
                            datasetTableLookup(Populated.datasetLookup()
                                    .datasetName("DEPARTMENTS")
                                    .predicate("ROW_KEY >= 1 AND ROW_KEY <=3")
                                    .build())))
                    .build();

            MapStepAppConfig mapStep = MapStepAppConfig.builder()
                    .pipelineStepInvocationId(2222L)
                    .pipelineStepDatasetLookup(PipelineStepDatasetLookup.pipelineStepInvocationId(1111L))
                    .selects(newHashSet(
                            SelectColumn.builder().select("this is not valid").build(),
                            SelectColumn.builder().select("Salary").build(),
                            SelectColumn.builder().select("DepartmentName").build()))
                    .outputDataset(
                            Populated.stagingDatasetConfig()
                                    .id(12312L)
                                    .stagingSchemaValidation(Populated.stagingSchemaValidation()
                                            .displayName("Mapped Employees")
                                            .physicalTableName("MAPPED_EMPLOYEES")
                                            .build())
                                    .build())
                    .build();

            AggregateStepAppConfig aggregationStep = AggregateStepAppConfig.builder()
                    .pipelineStepInvocationId(3333L)
                    .pipelineStepDatasetLookup(PipelineStepDatasetLookup.pipelineStepInvocationId(2222L))
                    .selects(newLinkedHashSet(newHashSet(
                            SelectColumn.builder().select("DepartmentName").as("Department").build(),
                            SelectColumn.builder().select("SUM(Salary)").as("TotalSalary").build(),
                            SelectColumn.builder().select("ROUND(AVG(Salary), 3)").as("AverageSalary").build(),
                            SelectColumn.builder().select("COUNT(*)").as("TotalEmployees").build())))
                    .groupings(newHashSet("DepartmentName"))
                    .outputDataset(
                            Populated.stagingDatasetConfig()
                                    .id(12312L)
                                    .stagingSchemaValidation(Populated.stagingSchemaValidation()
                                            .displayName("Department Salary")
                                            .physicalTableName("DEPARTMENT_SALARY")
                                            .build())
                                    .build())
                    .build();

            PipelineAppConfig pipelineAppConfig = PipelineAppConfig.builder()
                    .name("First pipeline run")
                    .serviceRequestId(1)
                    .pipelineInvocationId(436743L)
                    .pipelineSteps(asList(joinStep, mapStep, aggregationStep))
                    .sparkFunctionConfig(SparkFunctionConfig.builder().build())
                    .build();

            assertThatThrownBy(() -> pipelineJobOperator.runJob(pipelineAppConfig))
                    .isInstanceOf(PipelineJobOperatorException.class)
                    .hasMessageContaining("this is not valid");

            soft.assertThat(pipelineStatusClient.getRequests())
                    .containsOnlyKeys(436743L);

            soft.assertThat(pipelineStatusClient.getRequestsByPipelineInvocationId(436743L))
                    .containsExactly(
                            new UpdatePipelineStepStatusRequest(1111L, PipelineStepStatus.RUNNING),
                            new UpdatePipelineStepStatusRequest(1111L, PipelineStepStatus.SUCCESS),
                            new UpdatePipelineStepStatusRequest(2222L, PipelineStepStatus.RUNNING),
                            new UpdatePipelineStepStatusRequest(2222L, PipelineStepStatus.FAILED));
        }
    }
}
