package com.lombardrisk.ignis.server.dataset.pipeline.invocation;

import com.lombardrisk.ignis.client.external.pipeline.export.TransformationType;
import com.lombardrisk.ignis.client.external.pipeline.view.PipelineInvocationView;
import com.lombardrisk.ignis.client.external.pipeline.view.PipelineStepInvocationDatasetView;
import com.lombardrisk.ignis.client.external.pipeline.view.PipelineStepInvocationView;
import com.lombardrisk.ignis.client.external.pipeline.view.PipelineStepView;
import com.lombardrisk.ignis.client.external.pipeline.view.SchemaDetailsView;
import com.lombardrisk.ignis.common.assertions.VavrAssert;
import com.lombardrisk.ignis.common.time.TimeSource;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.server.dataset.fixture.DatasetPopulated;
import com.lombardrisk.ignis.server.dataset.pipeline.invocation.model.PipelineInvocation;
import com.lombardrisk.ignis.server.dataset.pipeline.invocation.model.PipelineStepInvocation;
import com.lombardrisk.ignis.server.dataset.pipeline.invocation.model.PipelineStepStatus;
import com.lombardrisk.ignis.server.product.fixture.ProductPopulated;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.validation.Validator;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PipelineInvocationServiceTest {

    @Mock
    private PipelineInvocationJpaRepository pipelineInvocationJpaRepository;

    @Mock
    private Validator validator;

    private PipelineInvocationService pipelineInvocationService;

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Before
    public void setUp() {
        TimeSource timeSource = new TimeSource(Clock.fixed(Instant.now(), ZoneId.of("UTC")));
        pipelineInvocationService = new PipelineInvocationService(timeSource, pipelineInvocationJpaRepository);
    }

    @Test
    public void findAllInvocation_ReturnsConvertedPipelineInvocationView() {
        when(pipelineInvocationJpaRepository.findAll())
                .thenReturn(singletonList(DatasetPopulated.pipelineInvocation()
                        .id(929L)
                        .name("Pipeline Job Name")
                        .createdTime(LocalDateTime.of(2001, 1, 1, 1, 1, 1))
                        .serviceRequestId(647346L)
                        .pipelineId(23L)
                        .referenceDate(LocalDate.of(1965, 3, 29))
                        .entityCode("High on a hill")
                        .steps(newHashSet(DatasetPopulated.pipelineStepInvocation()
                                .id(7272L)
                                .inputDatasets(singleton(
                                        DatasetPopulated.pipelineStepInvocationDataset()
                                                .datasetId(619123L)
                                                .datasetRunKey(9876L)
                                                .build()))
                                .inputPipelineStepIds(singleton(78291L))
                                .outputDatasetIds(singleton(88291L))
                                .status(PipelineStepStatus.SUCCESS)
                                .pipelineStep(ProductPopulated.mapPipelineStep()
                                        .id(6161L)
                                        .name("Was a lonely")
                                        .description("Goat herd")
                                        .schemaIn(ProductPopulated.schemaDetails()
                                                .id(88L)
                                                .displayName("Swiss Alps")
                                                .physicalTableName("S_ALPS")
                                                .version(1)
                                                .build())
                                        .schemaOut(ProductPopulated.schemaDetails()
                                                .id(88L)
                                                .displayName("German Alps")
                                                .physicalTableName("G_ALPS")
                                                .version(1)
                                                .build())
                                        .selects(newLinkedHashSet(asList(
                                                ProductPopulated.select()
                                                        .id(143214L)
                                                        .select("SOUND")
                                                        .outputFieldId(2343214L)
                                                        .build(),
                                                ProductPopulated.select()
                                                        .id(1345345L)
                                                        .select("OF")
                                                        .outputFieldId(135435L)
                                                        .build())))
                                        .filters(newHashSet("MUSIC = true"))
                                        .build())
                                .build()))
                        .build()));

        List<PipelineInvocationView> allInvocations = pipelineInvocationService.findAllInvocations();
        assertThat(allInvocations).hasSize(1);

        PipelineInvocationView pipelineInvocationView = allInvocations.get(0);
        soft.assertThat(pipelineInvocationView.getId())
                .isEqualTo(929L);
        soft.assertThat(pipelineInvocationView.getName())
                .isEqualTo("Pipeline Job Name");
        soft.assertThat(pipelineInvocationView.getCreatedTime())
                .isEqualTo(ZonedDateTime.of(
                        LocalDateTime.of(2001, 1, 1, 1, 1, 1),
                        ZoneId.of("UTC")));
        soft.assertThat(pipelineInvocationView.getServiceRequestId())
                .isEqualTo(647346L);
        soft.assertThat(pipelineInvocationView.getPipelineId())
                .isEqualTo(23L);
        soft.assertThat(pipelineInvocationView.getEntityCode())
                .isEqualTo("High on a hill");
        soft.assertThat(pipelineInvocationView.getReferenceDate())
                .isEqualTo(LocalDate.of(1965, 3, 29));

        assertThat(pipelineInvocationView.getInvocationSteps())
                .hasSize(1);

        PipelineStepInvocationView pipelineStepInvocationView =
                pipelineInvocationView.getInvocationSteps().get(0);

        soft.assertThat(pipelineStepInvocationView.getId())
                .isEqualTo(7272);
        soft.assertThat(pipelineStepInvocationView.getDatasetsIn())
                .containsOnly(PipelineStepInvocationDatasetView.builder()
                        .datasetId(619123L)
                        .datasetRunKey(9876L)
                        .build());
        soft.assertThat(pipelineStepInvocationView.getDatasetOutId())
                .isEqualTo(88291L);
        soft.assertThat(pipelineStepInvocationView.getInputPipelineStepIds())
                .containsOnly(78291L);
        soft.assertThat(pipelineStepInvocationView.getStatus())
                .isEqualTo(com.lombardrisk.ignis.client.external.pipeline.view.PipelineStepStatus.SUCCESS);

        PipelineStepView pipelineStepView = pipelineStepInvocationView.getPipelineStep();
        soft.assertThat(pipelineStepView.getId())
                .isEqualTo(6161);
        soft.assertThat(pipelineStepView.getName())
                .isEqualTo("Was a lonely");
        soft.assertThat(pipelineStepView.getDescription())
                .isEqualTo("Goat herd");
        soft.assertThat(pipelineStepView.getSchemaIn())
                .isEqualTo(SchemaDetailsView.builder()
                        .id(88L)
                        .displayName("Swiss Alps")
                        .physicalTableName("S_ALPS")
                        .version(1)
                        .build());
        soft.assertThat(pipelineStepView.getSchemaOut())
                .isEqualTo(SchemaDetailsView.builder()
                        .id(88L)
                        .displayName("German Alps")
                        .physicalTableName("G_ALPS")
                        .version(1)
                        .build());
        soft.assertThat(pipelineStepView.getType())
                .isEqualTo(TransformationType.MAP);
    }

    @Test
    public void findAllInvocations_AggregationStep_ReturnsConvertedPipelineStepView() {
        when(pipelineInvocationJpaRepository.findAll())
                .thenReturn(singletonList(DatasetPopulated.pipelineInvocation()
                        .steps(newHashSet(DatasetPopulated.pipelineStepInvocation()
                                .pipelineStep(ProductPopulated.aggregatePipelineStep()
                                        .schemaIn(ProductPopulated.schemaDetails()
                                                .id(88L)
                                                .displayName("Swiss Alps")
                                                .physicalTableName("S_ALPS")
                                                .version(1)
                                                .build())
                                        .schemaOut(ProductPopulated.schemaDetails()
                                                .id(88L)
                                                .displayName("German Alps")
                                                .physicalTableName("G_ALPS")
                                                .version(1)
                                                .build())
                                        .selects(newLinkedHashSet(asList(
                                                ProductPopulated.select()
                                                        .id(143214L)
                                                        .select("SOUND")
                                                        .outputFieldId(2343214L)
                                                        .build(),
                                                ProductPopulated.select()
                                                        .id(1345345L)
                                                        .select("OF")
                                                        .outputFieldId(135435L)
                                                        .build())))
                                        .filters(newHashSet("MUSIC = true"))
                                        .groupings(newHashSet("SOUND"))
                                        .build())
                                .build()))
                        .build()));

        List<PipelineInvocationView> allInvocations = pipelineInvocationService.findAllInvocations();
        assertThat(allInvocations).hasSize(1);

        PipelineInvocationView pipelineInvocationView = allInvocations.get(0);
        PipelineStepInvocationView pipelineStepInvocationView =
                pipelineInvocationView.getInvocationSteps().get(0);

        PipelineStepView pipelineStepView = pipelineStepInvocationView.getPipelineStep();
        soft.assertThat(pipelineStepView.getSchemaIn())
                .isEqualTo(SchemaDetailsView.builder()
                        .id(88L)
                        .displayName("Swiss Alps")
                        .physicalTableName("S_ALPS")
                        .version(1)
                        .build());
        soft.assertThat(pipelineStepView.getSchemaOut())
                .isEqualTo(SchemaDetailsView.builder()
                        .id(88L)
                        .displayName("German Alps")
                        .physicalTableName("G_ALPS")
                        .version(1)
                        .build());
        soft.assertThat(pipelineStepView.getGroupings())
                .contains("SOUND");
        soft.assertThat(pipelineStepView.getType())
                .isEqualTo(TransformationType.AGGREGATION);
    }

    @Test
    public void findByInvocationIdAndStepInvocationId_PipelineInvocationWithStepInvocation_ReturnsStepInvocation() {
        PipelineStepInvocation stepInvocation1 = DatasetPopulated.pipelineStepInvocation().id(1L).build();
        PipelineStepInvocation stepInvocation2 = DatasetPopulated.pipelineStepInvocation().id(2L).build();
        PipelineStepInvocation stepInvocation3 = DatasetPopulated.pipelineStepInvocation().id(3L).build();

        when(pipelineInvocationJpaRepository.findById(any()))
                .thenReturn(Optional.of(DatasetPopulated.pipelineInvocation()
                        .steps(newHashSet(stepInvocation1, stepInvocation2, stepInvocation3))
                        .build()));

        PipelineStepInvocation foundStepInvocation =
                VavrAssert.assertValid(pipelineInvocationService.findByInvocationIdAndStepInvocationId(123L, 2L))
                        .getResult();

        assertThat(foundStepInvocation).isSameAs(stepInvocation2);
    }

    @Test
    public void findByInvocationIdAndStepInvocationId_PipelineInvocationNotFound_ReturnsError() {
        when(pipelineInvocationJpaRepository.findById(any()))
                .thenReturn(Optional.empty());

        VavrAssert.assertFailed(pipelineInvocationService.findByInvocationIdAndStepInvocationId(123L, 2L))
                .withFailure(CRUDFailure.notFoundIds("PipelineInvocation", 123L));
    }

    @Test
    public void findByInvocationIdAndStepInvocationId_PipelineStepInvocationNotFound_ReturnsError() {
        PipelineStepInvocation stepInvocation1 = DatasetPopulated.pipelineStepInvocation().id(1L).build();
        PipelineStepInvocation stepInvocation3 = DatasetPopulated.pipelineStepInvocation().id(3L).build();

        when(pipelineInvocationJpaRepository.findById(any()))
                .thenReturn(Optional.of(DatasetPopulated.pipelineInvocation()
                        .steps(newHashSet(stepInvocation1, stepInvocation3))
                        .build()));

        VavrAssert.assertFailed(pipelineInvocationService.findByInvocationIdAndStepInvocationId(123L, 2L))
                .withFailure(CRUDFailure.notFoundIds("PipelineStepInvocation", 2L));
    }

    @Test
    public void findInvocationsByServiceRequestId_PipelineInvocationsFound_ReturnsInvocations() {
        when(pipelineInvocationJpaRepository.findAllByServiceRequestId(anyLong()))
                .thenReturn(singletonList(DatasetPopulated.pipelineInvocation()
                        .id(929L)
                        .name("Pipeline Job Name")
                        .createdTime(LocalDateTime.of(2001, 1, 1, 1, 1, 1))
                        .serviceRequestId(4666L)
                        .pipelineId(23L)
                        .referenceDate(LocalDate.of(1965, 3, 29))
                        .entityCode("High on a hill")
                        .steps(newHashSet(DatasetPopulated.pipelineStepInvocation()
                                .id(7272L)
                                .inputDatasets(singleton(
                                        DatasetPopulated.pipelineStepInvocationDataset()
                                                .datasetId(619123L)
                                                .datasetRunKey(9876L)
                                                .build()))
                                .inputPipelineStepIds(singleton(78291L))
                                .status(PipelineStepStatus.RUNNING)
                                .pipelineStep(ProductPopulated.mapPipelineStep()
                                        .id(6161L)
                                        .name("Was a lonely")
                                        .description("Goat herd")
                                        .schemaIn(ProductPopulated.schemaDetails()
                                                .id(88L)
                                                .displayName("Swiss Alps")
                                                .physicalTableName("S_ALPS")
                                                .version(1)
                                                .build())
                                        .schemaOut(ProductPopulated.schemaDetails()
                                                .id(88L)
                                                .displayName("German Alps")
                                                .physicalTableName("G_ALPS")
                                                .version(1)
                                                .build())
                                        .selects(newLinkedHashSet(asList(
                                                ProductPopulated.select()
                                                        .id(143214L).select("SOUND").outputFieldId(2343214L).build(),
                                                ProductPopulated.select()
                                                        .id(1345345L).select("OF").outputFieldId(135435L).build())))
                                        .build())
                                .build()))
                        .build()));

        List<PipelineInvocationView> allInvocations =
                pipelineInvocationService.findInvocationsByServiceRequestId(1234L);

        assertThat(allInvocations).hasSize(1);

        PipelineInvocationView pipelineInvocationView = allInvocations.get(0);
        soft.assertThat(pipelineInvocationView.getId())
                .isEqualTo(929L);
        soft.assertThat(pipelineInvocationView.getName())
                .isEqualTo("Pipeline Job Name");
        soft.assertThat(pipelineInvocationView.getCreatedTime())
                .isEqualTo(ZonedDateTime.of(
                        LocalDateTime.of(2001, 1, 1, 1, 1, 1),
                        ZoneId.of("UTC")));
        soft.assertThat(pipelineInvocationView.getServiceRequestId())
                .isEqualTo(4666L);
        soft.assertThat(pipelineInvocationView.getPipelineId())
                .isEqualTo(23L);
        soft.assertThat(pipelineInvocationView.getEntityCode())
                .isEqualTo("High on a hill");
        soft.assertThat(pipelineInvocationView.getReferenceDate())
                .isEqualTo(LocalDate.of(1965, 3, 29));

        assertThat(pipelineInvocationView.getInvocationSteps())
                .hasSize(1);

        PipelineStepInvocationView pipelineStepInvocationView =
                pipelineInvocationView.getInvocationSteps().get(0);

        soft.assertThat(pipelineStepInvocationView.getId())
                .isEqualTo(7272);
        soft.assertThat(pipelineStepInvocationView.getDatasetsIn())
                .containsOnly(PipelineStepInvocationDatasetView.builder()
                        .datasetId(619123L)
                        .datasetRunKey(9876L)
                        .build());
        soft.assertThat(pipelineStepInvocationView.getInputPipelineStepIds())
                .containsOnly(78291L);
        soft.assertThat(pipelineStepInvocationView.getStatus())
                .isEqualTo(com.lombardrisk.ignis.client.external.pipeline.view.PipelineStepStatus.RUNNING);

        PipelineStepView pipelineStepView = pipelineStepInvocationView.getPipelineStep();
        soft.assertThat(pipelineStepView.getId())
                .isEqualTo(6161);
        soft.assertThat(pipelineStepView.getName())
                .isEqualTo("Was a lonely");
        soft.assertThat(pipelineStepView.getDescription())
                .isEqualTo("Goat herd");
        soft.assertThat(pipelineStepView.getSchemaIn())
                .isEqualTo(SchemaDetailsView.builder()
                        .id(88L)
                        .displayName("Swiss Alps")
                        .physicalTableName("S_ALPS")
                        .version(1)
                        .build());
        soft.assertThat(pipelineStepView.getSchemaOut())
                .isEqualTo(SchemaDetailsView.builder()
                        .id(88L)
                        .displayName("German Alps")
                        .physicalTableName("G_ALPS")
                        .version(1)
                        .build());
        soft.assertThat(pipelineStepView.getType())
                .isEqualTo(TransformationType.MAP);
    }

    @Test
    public void findInvocationsByServiceRequestId_CallsRepositoryWithServiceRequestId() {
        when(pipelineInvocationJpaRepository.findAllByServiceRequestId(anyLong()))
                .thenReturn(singletonList(DatasetPopulated.pipelineInvocation().build()));

        pipelineInvocationService.findInvocationsByServiceRequestId(1234L);

        verify(pipelineInvocationJpaRepository).findAllByServiceRequestId(1234L);
    }

    @Test
    public void updateStepInvocationStatus_PipelineInvocationNotFound_ReturnsError() {
        when(pipelineInvocationJpaRepository.findById(any()))
                .thenReturn(Optional.empty());

        VavrAssert.assertFailed(pipelineInvocationService.updateStepInvocationStatus(12345L, 765L, "SUCCESS"))
                .withFailure(CRUDFailure.notFoundIds("PipelineInvocation", 12345L));
    }

    @Test
    public void updateStepInvocationStatus_PipelineStepInvocationNotFound_ReturnsError() {
        PipelineStepInvocation stepInvocation1 = DatasetPopulated.pipelineStepInvocation().id(1L).build();
        PipelineStepInvocation stepInvocation3 = DatasetPopulated.pipelineStepInvocation().id(3L).build();

        when(pipelineInvocationJpaRepository.findById(any()))
                .thenReturn(Optional.of(DatasetPopulated.pipelineInvocation()
                        .id(12345L)
                        .steps(newHashSet(stepInvocation1, stepInvocation3))
                        .build()));

        VavrAssert.assertFailed(pipelineInvocationService.updateStepInvocationStatus(12345L, 2L, "SUCCESS"))
                .withFailure(CRUDFailure.notFoundIds("PipelineStepInvocation", 2L));
    }

    @Test
    public void updateStepInvocationStatus_InvalidPipelineStepStatus_ReturnsError() {
        VavrAssert.assertFailed(pipelineInvocationService.updateStepInvocationStatus(12345L, 2L, "invalid step status"))
                .withFailure(CRUDFailure.invalidRequestParameter("PipelineStepStatus", "invalid step status"));
    }

    @Test
    public void updateStepInvocationStatus_ValidPipelineInvocation_ReturnsUpdatedStepInvocation() {
        PipelineStepInvocation stepInvocation1 = DatasetPopulated.pipelineStepInvocation()
                .id(1L)
                .status(PipelineStepStatus.SUCCESS)
                .build();
        PipelineStepInvocation stepInvocation2 = DatasetPopulated.pipelineStepInvocation()
                .id(2L)
                .status(PipelineStepStatus.RUNNING)
                .build();
        PipelineStepInvocation stepInvocation3 = DatasetPopulated.pipelineStepInvocation()
                .id(3L)
                .status(PipelineStepStatus.PENDING)
                .build();

        when(pipelineInvocationJpaRepository.findById(any()))
                .thenReturn(Optional.of(DatasetPopulated.pipelineInvocation()
                        .id(12345L)
                        .steps(newHashSet(stepInvocation1, stepInvocation2, stepInvocation3))
                        .build()));

        when(pipelineInvocationJpaRepository.save(any()))
                .thenReturn(DatasetPopulated.pipelineInvocation()
                        .id(12345L)
                        .steps(newHashSet(
                                stepInvocation1,
                                stepInvocation3,
                                PipelineStepInvocation.builder().id(2L).status(PipelineStepStatus.SUCCESS).build()))
                        .build());

        PipelineInvocation updatedInvocation =
                VavrAssert.assertValid(pipelineInvocationService.updateStepInvocationStatus(12345L, 2L, "SUCCESS"))
                        .getResult();

        assertThat(updatedInvocation.getId())
                .isEqualTo(12345L);

        assertThat(updatedInvocation.getSteps())
                .extracting(PipelineStepInvocation::getId, PipelineStepInvocation::getStatus)
                .containsExactlyInAnyOrder(
                        tuple(1L, PipelineStepStatus.SUCCESS),
                        tuple(2L, PipelineStepStatus.SUCCESS),
                        tuple(3L, PipelineStepStatus.PENDING));
    }

    @Test
    public void updateStepInvocationStatus_ValidPipelineInvocation_SavesUpdatedStepInvocation() {
        PipelineStepInvocation stepInvocation1 = DatasetPopulated.pipelineStepInvocation()
                .id(1L)
                .status(PipelineStepStatus.SUCCESS)
                .build();
        PipelineStepInvocation stepInvocation2 = DatasetPopulated.pipelineStepInvocation()
                .id(2L)
                .status(PipelineStepStatus.RUNNING)
                .build();
        PipelineStepInvocation stepInvocation3 = DatasetPopulated.pipelineStepInvocation()
                .id(3L)
                .status(PipelineStepStatus.PENDING)
                .build();

        when(pipelineInvocationJpaRepository.findById(any()))
                .thenReturn(Optional.of(DatasetPopulated.pipelineInvocation()
                        .id(12345L)
                        .steps(newHashSet(stepInvocation1, stepInvocation2, stepInvocation3))
                        .build()));

        when(pipelineInvocationJpaRepository.save(any()))
                .thenReturn(DatasetPopulated.pipelineInvocation()
                        .id(12345L)
                        .steps(newHashSet(
                                stepInvocation1,
                                stepInvocation3,
                                PipelineStepInvocation.builder().id(2L).status(PipelineStepStatus.SUCCESS).build()))
                        .build());

        VavrAssert.assertValid(pipelineInvocationService.updateStepInvocationStatus(12345L, 2L, "SUCCESS"));

        ArgumentCaptor<PipelineInvocation> captor = ArgumentCaptor.forClass(PipelineInvocation.class);

        verify(pipelineInvocationJpaRepository).save(captor.capture());

        PipelineInvocation updatedInvocation = captor.getValue();

        assertThat(updatedInvocation.getId())
                .isEqualTo(12345L);

        assertThat(updatedInvocation.getSteps())
                .extracting(PipelineStepInvocation::getId, PipelineStepInvocation::getStatus)
                .containsExactlyInAnyOrder(
                        tuple(1L, PipelineStepStatus.SUCCESS),
                        tuple(2L, PipelineStepStatus.SUCCESS),
                        tuple(3L, PipelineStepStatus.PENDING));
    }
}
