package com.lombardrisk.ignis.design.server.productconfig.export;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.ImmutableMap;
import com.lombardrisk.ignis.client.design.pipeline.PipelineView;
import com.lombardrisk.ignis.client.design.pipeline.join.JoinFieldRequest;
import com.lombardrisk.ignis.client.design.pipeline.select.OrderDirection;
import com.lombardrisk.ignis.client.design.pipeline.select.OrderRequest;
import com.lombardrisk.ignis.client.design.pipeline.select.SelectRequest;
import com.lombardrisk.ignis.client.design.pipeline.select.UnionRequest;
import com.lombardrisk.ignis.client.design.pipeline.select.WindowRequest;
import com.lombardrisk.ignis.client.design.schema.field.FieldDto;
import com.lombardrisk.ignis.client.external.pipeline.export.step.JoinType;
import com.lombardrisk.ignis.common.assertions.VavrAssert;
import com.lombardrisk.ignis.design.field.FieldService;
import com.lombardrisk.ignis.design.field.model.Field;
import com.lombardrisk.ignis.design.server.pipeline.PipelineService;
import com.lombardrisk.ignis.design.server.pipeline.PipelineStepService;
import com.lombardrisk.ignis.design.server.productconfig.ProductConfig;
import com.lombardrisk.ignis.design.server.productconfig.ProductConfigService;
import com.lombardrisk.ignis.design.server.productconfig.export.ProductConfigExportFileService.ProductZipOutputStream;
import com.lombardrisk.ignis.design.server.productconfig.fixture.ProductServiceFixtureFactory;
import com.lombardrisk.ignis.design.server.productconfig.fixture.SchemaServiceFixtureFactory;
import com.lombardrisk.ignis.design.server.productconfig.schema.Schema;
import org.apache.commons.compress.utils.IOUtils;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static com.lombardrisk.ignis.common.json.MapperWrapper.MAPPER;
import static com.lombardrisk.ignis.design.field.DesignField.Populated.longFieldRequest;
import static com.lombardrisk.ignis.design.field.DesignField.Populated.stringFieldRequest;
import static com.lombardrisk.ignis.design.server.fixtures.Design.Populated;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@SuppressWarnings("unchecked")
public class ProductConfigExportFileServiceTest {

    private static final String MANIFEST_FILENAME = "manifest.json";

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    private ProductConfigExportFileService productConfigExportService;
    private ProductConfigService productConfigService;
    private FieldService fieldService;
    private PipelineService pipelineService;
    private PipelineStepService pipelineStepService;

    private Integer csvImportMaxLines = 100;

    @Before
    public void setUp() {
        SchemaServiceFixtureFactory schemaFactory = SchemaServiceFixtureFactory.create();
        ProductServiceFixtureFactory productFactory =
                ProductServiceFixtureFactory.create(schemaFactory, csvImportMaxLines);

        productConfigExportService = productFactory.getProductExportService();
        productConfigService = productFactory.getProductService();
        fieldService = schemaFactory.getFieldService();
        pipelineService = productFactory.getPipelineDependencies().getPipelineService();
        pipelineStepService = productFactory.getPipelineDependencies().getPipelineStepService();
    }

    @Test
    public void exportProduct_WithOutputStream_ReturnsSameOutputStream() throws Exception {
        ProductConfig productConfig =
                productConfigService.createProductConfig(Populated.newProductRequest().build()).get();
        OutputStream outputStream = new ByteArrayOutputStream();

        OutputStream retrievedOutputStream =
                VavrAssert.assertValid(productConfigExportService.exportProduct(productConfig.getId(),
                        Arrays.asList(), outputStream))
                        .getResult()
                        .getOutputStream();

        assertThat(retrievedOutputStream).isSameAs(outputStream);
    }

    @Test
    public void exportProduct_SavedProduct_ReturnsProductZipName() throws Exception {
        ProductConfig productConfig = productConfigService.createProductConfig(Populated.newProductRequest()
                .name("name")
                .version("version")
                .build()).get();

        OutputStream outputStream = new ByteArrayOutputStream();

        ProductZipOutputStream<OutputStream> retrievedOutputStream =
                VavrAssert.assertValid(productConfigExportService.exportProduct(productConfig.getId(),
                        Arrays.asList(), outputStream))
                        .getResult();

        assertThat(retrievedOutputStream.getZipFilename())
                .isEqualTo("name_version.zip");
    }

    @Test
    public void exportProduct_WithBasicProductConfigProperties_CreatesPackageWithManifestFile() throws Exception {

        ProductConfig productConfig = productConfigService.createProductConfig(
                Populated.newProductRequest()
                        .name("new product config view")
                        .version("v1.0")
                        .build()).get();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        productConfigExportService.exportProduct(productConfig.getId(), Arrays.asList(), outputStream);

        Map<String, JsonNode> unpackedProduct = unzipProductConfiguration(outputStream);

        soft.assertThat(unpackedProduct.keySet()).containsOnly(MANIFEST_FILENAME);

        JsonNode productConfigView = unpackedProduct.get(MANIFEST_FILENAME);
        soft.assertThat(productConfigView.get("id")).isNull();
        soft.assertThat(productConfigView.get("name").textValue()).isEqualTo("new product config view");
        soft.assertThat(productConfigView.get("version").textValue()).isEqualTo("v1.0");
        soft.assertThat(productConfigView.get("tables")).isNull();
    }

    @Test
    public void exportProduct_WithMultipleTables_CreatesPackageWithMultipleTableFiles() throws Exception {
        ProductConfig productConfig =
                productConfigService.createProductConfig(Populated.newProductRequest().build()).get();

        VavrAssert.assertValid(productConfigService.createNewSchemaOnProduct(
                productConfig.getId(),
                Populated.createSchemaRequest()
                        .displayName("table 1")
                        .physicalTableName("tbl1")
                        .build()));
        VavrAssert.assertValid(productConfigService.createNewSchemaOnProduct(
                productConfig.getId(),
                Populated.createSchemaRequest()
                        .displayName("table 2")
                        .physicalTableName("tbl2")
                        .build()));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        productConfigExportService.exportProduct(productConfig.getId(), Arrays.asList(), outputStream);

        Map<String, JsonNode> unpackedProduct = unzipProductConfiguration(outputStream);

        String filename1 = "tbl1_1.json";
        String filename2 = "tbl2_1.json";
        soft.assertThat(unpackedProduct.keySet())
                .containsExactlyInAnyOrder(filename1, filename2, MANIFEST_FILENAME);

        soft.assertThat(unpackedProduct.get(filename1).has("id"))
                .isFalse();
        soft.assertThat(unpackedProduct.get(filename1).get("displayName").textValue())
                .isEqualTo("table 1");
        soft.assertThat(unpackedProduct.get(filename1).get("physicalTableName").textValue())
                .isEqualTo("tbl1");

        soft.assertThat(unpackedProduct.get(filename2).has("id"))
                .isFalse();
        soft.assertThat(unpackedProduct.get(filename2).get("displayName").textValue())
                .isEqualTo("table 2");
        soft.assertThat(unpackedProduct.get(filename2).get("physicalTableName").textValue())
                .isEqualTo("tbl2");
    }

    @Test
    public void exportProduct_PipelineWithNoSteps_ExportsWithPipelineFiles() throws Exception {
        ProductConfig productConfig =
                productConfigService.createProductConfig(Populated.newProductRequest().build()).get();

        VavrAssert.assertValid(pipelineService.saveNewPipeline(Populated.createPipelineRequest()
                .name("One")
                .productId(productConfig.getId())
                .build()));
        VavrAssert.assertValid(pipelineService.saveNewPipeline(Populated.createPipelineRequest()
                .name("Two")
                .productId(productConfig.getId())
                .build()));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        productConfigExportService.exportProduct(productConfig.getId(), Arrays.asList(), outputStream);
        Map<String, JsonNode> unpackedProduct = unzipProductConfiguration(outputStream);

        String filename1 = "pipelines/One.json";
        String filename2 = "pipelines/Two.json";

        soft.assertThat(unpackedProduct.keySet())
                .containsExactlyInAnyOrder(filename1, filename2, MANIFEST_FILENAME);

        soft.assertThat(unpackedProduct.get(filename1).has("id"))
                .isFalse();
        soft.assertThat(unpackedProduct.get(filename1).get("name").textValue())
                .isEqualTo("One");
        soft.assertThat(unpackedProduct.get(filename2).has("id"))
                .isFalse();
        soft.assertThat(unpackedProduct.get(filename2).get("name").textValue())
                .isEqualTo("Two");
    }

    @Test
    public void exportProduct_OnePipelineStep_ExportsStepWithSchemaDetails() throws Exception {
        ProductConfig productConfig =
                productConfigService.createProductConfig(Populated.newProductRequest().build()).get();

        Long schemaInId = VavrAssert.assertValid(productConfigService.createNewSchemaOnProduct(
                productConfig.getId(),
                Populated.createSchemaRequest()
                        .displayName("In Table")
                        .physicalTableName("IN")
                        .majorVersion(1)
                        .build()))
                .getResult().getId();

        Long schemaOutId = VavrAssert.assertValid(productConfigService
                .createNewSchemaOnProduct(productConfig.getId(), Populated.createSchemaRequest()
                        .displayName("Out Table")
                        .physicalTableName("OUT")
                        .majorVersion(2)
                        .build()))
                .getResult().getId();

        Long pipelineId = VavrAssert.assertValid(
                pipelineService.saveNewPipeline(Populated.createPipelineRequest()
                        .name("One")
                        .productId(productConfig.getId())
                        .build()))
                .getResult().getId();

        VavrAssert.assertValid(pipelineStepService.savePipelineStep(pipelineId, Populated.pipelineMapStepRequest()
                .name("Step 1")
                .schemaInId(schemaInId)
                .schemaOutId(schemaOutId)
                .build()));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        productConfigExportService.exportProduct(productConfig.getId(), Arrays.asList(), outputStream);
        Map<String, JsonNode> unpackedProduct = unzipProductConfiguration(outputStream);

        String filename1 = "pipelines/One.json";

        JsonNode stepOne = unpackedProduct.get(filename1).get("steps").get(0);
        soft.assertThat(stepOne.get("schemaIn").get("displayName").textValue())
                .isEqualTo("In Table");
        soft.assertThat(stepOne.get("schemaIn").get("physicalTableName").textValue())
                .isEqualTo("IN");
        soft.assertThat(stepOne.get("schemaIn").get("version").intValue())
                .isEqualTo(1);
        soft.assertThat(stepOne.get("schemaOut").get("displayName").textValue())
                .isEqualTo("Out Table");
        soft.assertThat(stepOne.get("schemaOut").get("physicalTableName").textValue())
                .isEqualTo("OUT");
        soft.assertThat(stepOne.get("schemaOut").get("version").intValue())
                .isEqualTo(2);
    }

    @Test
    public void exportProduct_WithPipeline_ExportsMaintainsSelectOrder() throws Exception {
        ProductConfig productConfig =
                productConfigService.createProductConfig(Populated.newProductRequest().build()).get();

        Schema schemaIn = VavrAssert.assertValid(productConfigService.createNewSchemaOnProduct(
                productConfig.getId(), Populated.randomisedCreateSchemaRequest()
                        .build()))
                .getResult();
        FieldDto schemaInFieldA = fieldService.save(schemaIn.getId(), stringFieldRequest("A").build())
                .get();
        FieldDto schemaInFieldB = fieldService.save(schemaIn.getId(), longFieldRequest("B").build())
                .get();

        Schema schemaOut = VavrAssert.assertValid(productConfigService.createNewSchemaOnProduct(
                productConfig.getId(), Populated.randomisedCreateSchemaRequest()
                        .build()))
                .getResult();

        FieldDto schemaOutFieldA = fieldService.save(schemaOut.getId(), stringFieldRequest("A").build()).get();
        FieldDto schemaOutFieldB = fieldService.save(schemaOut.getId(), longFieldRequest("B").build()).get();
        FieldDto schemaOutFieldC = fieldService.save(schemaOut.getId(), longFieldRequest("C").build()).get();

        Long pipelineId = VavrAssert.assertValid(
                pipelineService.saveNewPipeline(Populated.createPipelineRequest()
                        .name("One")
                        .productId(productConfig.getId())
                        .build()))
                .getResult().getId();

        VavrAssert.assertValid(
                pipelineStepService.savePipelineStep(pipelineId, Populated.pipelineMapStepRequest()
                        .selects(newLinkedHashSet(asList(
                                SelectRequest.builder()
                                        .select("A")
                                        .outputFieldId(schemaOutFieldA.getId())
                                        .build(),
                                SelectRequest.builder()
                                        .select("B")
                                        .outputFieldId(schemaOutFieldB.getId())
                                        .build(),
                                SelectRequest.builder()
                                        .select("CONCAT(A,B)")
                                        .outputFieldId(schemaOutFieldC.getId())
                                        .build())))
                        .schemaInId(schemaIn.getId())
                        .schemaOutId(schemaOut.getId())
                        .build()));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        productConfigExportService.exportProduct(productConfig.getId(), Arrays.asList(), outputStream);
        Map<String, JsonNode> unpackedProduct = unzipProductConfiguration(outputStream);

        String filename1 = "pipelines/One.json";

        JsonNode stepOne = unpackedProduct.get(filename1).get("steps").get(0);

        soft.assertThat(stepOne.get("selects"))
                .extracting(
                        json -> json.get("select").textValue(),
                        json -> json.get("outputFieldName").textValue(),
                        json -> json.get("isWindow").booleanValue())
                .containsExactly(
                        tuple("A", "A", false),
                        tuple("B", "B", false),
                        tuple("CONCAT(A,B)", "C", false));
    }

    @Test
    public void exportProduct_PipelineWithMultipleSteps_ExportsSelectsFiltersGroupings() throws Exception {
        ProductConfig productConfig =
                productConfigService.createProductConfig(Populated.newProductRequest().build()).get();

        Schema schema1 = VavrAssert.assertValid(productConfigService.createNewSchemaOnProduct(
                productConfig.getId(), Populated.randomisedCreateSchemaRequest()
                        .build()))
                .getResult();

        Long schema1FieldA = fieldService.save(schema1.getId(), stringFieldRequest("A").build())
                .get().getId();
        Long schema1FieldB = fieldService.save(schema1.getId(), stringFieldRequest("B").build())
                .get().getId();

        Schema schema2 = VavrAssert.assertValid(productConfigService.createNewSchemaOnProduct(
                productConfig.getId(), Populated.randomisedCreateSchemaRequest().build()))
                .getResult();

        Long schema2FieldA = fieldService.save(schema2.getId(), stringFieldRequest("A").build())
                .get().getId();
        Long schema2FieldB = fieldService.save(schema2.getId(), stringFieldRequest("B").build())
                .get().getId();
        Long schema2FieldC = fieldService.save(schema2.getId(), stringFieldRequest("C").build())
                .get().getId();

        Schema schema3 = VavrAssert.assertValid(productConfigService.createNewSchemaOnProduct(
                productConfig.getId(), Populated.randomisedCreateSchemaRequest()
                        .build()))
                .getResult();

        Long schema3FieldC = fieldService.save(schema3.getId(), stringFieldRequest("C").build())
                .get().getId();

        Long pipelineId = VavrAssert.assertValid(
                pipelineService.saveNewPipeline(Populated.createPipelineRequest()
                        .name("One")
                        .productId(productConfig.getId())
                        .build()))
                .getResult().getId();

        VavrAssert.assertValid(
                pipelineStepService.savePipelineStep(pipelineId, Populated.pipelineMapStepRequest()
                        .name("map rows")
                        .selects(newLinkedHashSet(asList(
                                SelectRequest.builder()
                                        .select("A")
                                        .outputFieldId(schema2FieldA)
                                        .build(),
                                SelectRequest.builder()
                                        .select("B")
                                        .outputFieldId(schema2FieldB)
                                        .build(),
                                SelectRequest.builder()
                                        .select("CONCAT(A,B)")
                                        .outputFieldId(schema2FieldC)
                                        .build())))
                        .filters(asList("A > 0", "B > 0", "A < 100"))
                        .schemaInId(schema1.getId())
                        .schemaOutId(schema2.getId())
                        .build()));

        VavrAssert.assertValid(
                pipelineStepService.savePipelineStep(pipelineId, Populated.pipelineAggregationStepRequest()
                        .name("aggregate rows")
                        .selects(singleton(
                                SelectRequest.builder()
                                        .select("AVG(C)")
                                        .outputFieldId(schema3FieldC)
                                        .build()))
                        .groupings(singletonList("C"))
                        .schemaInId(schema2.getId())
                        .schemaOutId(schema3.getId())
                        .build()));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        productConfigExportService.exportProduct(productConfig.getId(), Arrays.asList(), outputStream);
        Map<String, JsonNode> unpackedProduct = unzipProductConfiguration(outputStream);

        String filename1 = "pipelines/One.json";

        JsonNode stepOne = unpackedProduct.get(filename1).get("steps").get(0);

        soft.assertThat(stepOne.get("selects"))
                .extracting(
                        json -> json.get("select").textValue(),
                        json -> json.get("outputFieldName").textValue(),
                        json -> json.get("isWindow").booleanValue())
                .containsExactly(
                        tuple("A", "A", false),
                        tuple("B", "B", false),
                        tuple("CONCAT(A,B)", "C", false));
        soft.assertThat(stepOne.get("filters"))
                .extracting(JsonNode::textValue)
                .containsExactlyInAnyOrder("A > 0", "B > 0", "A < 100");
        soft.assertThat(stepOne.findPath("groupings").isMissingNode())
                .isTrue();

        JsonNode stepTwo = unpackedProduct.get(filename1).get("steps").get(1);

        soft.assertThat(stepTwo.get("selects"))
                .extracting(
                        json -> json.get("select").textValue(),
                        json -> json.get("outputFieldName").textValue(),
                        json -> json.get("isWindow").booleanValue())
                .containsExactly(
                        tuple("AVG(C)", "C", false));
        soft.assertThat(stepTwo.get("filters"))
                .extracting(JsonNode::textValue)
                .isEmpty();
        soft.assertThat(stepTwo.get("groupings"))
                .extracting(JsonNode::textValue)
                .containsExactly("C");
    }

    @Test
    public void exportProduct_PipelineWithWindowFunction_ExportsSelectWithWindow() throws Exception {
        ProductConfig productConfig =
                productConfigService.createProductConfig(Populated.newProductRequest().build()).get();

        Schema schema1 = VavrAssert.assertValid(productConfigService.createNewSchemaOnProduct(
                productConfig.getId(), Populated.randomisedCreateSchemaRequest().build()))
                .getResult();

        FieldDto schema1FieldA = fieldService.save(schema1.getId(), stringFieldRequest("A").build())
                .get();
        FieldDto schema1FieldB = fieldService.save(schema1.getId(), stringFieldRequest("B").build())
                .get();

        Schema schema2 = VavrAssert.assertValid(productConfigService.createNewSchemaOnProduct(
                productConfig.getId(), Populated.randomisedCreateSchemaRequest().build()))
                .getResult();

        FieldDto schema2FieldA = fieldService.save(schema2.getId(), stringFieldRequest("A").build()).get();
        FieldDto schema2FieldB = fieldService.save(schema2.getId(), stringFieldRequest("B").build()).get();
        FieldDto schema2FieldC = fieldService.save(schema2.getId(), stringFieldRequest("C").build()).get();

        Long pipelineId = VavrAssert.assertValid(
                pipelineService.saveNewPipeline(Populated.createPipelineRequest()
                        .name("One")
                        .productId(productConfig.getId())
                        .build()))
                .getResult().getId();

        VavrAssert.assertValid(
                pipelineStepService.savePipelineStep(pipelineId, Populated.pipelineMapStepRequest()
                        .name("window step")
                        .selects(newLinkedHashSet(asList(
                                SelectRequest.builder()
                                        .select("A")
                                        .outputFieldId(schema2FieldA.getId())
                                        .build(),
                                SelectRequest.builder()
                                        .select("B")
                                        .outputFieldId(schema2FieldB.getId())
                                        .build(),
                                SelectRequest.builder()
                                        .select("rank()")
                                        .outputFieldId(schema2FieldC.getId())
                                        .hasWindow(true)
                                        .window(WindowRequest.builder()
                                                .partitionBy(newHashSet("A", "B"))
                                                .orderBy(asList(
                                                        OrderRequest.builder()
                                                                .fieldName("B")
                                                                .direction(OrderDirection.DESC)
                                                                .priority(0)
                                                                .build(),
                                                        OrderRequest.builder()
                                                                .fieldName("A")
                                                                .direction(OrderDirection.ASC)
                                                                .priority(1)
                                                                .build()))
                                                .build())
                                        .build())))
                        .schemaInId(schema1.getId())
                        .schemaOutId(schema2.getId())
                        .build()));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        productConfigExportService.exportProduct(productConfig.getId(), Arrays.asList(), outputStream);
        Map<String, JsonNode> unpackedProduct = unzipProductConfiguration(outputStream);

        String filename1 = "pipelines/One.json";

        JsonNode step = unpackedProduct.get(filename1).get("steps").get(0);

        soft.assertThat(step.get("selects"))
                .extracting(
                        json -> json.get("select").textValue(),
                        json -> json.get("outputFieldName").textValue(),
                        json -> json.get("isWindow").booleanValue())
                .containsExactly(
                        tuple("A", "A", false),
                        tuple("B", "B", false),
                        tuple("rank()", "C", true));

        soft.assertThat(step.get("selects").get(0).get("window").textValue())
                .isNull();

        soft.assertThat(step.get("selects").get(1).get("window").textValue())
                .isNull();

        soft.assertThat(step.get("selects").get(2).get("window").get("partitionBy"))
                .extracting(JsonNode::textValue)
                .containsExactlyInAnyOrder("A", "B");

        soft.assertThat(step.get("selects").get(2).get("window").get("orderBy"))
                .extracting(
                        json -> json.get("fieldName").textValue(),
                        json -> json.get("direction").textValue(),
                        json -> json.get("priority").intValue())
                .containsExactlyInAnyOrder(
                        tuple("B", "DESC", 0), tuple("A", "ASC", 1));
    }

    @Test
    public void exportProduct_PipelineWithUnionStep_ExportsUnionSchemasSelects() throws Exception {
        ProductConfig productConfig =
                productConfigService.createProductConfig(Populated.newProductRequest().build()).get();

        Schema schemaA = VavrAssert.assertValid(productConfigService.createNewSchemaOnProduct(
                productConfig.getId(), Populated.createSchemaRequest()
                        .displayName("A Table")
                        .physicalTableName("A")
                        .majorVersion(3)
                        .build()))
                .getResult();
        Schema schemaB = VavrAssert.assertValid(productConfigService.createNewSchemaOnProduct(
                productConfig.getId(), Populated.createSchemaRequest()
                        .displayName("B Table")
                        .physicalTableName("B")
                        .majorVersion(4)
                        .build()))
                .getResult();

        FieldDto schemaAFieldA = fieldService.save(schemaA.getId(), stringFieldRequest("A").build()).get();
        FieldDto schemaBFieldB = fieldService.save(schemaB.getId(), stringFieldRequest("B").build()).get();

        Schema schemaC = VavrAssert.assertValid(productConfigService.createNewSchemaOnProduct(
                productConfig.getId(), Populated.randomisedCreateSchemaRequest().build()))
                .getResult();

        FieldDto schemaCFieldC = fieldService.save(schemaC.getId(), stringFieldRequest("C").build()).get();

        Long pipelineId = VavrAssert.assertValid(pipelineService.saveNewPipeline(
                Populated.createPipelineRequest()
                        .name("Union")
                        .productId(productConfig.getId())
                        .build())).getResult().getId();

        VavrAssert.assertValid(
                pipelineStepService.savePipelineStep(pipelineId, Populated.pipelineUnionStepRequest()
                        .schemaOutId(schemaC.getId())
                        .unionSchemas(ImmutableMap.of(
                                schemaA.getId(), UnionRequest.builder()
                                        .selects(singletonList(SelectRequest.builder()
                                                .outputFieldId(schemaCFieldC.getId())
                                                .select("A")
                                                .build()))
                                        .filters(singletonList("A > 2"))
                                        .build(),
                                schemaB.getId(), UnionRequest.builder()
                                        .selects(singletonList(SelectRequest.builder()
                                                .outputFieldId(schemaCFieldC.getId())
                                                .select("B")
                                                .build()))
                                        .build()))
                        .build()));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        productConfigExportService.exportProduct(productConfig.getId(), Arrays.asList(), outputStream);
        Map<String, JsonNode> unpackedProduct = unzipProductConfiguration(outputStream);

        String filename1 = "pipelines/Union.json";
        JsonNode step = unpackedProduct.get(filename1).get("steps").get(0);

        JsonNode firstUnion = step.get("unions").get(0);
        soft.assertThat(firstUnion.get("selects"))
                .extracting(
                        json -> json.get("select").textValue(),
                        json -> json.get("outputFieldName").textValue())
                .containsExactly(
                        tuple("A", "C"));
        soft.assertThat(firstUnion.get("filters"))
                .extracting(JsonNode::textValue)
                .contains("A > 2");
        soft.assertThat(firstUnion.get("unionInSchema").get("physicalTableName").textValue())
                .isEqualTo("A");
        soft.assertThat(firstUnion.get("unionInSchema").get("displayName").textValue())
                .isEqualTo("A Table");
        soft.assertThat(firstUnion.get("unionInSchema").get("version").intValue())
                .isEqualTo(3);

        JsonNode secondUnion = step.get("unions").get(1);
        soft.assertThat(secondUnion.get("selects"))
                .extracting(
                        json -> json.get("select").textValue(),
                        json -> json.get("outputFieldName").textValue())
                .containsExactly(
                        tuple("B", "C"));
        soft.assertThat(secondUnion.get("filters")).isEmpty();
        soft.assertThat(secondUnion.get("unionInSchema").get("physicalTableName").textValue())
                .isEqualTo("B");
        soft.assertThat(secondUnion.get("unionInSchema").get("displayName").textValue())
                .isEqualTo("B Table");
        soft.assertThat(secondUnion.get("unionInSchema").get("version").intValue())
                .isEqualTo(4);
    }

    @Test
    public void exportProduct_PipelineWithUnionStep_ExportsSchemaOutAndType() throws Exception {
        ProductConfig productConfig =
                productConfigService.createProductConfig(Populated.newProductRequest().build()).get();

        Schema schemaA = VavrAssert.assertValid(productConfigService.createNewSchemaOnProduct(
                productConfig.getId(), Populated.randomisedCreateSchemaRequest().build()))
                .getResult();
        Schema schemaB = VavrAssert.assertValid(productConfigService.createNewSchemaOnProduct(
                productConfig.getId(), Populated.randomisedCreateSchemaRequest().build()))
                .getResult();

        FieldDto schemaAFieldA = fieldService.save(schemaA.getId(), stringFieldRequest("A").build()).get();
        FieldDto schemaBFieldB = fieldService.save(schemaB.getId(), stringFieldRequest("B").build()).get();

        Schema schemaC = VavrAssert.assertValid(productConfigService.createNewSchemaOnProduct(
                productConfig.getId(), Populated.createSchemaRequest()
                        .displayName("C Table")
                        .physicalTableName("C")
                        .majorVersion(7)
                        .build()))
                .getResult();

        FieldDto schemaCFieldC = fieldService.save(schemaC.getId(), stringFieldRequest("C").build()).get();

        Long pipelineId = VavrAssert.assertValid(pipelineService.saveNewPipeline(
                Populated.createPipelineRequest()
                        .name("Union")
                        .productId(productConfig.getId())
                        .build())).getResult().getId();

        VavrAssert.assertValid(
                pipelineStepService.savePipelineStep(pipelineId, Populated.pipelineUnionStepRequest()
                        .schemaOutId(schemaC.getId())
                        .unionSchemas(ImmutableMap.of(
                                schemaA.getId(), UnionRequest.builder()
                                        .selects(singletonList(SelectRequest.builder()
                                                .outputFieldId(schemaCFieldC.getId())
                                                .select("A")
                                                .build()))
                                        .build(),
                                schemaB.getId(), UnionRequest.builder()
                                        .selects(singletonList(SelectRequest.builder()
                                                .outputFieldId(schemaCFieldC.getId())
                                                .select("B")
                                                .build()))
                                        .build()))
                        .build()));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        productConfigExportService.exportProduct(productConfig.getId(), Arrays.asList(), outputStream);
        Map<String, JsonNode> unpackedProduct = unzipProductConfiguration(outputStream);

        String filename1 = "pipelines/Union.json";
        JsonNode step = unpackedProduct.get(filename1).get("steps").get(0);

        soft.assertThat(step.get("type").textValue()).isEqualTo("UNION");
        soft.assertThat(step.get("schemaOut").get("physicalTableName").textValue())
                .isEqualTo("C");
        soft.assertThat(step.get("schemaOut").get("displayName").textValue())
                .isEqualTo("C Table");
        soft.assertThat(step.get("schemaOut").get("version").intValue())
                .isEqualTo(7);
    }

    @Test
    public void exportProduct_WithPipeline_ExportsWithPipelineDetails() throws Exception {
        ProductConfig productConfig =
                productConfigService.createProductConfig(Populated.newProductRequest().build()).get();

        Long schemaInId = VavrAssert.assertValid(productConfigService.createNewSchemaOnProduct(
                productConfig.getId(), Populated.randomisedCreateSchemaRequest().build()))
                .getResult().getId();

        Long schemaOutId = VavrAssert.assertValid(productConfigService.createNewSchemaOnProduct(
                productConfig.getId(), Populated.randomisedCreateSchemaRequest().build()))
                .getResult().getId();

        Long pipelineId = VavrAssert.assertValid(
                pipelineService.saveNewPipeline(Populated.createPipelineRequest()
                        .name("One")
                        .productId(productConfig.getId())
                        .build()))
                .getResult().getId();

        VavrAssert.assertValid(pipelineStepService.savePipelineStep(pipelineId, Populated.pipelineMapStepRequest()
                .name("Step 1")
                .description("Steppity Oneity")
                .schemaInId(schemaInId)
                .schemaOutId(schemaOutId)
                .build()));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        productConfigExportService.exportProduct(productConfig.getId(), Arrays.asList(), outputStream);
        Map<String, JsonNode> unpackedProduct = unzipProductConfiguration(outputStream);

        String filename1 = "pipelines/One.json";

        JsonNode stepOne = unpackedProduct.get(filename1).get("steps").get(0);
        soft.assertThat(stepOne.get("name").textValue())
                .isEqualTo("Step 1");
        soft.assertThat(stepOne.get("description").textValue())
                .isEqualTo("Steppity Oneity");
        soft.assertThat(stepOne.get("type").textValue())
                .isEqualTo("MAP");
    }

    @Test
    public void exportProduct_WithJoinPipeline_ExportsWithPipelineDetails() throws Exception {
        ProductConfig productConfig =
                productConfigService.createProductConfig(Populated.newProductRequest().build()).get();

        Schema leftSchema = VavrAssert.assertValid(
                productConfigService.createNewSchemaOnProduct(
                        productConfig.getId(),
                        Populated.createSchemaRequest()
                                .physicalTableName("LEFT")
                                .displayName("Left table")
                                .majorVersion(1)
                                .build()))
                .getResult();

        FieldDto leftIdField = fieldService.save(leftSchema.getId(), longFieldRequest("LEFT_ID").build())
                .get();
        FieldDto leftNameField = fieldService.save(leftSchema.getId(), stringFieldRequest("LEFT_NAME").build())
                .get();

        Schema rightSchema = VavrAssert.assertValid(
                productConfigService.createNewSchemaOnProduct(
                        productConfig.getId(),
                        Populated.createSchemaRequest()
                                .physicalTableName("RIGHT")
                                .displayName("Right table")
                                .majorVersion(2)
                                .build()))
                .getResult();

        FieldDto rightIdField = fieldService.save(rightSchema.getId(), longFieldRequest("RIGHT_ID").build())
                .get();
        FieldDto rightNameField = fieldService.save(rightSchema.getId(), stringFieldRequest("RIGHT_NAME").build())
                .get();

        Long schemaOutId = VavrAssert.assertValid(
                productConfigService.createNewSchemaOnProduct(
                        productConfig.getId(),
                        Populated.createSchemaRequest()
                                .physicalTableName("OUT")
                                .displayName("Out table")
                                .majorVersion(3)
                                .build()))
                .getResult().getId();

        Long pipelineId = VavrAssert.assertValid(
                pipelineService.saveNewPipeline(Populated.createPipelineRequest()
                        .productId(productConfig.getId())
                        .name("One")
                        .build()))
                .getResult().getId();

        VavrAssert.assertValid(pipelineStepService.savePipelineStep(pipelineId, Populated.pipelineJoinStepRequest()
                .schemaOutId(schemaOutId)
                .joins(newHashSet(Populated.joinRequest()
                        .rightSchemaId(rightSchema.getId())
                        .leftSchemaId(leftSchema.getId())
                        .joinFields(asList(
                                JoinFieldRequest.builder()
                                        .leftFieldId(leftIdField.getId())
                                        .rightFieldId(rightIdField.getId())
                                        .build(),
                                JoinFieldRequest.builder()
                                        .leftFieldId(leftNameField.getId())
                                        .rightFieldId(rightNameField.getId())
                                        .build()))
                        .joinType(JoinType.INNER)
                        .build()))
                .build()));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        productConfigExportService.exportProduct(productConfig.getId(), Arrays.asList(), outputStream);
        Map<String, JsonNode> unpackedProduct = unzipProductConfiguration(outputStream);

        String filename1 = "pipelines/One.json";

        JsonNode stepOne = unpackedProduct.get(filename1).get("steps").get(0);
        soft.assertThat(stepOne.get("type").textValue())
                .isEqualTo("JOIN");

        JsonNode schemaOut = stepOne.get("schemaOut");
        soft.assertThat(schemaOut.get("physicalTableName").textValue())
                .isEqualTo("OUT");
        soft.assertThat(schemaOut.get("displayName").textValue())
                .isEqualTo("Out table");
        soft.assertThat(schemaOut.get("version").intValue())
                .isEqualTo(3);

        JsonNode firstJoin = stepOne.get("joins").get(0);
        soft.assertThat(firstJoin.get("type").textValue())
                .isEqualTo("INNER");

        soft.assertThat(firstJoin.get("joinFields").get(0).get("leftColumn").textValue())
                .isEqualTo("LEFT_ID");
        soft.assertThat(firstJoin.get("joinFields").get(1).get("leftColumn").textValue())
                .isEqualTo("LEFT_NAME");
        soft.assertThat(firstJoin.get("left").get("physicalTableName").textValue())
                .isEqualTo("LEFT");
        soft.assertThat(firstJoin.get("left").get("displayName").textValue())
                .isEqualTo("Left table");
        soft.assertThat(firstJoin.get("left").get("version").intValue())
                .isEqualTo(1);

        soft.assertThat(firstJoin.get("joinFields").get(0).get("rightColumn").textValue())
                .isEqualTo("RIGHT_ID");
        soft.assertThat(firstJoin.get("joinFields").get(1).get("rightColumn").textValue())
                .isEqualTo("RIGHT_NAME");
        soft.assertThat(firstJoin.get("right").get("physicalTableName").textValue())
                .isEqualTo("RIGHT");
        soft.assertThat(firstJoin.get("right").get("displayName").textValue())
                .isEqualTo("Right table");
        soft.assertThat(firstJoin.get("right").get("version").intValue())
                .isEqualTo(2);
    }

    @Test
    public void exportProduct_ProductWithPipelines_FiltersOutNonSelectedPipelinesFromExport() throws Exception {
        ProductConfig productConfig =
                productConfigService.createProductConfig(Populated.newProductRequest().build()).get();

        Long schemaA = VavrAssert.assertValid(productConfigService.createNewSchemaOnProduct(
                productConfig.getId(),
                Populated.createSchemaRequest()
                        .displayName("Schema A")
                        .physicalTableName("A")
                        .majorVersion(1)
                        .build()))
                .getResult().getId();

        Long schemaB = VavrAssert.assertValid(productConfigService
                .createNewSchemaOnProduct(productConfig.getId(), Populated.createSchemaRequest()
                        .displayName("Schema B")
                        .physicalTableName("B")
                        .majorVersion(1)
                        .build()))
                .getResult().getId();

        PipelineView validPipeline1 = VavrAssert.assertValid(
                pipelineService.saveNewPipeline(Populated.createPipelineRequest()
                        .name("Valid Pipeline 1")
                        .productId(productConfig.getId())
                        .build()))
                .getResult();

        VavrAssert.assertValid(pipelineStepService.savePipelineStep(
                validPipeline1.getId(),
                Populated.pipelineMapStepRequest()
                        .name("Valid Pipeline 1 Step")
                        .schemaInId(schemaA)
                        .schemaOutId(schemaB)
                        .build()));

        PipelineView invalidPipeline = VavrAssert.assertValid(
                pipelineService.saveNewPipeline(Populated.createPipelineRequest()
                        .name("Invalid Pipeline")
                        .productId(productConfig.getId())
                        .build()))
                .getResult();

        VavrAssert.assertValid(pipelineStepService.savePipelineStep(
                invalidPipeline.getId(),
                Populated.pipelineMapStepRequest()
                        .name("Invalid Pipeline 1 Step")
                        .schemaInId(schemaA)
                        .schemaOutId(schemaA)
                        .build()));

        PipelineView validPipeline2 = VavrAssert.assertValid(
                pipelineService.saveNewPipeline(Populated.createPipelineRequest()
                        .name("Valid Pipeline 2")
                        .productId(productConfig.getId())
                        .build()))
                .getResult();

        VavrAssert.assertValid(pipelineStepService.savePipelineStep(
                validPipeline2.getId(),
                Populated.pipelineMapStepRequest()
                        .name("Valid Pipeline 2 Step")
                        .schemaInId(schemaA)
                        .schemaOutId(schemaB)
                        .build()));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        productConfigExportService.exportProduct(productConfig.getId(), Arrays.asList(invalidPipeline.getId(),
                validPipeline2.getId()), outputStream);
        Map<String, JsonNode> unpackedProduct = unzipProductConfiguration(outputStream);

        soft.assertThat(unpackedProduct)
                .doesNotContainKey("pipelines/Valid Pipeline 1.json");

        String filename1 = "pipelines/Invalid Pipeline.json";

        soft.assertThat(unpackedProduct)
                .containsKey(filename1);

        String filename2 = "pipelines/Valid Pipeline 2.json";

        soft.assertThat(unpackedProduct)
                .containsKey(filename2);

        JsonNode invalidStep = unpackedProduct.get(filename1).get("steps").get(0);
        soft.assertThat(invalidStep.get("schemaIn").get("displayName").textValue())
                .isEqualTo("Schema A");
        soft.assertThat(invalidStep.get("schemaIn").get("physicalTableName").textValue())
                .isEqualTo("A");
        soft.assertThat(invalidStep.get("schemaIn").get("version").intValue())
                .isEqualTo(1);
        soft.assertThat(invalidStep.get("schemaOut").get("displayName").textValue())
                .isEqualTo("Schema A");
        soft.assertThat(invalidStep.get("schemaOut").get("physicalTableName").textValue())
                .isEqualTo("A");
        soft.assertThat(invalidStep.get("schemaOut").get("version").intValue())
                .isEqualTo(1);

        JsonNode validStep = unpackedProduct.get(filename2).get("steps").get(0);
        soft.assertThat(validStep.get("schemaIn").get("displayName").textValue())
                .isEqualTo("Schema A");
        soft.assertThat(validStep.get("schemaIn").get("physicalTableName").textValue())
                .isEqualTo("A");
        soft.assertThat(validStep.get("schemaIn").get("version").intValue())
                .isEqualTo(1);
        soft.assertThat(validStep.get("schemaOut").get("displayName").textValue())
                .isEqualTo("Schema B");
        soft.assertThat(validStep.get("schemaOut").get("physicalTableName").textValue())
                .isEqualTo("B");
        soft.assertThat(validStep.get("schemaOut").get("version").intValue())
                .isEqualTo(1);
    }

    private static Map<String, JsonNode> unzipProductConfiguration(final ByteArrayOutputStream outputStream)
            throws Exception {

        Map<String, JsonNode> fileNameToJson = new HashMap<>();
        ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(outputStream.toByteArray()));
        ZipEntry entry;
        while ((entry = zipInputStream.getNextEntry()) != null) {
            byte[] zipEntryBytes = IOUtils.toByteArray(zipInputStream);
            fileNameToJson.put(entry.getName(), MAPPER.readTree(zipEntryBytes));
        }
        return fileNameToJson;
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private static Field findFieldInSchema(final Schema schema, final String fieldName) {
        return schema.getFields().stream()
                .filter(field -> field.getName().equals(fieldName))
                .findFirst()
                .get();
    }
}
