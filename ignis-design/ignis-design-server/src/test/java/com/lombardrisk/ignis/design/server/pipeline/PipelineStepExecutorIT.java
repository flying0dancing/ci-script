package com.lombardrisk.ignis.design.server.pipeline;

import com.google.common.collect.ImmutableMap;
import com.lombardrisk.ignis.client.design.pipeline.error.SelectResult;
import com.lombardrisk.ignis.client.external.pipeline.export.step.JoinType;
import com.lombardrisk.ignis.common.assertions.VavrAssert;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.design.field.DesignField;
import com.lombardrisk.ignis.design.server.DesignStudioTestConfig;
import com.lombardrisk.ignis.design.server.fixtures.Design;
import com.lombardrisk.ignis.design.server.pipeline.model.ExecuteStepContext;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineAggregationStep;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineJoinStep;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineMapStep;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineUnionStep;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineWindowStep;
import com.lombardrisk.ignis.design.server.pipeline.model.join.Join;
import com.lombardrisk.ignis.design.server.pipeline.model.join.JoinField;
import com.lombardrisk.ignis.design.server.pipeline.model.select.Order;
import com.lombardrisk.ignis.design.server.pipeline.model.select.Select;
import com.lombardrisk.ignis.design.server.pipeline.model.select.Union;
import com.lombardrisk.ignis.design.server.pipeline.model.select.Window;
import com.lombardrisk.ignis.design.server.productconfig.schema.Schema;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@RunWith(SpringRunner.class)
@DesignStudioTestConfig
public class PipelineStepExecutorIT {

    @Autowired
    private PipelineStepExecutor pipelineStepExecutor;

    @Test
    public void executeFullTransformation_MapStep_ReturnsValid() {
        Schema input = Design.Populated.schema()
                .fields(newHashSet(
                        DesignField.Populated.stringField("Name").id(1L).build(),
                        DesignField.Populated.doubleField("Salary").id(3L).build()))
                .build();
        Schema output = Design.Populated.schema()
                .fields(newHashSet(
                        DesignField.Populated.doubleField("SalaryInK").id(101L).build(),
                        DesignField.Populated.stringField("Display").id(102L).build()))
                .build();

        PipelineMapStep mapStep = Design.Populated.pipelineMapStep()
                .selects(newLinkedHashSet(Arrays.asList(
                        Select.builder()
                                .order(0L)
                                .select("Salary / 1000.0D")
                                .outputFieldId(101L)
                                .isIntermediate(true)
                                .build(),
                        Select.builder()
                                .select("concat('I earn ', SalaryInK)")
                                .outputFieldId(102L)
                                .isIntermediate(false)
                                .build())))
                .build();

        ExecuteStepContext executeStepContext = ExecuteStepContext.mockData(singletonList(input), output);

        VavrAssert.assertValid(
                pipelineStepExecutor.executeFullTransformation(mapStep, executeStepContext));
    }

    @Test
    public void executeFullTransformation_MapStepDefinedMissingIntermediateFlag_ReturnsInValid() {
        Schema input = Design.Populated.schema()
                .fields(newHashSet(
                        DesignField.Populated.stringField("Name").id(1L).build(),
                        DesignField.Populated.doubleField("Salary").id(3L).build()))
                .build();
        Schema output = Design.Populated.schema()
                .fields(newHashSet(
                        DesignField.Populated.doubleField("SalaryInK").id(101L).build(),
                        DesignField.Populated.stringField("Display").id(102L).build()))
                .build();

        PipelineMapStep mapStep = Design.Populated.pipelineMapStep()
                .selects(newLinkedHashSet(Arrays.asList(
                        Select.builder()
                                .select("concat('I earn ', SalaryInK)")
                                .outputFieldId(102L)
                                .build(),
                        Select.builder()
                                .order(1L)
                                .select("Salary / 1000.0D")
                                .isIntermediate(false)
                                .outputFieldId(101L)
                                .build())))
                .build();

        ExecuteStepContext executeStepContext = ExecuteStepContext.mockData(singletonList(input), output);

        VavrAssert.assertCollectionFailure(
                pipelineStepExecutor.executeFullTransformation(mapStep, executeStepContext))
                .withFailure(ErrorResponse.valueOf(
                        "cannot resolve '`SalaryInK`' given input columns: [ROW_KEY, Name, Salary]; line 1 pos 56",
                        "SQL_ANALYSIS_ERROR"));
    }

    @Test
    public void executeFullTransformation_MapStepWrongOutputFieldTypes_ReturnsInValid() {
        Schema input = Design.Populated.schema()
                .fields(newHashSet(
                        DesignField.Populated.stringField("Name").id(1L).build(),
                        DesignField.Populated.doubleField("Salary").id(3L).build()))
                .build();
        Schema output = Design.Populated.schema()
                .fields(newHashSet(
                        DesignField.Populated.doubleField("SalaryInK").id(101L).build(),
                        DesignField.Populated.stringField("Display").id(102L).build()))
                .build();

        PipelineMapStep mapStep = Design.Populated.pipelineMapStep()
                .selects(newLinkedHashSet(Arrays.asList(
                        Select.builder()
                                .order(0L)
                                .select("'One Hundred Pounds'")
                                .isIntermediate(true)
                                .outputFieldId(101L)
                                .build(),
                        Select.builder()
                                .select("concat('I earn ', SalaryInK)")
                                .outputFieldId(102L)
                                .build())))
                .build();

        ExecuteStepContext executeStepContext = ExecuteStepContext.mockData(singletonList(input), output);

        VavrAssert.assertCollectionFailure(
                pipelineStepExecutor.executeFullTransformation(mapStep, executeStepContext))
                .withFailure(ErrorResponse.valueOf(
                        "Output fields required the following fields that were not found [SalaryInK(type=DoubleType)]",
                        "UNMAPPED_OUTPUT_FIELDS"));
    }

    @Test
    public void transformOutputFieldsIndividually_MapStep_ReturnsValid() {
        Schema input = Design.Populated.schema()
                .fields(newHashSet(
                        DesignField.Populated.stringField("Name").id(1L).build(),
                        DesignField.Populated.doubleField("Salary").id(2L).build()))
                .build();
        Schema output = Design.Populated.schema()
                .fields(newHashSet(
                        DesignField.Populated.doubleField("SalaryInK").id(101L).build(),
                        DesignField.Populated.stringField("Display").id(102L).build()))
                .build();

        PipelineMapStep mapStep = Design.Populated.pipelineMapStep()
                .selects(newLinkedHashSet(Arrays.asList(
                        Select.builder()
                                .order(0L)
                                .select("Salary / 1000.0D")
                                .outputFieldId(101L)
                                .isIntermediate(true)
                                .build(),
                        Select.builder()
                                .select("concat('I earn ', SalaryInK)")
                                .outputFieldId(102L)
                                .build())))
                .build();

        ExecuteStepContext executeStepContext = ExecuteStepContext.mockData(singletonList(input), output);

        List<SelectResult> selectResults = pipelineStepExecutor.transformOutputFieldsIndividually(
                mapStep, executeStepContext);

        assertThat(selectResults)
                .extracting(SelectResult::isValid)
                .containsExactlyInAnyOrder(true, true);
    }

    @Test
    public void transformOutputFieldsIndividually_MapStepDefinedInWrongOrder_ReturnsInValid() {
        Schema input = Design.Populated.schema()
                .fields(newHashSet(
                        DesignField.Populated.stringField("Name").id(1L).build(),
                        DesignField.Populated.doubleField("Salary").id(3L).build()))
                .build();
        Schema output = Design.Populated.schema()
                .fields(newHashSet(
                        DesignField.Populated.doubleField("SalaryInK").id(101L).build(),
                        DesignField.Populated.stringField("Display").id(102L).build()))
                .build();

        PipelineMapStep mapStep = Design.Populated.pipelineMapStep()
                .selects(newLinkedHashSet(Arrays.asList(
                        Select.builder()
                                .order(0L)
                                .select("concat('I earn ', SalaryInK)")
                                .outputFieldId(102L)
                                .build(),
                        Select.builder()
                                .order(1L)
                                .select("Salary / 1000.0D")
                                .outputFieldId(101L)
                                .build())))
                .build();

        ExecuteStepContext executeStepContext = ExecuteStepContext.mockData(singletonList(input), output);

        List<SelectResult> selectResults = pipelineStepExecutor.transformOutputFieldsIndividually(
                mapStep, executeStepContext);

        assertThat(selectResults)
                .filteredOn(selectResult -> !selectResult.isValid())
                .extracting(SelectResult::getOutputFieldName, SelectResult::getErrors)
                .containsOnly(
                        tuple("Display", singletonList(ErrorResponse.valueOf(
                                "cannot resolve '`SalaryInK`' given input columns: [ROW_KEY, Name, Salary]; line 1 pos 25",
                                "SQL_ANALYSIS_ERROR"))));
    }

    @Test
    public void transformOutputFieldsIndividually_MapStepWrongOutputFieldTypes_ReturnsInValid() {
        Schema input = Design.Populated.schema()
                .fields(newHashSet(
                        DesignField.Populated.stringField("Name").id(1L).build(),
                        DesignField.Populated.doubleField("Salary").id(3L).build()))
                .build();
        Schema output = Design.Populated.schema()
                .fields(newHashSet(
                        DesignField.Populated.doubleField("SalaryInK").id(101L).build(),
                        DesignField.Populated.stringField("Display").id(102L).build()))
                .build();

        PipelineMapStep mapStep = Design.Populated.pipelineMapStep()
                .selects(newLinkedHashSet(Arrays.asList(
                        Select.builder()
                                .order(0L)
                                .select("'One Hundred Pounds'")
                                .outputFieldId(101L)
                                .isIntermediate(true)
                                .build(),
                        Select.builder()
                                .select("concat('I earn ', SalaryInK)")
                                .outputFieldId(102L)
                                .build())))
                .build();

        ExecuteStepContext executeStepContext = ExecuteStepContext.mockData(singletonList(input), output);

        List<SelectResult> selectResults =
                pipelineStepExecutor.transformOutputFieldsIndividually(mapStep, executeStepContext);

        assertThat(selectResults)
                .filteredOn(selectResult -> !selectResult.isValid())
                .extracting(SelectResult::getOutputFieldName, SelectResult::getErrors)
                .containsOnly(
                        tuple("SalaryInK", singletonList(ErrorResponse.valueOf(
                                "Output fields required the following fields that were not found SalaryInK(type=DoubleType)",
                                "UNMAPPED_OUTPUT_FIELDS"))));
    }

    @Test
    public void executeFullTransformation_AggregationStep_ReturnsValid() {
        Schema input = Design.Populated.schema()
                .fields(newHashSet(
                        DesignField.Populated.stringField("Name").id(1L).build(),
                        DesignField.Populated.stringField("Department").id(2L).build(),
                        DesignField.Populated.doubleField("Salary").id(3L).build()))
                .build();

        Schema output = Design.Populated.schema()
                .fields(newLinkedHashSet(Arrays.asList(
                        DesignField.Populated.stringField("Department").id(101L).build(),
                        DesignField.Populated.doubleField("AvgSalary").id(102L).build())))
                .build();

        PipelineAggregationStep aggStep = Design.Populated.pipelineAggregationStep()
                .groupings(newHashSet("Department"))
                .selects(newLinkedHashSet(Arrays.asList(
                        Select.builder()
                                .select("Department")
                                .outputFieldId(101L)
                                .build(),
                        Select.builder()
                                .select("avg(Salary)")
                                .outputFieldId(102L)
                                .build())))
                .build();

        ExecuteStepContext executeStepContext = ExecuteStepContext.mockData(singletonList(input), output);

        VavrAssert.assertValid(
                pipelineStepExecutor.executeFullTransformation(aggStep, executeStepContext));
    }

    @Test
    public void executeFullTransformation_AggregationStepWrongOutputFieldTypes_ReturnsInValid() {
        Schema input = Design.Populated.schema()
                .fields(newHashSet(
                        DesignField.Populated.stringField("Name").id(1L).build(),
                        DesignField.Populated.stringField("Department").id(2L).build(),
                        DesignField.Populated.doubleField("Salary").id(3L).build()))
                .build();

        Schema output = Design.Populated.schema()
                .fields(newLinkedHashSet(Arrays.asList(
                        DesignField.Populated.stringField("Department").id(101L).build(),
                        DesignField.Populated.stringField("AvgSalary").id(102L).build())))
                .build();

        PipelineAggregationStep aggStep = Design.Populated.pipelineAggregationStep()
                .groupings(newHashSet("Department"))
                .selects(newLinkedHashSet(Arrays.asList(
                        Select.builder()
                                .select("Department")
                                .outputFieldId(101L)
                                .build(),
                        Select.builder()
                                .select("avg(Salary)")
                                .outputFieldId(102L)
                                .build())))
                .build();

        ExecuteStepContext executeStepContext = ExecuteStepContext.mockData(singletonList(input), output);

        VavrAssert.assertCollectionFailure(
                pipelineStepExecutor.executeFullTransformation(aggStep, executeStepContext))
                .withFailure(ErrorResponse.valueOf(
                        "Output fields required the following fields that were not found [AvgSalary(type=StringType)]",
                        "UNMAPPED_OUTPUT_FIELDS"))
                .withFailure(ErrorResponse.valueOf(
                        "The following fields in the transformation were not mapped to any fields in the output [AvgSalary(type=DoubleType)]",
                        "UNMAPPED_INPUT_FIELDS"));
    }

    @Test
    public void transformOutputFieldsIndividually_AggregationStep_ReturnsValid() {
        Schema input = Design.Populated.schema()
                .fields(newHashSet(
                        DesignField.Populated.stringField("Name").id(1L).build(),
                        DesignField.Populated.stringField("Department").id(2L).build(),
                        DesignField.Populated.doubleField("Salary").id(3L).build()))
                .build();

        Schema output = Design.Populated.schema()
                .fields(newLinkedHashSet(Arrays.asList(
                        DesignField.Populated.stringField("Department").id(101L).build(),
                        DesignField.Populated.doubleField("AvgSalary").id(102L).build())))
                .build();

        PipelineAggregationStep aggStep = Design.Populated.pipelineAggregationStep()
                .groupings(newHashSet("Department"))
                .selects(newLinkedHashSet(Arrays.asList(
                        Select.builder()
                                .select("Department")
                                .outputFieldId(101L)
                                .build(),
                        Select.builder()
                                .select("avg(Salary)")
                                .outputFieldId(102L)
                                .build())))
                .build();

        ExecuteStepContext executeStepContext = ExecuteStepContext.mockData(singletonList(input), output);

        List<SelectResult> selectResults =
                pipelineStepExecutor.transformOutputFieldsIndividually(aggStep, executeStepContext);

        assertThat(selectResults)
                .filteredOn(selectResult -> !selectResult.isValid())
                .isEmpty();
    }

    @Test
    public void transformOutputFieldsIndividually_AggregationStepWithSyntaxError_ReturnsValid() {
        Schema input = Design.Populated.schema()
                .fields(newHashSet(
                        DesignField.Populated.stringField("Name").id(1L).build(),
                        DesignField.Populated.stringField("Department").id(2L).build(),
                        DesignField.Populated.doubleField("Salary").id(3L).build()))
                .build();

        Schema output = Design.Populated.schema()
                .fields(newLinkedHashSet(Arrays.asList(
                        DesignField.Populated.stringField("Department").id(101L).build(),
                        DesignField.Populated.doubleField("AvgSalary").id(102L).build())))
                .build();

        PipelineAggregationStep aggStep = Design.Populated.pipelineAggregationStep()
                .groupings(newHashSet("Department"))
                .selects(newLinkedHashSet(Arrays.asList(
                        Select.builder()
                                .select("notAFunction()")
                                .outputFieldId(101L)
                                .build(),
                        Select.builder()
                                .select("avg(Salary)")
                                .outputFieldId(102L)
                                .build())))
                .build();

        ExecuteStepContext executeStepContext = ExecuteStepContext.mockData(singletonList(input), output);

        List<SelectResult> selectResults =
                pipelineStepExecutor.transformOutputFieldsIndividually(aggStep, executeStepContext);

        assertThat(selectResults)
                .filteredOn(selectResult -> !selectResult.isValid())
                .extracting(SelectResult::getOutputFieldName, SelectResult::getErrors)
                .containsOnly(tuple(
                        "Department",
                        singletonList(ErrorResponse.valueOf(
                                "Undefined function: 'notAFunction'. This function is neither a registered "
                                        + "temporary function nor a permanent function registered in the database "
                                        + "'default'.; line 1 pos 7",
                                "SQL_ANALYSIS_ERROR"))));
    }

    @Test
    public void executeFullTransformation_JoinStep_ReturnsValid() {
        Schema left = Design.Populated.schema("left").id(200L)
                .fields(newHashSet(
                        DesignField.Populated.longField("L_ID").id(90L).build(),
                        DesignField.Populated.stringField("Department").id(91L).build(),
                        DesignField.Populated.doubleField("Salary").id(92L).build()))
                .build();

        Schema right = Design.Populated.schema("right").id(201L)
                .fields(newHashSet(
                        DesignField.Populated.longField("R_ID").id(80L).build(),
                        DesignField.Populated.stringField("FIRST_NAME").id(81L).build(),
                        DesignField.Populated.stringField("LastName").id(82L).build()))
                .build();

        Schema output = Design.Populated.schema().id(202L)
                .fields(newLinkedHashSet(Arrays.asList(
                        DesignField.Populated.stringField("FullName").id(100L).build(),
                        DesignField.Populated.stringField("Department").id(101L).build(),
                        DesignField.Populated.doubleField("Salary").id(102L).build())))
                .build();

        PipelineJoinStep joinStep = Design.Populated.pipelineJoinStep()
                .schemaOutId(output.getId())
                .joins(newHashSet(Join.builder()
                        .leftSchemaId(left.getId())
                        .rightSchemaId(right.getId())
                        .joinType(JoinType.FULL_OUTER)
                        .joinFields(newHashSet(JoinField.builder()
                                .rightJoinFieldId(80L)
                                .leftJoinFieldId(90L)
                                .build()))
                        .build()))
                .selects(newLinkedHashSet(Arrays.asList(
                        Select.builder()
                                .select("CONCAT(FIRST_NAME, LastName)")
                                .outputFieldId(100L)
                                .build(),
                        Select.builder()
                                .select("Department")
                                .outputFieldId(101L)
                                .build(),
                        Select.builder()
                                .select("Salary")
                                .outputFieldId(102L)
                                .build())))
                .build();

        ExecuteStepContext executeStepContext = ExecuteStepContext.mockData(Arrays.asList(left, right), output);

        VavrAssert.assertValid(
                pipelineStepExecutor.executeFullTransformation(joinStep, executeStepContext));
    }

    @Test
    public void executeFullTransformation_JoinStepInvalidSyntax_ReturnsInValid() {
        Schema left = Design.Populated.schema("left").id(200L)
                .fields(newHashSet(
                        DesignField.Populated.longField("L_ID").id(90L).build(),
                        DesignField.Populated.stringField("Department").id(91L).build(),
                        DesignField.Populated.doubleField("Salary").id(92L).build()))
                .build();

        Schema right = Design.Populated.schema("right").id(201L)
                .fields(newHashSet(
                        DesignField.Populated.longField("R_ID").id(80L).build(),
                        DesignField.Populated.stringField("FIRST_NAME").id(81L).build(),
                        DesignField.Populated.stringField("LastName").id(82L).build()))
                .build();

        Schema output = Design.Populated.schema().id(202L)
                .fields(newLinkedHashSet(Arrays.asList(
                        DesignField.Populated.stringField("FullName").id(100L).build(),
                        DesignField.Populated.stringField("Department").id(101L).build(),
                        DesignField.Populated.doubleField("Salary").id(102L).build())))
                .build();

        PipelineJoinStep joinStep = Design.Populated.pipelineJoinStep()
                .schemaOutId(output.getId())
                .joins(newHashSet(Join.builder()
                        .leftSchemaId(left.getId())
                        .rightSchemaId(right.getId())
                        .joinType(JoinType.FULL_OUTER)
                        .joinFields(newHashSet(JoinField.builder()
                                .rightJoinFieldId(80L)
                                .leftJoinFieldId(90L)
                                .build()))
                        .build()))
                .selects(newLinkedHashSet(Arrays.asList(
                        Select.builder()
                                .select("notAFunction(FIRST_NAME, LastName)")
                                .outputFieldId(100L)
                                .build(),
                        Select.builder()
                                .select("Department")
                                .outputFieldId(101L)
                                .build(),
                        Select.builder()
                                .select("Salary")
                                .outputFieldId(102L)
                                .build())))
                .build();

        ExecuteStepContext executeStepContext = ExecuteStepContext.mockData(Arrays.asList(left, right), output);

        VavrAssert.assertCollectionFailure(
                pipelineStepExecutor.executeFullTransformation(joinStep, executeStepContext))
                .withFailure(ErrorResponse.valueOf(
                        "Undefined function: 'notAFunction'. This function is neither a registered temporary "
                                + "function nor a permanent function registered in the database 'default'.; line 1 pos 7",
                        "SQL_ANALYSIS_ERROR"));
    }

    @Test
    public void transformOutputFieldsIndividually_JoinStep_ReturnsValidSelectResults() {
        Schema left = Design.Populated.schema("left").id(200L)
                .fields(newHashSet(
                        DesignField.Populated.longField("L_ID").id(90L).build(),
                        DesignField.Populated.stringField("Department").id(91L).build(),
                        DesignField.Populated.doubleField("Salary").id(92L).build()))
                .build();

        Schema right = Design.Populated.schema("right").id(201L)
                .fields(newHashSet(
                        DesignField.Populated.longField("R_ID").id(80L).build(),
                        DesignField.Populated.stringField("FIRST_NAME").id(81L).build(),
                        DesignField.Populated.stringField("LastName").id(82L).build()))
                .build();

        Schema output = Design.Populated.schema().id(202L)
                .fields(newLinkedHashSet(Arrays.asList(
                        DesignField.Populated.stringField("FullName").id(100L).build(),
                        DesignField.Populated.stringField("Department").id(101L).build(),
                        DesignField.Populated.doubleField("Salary").id(102L).build())))
                .build();

        PipelineJoinStep joinStep = Design.Populated.pipelineJoinStep()
                .schemaOutId(output.getId())
                .joins(newHashSet(Join.builder()
                        .leftSchemaId(left.getId())
                        .rightSchemaId(right.getId())
                        .joinType(JoinType.FULL_OUTER)
                        .joinFields(newHashSet(JoinField.builder()
                                .rightJoinFieldId(80L)
                                .leftJoinFieldId(90L)
                                .build()))
                        .build()))
                .selects(newLinkedHashSet(Arrays.asList(
                        Select.builder()
                                .select("CONCAT(FIRST_NAME, LastName)")
                                .outputFieldId(100L)
                                .build(),
                        Select.builder()
                                .select("Department")
                                .outputFieldId(101L)
                                .build(),
                        Select.builder()
                                .select("Salary")
                                .outputFieldId(102L)
                                .build())))
                .build();

        ExecuteStepContext executeStepContext = ExecuteStepContext.mockData(Arrays.asList(left, right), output);

        List<SelectResult> selectResults =
                pipelineStepExecutor.transformOutputFieldsIndividually(joinStep, executeStepContext);

        assertThat(selectResults)
                .filteredOn(selectResult -> !selectResult.isValid())
                .isEmpty();
    }

    @Test
    public void transformOutputFieldsIndividually_JoinStepInvalidSyntax_ReturnsInValid() {
        Schema left = Design.Populated.schema("left").id(200L)
                .fields(newHashSet(
                        DesignField.Populated.longField("L_ID").id(90L).build(),
                        DesignField.Populated.stringField("Department").id(91L).build(),
                        DesignField.Populated.doubleField("Salary").id(92L).build()))
                .build();

        Schema right = Design.Populated.schema("right").id(201L)
                .fields(newHashSet(
                        DesignField.Populated.longField("R_ID").id(80L).build(),
                        DesignField.Populated.stringField("FIRST_NAME").id(81L).build(),
                        DesignField.Populated.stringField("LastName").id(82L).build()))
                .build();

        Schema output = Design.Populated.schema().id(202L)
                .fields(newLinkedHashSet(Arrays.asList(
                        DesignField.Populated.stringField("FullName").id(100L).build(),
                        DesignField.Populated.stringField("Department").id(101L).build(),
                        DesignField.Populated.doubleField("Salary").id(102L).build())))
                .build();

        PipelineJoinStep joinStep = Design.Populated.pipelineJoinStep()
                .schemaOutId(output.getId())
                .joins(newHashSet(Join.builder()
                        .leftSchemaId(left.getId())
                        .rightSchemaId(right.getId())
                        .joinType(JoinType.FULL_OUTER)
                        .joinFields(newHashSet(JoinField.builder()
                                .rightJoinFieldId(80L)
                                .leftJoinFieldId(90L)
                                .build()))
                        .build()))
                .selects(newLinkedHashSet(Arrays.asList(
                        Select.builder()
                                .select("notAFunction(FIRST_NAME, LastName)")
                                .outputFieldId(100L)
                                .build(),
                        Select.builder()
                                .select("Department")
                                .outputFieldId(101L)
                                .build(),
                        Select.builder()
                                .select("Salary")
                                .outputFieldId(102L)
                                .build())))
                .build();

        ExecuteStepContext executeStepContext = ExecuteStepContext.mockData(Arrays.asList(left, right), output);

        List<SelectResult> selectResults =
                pipelineStepExecutor.transformOutputFieldsIndividually(joinStep, executeStepContext);

        assertThat(selectResults)
                .filteredOn(selectResult -> !selectResult.isValid())
                .extracting(SelectResult::getOutputFieldName, SelectResult::getErrors)
                .containsExactly(tuple("FullName", singletonList(
                        ErrorResponse.valueOf(
                                "Undefined function: 'notAFunction'. This function is neither a registered temporary "
                                        + "function nor a permanent function registered in the database 'default'.; line 1 pos 7",
                                "SQL_ANALYSIS_ERROR"))));
    }

    @Test
    public void executeFullTransformation_WindowStep_ReturnsValid() {
        Schema input = Design.Populated.schema()
                .fields(newHashSet(
                        DesignField.Populated.stringField("Name").id(1L).build(),
                        DesignField.Populated.stringField("Department").id(2L).build(),
                        DesignField.Populated.doubleField("Salary").id(3L).build()))
                .build();
        Schema output = Design.Populated.schema()
                .fields(newHashSet(
                        DesignField.Populated.stringField("Department").id(101L).build(),
                        DesignField.Populated.doubleField("SalaryInK").id(102L).build(),
                        DesignField.Populated.intField("Rank").id(103L).build()))
                .build();

        PipelineWindowStep windowStep = Design.Populated.pipelineWindowStep()
                .selects(newLinkedHashSet(Arrays.asList(
                        Select.builder()
                                .order(0L)
                                .select("Department")
                                .outputFieldId(101L)
                                .build(),
                        Select.builder()
                                .order(1L)
                                .select("Salary / 1000.0D")
                                .isIntermediate(true)
                                .outputFieldId(102L)
                                .build(),
                        Select.builder()
                                .select("dense_rank()")
                                .isWindow(true)
                                .window(Window.builder()
                                        .partitions(newHashSet("SalaryInK"))
                                        .orders(newHashSet(Order.builder()
                                                .fieldName("SalaryInK")
                                                .direction(Order.Direction.DESC)
                                                .build()))
                                        .build())
                                .outputFieldId(103L)
                                .build())))
                .build();

        ExecuteStepContext executeStepContext = ExecuteStepContext.mockData(singletonList(input), output);

        VavrAssert.assertValid(
                pipelineStepExecutor.executeFullTransformation(windowStep, executeStepContext));
    }

    @Test
    public void executeFullTransformation_WindowStepWithNoPartitionsOrOrderings_ReturnsValid() {
        Schema input = Design.Populated.schema()
                .fields(newHashSet(
                        DesignField.Populated.stringField("FieldA").id(1L).build(),
                        DesignField.Populated.stringField("FieldB").id(2L).build()))
                .build();
        Schema output = Design.Populated.schema()
                .fields(newHashSet(
                        DesignField.Populated.stringField("FieldA").id(101L).build(),
                        DesignField.Populated.stringField("FieldB").id(102L).build(),
                        DesignField.Populated.stringField("FieldC").id(103L).build()))
                .build();

        PipelineWindowStep windowStep = Design.Populated.pipelineWindowStep()
                .selects(newLinkedHashSet(Arrays.asList(
                        Select.builder()
                                .order(0L)
                                .select("FieldA")
                                .outputFieldId(101L)
                                .build(),
                        Select.builder()
                                .order(1L)
                                .select("FieldB")
                                .outputFieldId(102L)
                                .build(),
                        Select.builder()
                                .select("first_value(FieldB)")
                                .isWindow(true)
                                .window(Window.builder()
                                        .partitions(emptySet())
                                        .orders(emptySet())
                                        .build())
                                .outputFieldId(103L)
                                .build())))
                .build();

        ExecuteStepContext executeStepContext = ExecuteStepContext.mockData(singletonList(input), output);

        VavrAssert.assertValid(
                pipelineStepExecutor.executeFullTransformation(windowStep, executeStepContext));
    }

    @Test
    public void executeFullTransformation_WindowStepInvalidOutputFields_ReturnsInValid() {
        Schema input = Design.Populated.schema()
                .fields(newHashSet(
                        DesignField.Populated.stringField("Name").id(1L).build(),
                        DesignField.Populated.stringField("Department").id(2L).build(),
                        DesignField.Populated.doubleField("Salary").id(3L).build()))
                .build();
        Schema output = Design.Populated.schema()
                .fields(newHashSet(
                        DesignField.Populated.stringField("Department").id(101L).build(),
                        DesignField.Populated.doubleField("SalaryInK").id(102L).build(),
                        DesignField.Populated.stringField("Rank").id(103L).build()))
                .build();

        PipelineWindowStep windowStep = Design.Populated.pipelineWindowStep()
                .selects(newLinkedHashSet(Arrays.asList(
                        Select.builder()
                                .select("Department")
                                .outputFieldId(101L)
                                .build(),
                        Select.builder()
                                .order(1L)
                                .select("Salary / 1000.0D")
                                .outputFieldId(102L)
                                .isIntermediate(true)
                                .build(),
                        Select.builder()
                                .order(2L)
                                .select("dense_rank()")
                                .isWindow(true)
                                .window(Window.builder()
                                        .partitions(newHashSet("SalaryInK"))
                                        .orders(newHashSet(Order.builder()
                                                .fieldName("SalaryInK")
                                                .direction(Order.Direction.DESC)
                                                .build()))
                                        .build())
                                .outputFieldId(103L)
                                .build())))
                .build();

        ExecuteStepContext executeStepContext = ExecuteStepContext.mockData(singletonList(input), output);

        VavrAssert.assertCollectionFailure(
                pipelineStepExecutor.executeFullTransformation(windowStep, executeStepContext))
                .withFailure(ErrorResponse.valueOf(
                        "Output fields required the following fields that were not found [Rank(type=StringType)]",
                        "UNMAPPED_OUTPUT_FIELDS"))
                .withFailure(ErrorResponse.valueOf(
                        "The following fields in the transformation were not mapped to any fields in the"
                                + " output [Rank(type=IntegerType)]",
                        "UNMAPPED_INPUT_FIELDS"));
    }

    @Test
    public void executeFullTransformation_WindowStepDependantFieldsInWrongOrder_ReturnsInValid() {
        Schema input = Design.Populated.schema()
                .fields(newHashSet(
                        DesignField.Populated.stringField("Name").id(1L).build(),
                        DesignField.Populated.stringField("Department").id(2L).build(),
                        DesignField.Populated.doubleField("Salary").id(3L).build()))
                .build();
        Schema output = Design.Populated.schema()
                .fields(newHashSet(
                        DesignField.Populated.stringField("Department").id(101L).build(),
                        DesignField.Populated.doubleField("SalaryInK").id(102L).build(),
                        DesignField.Populated.intField("Rank").id(103L).build()))
                .build();

        PipelineWindowStep windowStep = Design.Populated.pipelineWindowStep()
                .selects(newLinkedHashSet(Arrays.asList(
                        Select.builder()
                                .select("Department")
                                .outputFieldId(101L)
                                .build(),
                        Select.builder()
                                .order(0L)
                                .select("dense_rank()")
                                .isIntermediate(true)
                                .isWindow(true)
                                .window(Window.builder()
                                        .partitions(newHashSet("SalaryInK"))
                                        .orders(newHashSet(Order.builder()
                                                .fieldName("SalaryInK")
                                                .direction(Order.Direction.DESC)
                                                .build()))
                                        .build())
                                .outputFieldId(103L)
                                .build(),
                        Select.builder()
                                .select("Salary / 1000.0D")
                                .outputFieldId(102L)
                                .build())))
                .build();

        ExecuteStepContext executeStepContext = ExecuteStepContext.mockData(singletonList(input), output);

        VavrAssert.assertCollectionFailure(
                pipelineStepExecutor.executeFullTransformation(windowStep, executeStepContext))
                .withFailure(ErrorResponse.valueOf(
                        "cannot resolve '`SalaryInK`' given input columns: [ROW_KEY, Name, Department, Salary];",
                        "SQL_ANALYSIS_ERROR"));
    }

    @Test
    public void transformOutputFieldsIndividually_WindowStep_ReturnsValid() {
        Schema input = Design.Populated.schema()
                .fields(newHashSet(
                        DesignField.Populated.stringField("Name").id(1L).build(),
                        DesignField.Populated.stringField("Department").id(2L).build(),
                        DesignField.Populated.doubleField("Salary").id(3L).build()))
                .build();
        Schema output = Design.Populated.schema()
                .fields(newHashSet(
                        DesignField.Populated.stringField("Department").id(101L).build(),
                        DesignField.Populated.doubleField("SalaryInK").id(102L).build(),
                        DesignField.Populated.intField("Rank").id(103L).build()))
                .build();

        PipelineWindowStep windowStep = Design.Populated.pipelineWindowStep()
                .selects(newLinkedHashSet(Arrays.asList(
                        Select.builder()
                                .select("Department")
                                .outputFieldId(101L)
                                .build(),
                        Select.builder()
                                .order(1L)
                                .select("Salary / 1000.0D")
                                .outputFieldId(102L)
                                .isIntermediate(true)
                                .build(),
                        Select.builder()
                                .select("dense_rank()")
                                .isWindow(true)
                                .window(Window.builder()
                                        .partitions(newHashSet("SalaryInK"))
                                        .orders(newHashSet(Order.builder()
                                                .fieldName("SalaryInK")
                                                .direction(Order.Direction.DESC)
                                                .build()))
                                        .build())
                                .outputFieldId(103L)
                                .build())))
                .build();

        ExecuteStepContext executeStepContext = ExecuteStepContext.mockData(singletonList(input), output);

        List<SelectResult> selectResults =
                pipelineStepExecutor.transformOutputFieldsIndividually(windowStep, executeStepContext);

        assertThat(selectResults)
                .filteredOn(selectResult -> !selectResult.isValid())
                .isEmpty();
    }

    @Test
    public void transformOutputFieldsIndividually_WindowStepWithoutPartitionsOrOrdering_ReturnsValid() {
        Schema input = Design.Populated.schema()
                .fields(newHashSet(
                        DesignField.Populated.stringField("FieldA").id(1L).build(),
                        DesignField.Populated.stringField("FieldB").id(2L).build()))
                .build();
        Schema output = Design.Populated.schema()
                .fields(newHashSet(
                        DesignField.Populated.stringField("FieldA").id(101L).build(),
                        DesignField.Populated.stringField("FieldB").id(102L).build(),
                        DesignField.Populated.stringField("FieldC").id(103L).build()))
                .build();

        PipelineWindowStep windowStep = Design.Populated.pipelineWindowStep()
                .selects(newLinkedHashSet(Arrays.asList(
                        Select.builder()
                                .select("FieldA")
                                .outputFieldId(101L)
                                .build(),
                        Select.builder()
                                .order(1L)
                                .select("FieldB")
                                .outputFieldId(102L)
                                .isIntermediate(true)
                                .build(),
                        Select.builder()
                                .select("first_value(FieldB, true)")
                                .isWindow(true)
                                .window(Window.builder()
                                        .partitions(emptySet())
                                        .orders(emptySet())
                                        .build())
                                .outputFieldId(103L)
                                .build())))
                .build();

        ExecuteStepContext executeStepContext = ExecuteStepContext.mockData(singletonList(input), output);

        List<SelectResult> selectResults =
                pipelineStepExecutor.transformOutputFieldsIndividually(windowStep, executeStepContext);

        assertThat(selectResults)
                .filteredOn(selectResult -> !selectResult.isValid())
                .isEmpty();
    }

    @Test
    public void transformOutputFieldsIndividually_WindowStepSingleSelectInvalid_ReturnsInValid() {
        Schema input = Design.Populated.schema()
                .fields(newHashSet(
                        DesignField.Populated.stringField("Name").id(1L).build(),
                        DesignField.Populated.stringField("Department").id(2L).build(),
                        DesignField.Populated.doubleField("Salary").id(3L).build()))
                .build();
        Schema output = Design.Populated.schema()
                .fields(newHashSet(
                        DesignField.Populated.stringField("Department").id(101L).build(),
                        DesignField.Populated.doubleField("SalaryInK").id(102L).build(),
                        DesignField.Populated.intField("Rank").id(103L).build()))
                .build();

        PipelineWindowStep windowStep = Design.Populated.pipelineWindowStep()
                .selects(newLinkedHashSet(Arrays.asList(
                        Select.builder()
                                .select("Department")
                                .outputFieldId(101L)
                                .build(),
                        Select.builder()
                                .order(1L)
                                .select("'Salary divided by one thousand'")
                                .isIntermediate(true)
                                .outputFieldId(102L)
                                .build(),
                        Select.builder()
                                .select("dense_rank()")
                                .isWindow(true)
                                .window(Window.builder()
                                        .partitions(newHashSet("SalaryInK"))
                                        .orders(newHashSet(Order.builder()
                                                .fieldName("SalaryInK")
                                                .direction(Order.Direction.DESC)
                                                .build()))
                                        .build())
                                .outputFieldId(103L)
                                .build())))
                .build();

        ExecuteStepContext executeStepContext = ExecuteStepContext.mockData(singletonList(input), output);

        List<SelectResult> selectResults =
                pipelineStepExecutor.transformOutputFieldsIndividually(windowStep, executeStepContext);

        assertThat(selectResults)
                .filteredOn(selectResult -> !selectResult.isValid())
                .extracting(SelectResult::getOutputFieldName, SelectResult::getErrors)
                .containsOnly(tuple(
                        "SalaryInK",
                        singletonList(ErrorResponse.valueOf(
                                "Output fields required the following fields that were not found SalaryInK(type=DoubleType)",
                                "UNMAPPED_OUTPUT_FIELDS"))));
    }

    @Test
    public void executeFullTransformation_UnionStep_ReturnsValid() {
        Schema inputCustomers = Design.Populated.schema("CUSTOMERS").id(1L)
                .fields(newHashSet(
                        DesignField.Populated.longField("CustomerId").id(901L).build(),
                        DesignField.Populated.stringField("Name").id(902L).build(),
                        DesignField.Populated.doubleField("Salary").id(903L).build()))
                .build();

        Schema inputClients = Design.Populated.schema("CLIENTS").id(2L)
                .fields(newHashSet(
                        DesignField.Populated.intField("BusinessNumber").id(904L).build(),
                        DesignField.Populated.stringField("TaxCode").id(905L).build(),
                        DesignField.Populated.doubleField("Rate").id(906L).build()))
                .build();

        Schema output = Design.Populated.schema().id(3L)
                .fields(newHashSet(
                        DesignField.Populated.stringField("Id").id(101L).build(),
                        DesignField.Populated.doubleField("IncomeFlow").id(102L).build()))
                .build();

        PipelineUnionStep mapStep = Design.Populated.pipelineUnionStep()
                .selects(newLinkedHashSet(Arrays.asList(
                        Select.builder()
                                .order(0L)
                                .select("Concat(CustomerId, ':', Name)")
                                .outputFieldId(101L)
                                .isUnion(true)
                                .union(Union.forSchema(1L))
                                .build(),
                        Select.builder()
                                .order(0L)
                                .select("Concat(BusinessNumber, ':', TaxCode)")
                                .outputFieldId(101L)
                                .isUnion(true)
                                .union(Union.forSchema(2L))
                                .build(),
                        Select.builder()
                                .order(1L)
                                .select("-1 * Salary")
                                .outputFieldId(102L)
                                .isUnion(true)
                                .union(Union.forSchema(1L))
                                .build(),
                        Select.builder()
                                .order(2L)
                                .select("Rate")
                                .outputFieldId(102L)
                                .isUnion(true)
                                .union(Union.forSchema(2L))
                                .build())))
                .build();

        ExecuteStepContext executeStepContext =
                ExecuteStepContext.mockData(Arrays.asList(inputClients, inputCustomers), output);

        VavrAssert.assertValid(pipelineStepExecutor.executeFullTransformation(mapStep, executeStepContext));
    }

    @Test
    public void executeFullTransformation_UnionStepSyntaxError_ReturnsInValid() {
        Schema inputCustomers = Design.Populated.schema("CUSTOMERS").id(1L)
                .fields(newHashSet(
                        DesignField.Populated.longField("CustomerId").id(901L).build(),
                        DesignField.Populated.stringField("Name").id(902L).build(),
                        DesignField.Populated.doubleField("Salary").id(903L).build()))
                .build();

        Schema inputClients = Design.Populated.schema("CLIENTS").id(2L)
                .fields(newHashSet(
                        DesignField.Populated.intField("BusinessNumber").id(904L).build(),
                        DesignField.Populated.stringField("TaxCode").id(905L).build(),
                        DesignField.Populated.doubleField("Rate").id(906L).build()))
                .build();

        Schema output = Design.Populated.schema().id(3L)
                .fields(newHashSet(
                        DesignField.Populated.stringField("Id").id(101L).build(),
                        DesignField.Populated.doubleField("IncomeFlow").id(102L).build()))
                .build();

        PipelineUnionStep mapStep = Design.Populated.pipelineUnionStep()
                .selects(newLinkedHashSet(Arrays.asList(
                        Select.builder()
                                .order(0L)
                                .select("Concat(CustomerId, ':', Name)")
                                .outputFieldId(101L)
                                .isUnion(true)
                                .union(Union.forSchema(1L))
                                .build(),
                        Select.builder()
                                .order(0L)
                                .select("KATCONENATE(BusinessNumber, ':', TaxCode)")
                                .outputFieldId(101L)
                                .isUnion(true)
                                .union(Union.forSchema(2L))
                                .build(),
                        Select.builder()
                                .order(1L)
                                .select("-1 * Salary")
                                .outputFieldId(102L)
                                .isUnion(true)
                                .union(Union.forSchema(1L))
                                .build(),
                        Select.builder()
                                .order(2L)
                                .select("Rate")
                                .outputFieldId(102L)
                                .isUnion(true)
                                .union(Union.forSchema(2L))
                                .build())))
                .build();

        ExecuteStepContext executeStepContext =
                ExecuteStepContext.mockData(Arrays.asList(inputClients, inputCustomers), output);

        VavrAssert.assertCollectionFailure(
                pipelineStepExecutor.executeFullTransformation(mapStep, executeStepContext))
                .withOnlyFailures(ErrorResponse.valueOf(
                        "Undefined function: 'KATCONENATE'. This function is neither a registered temporary "
                                + "function nor a permanent function registered in the database 'default'.; line 1 pos 7",
                        "SQL_ANALYSIS_ERROR"));
    }

    @Test
    public void executeFullTransformation_UnionStepInvalidReturnType_ReturnsValid() {
        Schema inputCustomers = Design.Populated.schema("CUSTOMERS").id(1L)
                .fields(newHashSet(
                        DesignField.Populated.longField("CustomerId").id(901L).build(),
                        DesignField.Populated.stringField("Name").id(902L).build(),
                        DesignField.Populated.doubleField("Salary").id(903L).build()))
                .build();

        Schema inputClients = Design.Populated.schema("CLIENTS").id(2L)
                .fields(newHashSet(
                        DesignField.Populated.intField("BusinessNumber").id(904L).build(),
                        DesignField.Populated.stringField("TaxCode").id(905L).build(),
                        DesignField.Populated.doubleField("Rate").id(906L).build()))
                .build();

        Schema output = Design.Populated.schema().id(3L)
                .fields(newHashSet(
                        DesignField.Populated.intField("Id").id(101L).build(),
                        DesignField.Populated.doubleField("IncomeFlow").id(102L).build()))
                .build();

        PipelineUnionStep mapStep = Design.Populated.pipelineUnionStep()
                .selects(newLinkedHashSet(Arrays.asList(
                        Select.builder()
                                .order(0L)
                                .select("Concat(CustomerId, ':', Name)")
                                .outputFieldId(101L)
                                .isUnion(true)
                                .union(Union.forSchema(1L))
                                .build(),
                        Select.builder()
                                .order(0L)
                                .select("Concat(BusinessNumber, ':', TaxCode)")
                                .outputFieldId(101L)
                                .isUnion(true)
                                .union(Union.forSchema(2L))
                                .build(),
                        Select.builder()
                                .order(1L)
                                .select("-1 * Salary")
                                .outputFieldId(102L)
                                .isUnion(true)
                                .union(Union.forSchema(1L))
                                .build(),
                        Select.builder()
                                .order(2L)
                                .select("Rate")
                                .outputFieldId(102L)
                                .isUnion(true)
                                .union(Union.forSchema(2L))
                                .build())))
                .build();

        ExecuteStepContext executeStepContext =
                ExecuteStepContext.mockData(Arrays.asList(inputClients, inputCustomers), output);


        VavrAssert.assertCollectionFailure(
                pipelineStepExecutor.executeFullTransformation(mapStep, executeStepContext))
                .withOnlyFailures(
                        ErrorResponse.valueOf(
                                "Output fields required the following fields that were not found [Id(type=IntegerType)]",
                                "UNMAPPED_OUTPUT_FIELDS"),
                        ErrorResponse.valueOf(
                                "The following fields in the transformation were not mapped to any fields in the output"
                                        + " [Id(type=StringType)]",
                                "UNMAPPED_INPUT_FIELDS"));
    }

    @Test
    public void transformOutputFieldsIndividually_UnionStep_ReturnsValid() {
        Schema inputCustomers = Design.Populated.schema("CUSTOMERS").id(1L)
                .fields(newHashSet(
                        DesignField.Populated.longField("CustomerId").id(901L).build(),
                        DesignField.Populated.stringField("Name").id(902L).build(),
                        DesignField.Populated.doubleField("Salary").id(903L).build()))
                .build();

        Schema inputClients = Design.Populated.schema("CLIENTS").id(2L)
                .fields(newHashSet(
                        DesignField.Populated.intField("BusinessNumber").id(904L).build(),
                        DesignField.Populated.stringField("TaxCode").id(905L).build(),
                        DesignField.Populated.doubleField("Rate").id(906L).build()))
                .build();

        Schema output = Design.Populated.schema().id(3L)
                .fields(newHashSet(
                        DesignField.Populated.stringField("Id").id(101L).build(),
                        DesignField.Populated.doubleField("IncomeFlow").id(102L).build()))
                .build();

        PipelineUnionStep mapStep = Design.Populated.pipelineUnionStep()
                .selects(newLinkedHashSet(Arrays.asList(
                        Select.builder()
                                .order(1L)
                                .select("-1 * Salary")
                                .outputFieldId(102L)
                                .isUnion(true)
                                .union(Union.forSchema(1L))
                                .build(),
                        Select.builder()
                                .order(2L)
                                .select("Rate")
                                .outputFieldId(102L)
                                .isUnion(true)
                                .union(Union.forSchema(2L))
                                .build(),
                        Select.builder()
                                .order(0L)
                                .select("Concat(CustomerId, ':', Name)")
                                .outputFieldId(101L)
                                .isUnion(true)
                                .union(Union.forSchema(1L))
                                .build(),
                        Select.builder()
                                .order(0L)
                                .select("Concat(BusinessNumber, ':', TaxCode)")
                                .outputFieldId(101L)
                                .isUnion(true)
                                .union(Union.forSchema(2L))
                                .build())))
                .build();

        ExecuteStepContext executeStepContext =
                ExecuteStepContext.mockData(Arrays.asList(inputClients, inputCustomers), output);

        List<SelectResult> selectResults = pipelineStepExecutor.transformOutputFieldsIndividually(
                mapStep, executeStepContext);

        assertThat(selectResults)
                .filteredOn(selectResult -> !selectResult.isValid())
                .isEmpty();
    }

    @Test
    public void transformOutputFieldsIndividually_UnionStepInvalidSelectSyntax_ReturnsInValid() {
        Schema inputCustomers = Design.Populated.schema("CUSTOMERS").id(1L)
                .fields(newHashSet(
                        DesignField.Populated.longField("CustomerId").id(901L).build(),
                        DesignField.Populated.stringField("Name").id(902L).build(),
                        DesignField.Populated.doubleField("Salary").id(903L).build()))
                .build();

        Schema inputClients = Design.Populated.schema("CLIENTS").id(2L)
                .fields(newHashSet(
                        DesignField.Populated.intField("BusinessNumber").id(904L).build(),
                        DesignField.Populated.stringField("TaxCode").id(905L).build(),
                        DesignField.Populated.doubleField("Rate").id(906L).build()))
                .build();

        Schema output = Design.Populated.schema().id(3L)
                .fields(newHashSet(
                        DesignField.Populated.stringField("Id").id(101L).build(),
                        DesignField.Populated.doubleField("IncomeFlow").id(102L).build()))
                .build();

        PipelineUnionStep mapStep = Design.Populated.pipelineUnionStep()
                .selects(newLinkedHashSet(Arrays.asList(
                        Select.builder()
                                .order(0L)
                                .select("catsInMyHouse(CustomerId, ':', Name)")
                                .outputFieldId(101L)
                                .isUnion(true)
                                .union(Union.forSchema(1L))
                                .build(),
                        Select.builder()
                                .order(0L)
                                .select("Concat(BusinessNumber, ':', TaxCode)")
                                .outputFieldId(101L)
                                .isUnion(true)
                                .union(Union.forSchema(2L))
                                .build(),
                        Select.builder()
                                .order(1L)
                                .select("-1 * Salary")
                                .outputFieldId(102L)
                                .isUnion(true)
                                .union(Union.forSchema(1L))
                                .build(),
                        Select.builder()
                                .order(2L)
                                .select("Rate")
                                .outputFieldId(102L)
                                .isUnion(true)
                                .union(Union.forSchema(2L))
                                .build())))
                .build();

        ExecuteStepContext executeStepContext =
                ExecuteStepContext.mockData(Arrays.asList(inputClients, inputCustomers), output);

        List<SelectResult> selectResults = pipelineStepExecutor.transformOutputFieldsIndividually(
                mapStep, executeStepContext);

        assertThat(selectResults)
                .filteredOn(selectResult -> !selectResult.isValid())
                .extracting(SelectResult::getOutputFieldName, SelectResult::getErrors)
                .containsOnly(
                        tuple("Id", singletonList(ErrorResponse.valueOf(
                                "Undefined function: 'catsInMyHouse'. This function is neither a registered "
                                        + "temporary function nor a permanent function registered in the database "
                                        + "'default'.; line 1 pos 7",
                                "SQL_ANALYSIS_ERROR"))));
    }


    @Test
    public void executeFullTransformation_UnionStepWithDifferentSelectOrder_ReturnsValid() {
        Schema inputTrainers = Design.Populated.schema("TRAINERS").id(1L)
                .fields(newHashSet(
                        DesignField.Populated.stringField("FIRST_NAME").id(901L).nullable(true).maxLength(50).build(),
                        DesignField.Populated.stringField("LAST_NAME").id(902L).nullable(true).maxLength(50).build(),
                        DesignField.Populated.longField("CARD_NUMBER").id(903L).nullable(true).build(),
                        DesignField.Populated.dateField("BIRTH_DATE").id(904L).nullable(true).build()))
                .build();

        Schema inputStudents = Design.Populated.schema("STUDENTS").id(2L)
                .fields(newHashSet(
                        DesignField.Populated.stringField("FIRST_NAME").id(905L).nullable(true).maxLength(50).build(),
                        DesignField.Populated.stringField("LAST_NAME").id(906L).nullable(true).maxLength(50).build(),
                        DesignField.Populated.dateField("BIRTH_DATE").id(907L).nullable(true).build(),
                        DesignField.Populated.longField("CARD_NUMBER").id(908L).nullable(true).build()))
                .build();

        Schema inputCoaches = Design.Populated.schema("COACHES").id(3L)
                .fields(newHashSet(
                        DesignField.Populated.longField("CARD_NUMBER").id(909L).nullable(true).build(),
                        DesignField.Populated.stringField("FIRST_NAME").id(910L).nullable(true).maxLength(50).build(),
                        DesignField.Populated.stringField("LAST_NAME").id(911L).nullable(true).maxLength(50).build(),
                        DesignField.Populated.dateField("BIRTH_DATE").id(912L).nullable(true).build()))
                .build();

        Schema output = Design.Populated.schema("PERSONS").id(4L)
                .fields(newHashSet(
                        DesignField.Populated.stringField("FIRST_NAME").id(101L).nullable(true).maxLength(50).build(),
                        DesignField.Populated.stringField("LAST_NAME").id(102L).nullable(true).maxLength(50).build(),
                        DesignField.Populated.dateField("BIRTH_DATE").id(103L).nullable(true).build(),
                        DesignField.Populated.longField("CARD_NUMBER").id(104L).nullable(true).build()))
                .build();

        PipelineUnionStep mapStep = Design.Populated.pipelineUnionStep()
                .selects(newLinkedHashSet(Arrays.asList(
                        Select.builder().union(Union.forSchema(1L))
                                .order(0L).select("CARD_NUMBER").outputFieldId(104L).isUnion(true).build(),
                        Select.builder().union(Union.forSchema(1L))
                                .order(1L).select("LAST_NAME").outputFieldId(102L).isUnion(true)
                                .isIntermediate(true).build(),
                        Select.builder().union(Union.forSchema(1L))
                                .order(2L).select("FIRST_NAME").outputFieldId(101L).isUnion(true)
                                .isIntermediate(true).build(),
                        Select.builder().union(Union.forSchema(1L))
                                .order(3L).select("BIRTH_DATE").outputFieldId(103L).isUnion(true).build(),

                        Select.builder().union(Union.forSchema(2L))
                                .order(0L).select("CARD_NUMBER").outputFieldId(104L).isUnion(true).build(),
                        Select.builder().union(Union.forSchema(2L))
                                .order(1L).select("FIRST_NAME").outputFieldId(101L).isUnion(true).build(),
                        Select.builder().union(Union.forSchema(2L))
                                .order(2L).select("LAST_NAME").outputFieldId(102L).isUnion(true).build(),
                        Select.builder().union(Union.forSchema(2L))
                                .order(3L).select("BIRTH_DATE").outputFieldId(103L).isUnion(true).build(),

                        Select.builder().union(Union.forSchema(3L))
                                .order(0L).select("BIRTH_DATE").outputFieldId(103L).isUnion(true).build(),
                        Select.builder().union(Union.forSchema(3L))
                                .order(1L).select("FIRST_NAME").outputFieldId(101L).isUnion(true).build(),
                        Select.builder().union(Union.forSchema(3L))
                                .order(2L).select("LAST_NAME").outputFieldId(102L).isUnion(true).build(),
                        Select.builder().union(Union.forSchema(3L))
                                .order(3L).select("CARD_NUMBER").outputFieldId(104L).isUnion(true).build())))
                .build();

        ExecuteStepContext executeStepContext =
                ExecuteStepContext.mockData(Arrays.asList(inputStudents, inputTrainers, inputCoaches), output);

        VavrAssert.assertValid(pipelineStepExecutor.executeFullTransformation(mapStep, executeStepContext));
    }

    @Test
    public void executeFullTransformation_UnionStepWithIntermediateResults_ReturnsValid() {
        Schema input1 = Design.Populated.schema("INPUT_ONE").id(1L)
                .fields(newHashSet(
                        DesignField.Populated.intField("A").id(901L).build(),
                        DesignField.Populated.intField("B").id(902L).build(),
                        DesignField.Populated.intField("C").id(903L).build()))
                .build();

        Schema input2 = Design.Populated.schema("INPUT_TWO").id(2L)
                .fields(newHashSet(
                        DesignField.Populated.intField("A").id(904L).build(),
                        DesignField.Populated.intField("B").id(905L).build(),
                        DesignField.Populated.intField("C").id(906L).build()))
                .build();

        Schema output = Design.Populated.schema("OUTPUT").id(3L)
                .fields(newHashSet(
                        DesignField.Populated.intField("A").id(101L).build(),
                        DesignField.Populated.intField("B").id(102L).build(),
                        DesignField.Populated.intField("C").id(103L).build()))
                .build();

        PipelineUnionStep unionWithIntermediateResults = Design.Populated.pipelineUnionStep()
                .selects(newLinkedHashSet(Arrays.asList(
                        Design.Populated.select().union(Union.forSchema(1L)).isUnion(true)
                                .order(0L).select("A").outputFieldId(101L).isIntermediate(false).build(),
                        Design.Populated.select().union(Union.forSchema(1L)).isUnion(true)
                                .order(1L).select("C + 5").outputFieldId(103L).isIntermediate(true).build(),
                        Design.Populated.select().union(Union.forSchema(1L)).isUnion(true)
                                .order(2L).select("C * B").outputFieldId(102L).isIntermediate(false).build(),

                        Design.Populated.select().union(Union.forSchema(2L)).isUnion(true)
                                .order(0L).select("C * 3").outputFieldId(103L).isIntermediate(true).build(),
                        Design.Populated.select().union(Union.forSchema(2L)).isUnion(true)
                                .order(1L).select("C + B").outputFieldId(102L).isIntermediate(true).build(),
                        Design.Populated.select().union(Union.forSchema(2L)).isUnion(true)
                                .order(2L).select("B + A").outputFieldId(101L).isIntermediate(false).build()
                )))
                .build();

        Map<Schema, List<Row>> inputData = ImmutableMap.of(
                input1, singletonList(RowFactory.create(1, 2, 3)),
                input2, singletonList(RowFactory.create(1, 10, 3)));

        ExecuteStepContext context = ExecuteStepContext.realData(Arrays.asList(input1, input2), output, inputData);

        List<Row> result = VavrAssert
                .assertValid(pipelineStepExecutor.executeFullTransformation(unionWithIntermediateResults, context))
                .getResult()
                .collectAsList();

        assertThat(result)
                .extracting(row -> row.getAs("A"), row -> row.getAs("B"), row -> row.getAs("C"))
                .describedAs("OUTPUT(A, B, C)")
                .containsExactly(tuple(1, 16, 8), tuple(20, 19, 9));
    }

    @Test
    public void executeFullTransformation_SparkTransformationReturnsNullOutput_ReturnsValid() {
        Schema input = Design.Populated.schema()
                .fields(newHashSet(
                        DesignField.Populated.stringField("InputString1").id(1L).build(),
                        DesignField.Populated.stringField("InputString2").id(2L).build()))
                .build();

        Schema output = Design.Populated.schema()
                .fields(newHashSet(
                        DesignField.Populated.stringField("OutputString1").id(101L).nullable(true).build(),
                        DesignField.Populated.stringField("OutputString2").id(102L).nullable(true).build()))
                .build();

        PipelineMapStep step = Design.Populated.pipelineMapStep()
                .selects(newLinkedHashSet(Arrays.asList(
                        Select.builder()
                                .select("null")
                                .outputFieldId(101L)
                                .build(),
                        Select.builder()
                                .select("nullif('abc', 'abc')")
                                .outputFieldId(102L)
                                .build())))
                .build();

        ExecuteStepContext executeStepContext = ExecuteStepContext.mockData(singletonList(input), output);

        VavrAssert.assertValid(pipelineStepExecutor.executeFullTransformation(step, executeStepContext));
    }

    @Test
    public void transformOutputFieldsIndividually_SparkTransformationReturnsNullOutput_ReturnsValid() {
        Schema input = Design.Populated.schema()
                .fields(newHashSet(
                        DesignField.Populated.stringField("InputString1").id(1L).build(),
                        DesignField.Populated.stringField("InputString2").id(2L).build()))
                .build();

        Schema output = Design.Populated.schema()
                .fields(newHashSet(
                        DesignField.Populated.stringField("OutputString1").id(101L).nullable(true).build(),
                        DesignField.Populated.stringField("OutputString2").id(102L).nullable(true).build()))
                .build();

        PipelineMapStep step = Design.Populated.pipelineMapStep()
                .selects(newLinkedHashSet(Arrays.asList(
                        Select.builder()
                                .select("null")
                                .outputFieldId(101L)
                                .build(),
                        Select.builder()
                                .select("nullif('abc', 'abc')")
                                .outputFieldId(102L)
                                .build())))
                .build();

        ExecuteStepContext executeStepContext = ExecuteStepContext.mockData(singletonList(input), output);

        List<SelectResult> selectResults =
                pipelineStepExecutor.transformOutputFieldsIndividually(step, executeStepContext);

        assertThat(selectResults)
                .extracting(SelectResult::isValid)
                .containsOnly(true);
    }
}
