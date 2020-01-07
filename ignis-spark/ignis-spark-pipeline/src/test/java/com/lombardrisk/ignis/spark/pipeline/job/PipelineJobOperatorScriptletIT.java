package com.lombardrisk.ignis.spark.pipeline.job;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.lombardrisk.ignis.spark.api.DatasetTableLookup;
import com.lombardrisk.ignis.spark.api.fixture.Populated;
import com.lombardrisk.ignis.spark.api.pipeline.PipelineAppConfig;
import com.lombardrisk.ignis.spark.api.pipeline.ScriptletStepAppConfig;
import com.lombardrisk.ignis.spark.api.pipeline.spark.SparkFunctionConfig;
import com.lombardrisk.ignis.spark.core.fixture.DatasetFixture;
import com.lombardrisk.ignis.spark.core.mock.StatefulDatasetClientStore;
import com.lombardrisk.ignis.spark.core.repository.DatasetRepository;
import com.lombardrisk.ignis.spark.core.schema.DatasetTableSchema;
import com.lombardrisk.ignis.spark.pipeline.TestPipelineApplication;
import com.lombardrisk.ignis.spark.pipeline.mock.CannotInstantiateScriptlet;
import com.lombardrisk.ignis.spark.pipeline.mock.MockPipelineStatusClient;
import com.lombardrisk.ignis.spark.pipeline.mock.ScriptletWithMultipleInputs;
import com.lombardrisk.ignis.spark.script.demo.AggregateUsersScriptlet;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
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

import static com.lombardrisk.ignis.spark.api.pipeline.PipelineStepDatasetLookup.datasetTableLookup;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { TestPipelineApplication.class, })
public class PipelineJobOperatorScriptletIT {

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
    public void runJob_SingleScriptletStep() throws Exception {
        sparkSession.sqlContext().dropTempTable("TABLE_OF_USERS");
        sparkSession.sqlContext().dropTempTable("USERS_BY_REGION");

        StructType usersStructType = DatasetTableSchema.rowKeyRangedSchema(
                "TABLE_OF_USERS", AggregateUsersScriptlet.USERS_STRUCT_TYPE).getStructType();

        List<List<?>> usersRow = ImmutableList.of(
                ImmutableList.of(1L, "Matt", "UK"),
                ImmutableList.of(2L, "Mohammed", "Tunis"),
                ImmutableList.of(3L, "Hassen", "Tunis"));

        new DatasetFixture()
                .createDataset(sparkSession, "TABLE_OF_USERS", usersStructType, usersRow);

        ScriptletStepAppConfig scriptletStepConfig = ScriptletStepAppConfig.builder()
                .pipelineStepInvocationId(8585L)
                .className(AggregateUsersScriptlet.class.getName())
                .inputSchemaMappings(ImmutableMap.of("Users", "TABLE_OF_USERS"))
                .pipelineStepDatasetLookups(singletonList(
                        datasetTableLookup(DatasetTableLookup.builder()
                                .datasetName("TABLE_OF_USERS")
                                .predicate("1=1")
                                .build())))
                .outputDataset(
                        Populated.stagingDatasetConfig()
                                .id(12312L)
                                .stagingSchemaValidation(Populated.stagingSchemaValidation()
                                        .displayName("Users By Region")
                                        .physicalTableName("USERS_BY_REGION")
                                        .build())
                                .build())
                .build();

        PipelineAppConfig pipelineAppConfig = PipelineAppConfig.builder()
                .name("test staging")
                .serviceRequestId(1)
                .pipelineInvocationId(8292L)
                .pipelineSteps(singletonList(scriptletStepConfig))
                .sparkFunctionConfig(SparkFunctionConfig.builder()
                        .referenceDate(LocalDate.of(2019, 1, 1))
                        .build())
                .build();

        pipelineJobOperator.runJob(pipelineAppConfig);

        Dataset<Row> createdDataset = datasetRepository.readDataFrame(DatasetTableLookup.builder()
                .datasetName("USERS_BY_REGION")
                .predicate("1=1")
                .build());

        List<Row> rows = createdDataset.collectAsList();
        assertThat(rows)
                .extracting(
                        row -> row.getAs("Region"),
                        row -> row.getAs("UsersInRegion"))
                .containsExactlyInAnyOrder(
                        tuple("UK", 1L),
                        tuple("Tunis", 2L));
    }

    @Test
    public void runJob_ClassNotFound_ThrowsException() {
        ScriptletStepAppConfig scriptletStepConfig = ScriptletStepAppConfig.builder()
                .pipelineStepInvocationId(8585L)
                .className("this.does.not.exist.Anywhere")
                .pipelineStepDatasetLookups(emptyList())
                .outputDataset(Populated.stagingDatasetConfig()
                        .stagingSchemaValidation(Populated.stagingSchemaValidation().build())
                        .build())
                .build();

        PipelineAppConfig pipelineAppConfig = PipelineAppConfig.builder()
                .name("test staging")
                .serviceRequestId(1)
                .pipelineInvocationId(8292L)
                .pipelineSteps(singletonList(scriptletStepConfig))
                .sparkFunctionConfig(SparkFunctionConfig.builder()
                        .referenceDate(LocalDate.of(2019, 1, 1))
                        .build())
                .build();

        assertThatThrownBy(() -> pipelineJobOperator.runJob(pipelineAppConfig))
                .isInstanceOf(PipelineJobOperatorException.class)
                .hasRootCauseInstanceOf(ClassNotFoundException.class)
                .hasStackTraceContaining("Caused by: java.lang.ClassNotFoundException: this.does.not.exist.Anywhere");
    }

    @Test
    public void runJob_ClassDoesNotImplementScriptlet_ThrowsException() {
        ScriptletStepAppConfig scriptletStepConfig = ScriptletStepAppConfig.builder()
                .pipelineStepInvocationId(8585L)
                .className(String.class.getName())
                .pipelineStepDatasetLookups(emptyList())
                .outputDataset(Populated.stagingDatasetConfig()
                        .stagingSchemaValidation(Populated.stagingSchemaValidation().build())
                        .build())
                .build();

        PipelineAppConfig pipelineAppConfig = PipelineAppConfig.builder()
                .name("test staging")
                .serviceRequestId(1)
                .pipelineInvocationId(8292L)
                .pipelineSteps(singletonList(scriptletStepConfig))
                .sparkFunctionConfig(SparkFunctionConfig.builder()
                        .referenceDate(LocalDate.of(2019, 1, 1))
                        .build())
                .build();

        assertThatThrownBy(() -> pipelineJobOperator.runJob(pipelineAppConfig))
                .isInstanceOf(PipelineJobOperatorException.class)
                .hasRootCauseInstanceOf(IllegalArgumentException.class)
                .hasStackTraceContaining("java.lang.IllegalArgumentException: java.lang.String is not a Scriptlet");
    }

    @Test
    public void runJob_ClassCannotBeInstantiated_ThrowsException() {
        ScriptletStepAppConfig scriptletStepConfig = ScriptletStepAppConfig.builder()
                .pipelineStepInvocationId(8585L)
                .className(CannotInstantiateScriptlet.class.getName())
                .pipelineStepDatasetLookups(emptyList())
                .outputDataset(Populated.stagingDatasetConfig()
                        .stagingSchemaValidation(Populated.stagingSchemaValidation().build())
                        .build())
                .build();

        PipelineAppConfig pipelineAppConfig = PipelineAppConfig.builder()
                .name("test staging")
                .serviceRequestId(1)
                .pipelineInvocationId(8292L)
                .pipelineSteps(singletonList(scriptletStepConfig))
                .sparkFunctionConfig(SparkFunctionConfig.builder()
                        .referenceDate(LocalDate.of(2019, 1, 1))
                        .build())
                .build();

        assertThatThrownBy(() -> pipelineJobOperator.runJob(pipelineAppConfig))
                .isInstanceOf(PipelineJobOperatorException.class)
                .hasCauseInstanceOf(IllegalStateException.class)
                .hasStackTraceContaining("Caused by: java.lang.InstantiationException: "
                        + "com.lombardrisk.ignis.spark.pipeline.mock.CannotInstantiateScriptlet");
    }

    @Test
    public void runJob_RequiredInputTraitsNotFoundInInputSchemaMappings_ThrowsException() {
        ScriptletStepAppConfig scriptletStepConfig = ScriptletStepAppConfig.builder()
                .pipelineStepInvocationId(8585L)
                .className(ScriptletWithMultipleInputs.class.getName())
                .inputSchemaMappings(ImmutableMap.of("Employees", "ALL_EMPLOYEES"))
                .pipelineStepDatasetLookups(asList(
                        datasetTableLookup(DatasetTableLookup.builder()
                                .datasetName("ALL_EMPLOYEES")
                                .predicate("1=1")
                                .build()),
                        datasetTableLookup(DatasetTableLookup.builder()
                                .datasetName("ALL_DEPARTMENTS")
                                .predicate("1=1")
                                .build()),
                        datasetTableLookup(DatasetTableLookup.builder()
                                .datasetName("EMPLOYEE_SALARIES")
                                .predicate("1=1")
                                .build())))
                .outputDataset(
                        Populated.stagingDatasetConfig()
                                .id(12312L)
                                .stagingSchemaValidation(Populated.stagingSchemaValidation()
                                        .displayName("Employees in departments with salaries")
                                        .physicalTableName("EMPLOYEES_DEPARTMENTS_SALARIES")
                                        .build())
                                .build())
                .build();

        PipelineAppConfig pipelineAppConfig = PipelineAppConfig.builder()
                .name("test staging")
                .serviceRequestId(1)
                .pipelineInvocationId(8292L)
                .pipelineSteps(singletonList(scriptletStepConfig))
                .sparkFunctionConfig(SparkFunctionConfig.builder()
                        .referenceDate(LocalDate.of(2019, 1, 1))
                        .build())
                .build();

        assertThatThrownBy(() -> pipelineJobOperator.runJob(pipelineAppConfig))
                .isInstanceOf(PipelineJobOperatorException.class)
                .hasRootCauseInstanceOf(IllegalArgumentException.class)
                .hasStackTraceContaining("Required input trait of "
                        + "com.lombardrisk.ignis.spark.pipeline.mock.ScriptletWithMultipleInputs not found: [Departments, Salaries]");
    }

    @Test
    public void runJob_RequiredInputTraitsNotFoundInDatasetLookups_ThrowsException() {
        ScriptletStepAppConfig scriptletStepConfig = ScriptletStepAppConfig.builder()
                .pipelineStepInvocationId(8585L)
                .className(ScriptletWithMultipleInputs.class.getName())
                .inputSchemaMappings(ImmutableMap.of(
                        "Employees", "ALL_EMPLOYEES",
                        "Departments", "ALL_DEPARTMENTS",
                        "Salaries", "EMPLOYEE_SALARIES"))
                .pipelineStepDatasetLookups(singletonList(
                        datasetTableLookup(DatasetTableLookup.builder()
                                .datasetName("ALL_DEPARTMENTS")
                                .predicate("1=1")
                                .build())))
                .outputDataset(
                        Populated.stagingDatasetConfig()
                                .id(12312L)
                                .stagingSchemaValidation(Populated.stagingSchemaValidation()
                                        .displayName("Employees in departments with salaries")
                                        .physicalTableName("EMPLOYEES_DEPARTMENTS_SALARIES")
                                        .build())
                                .build())
                .build();

        PipelineAppConfig pipelineAppConfig = PipelineAppConfig.builder()
                .name("test staging")
                .serviceRequestId(1)
                .pipelineInvocationId(8292L)
                .pipelineSteps(singletonList(scriptletStepConfig))
                .sparkFunctionConfig(SparkFunctionConfig.builder()
                        .referenceDate(LocalDate.of(2019, 1, 1))
                        .build())
                .build();

        assertThatThrownBy(() -> pipelineJobOperator.runJob(pipelineAppConfig))
                .isInstanceOf(PipelineJobOperatorException.class)
                .hasRootCauseInstanceOf(IllegalArgumentException.class)
                .hasStackTraceContaining("Required input trait of "
                        + "com.lombardrisk.ignis.spark.pipeline.mock.ScriptletWithMultipleInputs not found: [Employees, Salaries]");
    }

    @Test
    public void runJob_ScriptletStepWithMultipleInputs_RunsStep() throws Exception {
        sparkSession.sqlContext().dropTempTable("ALL_EMPLOYEES");
        sparkSession.sqlContext().dropTempTable("ALL_DEPARTMENTS");
        sparkSession.sqlContext().dropTempTable("EMPLOYEE_SALARIES");
        sparkSession.sqlContext().dropTempTable("EMPLOYEES_DEPARTMENTS_SALARIES");

        StructType employeesStruct = DatasetTableSchema.rowKeyRangedSchema(
                "ALL_EMPLOYEES", ScriptletWithMultipleInputs.EMPLOYEES).getStructType();

        StructType departmentsStruct = DatasetTableSchema.rowKeyRangedSchema(
                "ALL_DEPARTMENTS", ScriptletWithMultipleInputs.DEPARTMENTS).getStructType();

        StructType salariesStruct = DatasetTableSchema.rowKeyRangedSchema(
                "EMPLOYEE_SALARIES", ScriptletWithMultipleInputs.SALARIES).getStructType();

        List<List<?>> employees = ImmutableList.of(
                ImmutableList.of(1L, 101, "Tony Stark", 111),
                ImmutableList.of(2L, 102, "Natasha Romanoff", 111),
                ImmutableList.of(3L, 103, "Nick Fury", 222));

        List<List<?>> departments = ImmutableList.of(
                ImmutableList.of(1L, 111, "Avengers"),
                ImmutableList.of(2L, 222, "Shield"));

        List<List<?>> salaries = ImmutableList.of(
                ImmutableList.of(1L, 101, 10000),
                ImmutableList.of(2L, 102, 20000),
                ImmutableList.of(3L, 103, 30000));

        DatasetFixture datasetFixture = new DatasetFixture();
        datasetFixture.createDataset(sparkSession, "ALL_EMPLOYEES", employeesStruct, employees);
        datasetFixture.createDataset(sparkSession, "ALL_DEPARTMENTS", departmentsStruct, departments);
        datasetFixture.createDataset(sparkSession, "EMPLOYEE_SALARIES", salariesStruct, salaries);

        ScriptletStepAppConfig scriptletStepConfig = ScriptletStepAppConfig.builder()
                .pipelineStepInvocationId(8585L)
                .className(ScriptletWithMultipleInputs.class.getName())
                .inputSchemaMappings(ImmutableMap.of(
                        "Employees", "ALL_EMPLOYEES",
                        "Departments", "ALL_DEPARTMENTS",
                        "Salaries", "EMPLOYEE_SALARIES"))
                .pipelineStepDatasetLookups(asList(
                        datasetTableLookup(DatasetTableLookup.builder()
                                .datasetName("ALL_EMPLOYEES")
                                .predicate("1=1")
                                .build()),
                        datasetTableLookup(DatasetTableLookup.builder()
                                .datasetName("ALL_DEPARTMENTS")
                                .predicate("1=1")
                                .build()),
                        datasetTableLookup(DatasetTableLookup.builder()
                                .datasetName("EMPLOYEE_SALARIES")
                                .predicate("1=1")
                                .build())))
                .outputDataset(
                        Populated.stagingDatasetConfig()
                                .id(12312L)
                                .stagingSchemaValidation(Populated.stagingSchemaValidation()
                                        .displayName("Employees in departments with salaries")
                                        .physicalTableName("EMPLOYEES_DEPARTMENTS_SALARIES")
                                        .build())
                                .build())
                .build();

        PipelineAppConfig pipelineAppConfig = PipelineAppConfig.builder()
                .name("test staging")
                .serviceRequestId(1)
                .pipelineInvocationId(8292L)
                .pipelineSteps(singletonList(scriptletStepConfig))
                .sparkFunctionConfig(SparkFunctionConfig.builder()
                        .referenceDate(LocalDate.of(2019, 1, 1))
                        .build())
                .build();

        pipelineJobOperator.runJob(pipelineAppConfig);

        Dataset<Row> createdDataset = datasetRepository.readDataFrame(DatasetTableLookup.builder()
                .datasetName("EMPLOYEES_DEPARTMENTS_SALARIES")
                .predicate("1=1")
                .build());

        List<Row> rows = createdDataset.collectAsList();
        assertThat(rows)
                .extracting(
                        row -> row.getAs("Name"), row -> row.getAs("Department"), row -> row.getAs("Salary"))
                .containsExactlyInAnyOrder(
                        tuple("Tony Stark", "Avengers", 10000),
                        tuple("Natasha Romanoff", "Avengers", 20000),
                        tuple("Nick Fury", "Shield", 30000));
    }
}