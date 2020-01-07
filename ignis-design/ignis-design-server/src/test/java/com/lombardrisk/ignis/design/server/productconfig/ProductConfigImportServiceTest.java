package com.lombardrisk.ignis.design.server.productconfig;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.lombardrisk.ignis.client.design.pipeline.CreatePipelineRequest;
import com.lombardrisk.ignis.client.design.pipeline.PipelineView;
import com.lombardrisk.ignis.client.design.productconfig.ProductConfigDto;
import com.lombardrisk.ignis.client.external.fixture.ExternalClient;
import com.lombardrisk.ignis.client.external.pipeline.export.step.JoinType;
import com.lombardrisk.ignis.common.assertions.VavrAssert;
import com.lombardrisk.ignis.data.common.Identifiable;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.design.field.model.Field;
import com.lombardrisk.ignis.design.server.pipeline.PipelineService;
import com.lombardrisk.ignis.design.server.pipeline.model.Pipeline;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineAggregationStep;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineJoinStep;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineMapStep;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineUnionStep;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineWindowStep;
import com.lombardrisk.ignis.design.server.pipeline.model.join.Join;
import com.lombardrisk.ignis.design.server.pipeline.model.join.JoinField;
import com.lombardrisk.ignis.design.server.pipeline.model.select.Order;
import com.lombardrisk.ignis.design.server.pipeline.model.select.PipelineFilter;
import com.lombardrisk.ignis.design.server.pipeline.model.select.Select;
import com.lombardrisk.ignis.design.server.pipeline.model.select.Union;
import com.lombardrisk.ignis.design.server.pipeline.model.select.Window;
import com.lombardrisk.ignis.design.server.productconfig.export.ProductExportTestUtils;
import com.lombardrisk.ignis.design.server.productconfig.fixture.ProductServiceFixtureFactory;
import com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRule;
import com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRuleSeverity;
import com.lombardrisk.ignis.design.server.productconfig.rule.model.ValidationRuleType;
import com.lombardrisk.ignis.design.server.productconfig.schema.Schema;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.lombardrisk.ignis.client.design.productconfig.ProductConfigDto.ImportStatus.ERROR;
import static com.lombardrisk.ignis.client.design.productconfig.ProductConfigDto.ImportStatus.SUCCESS;
import static com.lombardrisk.ignis.common.json.MapperWrapper.MAPPER;
import static com.lombardrisk.ignis.design.server.fixtures.Design.Populated;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

@SuppressWarnings("unchecked")
public class ProductConfigImportServiceTest {

    private ProductConfigService productConfigService;
    private ProductConfigImportService productConfigImportService;
    private PipelineService pipelineService;

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Before
    public void setUp() {
        ProductServiceFixtureFactory factory = ProductServiceFixtureFactory.create();
        productConfigService = factory.getProductService();
        productConfigImportService = factory.getProductImportService();
        pipelineService = factory.getPipelineDependencies().getPipelineService();
    }

    @Test
    public void importProduct_ProductNameExists_ReturnsFailure() throws Exception {
        productConfigService.saveProductConfig(Populated.newProductRequest("taken").build());

        String productManifest = "{ \"name\" : \"taken\", \"version\" : \"1.0.0-RELEASE\"}";

        ProductConfig productConfig = Populated.productConfig().name("taken").build();
        ByteArrayInputStream byteInputStream = ProductExportTestUtils.productToInputStream(productManifest);

        VavrAssert.assertCollectionFailure(
                productConfigImportService
                        .importProductConfig("testZip.zip", byteInputStream))
                .withFailure(CRUDFailure.constraintFailure("Product already exists with name taken").toErrorResponse());
    }

    @Test
    public void importProduct_SetsSchemaLatest() throws Exception {
        String productManifest = "{ \"name\" : \"Three versions\", \"version\" : \"1.0.0-RELEASE\"}";
        List<ObjectNode> schemaNodes = asList(
                ExternalClient.Populated.schemaExportJsonNode()
                        .put("displayName", "schema")
                        .put("version", 1),
                ExternalClient.Populated.schemaExportJsonNode()
                        .put("displayName", "schema")
                        .put("version", 2),
                ExternalClient.Populated.schemaExportJsonNode()
                        .put("displayName", "schema")
                        .put("version", 3));

        ByteArrayInputStream byteInputStream =
                ProductExportTestUtils.productToInputStream(productManifest, schemaNodes);

        Long productId = VavrAssert.assertValid(
                productConfigImportService.importProductConfig("test.zip", byteInputStream))
                .getResult()
                .getId();

        ProductConfig productConfig = productConfigService.findById(productId).get();

        assertThat(productConfig.getTables())
                .extracting(Schema::getLatest)
                .contains(false, false, true);
    }

    @Test
    public void importProduct_SchemaHasPeriod_PeriodStartDateSetOnSchema() throws Exception {
        String productManifest = "{ \"name\" : \"Dates\", \"version\" : \"1.0.0-RELEASE\"}";
        ObjectNode schemaJson = ExternalClient.Populated.schemaExportJsonNode();
        schemaJson.put("startDate", 0);
        schemaJson.put("endDate", 0);

        schemaJson.set("period", MAPPER.createObjectNode()
                .put("startDate", "2001-12-12"));

        ByteArrayInputStream byteInputStream = ProductExportTestUtils.productToInputStream(
                productManifest, singletonList(schemaJson));

        Long productId = VavrAssert.assertValid(
                productConfigImportService.importProductConfig("test.zip", byteInputStream))
                .getResult()
                .getId();

        ProductConfig productConfig = productConfigService.findById(productId).get();

        Schema schema = productConfig.getTables().iterator().next();
        soft.assertThat(schema.getStartDate())
                .isEqualTo(LocalDate.of(2001, 12, 12));
        soft.assertThat(schema.getEndDate())
                .isNull();
    }

    @Test
    public void importProduct_SchemaHasSameStartEndDate_ReturnsError() throws Exception {
        String productManifest = "{ \"name\" : \"Dates\", \"version\" : \"1.0.0-RELEASE\"}";
        ObjectNode schemaJson = ExternalClient.Populated.schemaExportJsonNode();
        schemaJson.set("period", MAPPER.createObjectNode()
                .put("startDate", "2001-12-12")
                .put("endDate", "2001-12-12"));

        ByteArrayInputStream byteInputStream = ProductExportTestUtils.productToInputStream(
                productManifest, singletonList(schemaJson));

        VavrAssert.assertCollectionFailure(productConfigImportService.importProductConfig("test.zip", byteInputStream))
                .withFailure(ErrorResponse.valueOf(
                        "Schema end date [2001-12-12] must be at least one day after start date, [2001-12-12]",
                        "CONSTRAINT_VIOLATION"));
    }

    @Test
    public void importProduct_SchemaDoesNotHavePeriod_StartDateAndEndDateSetOnSchema() throws Exception {
        String productManifest = "{ \"name\" : \"Dates\", \"version\" : \"1.0.0-RELEASE\"}";
        ObjectNode schemaJson = ExternalClient.Populated.schemaExportJsonNode();
        schemaJson.put("startDate", LocalDateTime.of(2018, 1, 1, 0, 0, 0)
                .toInstant(ZoneOffset.UTC)
                .toEpochMilli());
        schemaJson.put("endDate", LocalDateTime.of(2018, 6, 2, 0, 0, 0)
                .toInstant(ZoneOffset.UTC)
                .toEpochMilli());

        schemaJson.remove("period");

        ByteArrayInputStream byteInputStream = ProductExportTestUtils.productToInputStream(
                productManifest, singletonList(schemaJson));

        Long productId = VavrAssert.assertValid(
                productConfigImportService.importProductConfig("test.zip", byteInputStream))
                .getResult()
                .getId();

        ProductConfig productConfig = productConfigService.findById(productId).get();

        Schema schema = productConfig.getTables().iterator().next();
        soft.assertThat(schema.getStartDate())
                .isEqualTo(LocalDate.of(2018, 1, 1));
        soft.assertThat(schema.getEndDate())
                .isEqualTo(LocalDate.of(2018, 6, 2));
    }

    @Test
    public void importProduct_SchemaDoesNotHavePeriodOrEndDate_StartDateSetOnSchemaAndEndDateNull() throws Exception {
        String productManifest = "{ \"name\" : \"Dates\", \"version\" : \"1.0.0-RELEASE\"}";
        ObjectNode schemaJson = ExternalClient.Populated.schemaExportJsonNode();
        schemaJson.put("startDate", LocalDateTime.of(2018, 1, 1, 0, 0, 0)
                .toInstant(ZoneOffset.UTC)
                .toEpochMilli());

        schemaJson.remove("endDate");
        schemaJson.remove("period");

        ByteArrayInputStream byteInputStream = ProductExportTestUtils.productToInputStream(
                productManifest, singletonList(schemaJson));

        Long productId = VavrAssert.assertValid(
                productConfigImportService.importProductConfig("test.zip", byteInputStream))
                .getResult()
                .getId();

        ProductConfig productConfig = productConfigService.findById(productId).get();

        Schema schema = productConfig.getTables().iterator().next();
        soft.assertThat(schema.getStartDate())
                .isEqualTo(LocalDate.of(2018, 1, 1));
        soft.assertThat(schema.getEndDate())
                .isNull();
    }

    @Test
    public void importProduct_SchemaExistsForDisplayNameAndVersion_ReturnsError() throws Exception {
        Long productId = VavrAssert.assertValid(
                productConfigService.createProductConfig(Populated.newProductRequest().name("other").build()))
                .getResult().getId();

        VavrAssert.assertValid(productConfigService.createNewSchemaOnProduct(
                productId, Populated.createSchemaRequest().displayName("disp").majorVersion(3).build()));

        String productManifest = "{ \"name\" : \"Dates\", \"version\" : \"1.0.0-RELEASE\"}";
        ObjectNode schemaJson = ExternalClient.Populated.schemaExportJsonNode();
        schemaJson.put("displayName", "disp");
        schemaJson.put("version", 3);

        ByteArrayInputStream byteInputStream = ProductExportTestUtils.productToInputStream(
                productManifest, singletonList(schemaJson));

        VavrAssert.assertCollectionFailure(
                productConfigImportService.importProductConfig("test.zip", byteInputStream))
                .withFailure(ErrorResponse.valueOf(
                        "Schema(s) exist for display name, [disp] and version [3]",
                        "CONSTRAINT_VIOLATION"));
    }

    @Test
    public void importProduct_SchemaExistsForPhysicalNameAndVersion_ReturnsError() throws Exception {
        Long productId = VavrAssert.assertValid(
                productConfigService.createProductConfig(Populated.newProductRequest().name("other").build()))
                .getResult().getId();

        VavrAssert.assertValid(productConfigService.createNewSchemaOnProduct(
                productId, Populated.createSchemaRequest().physicalTableName("PHYS").majorVersion(3).build()));

        String productManifest = "{ \"name\" : \"Dates\", \"version\" : \"1.0.0-RELEASE\"}";
        ObjectNode schemaJson = ExternalClient.Populated.schemaExportJsonNode();
        schemaJson.put("physicalTableName", "PHYS");
        schemaJson.put("version", 3);

        ByteArrayInputStream byteInputStream = ProductExportTestUtils.productToInputStream(
                productManifest, singletonList(schemaJson));

        VavrAssert.assertCollectionFailure(
                productConfigImportService.importProductConfig("test.zip", byteInputStream))
                .withFailure(ErrorResponse.valueOf(
                        "Schema(s) exist for physical table name, [PHYS] and version [3]",
                        "CONSTRAINT_VIOLATION"));
    }

    @Test
    public void importProduct_SavesValidationRules() throws Exception {
        String productManifest = "{ \"name\" : \"Three versions\", \"version\" : \"1.0.0-RELEASE\"}";
        ObjectNode schemaJson = ExternalClient.Populated.schemaExportJsonNode();
        schemaJson.putArray("fields")
                .add(ExternalClient.Populated.fieldExportJsonNode()
                        .put("name", "LOCOM"));

        ArrayNode validationRules = schemaJson.putArray("validationRules");
        validationRules.add(
                ExternalClient.Populated.validationRuleExportJsonNode()
                        .put("name", "LOCOM")
                        .put("ruleId", "FRY14_CIL_12")
                        .put("description", "LOCOM MUST BE 3.0")
                        .put("version", 2)
                        .put("expression", "LOCOM == 3.0")
                        .put("validationRuleSeverity", "CRITICAL")
                        .put("validationRuleType", "SYNTAX")
                        .put("startDate", "2001-01-01")
                        .put("endDate", "2001-06-01")
        );

        ByteArrayInputStream byteInputStream = ProductExportTestUtils.productToInputStream(
                productManifest, singletonList(schemaJson));

        Long productId = VavrAssert.assertValid(
                productConfigImportService.importProductConfig("test.zip", byteInputStream))
                .getResult()
                .getId();

        ProductConfig productConfig = productConfigService.findById(productId).get();

        Schema savedSchema = productConfig.getTables().iterator().next();
        ValidationRule validationRule = savedSchema.getValidationRules().iterator().next();

        soft.assertThat(validationRule.getRuleId())
                .isEqualTo("FRY14_CIL_12");
        soft.assertThat(validationRule.getName())
                .isEqualTo("LOCOM");
        soft.assertThat(validationRule.getDescription())
                .isEqualTo("LOCOM MUST BE 3.0");
        soft.assertThat(validationRule.getVersion())
                .isEqualTo(2);
        soft.assertThat(validationRule.getValidationRuleSeverity())
                .isEqualTo(ValidationRuleSeverity.CRITICAL);
        soft.assertThat(validationRule.getValidationRuleType())
                .isEqualTo(ValidationRuleType.SYNTAX);
        soft.assertThat(validationRule.getExpression())
                .isEqualTo("LOCOM == 3.0");
        soft.assertThat(validationRule.getStartDate())
                .isEqualTo(LocalDate.of(2001, 1, 1));
        soft.assertThat(validationRule.getEndDate())
                .isEqualTo(LocalDate.of(2001, 6, 1));
    }

    @Test
    public void importProduct_ValidationRulesValid_UpdatesProductImportStatusToSuccess() throws Exception {
        String productManifest = "{ \"name\" : \"Three versions\", \"version\" : \"1.0.0-RELEASE\"}";
        ObjectNode schemaJson = ExternalClient.Populated.schemaExportJsonNode();
        schemaJson.putArray("fields")
                .add(ExternalClient.Populated.fieldExportJsonNode()
                        .put("name", "LOCOM"));

        ArrayNode validationRules = schemaJson.putArray("validationRules");
        validationRules.add(
                ExternalClient.Populated.validationRuleExportJsonNode()
                        .put("expression", "LOCOM == 3.0")
        );

        ByteArrayInputStream byteInputStream = ProductExportTestUtils.productToInputStream(
                productManifest, singletonList(schemaJson));

        Long productId =
                VavrAssert.assertValid(productConfigImportService.importProductConfig("test.zip", byteInputStream))
                        .getResult()
                        .getId();

        List<ProductConfigDto> productConfigs = productConfigService.findAllProductConfigs();
        assertThat(productConfigs)
                .hasSize(1);

        assertThat(productConfigs.get(0).getImportStatus())
                .isEqualTo(SUCCESS);
    }

    @Test
    public void importProduct_InvalidRule_ErrorsAndDoesNotSave() throws Exception {
        String productManifest = "{ \"name\" : \"Three versions\", \"version\" : \"1.0.0-RELEASE\"}";
        ObjectNode schemaJson = ExternalClient.Populated.schemaExportJsonNode()
                .put("physicalTableName", "BAD_SCHEMA");

        schemaJson.putArray("fields")
                .add(ExternalClient.Populated.fieldExportJsonNode()
                        .put("name", "LOCOM"));

        ArrayNode validationRules = schemaJson.putArray("validationRules");
        validationRules.add(
                ExternalClient.Populated.validationRuleExportJsonNode()
                        .put("expression", "NOT_THERE == 3.0")
        );

        ByteArrayInputStream byteInputStream = ProductExportTestUtils.productToInputStream(
                productManifest, singletonList(schemaJson));

        List<ErrorResponse> expectedErrors = asList(
                ErrorResponse.valueOf("Undefined fields", "expression"),
                ErrorResponse.valueOf(
                        "Fields: NOT_THERE must be defined in schema BAD_SCHEMA",
                        "Undefined fields"));

        VavrAssert.assertFailed(productConfigImportService.importProductConfig("test.zip", byteInputStream))
                .withFailure(expectedErrors);
    }

    @Test
    public void importProduct_InvalidRule_UpdatesProductToErrorImportStatus() throws Exception {
        String productManifest = "{ \"name\" : \"Three versions\", \"version\" : \"1.0.0-RELEASE\"}";
        ObjectNode schemaJson = ExternalClient.Populated.schemaExportJsonNode()
                .put("physicalTableName", "BAD_SCHEMA");

        schemaJson.putArray("fields")
                .add(ExternalClient.Populated.fieldExportJsonNode()
                        .put("name", "LOCOM"));

        ArrayNode validationRules = schemaJson.putArray("validationRules");
        validationRules.add(
                ExternalClient.Populated.validationRuleExportJsonNode()
                        .put("expression", "NOT_THERE == 3.0")
        );

        ByteArrayInputStream byteInputStream = ProductExportTestUtils.productToInputStream(
                productManifest, singletonList(schemaJson));

        VavrAssert.assertFailed(
                productConfigImportService.importProductConfig("test.zip", byteInputStream));

        List<ProductConfigDto> productConfigs = productConfigService.findAllProductConfigs();
        assertThat(productConfigs).hasSize(1);
        ProductConfigDto productConfig = productConfigs.get(0);
        assertThat(productConfig.getImportStatus())
                .isEqualTo(ERROR);
    }

    @Test
    public void importProduct_NoClearLatestVersion_ReturnsConstraintFailures() throws Exception {
        String productManifest = "{ \"name\" : \"Three versions\", \"version\" : \"1.0.0-RELEASE\"}";
        List<ObjectNode> schemaNodes = asList(
                ExternalClient.Populated.schemaExportJsonNode()
                        .put("displayName", "TEST")
                        .put("version", 1)
                        .put("startDate", "1992-01-01")
                        .remove(singletonList("period")),
                ExternalClient.Populated.schemaExportJsonNode()
                        .put("displayName", "TEST")
                        .put("version", 1)
                        .put("startDate", "1993-01-01")
                        .remove(singletonList("period")),
                ExternalClient.Populated.schemaExportJsonNode()
                        .put("displayName", "TEST")
                        .put("version", 1)
                        .put("startDate", "1993-01-01")
                        .remove(singletonList("period")),
                ExternalClient.Populated.schemaExportJsonNode()
                        .put("displayName", "TEST")
                        .put("version", 2)
                        .put("startDate", "1993-01-01")
                        .remove(singletonList("period")),
                ExternalClient.Populated.schemaExportJsonNode()
                        .put("displayName", "TEST")
                        .put("version", 2)
                        .put("startDate", "1994-01-01")
                        .remove(singletonList("period")));

        ByteArrayInputStream byteInputStream =
                ProductExportTestUtils.productToInputStream(productManifest, schemaNodes);

        VavrAssert.assertCollectionFailure(
                productConfigImportService.importProductConfig("test.zip", byteInputStream))
                .withFailure(CRUDFailure.constraintFailure("Schema TEST has duplicates for versions [1, 2]")
                        .toErrorResponse());
    }

    @Test
    public void importProduct_PipelineWithNoSteps_SavesPipeline() throws Exception {
        String productManifest = "{ \"name\" : \"Product with Pipeline\", \"version\" : \"1.0.0-RELEASE\"}";
        List<ObjectNode> pipelines = singletonList(ExternalClient.Populated.pipelineExportJsonNode());

        ByteArrayInputStream byteArrayInputStream =
                ProductExportTestUtils.productToInputStream(productManifest, emptyList(), pipelines);

        Identifiable productId = VavrAssert.assertValid(
                productConfigImportService.importProductConfig("test.zip", byteArrayInputStream)).getResult();

        List<Pipeline> importedPipelines = pipelineService.findByProductId(productId.getId());

        soft.assertThat(importedPipelines)
                .hasSize(1);

        soft.assertThat(importedPipelines.get(0).getName())
                .isEqualTo("My Pipeline");

        soft.assertThat(importedPipelines.get(0).getProductId())
                .isEqualTo(productId.getId());

        soft.assertThat(importedPipelines.get(0).getSteps())
                .isEmpty();
    }

    @Test
    public void importProduct_PipelineWithNameAlreadyExists_DoesNotSavePipeline() throws Exception {
        PipelineView existingPipelineWithName =
                VavrAssert.assertValid(pipelineService.saveNewPipeline(CreatePipelineRequest.builder()
                        .name("My Pipeline")
                        .productId(1L)
                        .build())).getResult();

        String productManifest = "{ \"name\" : \"Product with Pipeline\", \"version\" : \"1.0.0-RELEASE\"}";
        List<ObjectNode> pipelines = singletonList(ExternalClient.Populated.pipelineExportJsonNode());

        ByteArrayInputStream byteArrayInputStream =
                ProductExportTestUtils.productToInputStream(productManifest, emptyList(), pipelines);

        List<ErrorResponse> errors = VavrAssert.assertFailed(
                productConfigImportService.importProductConfig("test.zip", byteArrayInputStream)).getValidation();

        soft.assertThat(errors)
                .containsExactly(ErrorResponse.valueOf(
                        "Pipeline 'My Pipeline' already exists", "CONSTRAINT_VIOLATION"));

        soft.assertThat(productConfigService.findAll())
                .isEmpty();

        soft.assertThat(pipelineService.findAll())
                .extracting(Pipeline::getId)
                .containsExactly(existingPipelineWithName.getId());
    }

    @Test
    public void importProduct_InvalidPipelineJson_DoesNotSavePipeline() throws Exception {
        String productManifest = "{ \"name\" : \"Product with Pipeline\", \"version\" : \"1.0.0-RELEASE\"}";

        ObjectNode invalidPipelineJson = MAPPER.createObjectNode()
                .put("name", "My Pipeline")
                .put("something", "silly");

        List<ObjectNode> pipelines = singletonList(invalidPipelineJson);

        ByteArrayInputStream byteArrayInputStream =
                ProductExportTestUtils.productToInputStream(productManifest, emptyList(), pipelines);

        List<ErrorResponse> errors = VavrAssert.assertFailed(
                productConfigImportService.importProductConfig("test.zip", byteArrayInputStream))
                .getValidation();

        soft.assertThat(errors)
                .hasSize(1);

        soft.assertThat(errors.get(0).getErrorCode())
                .isEqualTo("JSON_FORMAT_INVALID");

        soft.assertThat(errors.get(0).getErrorMessage())
                .contains("pipelines/My Pipeline");

        soft.assertThat(productConfigService.findAll())
                .isEmpty();

        soft.assertThat(pipelineService.findAll())
                .isEmpty();
    }

    @Test
    public void importProduct_TwoPipelinesWithSameName_DoesNotSavePipeline() throws Exception {
        String productManifest = "{ \"name\" : \"Product with Pipeline\", \"version\" : \"1.0.0-RELEASE\"}";
        List<ObjectNode> pipelines = asList(
                ExternalClient.Populated.pipelineExportJsonNode().put("name", "the pipeline name"),
                ExternalClient.Populated.pipelineExportJsonNode().put("name", "the pipeline name"));

        ByteArrayInputStream byteArrayInputStream =
                ProductExportTestUtils.productToInputStream(productManifest, emptyList(), pipelines);

        List<ErrorResponse> errors = VavrAssert.assertFailed(
                productConfigImportService.importProductConfig("test.zip", byteArrayInputStream))
                .getValidation();

        soft.assertThat(errors)
                .containsExactly(ErrorResponse.valueOf(
                        "Pipelines with same name [the pipeline name]", "CONSTRAINT_VIOLATION"));

        soft.assertThat(productConfigService.findAll())
                .isEmpty();

        soft.assertThat(pipelineService.findAll())
                .isEmpty();
    }

    @Test
    public void importProduct_ProductConfigWithPipelineInvalid_DoesNotSavePipeline() throws Exception {
        String productManifest = "{ \"name\" : \"Product\", \"version\" : \"1.0.0-RELEASE\"}";

        List<ObjectNode> schemas = asList(
                ExternalClient.Populated.schemaExportJsonNode()
                        .put("displayName", "TEST")
                        .put("version", 1)
                        .put("startDate", "1992-01-01")
                        .remove(singletonList("period")),
                ExternalClient.Populated.schemaExportJsonNode()
                        .put("displayName", "TEST")
                        .put("version", 1)
                        .put("startDate", "1993-01-01")
                        .remove(singletonList("period")));

        List<ObjectNode> pipelines = singletonList(ExternalClient.Populated.pipelineExportJsonNode());

        ByteArrayInputStream byteArrayInputStream =
                ProductExportTestUtils.productToInputStream(productManifest, schemas, pipelines);

        List<ErrorResponse> errors = VavrAssert.assertFailed(
                productConfigImportService.importProductConfig("test.zip", byteArrayInputStream))
                .getValidation();

        soft.assertThat(errors)
                .containsExactly(ErrorResponse.valueOf(
                        "Schema TEST has duplicates for versions [1]", "CONSTRAINT_VIOLATION"));

        soft.assertThat(productConfigService.findAll())
                .isEmpty();

        soft.assertThat(pipelineService.findAll())
                .isEmpty();
    }

    @Test
    public void importProduct_PipelineWithMapStep_SavesPipeline() throws Exception {
        String productManifest = "{ \"name\" : \"Product with Pipeline\", \"version\" : \"1.0.0-RELEASE\"}";

        List<ObjectNode> schemas = asList(
                ExternalClient.Populated.schemaExportJsonNode()
                        .put("displayName", "Input Schema")
                        .put("physicalTableName", "SCHEMA_IN")
                        .putPOJO("validationRules", MAPPER.createArrayNode())
                        .putPOJO("fields", MAPPER.createArrayNode()
                                .add(ExternalClient.Populated.fieldExportJsonNode().put("name", "FieldA"))
                                .add(ExternalClient.Populated.fieldExportJsonNode().put("name", "FieldB"))),
                ExternalClient.Populated.schemaExportJsonNode()
                        .put("displayName", "Output Schema")
                        .put("physicalTableName", "SCHEMA_OUT")
                        .putPOJO("validationRules", MAPPER.createArrayNode())
                        .putPOJO("fields", MAPPER.createArrayNode()
                                .add(ExternalClient.Populated.fieldExportJsonNode().put("name", "FieldC"))));

        ObjectNode mapStep = MAPPER.createObjectNode()
                .put("name", "map step")
                .put("description", "this step maps")
                .put("type", "MAP")
                .putPOJO("schemaIn", ExternalClient.Populated.schemaReferenceJsonNode()
                        .put("physicalTableName", "SCHEMA_IN")
                        .put("displayName", "Input Schema"))
                .putPOJO("schemaOut", ExternalClient.Populated.schemaReferenceJsonNode()
                        .put("physicalTableName", "SCHEMA_OUT")
                        .put("displayName", "Output Schema"))
                .putPOJO("selects", MAPPER.createArrayNode()
                        .add(ExternalClient.Populated.pipelineStepSelectJsonNode()
                                .put("select", "FieldA + FieldB")
                                .put("outputFieldName", "FieldC")))
                .putPOJO("filters", MAPPER.createArrayNode()
                        .add("FieldA > 0")
                        .add("FieldB > 0"));

        List<ObjectNode> pipelines = singletonList(ExternalClient.Populated.pipelineExportJsonNode()
                .putPOJO("steps", MAPPER.createArrayNode().add(mapStep)));

        ByteArrayInputStream byteArrayInputStream =
                ProductExportTestUtils.productToInputStream(productManifest, schemas, pipelines);

        Identifiable product = VavrAssert.assertValid(
                productConfigImportService.importProductConfig("test.zip", byteArrayInputStream)).getResult();

        Set<Schema> importedSchemas = VavrAssert.assertValid(
                productConfigService.findWithValidation(product.getId())).getResult().getTables();

        assertThat(importedSchemas).hasSize(2);

        Schema inputSchema = findSchema("SCHEMA_IN", importedSchemas);
        Schema outputSchema = findSchema("SCHEMA_OUT", importedSchemas);

        List<Pipeline> importedPipelines = pipelineService.findByProductId(product.getId());

        assertThat(importedPipelines).hasSize(1);
        assertThat(importedPipelines.get(0).getSteps()).hasSize(1);
        assertThat(importedPipelines.get(0).getSteps().iterator().next()).isInstanceOf(PipelineMapStep.class);

        PipelineMapStep pipelineStep = (PipelineMapStep) importedPipelines.get(0).getSteps().iterator().next();

        soft.assertThat(pipelineStep)
                .extracting(PipelineMapStep::getName, PipelineMapStep::getDescription,
                        PipelineMapStep::getPipelineId, PipelineMapStep::getSchemaInId, PipelineMapStep::getSchemaOutId)
                .containsExactly("map step", "this step maps",
                        importedPipelines.get(0).getId(), inputSchema.getId(), outputSchema.getId());

        soft.assertThat(pipelineStep.getSelects())
                .extracting(Select::getSelect, Select::getOutputFieldId, Select::isWindow, Select::isUnion)
                .containsExactly(tuple("FieldA + FieldB", findField("FieldC", outputSchema).getId(), false, false));

        soft.assertThat(pipelineStep.getFilters())
                .containsExactlyInAnyOrder("FieldA > 0", "FieldB > 0");
    }

    @Test
    public void importProduct_PipelineWithAggregationStep_SavesPipeline() throws Exception {
        String productManifest = "{ \"name\" : \"Product with Pipeline\", \"version\" : \"1.0.0-RELEASE\"}";

        List<ObjectNode> schemas = asList(
                ExternalClient.Populated.schemaExportJsonNode()
                        .put("displayName", "Employees")
                        .put("physicalTableName", "EMPLOYEE")
                        .putPOJO("validationRules", MAPPER.createArrayNode())
                        .putPOJO("fields", MAPPER.createArrayNode()
                                .add(ExternalClient.Populated.fieldExportJsonNode().put("name", "Department"))
                                .add(ExternalClient.Populated.fieldExportJsonNode().put("name", "Salary"))),
                ExternalClient.Populated.schemaExportJsonNode()
                        .put("displayName", "Departments")
                        .put("physicalTableName", "DEPARTMENT")
                        .putPOJO("validationRules", MAPPER.createArrayNode())
                        .putPOJO("fields", MAPPER.createArrayNode()
                                .add(ExternalClient.Populated.fieldExportJsonNode().put("name", "Department"))
                                .add(ExternalClient.Populated.fieldExportJsonNode().put("name", "AverageSalary"))));

        ObjectNode aggregationStep = MAPPER.createObjectNode()
                .put("name", "aggregation step")
                .put("description", "this step aggregates")
                .put("type", "AGGREGATION")
                .putPOJO("schemaIn", ExternalClient.Populated.schemaReferenceJsonNode()
                        .put("physicalTableName", "EMPLOYEE")
                        .put("displayName", "Employees"))
                .putPOJO("schemaOut", ExternalClient.Populated.schemaReferenceJsonNode()
                        .put("physicalTableName", "DEPARTMENT")
                        .put("displayName", "Departments"))
                .putPOJO("selects", MAPPER.createArrayNode()
                        .add(ExternalClient.Populated.pipelineStepSelectJsonNode()
                                .put("select", "Department")
                                .put("outputFieldName", "Department"))
                        .add(ExternalClient.Populated.pipelineStepSelectJsonNode()
                                .put("select", "avg(Salary)")
                                .put("outputFieldName", "AverageSalary")))
                .putPOJO("groupings", MAPPER.createArrayNode()
                        .add("Department"))
                .putPOJO("filters", MAPPER.createArrayNode()
                        .add("Department != null"));

        List<ObjectNode> pipelines = singletonList(ExternalClient.Populated.pipelineExportJsonNode()
                .putPOJO("steps", MAPPER.createArrayNode().add(aggregationStep)));

        ByteArrayInputStream byteArrayInputStream =
                ProductExportTestUtils.productToInputStream(productManifest, schemas, pipelines);

        Identifiable product = VavrAssert.assertValid(
                productConfigImportService.importProductConfig("test.zip", byteArrayInputStream)).getResult();

        Set<Schema> importedSchemas = VavrAssert.assertValid(
                productConfigService.findWithValidation(product.getId())).getResult().getTables();

        assertThat(importedSchemas).hasSize(2);

        Schema inputSchema = findSchema("EMPLOYEE", importedSchemas);
        Schema outputSchema = findSchema("DEPARTMENT", importedSchemas);

        List<Pipeline> importedPipelines = pipelineService.findByProductId(product.getId());

        assertThat(importedPipelines).hasSize(1);
        assertThat(importedPipelines.get(0).getSteps()).hasSize(1);
        assertThat(importedPipelines.get(0).getSteps().iterator().next()).isInstanceOf(PipelineAggregationStep.class);

        PipelineAggregationStep pipelineStep =
                (PipelineAggregationStep) importedPipelines.get(0).getSteps().iterator().next();

        soft.assertThat(pipelineStep)
                .extracting(
                        PipelineAggregationStep::getName,
                        PipelineAggregationStep::getDescription,
                        PipelineAggregationStep::getPipelineId,
                        PipelineAggregationStep::getSchemaInId,
                        PipelineAggregationStep::getSchemaOutId)
                .containsExactly("aggregation step", "this step aggregates",
                        importedPipelines.get(0).getId(), inputSchema.getId(), outputSchema.getId());

        soft.assertThat(pipelineStep.getSelects())
                .extracting(Select::getSelect, Select::getOutputFieldId, Select::isWindow, Select::isUnion)
                .containsExactlyInAnyOrder(
                        tuple("Department", findField("Department", outputSchema).getId(), false, false),
                        tuple("avg(Salary)", findField("AverageSalary", outputSchema).getId(), false, false));

        soft.assertThat(pipelineStep.getGroupings())
                .containsExactly("Department");

        soft.assertThat(pipelineStep.getFilters())
                .containsExactly("Department != null");
    }

    @Test
    public void importProduct_PipelineWithJoinStep_SavesPipeline() throws Exception {
        String productManifest = "{ \"name\" : \"Product with Pipeline\", \"version\" : \"1.0.0-RELEASE\"}";

        List<ObjectNode> schemas = asList(
                ExternalClient.Populated.schemaExportJsonNode()
                        .put("displayName", "Left Schema")
                        .put("physicalTableName", "LEFT")
                        .putPOJO("validationRules", MAPPER.createArrayNode())
                        .putPOJO("fields", MAPPER.createArrayNode()
                                .add(ExternalClient.Populated.fieldExportJsonNode().put("name", "ID"))
                                .add(ExternalClient.Populated.fieldExportJsonNode().put("name", "LeftField"))),
                ExternalClient.Populated.schemaExportJsonNode()
                        .put("displayName", "Right Schema")
                        .put("physicalTableName", "RIGHT")
                        .putPOJO("validationRules", MAPPER.createArrayNode())
                        .putPOJO("fields", MAPPER.createArrayNode()
                                .add(ExternalClient.Populated.fieldExportJsonNode().put("name", "ID"))
                                .add(ExternalClient.Populated.fieldExportJsonNode().put("name", "RightField"))),
                ExternalClient.Populated.schemaExportJsonNode()
                        .put("displayName", "Output Schema")
                        .put("physicalTableName", "LEFT_AND_RIGHT")
                        .putPOJO("validationRules", MAPPER.createArrayNode())
                        .putPOJO("fields", MAPPER.createArrayNode()
                                .add(ExternalClient.Populated.fieldExportJsonNode().put("name", "ID"))
                                .add(ExternalClient.Populated.fieldExportJsonNode().put("name", "Left"))
                                .add(ExternalClient.Populated.fieldExportJsonNode().put("name", "Right"))));

        ObjectNode joinStep = MAPPER.createObjectNode()
                .put("name", "join step")
                .put("description", "this step joins")
                .put("type", "JOIN")
                .putPOJO("joins", MAPPER.createArrayNode()
                        .add(MAPPER.createObjectNode()
                                .putPOJO("left", ExternalClient.Populated.schemaReferenceJsonNode()
                                        .put("physicalTableName", "LEFT").put("displayName", "Left Schema"))
                                .putPOJO("right", ExternalClient.Populated.schemaReferenceJsonNode()
                                        .put("physicalTableName", "RIGHT").put("displayName", "Right Schema"))
                                .putPOJO("joinFields", MAPPER.createArrayNode().add(MAPPER.createObjectNode()
                                        .put("leftColumn", "ID")
                                        .put("rightColumn", "ID")))
                                .putPOJO("type", "INNER")))
                .putPOJO("schemaOut", ExternalClient.Populated.schemaReferenceJsonNode()
                        .put("physicalTableName", "LEFT_AND_RIGHT")
                        .put("displayName", "Output Schema"))
                .putPOJO("selects", MAPPER.createArrayNode()
                        .add(ExternalClient.Populated.pipelineStepSelectJsonNode()
                                .put("select", "LEFT.ID")
                                .put("outputFieldName", "ID"))
                        .add(ExternalClient.Populated.pipelineStepSelectJsonNode()
                                .put("select", "LeftField")
                                .put("outputFieldName", "Left"))
                        .add(ExternalClient.Populated.pipelineStepSelectJsonNode()
                                .put("select", "RightField")
                                .put("outputFieldName", "Right")));

        List<ObjectNode> pipelines = singletonList(ExternalClient.Populated.pipelineExportJsonNode()
                .putPOJO("steps", MAPPER.createArrayNode().add(joinStep)));

        ByteArrayInputStream byteArrayInputStream =
                ProductExportTestUtils.productToInputStream(productManifest, schemas, pipelines);

        Identifiable product = VavrAssert.assertValid(
                productConfigImportService.importProductConfig("test.zip", byteArrayInputStream)).getResult();

        Set<Schema> importedSchemas = VavrAssert.assertValid(
                productConfigService.findWithValidation(product.getId())).getResult().getTables();

        assertThat(importedSchemas).hasSize(3);

        Schema leftSchema = findSchema("LEFT", importedSchemas);
        Schema rightSchema = findSchema("RIGHT", importedSchemas);
        Schema outputSchema = findSchema("LEFT_AND_RIGHT", importedSchemas);

        List<Pipeline> importedPipelines = pipelineService.findByProductId(product.getId());

        assertThat(importedPipelines).hasSize(1);
        assertThat(importedPipelines.get(0).getSteps()).hasSize(1);
        assertThat(importedPipelines.get(0).getSteps().iterator().next()).isInstanceOf(PipelineJoinStep.class);

        PipelineJoinStep pipelineStep = (PipelineJoinStep) importedPipelines.get(0).getSteps().iterator().next();

        soft.assertThat(pipelineStep)
                .extracting(PipelineJoinStep::getName, PipelineJoinStep::getDescription,
                        PipelineJoinStep::getPipelineId, PipelineJoinStep::getSchemaOutId)
                .containsExactly("join step", "this step joins",
                        importedPipelines.get(0).getId(), outputSchema.getId());

        soft.assertThat(pipelineStep.getJoins())
                .extracting(Join::getLeftSchemaId, Join::getRightSchemaId, Join::getJoinType)
                .containsExactly(tuple(leftSchema.getId(), rightSchema.getId(), JoinType.INNER));

        soft.assertThat(pipelineStep.getJoins().iterator().next().getJoinFields())
                .extracting(JoinField::getLeftJoinFieldId, JoinField::getRightJoinFieldId)
                .containsExactly(tuple(
                        findField("ID", leftSchema).getId(),
                        findField("ID", rightSchema).getId()));

        soft.assertThat(pipelineStep.getSelects())
                .extracting(Select::getSelect, Select::getOutputFieldId, Select::isWindow, Select::isUnion)
                .containsExactlyInAnyOrder(
                        tuple("LEFT.ID", findField("ID", outputSchema).getId(), false, false),
                        tuple("LeftField", findField("Left", outputSchema).getId(), false, false),
                        tuple("RightField", findField("Right", outputSchema).getId(), false, false));
    }

    @Test
    public void importProduct_PipelineWithInvalidJoinStepLeftRightJoinSchemas_DoesNotSavePipeline() throws Exception {
        String productManifest = "{ \"name\" : \"Product with Pipeline\", \"version\" : \"1.0.0-RELEASE\"}";

        List<ObjectNode> schemas = asList(
                ExternalClient.Populated.schemaExportJsonNode()
                        .put("displayName", "Left Schema")
                        .put("physicalTableName", "LEFT")
                        .put("version", 101)
                        .putPOJO("validationRules", MAPPER.createArrayNode())
                        .putPOJO("fields", MAPPER.createArrayNode()
                                .add(ExternalClient.Populated.fieldExportJsonNode().put("name", "ID"))
                                .add(ExternalClient.Populated.fieldExportJsonNode().put("name", "LeftField"))),
                ExternalClient.Populated.schemaExportJsonNode()
                        .put("displayName", "Right Schema")
                        .put("physicalTableName", "RIGHT")
                        .put("version", 101)
                        .putPOJO("validationRules", MAPPER.createArrayNode())
                        .putPOJO("fields", MAPPER.createArrayNode()
                                .add(ExternalClient.Populated.fieldExportJsonNode().put("name", "ID"))
                                .add(ExternalClient.Populated.fieldExportJsonNode().put("name", "RightField"))),
                ExternalClient.Populated.schemaExportJsonNode()
                        .put("displayName", "Output Schema")
                        .put("physicalTableName", "LEFT_AND_RIGHT")
                        .putPOJO("validationRules", MAPPER.createArrayNode())
                        .putPOJO("fields", MAPPER.createArrayNode()
                                .add(ExternalClient.Populated.fieldExportJsonNode().put("name", "ID"))
                                .add(ExternalClient.Populated.fieldExportJsonNode().put("name", "Left"))
                                .add(ExternalClient.Populated.fieldExportJsonNode().put("name", "Right"))));

        ObjectNode joinStep = MAPPER.createObjectNode()
                .put("name", "join step")
                .put("description", "this step joins")
                .put("type", "JOIN")
                .putPOJO("joins", MAPPER.createArrayNode()
                        .add(MAPPER.createObjectNode()
                                .putPOJO("left", ExternalClient.Populated.schemaReferenceJsonNode()
                                        .put("physicalTableName", "LEFT")
                                        .put("displayName", "Left Schema")
                                        .put("version", 999))
                                .putPOJO("right", ExternalClient.Populated.schemaReferenceJsonNode()
                                        .put("physicalTableName", "RIGHT")
                                        .put("displayName", "Wrong display name chump")
                                        .put("version", 101))
                                .putPOJO("joinFields", MAPPER.createArrayNode().add(MAPPER.createObjectNode()
                                        .put("leftColumn", "ID")
                                        .put("rightColumn", "ID")))
                                .putPOJO("type", "INNER")))
                .putPOJO("schemaOut", ExternalClient.Populated.schemaReferenceJsonNode()
                        .put("physicalTableName", "LEFT_AND_RIGHT")
                        .put("displayName", "Output Schema"))
                .putPOJO("selects", MAPPER.createArrayNode()
                        .add(ExternalClient.Populated.pipelineStepSelectJsonNode()
                                .put("select", "LEFT.ID")
                                .put("outputFieldName", "ID"))
                        .add(ExternalClient.Populated.pipelineStepSelectJsonNode()
                                .put("select", "LeftField")
                                .put("outputFieldName", "Left"))
                        .add(ExternalClient.Populated.pipelineStepSelectJsonNode()
                                .put("select", "RightField")
                                .put("outputFieldName", "Right")));

        List<ObjectNode> pipelines = singletonList(ExternalClient.Populated.pipelineExportJsonNode()
                .putPOJO("steps", MAPPER.createArrayNode().add(joinStep)));

        ByteArrayInputStream byteArrayInputStream =
                ProductExportTestUtils.productToInputStream(productManifest, schemas, pipelines);

        List<ErrorResponse> errors = VavrAssert.assertFailed(
                productConfigImportService.importProductConfig("test.zip", byteArrayInputStream)).getValidation();

        soft.assertThat(errors)
                .hasSize(1);

        soft.assertThat(errors.get(0).getErrorCode())
                .isEqualTo("CONSTRAINT_VIOLATION");

        soft.assertThat(errors.get(0).getErrorMessage())
                .startsWith("Pipeline step [join step] references invalid input schemas")
                .contains("LEFT")
                .contains("RIGHT");

        soft.assertThat(productConfigService.findAll())
                .isEmpty();

        soft.assertThat(pipelineService.findAll())
                .isEmpty();
    }

    @Test
    public void importProduct_PipelineWithInvalidJoinStepLeftRightJoinFields_DoesNotSavePipeline() throws Exception {
        String productManifest = "{ \"name\" : \"Product with Pipeline\", \"version\" : \"1.0.0-RELEASE\"}";

        List<ObjectNode> schemas = asList(
                ExternalClient.Populated.schemaExportJsonNode()
                        .put("displayName", "Left Schema")
                        .put("physicalTableName", "LEFT")
                        .putPOJO("validationRules", MAPPER.createArrayNode())
                        .putPOJO("fields", MAPPER.createArrayNode()
                                .add(ExternalClient.Populated.fieldExportJsonNode().put("name", "ID"))
                                .add(ExternalClient.Populated.fieldExportJsonNode().put("name", "LeftField"))),
                ExternalClient.Populated.schemaExportJsonNode()
                        .put("displayName", "Right Schema")
                        .put("physicalTableName", "RIGHT")
                        .putPOJO("validationRules", MAPPER.createArrayNode())
                        .putPOJO("fields", MAPPER.createArrayNode()
                                .add(ExternalClient.Populated.fieldExportJsonNode().put("name", "ID"))
                                .add(ExternalClient.Populated.fieldExportJsonNode().put("name", "RightField"))),
                ExternalClient.Populated.schemaExportJsonNode()
                        .put("displayName", "Output Schema")
                        .put("physicalTableName", "LEFT_AND_RIGHT")
                        .putPOJO("validationRules", MAPPER.createArrayNode())
                        .putPOJO("fields", MAPPER.createArrayNode()
                                .add(ExternalClient.Populated.fieldExportJsonNode().put("name", "ID"))
                                .add(ExternalClient.Populated.fieldExportJsonNode().put("name", "Left"))
                                .add(ExternalClient.Populated.fieldExportJsonNode().put("name", "Right"))));

        ObjectNode joinStep = MAPPER.createObjectNode()
                .put("name", "join step")
                .put("description", "this step joins")
                .put("type", "JOIN")
                .putPOJO("joins", MAPPER.createArrayNode()
                        .add(MAPPER.createObjectNode()
                                .putPOJO("left", ExternalClient.Populated.schemaReferenceJsonNode()
                                        .put("physicalTableName", "LEFT")
                                        .put("displayName", "Left Schema"))
                                .putPOJO("right", ExternalClient.Populated.schemaReferenceJsonNode()
                                        .put("physicalTableName", "RIGHT")
                                        .put("displayName", "Right Schema"))
                                .putPOJO("joinFields", MAPPER.createArrayNode().add(MAPPER.createObjectNode()
                                        .put("leftColumn", "WrongField")
                                        .put("rightColumn", "WrongField")))
                                .putPOJO("type", "INNER")))
                .putPOJO("schemaOut", ExternalClient.Populated.schemaReferenceJsonNode()
                        .put("physicalTableName", "LEFT_AND_RIGHT")
                        .put("displayName", "Output Schema"))
                .putPOJO("selects", MAPPER.createArrayNode()
                        .add(ExternalClient.Populated.pipelineStepSelectJsonNode()
                                .put("select", "LEFT.ID")
                                .put("outputFieldName", "ID"))
                        .add(ExternalClient.Populated.pipelineStepSelectJsonNode()
                                .put("select", "LeftField")
                                .put("outputFieldName", "Left"))
                        .add(ExternalClient.Populated.pipelineStepSelectJsonNode()
                                .put("select", "RightField")
                                .put("outputFieldName", "Right")));

        List<ObjectNode> pipelines = singletonList(ExternalClient.Populated.pipelineExportJsonNode()
                .putPOJO("steps", MAPPER.createArrayNode().add(joinStep)));

        ByteArrayInputStream byteArrayInputStream =
                ProductExportTestUtils.productToInputStream(productManifest, schemas, pipelines);

        List<ErrorResponse> errors = VavrAssert.assertFailed(
                productConfigImportService.importProductConfig("test.zip", byteArrayInputStream)).getValidation();

        soft.assertThat(errors)
                .hasSize(1);

        soft.assertThat(errors.get(0).getErrorCode())
                .isEqualTo("CONSTRAINT_VIOLATION");

        soft.assertThat(errors.get(0).getErrorMessage())
                .startsWith("Pipeline step [join step] references invalid fields")
                .contains("LEFT.WrongField")
                .contains("RIGHT.WrongField");

        soft.assertThat(productConfigService.findAll())
                .isEmpty();

        soft.assertThat(pipelineService.findAll())
                .isEmpty();
    }

    @Test
    public void importProduct_PipelineWithWindowStep_SavesPipeline() throws Exception {
        String productManifest = "{ \"name\" : \"Product with Pipeline\", \"version\" : \"1.0.0-RELEASE\"}";

        List<ObjectNode> schemas = asList(
                ExternalClient.Populated.schemaExportJsonNode()
                        .put("displayName", "Employees")
                        .put("physicalTableName", "EMPLOYEE")
                        .putPOJO("validationRules", MAPPER.createArrayNode())
                        .putPOJO("fields", MAPPER.createArrayNode()
                                .add(ExternalClient.Populated.fieldExportJsonNode().put("name", "Name"))
                                .add(ExternalClient.Populated.fieldExportJsonNode().put("name", "Department"))
                                .add(ExternalClient.Populated.fieldExportJsonNode().put("name", "Salary"))),
                ExternalClient.Populated.schemaExportJsonNode()
                        .put("displayName", "Employees Ranked")
                        .put("physicalTableName", "EMPLOYEE_RANKED")
                        .putPOJO("validationRules", MAPPER.createArrayNode())
                        .putPOJO("fields", MAPPER.createArrayNode()
                                .add(ExternalClient.Populated.fieldExportJsonNode().put("name", "Name"))
                                .add(ExternalClient.Populated.fieldExportJsonNode().put("name", "Department"))
                                .add(ExternalClient.Populated.fieldExportJsonNode().put("name", "Salary"))
                                .add(ExternalClient.Populated.fieldExportJsonNode().put("name", "Rank"))));

        ObjectNode mapStep = MAPPER.createObjectNode()
                .put("name", "window step")
                .put("description", "this step ranks employees within department")
                .put("type", "WINDOW")
                .putPOJO("schemaIn", ExternalClient.Populated.schemaReferenceJsonNode()
                        .put("physicalTableName", "EMPLOYEE")
                        .put("displayName", "Employees"))
                .putPOJO("schemaOut", ExternalClient.Populated.schemaReferenceJsonNode()
                        .put("physicalTableName", "EMPLOYEE_RANKED")
                        .put("displayName", "Employees Ranked"))
                .putPOJO("selects", MAPPER.createArrayNode()
                        .add(ExternalClient.Populated.pipelineStepSelectJsonNode()
                                .put("select", "Name").put("outputFieldName", "Name"))
                        .add(ExternalClient.Populated.pipelineStepSelectJsonNode()
                                .put("select", "Department").put("outputFieldName", "Department"))
                        .add(ExternalClient.Populated.pipelineStepSelectJsonNode()
                                .put("select", "Salary").put("outputFieldName", "Salary"))
                        .add(ExternalClient.Populated.pipelineStepSelectJsonNode()
                                .put("select", "rank()")
                                .put("outputFieldName", "Rank")
                                .put("isWindow", true)
                                .putPOJO("window", MAPPER.createObjectNode()
                                        .putPOJO("partitionBy", MAPPER.createArrayNode()
                                                .add("Department"))
                                        .putPOJO("orderBy", MAPPER.createArrayNode().add(MAPPER.createObjectNode()
                                                .put("fieldName", "Salary")
                                                .put("direction", "DESC")
                                                .put("priority", 1))))));

        List<ObjectNode> pipelines = singletonList(ExternalClient.Populated.pipelineExportJsonNode()
                .putPOJO("steps", MAPPER.createArrayNode().add(mapStep)));

        ByteArrayInputStream byteArrayInputStream =
                ProductExportTestUtils.productToInputStream(productManifest, schemas, pipelines);

        Identifiable product = VavrAssert.assertValid(
                productConfigImportService.importProductConfig("test.zip", byteArrayInputStream)).getResult();

        Set<Schema> importedSchemas = VavrAssert.assertValid(
                productConfigService.findWithValidation(product.getId())).getResult().getTables();

        assertThat(importedSchemas).hasSize(2);

        Schema inputSchema = findSchema("EMPLOYEE", importedSchemas);
        Schema outputSchema = findSchema("EMPLOYEE_RANKED", importedSchemas);

        List<Pipeline> importedPipelines = pipelineService.findByProductId(product.getId());

        assertThat(importedPipelines).hasSize(1);
        assertThat(importedPipelines.get(0).getSteps()).hasSize(1);
        assertThat(importedPipelines.get(0).getSteps().iterator().next()).isInstanceOf(PipelineWindowStep.class);

        PipelineWindowStep pipelineStep = (PipelineWindowStep) importedPipelines.get(0).getSteps().iterator().next();

        soft.assertThat(pipelineStep)
                .extracting(
                        PipelineWindowStep::getName,
                        PipelineWindowStep::getDescription,
                        PipelineWindowStep::getPipelineId,
                        PipelineWindowStep::getSchemaInId,
                        PipelineWindowStep::getSchemaOutId)
                .containsExactly("window step", "this step ranks employees within department",
                        importedPipelines.get(0).getId(), inputSchema.getId(), outputSchema.getId());

        soft.assertThat(pipelineStep.getSelects())
                .extracting(Select::getSelect, Select::getOutputFieldId, Select::isWindow, Select::getWindow)
                .containsExactlyInAnyOrder(
                        tuple("Name", findField("Name", outputSchema).getId(), false, Window.none()),
                        tuple("Department", findField("Department", outputSchema).getId(), false, Window.none()),
                        tuple("Salary", findField("Salary", outputSchema).getId(), false, Window.none()),
                        tuple("rank()", findField("Rank", outputSchema).getId(), true,
                                Window.builder()
                                        .partitions(singleton("Department"))
                                        .orders(singleton(Order.builder()
                                                .fieldName("Salary")
                                                .direction(Order.Direction.DESC)
                                                .priority(1)
                                                .build()))
                                        .build()));
    }

    @Test
    public void importProduct_PipelineWithUnionStep_SavesPipeline() throws Exception {
        String productManifest = "{ \"name\" : \"Product with Pipeline\", \"version\" : \"1.0.0-RELEASE\"}";

        List<ObjectNode> schemas = asList(
                ExternalClient.Populated.schemaExportJsonNode()
                        .put("displayName", "Schema A")
                        .put("physicalTableName", "SCHEMA_A")
                        .putPOJO("validationRules", MAPPER.createArrayNode())
                        .putPOJO("fields", MAPPER.createArrayNode()
                                .add(ExternalClient.Populated.fieldExportJsonNode().put("name", "NameA"))),
                ExternalClient.Populated.schemaExportJsonNode()
                        .put("displayName", "Schema B")
                        .put("physicalTableName", "SCHEMA_B")
                        .putPOJO("validationRules", MAPPER.createArrayNode())
                        .putPOJO("fields", MAPPER.createArrayNode()
                                .add(ExternalClient.Populated.fieldExportJsonNode().put("name", "NameB"))),
                ExternalClient.Populated.schemaExportJsonNode()
                        .put("displayName", "Schema C")
                        .put("physicalTableName", "SCHEMA_C")
                        .putPOJO("validationRules", MAPPER.createArrayNode())
                        .putPOJO("fields", MAPPER.createArrayNode()
                                .add(ExternalClient.Populated.fieldExportJsonNode().put("name", "NameC"))));

        ObjectNode mapStep = MAPPER.createObjectNode()
                .put("name", "union step")
                .put("description", "this step unions two datasets")
                .put("type", "UNION")
                .putPOJO("schemaOut", ExternalClient.Populated.schemaReferenceJsonNode()
                        .put("physicalTableName", "SCHEMA_C")
                        .put("displayName", "Schema C"))
                .putPOJO("unions", MAPPER.createArrayNode()
                        .add(MAPPER.createObjectNode()
                                .putPOJO("unionInSchema", ExternalClient.Populated.schemaReferenceJsonNode()
                                        .put("physicalTableName", "SCHEMA_A")
                                        .put("displayName", "Schema A"))
                                .putPOJO("selects", MAPPER.createArrayNode()
                                        .add(ExternalClient.Populated.pipelineStepSelectJsonNode()
                                                .put("select", "NameA")
                                                .put("outputFieldName", "NameC"))))
                        .add(MAPPER.createObjectNode()
                                .putPOJO("unionInSchema", ExternalClient.Populated.schemaReferenceJsonNode()
                                        .put("physicalTableName", "SCHEMA_B")
                                        .put("displayName", "Schema B"))
                                .putPOJO("selects", MAPPER.createArrayNode()
                                        .add(ExternalClient.Populated.pipelineStepSelectJsonNode()
                                                .put("select", "NameB")
                                                .put("outputFieldName", "NameC")))
                                .putPOJO("filters", MAPPER.createArrayNode()
                                        .add("NameB LIKE 'B%'"))));

        List<ObjectNode> pipelines = singletonList(ExternalClient.Populated.pipelineExportJsonNode()
                .putPOJO("steps", MAPPER.createArrayNode().add(mapStep)));

        ByteArrayInputStream byteArrayInputStream =
                ProductExportTestUtils.productToInputStream(productManifest, schemas, pipelines);

        Identifiable product = VavrAssert.assertValid(
                productConfigImportService.importProductConfig("test.zip", byteArrayInputStream)).getResult();

        Set<Schema> importedSchemas = VavrAssert.assertValid(
                productConfigService.findWithValidation(product.getId())).getResult().getTables();

        assertThat(importedSchemas).hasSize(3);

        Schema schemaA = findSchema("SCHEMA_A", importedSchemas);
        Schema schemaB = findSchema("SCHEMA_B", importedSchemas);
        Schema schemaC = findSchema("SCHEMA_C", importedSchemas);

        List<Pipeline> importedPipelines = pipelineService.findByProductId(product.getId());

        assertThat(importedPipelines).hasSize(1);
        assertThat(importedPipelines.get(0).getSteps()).hasSize(1);
        assertThat(importedPipelines.get(0).getSteps().iterator().next()).isInstanceOf(PipelineUnionStep.class);

        PipelineUnionStep pipelineStep = (PipelineUnionStep) importedPipelines.get(0).getSteps().iterator().next();

        soft.assertThat(pipelineStep)
                .extracting(
                        PipelineUnionStep::getName,
                        PipelineUnionStep::getDescription,
                        PipelineUnionStep::getPipelineId,
                        PipelineUnionStep::getSchemaOutId)
                .containsExactly("union step", "this step unions two datasets",
                        importedPipelines.get(0).getId(),
                        schemaC.getId());

        soft.assertThat(pipelineStep.getSchemaInIds())
                .containsExactlyInAnyOrder(schemaA.getId(), schemaB.getId());

        soft.assertThat(pipelineStep.getSelects())
                .extracting(Select::getSelect, Select::getOutputFieldId, Select::isUnion, Select::getUnion)
                .containsExactlyInAnyOrder(
                        tuple("NameA", findField("NameC", schemaC).getId(), true, Union.forSchema(schemaA.getId())),
                        tuple("NameB", findField("NameC", schemaC).getId(), true, Union.forSchema(schemaB.getId())));

        soft.assertThat(pipelineStep.getPipelineFilters())
                .extracting(PipelineFilter::getFilter, PipelineFilter::getUnionSchemaId)
                .containsExactlyInAnyOrder(tuple("NameB LIKE 'B%'", schemaB.getId()));
    }

    @Test
    public void importProduct_PipelineWithInvalidUnionStepInputSchema_DoesNotSavePipeline() throws Exception {
        String productManifest = "{ \"name\" : \"Product with Pipeline\", \"version\" : \"1.0.0-RELEASE\"}";

        List<ObjectNode> schemas = asList(
                ExternalClient.Populated.schemaExportJsonNode()
                        .put("displayName", "Schema C")
                        .put("physicalTableName", "SCHEMA_C")
                        .putPOJO("validationRules", MAPPER.createArrayNode())
                        .putPOJO("fields", MAPPER.createArrayNode()
                                .add(ExternalClient.Populated.fieldExportJsonNode().put("name", "NameC"))));

        ObjectNode mapStep = MAPPER.createObjectNode()
                .put("name", "union step")
                .put("description", "this step unions two datasets")
                .put("type", "UNION")
                .putPOJO("schemaOut", ExternalClient.Populated.schemaReferenceJsonNode()
                        .put("physicalTableName", "SCHEMA_C")
                        .put("displayName", "Schema C"))
                .putPOJO("unions", MAPPER.createArrayNode()
                        .add(MAPPER.createObjectNode()
                                .putPOJO("unionInSchema", ExternalClient.Populated.schemaReferenceJsonNode()
                                        .put("physicalTableName", "SCHEMA_A")
                                        .put("displayName", "Schema A"))
                                .putPOJO("selects", MAPPER.createArrayNode()
                                        .add(ExternalClient.Populated.pipelineStepSelectJsonNode()
                                                .put("select", "NameA")
                                                .put("outputFieldName", "NameC"))))
                        .add(MAPPER.createObjectNode()
                                .putPOJO("unionInSchema", ExternalClient.Populated.schemaReferenceJsonNode()
                                        .put("physicalTableName", "SCHEMA_B")
                                        .put("displayName", "Schema B"))
                                .putPOJO("selects", MAPPER.createArrayNode()
                                        .add(ExternalClient.Populated.pipelineStepSelectJsonNode()
                                                .put("select", "NameB")
                                                .put("outputFieldName", "NameC")))));

        List<ObjectNode> pipelines = singletonList(ExternalClient.Populated.pipelineExportJsonNode()
                .putPOJO("steps", MAPPER.createArrayNode().add(mapStep)));

        ByteArrayInputStream byteArrayInputStream =
                ProductExportTestUtils.productToInputStream(productManifest, schemas, pipelines);

        List<ErrorResponse> errors = VavrAssert.assertFailed(
                productConfigImportService.importProductConfig("test.zip", byteArrayInputStream)).getValidation();

        soft.assertThat(errors)
                .hasSize(1);

        soft.assertThat(errors.get(0).getErrorCode())
                .isEqualTo("CONSTRAINT_VIOLATION");

        soft.assertThat(errors.get(0).getErrorMessage())
                .startsWith("Pipeline step [union step] references invalid input schemas")
                .contains("SCHEMA_A")
                .contains("SCHEMA_B");

        soft.assertThat(productConfigService.findAll())
                .isEmpty();

        soft.assertThat(pipelineService.findAll())
                .isEmpty();
    }

    @Test
    public void importProduct_PipelineWithInvalidUnionOutputFieldNames_DoesNotSavePipeline() throws Exception {
        String productManifest = "{ \"name\" : \"Product with Pipeline\", \"version\" : \"1.0.0-RELEASE\"}";

        List<ObjectNode> schemas = asList(
                ExternalClient.Populated.schemaExportJsonNode()
                        .put("displayName", "Schema A")
                        .put("physicalTableName", "SCHEMA_A")
                        .putPOJO("validationRules", MAPPER.createArrayNode())
                        .putPOJO("fields", MAPPER.createArrayNode()
                                .add(ExternalClient.Populated.fieldExportJsonNode().put("name", "NameA"))),
                ExternalClient.Populated.schemaExportJsonNode()
                        .put("displayName", "Schema B")
                        .put("physicalTableName", "SCHEMA_B")
                        .putPOJO("validationRules", MAPPER.createArrayNode())
                        .putPOJO("fields", MAPPER.createArrayNode()
                                .add(ExternalClient.Populated.fieldExportJsonNode().put("name", "NameB"))),
                ExternalClient.Populated.schemaExportJsonNode()
                        .put("displayName", "Schema C")
                        .put("physicalTableName", "SCHEMA_C")
                        .putPOJO("validationRules", MAPPER.createArrayNode())
                        .putPOJO("fields", MAPPER.createArrayNode()
                                .add(ExternalClient.Populated.fieldExportJsonNode().put("name", "NameC"))));

        ObjectNode mapStep = MAPPER.createObjectNode()
                .put("name", "union step")
                .put("description", "this step unions two datasets")
                .put("type", "UNION")
                .putPOJO("schemaOut", ExternalClient.Populated.schemaReferenceJsonNode()
                        .put("physicalTableName", "SCHEMA_C")
                        .put("displayName", "Schema C"))
                .putPOJO("unions", MAPPER.createArrayNode()
                        .add(MAPPER.createObjectNode()
                                .putPOJO("unionInSchema", ExternalClient.Populated.schemaReferenceJsonNode()
                                        .put("physicalTableName", "SCHEMA_A")
                                        .put("displayName", "Schema A"))
                                .putPOJO("selects", MAPPER.createArrayNode()
                                        .add(ExternalClient.Populated.pipelineStepSelectJsonNode()
                                                .put("select", "NameA")
                                                .put("outputFieldName", "NotOnSchemaC"))))
                        .add(MAPPER.createObjectNode()
                                .putPOJO("unionInSchema", ExternalClient.Populated.schemaReferenceJsonNode()
                                        .put("physicalTableName", "SCHEMA_B")
                                        .put("displayName", "Schema B"))
                                .putPOJO("selects", MAPPER.createArrayNode()
                                        .add(ExternalClient.Populated.pipelineStepSelectJsonNode()
                                                .put("select", "NameB")
                                                .put("outputFieldName", "AlsoNotOnSchemaC")))));

        List<ObjectNode> pipelines = singletonList(ExternalClient.Populated.pipelineExportJsonNode()
                .putPOJO("steps", MAPPER.createArrayNode().add(mapStep)));

        ByteArrayInputStream byteArrayInputStream =
                ProductExportTestUtils.productToInputStream(productManifest, schemas, pipelines);

        List<ErrorResponse> errors = VavrAssert.assertFailed(
                productConfigImportService.importProductConfig("test.zip", byteArrayInputStream)).getValidation();

        soft.assertThat(errors)
                .hasSize(1);

        soft.assertThat(errors.get(0).getErrorCode())
                .isEqualTo("CONSTRAINT_VIOLATION");

        soft.assertThat(errors.get(0).getErrorMessage())
                .startsWith("Pipeline step [union step] references invalid fields")
                .contains("SCHEMA_C.NotOnSchemaC")
                .contains("SCHEMA_C.AlsoNotOnSchemaC");

        soft.assertThat(productConfigService.findAll())
                .isEmpty();

        soft.assertThat(pipelineService.findAll())
                .isEmpty();
    }

    @Test
    public void importProduct_PipelineStepInputOutputSchemaDoesNotExist_DoesNotSavePipeline() throws Exception {
        String productManifest = "{ \"name\" : \"Product with Pipeline\", \"version\" : \"1.0.0-RELEASE\"}";

        List<ObjectNode> schemas = asList(
                ExternalClient.Populated.schemaExportJsonNode()
                        .put("displayName", "Input Schema")
                        .put("physicalTableName", "SCHEMA_IN")
                        .putPOJO("validationRules", MAPPER.createArrayNode())
                        .putPOJO("fields", MAPPER.createArrayNode()
                                .add(ExternalClient.Populated.fieldExportJsonNode().put("name", "FieldA"))
                                .add(ExternalClient.Populated.fieldExportJsonNode().put("name", "FieldB"))),
                ExternalClient.Populated.schemaExportJsonNode()
                        .put("displayName", "Output Schema")
                        .put("physicalTableName", "SCHEMA_OUT")
                        .putPOJO("validationRules", MAPPER.createArrayNode())
                        .putPOJO("fields", MAPPER.createArrayNode()
                                .add(ExternalClient.Populated.fieldExportJsonNode().put("name", "FieldC"))));

        ObjectNode mapStep = MAPPER.createObjectNode()
                .put("name", "map step")
                .put("description", "this step maps")
                .put("type", "MAP")
                .putPOJO("schemaIn", ExternalClient.Populated.schemaReferenceJsonNode()
                        .put("physicalTableName", "INVALID_INPUT")
                        .put("displayName", "this schema doesn't exist"))
                .putPOJO("schemaOut", ExternalClient.Populated.schemaReferenceJsonNode()
                        .put("physicalTableName", "INVALID_OUTPUT")
                        .put("displayName", "this schema also doesn't exist"))
                .putPOJO("selects", MAPPER.createArrayNode()
                        .add(ExternalClient.Populated.pipelineStepSelectJsonNode()
                                .put("select", "FieldA + FieldB")
                                .put("outputFieldName", "FieldC")))
                .putPOJO("filters", MAPPER.createArrayNode()
                        .add("FieldA > 0")
                        .add("FieldB > 0"));

        List<ObjectNode> pipelines = singletonList(ExternalClient.Populated.pipelineExportJsonNode()
                .putPOJO("steps", MAPPER.createArrayNode().add(mapStep)));

        ByteArrayInputStream byteArrayInputStream =
                ProductExportTestUtils.productToInputStream(productManifest, schemas, pipelines);

        List<ErrorResponse> errors = VavrAssert.assertFailed(
                productConfigImportService.importProductConfig("test.zip", byteArrayInputStream))
                .getValidation();

        soft.assertThat(errors)
                .hasSize(2);

        soft.assertThat(errors)
                .extracting(ErrorResponse::getErrorCode)
                .containsOnly("CONSTRAINT_VIOLATION");

        soft.assertThat(errors)
                .extracting(ErrorResponse::getErrorMessage)
                .containsExactly(
                        "Pipeline step [map step] references invalid input schemas [INVALID_INPUT]",
                        "Pipeline step [map step] references invalid output schema [INVALID_OUTPUT]");

        soft.assertThat(productConfigService.findAll())
                .isEmpty();

        soft.assertThat(pipelineService.findAll())
                .isEmpty();
    }

    @Test
    public void importProduct_PipelineStepSelectOutputFieldDoesNotExistOnOutputSchema_DoesNotSavePipeline() throws Exception {
        String productManifest = "{ \"name\" : \"Product with Pipeline\", \"version\" : \"1.0.0-RELEASE\"}";

        List<ObjectNode> schemas = asList(
                ExternalClient.Populated.schemaExportJsonNode()
                        .put("displayName", "Employees")
                        .put("physicalTableName", "EMPLOYEE")
                        .putPOJO("validationRules", MAPPER.createArrayNode())
                        .putPOJO("fields", MAPPER.createArrayNode()
                                .add(ExternalClient.Populated.fieldExportJsonNode().put("name", "Department"))
                                .add(ExternalClient.Populated.fieldExportJsonNode().put("name", "Salary"))),
                ExternalClient.Populated.schemaExportJsonNode()
                        .put("displayName", "Departments")
                        .put("physicalTableName", "DEPARTMENT")
                        .putPOJO("validationRules", MAPPER.createArrayNode())
                        .putPOJO("fields", MAPPER.createArrayNode()
                                .add(ExternalClient.Populated.fieldExportJsonNode().put("name", "Department"))
                                .add(ExternalClient.Populated.fieldExportJsonNode().put("name", "AverageSalary"))));

        ObjectNode aggregationStep = MAPPER.createObjectNode()
                .put("name", "aggregation step")
                .put("description", "this step aggregates")
                .put("type", "AGGREGATION")
                .putPOJO("schemaIn", ExternalClient.Populated.schemaReferenceJsonNode()
                        .put("physicalTableName", "EMPLOYEE")
                        .put("displayName", "Employees"))
                .putPOJO("schemaOut", ExternalClient.Populated.schemaReferenceJsonNode()
                        .put("physicalTableName", "DEPARTMENT")
                        .put("displayName", "Departments"))
                .putPOJO("selects", MAPPER.createArrayNode()
                        .add(ExternalClient.Populated.pipelineStepSelectJsonNode()
                                .put("select", "Department")
                                .put("outputFieldName", "ThisFieldDoesNotExist"))
                        .add(ExternalClient.Populated.pipelineStepSelectJsonNode()
                                .put("select", "avg(Salary)")
                                .put("outputFieldName", "AverageSalary"))
                        .add(ExternalClient.Populated.pipelineStepSelectJsonNode()
                                .put("select", "null")
                                .put("outputFieldName", "ThisFieldDoesNotExistEither")))
                .putPOJO("groupings", MAPPER.createArrayNode()
                        .add("Department"))
                .putPOJO("filters", MAPPER.createArrayNode()
                        .add("Department != null"));

        List<ObjectNode> pipelines = singletonList(ExternalClient.Populated.pipelineExportJsonNode()
                .putPOJO("steps", MAPPER.createArrayNode().add(aggregationStep)));

        ByteArrayInputStream byteArrayInputStream =
                ProductExportTestUtils.productToInputStream(productManifest, schemas, pipelines);

        List<ErrorResponse> errors = VavrAssert.assertFailed(
                productConfigImportService.importProductConfig("test.zip", byteArrayInputStream))
                .getValidation();

        soft.assertThat(errors)
                .hasSize(1);

        soft.assertThat(errors.get(0).getErrorCode())
                .isEqualTo("CONSTRAINT_VIOLATION");

        soft.assertThat(errors.get(0).getErrorMessage())
                .startsWith("Pipeline step [aggregation step] references invalid fields")
                .contains("DEPARTMENT.ThisFieldDoesNotExist")
                .contains("DEPARTMENT.ThisFieldDoesNotExistEither");

        soft.assertThat(productConfigService.findAll())
                .isEmpty();

        soft.assertThat(pipelineService.findAll())
                .isEmpty();
    }

    @Test
    public void importProduct_PipelineWithMultipleVersionsOfSchema_SavesPipeline() throws Exception {
        String productManifest = "{ \"name\" : \"Product with Pipeline\", \"version\" : \"1.0.0-RELEASE\"}";

        List<ObjectNode> schemas = asList(
                ExternalClient.Populated.schemaExportJsonNode()
                        .put("physicalTableName", "SCHEMA_IN")
                        .put("version", 99)
                        .putPOJO("validationRules", MAPPER.createArrayNode())
                        .putPOJO("fields", MAPPER.createArrayNode()),
                ExternalClient.Populated.schemaExportJsonNode()
                        .put("physicalTableName", "SCHEMA_IN")
                        .put("version", 100)
                        .putPOJO("validationRules", MAPPER.createArrayNode())
                        .putPOJO("fields", MAPPER.createArrayNode()
                                .add(ExternalClient.Populated.fieldExportJsonNode().put("name", "FieldA"))
                                .add(ExternalClient.Populated.fieldExportJsonNode().put("name", "FieldB"))),
                ExternalClient.Populated.schemaExportJsonNode()
                        .put("physicalTableName", "SCHEMA_OUT")
                        .put("version", 777)
                        .putPOJO("validationRules", MAPPER.createArrayNode())
                        .putPOJO("fields", MAPPER.createArrayNode()),
                ExternalClient.Populated.schemaExportJsonNode()
                        .put("physicalTableName", "SCHEMA_OUT")
                        .put("version", 778)
                        .putPOJO("validationRules", MAPPER.createArrayNode())
                        .putPOJO("fields", MAPPER.createArrayNode()
                                .add(ExternalClient.Populated.fieldExportJsonNode().put("name", "FieldC"))));

        ObjectNode mapStep = MAPPER.createObjectNode()
                .put("name", "map step")
                .put("description", "this step maps")
                .put("type", "MAP")
                .putPOJO("schemaIn", ExternalClient.Populated.schemaReferenceJsonNode()
                        .put("physicalTableName", "SCHEMA_IN")
                        .put("version", 100))
                .putPOJO("schemaOut", ExternalClient.Populated.schemaReferenceJsonNode()
                        .put("physicalTableName", "SCHEMA_OUT")
                        .put("version", 778))
                .putPOJO("selects", MAPPER.createArrayNode()
                        .add(ExternalClient.Populated.pipelineStepSelectJsonNode()
                                .put("select", "FieldA + FieldB").put("outputFieldName", "FieldC")));

        List<ObjectNode> pipelines = singletonList(ExternalClient.Populated.pipelineExportJsonNode()
                .putPOJO("steps", MAPPER.createArrayNode().add(mapStep)));

        ByteArrayInputStream byteArrayInputStream =
                ProductExportTestUtils.productToInputStream(productManifest, schemas, pipelines);

        Identifiable product = VavrAssert.assertValid(
                productConfigImportService.importProductConfig("test.zip", byteArrayInputStream)).getResult();

        Set<Schema> importedSchemas = VavrAssert.assertValid(
                productConfigService.findWithValidation(product.getId())).getResult().getTables();

        List<Pipeline> importedPipelines = pipelineService.findByProductId(product.getId());

        assertThat(importedPipelines).hasSize(1);
        assertThat(importedPipelines.get(0).getSteps()).hasSize(1);
        assertThat(importedPipelines.get(0).getSteps().iterator().next()).isInstanceOf(PipelineMapStep.class);

        PipelineMapStep pipelineStep = (PipelineMapStep) importedPipelines.get(0).getSteps().iterator().next();

        soft.assertThat(importedSchemas).hasSize(4);

        soft.assertThat(pipelineStep)
                .extracting(PipelineMapStep::getSchemaInId, PipelineMapStep::getSchemaOutId)
                .containsExactly(
                        findSchema("SCHEMA_IN", 100, importedSchemas).getId(),
                        findSchema("SCHEMA_OUT", 778, importedSchemas).getId());
    }

    private static Schema findSchema(final String schemaName, final Collection<Schema> schemas) {
        return schemas.stream()
                .filter(schema -> schemaName.equals(schema.getPhysicalTableName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Schema [%s] not found in schemas", schemaName)));
    }

    private static Schema findSchema(final String schemaName, final Integer version, final Collection<Schema> schemas) {
        return schemas.stream()
                .filter(schema ->
                        schemaName.equals(schema.getPhysicalTableName()) && version.equals(schema.getMajorVersion()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Schema [%s, version %d] not found in schemas", schemaName, version)));
    }

    private static Field findField(final String fieldName, final Schema schema) {
        return schema.getFields().stream()
                .filter(field -> fieldName.equals(field.getName()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Field [%s] not found on schema [%s]", fieldName, schema.getDisplayName())));
    }
}
