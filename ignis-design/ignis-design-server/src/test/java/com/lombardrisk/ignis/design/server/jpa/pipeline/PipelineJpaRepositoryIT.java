package com.lombardrisk.ignis.design.server.jpa.pipeline;

import com.lombardrisk.ignis.client.external.pipeline.export.TransformationType;
import com.lombardrisk.ignis.client.external.pipeline.export.step.JoinType;
import com.lombardrisk.ignis.design.field.model.DecimalField;
import com.lombardrisk.ignis.design.field.model.Field;
import com.lombardrisk.ignis.design.server.DesignStudioTestConfig;
import com.lombardrisk.ignis.design.server.fixtures.Design;
import com.lombardrisk.ignis.design.server.jpa.FieldJpaRepository;
import com.lombardrisk.ignis.design.server.jpa.ProductConfigJpaRepository;
import com.lombardrisk.ignis.design.server.jpa.SchemaJpaRepository;
import com.lombardrisk.ignis.design.server.pipeline.model.Pipeline;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineAggregationStep;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineJoinStep;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineMapStep;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineScriptletStep;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineStep;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineUnionStep;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineWindowStep;
import com.lombardrisk.ignis.design.server.pipeline.model.ScriptletInput;
import com.lombardrisk.ignis.design.server.pipeline.model.join.Join;
import com.lombardrisk.ignis.design.server.pipeline.model.join.JoinField;
import com.lombardrisk.ignis.design.server.pipeline.model.select.Order;
import com.lombardrisk.ignis.design.server.pipeline.model.select.PipelineFilter;
import com.lombardrisk.ignis.design.server.pipeline.model.select.Select;
import com.lombardrisk.ignis.design.server.pipeline.model.select.Union;
import com.lombardrisk.ignis.design.server.pipeline.model.select.Window;
import com.lombardrisk.ignis.design.server.productconfig.ProductConfig;
import com.lombardrisk.ignis.design.server.productconfig.schema.Schema;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static com.lombardrisk.ignis.design.field.DesignField.Populated.decimalField;
import static com.lombardrisk.ignis.design.field.DesignField.Populated.intField;
import static com.lombardrisk.ignis.design.field.DesignField.Populated.longField;
import static com.lombardrisk.ignis.design.field.DesignField.Populated.stringField;
import static com.lombardrisk.ignis.design.server.fixtures.Design.Populated.emptySchema;
import static com.lombardrisk.ignis.design.server.fixtures.Design.Populated.join;
import static com.lombardrisk.ignis.design.server.fixtures.Design.Populated.pipeline;
import static com.lombardrisk.ignis.design.server.fixtures.Design.Populated.pipelineAggregationStep;
import static com.lombardrisk.ignis.design.server.fixtures.Design.Populated.pipelineJoinStep;
import static com.lombardrisk.ignis.design.server.fixtures.Design.Populated.pipelineMapStep;
import static com.lombardrisk.ignis.design.server.fixtures.Design.Populated.pipelineUnionStep;
import static com.lombardrisk.ignis.design.server.fixtures.Design.Populated.pipelineWindowStep;
import static com.lombardrisk.ignis.design.server.fixtures.Design.Populated.productConfig;
import static com.lombardrisk.ignis.design.server.fixtures.Design.Populated.schema;
import static com.lombardrisk.ignis.design.server.fixtures.Design.Populated.select;
import static com.lombardrisk.ignis.design.server.fixtures.Design.Populated.unionSelect;
import static com.lombardrisk.ignis.design.server.pipeline.model.select.Order.Direction.ASC;
import static com.lombardrisk.ignis.design.server.pipeline.model.select.Order.Direction.DESC;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@RunWith(SpringRunner.class)
@DesignStudioTestConfig
public class PipelineJpaRepositoryIT {

    @Autowired
    private PipelineJpaRepository pipelineJpaRepository;
    @Autowired
    private PipelineStepJpaRepository pipelineStepJpaRepository;

    @Autowired
    private SchemaJpaRepository schemaJpaRepository;

    @Autowired
    private FieldJpaRepository fieldJpaRepository;

    @Autowired
    private ProductConfigJpaRepository productConfigJpaRepository;

    @Autowired
    private EntityManager entityManager;

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Test
    public void create_retrieve() {
        ProductConfig product = productConfigJpaRepository.save(productConfig().build());

        Schema table1 = schemaJpaRepository.save(schema()
                .productId(product.getId())
                .displayName("table1")
                .physicalTableName("tb1")
                .validationRules(emptySet())
                .fields(emptySet())
                .build());

        DecimalField table1A = fieldJpaRepository.save(
                decimalField("A").schemaId(table1.getId()).build());
        DecimalField table1B = fieldJpaRepository.save(
                decimalField("B").schemaId(table1.getId()).build());

        Schema table2 = schemaJpaRepository.save(schema()
                .productId(product.getId())
                .displayName("table2")
                .physicalTableName("tb2")
                .validationRules(emptySet())
                .fields(emptySet())
                .build());

        DecimalField table2A = fieldJpaRepository.save(
                decimalField("A").schemaId(table2.getId()).build());
        DecimalField table2B = fieldJpaRepository.save(
                decimalField("B").schemaId(table2.getId()).build());
        DecimalField table2C = fieldJpaRepository.save(
                decimalField("C").schemaId(table2.getId()).build());

        Schema table3 = schemaJpaRepository.save(schema()
                .productId(product.getId())
                .displayName("table3")
                .physicalTableName("tb3")
                .validationRules(emptySet())
                .fields(emptySet())
                .build());

        DecimalField table3C =
                fieldJpaRepository.save(decimalField("C").schemaId(table3.getId()).build());

        Pipeline createdPipeline = pipelineJpaRepository.save(pipeline()
                .productId(product.getId())
                .name("test")
                .build());

        pipelineStepJpaRepository.save(
                pipelineMapStep()
                        .pipelineId(createdPipeline.getId())
                        .name("summingStep")
                        .description("This adds a and b to get c")
                        .schemaInId(table1.getId())
                        .schemaOutId(table2.getId())
                        .selects(newLinkedHashSet(asList(
                                select()
                                        .outputFieldId(table2A.getId())
                                        .order(0L)
                                        .isIntermediate(true)
                                        .select("A")
                                        .build(),
                                select()
                                        .outputFieldId(table2B.getId())
                                        .order(1L)
                                        .isIntermediate(true)
                                        .select("B").build(),
                                select()
                                        .outputFieldId(table2C.getId())
                                        .select("A+B")
                                        .build())))
                        .filters(newLinkedHashSet(asList("A > 10", "B > 20")))
                        .build());

        pipelineStepJpaRepository.save(
                pipelineAggregationStep()
                        .pipelineId(createdPipeline.getId())
                        .name("averageStep")
                        .description("Average c")
                        .schemaInId(table2.getId())
                        .schemaOutId(table3.getId())
                        .selects(singleton(
                                select()
                                        .outputFieldId(table3C.getId())
                                        .select("AVG(C)")
                                        .build()))
                        .groupings(newLinkedHashSet(singletonList("C")))
                        .build());

        entityManager.flush();
        entityManager.clear();

        Pipeline retrievedPipeline = pipelineJpaRepository.findById(createdPipeline.getId()).get();

        soft.assertThat(retrievedPipeline.getName())
                .isEqualTo("test");
        soft.assertThat(retrievedPipeline.getProductId())
                .isEqualTo(product.getId());
        assertThat(retrievedPipeline.getSteps()).hasSize(2);

        Iterator<PipelineStep> stepsIterator = retrievedPipeline.getSteps().iterator();

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
                .extracting(Select::getSelect, Select::getOutputFieldId, Select::isIntermediate, Select::getOrder)
                .containsExactly(
                        tuple("A", table2A.getId(), true, 0L),
                        tuple("B", table2B.getId(), true, 1L),
                        tuple("A+B", table2C.getId(), false, null));
        soft.assertThat(step1.getSelects())
                .extracting(Select::getWindow)
                .flatExtracting(Window::getPartitions)
                .isEmpty();
        soft.assertThat(step1.getSelects())
                .extracting(Select::getWindow)
                .flatExtracting(Window::getOrders)
                .isEmpty();

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
                .extracting(Select::getSelect, Select::getOutputFieldId)
                .containsExactly(tuple("AVG(C)", table3C.getId()));
        soft.assertThat(step2.getSelects())
                .extracting(Select::getWindow)
                .flatExtracting(Window::getPartitions)
                .isEmpty();
        soft.assertThat(step2.getSelects())
                .extracting(Select::getWindow)
                .flatExtracting(Window::getOrders)
                .isEmpty();
        soft.assertThat(step2.getFilters())
                .isEmpty();
        soft.assertThat(step2.getGroupings())
                .containsExactly("C");
    }

    @Test
    public void createJoinPipelineStep_retrievesAllPropertiesAndJoins() {
        ProductConfig product = productConfigJpaRepository.save(productConfig().build());

        Schema left = schemaJpaRepository.save(schema()
                .productId(product.getId())
                .displayName("left table")
                .physicalTableName("left")
                .majorVersion(1)
                .validationRules(emptySet())
                .fields(emptySet())
                .build());

        Field leftFieldA = fieldJpaRepository.save(
                decimalField("A").schemaId(left.getId()).build());
        Field leftFieldB = fieldJpaRepository.save(
                decimalField("B").schemaId(left.getId()).build());

        Schema right = schemaJpaRepository.save(schema()
                .productId(product.getId())
                .displayName("right table")
                .physicalTableName("right")
                .majorVersion(1)
                .validationRules(emptySet())
                .fields(emptySet())
                .build());

        Field rightFieldA = fieldJpaRepository.save(
                decimalField("C").schemaId(right.getId()).build());
        Field rightFieldD = fieldJpaRepository.save(
                decimalField("D").schemaId(right.getId()).build());
        Field rightFieldE = fieldJpaRepository.save(
                decimalField("E").schemaId(right.getId()).build());

        Schema output = schemaJpaRepository.save(schema()
                .productId(product.getId())
                .displayName("output table")
                .physicalTableName("out")
                .validationRules(emptySet())
                .fields(emptySet())
                .build());

        Field outputFieldA = fieldJpaRepository.save(
                decimalField("L_A").schemaId(output.getId()).build());
        Field outputFieldC = fieldJpaRepository.save(
                decimalField("R_C").schemaId(output.getId()).build());

        Pipeline createdPipeline = pipelineJpaRepository.save(pipeline()
                .productId(product.getId())
                .name("join pipeline")
                .build());

        pipelineStepJpaRepository.save(
                pipelineJoinStep()
                        .pipelineId(createdPipeline.getId())
                        .schemaOutId(output.getId())
                        .selects(newLinkedHashSet(asList(
                                select()
                                        .outputFieldId(outputFieldA.getId())
                                        .select("L.A")
                                        .order(0L)
                                        .build(),
                                select()
                                        .outputFieldId(outputFieldC.getId())
                                        .select("R.C")
                                        .order(1L)
                                        .build())))
                        .joins(newHashSet(join()
                                .leftSchemaId(left.getId())
                                .rightSchemaId(right.getId())
                                .joinType(JoinType.FULL_OUTER)
                                .joinFields(newHashSet(
                                        JoinField.builder()
                                                .leftJoinFieldId(leftFieldA.getId())
                                                .rightJoinFieldId(rightFieldA.getId())
                                                .build(),
                                        JoinField.builder()
                                                .leftJoinFieldId(leftFieldB.getId())
                                                .rightJoinFieldId(rightFieldD.getId())
                                                .build()))
                                .build()))
                        .build());

        entityManager.flush();
        entityManager.clear();

        Pipeline retrieved = pipelineJpaRepository.findById(createdPipeline.getId()).get();

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
                .extracting(Select::getSelect, Select::getOutputFieldId)
                .containsExactly(
                        tuple("L.A", outputFieldA.getId()),
                        tuple("R.C", outputFieldC.getId()));
        soft.assertThat(joinStep.getSelects())
                .extracting(Select::getWindow)
                .flatExtracting(Window::getOrders)
                .isEmpty();
        soft.assertThat(joinStep.getSelects())
                .extracting(Select::getWindow)
                .flatExtracting(Window::getPartitions)
                .isEmpty();
        assertThat(joinStep.getJoins())
                .hasSize(1);

        Join join = joinStep.getJoins().iterator().next();

        soft.assertThat(join.getJoinType())
                .isEqualTo(JoinType.FULL_OUTER);
        soft.assertThat(join.getLeftSchemaId())
                .isEqualTo(left.getId());
        soft.assertThat(join.getRightSchemaId())
                .isEqualTo(right.getId());

        List<JoinField> joinFields = new ArrayList<>(join.getJoinFields());
        JoinField aToAJoinField = joinFields.get(0);
        JoinField bToDJoinField = joinFields.get(1);

        soft.assertThat(aToAJoinField.getLeftJoinFieldId())
                .isEqualTo(leftFieldA.getId());
        soft.assertThat(aToAJoinField.getRightJoinFieldId())
                .isEqualTo(rightFieldA.getId());

        soft.assertThat(bToDJoinField.getLeftJoinFieldId())
                .isEqualTo(leftFieldB.getId());
        soft.assertThat(bToDJoinField.getRightJoinFieldId())
                .isEqualTo(rightFieldD.getId());
    }

    @Test
    public void createUnionPipelineStep_retrievesAllPropertiesAndUnions() {
        ProductConfig product = productConfigJpaRepository.save(productConfig().build());

        Schema aSchema = schemaJpaRepository.save(emptySchema("A").productId(product.getId()).build());

        Field aSchemaIdField = fieldJpaRepository.save(longField("ID").schemaId(aSchema.getId()).build());
        Field aSchemaNameField = fieldJpaRepository.save(stringField("NAME").schemaId(aSchema.getId()).build());

        Schema bSchema = schemaJpaRepository.save(emptySchema("B").productId(product.getId()).build());

        Field bSchemaIdField = fieldJpaRepository.save(longField("RAD_ID").schemaId(bSchema.getId()).build());
        Field bSchemaPrefField = fieldJpaRepository.save(stringField("PREF_NAME").schemaId(bSchema.getId()).build());
        Field bSchemaSufField = fieldJpaRepository.save(stringField("SUF_NAME").schemaId(bSchema.getId()).build());

        Schema output = schemaJpaRepository.save(emptySchema("OUT").productId(product.getId()).build());

        Field outputIdField = fieldJpaRepository.save(longField("ID").schemaId(output.getId()).build());
        Field outputNameField = fieldJpaRepository.save(stringField("NAME").schemaId(output.getId()).build());

        Pipeline createdPipeline = pipelineJpaRepository.save(
                pipeline().productId(product.getId()).name("union pipeline").build());

        pipelineStepJpaRepository.save(
                pipelineUnionStep()
                        .pipelineId(createdPipeline.getId())
                        .schemaOutId(output.getId())
                        .schemaInIds(newHashSet(aSchema.getId(), bSchema.getId()))
                        .selects(newLinkedHashSet(asList(
                                unionSelect().outputFieldId(outputIdField.getId())
                                        .order(0L)
                                        .union(Union.forSchema(aSchema.getId()))
                                        .select("ID").build(),
                                unionSelect().outputFieldId(outputNameField.getId())
                                        .order(1L)
                                        .union(Union.forSchema(aSchema.getId()))
                                        .select("NAME").build(),
                                unionSelect().outputFieldId(outputIdField.getId())
                                        .order(0L)
                                        .union(Union.forSchema(bSchema.getId()))
                                        .select("RAD_ID").build(),
                                unionSelect().outputFieldId(outputNameField.getId())
                                        .order(1L)
                                        .union(Union.forSchema(bSchema.getId()))
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
                        .build());

        entityManager.flush();
        entityManager.clear();

        Pipeline retrieved = pipelineJpaRepository.findById(createdPipeline.getId()).get();

        assertThat(retrieved.getSteps()).hasSize(1);

        Iterator<PipelineStep> stepsIterator = retrieved.getSteps().iterator();

        PipelineUnionStep unionStep = (PipelineUnionStep) stepsIterator.next();
        soft.assertThat(unionStep.getType())
                .isEqualTo(TransformationType.UNION);
        soft.assertThat(unionStep.getSchemaInIds())
                .containsExactlyInAnyOrder(aSchema.getId(), bSchema.getId());
        soft.assertThat(unionStep.getSchemaOutId())
                .isEqualTo(output.getId());

        soft.assertThat(unionStep.getSelects())
                .extracting(Select::getSelect, Select::getOutputFieldId, select -> select.getUnion().getUnionSchemaId())
                .containsExactlyInAnyOrder(
                        tuple("ID", outputIdField.getId(), aSchema.getId()),
                        tuple("NAME", outputNameField.getId(), aSchema.getId()),
                        tuple("RAD_ID", outputIdField.getId(), bSchema.getId()),
                        tuple("CONCAT(PREF_NAME, ' ', SUF_NAME)", outputNameField.getId(), bSchema.getId()));
        soft.assertThat(unionStep.getPipelineFilters())
                .extracting(PipelineFilter::getFilter, PipelineFilter::getUnionSchemaId)
                .containsExactlyInAnyOrder(
                        tuple("NAME == 'HOMER'", aSchema.getId()),
                        tuple("PREF_NAME == 'prof'", bSchema.getId()));
    }

    @Test
    public void createWindowPipelineStep_retrievesAllPropertiesWithWindows() {
        ProductConfig product = productConfigJpaRepository.save(productConfig().build());

        Schema schemaIn = schemaJpaRepository.save(schema()
                .productId(product.getId())
                .displayName("Product Revenue")
                .physicalTableName("IN")
                .validationRules(emptySet())
                .fields(emptySet())
                .build());

        Field productField = fieldJpaRepository.save(stringField("Product")
                .schemaId(schemaIn.getId())
                .build());
        Field categoryField = fieldJpaRepository.save(stringField("Category")
                .schemaId(schemaIn.getId())
                .build());
        Field revenueField =
                fieldJpaRepository.save(intField("Revenue").schemaId(schemaIn.getId()).build());

        Schema schemaOut = schemaJpaRepository.save(schema()
                .productId(product.getId())
                .displayName("Best selling products")
                .physicalTableName("OUT")
                .validationRules(emptySet())
                .fields(emptySet())
                .build());

        Field productOutField = fieldJpaRepository.save(stringField("Product").schemaId(schemaOut.getId()).build());
        Field categoryOutField = fieldJpaRepository.save(stringField("Category").schemaId(schemaOut.getId()).build());
        Field revenueOutField = fieldJpaRepository.save(intField("Revenue").schemaId(schemaOut.getId()).build());
        Field rankOutField = fieldJpaRepository.save(intField("Rank").schemaId(schemaOut.getId()).build());

        Pipeline createdPipeline = pipelineJpaRepository.save(pipeline()
                .productId(product.getId())
                .name("Product revenue pipeline")
                .build());

        pipelineStepJpaRepository.save(
                pipelineWindowStep()
                        .pipelineId(createdPipeline.getId())
                        .schemaInId(schemaIn.getId())
                        .schemaOutId(schemaOut.getId())
                        .selects(newHashSet(
                                select()
                                        .select("Category")
                                        .outputFieldId(categoryOutField.getId())
                                        .order(0L)
                                        .build(),
                                select()
                                        .select("Product")
                                        .outputFieldId(productOutField.getId())
                                        .order(1L)
                                        .build(),
                                select()
                                        .select("Revenue")
                                        .outputFieldId(revenueOutField.getId())
                                        .order(2L)
                                        .build(),
                                select()
                                        .select("rank()")
                                        .outputFieldId(rankOutField.getId())
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
                        .build());

        entityManager.flush();
        entityManager.clear();

        Pipeline retrieved = pipelineJpaRepository.findById(createdPipeline.getId()).get();

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

        Iterator<Select> selectIterator = step.getSelects().iterator();

        Select select1 = selectIterator.next();
        soft.assertThat(select1.getSelect()).isEqualTo("Category");
        soft.assertThat(select1.getOutputFieldId()).isEqualTo(categoryOutField.getId());
        soft.assertThat(select1.getWindow().getPartitions()).isEmpty();
        soft.assertThat(select1.getWindow().getOrders()).isEmpty();

        Select select2 = selectIterator.next();
        soft.assertThat(select2.getSelect()).isEqualTo("Product");
        soft.assertThat(select2.getOutputFieldId())
                .isEqualTo(productOutField.getId());
        soft.assertThat(select2.getWindow().getPartitions()).isEmpty();
        soft.assertThat(select2.getWindow().getOrders()).isEmpty();

        Select select3 = selectIterator.next();
        soft.assertThat(select3.getSelect()).isEqualTo("Revenue");
        soft.assertThat(select3.getOutputFieldId())
                .isEqualTo(revenueOutField.getId());
        soft.assertThat(select3.getWindow().getPartitions()).isEmpty();
        soft.assertThat(select3.getWindow().getOrders()).isEmpty();

        Select select4 = selectIterator.next();
        soft.assertThat(select4.getSelect()).isEqualTo("rank()");
        soft.assertThat(select4.getOutputFieldId())
                .isEqualTo(rankOutField.getId());
        soft.assertThat(select4.getWindow().getPartitions()).containsExactly("Category");
        soft.assertThat(select4.getWindow().getOrders())
                .containsExactly(
                        Order.builder().fieldName("Revenue").direction(DESC).priority(0).build(),
                        Order.builder().fieldName("Product").direction(ASC).priority(1).build());
    }

    @Test
    public void findByProductId() {
        ProductConfig productConfig1 = productConfigJpaRepository.save(productConfig()
                .name("p1")
                .build());
        ProductConfig productConfig2 = productConfigJpaRepository.save(productConfig()
                .name("p2")
                .build());

        Schema inputSchema1 = schemaJpaRepository.save(schema()
                .fields(emptySet())
                .productId(productConfig1.getId())
                .displayName("Input1")
                .physicalTableName("IN1")
                .build());

        Schema outputSchema1 = schemaJpaRepository.save(schema()
                .fields(emptySet())
                .productId(productConfig1.getId())
                .displayName("Output1")
                .physicalTableName("OUT1")
                .build());

        Schema inputSchema2 = schemaJpaRepository.save(schema()
                .fields(emptySet())
                .productId(productConfig2.getId())
                .displayName("Input2")
                .physicalTableName("IN2")
                .build());

        Schema outputSchema2 = schemaJpaRepository.save(schema()
                .fields(emptySet())
                .productId(productConfig2.getId())
                .displayName("Output2")
                .physicalTableName("OUT2")
                .build());

        productConfig1.setTables(newHashSet(inputSchema1, outputSchema1));
        productConfig1.setTables(newHashSet(inputSchema2, outputSchema2));

        productConfigJpaRepository.saveAndFlush(productConfig1);
        productConfigJpaRepository.saveAndFlush(productConfig2);

        Pipeline pipeline1 = pipelineJpaRepository.save(pipeline()
                .name("p1")
                .productId(productConfig1.getId())
                .steps(newHashSet(pipelineMapStep()
                        .schemaInId(inputSchema1.getId())
                        .schemaOutId(outputSchema1.getId())
                        .build()))
                .build());

        Pipeline pipeline2 = pipelineJpaRepository.save(pipeline()
                .name("p2")
                .productId(productConfig2.getId())
                .steps(newHashSet(pipelineMapStep()
                        .schemaInId(inputSchema2.getId())
                        .schemaOutId(outputSchema2.getId())
                        .build()))
                .build());

        soft.assertThat(pipelineJpaRepository.findByProductId(productConfig1.getId()))
                .containsOnly(pipeline1);
        soft.assertThat(pipelineJpaRepository.findByProductId(productConfig2.getId()))
                .containsOnly(pipeline2);
    }

    @Test
    public void deleteAllByProductId() {
        ProductConfig productConfig1 = productConfigJpaRepository.save(productConfig()
                .name("p1")
                .build());

        ProductConfig productConfig2 = productConfigJpaRepository.save(productConfig()
                .name("p2")
                .build());

        Schema inputSchema1 = schemaJpaRepository.save(schema().fields(emptySet())
                .productId(productConfig1.getId())
                .displayName("Input1")
                .physicalTableName("IN1")
                .build());

        Schema outputSchema1 = schemaJpaRepository.save(schema().fields(emptySet())
                .productId(productConfig1.getId())
                .displayName("Output1")
                .physicalTableName("OUT1")
                .build());

        Schema inputSchema2 = schemaJpaRepository.save(schema().fields(emptySet())
                .productId(productConfig2.getId())
                .displayName("Input2")
                .physicalTableName("IN2")
                .build());

        Schema outputSchema2 = schemaJpaRepository.save(schema().fields(emptySet())
                .productId(productConfig2.getId())
                .displayName("Output2")
                .physicalTableName("OUT2")
                .build());

        Pipeline pipeline1 = pipelineJpaRepository.save(pipeline()
                .name("p1")
                .productId(productConfig1.getId())
                .steps(newHashSet(pipelineMapStep()
                        .schemaInId(inputSchema1.getId())
                        .schemaOutId(outputSchema1.getId())
                        .build()))
                .build());

        Pipeline pipeline2 = pipelineJpaRepository.save(pipeline()
                .name("p2")
                .productId(productConfig2.getId())
                .steps(newHashSet(pipelineMapStep()
                        .schemaInId(inputSchema2.getId())
                        .schemaOutId(outputSchema2.getId())
                        .build()))
                .build());

        Pipeline pipeline3 = pipelineJpaRepository.save(pipeline()
                .name("p3")
                .productId(productConfig2.getId())
                .steps(newHashSet(pipelineMapStep()
                        .schemaInId(inputSchema2.getId())
                        .schemaOutId(outputSchema2.getId())
                        .build()))
                .build());

        soft.assertThat(pipelineJpaRepository.findAll())
                .containsOnly(pipeline1, pipeline2, pipeline3);

        pipelineJpaRepository.deleteAllByProductId(productConfig2.getId());

        soft.assertThat(pipelineJpaRepository.findAll())
                .containsOnly(pipeline1);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private Schema find(final ProductConfig productConfig, final String displayName) {
        return productConfig.getTables().stream()
                .filter(schema -> schema.getDisplayName().equals(displayName))
                .findFirst()
                .get();
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private static Field findFieldInSchema(final Schema schema, final String fieldName) {
        return schema.getFields().stream()
                .filter(field -> field.getName().equals(fieldName))
                .findFirst()
                .get();
    }

    @Test
    public void createScriptletPipelineStep_SavesStepWithScriptletInputs() {
        ProductConfig product = productConfigJpaRepository.save(Design.Populated.productConfig().build());

        Schema schemaIn = schemaJpaRepository.save(schema()
                .productId(product.getId())
                .displayName("Input")
                .physicalTableName("IN")
                .validationRules(emptySet())
                .build());

        Schema schemaOut = schemaJpaRepository.save(schema()
                .productId(product.getId())
                .displayName("Output")
                .physicalTableName("OUT")
                .validationRules(emptySet())
                .build());

        Pipeline createdPipeline = pipelineJpaRepository.save(pipeline()
                .productId(product.getId())
                .name("Pipeline with Scriptlet Step")
                .steps(singleton(
                        Design.Populated.scriptletPipelineStep()
                                .name("scriptlet step name")
                                .description("scriptlet step description")
                                .jarFile("jar-file.jar")
                                .className("com.ScriptletStep")
                                .schemaOutId(schemaOut.getId())
                                .schemaIns(singleton(Design.Populated.scriptletInput()
                                        .inputName("Scriptlet Step Input")
                                        .schemaInId(schemaIn.getId())
                                        .build()))

                                .build()))
                .build());

        entityManager.flush();

        Pipeline retrieved = pipelineJpaRepository.findById(createdPipeline.getId()).get();

        soft.assertThat(retrieved.getSteps()).hasSize(1);

        Iterator<PipelineStep> stepsIterator = retrieved.getSteps().iterator();

        PipelineScriptletStep scriptletStep = (PipelineScriptletStep) stepsIterator.next();

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

        soft.assertThat(scriptletStep.getSchemaOutId())
                .isEqualTo(schemaOut.getId());

        pipelineJpaRepository.deleteById(createdPipeline.getId());

        entityManager.flush();
        entityManager.clear();

        soft.assertThat(pipelineJpaRepository.findAll())
                .isEmpty();
    }
}
