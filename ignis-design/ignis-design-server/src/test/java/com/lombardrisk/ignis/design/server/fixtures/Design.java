package com.lombardrisk.ignis.design.server.fixtures;

import com.google.common.collect.ImmutableMap;
import com.lombardrisk.ignis.client.design.pipeline.CreatePipelineRequest;
import com.lombardrisk.ignis.client.design.pipeline.PipelineView;
import com.lombardrisk.ignis.client.design.pipeline.SyntaxCheckRequest;
import com.lombardrisk.ignis.client.design.pipeline.UpdatePipelineRequest;
import com.lombardrisk.ignis.client.design.pipeline.aggregation.PipelineAggregationStepRequest;
import com.lombardrisk.ignis.client.design.pipeline.aggregation.PipelineAggregationStepView;
import com.lombardrisk.ignis.client.design.pipeline.join.JoinFieldRequest;
import com.lombardrisk.ignis.client.design.pipeline.join.JoinRequest;
import com.lombardrisk.ignis.client.design.pipeline.join.PipelineJoinStepRequest;
import com.lombardrisk.ignis.client.design.pipeline.map.PipelineMapStepRequest;
import com.lombardrisk.ignis.client.design.pipeline.map.PipelineMapStepView;
import com.lombardrisk.ignis.client.design.pipeline.scriptlet.PipelineScriptletStepRequest;
import com.lombardrisk.ignis.client.design.pipeline.scriptlet.ScriptletInputRequest;
import com.lombardrisk.ignis.client.design.pipeline.select.SelectRequest;
import com.lombardrisk.ignis.client.design.pipeline.select.SelectView;
import com.lombardrisk.ignis.client.design.pipeline.select.UnionRequest;
import com.lombardrisk.ignis.client.design.pipeline.union.PipelineUnionStepRequest;
import com.lombardrisk.ignis.client.design.pipeline.window.PipelineWindowStepRequest;
import com.lombardrisk.ignis.client.design.productconfig.NewProductConfigRequest;
import com.lombardrisk.ignis.client.design.productconfig.ProductConfigDto;
import com.lombardrisk.ignis.client.design.schema.NewSchemaVersionRequest;
import com.lombardrisk.ignis.client.design.schema.UpdateSchema;
import com.lombardrisk.ignis.client.external.pipeline.export.step.JoinType;
import com.lombardrisk.ignis.design.field.DesignField;
import com.lombardrisk.ignis.design.field.model.IntField;
import com.lombardrisk.ignis.design.field.model.StringField;
import com.lombardrisk.ignis.design.server.pipeline.model.Pipeline;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineAggregationStep;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineJoinStep;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineMapStep;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineScriptletStep;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineUnionStep;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineWindowStep;
import com.lombardrisk.ignis.design.server.pipeline.model.ScriptletInput;
import com.lombardrisk.ignis.design.server.pipeline.model.join.Join;
import com.lombardrisk.ignis.design.server.pipeline.model.join.JoinField;
import com.lombardrisk.ignis.design.server.pipeline.model.select.Select;
import com.lombardrisk.ignis.design.server.pipeline.model.select.Union;
import com.lombardrisk.ignis.design.server.pipeline.model.select.Window;
import com.lombardrisk.ignis.design.server.pipeline.test.model.ActualDataRow;
import com.lombardrisk.ignis.design.server.pipeline.test.model.ExpectedDataRow;
import com.lombardrisk.ignis.design.server.pipeline.test.model.InputDataRow;
import com.lombardrisk.ignis.design.server.pipeline.test.model.PipelineStepTest;
import com.lombardrisk.ignis.design.server.productconfig.ProductConfig;
import com.lombardrisk.ignis.design.server.productconfig.rule.model.TestResult;
import com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRule;
import com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRuleExample;
import com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRuleExampleField;
import com.lombardrisk.ignis.design.server.productconfig.schema.Schema;
import com.lombardrisk.ignis.design.server.productconfig.schema.request.CreateSchemaRequest;
import org.apache.commons.lang3.RandomStringUtils;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static com.lombardrisk.ignis.design.field.request.FieldRequest.IntegerFieldRequest;
import static com.lombardrisk.ignis.design.field.request.FieldRequest.StringFieldRequest;
import static com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRuleSeverity.WARNING;
import static com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRuleType.QUALITY;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;

public final class Design {

    public static final class Populated {

        private static final String SCHEMA_NAME = "CQRS";
        private static final String SCHEMA_DISPLAY_NAME = "A FRIENDLY DISPLAY NAME";

        public static NewProductConfigRequest.NewProductConfigRequestBuilder newProductRequest() {
            return NewProductConfigRequest.builder()
                    .name("default name")
                    .version("default version");
        }

        public static NewProductConfigRequest.NewProductConfigRequestBuilder newProductRequest(final String name) {
            return newProductRequest().name(name);
        }

        public static ProductConfig.ProductConfigBuilder productConfig() {
            return ProductConfig.builder()
                    .name("default name")
                    .version("default version")
                    .tables(new HashSet<>());
        }

        public static ProductConfigDto.ProductConfigDtoBuilder productConfigDto() {
            return ProductConfigDto.builder()
                    .name("default name")
                    .version("default version")
                    .schemas(new HashSet<>());
        }

        public static Schema.SchemaBuilder schema(final String name) {
            return schema()
                    .productId(213412L)
                    .displayName(name)
                    .physicalTableName(name);
        }

        public static Schema.SchemaBuilder schema() {
            IntField ageField = new IntField();
            ageField.setName("AGE");

            StringField descriptionField = new StringField();
            descriptionField.setName("description");

            return Schema.builder()
                    .productId(2342L)
                    .physicalTableName(SCHEMA_NAME)
                    .displayName(SCHEMA_DISPLAY_NAME)
                    .majorVersion(1)
                    .latest(true)
                    .startDate(LocalDate.of(2018, 1, 1))
                    .endDate(LocalDate.of(2018, 12, 31))
                    .createdBy("Ignis")
                    .createdTime(new Date())
                    .fields(newLinkedHashSet(asList(ageField, descriptionField)))
                    .validationRules(validationRules());
        }

        public static Schema.SchemaBuilder emptySchema(final String name) {
            return Schema.builder()
                    .productId(2342L)
                    .physicalTableName(name)
                    .displayName(name)
                    .majorVersion(1)
                    .latest(true)
                    .startDate(LocalDate.of(2018, 1, 1))
                    .endDate(LocalDate.of(2018, 12, 31))
                    .createdBy("Ignis")
                    .createdTime(new Date());
        }

        public static CreateSchemaRequest.CreateSchemaRequestBuilder createSchemaRequest() {
            IntegerFieldRequest ageField = new IntegerFieldRequest();
            ageField.setName("AGE");

            StringFieldRequest descriptionField = new StringFieldRequest();
            descriptionField.setName("description");

            return CreateSchemaRequest.builder()
                    .physicalTableName(SCHEMA_NAME)
                    .displayName(SCHEMA_DISPLAY_NAME)
                    .majorVersion(1)
                    .startDate(LocalDate.of(2018, 1, 1))
                    .endDate(LocalDate.of(2018, 12, 31));
        }

        public static CreateSchemaRequest.CreateSchemaRequestBuilder randomisedCreateSchemaRequest() {
            String randomString = RandomStringUtils.randomAlphabetic(10);
            IntegerFieldRequest ageField = new IntegerFieldRequest();
            ageField.setName("AGE");

            StringFieldRequest descriptionField = new StringFieldRequest();
            descriptionField.setName("description");

            return CreateSchemaRequest.builder()
                    .physicalTableName(SCHEMA_NAME + randomString)
                    .displayName(SCHEMA_DISPLAY_NAME + randomString)
                    .majorVersion(1)
                    .startDate(LocalDate.of(2018, 1, 1))
                    .endDate(LocalDate.of(2018, 12, 31))
//                    .fields(newLinkedHashSet(asList(ageField, descriptionField))) TODO
                    ;
        }

        public static Set<ValidationRule> validationRules() {
            return newHashSet(validationRule().build());
        }

        public static ValidationRule.ValidationRuleBuilder validationRule() {
            return ValidationRule.builder()
                    .ruleId("RegId")
                    .name("ValidationRule1")
                    .description("Description")
                    .version(2)
                    .startDate(LocalDate.of(1991, 12, 7))
                    .endDate(LocalDate.of(1992, 12, 7))
                    .expression(DesignField.Populated.DECIMAL_FIELD_EXPRESSION)
                    .validationRuleSeverity(WARNING)
                    .validationRuleType(QUALITY)
                    .contextFields(newHashSet())
                    .validationRuleExamples(newHashSet(validationRuleExample().build()))
                    .version(1);
        }

        public static ValidationRuleExample.ValidationRuleExampleBuilder validationRuleExample() {
            return ValidationRuleExample.builder()
                    .expectedResult(TestResult.PASS)
                    .validationRuleExampleFields(newHashSet(validationRuleExampleField().build()));
        }

        public static ValidationRuleExampleField.ValidationRuleExampleFieldBuilder validationRuleExampleField() {
            return ValidationRuleExampleField.builder()
                    .name(DesignField.Populated.DECIMAL_FIELD)
                    .value("18");
        }

        public static NewSchemaVersionRequest.NewSchemaVersionRequestBuilder newSchemaVersionRequest() {
            return NewSchemaVersionRequest.builder()
                    .startDate(LocalDate.of(2018, 1, 1));
        }

        public static UpdateSchema.UpdateSchemaBuilder schemaUpdateRequest() {
            return UpdateSchema.builder()
                    .displayName("my schema")
                    .physicalTableName("MY_SCHEMA")
                    .startDate(LocalDate.of(2018, 1, 1))
                    .endDate(LocalDate.of(2018, 12, 31));
        }

        public static PipelineView.PipelineViewBuilder pipelineView() {
            return PipelineView.builder()
                    .name("default pipeline")
                    .productId(12312L)
                    .steps(newHashSet(pipelineMapStepView().build()));
        }

        public static PipelineMapStepView.PipelineMapStepViewBuilder pipelineMapStepView() {
            return PipelineMapStepView.builder()
                    .name("default pipeline step")
                    .description("default pipeline step description")
                    .schemaInId(1234L)
                    .schemaOutId(5678L)
                    .selects(emptyList())
                    .filters(emptyList());
        }

        public static PipelineAggregationStepView.PipelineAggregationStepViewBuilder pipelineAggregationStepView() {
            return PipelineAggregationStepView.builder()
                    .name("default pipeline step")
                    .description("default pipeline step description")
                    .schemaInId(1234L)
                    .schemaOutId(5678L)
                    .selects(singletonList(SelectView.builder().select("SUM(B)").build()))
                    .filters(singletonList("C=1"))
                    .groupings(singletonList("A"));
        }

        public static CreatePipelineRequest.CreatePipelineRequestBuilder createPipelineRequest() {
            return CreatePipelineRequest.builder()
                    .name("default pipeline request name")
                    .productId(234523L);
        }

        public static UpdatePipelineRequest.UpdatePipelineRequestBuilder updatePipelineRequest() {
            return UpdatePipelineRequest.builder()
                    .name("default updated pipeline request name");
        }

        public static SyntaxCheckRequest.SyntaxCheckRequestBuilder syntaxCheckRequest() {
            return SyntaxCheckRequest.builder()
                    .sparkSql("Ooops this is wrong")
                    .pipelineStep(pipelineMapStepRequest().build())
                    .outputFieldId(100L);
        }

        public static PipelineMapStepRequest.PipelineMapStepRequestBuilder pipelineMapStepRequest() {
            return PipelineMapStepRequest.builder()
                    .name("default pipeline step request name")
                    .description("default pipeline step request description")
                    .schemaInId(324L)
                    .schemaOutId(23L)
                    .selects(emptySet())
                    .filters(emptyList());
        }

        public static PipelineUnionStepRequest.PipelineUnionStepRequestBuilder pipelineUnionStepRequest() {
            return PipelineUnionStepRequest.builder()
                    .name("default pipeline step request name")
                    .description("default pipeline step request description")
                    .unionSchemas(ImmutableMap.of(
                            1L, UnionRequest.builder()
                                    .selects(singletonList(SelectRequest.builder()
                                            .select("ID")
                                            .outputFieldId(1L)
                                            .order(0L)
                                            .build()))
                                    .build(),
                            2L, UnionRequest.builder()
                                    .selects(singletonList(SelectRequest.builder()
                                            .select("NAME")
                                            .order(1L)
                                            .outputFieldId(2L)
                                            .build()))
                                    .build()))
                    .schemaOutId(23L);
        }

        public static PipelineAggregationStepRequest.PipelineAggregationStepRequestBuilder pipelineAggregationStepRequest() {
            return PipelineAggregationStepRequest.builder()
                    .name("default pipeline step request name")
                    .description("default pipeline step request description")
                    .schemaInId(324L)
                    .schemaOutId(23L)
                    .selects(emptySet())
                    .filters(emptyList())
                    .groupings(emptyList());
        }

        public static PipelineJoinStepRequest.PipelineJoinStepRequestBuilder pipelineJoinStepRequest() {
            return PipelineJoinStepRequest.builder()
                    .name("default pipeline step request name")
                    .description("default pipeline step request description")
                    .schemaOutId(23L)
                    .selects(emptySet())
                    .joins(singleton(joinRequest().build()));
        }

        public static JoinRequest.JoinRequestBuilder joinRequest() {
            return JoinRequest.builder()
                    .id(1L)
                    .leftSchemaId(101L)
                    .rightSchemaId(201L)
                    .joinFields(singletonList(JoinFieldRequest.builder()
                            .leftFieldId(2L)
                            .rightFieldId(3L)
                            .build()));
        }

        public static PipelineWindowStepRequest.PipelineWindowStepRequestBuilder pipelineWindowStepRequest() {
            return PipelineWindowStepRequest.builder()
                    .name("default pipeline step request name")
                    .description("default pipeline step request description")
                    .schemaInId(324L)
                    .schemaOutId(23L)
                    .selects(emptySet());
        }

        public static ScriptletInputRequest.ScriptletInputRequestBuilder scriptletInputRequest() {
            return ScriptletInputRequest.builder()
                    .metadataInput("MyInput")
                    .schemaInId(11L);
        }

        public static PipelineScriptletStepRequest.PipelineScriptletStepRequestBuilder PipelineScriptletStepRequest() {
            return PipelineScriptletStepRequest.builder()
                    .name("default pipeline step request name")
                    .description("default pipeline step request description")
                    .javaClass("default scriptlet class")
                    .jarFile("default scriptlet jar")
                    .scriptletInputs(emptySet())
                    .schemaOutId(23L);
        }

        public static Pipeline.PipelineBuilder pipeline() {
            return Pipeline.builder()
                    .name("default pipeline")
                    .productId(234L)
                    .steps(newHashSet(pipelineMapStep().build()));
        }

        public static PipelineMapStep.PipelineMapStepBuilder pipelineMapStep() {
            return PipelineMapStep.builder()
                    .name("default pipeline step")
                    .description("default pipeline step description")
                    .schemaInId(1234L)
                    .schemaOutId(5678L)
                    .selects(emptySet());
        }

        public static PipelineAggregationStep.PipelineAggregationStepBuilder pipelineAggregationStep() {
            return PipelineAggregationStep.builder()
                    .name("default pipeline step")
                    .description("default pipeline step description")
                    .schemaInId(1234L)
                    .schemaOutId(5678L)
                    .selects(emptySet())
                    .groupings(singleton("A"));
        }

        public static PipelineJoinStep.PipelineJoinStepBuilder pipelineJoinStep() {
            return PipelineJoinStep.builder()
                    .name("default pipeline step")
                    .description("default pipeline step description")
                    .schemaOutId(5678L)
                    .selects(emptySet())
                    .joins(singleton(join().build()));
        }

        public static PipelineJoinStep.PipelineJoinStepBuilder pipelineJoinStepWithJoinFields() {
            return PipelineJoinStep.builder()
                    .name("default pipeline step")
                    .description("default pipeline step description")
                    .schemaOutId(9101L)
                    .selects(emptySet())
                    .joins(singleton(joinWithFields()
                            .build()));
        }

        public static PipelineUnionStep.PipelineUnionStepBuilder pipelineUnionStep() {
            return PipelineUnionStep.builder()
                    .name("default pipeline step")
                    .description("default pipeline step description")
                    .schemaOutId(5678L)
                    .schemaInIds(newHashSet(123L, 456L))
                    .selects(emptySet());
        }

        public static PipelineWindowStep.PipelineWindowStepBuilder pipelineWindowStep() {
            return PipelineWindowStep.builder()
                    .name("default pipeline step")
                    .description("default pipeline step description")
                    .schemaInId(1234L)
                    .schemaOutId(5678L)
                    .selects(emptySet());
        }

        public static PipelineScriptletStep.PipelineScriptletStepBuilder scriptletPipelineStep() {
            return PipelineScriptletStep.builder()
                    .name("Scriptlet step")
                    .description("Custom scriptlet step")
                    .jarFile("custom-step.jar")
                    .className("com.vermeg.MyCustomStep")
                    .schemaOutId(10L)
                    .schemaIns(singleton(scriptletInput().build()));
        }

        public static ScriptletInput.ScriptletInputBuilder scriptletInput() {
            return ScriptletInput.builder()
                    .inputName("MyInput")
                    .schemaInId(11L);
        }

        public static Select.SelectBuilder select() {
            return Select.builder()
                    .isWindow(false)
                    .window(Window.none());
        }

        public static Select.SelectBuilder unionSelect() {
            return Select.builder()
                    .isWindow(false)
                    .window(Window.none())
                    .isUnion(true)
                    .union(Union.forSchema(123L));
        }

        public static Join.JoinBuilder join() {
            return Join.builder()
                    .joinType(JoinType.INNER);
        }

        public static Join.JoinBuilder joinWithFields() {
            return Join.builder()
                    .leftSchemaId(123L)
                    .rightSchemaId(456L)
                    .joinFields(newHashSet(
                            JoinField.builder()
                                    .leftJoinFieldId(678L)
                                    .rightJoinFieldId(910L)
                                    .build(),
                            JoinField.builder()
                                    .leftJoinFieldId(112L)
                                    .rightJoinFieldId(134L)
                                    .build()))
                    .joinType(JoinType.INNER);
        }

        public static PipelineStepTest.PipelineStepTestBuilder pipelineStepTest() {
            return PipelineStepTest.builder()
                    .name("default pipeline step test")
                    .description("default pipeline step test");
        }

        public static InputDataRow.InputDataRowBuilder inputDataRow() {
            return InputDataRow.builder()
                    .isRun(false);
        }

        public static ExpectedDataRow.ExpectedDataRowBuilder expectedDataRow() {
            return ExpectedDataRow.builder()
                    .isRun(false)
                    .status(ExpectedDataRow.Status.MATCHED);
        }

        public static ActualDataRow.ActualDataRowBuilder actualDataRow() {
            return ActualDataRow.builder()
                    .isRun(false)
                    .status(ActualDataRow.Status.MATCHED);
        }
    }
}
