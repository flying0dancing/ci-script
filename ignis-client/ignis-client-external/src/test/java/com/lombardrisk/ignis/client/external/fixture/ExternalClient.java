package com.lombardrisk.ignis.client.external.fixture;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableMap;
import com.lombardrisk.ignis.client.core.fixture.ClientCore;
import com.lombardrisk.ignis.client.core.page.response.Page;
import com.lombardrisk.ignis.client.external.dataset.DatasetQueryParams;
import com.lombardrisk.ignis.client.external.dataset.model.Dataset;
import com.lombardrisk.ignis.client.external.dataset.model.PagedDataset;
import com.lombardrisk.ignis.client.external.drillback.DatasetRowDataView;
import com.lombardrisk.ignis.client.external.drillback.DrillBackStepDetails;
import com.lombardrisk.ignis.client.external.job.ExternalExitStatus;
import com.lombardrisk.ignis.client.external.job.JobExecutionView;
import com.lombardrisk.ignis.client.external.job.pipeline.PipelineRequest;
import com.lombardrisk.ignis.client.external.job.staging.request.DataSource;
import com.lombardrisk.ignis.client.external.job.staging.request.DatasetMetadata;
import com.lombardrisk.ignis.client.external.job.staging.request.v1.StagingItemRequest;
import com.lombardrisk.ignis.client.external.job.staging.request.v1.StagingRequest;
import com.lombardrisk.ignis.client.external.job.staging.request.v2.StagingItemRequestV2;
import com.lombardrisk.ignis.client.external.job.staging.request.v2.StagingRequestV2;
import com.lombardrisk.ignis.client.external.job.validation.ValidationJobRequest;
import com.lombardrisk.ignis.client.external.pipeline.export.PipelineExport;
import com.lombardrisk.ignis.client.external.pipeline.export.SchemaReference;
import com.lombardrisk.ignis.client.external.pipeline.export.TransformationType;
import com.lombardrisk.ignis.client.external.pipeline.export.step.JoinExport;
import com.lombardrisk.ignis.client.external.pipeline.export.step.JoinFieldExport;
import com.lombardrisk.ignis.client.external.pipeline.export.step.JoinType;
import com.lombardrisk.ignis.client.external.pipeline.export.step.PipelineAggregationStepExport;
import com.lombardrisk.ignis.client.external.pipeline.export.step.PipelineJoinStepExport;
import com.lombardrisk.ignis.client.external.pipeline.export.step.PipelineMapStepExport;
import com.lombardrisk.ignis.client.external.pipeline.export.step.PipelineScriptletStepExport;
import com.lombardrisk.ignis.client.external.pipeline.export.step.PipelineUnionStepExport;
import com.lombardrisk.ignis.client.external.pipeline.export.step.PipelineWindowStepExport;
import com.lombardrisk.ignis.client.external.pipeline.export.step.ScriptletInputExport;
import com.lombardrisk.ignis.client.external.pipeline.export.step.UnionExport;
import com.lombardrisk.ignis.client.external.pipeline.export.step.select.OrderExport;
import com.lombardrisk.ignis.client.external.pipeline.export.step.select.SelectExport;
import com.lombardrisk.ignis.client.external.pipeline.export.step.select.WindowExport;
import com.lombardrisk.ignis.client.external.pipeline.view.PipelineInvocationView;
import com.lombardrisk.ignis.client.external.pipeline.view.PipelineStepInvocationDatasetView;
import com.lombardrisk.ignis.client.external.pipeline.view.PipelineStepInvocationView;
import com.lombardrisk.ignis.client.external.pipeline.view.PipelineStepView;
import com.lombardrisk.ignis.client.external.pipeline.view.PipelineView;
import com.lombardrisk.ignis.client.external.pipeline.view.SchemaDetailsView;
import com.lombardrisk.ignis.client.external.pipeline.view.select.SelectView;
import com.lombardrisk.ignis.client.external.pipeline.view.select.WindowView;
import com.lombardrisk.ignis.client.external.productconfig.export.FieldExport;
import com.lombardrisk.ignis.client.external.productconfig.export.ProductConfigExport;
import com.lombardrisk.ignis.client.external.productconfig.export.SchemaExport;
import com.lombardrisk.ignis.client.external.productconfig.export.ValidationRuleExport;
import com.lombardrisk.ignis.client.external.productconfig.view.FieldView;
import com.lombardrisk.ignis.client.external.productconfig.view.ProductConfigView;
import com.lombardrisk.ignis.client.external.productconfig.view.SchemaView;
import com.lombardrisk.ignis.client.external.productconfig.view.ValidationRuleView;
import com.lombardrisk.ignis.client.external.rule.ValidationResultsDetailView;
import com.lombardrisk.ignis.client.external.rule.ValidationRuleSummaryView;
import com.lombardrisk.ignis.common.json.MapperWrapper;
import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Sets.newHashSet;
import static com.lombardrisk.ignis.client.external.job.JobStatus.COMPLETED;
import static com.lombardrisk.ignis.client.external.productconfig.export.ValidationRuleExport.Severity.CRITICAL;
import static com.lombardrisk.ignis.client.external.productconfig.export.ValidationRuleExport.Type.SYNTAX;
import static com.lombardrisk.ignis.client.external.rule.ValidationRuleSummaryView.SummaryStatus.SUCCESS;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;

@UtilityClass
public class ExternalClient {

    @UtilityClass
    public static class Populated {

        public static JobExecutionView.JobExecutionViewBuilder jobExecution() {
            return JobExecutionView.builder()
                    .requestMessage("default message")
                    .exitCode(ExternalExitStatus.COMPLETED)
                    .id(1L)
                    .name("default job name")
                    .status(COMPLETED)
                    .yarnApplicationTrackingUrl("default yarn url")
                    .createUser("default create user");
        }

        public static DatasetQueryParams.DatasetQueryParamsBuilder datasetQueryParams() {
            return DatasetQueryParams.builder()
                    .name("Dataset")
                    .entityCode("1234")
                    .referenceDate(LocalDate.of(2000, 1, 1))
                    .pageRequest(ClientCore.Populated.pageRequest().build());
        }

        public static Dataset.DatasetBuilder datasetDto() {
            return Dataset.builder()
                    .name("Dataset")
                    .id(88L)
                    .predicate("WHERE 2=2");
        }

        public static DatasetMetadata.DatasetMetadataBuilder datasetMetadata() {
            return DatasetMetadata.builder()
                    .entityCode("entityCode")
                    .referenceDate("01/01/2001");
        }

        @Deprecated
        public static StagingRequest.StagingRequestBuilder stagingRequest() {
            return StagingRequest.builder()
                    .name("name")
                    .items(newHashSet(stagingItemRequest().build()));
        }

        @Deprecated
        public static StagingItemRequest.StagingItemRequestBuilder stagingItemRequest() {
            return StagingItemRequest.builder()
                    .dataset(datasetMetadata().build())
                    .schema("table")
                    .autoValidate(true)
                    .source(dataSource().build());
        }

        public static StagingRequestV2.StagingRequestV2Builder stagingRequestV2() {
            return StagingRequestV2.builder()
                    .name("name")
                    .metadata(datasetMetadata().build())
                    .items(newHashSet(stagingItemRequestV2().build()))
                    .downstreamPipelines(emptySet());
        }

        public static StagingItemRequestV2.StagingItemRequestV2Builder stagingItemRequestV2() {
            return StagingItemRequestV2.builder()
                    .schema("table")
                    .autoValidate(true)
                    .source(dataSource().build());
        }

        public static PipelineRequest.PipelineRequestBuilder pipelineItemRequest() {
            return PipelineRequest.builder()
                    .name("pipelineJob")
                    .pipelineId(123L)
                    .entityCode("ent")
                    .referenceDate(LocalDate.of(100, 1, 1));
        }

        public static DataSource.DataSourceBuilder dataSource() {
            return DataSource.builder()
                    .filePath("path/to/file")
                    .header(true);
        }

        public static ValidationJobRequest.ValidationJobRequestBuilder validationJobRequest() {
            return ValidationJobRequest.builder()
                    .datasetId(3L)
                    .name("Validate Dataset 3");
        }

        public static ValidationRuleSummaryView.ValidationRuleSummaryViewBuilder validationRuleSummaryView() {
            return ValidationRuleSummaryView.builder()
                    .datasetId(123L)
                    .errorMessage("Ooops")
                    .numberOfFailures(100L)
                    .status(SUCCESS)
                    .totalRecords(100L)
                    .validationRule(validationRuleExport().build());
        }

        public static PagedDataset pageFromList(final List<Dataset> datasetViews) {
            return new PagedDataset(
                    new PagedDataset.Embedded(datasetViews),
                    new Page(0, datasetViews.size(), datasetViews.size(), 1));
        }

        public static ValidationRuleExport.ValidationRuleExportBuilder validationRuleExport() {
            return ValidationRuleExport.builder()
                    .id(1L)
                    .name("Rule1")
                    .description("RuleOne")
                    .ruleId("RULE1_")
                    .validationRuleSeverity(CRITICAL)
                    .validationRuleType(SYNTAX)
                    .version(1)
                    .expression("DECIMAL == 0.01")
                    .startDate(LocalDate.of(2000, 1, 1))
                    .endDate(LocalDate.of(2000, 2, 1));
        }

        public static FieldExport.StringFieldExport.StringFieldExportBuilder stringFieldExport() {
            return FieldExport.StringFieldExport.builder()
                    .name("string field")
                    .maxLength(100)
                    .minLength(10)
                    .regularExpression("regex");
        }

        public static ValidationResultsDetailView.ValidationResultsDetailViewBuilder validationResultsDetailView() {
            return ValidationResultsDetailView.builder()
                    .page(ClientCore.Populated.page().build())
                    .data(singletonList(validationDetailRowView()))
                    .schema(singletonList(stringFieldExport().build()));
        }

        private static Map<String, Object> validationDetailRowView() {
            Map<String, Object> detailRowView = new HashMap<>();
            detailRowView.put("field", "HELLO");
            return detailRowView;
        }

        public static ProductConfigExport.ProductConfigExportBuilder productConfigExport() {
            return ProductConfigExport.builder()
                    .name("default product config name")
                    .version("default product config version")
                    .tables(emptyList());
        }

        public static SchemaExport.SchemaExportBuilder schemaExport() {
            return SchemaExport.builder()
                    .id(432L)
                    .physicalTableName("TBL_NM")
                    .displayName("Table Name")
                    .createdBy("Me")
                    .createdTime(new Date())
                    .hasDatasets(true)
                    .startDate(toDate(LocalDateTime.of(2001, 1, 1, 0, 0)))
                    .endDate(toDate(LocalDateTime.of(2001, 6, 1, 0, 0)))
                    .fields(emptyList())
                    .validationRules(emptyList());
        }

        public static Date toDate(final LocalDateTime localDateTime) {
            return Date.from(localDateTime.toInstant(ZoneOffset.UTC));
        }

        public static ObjectNode schemaExportJsonNode() {
            JsonNode periodObject = MapperWrapper.MAPPER.createObjectNode()
                    .put("startDate", "2001-01-01")
                    .put("endDate", "2001-12-01");

            ObjectNode objectNode = MapperWrapper.MAPPER.createObjectNode()
                    .put("physicalTableName", "TBL_NM")
                    .put("displayName", "Table Name")
                    .put("version", 1)
                    .put("startDate", toDate(LocalDateTime.of(2001, 1, 1, 0, 0))
                            .getTime())
                    .put("endDate", toDate(LocalDateTime.of(2001, 6, 1, 0, 0))
                            .getTime());

            objectNode.set("period", periodObject);
            objectNode.putArray("fields")
                    .add(fieldExportJsonNode());
            objectNode.putArray("validationRules")
                    .add(validationRuleExportJsonNode());

            return objectNode;
        }

        public static ObjectNode validationRuleExportJsonNode() {
            ObjectNode ruleNode = MapperWrapper.MAPPER.createObjectNode()
                    .put("name", "Rule1")
                    .put("ruleId", "RULE1_")
                    .put("description", "RuleOne")
                    .put("version", 1)
                    .put("validationRuleSeverity", "CRITICAL")
                    .put("validationRuleType", "SYNTAX")
                    .put("expression", "DECIMAL == 0.01")
                    .put("startDate", "2001-01-01")
                    .put("endDate", "2001-06-01");

            ruleNode.putArray("contextFields");
            return ruleNode;
        }

        public static ObjectNode fieldExportJsonNode() {

            return MapperWrapper.MAPPER.createObjectNode()
                    .put("name", "DECIMAL")
                    .put("type", "decimal")
                    .put("scale", "10")
                    .put("precision", 2);
        }

        public static ObjectNode pipelineExportJsonNode() {
            ObjectNode pipelineNode = MapperWrapper.MAPPER.createObjectNode()
                    .put("name", "My Pipeline");

            pipelineNode.putArray("steps");
            return pipelineNode;
        }

        public static ObjectNode schemaReferenceJsonNode() {
            return MapperWrapper.MAPPER.createObjectNode()
                    .put("physicalTableName", "TBL_NM")
                    .put("displayName", "Table Name")
                    .put("version", 1);
        }

        public static ObjectNode pipelineStepSelectJsonNode() {
            return MapperWrapper.MAPPER.createObjectNode()
                    .put("select", "MyField")
                    .put("outputFieldName", "MyOtherField")
                    .put("isWindow", false);
        }

        public static PipelineExport.PipelineExportBuilder pipelineExport() {
            return PipelineExport.builder()
                    .name("pipeline")
                    .steps(singletonList(pipelineMapStepExport().build()));
        }

        public static PipelineMapStepExport.PipelineMapStepExportBuilder pipelineMapStepExport() {
            return PipelineMapStepExport.builder()
                    .name("step1")
                    .description("A plus B = C")
                    .schemaIn(schemaReference().build())
                    .schemaOut(schemaReference().physicalTableName("sch_2").displayName("schema2").build())
                    .selects(singletonList(SelectExport.builder().select("A + B").outputFieldName("C").build()));
        }

        public static PipelineUnionStepExport.PipelineUnionStepExportBuilder pipelineUnionStepExport() {
            return PipelineUnionStepExport.builder()
                    .name("step1")
                    .description("A plus B = C")
                    .schemaOut(schemaReference().physicalTableName("sch_2").displayName("schema2").build())
                    .unions(Arrays.asList(
                            unionExport()
                                    .unionInSchema(schemaReference().displayName("A")
                                            .physicalTableName("A")
                                            .build()).build(),
                            unionExport()
                                    .unionInSchema(schemaReference().displayName("B")
                                            .physicalTableName("B")
                                            .build()).build()));
        }

        public static UnionExport.UnionExportBuilder unionExport() {
            return UnionExport.builder()
                    .selects(singletonList(SelectExport.builder()
                            .select("A + B")
                            .outputFieldName("C")
                            .build()));
        }

        public static PipelineAggregationStepExport.PipelineAggregationStepExportBuilder pipelineAggregationStepExport() {
            return PipelineAggregationStepExport.builder()
                    .name("step1")
                    .description("sum of B = C")
                    .schemaIn(schemaReference().build())
                    .schemaOut(schemaReference().physicalTableName("sch_2").displayName("schema2").build())
                    .selects(singletonList(SelectExport.builder().select("sum(B)").outputFieldName("C").build()))
                    .groupings(singletonList("A"));
        }

        public static PipelineJoinStepExport.PipelineJoinStepExportBuilder pipelineJoinStepExport() {
            return PipelineJoinStepExport.builder()
                    .name("step1")
                    .description("A joined to B")
                    .schemaOut(schemaReference().physicalTableName("sch_2").displayName("schema2").build())
                    .selects(Arrays.asList(
                            SelectExport.builder().select("A.NAME").outputFieldName("NAME").build(),
                            SelectExport.builder().select("B.ROLE").outputFieldName("ROLE").build()))
                    .joins(singletonList(JoinExport.builder()
                            .left(schemaReference().physicalTableName("A").displayName("A").build())
                            .right(schemaReference().physicalTableName("B").displayName("B").build())
                            .joinFields(Arrays.asList(
                                    JoinFieldExport.builder()
                                            .leftColumn("EMPLOYEE_ID")
                                            .rightColumn("EMPLOYEE_ID")
                                            .build(),
                                    JoinFieldExport.builder()
                                            .leftColumn("NAME")
                                            .rightColumn("NAME")
                                            .build()))
                            .type(JoinType.INNER)
                            .build()));
        }

        public static PipelineWindowStepExport.PipelineWindowStepExportBuilder pipelineWindowStepExport() {
            return PipelineWindowStepExport.builder()
                    .name("window")
                    .description("step with window function")
                    .schemaIn(schemaReference().build())
                    .schemaOut(schemaReference().build())
                    .selects(singletonList(
                            SelectExport.builder()
                                    .select("window()")
                                    .outputFieldName("C")
                                    .isWindow(true)
                                    .window(WindowExport.builder()
                                            .partitionBy(singleton("A"))
                                            .orderBy(singletonList(OrderExport.builder()
                                                    .fieldName("B")
                                                    .direction("DESC")
                                                    .priority(0)
                                                    .build()))
                                            .build())
                                    .build()));
        }

        public static PipelineScriptletStepExport.PipelineScriptletStepExportBuilder pipelineScriptletStepExport() {
            return PipelineScriptletStepExport.builder()
                    .name("scriptlet step")
                    .description("custom scriptlet step")
                    .jarFile("my-custom-scriptlet.jar")
                    .className("com.ignis.MyCustomStep")
                    .schemasIn(Arrays.asList(
                            ScriptletInputExport.builder()
                                    .inputName("InputA")
                                    .inputSchema(schemaReference().physicalTableName("A").displayName("a").build())
                                    .build(),
                            ScriptletInputExport.builder()
                                    .inputName("InputB")
                                    .inputSchema(schemaReference().physicalTableName("B").displayName("b").build())
                                    .build()))
                    .schemaOut(schemaReference().physicalTableName("C").displayName("c").build());
        }

        public static SelectExport.SelectExportBuilder selectExport() {
            return SelectExport.builder()
                    .isWindow(false)
                    .window(WindowExport.builder().partitionBy(emptySet()).orderBy(emptyList()).build());
        }

        public static SchemaReference.SchemaReferenceBuilder schemaReference() {
            return SchemaReference.builder()
                    .displayName("schema1")
                    .physicalTableName("sch_1")
                    .version(1);
        }

        public static Page.PageBuilder page() {
            return Page.builder()
                    .totalPages(100)
                    .size(1)
                    .totalElements(100)
                    .number(2);
        }

        public static DatasetRowDataView.DatasetRowDataViewBuilder datasetRowDataView() {
            return DatasetRowDataView.builder()
                    .datasetId(123L)
                    .data(singletonList(ImmutableMap.of("ROW_KEY", 1L, "NAME", "LRM")))
                    .page(page().build());
        }

        public static DrillBackStepDetails.DrillBackStepDetailsBuilder drillBackStepDetails() {
            return DrillBackStepDetails.builder()
                    .schemasIn(singletonMap(
                            1L, singletonList(fieldView().name("name").build())))
                    .schemasOut(singletonMap(
                            2L, singletonList(fieldView().name("name").build())));
        }

        public static ProductConfigView.ProductConfigViewBuilder productConfigView() {
            return ProductConfigView.builder()
                    .importStatus("SUCCESS")
                    .name("Prod 1")
                    .version("1.0.0")
                    .pipelines(singletonList(pipelineView().build()))
                    .schemas(singletonList(schemaView().build()));
        }

        public static FieldView.FieldViewBuilder fieldView() {
            return FieldView.builder()
                    .name("field")
                    .fieldType(FieldView.Type.STRING);
        }

        public static SchemaView.SchemaViewBuilder schemaView() {
            return SchemaView.builder()
                    .id(432L)
                    .physicalTableName("TBL_NM")
                    .displayName("Table Name")
                    .createdBy("Me")
                    .createdTime(new Date())
                    .hasDatasets(true)
                    .startDate(LocalDate.of(2001, 1, 1))
                    .endDate(LocalDate.of(2001, 6, 1));
        }

        public static ValidationRuleView.ValidationRuleViewBuilder validationRuleView() {
            return ValidationRuleView.builder()
                    .id(1L)
                    .name("Rule1")
                    .description("RuleOne")
                    .ruleId("RULE1_")
                    .validationRuleSeverity(CRITICAL)
                    .validationRuleType(SYNTAX)
                    .version(1)
                    .expression("DECIMAL == 0.01")
                    .startDate(LocalDate.of(2000, 1, 1))
                    .endDate(LocalDate.of(2000, 2, 1));
        }

        public static SchemaDetailsView.SchemaDetailsViewBuilder schemaDetailsView() {
            return SchemaDetailsView.builder()
                    .id(8L)
                    .physicalTableName("TBL")
                    .displayName("TABLE")
                    .version(1);
        }

        public static SchemaDetailsView.SchemaDetailsViewBuilder schemaDetailsView(
                final String name, final int version) {

            return SchemaDetailsView.builder()
                    .id(8L)
                    .physicalTableName(name)
                    .displayName(name)
                    .version(version);
        }

        public static PipelineView.PipelineViewBuilder pipelineView() {
            return PipelineView.builder()
                    .id(6262L)
                    .name("step1");
        }

        public static PipelineStepView.PipelineStepViewBuilder pipelineStepView() {
            return PipelineStepView.builder()
                    .id(6262L)
                    .name("step1")
                    .description("Step One")
                    .type(TransformationType.MAP)
                    .schemaIn(schemaDetailsView().build())
                    .schemaOut(schemaDetailsView().version(2).build());
        }

        public static SelectView.SelectViewBuilder selectView() {
            return SelectView.builder()
                    .select("A")
                    .outputFieldId(123412341234L)
                    .isWindow(false)
                    .window(WindowView.builder().partitionBy(emptySet()).orderBy(emptyList()).build());
        }

        public static PipelineInvocationView.PipelineInvocationViewBuilder pipelineInvocationView() {
            return PipelineInvocationView.builder()
                    .id(1L)
                    .pipelineId(99L)
                    .entityCode("entityCode")
                    .referenceDate(LocalDate.of(2001, 1, 1))
                    .invocationSteps(singletonList(pipelineStepInvocationView()
                            .pipelineStep(pipelineStepView().build())
                            .build()));
        }

        public static PipelineStepInvocationView.PipelineStepInvocationViewBuilder pipelineStepInvocationView() {
            return PipelineStepInvocationView.builder()
                    .id(8282L)
                    .datasetsIn(singleton(PipelineStepInvocationDatasetView.builder()
                            .datasetId(21L)
                            .datasetRunKey(92L)
                            .build()))
                    .pipelineStep(pipelineStepView().build());
        }
    }
}
