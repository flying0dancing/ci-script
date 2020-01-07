package com.lombardrisk.ignis.server.dataset;

import com.lombardrisk.ignis.api.dataset.DatasetType;
import com.lombardrisk.ignis.api.dataset.ValidationStatus;
import com.lombardrisk.ignis.client.internal.UpdateDatasetRunCall;
import com.lombardrisk.ignis.common.assertions.VavrAssert;
import com.lombardrisk.ignis.common.time.TimeSource;
import com.lombardrisk.ignis.data.common.Identifiable;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure.Type;
import com.lombardrisk.ignis.feature.IgnisFeature;
import com.lombardrisk.ignis.server.dataset.fixture.DatasetPopulated;
import com.lombardrisk.ignis.server.dataset.model.Dataset;
import com.lombardrisk.ignis.server.dataset.model.DatasetOnly;
import com.lombardrisk.ignis.server.dataset.model.DatasetOnlyBean;
import com.lombardrisk.ignis.server.dataset.model.DatasetQuery;
import com.lombardrisk.ignis.server.dataset.model.DatasetServiceRequest;
import com.lombardrisk.ignis.server.product.fixture.ProductPopulated;
import com.lombardrisk.ignis.server.product.table.TableRepository;
import com.lombardrisk.ignis.server.product.table.model.Table;
import com.lombardrisk.ignis.spark.api.fixture.Populated;
import com.lombardrisk.ignis.spark.api.staging.DatasetProperties;
import io.vavr.control.Validation;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.togglz.junit.TogglzRule;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.google.common.collect.Sets.newHashSet;
import static com.lombardrisk.ignis.api.dataset.ValidationStatus.NOT_VALIDATED;
import static com.lombardrisk.ignis.api.dataset.ValidationStatus.QUEUED;
import static com.lombardrisk.ignis.api.dataset.ValidationStatus.VALIDATED;
import static com.lombardrisk.ignis.common.fixtures.PopulatedDates.toDate;
import static com.lombardrisk.ignis.common.json.MapperWrapper.MAPPER;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DatasetServiceTest {

    @Mock
    private DatasetJpaRepository datasetRepository;
    @Mock
    private TableRepository tableRepository;
    @Mock
    private TimeSource timeSource;

    @InjectMocks
    private DatasetService datasetService;

    @Captor
    private ArgumentCaptor<Dataset> datasetCaptor;
    @Captor
    private ArgumentCaptor<Table> schemaCaptor;

    @Rule
    public TogglzRule togglzRule = TogglzRule.allDisabled(IgnisFeature.class);

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Before
    public void setUp() {
        when(tableRepository.findById(any()))
                .thenReturn(Optional.of(ProductPopulated.table().build()));

        when(tableRepository.save(any()))
                .thenReturn(ProductPopulated.table().build());

        when(datasetRepository.save(any()))
                .thenReturn(DatasetPopulated.dataset().build());

        when(datasetRepository.findLatestDataset(any(), any(), any()))
                .thenReturn(Optional.empty());
    }

    @Test
    public void createDataset_ReturnsIdentifiable() {
        when(datasetRepository.save(any()))
                .thenReturn(DatasetPopulated.dataset().id(123L).build());

        Validation<CRUDFailure, Identifiable> datasetValidation =
                datasetService.createDataset(DatasetPopulated.createDatasetCall().build());

        assertThat(datasetValidation.get().getId())
                .isEqualTo(123L);
    }

    @Test
    public void createDataset_MissingSchema_ReturnsFailure() {
        when(tableRepository.findById(any()))
                .thenReturn(Optional.empty());

        Validation<CRUDFailure, Identifiable> datasetValidation =
                datasetService.createDataset(DatasetPopulated.createDatasetCall().schemaId(223L).build());

        assertThat(datasetValidation.getError())
                .matches(failure -> failure.getErrorCode().equals(Type.NOT_FOUND.name()))
                .matches(failure -> failure.getErrorMessage().contains("223"))
                .matches(failure -> failure.getErrorMessage().contains("schema"));
    }

    @Test
    public void createDataset_DatasetWithStagingDataset_SavesDataset() {
        when(datasetRepository.save(any()))
                .thenReturn(DatasetPopulated.dataset().id(9876L).build());

        when(tableRepository.findById(22234L))
                .thenReturn(Optional.of(
                        ProductPopulated.table().id(22234L).physicalTableName("S_1").build()));

        when(timeSource.nowAsDate())
                .thenReturn(toDate("1980-03-02"));

        datasetService.createDataset(
                DatasetPopulated.createDatasetCall()
                        .stagingDatasetId(4001L)
                        .entityCode("E1")
                        .referenceDate(LocalDate.of(2001, 3, 2))
                        .schemaId(22234L)
                        .stagingJobId(443L)
                        .pipelineJobId(72727L)
                        .pipelineInvocationId(829239L)
                        .predicate("2 > 1")
                        .rowKeySeed(3413L)
                        .recordsCount(331)
                        .autoValidate(true)
                        .build());

        verify(datasetRepository).save(datasetCaptor.capture());

        Dataset dataset = datasetCaptor.getValue();

        soft.assertThat(dataset.getEntityCode())
                .isEqualTo("E1");
        soft.assertThat(dataset.getReferenceDate())
                .isEqualTo(LocalDate.of(2001, 3, 2));
        soft.assertThat(dataset.getPredicate())
                .isEqualTo("2 > 1");
        soft.assertThat(dataset.getRowKeySeed())
                .isEqualTo(3413L);
        soft.assertThat(dataset.getRecordsCount())
                .isEqualTo(331);
        soft.assertThat(dataset.getValidationStatus())
                .isEqualTo(ValidationStatus.QUEUED);
        soft.assertThat(dataset.getSchema().getId())
                .isEqualTo(22234L);
        soft.assertThat(dataset.getTable())
                .isEqualTo("S_1");
        soft.assertThat(dataset.getName())
                .isEqualTo("S_1");
        soft.assertThat(dataset.getPipelineJobId())
                .isEqualTo(72727L);
        soft.assertThat(dataset.getPipelineInvocationId())
                .isEqualTo(829239L);
        soft.assertThat(dataset.getCreatedTime())
                .isEqualTo(toDate("1980-03-02"));
        soft.assertThat(dataset.getDatasetType())
                .isEqualTo(DatasetType.STAGING_DATASET);
        soft.assertThat(dataset.getRunKey())
                .isEqualTo(1L);
        soft.assertThat(dataset.getLastUpdated())
                .isEqualTo(toDate("1980-03-02"));
        soft.assertThat(dataset.getStagingJobs())
                .containsExactly(DatasetServiceRequest.builder()
                        .id(4001L)
                        .serviceRequestId(443L)
                        .build());
    }

    @Test
    public void createDataset_DatasetWithoutStagingDataset_SavesDataset() {
        when(datasetRepository.save(any()))
                .thenReturn(DatasetPopulated.dataset().id(9876L).build());

        when(tableRepository.findById(22234L))
                .thenReturn(Optional.of(
                        ProductPopulated.table().id(22234L).physicalTableName("S_1").build()));

        when(timeSource.nowAsDate())
                .thenReturn(toDate("1980-03-02"));

        datasetService.createDataset(
                DatasetPopulated.createDatasetCall()
                        .stagingDatasetId(null)
                        .entityCode("E1")
                        .referenceDate(LocalDate.of(2001, 3, 2))
                        .schemaId(22234L)
                        .stagingJobId(443L)
                        .pipelineJobId(72727L)
                        .pipelineInvocationId(829239L)
                        .predicate("2 > 1")
                        .rowKeySeed(3413L)
                        .recordsCount(331)
                        .autoValidate(true)
                        .build());

        verify(datasetRepository).save(datasetCaptor.capture());

        Dataset dataset = datasetCaptor.getValue();

        soft.assertThat(dataset.getStagingJobs())
                .isEmpty();
    }

    @Test
    public void createDataset_DatasetWithNameEntityCodeReferenceDateAlreadyExists_CreatesDatasetWithRunKey() {
        Table schema = ProductPopulated.table().id(22234L).physicalTableName("BATMAN").build();

        Dataset previousDatasetRun = DatasetPopulated.dataset()
                .id(123L).name("BATMAN").entityCode("I'M BATMAN").referenceDate(LocalDate.of(1939, 5, 1)).runKey(998L)
                .build();

        when(tableRepository.findById(any()))
                .thenReturn(Optional.of(schema));

        when(datasetRepository.findLatestDataset("BATMAN", "I'M BATMAN", LocalDate.of(1939, 5, 1)))
                .thenReturn(Optional.of(previousDatasetRun));

        when(timeSource.nowAsDate())
                .thenReturn(toDate("1982-03-02"));

        datasetService.createDataset(
                DatasetPopulated.createDatasetCall()
                        .stagingDatasetId(12341234L)
                        .entityCode("I'M BATMAN")
                        .referenceDate(LocalDate.of(1939, 5, 1))
                        .schemaId(22234L)
                        .stagingJobId(443L)
                        .predicate("2 > 1")
                        .rowKeySeed(3413L)
                        .recordsCount(331)
                        .autoValidate(true)
                        .build());

        verify(datasetRepository).save(datasetCaptor.capture());

        Dataset savedDataset = datasetCaptor.getValue();

        soft.assertThat(savedDataset.getId()).isNull();
        soft.assertThat(savedDataset.getRunKey()).isEqualTo(999L);
        soft.assertThat(savedDataset.getTable()).isEqualTo("BATMAN");
        soft.assertThat(savedDataset.getName()).isEqualTo("BATMAN");
        soft.assertThat(savedDataset.getSchema()).isEqualTo(schema);
        soft.assertThat(savedDataset.getDatasetType()).isEqualTo(DatasetType.STAGING_DATASET);
        soft.assertThat(savedDataset.getCreatedTime()).isEqualTo(toDate("1982-03-02"));
        soft.assertThat(savedDataset.getLastUpdated()).isEqualTo(toDate("1982-03-02"));
        soft.assertThat(savedDataset.getRecordsCount()).isEqualTo(331);
        soft.assertThat(savedDataset.getRowKeySeed()).isEqualTo(3413L);
        soft.assertThat(savedDataset.getPredicate()).isEqualTo("2 > 1");
        soft.assertThat(savedDataset.getValidationStatus()).isEqualTo(QUEUED);
        soft.assertThat(savedDataset.getEntityCode()).isEqualTo("I'M BATMAN");
        soft.assertThat(savedDataset.getReferenceDate()).isEqualTo(LocalDate.of(1939, 5, 1));
        soft.assertThat(savedDataset.getStagingJobs())
                .containsExactly(DatasetServiceRequest.builder()
                        .id(12341234L)
                        .serviceRequestId(443L)
                        .build());
    }

    @Test
    public void createDataset_WithoutAutoValidate_CreatesDatasetWithNotValidatedStatus() {
        datasetService.createDataset(
                DatasetPopulated.createDatasetCall()
                        .autoValidate(false)
                        .build());

        verify(datasetRepository).save(datasetCaptor.capture());

        assertThat(datasetCaptor.getValue().getValidationStatus())
                .isEqualTo(ValidationStatus.NOT_VALIDATED);
    }

    @Test
    public void createDataset_SavesSchemaWithDatasetFlag() {
        when(tableRepository.findById(any()))
                .thenReturn(Optional.of(
                        ProductPopulated.table().id(0L).hasDatasets(false).build()));

        datasetService.createDataset(DatasetPopulated.createDatasetCall().build());

        verify(tableRepository).save(schemaCaptor.capture());

        assertThat(schemaCaptor.getValue().getHasDatasets())
                .isTrue();
    }

    @Test
    public void findLatestDataset_NotFound_ReturnsCrudFailure() throws Exception {
        when(datasetRepository.findLatestDataset(anyString(), anyString(), any()))
                .thenReturn(Optional.empty());

        DatasetProperties datasetMetadata = Populated.datasetProperties().build();
        assertThat(datasetService.findLatestDataset("dataset schema", datasetMetadata).getError())
                .isEqualTo(CRUDFailure.notFoundIndices(
                        "dataset",
                        "Dataset",
                        singletonList(String.format("schema: %s, metadata: %s",
                                "dataset schema", MAPPER.writeValueAsString(datasetMetadata)))));
    }

    @Test
    public void findLatestDataset_Found_ReturnsDataset() {
        Dataset dataset = DatasetPopulated.dataset().id(23L).build();

        when(datasetRepository.findLatestDataset(anyString(), anyString(), any()))
                .thenReturn(Optional.of(dataset));

        DatasetProperties datasetMetadata = Populated.datasetProperties().build();
        assertThat(datasetService.findLatestDataset("dataset schema", datasetMetadata).get())
                .isEqualTo(dataset);
    }

    @Test
    public void findAllDatasets_CallsDatasetRepository() {
        DatasetOnlyBean dataset = DatasetPopulated.datasetOnly().build();

        when(datasetRepository.findAllDatasetsOnly(any(Pageable.class)))
                .thenReturn(new PageImpl<>(singletonList(dataset)));

        PageRequest datasetPageRequest = PageRequest.of(0, 1);
        assertThat(datasetService.findAllDatasets(datasetPageRequest).getContent())
                .containsExactly(dataset);

        verify(datasetRepository).findAllDatasetsOnly(datasetPageRequest);
    }

    @Test
    public void findAllDatasets_QueryByDatasetName_CallsDatasetRepository() {
        PageRequest datasetPageRequest = PageRequest.of(0, 1);

        LocalDate referenceDate = LocalDate.of(2000, 1, 1);

        DatasetQuery datasetQuery = DatasetPopulated.datasetQuery()
                .name("DatasetName")
                .entityCode("EntityCode")
                .referenceDate(referenceDate)
                .build();

        datasetService.findAllDatasets(datasetQuery, datasetPageRequest);

        verify(datasetRepository).findAllDatasetsOnlyByName(
                "DatasetName", "EntityCode", referenceDate, datasetPageRequest);
    }

    @Test
    public void findAllDatasets_QueryByDatasetName_ReturnsDatasetFromRepository() {
        PageRequest datasetPageRequest = PageRequest.of(0, 1);

        DatasetQuery datasetQuery = DatasetPopulated.datasetQuery()
                .name("Dataset")
                .build();

        DatasetOnlyBean dataset = DatasetPopulated.datasetOnly().build();

        when(datasetRepository.findAllDatasetsOnlyByName(anyString(), anyString(), any(), any()))
                .thenReturn(new PageImpl<>(singletonList(dataset)));

        Page<DatasetOnly> allDatasets = datasetService.findAllDatasets(datasetQuery, datasetPageRequest);
        assertThat(allDatasets.getContent())
                .contains(dataset);
    }

    @Test
    public void findAllDatasets_QueryByDatasetSchema_CallsDatasetRepository() {
        PageRequest datasetPageRequest = PageRequest.of(0, 1);

        LocalDate referenceDate = LocalDate.of(2000, 1, 1);

        DatasetQuery datasetQuery = DatasetPopulated.datasetQuery()
                .schema("DatasetSchema")
                .entityCode("EntityCode")
                .referenceDate(referenceDate)
                .build();

        datasetService.findAllDatasets(datasetQuery, datasetPageRequest);

        verify(datasetRepository).findAllDatasetsOnlyByDisplayName(
                "DatasetSchema", "EntityCode", referenceDate, datasetPageRequest);
    }

    @Test
    public void findAllDatasets_QueryByDatasetSchema_ReturnsDatasetFromRepository() {
        PageRequest datasetPageRequest = PageRequest.of(0, 1);

        DatasetQuery datasetQuery = DatasetPopulated.datasetQuery()
                .schema("DatasetSchema")
                .build();

        DatasetOnlyBean dataset = DatasetPopulated.datasetOnly().build();

        when(datasetRepository.findAllDatasetsOnlyByDisplayName(anyString(), anyString(), any(), any()))
                .thenReturn(new PageImpl<>(singletonList(dataset)));

        Page<DatasetOnly> allDatasets = datasetService.findAllDatasets(datasetQuery, datasetPageRequest);
        assertThat(allDatasets.getContent())
                .contains(dataset);
    }

    @Test
    public void findAllDatasets_DatasetNameOrSchemaNotProvided_ThrowsException() {
        PageRequest datasetPageRequest = PageRequest.of(0, 1);

        DatasetQuery datasetQuery = DatasetPopulated.datasetQuery().build();

        assertThatThrownBy(() -> datasetService.findAllDatasets(datasetQuery, datasetPageRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void findById_datasetPresent_ReturnsDataset() {
        Dataset dataset = DatasetPopulated.dataset().build();
        when(datasetRepository.findById(123L))
                .thenReturn(Optional.of(dataset));

        assertThat(datasetService.findById(123L).get())
                .isEqualTo(dataset);
    }

    @Test
    public void findById_datasetNotFound_ReturnsOptionalEmpty() {
        when(datasetRepository.findById(123L))
                .thenReturn(Optional.empty());

        assertThat(datasetService.findById(123L).isDefined())
                .isFalse();
    }

    @Test
    public void entityName_ReturnsDatasetSimpleName() {
        assertThat(datasetService.entityName())
                .isEqualTo("Dataset");
    }

    @Test
    public void delete_DelegatesToRepository() {
        Dataset dataset = DatasetPopulated.dataset().build();

        datasetService.delete(dataset);

        verify(datasetRepository).delete(dataset);
    }

    @Test
    public void delete_ReturnsDataset() {
        Dataset dataset = DatasetPopulated.dataset().build();

        Dataset deleted = datasetService.delete(dataset);

        assertThat(deleted).isSameAs(dataset);
    }

    @Test
    public void findByIds_DelegatesToRepository() {
        datasetService.findAllByIds(asList(1L, 2L, 3L));

        verify(datasetRepository).findAllById(asList(1L, 2L, 3L));
    }

    @Test
    public void findByIds_ReturnsResultFromRepository() {
        Dataset dataset = DatasetPopulated.dataset().build();

        when(datasetRepository.findAllById(asList(1L, 2L, 3L)))
                .thenReturn(Collections.singletonList(dataset));

        List<Dataset> allByIds = datasetService.findAllByIds(asList(1L, 2L, 3L));

        assertThat(allByIds)
                .containsExactly(dataset);
    }

    @Test
    public void repository_returnsDatasetRepository() {
        assertThat(datasetService.repository())
                .isEqualTo(datasetRepository);
    }

    @Test
    public void findLatestDatasetForSchemaId_DatasetFound_ReturnsValid() {
        Dataset dataset = DatasetPopulated.dataset().build();
        when(datasetRepository.findLatestDatasetForSchemaId(anyLong(), anyString(), any()))
                .thenReturn(Optional.of(dataset));

        VavrAssert.assertValid(
                datasetService.findLatestDatasetForSchemaId(
                        19, "ent", LocalDate.of(100, 1, 1)))
                .withResult(dataset);
    }

    @Test
    public void findLatestDatasetForSchemaId_DatasetNotFound_ReturnsCrudFailure() {
        when(datasetRepository.findLatestDatasetForSchemaId(anyLong(), anyString(), any()))
                .thenReturn(Optional.empty());

        CRUDFailure crudFailure = VavrAssert.assertFailed(
                datasetService.findLatestDatasetForSchemaId(19, "ent", LocalDate.of(100, 1, 1)))
                .getValidation();

        assertThat(crudFailure.toErrorResponse())
                .isEqualTo(
                        CRUDFailure.cannotFind("Dataset")
                                .with("schemaId", 19)
                                .with("entityCode", "ent")
                                .with("referenceDate", LocalDate.of(100, 1, 1))
                                .asFailure()
                                .toErrorResponse());
    }

    @Test
    public void findForPipeline_DatasetNotFound_ReturnsFailure() {
        when(datasetRepository.findByPipelineInvocationIdAndSchemaId(anyLong(), anyLong()))
                .thenReturn(Optional.empty());

        VavrAssert.assertFailed(datasetService.findForPipeline(
                99L, 626L))
                .withFailure(CRUDFailure.cannotFind("Dataset")
                        .with("pipelineInvocationId", 99L)
                        .with("schemaId", 626L)
                        .asFailure());
    }

    @Test
    public void findForPipeline_DatasetFound_ReturnsDataset() {
        Dataset dataset = DatasetPopulated.dataset().build();
        when(datasetRepository.findByPipelineInvocationIdAndSchemaId(anyLong(), anyLong()))
                .thenReturn(Optional.of(dataset));

        VavrAssert.assertValid(datasetService.findForPipeline(
                99L, 72L))
                .withResult(dataset);
    }

    @Test
    public void updateDatasetRun_DatasetIdNotFound_ReturnsCrudFailure() {
        when(datasetRepository.findById(any()))
                .thenReturn(Optional.empty());

        VavrAssert.assertFailed(datasetService.updateDatasetRun(
                12345L,
                UpdateDatasetRunCall.builder()
                        .stagingJobId(777L)
                        .recordsCount(10L)
                        .build()))
                .withFailure(CRUDFailure.notFoundIds("Dataset", 12345L));
    }

    @Test
    public void updateDatasetRun_DatasetIdFound_ReturnsDatasetId() {
        Dataset currentDataset = DatasetPopulated.dataset()
                .id(12345L).runKey(0L).recordsCount(5L).validationStatus(VALIDATED).build();
        Dataset updatedDataset = DatasetPopulated.dataset()
                .id(12345L).runKey(1L).recordsCount(10L).validationStatus(NOT_VALIDATED).build();

        when(datasetRepository.findById(any()))
                .thenReturn(Optional.of(currentDataset));

        when(datasetRepository.save(any()))
                .thenReturn(updatedDataset);

        VavrAssert.assertValid(datasetService.updateDatasetRun(
                12345L,
                UpdateDatasetRunCall.builder()
                        .stagingJobId(777L)
                        .recordsCount(10L)
                        .build()))
                .withResult(updatedDataset);
    }

    @Test
    public void updateDatasetRun_DatasetIdFound_ChecksLatestDatasetRunForNameEntityCodeReferenceDate() {
        when(timeSource.nowAsDate())
                .thenReturn(toDate("1982-03-02"));

        when(datasetRepository.findById(any()))
                .thenReturn(Optional.of(DatasetPopulated.dataset()
                        .id(12345L)
                        .name("BATMAN")
                        .entityCode("I'M BATMAN")
                        .referenceDate(LocalDate.of(1939, 5, 1))
                        .build()));

        datasetService.updateDatasetRun(12345L, UpdateDatasetRunCall.builder()
                .stagingDatasetId(13L)
                .stagingJobId(7772L)
                .recordsCount(10L)
                .build());

        verify(datasetRepository).findLatestDataset("BATMAN", "I'M BATMAN", LocalDate.of(1939, 5, 1));
    }

    @Test
    public void updateDatasetRun_DatasetIdFound_SavesUpdatedDataset() {
        Dataset latestDatasetRun = DatasetPopulated.dataset()
                .id(12345L)
                .name("BATMAN")
                .entityCode("I'M BATMAN")
                .referenceDate(LocalDate.of(1939, 5, 1))
                .runKey(98L)
                .recordsCount(5L)
                .validationStatus(VALIDATED)
                .stagingJobs(newHashSet(
                        DatasetServiceRequest.builder()
                                .id(12L)
                                .serviceRequestId(7771L)
                                .build()))
                .build();

        when(timeSource.nowAsDate())
                .thenReturn(toDate("1982-03-02"));

        when(datasetRepository.findLatestDataset(any(), any(), any()))
                .thenReturn(Optional.of(latestDatasetRun));

        when(datasetRepository.findById(any()))
                .thenReturn(Optional.of(latestDatasetRun));

        datasetService.updateDatasetRun(12345L, UpdateDatasetRunCall.builder()
                .stagingDatasetId(13L)
                .stagingJobId(7772L)
                .recordsCount(10L)
                .build());

        verify(datasetRepository).save(datasetCaptor.capture());

        Dataset updatedDataset = datasetCaptor.getValue();

        assertThat(updatedDataset.getId()).isEqualTo(12345L);
        assertThat(updatedDataset.getRunKey()).isEqualTo(99L);
        assertThat(updatedDataset.getRecordsCount()).isEqualTo(10L);
        assertThat(updatedDataset.getValidationStatus()).isEqualTo(NOT_VALIDATED);
        assertThat(updatedDataset.getLastUpdated()).isEqualTo(toDate("1982-03-02"));
        assertThat(updatedDataset.getStagingJobs())
                .containsExactlyInAnyOrder(
                        DatasetServiceRequest.builder()
                                .id(12L)
                                .serviceRequestId(7771L)
                                .build(),
                        DatasetServiceRequest.builder()
                                .id(13L)
                                .serviceRequestId(7772L)
                                .build());
    }

    @Test
    public void updateDatasetRun_NewerDatasetRunFound_SavesUpdatedDatasetWithLatestRunKey() {
        when(timeSource.nowAsDate())
                .thenReturn(toDate("1998-04-03"));

        when(datasetRepository.findLatestDataset(any(), any(), any()))
                .thenReturn(Optional.of(DatasetPopulated.dataset()
                        .id(98765L)
                        .name("BATMAN")
                        .entityCode("I'M BATMAN")
                        .referenceDate(LocalDate.of(1939, 5, 1))
                        .runKey(778L)
                        .recordsCount(876L)
                        .lastUpdated(toDate("1998-04-02"))
                        .build()));

        when(datasetRepository.findById(any()))
                .thenReturn(Optional.of(DatasetPopulated.dataset()
                        .id(12345L)
                        .name("BATMAN")
                        .entityCode("I'M BATMAN")
                        .referenceDate(LocalDate.of(1939, 5, 1))
                        .runKey(777L)
                        .recordsCount(765L)
                        .validationStatus(VALIDATED)
                        .stagingJobs(newHashSet(
                                DatasetServiceRequest.builder()
                                        .id(12L)
                                        .serviceRequestId(7771L)
                                        .build()))
                        .lastUpdated(toDate("1998-04-01"))
                        .build()));

        datasetService.updateDatasetRun(12345L, UpdateDatasetRunCall.builder()
                .stagingDatasetId(13L)
                .stagingJobId(7772L)
                .recordsCount(766L)
                .build());

        verify(datasetRepository).save(datasetCaptor.capture());

        Dataset updatedDataset = datasetCaptor.getValue();

        assertThat(updatedDataset.getId()).isEqualTo(12345L);
        assertThat(updatedDataset.getRunKey()).isEqualTo(779L);
        assertThat(updatedDataset.getRecordsCount()).isEqualTo(766L);
        assertThat(updatedDataset.getValidationStatus()).isEqualTo(NOT_VALIDATED);
        assertThat(updatedDataset.getLastUpdated()).isEqualTo(toDate("1998-04-03"));
        assertThat(updatedDataset.getStagingJobs())
                .containsExactlyInAnyOrder(
                        DatasetServiceRequest.builder()
                                .id(12L)
                                .serviceRequestId(7771L)
                                .build(),
                        DatasetServiceRequest.builder()
                                .id(13L)
                                .serviceRequestId(7772L)
                                .build());
    }

    @Test
    public void findPipelineDataset_DatasetIsPipelineDataset_ReturnsDataset() {
        Dataset pipelineDataset = DatasetPopulated.dataset()
                .pipelineInvocationId(1L)
                .pipelineStepInvocationId(2L)
                .build();
        when(datasetRepository.findByNameAndAndRunKeyAndAndReferenceDateAndEntityCode(
                anyString(),
                any(),
                any(),
                anyString()))
                .thenReturn(Optional.of(pipelineDataset));

        VavrAssert.assertValid(datasetService.findPipelineDataset("LIQ", 1L, LocalDate.of(2019, 1, 1), "123"))
                .withResult(pipelineDataset);
    }

    @Test
    public void findPipelineDataset_DatasetIsNotPipelineDataset_ReturnsError() {
        Dataset pipelineDataset = DatasetPopulated.dataset()
                .pipelineInvocationId(null)
                .pipelineStepInvocationId(null)
                .build();

        when(datasetRepository.findByNameAndAndRunKeyAndAndReferenceDateAndEntityCode(
                anyString(),
                any(),
                any(),
                anyString()))
                .thenReturn(Optional.of(pipelineDataset));

        VavrAssert.assertFailed(datasetService.findPipelineDataset("LIQ", 1L, LocalDate.of(2019, 1, 1), "123"))
                .withFailure(CRUDFailure.cannotFind("PipelineDataset")
                        .with("name", "LIQ")
                        .with("runKey", 1L)
                        .with("referenceDate", LocalDate.of(2019, 1, 1))
                        .with("entityCode", "123")
                        .asFailure());
    }

    @Test
    public void findPipelineDataset_DatasetDoesNotExist_ReturnsError() {
        when(datasetRepository.findByNameAndAndRunKeyAndAndReferenceDateAndEntityCode(
                anyString(),
                any(),
                any(),
                anyString()))
                .thenReturn(Optional.empty());

        VavrAssert.assertFailed(datasetService.findPipelineDataset("LIQ", 1L, LocalDate.of(2019, 1, 1), "123"))
                .withFailure(CRUDFailure.cannotFind("Dataset")
                        .with("name", "LIQ")
                        .with("runKey", 1L)
                        .with("referenceDate", LocalDate.of(2019, 1, 1))
                        .with("entityCode", "123")
                        .asFailure());
    }
}
