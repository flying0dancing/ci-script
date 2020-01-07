package com.lombardrisk.ignis.server.jpa;

import com.lombardrisk.ignis.server.config.IntegrationTestConfig;
import com.lombardrisk.ignis.server.product.fixture.ProductPopulated;
import com.lombardrisk.ignis.server.product.pipeline.PipelineJpaRepository;
import com.lombardrisk.ignis.server.product.pipeline.SelectJpaRepository;
import com.lombardrisk.ignis.server.product.pipeline.model.Pipeline;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineStep;
import com.lombardrisk.ignis.server.product.pipeline.select.Order;
import com.lombardrisk.ignis.server.product.pipeline.select.Select;
import com.lombardrisk.ignis.server.product.pipeline.select.Window;
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
import java.util.List;

import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static com.lombardrisk.ignis.server.product.fixture.ProductPopulated.pipeline;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;

@RunWith(SpringRunner.class)
@IntegrationTestConfig
public class SelectJpaRepositoryIT {

    @Autowired
    private SelectJpaRepository selectRepository;

    @Autowired
    private PipelineJpaRepository pipelineRepository;

    @Autowired
    private TableRepository tableRepository;

    @Autowired
    private ProductConfigRepository productConfigRepository;

    @Autowired
    private EntityManager entityManager;

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    private ProductConfig product;
    private Table table1;
    private Table table2;
    private Table table3;

    @Before
    public void setUp() {
        product = productConfigRepository.save(ProductPopulated.productConfig().build());

        table1 = tableRepository.save(ProductPopulated.table()
                .productId(product.getId())
                .displayName("table1")
                .physicalTableName("TB1")
                .validationRules(emptySet())
                .fields(newLinkedHashSet(asList(
                        ProductPopulated.decimalField("A").build(),
                        ProductPopulated.decimalField("B").build(),
                        ProductPopulated.stringField("C").build(),
                        ProductPopulated.stringField("D").build())))
                .build());

        table2 = tableRepository.save(ProductPopulated.table()
                .productId(product.getId())
                .displayName("table2")
                .physicalTableName("TB2")
                .validationRules(emptySet())
                .fields(newLinkedHashSet(asList(
                        ProductPopulated.decimalField("X").build(),
                        ProductPopulated.decimalField("Y").build(),
                        ProductPopulated.intField("Z").build())))
                .build());

        table3 = tableRepository.save(ProductPopulated.table()
                .productId(product.getId())
                .displayName("table3")
                .physicalTableName("TB3")
                .validationRules(emptySet())
                .fields(newLinkedHashSet(asList(
                        ProductPopulated.decimalField("SUM_X").build(),
                        ProductPopulated.decimalField("SUM_Y").build())))
                .build());

        entityManager.flush();
        entityManager.clear();
    }

    @Test
    public void findAllByPipelineStepOrderByOrder() {
        Pipeline pipeline = savePipeline(pipeline()
                .productId(product.getId())
                .steps(newLinkedHashSet(asList(
                        ProductPopulated.windowPipelineStep()
                                .name("Window Step")
                                .schemaInId(table1.getId())
                                .schemaOutId(table2.getId())
                                .selects(emptySet())
                                .build(),
                        ProductPopulated.aggregatePipelineStep()
                                .name("Aggregation Step")
                                .schemaInId(table2.getId())
                                .schemaOutId(table3.getId())
                                .selects(emptySet())
                                .build())))
                .build());

        this.entityManager.flush();
        this.entityManager.clear();

        PipelineStep windowStep = findStepByName(pipeline, "Window Step");
        PipelineStep aggregationStep = findStepByName(pipeline, "Aggregation Step");

        Select x = selectRepository.saveAndFlush(ProductPopulated.select()
                .pipelineStep(windowStep)
                .select("A + B")
                .outputFieldId(findFieldInSchema(table2, "X").getId())
                .order(0L)
                .isIntermediateResult(true)
                .build());

        Select y = selectRepository.saveAndFlush(ProductPopulated.select()
                .pipelineStep(windowStep)
                .select("X * 2")
                .outputFieldId(findFieldInSchema(table2, "Y").getId())
                .order(1L)
                .isIntermediateResult(true)
                .build());

        Select z = selectRepository.saveAndFlush(ProductPopulated.select()
                .pipelineStep(windowStep)
                .select("rank()")
                .outputFieldId(findFieldInSchema(table2, "Z").getId())
                .order(2L)
                .isWindow(true)
                .window(Window.builder()
                        .partitions(newHashSet("C", "D"))
                        .orders(newHashSet(
                                Order.builder().fieldName("A").direction(Order.Direction.ASC).priority(0).build(),
                                Order.builder().fieldName("B").direction(Order.Direction.DESC).priority(1).build()))
                        .build())
                .build());

        Select sumX = selectRepository.saveAndFlush(ProductPopulated.select()
                .pipelineStep(aggregationStep)
                .select("sum(X)")
                .outputFieldId(findFieldInSchema(table3, "SUM_X").getId())
                .order(0L)
                .build());

        Select sumY = selectRepository.saveAndFlush(ProductPopulated.select()
                .pipelineStep(aggregationStep)
                .select("sum(Y)")
                .outputFieldId(findFieldInSchema(table3, "SUM_Y").getId())
                .order(1L)
                .build());

        List<Select> windowStepSelects = selectRepository.findAllByPipelineStepOrderByOrder(windowStep);
        List<Select> aggregationStepSelects = selectRepository.findAllByPipelineStepOrderByOrder(aggregationStep);

        soft.assertThat(windowStepSelects)
                .containsExactlyInAnyOrder(x, y, z);

        soft.assertThat(windowStepSelects)
                .extracting(Select::getPipelineStep)
                .containsOnly(windowStep);

        soft.assertThat(aggregationStepSelects)
                .containsExactlyInAnyOrder(sumX, sumY);

        soft.assertThat(aggregationStepSelects)
                .extracting(Select::getPipelineStep)
                .containsOnly(aggregationStep);
    }

    @Test
    public void findAllWithWindowByPipelineStep() {
        Pipeline pipeline = savePipeline(pipeline()
                .productId(product.getId())
                .steps(newLinkedHashSet(asList(
                        ProductPopulated.windowPipelineStep()
                                .name("First Window Step")
                                .schemaInId(table1.getId())
                                .schemaOutId(table2.getId())
                                .selects(emptySet())
                                .build(),
                        ProductPopulated.windowPipelineStep()
                                .name("Second Window Step")
                                .schemaInId(table2.getId())
                                .schemaOutId(table3.getId())
                                .selects(emptySet())
                                .build())))
                .build());

        this.entityManager.flush();
        this.entityManager.clear();

        PipelineStep step1 = findStepByName(pipeline, "First Window Step");
        PipelineStep step2 = findStepByName(pipeline, "Second Window Step");

        Select x = selectRepository.saveAndFlush(ProductPopulated.select()
                .pipelineStep(step1)
                .select("A")
                .outputFieldId(findFieldInSchema(table2, "X").getId())
                .isWindow(false)
                .build());

        Select y = selectRepository.saveAndFlush(ProductPopulated.select()
                .pipelineStep(step1)
                .select("rank()")
                .outputFieldId(findFieldInSchema(table2, "Z").getId())
                .isWindow(true)
                .window(Window.builder()
                        .partitions(newHashSet("A", "B"))
                        .orders(newHashSet(
                                Order.builder().fieldName("C").direction(Order.Direction.ASC).priority(0).build(),
                                Order.builder().fieldName("D").direction(Order.Direction.DESC).priority(1).build()))
                        .build())
                .build());

        Select z = selectRepository.saveAndFlush(ProductPopulated.select()
                .pipelineStep(step1)
                .select("rank()")
                .outputFieldId(findFieldInSchema(table2, "Z").getId())
                .isWindow(true)
                .window(Window.builder()
                        .partitions(newHashSet("C", "D"))
                        .orders(newHashSet(
                                Order.builder().fieldName("A").direction(Order.Direction.ASC).priority(0).build(),
                                Order.builder().fieldName("B").direction(Order.Direction.DESC).priority(1).build()))
                        .build())
                .build());

        Select sumX = selectRepository.saveAndFlush(ProductPopulated.select()
                .pipelineStep(step2)
                .select("X")
                .outputFieldId(findFieldInSchema(table3, "SUM_X").getId())
                .isWindow(false)
                .build());

        Select sumY = selectRepository.saveAndFlush(ProductPopulated.select()
                .pipelineStep(step2)
                .select("sum(Y)")
                .outputFieldId(findFieldInSchema(table3, "SUM_Y").getId())
                .isWindow(true)
                .window(Window.builder().partitions(newHashSet("A")).build())
                .build());

        List<Select> step1WindowSelects = selectRepository.findAllWithWindowByPipelineStep(step1);
        List<Select> step2WindowSelects = selectRepository.findAllWithWindowByPipelineStep(step2);

        soft.assertThat(step1WindowSelects)
                .containsExactlyInAnyOrder(y, z);

        soft.assertThat(step1WindowSelects)
                .extracting(Select::getPipelineStep)
                .containsOnly(step1);

        soft.assertThat(step2WindowSelects)
                .containsExactly(sumY);

        soft.assertThat(step2WindowSelects)
                .extracting(Select::getPipelineStep)
                .containsOnly(step2);
    }

    private Pipeline savePipeline(final Pipeline pipeline) {
        for (PipelineStep step : pipeline.getSteps()) {
            step.setPipeline(pipeline);
        }
        return pipelineRepository.save(pipeline);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private PipelineStep findStepByName(final Pipeline pipeline, final String stepName) {
        return pipeline.getSteps().stream()
                .filter(step -> step.getName().equals(stepName))
                .findFirst()
                .get();
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private static Field findFieldInSchema(final Table schema, final String fieldName) {
        return schema.getFields().stream()
                .filter(field -> field.getName().equals(fieldName))
                .findFirst()
                .get();
    }
}
