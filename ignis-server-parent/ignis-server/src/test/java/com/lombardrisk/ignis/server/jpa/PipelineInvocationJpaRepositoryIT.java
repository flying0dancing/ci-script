package com.lombardrisk.ignis.server.jpa;

import com.lombardrisk.ignis.server.config.IntegrationTestConfig;
import com.lombardrisk.ignis.server.dataset.DatasetJpaRepository;
import com.lombardrisk.ignis.server.dataset.fixture.DatasetPopulated;
import com.lombardrisk.ignis.server.dataset.model.Dataset;
import com.lombardrisk.ignis.server.dataset.pipeline.invocation.PipelineInvocationJpaRepository;
import com.lombardrisk.ignis.server.dataset.pipeline.invocation.model.PipelineInvocation;
import com.lombardrisk.ignis.server.dataset.pipeline.invocation.model.PipelineStepInvocation;
import com.lombardrisk.ignis.server.dataset.pipeline.invocation.model.PipelineStepStatus;
import com.lombardrisk.ignis.server.product.fixture.ProductPopulated;
import com.lombardrisk.ignis.server.product.pipeline.PipelineJpaRepository;
import com.lombardrisk.ignis.server.product.pipeline.PipelineService;
import com.lombardrisk.ignis.server.product.pipeline.model.Pipeline;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineAggregationStep;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineMapStep;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineStep;
import com.lombardrisk.ignis.server.product.pipeline.select.Select;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@RunWith(SpringRunner.class)
@IntegrationTestConfig
public class PipelineInvocationJpaRepositoryIT {

    @Autowired
    private PipelineJpaRepository pipelineRepository;

    @Autowired
    private PipelineInvocationJpaRepository pipelineInvocationRepository;

    @Autowired
    private TableRepository tableRepository;

    @Autowired
    private ProductConfigRepository productConfigRepository;

    @Autowired
    private DatasetJpaRepository datasetRepository;

    @Autowired
    private EntityManager entityManager;

    private PipelineService pipelineService;

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Before
    public void setUp() {
        pipelineService = new PipelineService(pipelineRepository);
    }

    @SuppressWarnings({ "OptionalGetWithoutIsPresent", "unchecked" })
    @Test
    public void createRetrieveUpdate() {
        ProductConfig product = productConfigRepository.save(ProductPopulated.productConfig()
                .build());

        Table schema1 = tableRepository.save(ProductPopulated.table()
                .displayName("table1")
                .physicalTableName("TB1")
                .validationRules(emptySet())
                .productId(product.getId())
                .fields(newLinkedHashSet(asList(
                        ProductPopulated.decimalField("A").build(),
                        ProductPopulated.decimalField("B").build())))
                .build());
        Table schema2 = tableRepository.save(ProductPopulated.table()
                .productId(product.getId())
                .displayName("table2")
                .physicalTableName("TB2")
                .validationRules(emptySet())
                .fields(newLinkedHashSet(asList(
                        ProductPopulated.decimalField("A").build(),
                        ProductPopulated.decimalField("B").build(),
                        ProductPopulated.decimalField("C").build())))
                .build());
        Table schema3 = tableRepository.save(ProductPopulated.table()
                .productId(product.getId())
                .displayName("table3")
                .physicalTableName("TB3")
                .validationRules(emptySet())
                .fields(singleton(ProductPopulated.decimalField("C").build()))
                .build());

        Pipeline createdPipeline = pipelineService.savePipeline(ProductPopulated.pipeline()
                .productId(product.getId())
                .name("test")
                .steps(newLinkedHashSet(asList(
                        ProductPopulated.mapPipelineStep()
                                .name("summingStep")
                                .description("This adds a and b to get c")
                                .schemaInId(schema1.getId()).schemaIn(null)
                                .schemaOutId(schema2.getId()).schemaOut(null)
                                .selects(newLinkedHashSet(asList(
                                        ProductPopulated.select()
                                                .select("A")
                                                .outputFieldId(findFieldInSchema(schema2, "A").getId())
                                                .build(),
                                        ProductPopulated.select()
                                                .select("B")
                                                .outputFieldId(findFieldInSchema(schema2, "B").getId())
                                                .build(),
                                        ProductPopulated.select()
                                                .select("A+B")
                                                .outputFieldId(findFieldInSchema(schema2, "C").getId())
                                                .build())))
                                .build(),
                        ProductPopulated.aggregatePipelineStep()
                                .name("average step")
                                .description("This averages c")
                                .schemaInId(schema2.getId()).schemaIn(null)
                                .schemaOutId(schema3.getId()).schemaOut(null)
                                .selects(singleton(ProductPopulated.select()
                                        .select("AVG(C)")
                                        .outputFieldId(findFieldInSchema(schema3, "C").getId())
                                        .build()))
                                .build())))
                .build());

        Iterator<PipelineStep> stepIterator = createdPipeline.getSteps().iterator();
        PipelineMapStep firstStep = (PipelineMapStep) stepIterator.next();
        PipelineAggregationStep secondStep = (PipelineAggregationStep) stepIterator.next();

        Dataset inputDataset = datasetRepository.save(DatasetPopulated.dataset()
                .schema(schema1)
                .build());

        PipelineInvocation createdInvocation = pipelineInvocationRepository.save(DatasetPopulated.pipelineInvocation()
                .name("Job ran on")
                .createdTime(LocalDateTime.of(2006, 2, 1, 12, 19, 11))
                .pipelineId(createdPipeline.getId()).entityCode("ENTITY")
                .referenceDate(LocalDate.of(2001, 1, 1))
                .steps(newLinkedHashSet(asList(
                        DatasetPopulated.pipelineStepInvocation()
                                .id(null)
                                .pipelineStepId(firstStep.getId())
                                .pipelineStep(null)
                                .inputDatasets(newHashSet(
                                        DatasetPopulated.pipelineStepInvocationDataset()
                                                .datasetId(inputDataset.getId())
                                                .datasetRunKey(987L)
                                                .build()))
                                .inputPipelineStepIds(emptySet())
                                .status(PipelineStepStatus.SUCCESS)
                                .build(),
                        DatasetPopulated.pipelineStepInvocation()
                                .id(null)
                                .pipelineStepId(secondStep.getId())
                                .pipelineStep(null)
                                .inputDatasets(emptySet())
                                .inputPipelineStepIds(singleton(firstStep.getId()))
                                .status(PipelineStepStatus.PENDING)
                                .build())))
                .build());

        soft.assertThat(createdInvocation.getName())
                .isEqualTo("Job ran on");
        soft.assertThat(createdInvocation.getPipelineId())
                .isEqualTo(createdPipeline.getId());
        soft.assertThat(createdInvocation.getEntityCode())
                .isEqualTo("ENTITY");
        soft.assertThat(createdInvocation.getReferenceDate())
                .isEqualTo(LocalDate.of(2001, 1, 1));
        soft.assertThat(createdInvocation.getCreatedTime())
                .isEqualTo(LocalDateTime.of(2006, 2, 1, 12, 19, 11));
        assertThat(createdInvocation.getSteps())
                .hasSize(2);

        assertThat(createdInvocation.getSteps())
                .extracting(
                        PipelineStepInvocation::getPipelineStepId,
                        PipelineStepInvocation::getStatus,
                        PipelineStepInvocation::getInputPipelineStepIds,
                        PipelineStepInvocation::getInputDatasets)
                .containsExactlyInAnyOrder(
                        tuple(
                                firstStep.getId(),
                                PipelineStepStatus.SUCCESS,
                                emptySet(),
                                singleton(DatasetPopulated.pipelineStepInvocationDataset()
                                        .datasetId(inputDataset.getId())
                                        .datasetRunKey(987L)
                                        .build())),
                        tuple(
                                secondStep.getId(),
                                PipelineStepStatus.PENDING,
                                singleton(firstStep.getId()),
                                emptySet()));

        //Test the insertable = false, updatable = false, @ManyToOne mapping
        entityManager.flush();
        entityManager.clear();

        PipelineInvocation retrievedInvocation = pipelineInvocationRepository.findById(createdInvocation.getId()).get();

        Iterator<PipelineStepInvocation> stepInvocationIterator = retrievedInvocation.getSteps().iterator();

        PipelineStepInvocation firstStepInvocation = stepInvocationIterator.next();

        PipelineMapStep firstStepInvocationStepDetails = (PipelineMapStep) firstStepInvocation.getPipelineStep();
        soft.assertThat(firstStepInvocationStepDetails.getId())
                .isEqualTo(firstStep.getId());
        soft.assertThat(firstStepInvocationStepDetails.getName())
                .isEqualTo(firstStep.getName());
        soft.assertThat(firstStepInvocationStepDetails.getDescription())
                .isEqualTo(firstStep.getDescription());
        soft.assertThat(firstStepInvocationStepDetails.getType())
                .isEqualTo(firstStep.getType());
        soft.assertThat(firstStepInvocationStepDetails.getSchemaIn().getId())
                .isEqualTo(firstStep.getSchemaInId());
        soft.assertThat(firstStepInvocationStepDetails.getSchemaOut().getId())
                .isEqualTo(firstStep.getSchemaOutId());
        soft.assertThat(firstStepInvocationStepDetails.getSelects())
                .extracting(Select::getSelect, Select::getOutputFieldId)
                .containsExactlyInAnyOrder(
                        tuple("A", findFieldInSchema(schema2, "A").getId()),
                        tuple("B", findFieldInSchema(schema2, "B").getId()),
                        tuple("A+B", findFieldInSchema(schema2, "C").getId()));

        PipelineStepInvocation secondStepInvocation = stepInvocationIterator.next();

        PipelineAggregationStep secondStepInvocationStepDetails =
                (PipelineAggregationStep) secondStepInvocation.getPipelineStep();
        soft.assertThat(secondStepInvocationStepDetails.getId())
                .isEqualTo(secondStep.getId());
        soft.assertThat(secondStepInvocationStepDetails.getName())
                .isEqualTo(secondStep.getName());
        soft.assertThat(secondStepInvocationStepDetails.getDescription())
                .isEqualTo(secondStep.getDescription());
        soft.assertThat(secondStepInvocationStepDetails.getType())
                .isEqualTo(secondStep.getType());
        soft.assertThat(secondStepInvocationStepDetails.getSchemaIn().getId())
                .isEqualTo(secondStep.getSchemaInId());
        soft.assertThat(secondStepInvocationStepDetails.getSchemaOut().getId())
                .isEqualTo(secondStep.getSchemaOutId());
        soft.assertThat(secondStepInvocationStepDetails.getSelects())
                .extracting(Select::getSelect, Select::getOutputFieldId)
                .containsExactlyInAnyOrder(
                        tuple("AVG(C)", findFieldInSchema(schema3, "C").getId()));
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    public void create_Retrieve_OutputDatasetIdSet() {
        ProductConfig product = productConfigRepository.save(ProductPopulated.productConfig()
                .build());

        Table schemaIn = tableRepository.save(ProductPopulated.table()
                .displayName("table1")
                .physicalTableName("TB1")
                .validationRules(emptySet())
                .productId(product.getId())
                .fields(newLinkedHashSet(asList(
                        ProductPopulated.decimalField("A").build(),
                        ProductPopulated.decimalField("B").build())))
                .build());
        Table schemaOut = tableRepository.save(ProductPopulated.table()
                .productId(product.getId())
                .displayName("table2")
                .physicalTableName("TB2")
                .validationRules(emptySet())
                .fields(newLinkedHashSet(asList(
                        ProductPopulated.decimalField("A").build(),
                        ProductPopulated.decimalField("B").build(),
                        ProductPopulated.decimalField("C").build())))
                .build());

        Pipeline createdPipeline = pipelineService.savePipeline(ProductPopulated.pipeline()
                .productId(product.getId())
                .name("test")
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
                                                .build(),
                                        ProductPopulated.select()
                                                .select("B")
                                                .outputFieldId(findFieldInSchema(schemaOut, "B").getId())
                                                .build(),
                                        ProductPopulated.select()
                                                .select("A+B")
                                                .outputFieldId(findFieldInSchema(schemaOut, "C").getId())
                                                .build())))
                                .build())))
                .build());

        PipelineMapStep createdStep = (PipelineMapStep) createdPipeline.getSteps().iterator().next();

        Dataset inputDataset = datasetRepository.save(DatasetPopulated.dataset()
                .schema(schemaIn)
                .runKey(1L)
                .build());

        PipelineInvocation createdInvocation = pipelineInvocationRepository.save(DatasetPopulated.pipelineInvocation()
                .name("Job ran on")
                .createdTime(LocalDateTime.of(2006, 2, 1, 12, 19, 11))
                .pipelineId(createdPipeline.getId()).entityCode("ENTITY")
                .referenceDate(LocalDate.of(2001, 1, 1))
                .steps(newHashSet(
                        DatasetPopulated.pipelineStepInvocation()
                                .id(null)
                                .pipelineStepId(createdStep.getId())
                                .pipelineStep(null)
                                .inputDatasets(newHashSet(DatasetPopulated.pipelineStepInvocationDataset()
                                        .datasetId(inputDataset.getId())
                                        .datasetRunKey(1L)
                                        .build()))
                                .build()
                ))
                .build());

        entityManager.flush();
        entityManager.clear();

        Dataset output = datasetRepository.saveAndFlush(DatasetPopulated.dataset()
                .schema(schemaIn)
                .pipelineInvocationId(createdInvocation.getId())
                .pipelineStepInvocationId(createdInvocation.getSteps().iterator().next().getId())
                .runKey(2L)
                .build());

        PipelineInvocation retrievedInvocation = pipelineInvocationRepository.findById(createdInvocation.getId())
                .get();

        PipelineStepInvocation retrievedStepInvocation = retrievedInvocation.getSteps().iterator().next();

        assertThat(retrievedStepInvocation.getOutputDatasetIds())
                .containsOnly(output.getId());

        retrievedStepInvocation.setStatus(PipelineStepStatus.RUNNING);

        PipelineInvocation updatedInvocation = pipelineInvocationRepository.save(retrievedInvocation);

        soft.assertThat(updatedInvocation.getSteps())
                .hasSize(1);

        soft.assertThat(updatedInvocation.getSteps().iterator().next().getStatus())
                .isEqualTo(PipelineStepStatus.RUNNING);
    }

    @Test
    public void findAllByServiceRequestId() {
        ProductConfig product = productConfigRepository.save(ProductPopulated.productConfig()
                .build());

        Table schemaIn = tableRepository.save(ProductPopulated.table()
                .displayName("table1")
                .physicalTableName("TB1")
                .validationRules(emptySet())
                .productId(product.getId())
                .fields(newLinkedHashSet(Arrays.asList(
                        ProductPopulated.decimalField("A").build(),
                        ProductPopulated.decimalField("B").build())))
                .build());

        Table schemaOut = tableRepository.save(ProductPopulated.table()
                .productId(product.getId())
                .displayName("table2")
                .physicalTableName("TB2")
                .validationRules(emptySet())
                .fields(newLinkedHashSet(Arrays.asList(
                        ProductPopulated.decimalField("A").build(),
                        ProductPopulated.decimalField("B").build(),
                        ProductPopulated.decimalField("C").build())))
                .build());

        Pipeline createdPipeline = pipelineService.savePipeline(ProductPopulated.pipeline()
                .productId(product.getId())
                .name("test")
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
                                                .build(),
                                        ProductPopulated.select()
                                                .select("B")
                                                .outputFieldId(findFieldInSchema(schemaOut, "B").getId())
                                                .build(),
                                        ProductPopulated.select()
                                                .select("A+B")
                                                .outputFieldId(findFieldInSchema(schemaOut, "C").getId())
                                                .build())))
                                .build())))
                .build());

        PipelineMapStep createdStep = (PipelineMapStep) createdPipeline.getSteps().iterator().next();

        PipelineInvocation savedInvocation1 = pipelineInvocationRepository.save(DatasetPopulated.pipelineInvocation()
                .id(null)
                .name("job 1 pipeline 1")
                .serviceRequestId(10001L)
                .pipelineId(createdPipeline.getId())
                .steps(singleton(
                        DatasetPopulated.pipelineStepInvocation()
                                .id(null)
                                .pipelineStep(null)
                                .pipelineStepId(createdStep.getId())
                                .inputDatasets(null)
                                .inputPipelineStepIds(null)
                                .build()))
                .build());

        PipelineInvocation savedInvocation2 = pipelineInvocationRepository.save(DatasetPopulated.pipelineInvocation()
                .id(null)
                .name("job 1 pipeline 2")
                .serviceRequestId(10001L)
                .pipelineId(createdPipeline.getId())
                .steps(singleton(
                        DatasetPopulated.pipelineStepInvocation()
                                .id(null)
                                .pipelineStep(null)
                                .pipelineStepId(createdStep.getId())
                                .inputDatasets(null)
                                .inputPipelineStepIds(null)
                                .build()))
                .build());

        PipelineInvocation savedInvocation3 = pipelineInvocationRepository.save(DatasetPopulated.pipelineInvocation()
                .id(null)
                .name("job 2 pipeline 1")
                .serviceRequestId(10002L)
                .pipelineId(createdPipeline.getId())
                .steps(singleton(
                        DatasetPopulated.pipelineStepInvocation()
                                .id(null)
                                .pipelineStep(null)
                                .pipelineStepId(createdStep.getId())
                                .inputDatasets(null)
                                .inputPipelineStepIds(null)
                                .build()))
                .build());

        List<PipelineInvocation> invocationsFoundForServiceRequest1 =
                pipelineInvocationRepository.findAllByServiceRequestId(10001L);

        List<PipelineInvocation> invocationsFoundForServiceRequest2 =
                pipelineInvocationRepository.findAllByServiceRequestId(10002L);

        soft.assertThat(invocationsFoundForServiceRequest1)
                .extracting(PipelineInvocation::getId)
                .containsExactlyInAnyOrder(savedInvocation1.getId(), savedInvocation2.getId());

        soft.assertThat(invocationsFoundForServiceRequest1)
                .extracting(PipelineInvocation::getServiceRequestId)
                .containsOnly(10001L);

        soft.assertThat(invocationsFoundForServiceRequest2)
                .extracting(PipelineInvocation::getId)
                .containsExactly(savedInvocation3.getId());

        soft.assertThat(invocationsFoundForServiceRequest2)
                .extracting(PipelineInvocation::getServiceRequestId)
                .containsOnly(10002L);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    private static Field findFieldInSchema(final Table schema, final String fieldName) {
        return schema.getFields().stream()
                .filter(field -> field.getName().equals(fieldName))
                .findFirst()
                .get();
    }
}
