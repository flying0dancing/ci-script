package com.lombardrisk.ignis.server.jpa;

import com.lombardrisk.ignis.server.config.IntegrationTestConfig;
import com.lombardrisk.ignis.server.product.fixture.ProductPopulated;
import com.lombardrisk.ignis.server.product.pipeline.PipelineJpaRepository;
import com.lombardrisk.ignis.server.product.pipeline.PipelineService;
import com.lombardrisk.ignis.server.product.pipeline.details.SchemaDetails;
import com.lombardrisk.ignis.server.product.pipeline.model.Pipeline;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineAggregationStep;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineJoinStep;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineMapStep;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineScriptletStep;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineStep;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineUnionStep;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineWindowStep;
import com.lombardrisk.ignis.server.product.pipeline.model.TransformationType;
import com.lombardrisk.ignis.server.product.pipeline.select.Order;
import com.lombardrisk.ignis.server.product.pipeline.select.PipelineFilter;
import com.lombardrisk.ignis.server.product.pipeline.select.Select;
import com.lombardrisk.ignis.server.product.pipeline.select.Union;
import com.lombardrisk.ignis.server.product.pipeline.select.Window;
import com.lombardrisk.ignis.server.product.pipeline.transformation.Join;
import com.lombardrisk.ignis.server.product.pipeline.transformation.JoinField;
import com.lombardrisk.ignis.server.product.pipeline.transformation.ScriptletInput;
import com.lombardrisk.ignis.server.product.productconfig.ProductConfigRepository;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductConfig;
import com.lombardrisk.ignis.server.product.table.TableRepository;
import com.lombardrisk.ignis.server.product.table.model.Field;
import com.lombardrisk.ignis.server.product.table.model.Table;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static com.lombardrisk.ignis.server.product.fixture.ProductPopulated.longField;
import static com.lombardrisk.ignis.server.product.fixture.ProductPopulated.pipeline;
import static com.lombardrisk.ignis.server.product.fixture.ProductPopulated.productConfig;
import static com.lombardrisk.ignis.server.product.fixture.ProductPopulated.stringField;
import static com.lombardrisk.ignis.server.product.pipeline.select.Order.Direction.ASC;
import static com.lombardrisk.ignis.server.product.pipeline.select.Order.Direction.DESC;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@SuppressWarnings("unchecked")
@RunWith(SpringRunner.class)
@IntegrationTestConfig
public class PipelineJpaRepositoryIT {

    @Autowired
    private PipelineJpaRepository pipelineRepository;

    @Autowired
    private TableRepository tableRepository;

    @Autowired
    private ProductConfigRepository productConfigRepository;

    @Autowired
    private EntityManager entityManager;

    private PipelineService pipelineService;

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Before
    public void setUp() {
        pipelineService = new PipelineService(pipelineRepository);
    }

    @Test
    public void create_retrieve() {
        ProductConfig product = productConfigRepository.save(ProductPopulated.productConfig().build());

        Table table1 = tableRepository.save(ProductPopulated.table()
                .productId(product.getId())
                .displayName("table1")
                .physicalTableName("TB1")
                .validationRules(emptySet())
                .fields(newLinkedHashSet(asList(
                        ProductPopulated.decimalField("A").build(),
                        ProductPopulated.decimalField("B").build())))
                .build());
        Table table2 = tableRepository.save(ProductPopulated.table()
                .productId(product.getId())
                .displayName("table2")
                .physicalTableName("TB2")
                .validationRules(emptySet())
                .fields(newLinkedHashSet(asList(
                        ProductPopulated.decimalField("A").build(),
                        ProductPopulated.decimalField("B").build(),
                        ProductPopulated.decimalField("C").build())))
                .build());
        Table table3 = tableRepository.save(ProductPopulated.table()
                .productId(product.getId())
                .displayName("table3")
                .physicalTableName("TB3")
                .validationRules(emptySet())
                .fields(newLinkedHashSet(singletonList(ProductPopulated.decimalField("C").build())))
                .build());

        Pipeline created = pipelineService.savePipeline(pipeline()
                .productId(product.getId())
                .name("test")
                .steps(newLinkedHashSet(asList(
                        ProductPopulated.mapPipelineStep()
                                .name("summingStep")
                                .description("This adds a and b to get c")
                                .schemaInId(table1.getId())
                                .schemaOutId(table2.getId())
                                .selects(newLinkedHashSet(asList(
                                        ProductPopulated.select()
                                                .select("A")
                                                .outputFieldId(findFieldInSchema(table2, "A").getId())
                                                .order(0L)
                                                .build(),
                                        ProductPopulated.select()
                                                .select("B")
                                                .outputFieldId(findFieldInSchema(table2, "B").getId())
                                                .order(1L)
                                                .build(),
                                        ProductPopulated.select()
                                                .select("A+B")
                                                .outputFieldId(findFieldInSchema(table2, "C").getId())
                                                .order(2L)
                                                .build())))
                                .filters(newLinkedHashSet(asList("A > 10", "B > 20")))
                                .build(),
                        ProductPopulated.aggregatePipelineStep()
                                .name("averageStep")
                                .description("Average c")
                                .schemaInId(table2.getId())
                                .schemaOutId(table3.getId())
                                .selects(singleton(ProductPopulated.select()
                                        .select("AVG(C)")
                                        .outputFieldId(findFieldInSchema(table3, "C").getId())
                                        .build()))
                                .groupings(newLinkedHashSet(singletonList("C")))
                                .build())))
                .build());

        entityManager.flush();
        entityManager.clear();

        soft.assertThat(created.getName())
                .isEqualTo("test");
        soft.assertThat(created.getProductId())
                .isEqualTo(product.getId());
        assertThat(created.getSteps()).hasSize(2);

        Iterator<PipelineStep> stepsIterator = created.getSteps().iterator();

        PipelineMapStep step1 = (PipelineMapStep) stepsIterator.next();
        soft.assertThat(step1.getName())
                .isEqualTo("summingStep");
        soft.assertThat(step1.getDescription())
                .isEqualTo("This adds a and b to get c");
        soft.assertThat(step1.getType())
                .isEqualTo(TransformationType.MAP);
        soft.assertThat(step1.getSchemaInId())
                .isEqualTo(table1.getId());
        soft.assertThat(step1.getSchemaOutId())
                .isEqualTo(table2.getId());
        soft.assertThat(step1.getSelects())
                .extracting(Select::getId)
                .isNotNull();
        soft.assertThat(step1.getSelects())
                .extracting(Select::getSelect, Select::getOutputFieldId, Select::isWindow)
                .containsExactly(
                        tuple("A", findFieldInSchema(table2, "A").getId(), false),
                        tuple("B", findFieldInSchema(table2, "B").getId(), false),
                        tuple("A+B", findFieldInSchema(table2, "C").getId(), false));
        soft.assertThat(step1.getFilters())
                .containsExactlyInAnyOrder("A > 10", "B > 20");

        PipelineAggregationStep step2 = (PipelineAggregationStep) stepsIterator.next();
        soft.assertThat(step2.getName())
                .isEqualTo("averageStep");
        soft.assertThat(step2.getDescription())
                .isEqualTo("Average c");
        soft.assertThat(step2.getType())
                .isEqualTo(TransformationType.AGGREGATION);
        soft.assertThat(step2.getSchemaInId())
                .isEqualTo(table2.getId());
        soft.assertThat(step2.getSchemaOutId())
                .isEqualTo(table3.getId());
        soft.assertThat(step2.getSelects())
                .extracting(Select::getId)
                .isNotNull();
        soft.assertThat(step2.getSelects())
                .extracting(Select::getSelect, Select::getOutputFieldId, Select::isWindow)
                .containsExactly(tuple("AVG(C)", findFieldInSchema(table3, "C").getId(), false));
        soft.assertThat(step2.getFilters())
                .isEmpty();
        soft.assertThat(step2.getGroupings())
                .containsExactly("C");
    }

    @Test
    public void createPipeline_retrieveDetails() {
        ProductConfig product = productConfigRepository.save(ProductPopulated.productConfig().build());
        Table schemaIn = tableRepository.save(ProductPopulated.table()
                .productId(product.getId())
                .displayName("table1")
                .physicalTableName("TB11")
                .version(1)
                .validationRules(emptySet())
                .fields(newLinkedHashSet(asList(
                        ProductPopulated.decimalField("A").build(),
                        ProductPopulated.decimalField("B").build())))
                .build());
        Table schemaOut = tableRepository.save(ProductPopulated.table()
                .productId(product.getId())
                .displayName("table2")
                .physicalTableName("TBl2")
                .validationRules(emptySet())
                .version(2)
                .fields(newLinkedHashSet(asList(
                        ProductPopulated.decimalField("A").build(),
                        ProductPopulated.decimalField("B").build(),
                        ProductPopulated.decimalField("C").build())))
                .build());

        pipelineService.savePipeline(pipeline()
                .productId(product.getId())
                .name("test")
                .productId(product.getId())
                .steps(newLinkedHashSet(singletonList(
                        ProductPopulated.mapPipelineStep()
                                .name("summingStep")
                                .description("This adds a and b to get c")
                                .schemaInId(schemaIn.getId())
                                .schemaOutId(schemaOut.getId())
                                .selects(newLinkedHashSet(asList(
                                        ProductPopulated.select()
                                                .select("A")
                                                .outputFieldId(findFieldInSchema(schemaOut, "A").getId())
                                                .order(0L)
                                                .build(),
                                        ProductPopulated.select()
                                                .select("B")
                                                .outputFieldId(findFieldInSchema(schemaOut, "B").getId())
                                                .order(1L)
                                                .build(),
                                        ProductPopulated.select()
                                                .select("A+B")
                                                .outputFieldId(findFieldInSchema(schemaOut, "C").getId())
                                                .order(2L)
                                                .build())))
                                .build())))
                .build());

        entityManager.flush();
        entityManager.clear();

        List<Pipeline> details = pipelineRepository.findAll();

        assertThat(details)
                .hasSize(1);

        Pipeline pipelineDetails = details.get(0);
        assertThat(pipelineDetails.getName())
                .isEqualTo("test");
        assertThat(pipelineDetails.getSteps())
                .hasSize(1);

        PipelineMapStep stepDetails = (PipelineMapStep) pipelineDetails.getSteps().iterator().next();

        soft.assertThat(stepDetails.getName())
                .isEqualTo("summingStep");
        soft.assertThat(stepDetails.getDescription())
                .isEqualTo("This adds a and b to get c");
        soft.assertThat(stepDetails.getType())
                .isEqualTo(TransformationType.MAP);
        soft.assertThat(stepDetails.getSelects())
                .extracting(Select::getId)
                .isNotNull();
        soft.assertThat(stepDetails.getSelects())
                .extracting(Select::getSelect, Select::getOutputFieldId, Select::isWindow)
                .containsExactlyInAnyOrder(
                        tuple("A", findFieldInSchema(schemaOut, "A").getId(), false),
                        tuple("B", findFieldInSchema(schemaOut, "B").getId(), false),
                        tuple("A+B", findFieldInSchema(schemaOut, "C").getId(), false));
        soft.assertThat(stepDetails.getSchemaIn())
                .isEqualTo(SchemaDetails.builder()
                        .id(schemaIn.getId())
                        .displayName("table1")
                        .physicalTableName("TB11")
                        .version(1)
                        .build());
        soft.assertThat(stepDetails.getSchemaOut())
                .isEqualTo(SchemaDetails.builder()
                        .id(schemaOut.getId())
                        .displayName("table2")
                        .physicalTableName("TBl2")
                        .version(2)
                        .build());
    }

    @Test
    public void createJoinPipelineStep_retrievesFieldAndSchemaDetailsForJoins() {
        ProductConfig product = productConfigRepository.save(ProductPopulated.productConfig().build());

        Table left = tableRepository.save(ProductPopulated.table()
                .productId(product.getId())
                .displayName("left table")
                .physicalTableName("LEFT")
                .version(1)
                .validationRules(emptySet())
                .fields(newLinkedHashSet(asList(
                        ProductPopulated.decimalField("A").build(),
                        ProductPopulated.decimalField("B").build())))
                .build());

        Field leftField = left.getFields().stream()
                .filter(field -> field.getName().equals("B"))
                .findFirst().get();

        Table right = tableRepository.save(ProductPopulated.table()
                .productId(product.getId())
                .displayName("right table")
                .physicalTableName("RIGHT")
                .version(1)
                .validationRules(emptySet())
                .fields(newLinkedHashSet(asList(
                        ProductPopulated.decimalField("C").build(),
                        ProductPopulated.decimalField("D").build(),
                        ProductPopulated.decimalField("E").build())))
                .build());

        Field rightField = right.getFields().stream()
                .filter(field -> field.getName().equals("D"))
                .findFirst().get();

        Table output = tableRepository.save(ProductPopulated.table()
                .productId(product.getId())
                .displayName("output table")
                .physicalTableName("OUT")
                .validationRules(emptySet())
                .fields(newLinkedHashSet(Arrays.asList(
                        ProductPopulated.decimalField("L_A").build(),
                        ProductPopulated.decimalField("R_C").build())))
                .build());

        Pipeline created = pipelineService.savePipeline(pipeline()
                .productId(product.getId())
                .name("join pipeline")
                .steps(newLinkedHashSet(singletonList(
                        ProductPopulated.joinPipelineStep()
                                .schemaOutId(output.getId())
                                .schemaOut(null)
                                .selects(newLinkedHashSet(asList(
                                        ProductPopulated.select()
                                                .select("L.A")
                                                .outputFieldId(findFieldInSchema(output, "L_A").getId())
                                                .order(0L)
                                                .build(),
                                        ProductPopulated.select()
                                                .select("R.C")
                                                .outputFieldId(findFieldInSchema(output, "R_C").getId())
                                                .order(1L)
                                                .build())))
                                .joins(newHashSet(ProductPopulated.join()
                                        .leftSchemaId(left.getId())
                                        .leftSchema(null)
                                        .rightSchemaId(right.getId())
                                        .rightSchema(null)
                                        .joinType(Join.JoinType.FULL_OUTER)
                                        .joinFields(newHashSet(JoinField.builder()
                                                .leftJoinFieldId(leftField.getId())
                                                .leftJoinField(null)
                                                .rightJoinFieldId(rightField.getId())
                                                .build()))
                                        .build()))
                                .build())))
                .build());

        entityManager.flush();
        entityManager.clear();

        Pipeline retrieved = pipelineRepository.findById(created.getId()).get();

        assertThat(retrieved.getSteps()).hasSize(1);

        Iterator<PipelineStep> stepsIterator = retrieved.getSteps().iterator();

        PipelineJoinStep joinStep = (PipelineJoinStep) stepsIterator.next();
        soft.assertThat(joinStep.getType())
                .isEqualTo(TransformationType.JOIN);
        soft.assertThat(joinStep.getSchemaOutId())
                .isEqualTo(output.getId());
        soft.assertThat(joinStep.getSelects())
                .extracting(Select::getId)
                .isNotNull();
        soft.assertThat(joinStep.getSelects())
                .extracting(Select::getSelect, Select::getOutputFieldId, Select::isWindow)
                .containsExactly(
                        tuple("L.A", findFieldInSchema(output, "L_A").getId(), false),
                        tuple("R.C", findFieldInSchema(output, "R_C").getId(), false));

        assertThat(joinStep.getJoins())
                .hasSize(1);

        Join join = joinStep.getJoins().iterator().next();

        soft.assertThat(join.getJoinType())
                .isEqualTo(Join.JoinType.FULL_OUTER);
        JoinField joinField = join.getJoinFields().iterator().next();
        soft.assertThat(joinField.getLeftJoinField())
                .isEqualTo(leftField);
        soft.assertThat(join.getLeftSchema())
                .isEqualTo(SchemaDetails.builder()
                        .id(left.getId())
                        .displayName("left table")
                        .physicalTableName("LEFT")
                        .version(1)
                        .build());

        soft.assertThat(joinField.getRightJoinField())
                .isEqualTo(rightField);

        soft.assertThat(join.getRightSchema())
                .isEqualTo(SchemaDetails.builder()
                        .id(right.getId())
                        .displayName("right table")
                        .physicalTableName("RIGHT")
                        .version(1)
                        .build());
    }

    @Test
    public void createUnionPipelineStep_retrievesAllPropertiesAndUnions() {
        ProductConfig product = productConfigRepository.save(productConfig().build());

        Table aSchema = tableRepository.save(ProductPopulated.table("A")
                .productId(product.getId())
                .fields(newHashSet(longField("ID").build(), stringField("NAME").build()))
                .version(1)
                .validationRules(emptySet())
                .build());
        Table bSchema = tableRepository.save(ProductPopulated.table("B")
                .productId(product.getId())
                .version(1)
                .fields(newHashSet(
                        longField("RAD_ID").build(),
                        stringField("PREF_NAME").build(),
                        stringField("SUF_NAME").build()))
                .validationRules(emptySet())
                .build());

        Table output = tableRepository.save(ProductPopulated.table("OUT")
                .productId(product.getId())
                .fields(newLinkedHashSet(Arrays.asList(longField("ID").build(), stringField("NAME").build())))
                .validationRules(emptySet())
                .build());

        ArrayList<Field> outputFields = new ArrayList<>(output.getFields());
        Field outputIdField = outputFields.get(0);
        Field outputNameField = outputFields.get(1);

        Pipeline createdPipeline = pipelineService.savePipeline(
                pipeline()
                        .productId(product.getId())
                        .name("union pipeline")
                        .steps(singleton(
                                PipelineUnionStep.builder()
                                        .schemaOutId(output.getId())
                                        .schemaInIds(newHashSet(aSchema.getId(), bSchema.getId()))
                                        .selects(newLinkedHashSet(asList(
                                                Select.builder().outputFieldId(outputIdField.getId())
                                                        .order(0L)
                                                        .selectUnion(Union.forSchema(aSchema.getId()))
                                                        .select("ID").build(),
                                                Select.builder().outputFieldId(outputNameField.getId())
                                                        .order(1L)
                                                        .selectUnion(Union.forSchema(aSchema.getId()))
                                                        .select("NAME").build(),
                                                Select.builder().outputFieldId(outputIdField.getId())
                                                        .order(0L)
                                                        .selectUnion(Union.forSchema(bSchema.getId()))
                                                        .select("RAD_ID").build(),
                                                Select.builder().outputFieldId(outputNameField.getId())
                                                        .order(1L)
                                                        .selectUnion(Union.forSchema(bSchema.getId()))
                                                        .select("CONCAT(PREF_NAME, ' ', SUF_NAME)").build())))
                                        .filters(newHashSet(
                                                PipelineFilter.builder()
                                                        .filter("NAME == 'HOMER'")
                                                        .unionSchemaId(aSchema.getId())
                                                        .build(),
                                                PipelineFilter.builder()
                                                        .filter("PREF_NAME == 'prof'")
                                                        .unionSchemaId(bSchema.getId())
                                                        .build()))
                                        .build()
                        ))
                        .build());

        entityManager.flush();
        entityManager.clear();

        Pipeline retrieved = pipelineRepository.findById(createdPipeline.getId()).get();

        assertThat(retrieved.getSteps()).hasSize(1);

        Iterator<PipelineStep> stepsIterator = retrieved.getSteps().iterator();

        PipelineUnionStep unionStep = (PipelineUnionStep) stepsIterator.next();
        soft.assertThat(unionStep.getType())
                .isEqualTo(TransformationType.UNION);
        soft.assertThat(unionStep.getSchemaOutId())
                .isEqualTo(output.getId());

        soft.assertThat(unionStep.getSchemaInIds())
                .containsExactlyInAnyOrder(aSchema.getId(), bSchema.getId());

        SchemaDetails schemaADetails = ProductPopulated.schemaDetails("A", 1).id(aSchema.getId()).build();
        SchemaDetails schemaBDetails = ProductPopulated.schemaDetails("B", 1).id(bSchema.getId()).build();

        soft.assertThat(unionStep.getSchemasIn())
                .containsExactlyInAnyOrder(schemaADetails, schemaBDetails);

        soft.assertThat(unionStep.getSelects())
                .extracting(
                        Select::getSelect,
                        Select::getOutputFieldId,
                        select -> select.getSelectUnion().getUnionSchema())
                .containsExactlyInAnyOrder(
                        tuple("ID", outputIdField.getId(), schemaADetails),
                        tuple("NAME", outputNameField.getId(), schemaADetails),
                        tuple("RAD_ID", outputIdField.getId(), schemaBDetails),
                        tuple("CONCAT(PREF_NAME, ' ', SUF_NAME)", outputNameField.getId(), schemaBDetails));
        soft.assertThat(unionStep.getPipelineFilters())
                .extracting(PipelineFilter::getFilter, PipelineFilter::getUnionSchemaId)
                .containsExactlyInAnyOrder(
                        tuple("NAME == 'HOMER'", aSchema.getId()),
                        tuple("PREF_NAME == 'prof'", bSchema.getId()));
    }

    @Test
    public void createWindowPipelineStep_retrievesAllPropertiesWithWindows() {
        ProductConfig product = productConfigRepository.save(ProductPopulated.productConfig().build());

        Table schemaIn = tableRepository.save(ProductPopulated.table()
                .productId(product.getId())
                .displayName("Product Revenue")
                .physicalTableName("IN")
                .validationRules(emptySet())
                .fields(newLinkedHashSet(asList(
                        stringField("Product").build(),
                        stringField("Category").build(),
                        ProductPopulated.intField("Revenue").build())))
                .build());

        Table schemaOut = tableRepository.save(ProductPopulated.table()
                .productId(product.getId())
                .displayName("Best selling products")
                .physicalTableName("OUT")
                .validationRules(emptySet())
                .fields(newLinkedHashSet(Arrays.asList(
                        stringField("Product").build(),
                        stringField("Category").build(),
                        ProductPopulated.intField("Revenue").build(),
                        ProductPopulated.intField("Rank").build())))
                .build());

        Long productFieldId = findFieldInSchema(schemaOut, "Product").getId();
        Long categoryFieldId = findFieldInSchema(schemaOut, "Category").getId();
        Long revenueFieldId = findFieldInSchema(schemaOut, "Revenue").getId();
        Long rankFieldId = findFieldInSchema(schemaOut, "Rank").getId();

        Pipeline createdPipeline = pipelineService.savePipeline(pipeline()
                .productId(product.getId())
                .name("Product revenue pipeline")
                .steps(singleton(
                        ProductPopulated.windowPipelineStep()
                                .schemaInId(schemaIn.getId())
                                .schemaOutId(schemaOut.getId())
                                .selects(newHashSet(
                                        ProductPopulated.select()
                                                .select("Product")
                                                .outputFieldId(productFieldId)
                                                .order(0L)
                                                .build(),
                                        ProductPopulated.select()
                                                .select("Category")
                                                .outputFieldId(categoryFieldId)
                                                .order(1L)
                                                .build(),
                                        ProductPopulated.select()
                                                .select("Revenue")
                                                .outputFieldId(revenueFieldId)
                                                .order(2L)
                                                .build(),
                                        ProductPopulated.select()
                                                .select("rank()").outputFieldId(rankFieldId)
                                                .order(3L)
                                                .isWindow(true)
                                                .window(Window.builder()
                                                        .partitions(singleton("Category"))
                                                        .orders(newLinkedHashSet(asList(
                                                                Order.builder()
                                                                        .fieldName("Revenue")
                                                                        .direction(DESC)
                                                                        .priority(0)
                                                                        .build(),
                                                                Order.builder()
                                                                        .fieldName("Product")
                                                                        .direction(ASC)
                                                                        .priority(1)
                                                                        .build())))
                                                        .build())
                                                .build()))
                                .build()
                ))
                .build());

        entityManager.flush();
        entityManager.clear();

        Pipeline retrieved = pipelineRepository.findById(createdPipeline.getId()).get();

        assertThat(retrieved.getSteps()).hasSize(1);

        assertThat(retrieved.getSteps())
                .hasOnlyElementsOfType(PipelineWindowStep.class);

        PipelineWindowStep step = (PipelineWindowStep) retrieved.getSteps().iterator().next();

        soft.assertThat(step.getType())
                .isEqualTo(TransformationType.WINDOW);

        soft.assertThat(step.getSchemaInId())
                .isEqualTo(schemaIn.getId());

        soft.assertThat(step.getSchemaOutId())
                .isEqualTo(schemaOut.getId());

        soft.assertThat(step.getSelects())
                .hasSize(4);

        assertThat(step.getSelects())
                .extracting(Select::getSelect, Select::getOutputFieldId, Select::isWindow, Select::getWindow)
                .containsExactlyInAnyOrder(
                        tuple("Product", productFieldId, false, Window.none()),
                        tuple("Category", categoryFieldId, false, Window.none()),
                        tuple("Revenue", revenueFieldId, false, Window.none()),
                        tuple("rank()", rankFieldId, true, Window.builder()
                                .partitions(singleton("Category"))
                                .orders(newHashSet(
                                        Order.builder().fieldName("Revenue").direction(DESC).priority(0).build(),
                                        Order.builder().fieldName("Product").direction(ASC).priority(1).build()))
                                .build()));
    }

    @Test
    public void createScriptletPipelineStep_SavesStepWithScriptletInputs() {
        ProductConfig product = productConfigRepository.save(ProductPopulated.productConfig().build());

        Table schemaIn = tableRepository.save(ProductPopulated.table()
                .productId(product.getId())
                .displayName("Input")
                .physicalTableName("IN")
                .version(5)
                .validationRules(emptySet())
                .build());

        Table schemaOut = tableRepository.save(ProductPopulated.table()
                .productId(product.getId())
                .displayName("Output")
                .physicalTableName("OUT")
                .version(6)
                .validationRules(emptySet())
                .build());

        Pipeline createdPipeline = pipelineService.savePipeline(pipeline()
                .productId(product.getId())
                .name("Pipeline with Scriptlet Step")
                .steps(singleton(
                        ProductPopulated.scriptletPipelineStep()
                                .name("scriptlet step name")
                                .description("scriptlet step description")
                                .jarFile("jar-file.jar")
                                .className("com.ScriptletStep")
                                .schemaIns(singleton(ProductPopulated.scriptletInput()
                                        .inputName("Scriptlet Step Input")
                                        .schemaInId(schemaIn.getId())
                                        .build()))
                                .schemaOutId(schemaOut.getId())
                                .build()))
                .build());

        entityManager.flush();
        entityManager.clear();

        Pipeline retrieved = pipelineRepository.findById(createdPipeline.getId()).get();

        soft.assertThat(retrieved.getSteps())
                .hasSize(1);

        PipelineScriptletStep scriptletStep = (PipelineScriptletStep) retrieved.getSteps().iterator().next();
        soft.assertThat(scriptletStep.getType())
                .isEqualTo(TransformationType.SCRIPTLET);
        soft.assertThat(scriptletStep.getName())
                .isEqualTo("scriptlet step name");
        soft.assertThat(scriptletStep.getDescription())
                .isEqualTo("scriptlet step description");
        soft.assertThat(scriptletStep.getJarFile())
                .isEqualTo("jar-file.jar");
        soft.assertThat(scriptletStep.getClassName())
                .isEqualTo("com.ScriptletStep");
        soft.assertThat(scriptletStep.getSchemaIns())
                .hasSize(1);

        ScriptletInput scriptletInput = scriptletStep.getSchemaIns().iterator().next();
        soft.assertThat(scriptletInput.getInputName())
                .isEqualTo("Scriptlet Step Input");
        soft.assertThat(scriptletInput.getSchemaInId())
                .isEqualTo(schemaIn.getId());
        soft.assertThat(scriptletInput.getSchemaIn())
                .isEqualTo(SchemaDetails.builder()
                        .id(schemaIn.getId())
                        .displayName(schemaIn.getDisplayName())
                        .physicalTableName(schemaIn.getPhysicalTableName())
                        .version(schemaIn.getVersion())
                        .build());

        soft.assertThat(scriptletStep.getSchemaOutId())
                .isEqualTo(schemaOut.getId());
        soft.assertThat(scriptletStep.getOutput())
                .isEqualTo(SchemaDetails.builder()
                        .id(schemaOut.getId())
                        .displayName(schemaOut.getDisplayName())
                        .physicalTableName(schemaOut.getPhysicalTableName())
                        .version(schemaOut.getVersion())
                        .build());

        pipelineRepository.deleteById(createdPipeline.getId());

        entityManager.flush();
        entityManager.clear();

        soft.assertThat(pipelineRepository.findAll())
                .isEmpty();
    }

    @Test
    public void deleteAllByProductId() {
        ProductConfig product = productConfigRepository.save(ProductPopulated.productConfig().build());

        Table schemaIn = tableRepository.save(ProductPopulated.table()
                .productId(product.getId())
                .displayName("table1")
                .physicalTableName("TB1")
                .build());
        Table schemaOut = tableRepository.save(ProductPopulated.table()
                .productId(product.getId())
                .displayName("table2")
                .physicalTableName("TB2")
                .build());

        Pipeline created1 = pipelineService.savePipeline(pipeline()
                .productId(product.getId())
                .name("test1")
                .steps(newLinkedHashSet(singletonList(
                        ProductPopulated.mapPipelineStep()
                                .schemaInId(schemaIn.getId())
                                .schemaOutId(schemaOut.getId())
                                .selects(emptySet())
                                .build())))
                .build());
        Pipeline created2 = pipelineService.savePipeline(pipeline()
                .productId(product.getId())
                .name("test2")
                .steps(newLinkedHashSet(singletonList(
                        ProductPopulated.mapPipelineStep()
                                .schemaInId(schemaIn.getId())
                                .schemaOutId(schemaOut.getId())
                                .selects(emptySet())
                                .build())))
                .build());

        assertThat(pipelineRepository.count())
                .isEqualTo(2);

        pipelineRepository.deleteAllByProductIdIn(singletonList(product.getId()));

        assertThat(pipelineRepository.count())
                .isEqualTo(0);
    }

    @Test
    public void findByProductId() {
        ProductConfig product = productConfigRepository.save(ProductPopulated.productConfig().build());

        Table schemaIn = tableRepository.save(ProductPopulated.table()
                .productId(product.getId())
                .displayName("table1")
                .physicalTableName("TB1")
                .build());
        Table schemaOut = tableRepository.save(ProductPopulated.table()
                .productId(product.getId())
                .displayName("table2")
                .physicalTableName("TB2")
                .build());

        Pipeline created1 = pipelineService.savePipeline(pipeline()
                .productId(product.getId())
                .name("test1")
                .steps(newLinkedHashSet(singletonList(
                        ProductPopulated.mapPipelineStep()
                                .schemaInId(schemaIn.getId())
                                .schemaOutId(schemaOut.getId())
                                .selects(emptySet())
                                .build())))
                .build());
        Pipeline created2 = pipelineService.savePipeline(pipeline()
                .productId(product.getId())
                .name("test2")
                .steps(newLinkedHashSet(singletonList(
                        ProductPopulated.mapPipelineStep()
                                .schemaInId(schemaIn.getId())
                                .schemaOutId(schemaOut.getId())
                                .selects(emptySet())
                                .build())))
                .build());

        List<Pipeline> byProductId = pipelineRepository.findByProductId(product.getId());

        assertThat(byProductId)
                .containsOnly(created1, created2);
    }

    @Test
    public void existsByName() {
        ProductConfig product = productConfigRepository.save(ProductPopulated.productConfig().build());

        Table schemaIn = tableRepository.save(ProductPopulated.table()
                .productId(product.getId())
                .displayName("table1")
                .physicalTableName("TB1")
                .build());
        Table schemaOut = tableRepository.save(ProductPopulated.table()
                .productId(product.getId())
                .displayName("table2")
                .physicalTableName("TB2")
                .build());

        Pipeline created = pipelineService.savePipeline(pipeline()
                .productId(product.getId())
                .name("test1")
                .steps(newLinkedHashSet(singletonList(
                        ProductPopulated.mapPipelineStep()
                                .schemaInId(schemaIn.getId())
                                .schemaOutId(schemaOut.getId())
                                .selects(emptySet())
                                .build())))
                .build());

        assertThat(pipelineRepository.existsByName("test1"))
                .isTrue();
        assertThat(pipelineRepository.existsByName("nah"))
                .isFalse();
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    public void findByName() {
        ProductConfig product = productConfigRepository.save(ProductPopulated.productConfig().build());

        Table schemaIn = tableRepository.save(ProductPopulated.table()
                .productId(product.getId())
                .displayName("table1")
                .physicalTableName("TB1")
                .build());
        Table schemaOut = tableRepository.save(ProductPopulated.table()
                .productId(product.getId())
                .displayName("table2")
                .physicalTableName("TB2")
                .build());

        Pipeline pipeline1 = pipelineService.savePipeline(pipeline()
                .productId(product.getId())
                .name("first pipeline")
                .steps(newLinkedHashSet(singletonList(
                        ProductPopulated.mapPipelineStep()
                                .schemaInId(schemaIn.getId())
                                .schemaOutId(schemaOut.getId())
                                .selects(emptySet())
                                .build())))
                .build());

        Pipeline pipeline2 = pipelineService.savePipeline(pipeline()
                .productId(product.getId())
                .name("second pipeline")
                .steps(newLinkedHashSet(singletonList(
                        ProductPopulated.mapPipelineStep()
                                .schemaInId(schemaIn.getId())
                                .schemaOutId(schemaOut.getId())
                                .selects(emptySet())
                                .build())))
                .build());

        Pipeline pipeline3 = pipelineService.savePipeline(pipeline()
                .productId(product.getId())
                .name("third pipeline")
                .steps(newLinkedHashSet(singletonList(
                        ProductPopulated.mapPipelineStep()
                                .schemaInId(schemaIn.getId())
                                .schemaOutId(schemaOut.getId())
                                .selects(emptySet())
                                .build())))
                .build());

        assertThat(pipelineRepository.findByName("first pipeline").get())
                .isEqualTo(pipeline1);

        assertThat(pipelineRepository.findByName("second pipeline").get())
                .isEqualTo(pipeline2);

        assertThat(pipelineRepository.findByName("third pipeline").get())
                .isEqualTo(pipeline3);

        assertThat(pipelineRepository.findByName("doesn't exist"))
                .isEmpty();
    }

    @Test
    public void findByIdFetchingFilters() {
        ProductConfig product = productConfigRepository.save(ProductPopulated.productConfig().build());

        Table table1 = tableRepository.save(ProductPopulated.table()
                .productId(product.getId())
                .displayName("table1")
                .physicalTableName("TB1")
                .build());
        Table table2 = tableRepository.save(ProductPopulated.table()
                .productId(product.getId())
                .displayName("table2")
                .physicalTableName("TB2")
                .build());
        Table table3 = tableRepository.save(ProductPopulated.table()
                .productId(product.getId())
                .displayName("table3")
                .physicalTableName("TB3")
                .build());

        Pipeline pipeline1 = pipelineService.savePipeline(pipeline()
                .productId(product.getId())
                .name("first pipeline")
                .steps(newLinkedHashSet(singletonList(
                        ProductPopulated.mapPipelineStep()
                                .name("first pipeline step 1")
                                .schemaInId(table1.getId())
                                .schemaOutId(table2.getId())
                                .selects(emptySet())
                                .filters(newHashSet("A > 0", "B != -999", "C == 0"))
                                .build())))
                .build());

        Pipeline pipeline2 = pipelineService.savePipeline(pipeline()
                .productId(product.getId())
                .name("second pipeline")
                .steps(newLinkedHashSet(asList(
                        ProductPopulated.mapPipelineStep()
                                .name("second pipeline step 1")
                                .schemaInId(table1.getId())
                                .schemaOutId(table2.getId())
                                .selects(emptySet())
                                .filters(newHashSet("D >= 100", "D < 1000"))
                                .build(),
                        ProductPopulated.windowPipelineStep()
                                .name("second pipeline step 2")
                                .schemaInId(table2.getId())
                                .schemaOutId(table3.getId())
                                .selects(emptySet())
                                .filters(newHashSet("E IS NOT NULL"))
                                .build())))
                .build());

        entityManager.flush();
        entityManager.clear();

        Optional<Pipeline> firstPipeline = pipelineRepository.findByIdFetchingFilters(pipeline1.getId());
        Optional<Pipeline> secondPipeline = pipelineRepository.findByIdFetchingFilters(pipeline2.getId());

        soft.assertThat(firstPipeline)
                .isPresent();

        soft.assertThat(firstPipeline.get())
                .extracting(Pipeline::getId, Pipeline::getName)
                .containsExactly(pipeline1.getId(), "first pipeline");

        soft.assertThat(firstPipeline.get().getSteps())
                .extracting(PipelineStep::getName, PipelineStep::getFilters)
                .containsExactly(tuple("first pipeline step 1", newHashSet("A > 0", "B != -999", "C == 0")));

        soft.assertThat(secondPipeline)
                .isPresent();

        soft.assertThat(secondPipeline.get())
                .extracting(Pipeline::getId, Pipeline::getName)
                .containsExactly(pipeline2.getId(), "second pipeline");

        soft.assertThat(secondPipeline.get().getSteps())
                .extracting(PipelineStep::getName, PipelineStep::getFilters)
                .containsExactlyInAnyOrder(
                        tuple("second pipeline step 1", newHashSet("D >= 100", "D < 1000")),
                        tuple("second pipeline step 2", newHashSet("E IS NOT NULL")));
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private static Field findFieldInSchema(final Table schema, final String fieldName) {
        return schema.getFields().stream()
                .filter(field -> field.getName().equals(fieldName))
                .findFirst()
                .get();
    }
}
