package com.lombardrisk.ignis.design.server.pipeline;

import com.lombardrisk.ignis.client.design.pipeline.SyntaxCheckRequest;
import com.lombardrisk.ignis.client.design.pipeline.error.SelectResult;
import com.lombardrisk.ignis.client.design.pipeline.error.StepExecutionResult;
import com.lombardrisk.ignis.client.design.schema.field.FieldDto;
import com.lombardrisk.ignis.client.external.pipeline.export.step.JoinType;
import com.lombardrisk.ignis.common.assertions.VavrAssert;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.design.field.DesignField;
import com.lombardrisk.ignis.design.field.FieldService;
import com.lombardrisk.ignis.design.field.model.Field;
import com.lombardrisk.ignis.design.server.fixtures.Design.Populated;
import com.lombardrisk.ignis.design.server.pipeline.model.ExecuteStepContext;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineStep;
import com.lombardrisk.ignis.design.server.pipeline.model.join.Join;
import com.lombardrisk.ignis.design.server.pipeline.model.join.JoinField;
import com.lombardrisk.ignis.design.server.pipeline.model.select.Order;
import com.lombardrisk.ignis.design.server.pipeline.model.select.PipelineFilter;
import com.lombardrisk.ignis.design.server.pipeline.model.select.Select;
import com.lombardrisk.ignis.design.server.pipeline.model.select.Union;
import com.lombardrisk.ignis.design.server.pipeline.model.select.Window;
import com.lombardrisk.ignis.design.server.productconfig.ProductConfig;
import com.lombardrisk.ignis.design.server.productconfig.ProductConfigService;
import com.lombardrisk.ignis.design.server.productconfig.fixture.ProductServiceFixtureFactory;
import com.lombardrisk.ignis.design.server.productconfig.fixture.SchemaServiceFixtureFactory;
import com.lombardrisk.ignis.design.server.productconfig.schema.Schema;
import com.lombardrisk.ignis.design.server.productconfig.schema.SchemaService;
import io.vavr.control.Validation;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static com.lombardrisk.ignis.design.field.DesignField.Populated.intFieldRequest;
import static com.lombardrisk.ignis.design.field.DesignField.Populated.longFieldRequest;
import static com.lombardrisk.ignis.design.field.DesignField.Populated.stringFieldRequest;
import static com.lombardrisk.ignis.design.server.fixtures.Design.Populated.unionSelect;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class PipelineStepSelectsValidatorTest {

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    private SchemaService schemaService;
    private FieldService fieldService;
    private PipelineStepSelectsValidator selectsValidator;
    private PipelineStepExecutor pipelineStepExecutor;
    private SparkSession sparkSession;
    private ProductConfig product;

    @Captor
    private ArgumentCaptor<Map<Schema, List<Row>>> inputDataCaptor;
    @Captor
    private ArgumentCaptor<PipelineStep> pipelineStepArgumentCaptor;
    @Captor
    private ArgumentCaptor<ExecuteStepContext> executionContextCaptor;
    @Captor
    private ArgumentCaptor<Set<Field>> fieldsCaptor;

    @Before
    public void setUp() {
        ProductServiceFixtureFactory productServiceFactory = ProductServiceFixtureFactory.create();
        SchemaServiceFixtureFactory schemaServiceFactory
                = SchemaServiceFixtureFactory.create();

        ProductConfigService productConfigService = productServiceFactory.getProductService();
        schemaService = schemaServiceFactory.getSchemaService();
        fieldService = schemaServiceFactory.getFieldService();

        pipelineStepExecutor = mock(PipelineStepExecutor.class);
        when(pipelineStepExecutor.executeFullTransformation(any(PipelineStep.class), any()))
                .thenReturn(Validation.valid(mock(Dataset.class)));

        selectsValidator =
                new PipelineStepSelectsValidator(schemaService, pipelineStepExecutor, mock(SparkUDFService.class));

        product = VavrAssert.assertValid(productConfigService.createProductConfig(
                Populated.newProductRequest().build()))
                .getResult();
    }

    @Test
    public void validate_ValidSelects_ReturnsNoErrorResponse() {
        Schema schemaIn = VavrAssert.assertValid(schemaService.validateAndCreateNew(
                Populated.schema()
                        .productId(product.getId())
                        .displayName("MY_FIRST_SCHEMA")
                        .physicalTableName("MYFS")
                        .build())).getResult();

        Schema schemaOut = VavrAssert.assertValid(schemaService.validateAndCreateNew(
                Populated.schema()
                        .productId(product.getId())
                        .displayName("MY_SECOND_SCHEMA")
                        .physicalTableName("MYSS")
                        .build())).getResult();

        FieldDto schemaInNameField = fieldService.save(schemaIn.getId(), stringFieldRequest("NAME").build()).get();
        FieldDto schemaInVersionField = fieldService.save(schemaIn.getId(), longFieldRequest("VERSION").build()).get();
        FieldDto schemaOutVersionField =
                fieldService.save(schemaOut.getId(), longFieldRequest("NAME_VERSION").build()).get();

        Set<Select> selects = singleton(Populated.select()
                .outputFieldId(schemaOutVersionField.getId())
                .select("CONCAT(NAME, VERSION)")
                .build());

        PipelineStep step = Populated.pipelineMapStep()
                .schemaInId(schemaIn.getId())
                .schemaOutId(schemaOut.getId())
                .selects(selects)
                .build();

        StepExecutionResult error = selectsValidator.validate(step);

        assertThat(error.isSuccessful()).isTrue();
    }

    @Test
    public void validate_SchemaInDoesNotExist_ReturnsErrorResponse() {
        Schema schemaOut = VavrAssert.assertValid(schemaService.validateAndCreateNew(
                Populated.schema()
                        .productId(product.getId())
                        .displayName("MY_SECOND_SCHEMA")
                        .physicalTableName("MYSS")
                        .build())).getResult();

        PipelineStep step = Populated.pipelineMapStep()
                .schemaInId(123456L)
                .schemaOutId(schemaOut.getId())
                .build();

        StepExecutionResult error = selectsValidator.validate(step);

        assertThat(error.isSuccessful()).isFalse();
        assertThat(error.getErrors().get(0))
                .isEqualTo(CRUDFailure.notFoundIds(Schema.class.getSimpleName(), 123456L).toErrorResponse());
    }

    @Test
    public void validate_SchemaOutDoesNotExist_ReturnsErrorResponse() {
        Schema schemaIn = VavrAssert.assertValid(schemaService.validateAndCreateNew(
                Populated.schema()
                        .productId(product.getId())
                        .displayName("MY_FIRST_SCHEMA")
                        .physicalTableName("MYFS")
                        .build())).getResult();

        PipelineStep step = Populated.pipelineMapStep()
                .schemaInId(schemaIn.getId())
                .schemaOutId(654321L)
                .build();

        StepExecutionResult error = selectsValidator.validate(step);

        assertThat(error.isSuccessful()).isFalse();
        assertThat(error.getErrors().get(0))
                .isEqualTo(CRUDFailure.notFoundIds(Schema.class.getSimpleName(), 654321L).toErrorResponse());
    }

    @Test
    public void validate_MapTransformation_CallsStepSelectsExecutorWithInputData() {
        when(pipelineStepExecutor.executeFullTransformation(any(), any()))
                .thenReturn(Validation.valid(null));

        Schema schemaIn = VavrAssert.assertValid(schemaService.validateAndCreateNew(
                Populated.schema()
                        .productId(product.getId())
                        .displayName("MY_FIRST_SCHEMA")
                        .physicalTableName("MYFS")
                        .build())).getResult();

        Schema schemaOut = VavrAssert.assertValid(schemaService.validateAndCreateNew(
                Populated.schema()
                        .productId(product.getId())
                        .displayName("MY_SECOND_SCHEMA")
                        .physicalTableName("MYSS")
                        .build())).getResult();

        FieldDto schemaInNameField = fieldService.save(schemaIn.getId(), stringFieldRequest("NAME").build()).get();
        FieldDto schemaInVersionField = fieldService.save(schemaIn.getId(), longFieldRequest("VERSION").build()).get();
        FieldDto schemaOutVersionField =
                fieldService.save(schemaOut.getId(), longFieldRequest("NAME_VERSION").build()).get();
        FieldDto constantOutField =
                fieldService.save(schemaOut.getId(), stringFieldRequest("CONST").build()).get();

        Set<Select> selects = newLinkedHashSet(Arrays.asList(
                Populated.select()
                        .outputFieldId(schemaOutVersionField.getId())
                        .select("CONCAT(NAME, VERSION)")
                        .build(),
                Populated.select()
                        .outputFieldId(constantOutField.getId())
                        .select("'ConstantColumn'")
                        .build()));

        PipelineStep step = Populated.pipelineMapStep()
                .schemaInId(schemaIn.getId())
                .schemaOutId(schemaOut.getId())
                .selects(selects)
                .build();

        selectsValidator.validateWithIndividualErrors(step);

        Set<Field> fields = schemaOut.getFields();
        verify(pipelineStepExecutor)
                .executeFullTransformation(pipelineStepArgumentCaptor.capture(), executionContextCaptor.capture());

        assertThat(pipelineStepArgumentCaptor.getValue().getSelects())
                .extracting(Select::getSelect)
                .containsExactly(
                        "CONCAT(NAME, VERSION)",
                        "'ConstantColumn'");

        Map<Schema, List<Row>> inputData = executionContextCaptor.getValue().getExecutionInputData();
        assertThat(inputData)
                .containsOnlyKeys(schemaIn);
    }

    @Test
    public void validate_MapWithError_ReturnsStepExecutionResultWithErrors() {
        Schema schemaIn = VavrAssert.assertValid(schemaService.validateAndCreateNew(
                Populated.schema()
                        .productId(product.getId())
                        .displayName("MY_FIRST_SCHEMA")
                        .physicalTableName("MYFS")
                        .build())).getResult();

        Schema schemaOut = VavrAssert.assertValid(schemaService.validateAndCreateNew(
                Populated.schema()
                        .productId(product.getId())
                        .displayName("MY_SECOND_SCHEMA")
                        .physicalTableName("MYSS")
                        .build())).getResult();

        FieldDto schemaInNameField = fieldService.save(schemaIn.getId(), stringFieldRequest("NAME").build()).get();
        FieldDto schemaInVersionField = fieldService.save(schemaIn.getId(), longFieldRequest("VERSION").build()).get();
        FieldDto schemaOutVersionField =
                fieldService.save(schemaOut.getId(), longFieldRequest("NAME_VERSION").build()).get();
        FieldDto constantOutField =
                fieldService.save(schemaOut.getId(), stringFieldRequest("CONST").build()).get();

        Set<Select> selects = newLinkedHashSet(Arrays.asList(
                Populated.select()
                        .id(912812L)
                        .outputFieldId(schemaOutVersionField.getId())
                        .select("CONCAT(NAME, VERSION)")
                        .build(),
                Populated.select()
                        .outputFieldId(constantOutField.getId())
                        .select("'ConstantColumn'")
                        .build()));

        PipelineStep step = Populated.pipelineMapStep()
                .schemaInId(schemaIn.getId())
                .schemaOutId(schemaOut.getId())
                .selects(selects)
                .build();

        Set<Field> fields = VavrAssert.assertValid(schemaService.findWithValidation(schemaOut.getId()))
                .getResult()
                .getFields();
        Field schemaOutFirstField = get(fields, 0);
        Set<Field> justFirstField = singleton(schemaOutFirstField);

        when(pipelineStepExecutor.transformOutputFieldsIndividually(any(), any()))
                .thenReturn(singletonList(SelectResult.error(
                        "NAME_VERSION",
                        schemaOutFirstField.getId(),
                        null,
                        singletonList(ErrorResponse.valueOf("Ooops", "OP")))));

        StepExecutionResult stepExecutionResult = selectsValidator.validateWithIndividualErrors(step);
        assertThat(stepExecutionResult.isSuccessful())
                .isFalse();

        assertThat(stepExecutionResult.getSelectsExecutionErrors().getIndividualErrors())
                .containsExactly(
                        SelectResult.error(
                                "NAME_VERSION",
                                schemaOutFirstField.getId(),
                                null,
                                singletonList(ErrorResponse.valueOf("Ooops", "OP"))));
    }

    @Test
    public void validate_UnionTransformation_CallsStepSelectsExecutorWithSql() {
        Schema aSchema = VavrAssert.assertValid(schemaService.validateAndCreateNew(
                Populated.schema()
                        .productId(product.getId())
                        .displayName("SCHEMA_A")
                        .physicalTableName("A")
                        .build())).getResult();

        Schema bSchema = VavrAssert.assertValid(schemaService.validateAndCreateNew(
                Populated.schema()
                        .productId(product.getId())
                        .displayName("SCHEMA_B")
                        .physicalTableName("B")
                        .build())).getResult();

        Schema cSchema = VavrAssert.assertValid(schemaService.validateAndCreateNew(
                Populated.schema()
                        .productId(product.getId())
                        .displayName("SCHEMA_C")
                        .physicalTableName("C")
                        .build())).getResult();

        fieldService.save(aSchema.getId(), longFieldRequest("A_ID").build()).get();
        fieldService.save(aSchema.getId(), stringFieldRequest("A_NAME").build()).get();
        fieldService.save(bSchema.getId(), longFieldRequest("B_ID").build()).get();
        fieldService.save(bSchema.getId(), stringFieldRequest("B_NAME").build()).get();
        FieldDto outputIdField = fieldService.save(cSchema.getId(), longFieldRequest("ID").build()).get();
        FieldDto outputNameField = fieldService.save(cSchema.getId(), stringFieldRequest("NAME").build()).get();

        PipelineStep step = Populated.pipelineUnionStep()
                .schemaOutId(cSchema.getId())
                .schemaInIds(newHashSet(aSchema.getId(), bSchema.getId()))
                .filters(newLinkedHashSet(asList(
                        PipelineFilter.builder()
                                .unionSchemaId(aSchema.getId())
                                .filter("A_NOTHER_FIELD > 2")
                                .build(),
                        PipelineFilter.builder()
                                .unionSchemaId(bSchema.getId())
                                .filter("B_MY_FRIEND = true")
                                .build())))
                .selects(newLinkedHashSet(asList(
                        unionSelect()
                                .outputFieldId(outputIdField.getId())
                                .union(Union.forSchema(aSchema.getId()))
                                .select("A_ID")
                                .build(),
                        unionSelect()
                                .outputFieldId(outputNameField.getId())
                                .union(Union.forSchema(aSchema.getId()))
                                .select("A_NAME")
                                .build(),
                        unionSelect()
                                .outputFieldId(outputIdField.getId())
                                .union(Union.forSchema(bSchema.getId()))
                                .select("B_ID")
                                .build(),
                        unionSelect()
                                .outputFieldId(outputNameField.getId())
                                .union(Union.forSchema(bSchema.getId()))
                                .select("CONCAT('TEST_', B_NAME)")
                                .build())))
                .build();

        selectsValidator.validate(step);

        verify(pipelineStepExecutor)
                .executeFullTransformation(pipelineStepArgumentCaptor.capture(), executionContextCaptor.capture());

        assertThat(pipelineStepArgumentCaptor.getValue())
                .isEqualTo(step);

        ExecuteStepContext stepContext = executionContextCaptor.getValue();
        assertThat(stepContext.getExecutionInputData())
                .containsKeys(aSchema, bSchema);
        assertThat(stepContext.getInputSchemas())
                .containsExactly(aSchema, bSchema);
        assertThat(stepContext.getOutputSchema())
                .isEqualTo(cSchema);
    }

    @Test
    public void validate_UnionTransformationWithErrorInSelect_ReturnsError() {
        Schema aSchema = VavrAssert.assertValid(schemaService.validateAndCreateNew(
                Populated.schema()
                        .productId(product.getId())
                        .displayName("SCHEMA_A")
                        .physicalTableName("A")
                        .build())).getResult();

        Schema bSchema = VavrAssert.assertValid(schemaService.validateAndCreateNew(
                Populated.schema()
                        .productId(product.getId())
                        .displayName("SCHEMA_B")
                        .physicalTableName("B")
                        .build())).getResult();

        Schema cSchema = VavrAssert.assertValid(schemaService.validateAndCreateNew(
                Populated.schema()
                        .productId(product.getId())
                        .displayName("SCHEMA_C")
                        .physicalTableName("C")
                        .build())).getResult();

        fieldService.save(aSchema.getId(), longFieldRequest("A_ID").build()).get();
        fieldService.save(aSchema.getId(), stringFieldRequest("A_NAME").build()).get();
        fieldService.save(bSchema.getId(), longFieldRequest("B_ID").build()).get();
        fieldService.save(bSchema.getId(), stringFieldRequest("B_NAME").build()).get();
        FieldDto outputIdField = fieldService.save(cSchema.getId(), longFieldRequest("ID").build()).get();
        FieldDto outputNameField = fieldService.save(cSchema.getId(), stringFieldRequest("NAME").build()).get();

        PipelineStep step = Populated.pipelineUnionStep()
                .schemaOutId(cSchema.getId())
                .schemaInIds(newHashSet(aSchema.getId(), bSchema.getId()))
                .filters(newLinkedHashSet(asList(
                        PipelineFilter.builder()
                                .unionSchemaId(aSchema.getId())
                                .filter("A_NOTHER_FIELD > 2")
                                .build(),
                        PipelineFilter.builder()
                                .unionSchemaId(bSchema.getId())
                                .filter("B_MY_FRIEND = true")
                                .build())))
                .selects(newLinkedHashSet(asList(
                        unionSelect()
                                .outputFieldId(outputIdField.getId())
                                .union(Union.forSchema(aSchema.getId()))
                                .select("A_ID")
                                .build(),
                        unionSelect()
                                .outputFieldId(outputNameField.getId())
                                .union(Union.forSchema(aSchema.getId()))
                                .select("A_NAME")
                                .build(),
                        unionSelect()
                                .outputFieldId(outputIdField.getId())
                                .union(Union.forSchema(bSchema.getId()))
                                .select("B_ID")
                                .build(),
                        unionSelect()
                                .id(8291L)
                                .outputFieldId(outputNameField.getId())
                                .union(Union.forSchema(bSchema.getId()))
                                .select("CONCAT('TEST_', B_NAME)")
                                .build())))
                .build();

        when(pipelineStepExecutor.transformOutputFieldsIndividually(any(), any()))
                .thenReturn(singletonList(SelectResult.error(
                        "NAME",
                        outputNameField.getId(),
                        bSchema.getId(),
                        singletonList(ErrorResponse.valueOf("Ooops", "OP")))));

        StepExecutionResult stepExecutionResult = selectsValidator.validateWithIndividualErrors(step);
        assertThat(stepExecutionResult.isSuccessful())
                .isFalse();

        assertThat(stepExecutionResult.getSelectsExecutionErrors().getIndividualErrors())
                .containsExactly(
                        SelectResult.error(
                                "NAME",
                                outputNameField.getId(),
                                bSchema.getId(),
                                singletonList(ErrorResponse.valueOf("Ooops", "OP"))));
    }

    @Test
    public void validate_AggregateTransformation_CallsStepSelectsExecutorWithSql() {
        Schema schemaIn = VavrAssert.assertValid(schemaService.validateAndCreateNew(
                Populated.schema()
                        .productId(product.getId())
                        .displayName("MY_FIRST_SCHEMA")
                        .physicalTableName("MYFS")
                        .fields(newHashSet(
                                DesignField.Populated.stringField("NAME").build(),
                                DesignField.Populated.longField("AMOUNT").build()))
                        .build())).getResult();

        Schema schemaOut = VavrAssert.assertValid(schemaService.validateAndCreateNew(
                Populated.schema()
                        .productId(product.getId())
                        .displayName("MY_SECOND_SCHEMA")
                        .physicalTableName("MYSS")
                        .fields(newHashSet(
                                DesignField.Populated.stringField("NAME").build(),
                                DesignField.Populated.longField("TOTAL_AMOUNT").build()))
                        .build())).getResult();

        FieldDto schemaInNameField = fieldService.save(schemaIn.getId(), stringFieldRequest("NAME").build()).get();
        FieldDto schemaInAmountField = fieldService.save(schemaIn.getId(), longFieldRequest("AMOUNT").build()).get();
        FieldDto schemaOutNameField = fieldService.save(schemaOut.getId(), longFieldRequest("NAME").build()).get();
        FieldDto schemaOutAmountField =
                fieldService.save(schemaOut.getId(), longFieldRequest("TOTAL_AMOUNT").build()).get();

        Set<Select> selects = newHashSet(
                Populated.select()
                        .outputFieldId(schemaOutNameField.getId())
                        .select("NAME")
                        .build(),
                Populated.select()
                        .outputFieldId(schemaOutAmountField.getId())
                        .select("SUM(AMOUNT)")
                        .build());

        Set<String> groupings = newHashSet("NAME");

        PipelineStep step = Populated.pipelineAggregationStep()
                .schemaInId(schemaIn.getId())
                .schemaOutId(schemaOut.getId())
                .selects(selects)
                .groupings(groupings)
                .build();

        ArgumentCaptor<String> sqlArgument = ArgumentCaptor.forClass(String.class);

        selectsValidator.validate(step);

        verify(pipelineStepExecutor).executeFullTransformation(eq(step), executionContextCaptor.capture());

        ExecuteStepContext stepContext = executionContextCaptor.getValue();
        assertThat(stepContext.getExecutionInputData())
                .containsKeys(schemaIn);
        assertThat(stepContext.getInputSchemas())
                .contains(schemaIn);
        assertThat(stepContext.getOutputSchema())
                .isEqualTo(schemaOut);
    }

    @Test
    public void validate_AggregateTransformation_CallsStepSelectsExecutorWithInputData() {
        Schema schemaIn = VavrAssert.assertValid(schemaService.validateAndCreateNew(
                Populated.schema()
                        .productId(product.getId())
                        .displayName("MY_FIRST_SCHEMA")
                        .physicalTableName("MYFS")
                        .build())).getResult();

        FieldDto schemaInNameField = fieldService.save(schemaIn.getId(), stringFieldRequest("NAME").build()).get();
        FieldDto schemaInAmountField = fieldService.save(schemaIn.getId(), longFieldRequest("AMOUNT").build()).get();

        Schema schemaOut = VavrAssert.assertValid(schemaService.validateAndCreateNew(
                Populated.schema()
                        .productId(product.getId())
                        .displayName("MY_SECOND_SCHEMA")
                        .physicalTableName("MYSS")
                        .build())).getResult();

        FieldDto schemaOutNameField = fieldService.save(schemaOut.getId(), stringFieldRequest("NAME").build()).get();
        FieldDto schemaOutAmountField = fieldService.save(schemaOut.getId(), longFieldRequest("AMOUNT").build()).get();

        Set<Select> selects = newHashSet(
                Populated.select()
                        .outputFieldId(schemaOutNameField.getId())
                        .select("NAME")
                        .build(),
                Populated.select()
                        .outputFieldId(schemaOutAmountField.getId())
                        .select("SUM(AMOUNT)")
                        .build());

        Set<String> groupings = newHashSet("NAME");

        PipelineStep step = Populated.pipelineAggregationStep()
                .schemaInId(schemaIn.getId())
                .schemaOutId(schemaOut.getId())
                .selects(selects)
                .groupings(groupings)
                .build();

        selectsValidator.validate(step);
        verify(pipelineStepExecutor).transformOutputFieldsIndividually(
                eq(step),
                executionContextCaptor.capture());

        Map<Schema, List<Row>> inputData = executionContextCaptor.getValue().getExecutionInputData();
        assertThat(inputData)
                .containsOnlyKeys(schemaIn);
    }

    @Test
    public void validate_AggregateTransformationWithError_ReturnsIndividualSelectError() {
        Schema schemaIn = VavrAssert.assertValid(schemaService.validateAndCreateNew(
                Populated.schema()
                        .productId(product.getId())
                        .displayName("MY_FIRST_SCHEMA")
                        .physicalTableName("MYFS")
                        .build())).getResult();

        FieldDto schemaInNameField = fieldService.save(schemaIn.getId(), stringFieldRequest("NAME").build()).get();
        FieldDto schemaInAmountField = fieldService.save(schemaIn.getId(), longFieldRequest("AMOUNT").build()).get();

        Schema schemaOut = VavrAssert.assertValid(schemaService.validateAndCreateNew(
                Populated.schema()
                        .productId(product.getId())
                        .displayName("MY_SECOND_SCHEMA")
                        .physicalTableName("MYSS")
                        .build())).getResult();

        FieldDto schemaOutNameField = fieldService.save(schemaOut.getId(), stringFieldRequest("NAME").build()).get();
        FieldDto schemaOutAmountField = fieldService.save(schemaOut.getId(), longFieldRequest("AMOUNT").build()).get();

        Set<Select> selects = newHashSet(
                Populated.select()
                        .outputFieldId(schemaOutNameField.getId())
                        .select("NAME")
                        .build(),
                Populated.select()
                        .outputFieldId(schemaOutAmountField.getId())
                        .select("NOT_AN_AGG(AMOUNT)")
                        .build());

        Set<String> groupings = newHashSet("NAME");

        PipelineStep step = Populated.pipelineAggregationStep()
                .schemaInId(schemaIn.getId())
                .schemaOutId(schemaOut.getId())
                .selects(selects)
                .groupings(groupings)
                .build();

        when(pipelineStepExecutor.transformOutputFieldsIndividually(any(), any()))
                .thenReturn(singletonList(SelectResult.error(
                        "AMOUNT",
                        schemaOutAmountField.getId(), null,
                        singletonList(ErrorResponse.valueOf("Ooops", "OP")))));

        StepExecutionResult stepExecutionResult = selectsValidator.validateWithIndividualErrors(step);
        assertThat(stepExecutionResult.isSuccessful())
                .isFalse();

        assertThat(stepExecutionResult.getSelectsExecutionErrors().getIndividualErrors())
                .containsExactly(
                        SelectResult.error(
                                "AMOUNT",
                                schemaOutAmountField.getId(),
                                null,
                                singletonList(ErrorResponse.valueOf("Ooops", "OP"))));
    }

    @Test
    public void validate_JoinTransformation_CallsStepExecutorWithStepAndContezt() {
        Schema left = VavrAssert.assertValid(schemaService.validateAndCreateNew(
                Populated.schema()
                        .productId(product.getId())
                        .displayName("Left Table")
                        .physicalTableName("LEFT")
                        .build())).getResult();

        FieldDto leftNameField = fieldService.save(left.getId(), stringFieldRequest("NAME").build()).get();
        FieldDto leftIdField = fieldService.save(left.getId(), longFieldRequest("ID").build()).get();

        Schema right = VavrAssert.assertValid(schemaService.validateAndCreateNew(
                Populated.schema()
                        .productId(product.getId())
                        .displayName("Right Table")
                        .physicalTableName("RIGHT")
                        .fields(newHashSet(
                                DesignField.Populated.stringField("DESCRIPTION").build(),
                                DesignField.Populated.longField("ID").build()))
                        .build())).getResult();

        FieldDto rightDescriptionField =
                fieldService.save(right.getId(), stringFieldRequest("DESCRIPTION").build()).get();
        FieldDto rightIdField = fieldService.save(right.getId(), longFieldRequest("ID").build()).get();

        Schema schemaOut = VavrAssert.assertValid(schemaService.validateAndCreateNew(
                Populated.schema()
                        .productId(product.getId())
                        .displayName("MY_SECOND_SCHEMA")
                        .physicalTableName("MYSS")
                        .fields(newHashSet(
                                DesignField.Populated.stringField("NAME").build(),
                                DesignField.Populated.stringField("DESCRIPTION").build()))
                        .build())).getResult();

        FieldDto schemaOutNameField = fieldService.save(schemaOut.getId(), stringFieldRequest("NAME").build()).get();
        FieldDto schemaOutDescriptionField =
                fieldService.save(schemaOut.getId(), stringFieldRequest("DESCRIPTION").build()).get();

        Set<Select> selects = newLinkedHashSet(asList(
                Populated.select()
                        .outputFieldId(schemaOutNameField.getId())
                        .select("NAME")
                        .build(),
                Populated.select()
                        .outputFieldId(schemaOutDescriptionField.getId())
                        .select("DESCRIPTION")
                        .build()));

        PipelineStep step = Populated.pipelineJoinStep()
                .schemaOutId(schemaOut.getId())
                .selects(selects)
                .joins(newHashSet(Join.builder()
                        .leftSchemaId(left.getId())
                        .rightSchemaId(right.getId())
                        .joinType(JoinType.FULL_OUTER)
                        .joinFields(newHashSet(
                                JoinField.builder()
                                        .leftJoinFieldId(leftIdField.getId())
                                        .rightJoinFieldId(rightIdField.getId())
                                        .build(),
                                JoinField.builder()
                                        .leftJoinFieldId(leftNameField.getId())
                                        .rightJoinFieldId(rightDescriptionField.getId())
                                        .build()))
                        .build()))
                .build();

        ArgumentCaptor<String> sqlArgument = ArgumentCaptor.forClass(String.class);

        selectsValidator.validate(step);

        verify(pipelineStepExecutor).executeFullTransformation(eq(step), executionContextCaptor.capture());

        ExecuteStepContext stepContext = executionContextCaptor.getValue();
        assertThat(stepContext.getExecutionInputData())
                .containsKeys(left, right);
        assertThat(stepContext.getInputSchemas())
                .contains(left, right);
        assertThat(stepContext.getOutputSchema())
                .isEqualTo(schemaOut);
    }

    @Test
    public void validate_JoinTransformation_CallsIndividualSelectValidationWithStepAndContext() {
        Schema left = VavrAssert.assertValid(schemaService.validateAndCreateNew(
                Populated.schema()
                        .productId(product.getId())
                        .displayName("Left Table")
                        .physicalTableName("LEFT")
                        .build())).getResult();

        FieldDto leftNameField = fieldService.save(left.getId(), stringFieldRequest("NAME").build()).get();
        FieldDto leftIdField = fieldService.save(left.getId(), longFieldRequest("ID").build()).get();

        Schema right = VavrAssert.assertValid(schemaService.validateAndCreateNew(
                Populated.schema()
                        .productId(product.getId())
                        .displayName("Right Table")
                        .physicalTableName("RIGHT")
                        .fields(newHashSet(
                                DesignField.Populated.stringField("DESCRIPTION").build(),
                                DesignField.Populated.longField("ID").build()))
                        .build())).getResult();

        FieldDto rightDescriptionField =
                fieldService.save(right.getId(), stringFieldRequest("DESCRIPTION").build()).get();
        FieldDto rightIdField = fieldService.save(right.getId(), longFieldRequest("ID").build()).get();

        Schema schemaOut = VavrAssert.assertValid(schemaService.validateAndCreateNew(
                Populated.schema()
                        .productId(product.getId())
                        .displayName("MY_SECOND_SCHEMA")
                        .physicalTableName("MYSS")
                        .fields(newHashSet(
                                DesignField.Populated.stringField("NAME").build(),
                                DesignField.Populated.stringField("DESCRIPTION").build()))
                        .build())).getResult();

        FieldDto schemaOutNameField = fieldService.save(schemaOut.getId(), stringFieldRequest("NAME").build()).get();
        FieldDto schemaOutDescriptionField =
                fieldService.save(schemaOut.getId(), stringFieldRequest("DESCRIPTION").build()).get();

        Set<Select> selects = newHashSet(
                Populated.select()
                        .outputFieldId(schemaOutNameField.getId())
                        .select("NAME")
                        .build(),
                Populated.select()
                        .outputFieldId(schemaOutDescriptionField.getId())
                        .select("DESCRIPTION")
                        .build());

        PipelineStep step = Populated.pipelineJoinStep()
                .schemaOutId(schemaOut.getId())
                .selects(selects)
                .joins(newHashSet(Join.builder()
                        .leftSchemaId(left.getId())
                        .rightSchemaId(right.getId())
                        .joinType(JoinType.FULL_OUTER)
                        .joinFields(newHashSet(
                                JoinField.builder()
                                        .leftJoinFieldId(leftIdField.getId())
                                        .rightJoinFieldId(rightIdField.getId())
                                        .build(),
                                JoinField.builder()
                                        .leftJoinFieldId(leftNameField.getId())
                                        .rightJoinFieldId(rightDescriptionField.getId())
                                        .build()))
                        .build()))
                .build();

        ArgumentCaptor<String> sqlArgument = ArgumentCaptor.forClass(String.class);

        selectsValidator.validate(step);

        verify(pipelineStepExecutor).transformOutputFieldsIndividually(eq(step), executionContextCaptor.capture());

        ExecuteStepContext stepContext = executionContextCaptor.getValue();
        assertThat(stepContext.getExecutionInputData())
                .containsKeys(left, right);
        assertThat(stepContext.getInputSchemas())
                .contains(left, right);
        assertThat(stepContext.getOutputSchema())
                .isEqualTo(schemaOut);
    }

    @Test
    public void validate_JoinTransformationWithError_ReturnsErrorWithIndividualErrors() {
        Schema left = VavrAssert.assertValid(schemaService.validateAndCreateNew(
                Populated.schema()
                        .productId(product.getId())
                        .displayName("Left Table")
                        .physicalTableName("LEFT")
                        .build())).getResult();

        FieldDto leftNameField = fieldService.save(left.getId(), stringFieldRequest("NAME").build()).get();
        FieldDto leftIdField = fieldService.save(left.getId(), longFieldRequest("ID").build()).get();

        Schema right = VavrAssert.assertValid(schemaService.validateAndCreateNew(
                Populated.schema()
                        .productId(product.getId())
                        .displayName("Right Table")
                        .physicalTableName("RIGHT")
                        .fields(newHashSet(
                                DesignField.Populated.stringField("DESCRIPTION").build(),
                                DesignField.Populated.longField("ID").build()))
                        .build())).getResult();

        FieldDto rightDescriptionField =
                fieldService.save(right.getId(), stringFieldRequest("DESCRIPTION").build()).get();
        FieldDto rightIdField = fieldService.save(right.getId(), longFieldRequest("ID").build()).get();

        Schema schemaOut = VavrAssert.assertValid(schemaService.validateAndCreateNew(
                Populated.schema()
                        .productId(product.getId())
                        .displayName("MY_SECOND_SCHEMA")
                        .physicalTableName("MYSS")
                        .fields(newHashSet(
                                DesignField.Populated.stringField("NAME").build(),
                                DesignField.Populated.stringField("DESCRIPTION").build()))
                        .build())).getResult();

        FieldDto schemaOutNameField = fieldService.save(schemaOut.getId(), stringFieldRequest("NAME").build()).get();
        FieldDto schemaOutDescriptionField =
                fieldService.save(schemaOut.getId(), stringFieldRequest("DESCRIPTION").build()).get();

        Set<Select> selects = newHashSet(
                Populated.select()
                        .outputFieldId(schemaOutNameField.getId())
                        .select("NAME")
                        .build(),
                Populated.select()
                        .outputFieldId(schemaOutDescriptionField.getId())
                        .select("DESCRIPTION + er_123_noT_exist")
                        .build());

        PipelineStep step = Populated.pipelineJoinStep()
                .schemaOutId(schemaOut.getId())
                .selects(selects)
                .joins(newHashSet(Join.builder()
                        .leftSchemaId(left.getId())
                        .rightSchemaId(right.getId())
                        .joinType(JoinType.FULL_OUTER)
                        .joinFields(newHashSet(
                                JoinField.builder()
                                        .leftJoinFieldId(leftIdField.getId())
                                        .rightJoinFieldId(rightIdField.getId())
                                        .build(),
                                JoinField.builder()
                                        .leftJoinFieldId(leftNameField.getId())
                                        .rightJoinFieldId(rightDescriptionField.getId())
                                        .build()))
                        .build()))
                .build();

        when(pipelineStepExecutor.transformOutputFieldsIndividually(any(), any()))
                .thenReturn(singletonList(SelectResult.error(
                        "DESCRIPTION",
                        schemaOutDescriptionField.getId(),
                        null,
                        singletonList(ErrorResponse.valueOf("Ooops", "OP")))));

        StepExecutionResult stepExecutionResult = selectsValidator.validateWithIndividualErrors(step);
        assertThat(stepExecutionResult.isSuccessful())
                .isFalse();

        assertThat(stepExecutionResult.getSelectsExecutionErrors().getIndividualErrors())
                .containsExactly(
                        SelectResult.error(
                                "DESCRIPTION",
                                schemaOutDescriptionField.getId(),
                                null,
                                singletonList(ErrorResponse.valueOf("Ooops", "OP"))));
    }

    @Test
    public void validate_WindowTransformation_CallsExecutorWithStepAndExecutionContext() {
        Schema schemaIn = VavrAssert.assertValid(schemaService.validateAndCreateNew(
                Populated.schema()
                        .productId(product.getId())
                        .displayName("MY_FIRST_SCHEMA")
                        .physicalTableName("MYFS")
                        .build())).getResult();

        FieldDto schemaInNameField = fieldService.save(schemaIn.getId(), stringFieldRequest("NAME").build()).get();
        FieldDto schemaInDeptField = fieldService.save(schemaIn.getId(), stringFieldRequest("DEPT").build()).get();
        FieldDto schemaInSalaryField = fieldService.save(schemaIn.getId(), intFieldRequest("SALARY").build()).get();

        Schema schemaOut = VavrAssert.assertValid(schemaService.validateAndCreateNew(
                Populated.schema()
                        .productId(product.getId())
                        .displayName("MY_SECOND_SCHEMA")
                        .physicalTableName("MYSS")
                        .fields(newLinkedHashSet(asList(
                                DesignField.Populated.stringField("NAME").build(),
                                DesignField.Populated.stringField("DEPT").build(),
                                DesignField.Populated.stringField("SALARY").build(),
                                DesignField.Populated.stringField("DENSE_RANK").build())))
                        .build())).getResult();

        FieldDto schemaOutNameField = fieldService.save(schemaOut.getId(), stringFieldRequest("NAME").build()).get();
        FieldDto schemaOutDeptField = fieldService.save(schemaOut.getId(), stringFieldRequest("DEPT").build()).get();
        FieldDto schemaOutSalaryField = fieldService.save(schemaOut.getId(), intFieldRequest("SALARY").build()).get();
        FieldDto schemaOutDenseRankField =
                fieldService.save(schemaOut.getId(), stringFieldRequest("DENSE_RANK").build()).get();

        Set<Select> selects = newLinkedHashSet(asList(
                Populated.select().outputFieldId(schemaOutNameField.getId()).select("NAME").build(),
                Populated.select().outputFieldId(schemaOutDeptField.getId()).select("DEPT").build(),
                Populated.select().outputFieldId(schemaOutSalaryField.getId()).select("SALARY").build(),
                Populated.select().outputFieldId(schemaOutDenseRankField.getId()).select("dense_rank()")
                        .isWindow(true)
                        .window(Window.builder()
                                .partitions(singleton("DEPT"))
                                .orders(singleton(Order.builder()
                                        .fieldName("SALARY").direction(Order.Direction.DESC)
                                        .build()))
                                .build())
                        .build()));

        PipelineStep step = Populated.pipelineWindowStep()
                .schemaInId(schemaIn.getId())
                .schemaOutId(schemaOut.getId())
                .selects(selects)
                .build();

        selectsValidator.validate(step);

        verify(pipelineStepExecutor).executeFullTransformation(eq(step), executionContextCaptor.capture());

        ExecuteStepContext stepContext = executionContextCaptor.getValue();
        assertThat(stepContext.getExecutionInputData())
                .containsKeys(schemaIn);
        assertThat(stepContext.getInputSchemas())
                .contains(schemaIn);
        assertThat(stepContext.getOutputSchema())
                .isEqualTo(schemaOut);
    }

    @Test
    public void validate_WindowTransformation_CallsIndividualSelectExecutionWithStepAndExecutionContext() {
        Schema schemaIn = VavrAssert.assertValid(schemaService.validateAndCreateNew(
                Populated.schema()
                        .productId(product.getId())
                        .displayName("MY_FIRST_SCHEMA")
                        .physicalTableName("MYFS")
                        .build())).getResult();

        FieldDto schemaInNameField = fieldService.save(schemaIn.getId(), stringFieldRequest("NAME").build()).get();
        FieldDto schemaInDeptField = fieldService.save(schemaIn.getId(), stringFieldRequest("DEPT").build()).get();
        FieldDto schemaInSalaryField = fieldService.save(schemaIn.getId(), intFieldRequest("SALARY").build()).get();

        Schema schemaOut = VavrAssert.assertValid(schemaService.validateAndCreateNew(
                Populated.schema()
                        .productId(product.getId())
                        .displayName("MY_SECOND_SCHEMA")
                        .physicalTableName("MYSS")
                        .fields(newLinkedHashSet(asList(
                                DesignField.Populated.stringField("NAME").build(),
                                DesignField.Populated.stringField("DEPT").build(),
                                DesignField.Populated.stringField("SALARY").build(),
                                DesignField.Populated.stringField("DENSE_RANK").build())))
                        .build())).getResult();

        FieldDto schemaOutNameField = fieldService.save(schemaOut.getId(), stringFieldRequest("NAME").build()).get();
        FieldDto schemaOutDeptField = fieldService.save(schemaOut.getId(), stringFieldRequest("DEPT").build()).get();
        FieldDto schemaOutSalaryField = fieldService.save(schemaOut.getId(), intFieldRequest("SALARY").build()).get();
        FieldDto schemaOutDenseRankField =
                fieldService.save(schemaOut.getId(), stringFieldRequest("DENSE_RANK").build()).get();

        Set<Select> selects = newLinkedHashSet(asList(
                Populated.select().outputFieldId(schemaOutNameField.getId()).select("NAME").build(),
                Populated.select().outputFieldId(schemaOutDeptField.getId()).select("DEPT").build(),
                Populated.select().outputFieldId(schemaOutSalaryField.getId()).select("SALARY").build(),
                Populated.select().outputFieldId(schemaOutDenseRankField.getId()).select("dense_rank()")
                        .isWindow(true)
                        .window(Window.builder()
                                .partitions(singleton("DEPT"))
                                .orders(singleton(Order.builder()
                                        .fieldName("SALARY").direction(Order.Direction.DESC)
                                        .build()))
                                .build())
                        .build()));

        PipelineStep step = Populated.pipelineWindowStep()
                .schemaInId(schemaIn.getId())
                .schemaOutId(schemaOut.getId())
                .selects(selects)
                .build();

        selectsValidator.validate(step);

        verify(pipelineStepExecutor).transformOutputFieldsIndividually(eq(step), executionContextCaptor.capture());

        ExecuteStepContext stepContext = executionContextCaptor.getValue();
        assertThat(stepContext.getExecutionInputData())
                .containsKeys(schemaIn);
        assertThat(stepContext.getInputSchemas())
                .contains(schemaIn);
        assertThat(stepContext.getOutputSchema())
                .isEqualTo(schemaOut);
    }

    @Test
    public void validate_WindowTransformationWithError_ReturnsErrorForIndividualSelect() {
        Schema schemaIn = VavrAssert.assertValid(schemaService.validateAndCreateNew(
                Populated.schema()
                        .productId(product.getId())
                        .displayName("MY_FIRST_SCHEMA")
                        .physicalTableName("MYFS")
                        .build())).getResult();

        FieldDto schemaInNameField = fieldService.save(schemaIn.getId(), stringFieldRequest("NAME").build()).get();
        FieldDto schemaInDeptField = fieldService.save(schemaIn.getId(), stringFieldRequest("DEPT").build()).get();
        FieldDto schemaInSalaryField = fieldService.save(schemaIn.getId(), intFieldRequest("SALARY").build()).get();

        Schema schemaOut = VavrAssert.assertValid(schemaService.validateAndCreateNew(
                Populated.schema()
                        .productId(product.getId())
                        .displayName("MY_SECOND_SCHEMA")
                        .physicalTableName("MYSS")
                        .build())).getResult();

        FieldDto schemaOutNameField = fieldService.save(schemaOut.getId(), stringFieldRequest("NAME").build()).get();
        FieldDto schemaOutDeptField = fieldService.save(schemaOut.getId(), stringFieldRequest("DEPT").build()).get();
        FieldDto schemaOutSalaryField = fieldService.save(schemaOut.getId(), intFieldRequest("SALARY").build()).get();
        FieldDto schemaOutDenseRankField =
                fieldService.save(schemaOut.getId(), stringFieldRequest("DENSE_RANK").build()).get();

        Set<Select> selects = newHashSet(
                Populated.select()
                        .outputFieldId(schemaOutDenseRankField.getId())
                        .select("dense_ranqor()")
                        .window(Window.builder()
                                .partitions(singleton("DEPT"))
                                .orders(singleton(Order.builder()
                                        .fieldName("SALARY").direction(Order.Direction.DESC)
                                        .build()))
                                .build())
                        .build());

        PipelineStep step = Populated.pipelineWindowStep()
                .schemaInId(schemaIn.getId())
                .schemaOutId(schemaOut.getId())
                .selects(selects)
                .build();

        when(pipelineStepExecutor.transformOutputFieldsIndividually(any(), any()))
                .thenReturn(singletonList(SelectResult.error(
                        "DENSE_RANK",
                        schemaOutDenseRankField.getId(),
                        null,
                        singletonList(ErrorResponse.valueOf("Ooops", "OP")))));

        StepExecutionResult stepExecutionResult = selectsValidator.validateWithIndividualErrors(step);
        assertThat(stepExecutionResult.isSuccessful())
                .isFalse();

        assertThat(stepExecutionResult.getSelectsExecutionErrors().getIndividualErrors())
                .containsExactly(
                        SelectResult.error(
                                "DENSE_RANK",
                                schemaOutDenseRankField.getId(),
                                null,
                                singletonList(ErrorResponse.valueOf("Ooops", "OP"))));
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private static Field findFieldInSchema(final Schema schema, final String fieldName) {
        return schema.getFields().stream()
                .filter(field -> field.getName().equals(fieldName))
                .findFirst()
                .get();
    }

    @Test
    public void checkSyntax_MapTransformation_CallsStepSelectsExecutorWithInputData() {

        Schema schemaIn = VavrAssert.assertValid(schemaService.validateAndCreateNew(
                Populated.schema("A").productId(product.getId()).build()))
                .getResult();

        Schema schemaOut = VavrAssert.assertValid(schemaService.validateAndCreateNew(
                Populated.schema("B").productId(product.getId()).build()))
                .getResult();

        FieldDto schemaInNameField = fieldService.save(schemaIn.getId(), stringFieldRequest("NAME").build()).get();
        FieldDto schemaInVersionField = fieldService.save(schemaIn.getId(), longFieldRequest("VERSION").build()).get();
        FieldDto schemaOutVersionField =
                fieldService.save(schemaOut.getId(), longFieldRequest("NAME_VERSION").build()).get();
        FieldDto constantOutField =
                fieldService.save(schemaOut.getId(), stringFieldRequest("CONST").build()).get();

        Set<Select> selects = newLinkedHashSet(Arrays.asList(
                Populated.select()
                        .outputFieldId(schemaOutVersionField.getId())
                        .select("CONCAT(NAME, VERSION)")
                        .isIntermediate(true)
                        .build(),
                Populated.select()
                        .outputFieldId(constantOutField.getId())
                        .build()));

        PipelineStep step = Populated.pipelineMapStep()
                .schemaInId(schemaIn.getId())
                .schemaOutId(schemaOut.getId())
                .selects(selects)
                .build();

        when(pipelineStepExecutor.transformOutputFieldsIndividually(any(), any()))
                .thenReturn(singletonList(SelectResult.success(
                        "NAME_VERSION",
                        schemaOutVersionField.getId(),
                        null)));

        VavrAssert.assertValid(selectsValidator.checkSyntax(step, SyntaxCheckRequest.builder()
                .outputFieldId(constantOutField.getId())
                .sparkSql("something_to_test()")
                .build()));

        verify(pipelineStepExecutor)
                .transformOutputFieldsIndividually(
                        pipelineStepArgumentCaptor.capture(),
                        executionContextCaptor.capture());

        soft.assertThat(pipelineStepArgumentCaptor.getValue().getSelects())
                .extracting(Select::getSelect)
                .containsExactly("CONCAT(NAME, VERSION)", "something_to_test()");

        soft.assertThat(executionContextCaptor.getValue().getInputSchemas())
                .containsExactly(schemaIn);
        soft.assertThat(executionContextCaptor.getValue().getOutputSchema())
                .isEqualTo(schemaOut);
        soft.assertThat(executionContextCaptor.getValue().getExecutionInputData())
                .containsKeys(schemaIn);
    }

    @Test
    public void checkSyntax_UnionTransformation_CallsStepSelectsExecutorExecutionContext() {
        when(pipelineStepExecutor.transformOutputFieldsIndividually(any(), any()))
                .thenReturn(singletonList(
                        SelectResult.success("something", 1L, null)));

        Schema aSchema = VavrAssert.assertValid(schemaService.validateAndCreateNew(
                Populated.schema()
                        .productId(product.getId())
                        .displayName("SCHEMA_A")
                        .physicalTableName("A")
                        .build())).getResult();

        Schema bSchema = VavrAssert.assertValid(schemaService.validateAndCreateNew(
                Populated.schema()
                        .productId(product.getId())
                        .displayName("SCHEMA_B")
                        .physicalTableName("B")
                        .build())).getResult();

        Schema cSchema = VavrAssert.assertValid(schemaService.validateAndCreateNew(
                Populated.schema()
                        .productId(product.getId())
                        .displayName("SCHEMA_C")
                        .physicalTableName("C")
                        .build())).getResult();

        fieldService.save(aSchema.getId(), longFieldRequest("A_ID").build()).get();
        fieldService.save(aSchema.getId(), stringFieldRequest("A_NAME").build()).get();
        fieldService.save(bSchema.getId(), longFieldRequest("B_ID").build()).get();
        fieldService.save(bSchema.getId(), stringFieldRequest("B_NAME").build()).get();
        FieldDto outputIdField = fieldService.save(cSchema.getId(), longFieldRequest("ID").build()).get();
        FieldDto outputNameField = fieldService.save(cSchema.getId(), stringFieldRequest("NAME").build()).get();

        PipelineStep step = Populated.pipelineUnionStep()
                .schemaOutId(cSchema.getId())
                .schemaInIds(newHashSet(aSchema.getId(), bSchema.getId()))
                .filters(newLinkedHashSet(asList(
                        PipelineFilter.builder()
                                .unionSchemaId(aSchema.getId())
                                .filter("A_NOTHER_FIELD > 2")
                                .build(),
                        PipelineFilter.builder()
                                .unionSchemaId(bSchema.getId())
                                .filter("B_MY_FRIEND = true")
                                .build())))
                .selects(newLinkedHashSet(asList(
                        unionSelect()
                                .outputFieldId(outputIdField.getId())
                                .union(Union.forSchema(aSchema.getId()))
                                .select("A_ID")
                                .build(),
                        unionSelect()
                                .outputFieldId(outputNameField.getId())
                                .union(Union.forSchema(aSchema.getId()))
                                .select("A_NAME")
                                .build(),
                        unionSelect()
                                .outputFieldId(outputIdField.getId())
                                .union(Union.forSchema(bSchema.getId()))
                                .select("B_ID")
                                .build(),
                        unionSelect()
                                .outputFieldId(outputNameField.getId())
                                .union(Union.forSchema(bSchema.getId()))
                                .build())))
                .build();

        VavrAssert.assertValid(selectsValidator.checkSyntax(step, SyntaxCheckRequest.builder()
                .outputFieldId(outputNameField.getId())
                .sparkSql("something_to_test()")
                .build()));

        ArgumentCaptor<PipelineStep> pipelineStepArgumentCaptor = ArgumentCaptor.forClass(PipelineStep.class);
        Set<Field> fields = cSchema.getFields();
        verify(pipelineStepExecutor)
                .transformOutputFieldsIndividually(pipelineStepArgumentCaptor.capture(), ArgumentMatchers.any());

        assertThat(pipelineStepArgumentCaptor.getValue().getSelects())
                .extracting(Select::getSelect)
                .contains("something_to_test()");
    }
}
