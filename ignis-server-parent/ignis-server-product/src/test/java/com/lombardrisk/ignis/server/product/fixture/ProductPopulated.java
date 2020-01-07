package com.lombardrisk.ignis.server.product.fixture;

import com.google.common.collect.ImmutableMap;
import com.lombardrisk.ignis.api.rule.TestResult;
import com.lombardrisk.ignis.client.external.fixture.ExternalClient;
import com.lombardrisk.ignis.client.external.productconfig.export.SchemaPeriod;
import com.lombardrisk.ignis.server.product.model.ProductSchemaDetailsBean;
import com.lombardrisk.ignis.server.product.pipeline.details.SchemaDetails;
import com.lombardrisk.ignis.server.product.pipeline.model.Pipeline;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineAggregationStep;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineJoinStep;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineMapStep;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineScriptletStep;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineUnionStep;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineWindowStep;
import com.lombardrisk.ignis.server.product.pipeline.select.Order;
import com.lombardrisk.ignis.server.product.pipeline.select.Select;
import com.lombardrisk.ignis.server.product.pipeline.select.Union;
import com.lombardrisk.ignis.server.product.pipeline.select.Window;
import com.lombardrisk.ignis.server.product.pipeline.transformation.Join;
import com.lombardrisk.ignis.server.product.pipeline.transformation.JoinField;
import com.lombardrisk.ignis.server.product.pipeline.transformation.ScriptletInput;
import com.lombardrisk.ignis.server.product.productconfig.model.ImportStatus;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductConfig;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductConfigFileContents;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductImportContext;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductImportDiff;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductManifest;
import com.lombardrisk.ignis.server.product.rule.model.ValidationRule;
import com.lombardrisk.ignis.server.product.rule.model.ValidationRuleExample;
import com.lombardrisk.ignis.server.product.rule.model.ValidationRuleExampleField;
import com.lombardrisk.ignis.server.product.table.model.BooleanField;
import com.lombardrisk.ignis.server.product.table.model.DateField;
import com.lombardrisk.ignis.server.product.table.model.DecimalField;
import com.lombardrisk.ignis.server.product.table.model.DoubleField;
import com.lombardrisk.ignis.server.product.table.model.FloatField;
import com.lombardrisk.ignis.server.product.table.model.IntField;
import com.lombardrisk.ignis.server.product.table.model.LongField;
import com.lombardrisk.ignis.server.product.table.model.StringField;
import com.lombardrisk.ignis.server.product.table.model.Table;
import com.lombardrisk.ignis.server.product.table.model.TimestampField;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static com.lombardrisk.ignis.api.rule.ValidationRuleSeverity.WARNING;
import static com.lombardrisk.ignis.api.rule.ValidationRuleType.QUALITY;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;

@UtilityClass
@Slf4j
public class ProductPopulated {

    public static final String DECIMAL_FIELD = "DECIMAL";
    private static final String DECIMAL_FIELD_EXPRESSION = DECIMAL_FIELD + " > 0";
    public static final String SCHEMA_NAME = "CQRS";
    private static final String SCHEMA_DISPLAY_NAME = "A FRIENDLY DISPLAY NAME";

    public static BooleanField.BooleanFieldBuilder booleanField() {
        return BooleanField.builder()
                .name("BOOLEAN")
                .nullable(false);
    }

    public static BooleanField.BooleanFieldBuilder booleanField(final String name) {
        return booleanField().name(name);
    }

    public static LongField.LongFieldBuilder longField(final String name) {
        return longField().name(name);
    }

    public static LongField.LongFieldBuilder longField() {
        return LongField.builder()
                .name("LONG")
                .nullable(false);
    }

    public static IntField.IntFieldBuilder intField(final String name) {
        return intField().name(name);
    }

    public static IntField.IntFieldBuilder intField() {
        return IntField.builder()
                .name("INT")
                .nullable(false);
    }

    public static DoubleField.DoubleFieldBuilder doubleField(final String name) {
        return doubleField().name(name);
    }

    public static DoubleField.DoubleFieldBuilder doubleField() {
        return DoubleField.builder()
                .name("DOUBLE")
                .nullable(false);
    }

    public static FloatField.FloatFieldBuilder floatField(final String name) {
        return floatField().name(name);
    }

    public static FloatField.FloatFieldBuilder floatField() {
        return FloatField.builder()
                .name("FLOAT")
                .nullable(false);
    }

    public static DateField.DateFieldBuilder dateField(final String name) {
        return dateField().name(name);
    }

    public static DateField.DateFieldBuilder dateField() {
        return DateField.builder()
                .name("DATE")
                .nullable(false)
                .format("dd/MM/yyyy");
    }

    public static StringField.StringFieldBuilder stringField() {
        return StringField.builder()
                .name("STRING")
                .nullable(false)
                .maxLength(38)
                .minLength(2)
                .regularExpression("[a-zA-Z0-9]");
    }

    public static StringField.StringFieldBuilder stringField(final String name) {
        return stringField().name(name);
    }

    public static TimestampField.TimestampFieldBuilder timestampField(final String name) {
        return timestampField().name(name);
    }

    public static TimestampField.TimestampFieldBuilder timestampField() {
        return TimestampField.builder()
                .name("TIMESTAMP")
                .nullable(false)
                .format("dd/MM/yyyy");
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
                .expression(DECIMAL_FIELD_EXPRESSION)
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
                .name(DECIMAL_FIELD)
                .value("18");
    }

    public static Table.TableBuilder table() {
        return Table.builder()
                .physicalTableName(SCHEMA_NAME)
                .displayName(SCHEMA_DISPLAY_NAME)
                .version(22)
                .createdBy("Ignis")
                .createdTime(new Date())
                .startDate(LocalDate.of(1970, 1, 1))
                .fields(newLinkedHashSet(asList(
                        decimalField("AMOUNT").build(), stringField("DESCRIPTION").build())))
                .validationRules(validationRules());
    }

    public static Table.TableBuilder table(final String name) {
        return table()
                .displayName(name)
                .physicalTableName(name);
    }

    public static DecimalField.DecimalFieldBuilder decimalField(final String name) {
        return decimalField().name(name);
    }

    public static DecimalField.DecimalFieldBuilder decimalField() {
        return DecimalField.builder()
                .name(DECIMAL_FIELD)
                .nullable(false)
                .precision(38)
                .scale(2);
    }

    public static Pipeline.PipelineBuilder pipeline() {
        return Pipeline.builder()
                .name("Pipeline 1")
                .steps(newLinkedHashSet(singletonList(mapPipelineStep().build())));
    }

    public static PipelineMapStep.PipelineMapStepBuilder mapPipelineStep() {
        return PipelineMapStep.builder()
                .name("Mapping step")
                .description("Adds two columns")
                .schemaInId(1L)
                .schemaIn(SchemaDetails.builder()
                        .id(1L)
                        .physicalTableName("SCHEMA_IN_PHYSICAL")
                        .displayName("schema in display name")
                        .build())
                .schemaOutId(2L)
                .schemaOut(SchemaDetails.builder()
                        .id(2L)
                        .physicalTableName("SCHEMA_OUT_PHYSICAL")
                        .displayName("schema out display name")
                        .build())
                .selects(newLinkedHashSet(asList(
                        select().select("X").outputFieldId(1L).build(),
                        select().select("Y").outputFieldId(2L).build(),
                        select().select("X+Y").outputFieldId(3L).build())))
                .filters(newLinkedHashSet(Arrays.asList("X = 1", "Y < 50")));
    }

    public static PipelineJoinStep.PipelineJoinStepBuilder joinPipelineStep() {
        return PipelineJoinStep.builder()
                .name("Join step")
                .description("Joins two schemas")
                .schemaOutId(2L)
                .schemaOut(schemaDetails().id(2L).build())
                .joins(newHashSet(join().build()))
                .selects(newLinkedHashSet(asList(
                        select().select("X.ID").outputFieldId(1L).build(),
                        select().select("Y.NAME").outputFieldId(2L).build())));
    }

    public static PipelineAggregationStep.PipelineAggregationStepBuilder aggregatePipelineStep() {
        return PipelineAggregationStep.builder()
                .name("Aggregation step")
                .description("Sum a column")
                .schemaInId(2L)
                .schemaIn(schemaDetails().id(2L).build())
                .schemaOutId(3L)
                .schemaOut(schemaDetails().id(3L).build())
                .selects(newLinkedHashSet(asList(
                        select().select("Y").outputFieldId(1L).build(),
                        select().select("SUM(Z)").outputFieldId(2L).build())))
                .groupings(singleton("Y"))
                .filters(emptySet());
    }

    public static PipelineWindowStep.PipelineWindowStepBuilder windowPipelineStep() {
        return PipelineWindowStep.builder()
                .name("Window step")
                .description("Step with window function")
                .schemaInId(4L)
                .schemaIn(schemaDetails().id(4L).build())
                .schemaOutId(5L)
                .schemaOut(schemaDetails().id(4L).build())
                .selects(singleton(select()
                        .isWindow(true)
                        .window(Window.builder()
                                .partitions(newHashSet("A", "B"))
                                .orders(singleton(Order.builder()
                                        .fieldName("A").direction(Order.Direction.ASC).priority(0)
                                        .build()))
                                .build())
                        .build()));
    }

    public static PipelineUnionStep.PipelineUnionStepBuilder unionPipelineStep() {
        return PipelineUnionStep.builder()
                .name("Union step")
                .description("Step with union")
                .schemaOutId(5L)
                .schemaOut(schemaDetails().id(4L).build())
                .selects(newHashSet(
                        select()
                                .isUnion(true)
                                .selectUnion(Union.builder()
                                        .unionSchemaId(1L)
                                        .unionSchema(schemaDetails().build())
                                        .build())
                                .build(),
                        select()
                                .isUnion(true)
                                .selectUnion(Union.builder()
                                        .unionSchemaId(2L)
                                        .unionSchema(schemaDetails().build())
                                        .build())
                                .build()));
    }

    public static PipelineScriptletStep.PipelineScriptletStepBuilder scriptletPipelineStep() {
        return PipelineScriptletStep.builder()
                .name("Scriptlet step")
                .description("Custom scriptlet step")
                .jarFile("custom-step.jar")
                .className("com.vermeg.MyCustomStep")
                .schemaOutId(10L)
                .schemaOut(schemaDetails().id(10L).build())
                .schemaIns(singleton(scriptletInput().build()));
    }

    public static ScriptletInput.ScriptletInputBuilder scriptletInput() {
        return ScriptletInput.builder()
                .inputName("MyInput")
                .schemaInId(11L)
                .schemaIn(schemaDetails().id(11L).build());
    }

    public static Join.JoinBuilder join() {
        return Join.builder()
                .leftSchemaId(101L)
                .leftSchema(schemaDetails().id(101L).build())
                .rightSchemaId(102L)
                .rightSchema(schemaDetails().id(102L).build())
                .joinType(Join.JoinType.FULL_OUTER)
                .joinFields(newHashSet(JoinField.builder()
                        .leftJoinFieldId(1L)
                        .leftJoinField(longField("ID").id(1L).build())
                        .rightJoinFieldId(2L)
                        .rightJoinField(longField("ID").id(2L).build())
                        .build()))
                ;
    }

    public static Select.SelectBuilder select() {
        return Select.builder()
                .order(0L)
                .isWindow(false)
                .window(Window.none());
    }

    public static SchemaDetails.SchemaDetailsBuilder schemaDetails() {
        return SchemaDetails.builder()
                .displayName("table1")
                .physicalTableName("tb1")
                .version(1);
    }

    public static SchemaDetails.SchemaDetailsBuilder schemaDetails(final String name, final int version) {
        return SchemaDetails.builder()
                .displayName(name)
                .physicalTableName(name)
                .version(version);
    }

    public static ProductSchemaDetailsBean.ProductSchemaDetailsBeanBuilder productSchemaDetailsOnly() {
        return ProductSchemaDetailsBean.builder()
                .schemaPhysicalName("CQ_RS")
                .schemaDisplayName("CQRS")
                .schemaVersion(1)
                .productName("default name")
                .productVersion("default version");
    }

    public static ProductConfig.ProductConfigBuilder productConfig() {
        return ProductConfig.builder()
                .name("default name")
                .version("default version")
                .importStatus(ImportStatus.SUCCESS)
                .createdTime(new Date())
                .tables(emptySet());
    }

    public static ProductConfig.ProductConfigBuilder productConfig(final String name) {
        return productConfig().name(name);
    }

    public static ProductImportDiff.ProductImportDiffBuilder productImportDiff() {
        ProductConfig productConfig = productConfig().build();
        return ProductImportDiff.builder()
                .productName(productConfig.getName())
                .productVersion(productConfig.getVersion())
                .newSchemas(newHashSet(
                        table()
                                .id(1L)
                                .physicalTableName("new table")
                                .version(1)
                                .build()))
                .existingSchemas(newHashSet(
                        table()
                                .id(2L)
                                .physicalTableName("existing table")
                                .version(2)
                                .build()))
                .existingSchemaToNewPeriod(ImmutableMap.of(table().id(0L).build(), SchemaPeriod.max()))
                .newVersionedSchemas(newHashSet(
                        table()
                                .id(3L)
                                .physicalTableName("new table")
                                .version(23)
                                .build(),
                        table()
                                .id(5L)
                                .physicalTableName("existing table")
                                .version(3)
                                .build()));
    }

    public static ProductImportContext.ProductImportContextBuilder productImportContext() {
        return ProductImportContext.builder()
                .existingSchemaIdToOldPeriod(ImmutableMap.of())
                .existingSchemaIdToNewPeriod(ImmutableMap.of())
                .newSchemaNameToId(ImmutableMap.of())
                .schemaNameToNewFields(ImmutableMap.of());
    }

    public static ProductConfigFileContents.ProductConfigFileContentsBuilder productConfigFileContents() {
        return ProductConfigFileContents.builder()
                .productMetadata(productManifest().build())
                .schemas(singletonList(ExternalClient.Populated.schemaExport().build()));
    }

    public static ProductManifest.ProductManifestBuilder productManifest() {
        return ProductManifest.builder()
                .name("prod")
                .version("1.0.0-RELEASE");
    }
}
