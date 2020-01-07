package com.lombardrisk.ignis.server.dataset;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.lombardrisk.ignis.api.dataset.DatasetType;
import com.lombardrisk.ignis.api.dataset.ValidationStatus;
import com.lombardrisk.ignis.client.internal.CreateDatasetCall;
import com.lombardrisk.ignis.client.internal.UpdateDatasetRunCall;
import com.lombardrisk.ignis.common.time.TimeSource;
import com.lombardrisk.ignis.data.common.Identifiable;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.data.common.failure.NotFoundFailure;
import com.lombardrisk.ignis.data.common.service.JpaCRUDService;
import com.lombardrisk.ignis.server.dataset.model.Dataset;
import com.lombardrisk.ignis.server.dataset.model.DatasetOnly;
import com.lombardrisk.ignis.server.dataset.model.DatasetQuery;
import com.lombardrisk.ignis.server.dataset.model.DatasetServiceRequest;
import com.lombardrisk.ignis.server.product.table.TableRepository;
import com.lombardrisk.ignis.server.product.table.model.Table;
import com.lombardrisk.ignis.spark.api.staging.DatasetProperties;
import io.vavr.control.Option;
import io.vavr.control.Validation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;

import static com.google.common.collect.Sets.newHashSet;
import static com.lombardrisk.ignis.api.dataset.ValidationStatus.NOT_VALIDATED;
import static com.lombardrisk.ignis.common.json.MapperWrapper.MAPPER;
import static com.lombardrisk.ignis.data.common.failure.CRUDFailure.cannotFind;
import static java.util.Collections.singletonList;

@Service
@Transactional
@Slf4j
public class DatasetService implements JpaCRUDService<Dataset> {

    private final DatasetJpaRepository datasetRepository;
    private final TableRepository tableRepository;
    private final TimeSource timeSource;

    public DatasetService(
            final DatasetJpaRepository datasetRepository,
            final TableRepository tableRepository,
            final TimeSource timeSource) {
        this.datasetRepository = datasetRepository;
        this.tableRepository = tableRepository;
        this.timeSource = timeSource;
    }

    @Override
    public String entityName() {
        return Dataset.class.getSimpleName();
    }

    @Override
    public JpaRepository<Dataset, Long> repository() {
        return datasetRepository;
    }

    @Transactional
    public Validation<CRUDFailure, Identifiable> createDataset(final CreateDatasetCall datasetCall) {
        Optional<Table> datasetSchema = findSchema(datasetCall.getSchemaId());

        if (!datasetSchema.isPresent()) {
            return Validation.invalid(
                    cannotFind("schema")
                            .with("id", datasetCall.getSchemaId())
                            .asFailure());
        }
        Dataset dataset = saveDataset(datasetCall, datasetSchema.get());
        return Validation.valid(dataset);
    }

    @Transactional
    public Validation<CRUDFailure, Identifiable> updateDatasetRun(
            final Long datasetId, final UpdateDatasetRunCall datasetRunCall) {

        Validation<CRUDFailure, Dataset> datasetValidation = findWithValidation(datasetId);

        if (datasetValidation.isInvalid()) {
            CRUDFailure failure = datasetValidation.getError();
            log.error("Validation failure, code [{}], message [{}]",
                    failure.getErrorCode(), failure.getErrorMessage());

            return Validation.invalid(failure);
        }

        Dataset dataset = datasetValidation.get();

        Long nextRunKey = getNextRunKey(dataset.getName(), dataset.getEntityCode(), dataset.getReferenceDate());

        dataset.setRunKey(nextRunKey);
        dataset.setRecordsCount(datasetRunCall.getRecordsCount());
        dataset.setValidationStatus(NOT_VALIDATED);
        dataset.setLastUpdated(timeSource.nowAsDate());

        DatasetServiceRequest stagingJobServiceRequest = DatasetServiceRequest.builder()
                .id(datasetRunCall.getStagingDatasetId())
                .serviceRequestId(datasetRunCall.getStagingJobId())
                .build();

        dataset.getStagingJobs().add(stagingJobServiceRequest);

        Dataset updatedDataset = datasetRepository.save(dataset);

        return Validation.valid(updatedDataset);
    }

    public Validation<CRUDFailure, Dataset> findForPipeline(
            final Long pipelineInvocationId, final Long schemaId) {
        return Option.ofOptional(
                datasetRepository.findByPipelineInvocationIdAndSchemaId(pipelineInvocationId, schemaId))
                .toValid(CRUDFailure.cannotFind("Dataset")
                        .with("pipelineInvocationId", pipelineInvocationId)
                        .with("schemaId", schemaId)
                        .asFailure());
    }

    public Validation<NotFoundFailure<String>, Dataset> findLatestDataset(
            final String schemaName, final DatasetProperties datasetProperties) {
        Optional<Dataset> latestDataset = datasetRepository.findLatestDataset(
                schemaName, datasetProperties.getEntityCode(), datasetProperties.getReferenceDate());

        return Option.ofOptional(latestDataset)
                .toValid(notFoundForMetadata(schemaName, datasetProperties));
    }

    public Page<DatasetOnly> findAllDatasets(final Pageable pageable) {
        return datasetRepository.findAllDatasetsOnly(pageable);
    }

    public Page<DatasetOnly> findAllDatasets(final DatasetQuery datasetQuery, final Pageable pageable) {
        if (datasetQuery.getName() != null) {
            return datasetRepository.findAllDatasetsOnlyByName(
                    datasetQuery.getName(), datasetQuery.getEntityCode(), datasetQuery.getReferenceDate(), pageable);
        }

        if (datasetQuery.getSchema() != null) {
            return datasetRepository.findAllDatasetsOnlyByDisplayName(
                    datasetQuery.getSchema(), datasetQuery.getEntityCode(), datasetQuery.getReferenceDate(), pageable);
        }

        throw new IllegalArgumentException("Dataset name or schema must be provided to query datasets");
    }

    public Validation<CRUDFailure, Dataset> findLatestDatasetForSchemaId(
            final long schemaId, final String entityCode, final LocalDate referenceDate) {

        return Option.ofOptional(
                datasetRepository.findLatestDatasetForSchemaId(schemaId, entityCode, referenceDate))
                .toValid(CRUDFailure.cannotFind("Dataset")
                        .with("schemaId", schemaId)
                        .with("entityCode", entityCode)
                        .with("referenceDate", referenceDate)
                        .asFailure());
    }

    public Validation<CRUDFailure, Dataset> findPipelineDataset(
            final String name,
            final Long runKey,
            final LocalDate referenceDate,
            final String entityCode) {
        Optional<Dataset> dataset =
                datasetRepository.findByNameAndAndRunKeyAndAndReferenceDateAndEntityCode(
                        name,
                        runKey,
                        referenceDate,
                        entityCode);

        if (dataset.isPresent()) {
            Long pipelineInvocationId = dataset.get().getPipelineInvocationId();
            if (pipelineInvocationId != null) {
                return Validation.valid(dataset.get());
            }
            return Validation.invalid(CRUDFailure.cannotFind("PipelineDataset")
                    .with("name", name)
                    .with("runKey", runKey)
                    .with("referenceDate", referenceDate)
                    .with("entityCode", entityCode)
                    .asFailure());
        }

        return Validation.invalid(CRUDFailure.cannotFind("Dataset")
                .with("name", name)
                .with("runKey", runKey)
                .with("referenceDate", referenceDate)
                .with("entityCode", entityCode)
                .asFailure());
    }

    private Optional<Table> findSchema(final long schemaId) {
        Optional<Table> optionalSchema = tableRepository.findById(schemaId);
        if (!optionalSchema.isPresent()) {
            log.error("Cannot find schema with id [{}]", schemaId);
        }
        return optionalSchema;
    }

    private Dataset saveDataset(final CreateDatasetCall datasetCall, final Table schema) {
        schema.setHasDatasets(true);
        tableRepository.save(schema);

        Dataset.DatasetBuilder baseDataset = baseDataset(datasetCall, schema);

        String datasetName = schema.getPhysicalTableName();
        String entityCode = datasetCall.getEntityCode();
        LocalDate referenceDate = datasetCall.getReferenceDate();

        baseDataset.runKey(getNextRunKey(datasetName, entityCode, referenceDate));

        if (datasetCall.getStagingDatasetId() != null) {
            DatasetServiceRequest stagingJob = DatasetServiceRequest.builder()
                    .id(datasetCall.getStagingDatasetId())
                    .serviceRequestId(datasetCall.getStagingJobId())
                    .build();

            baseDataset.stagingJobs(newHashSet(stagingJob));
        }

        return datasetRepository.save(baseDataset.build());
    }

    private Dataset.DatasetBuilder baseDataset(final CreateDatasetCall datasetCall, final Table schema) {
        Date now = timeSource.nowAsDate();

        return Dataset.builder()
                .entityCode(datasetCall.getEntityCode())
                .referenceDate(datasetCall.getReferenceDate())
                .predicate(datasetCall.getPredicate())
                .rowKeySeed(datasetCall.getRowKeySeed())
                .recordsCount(datasetCall.getRecordsCount())
                .validationStatus(
                        datasetCall.isAutoValidate()
                                ? ValidationStatus.QUEUED
                                : NOT_VALIDATED)
                .schema(schema)
                .table(schema.getPhysicalTableName())
                .name(schema.getPhysicalTableName())
                .pipelineJobId(datasetCall.getPipelineJobId())
                .pipelineInvocationId(datasetCall.getPipelineInvocationId())
                .pipelineStepInvocationId(datasetCall.getPipelineStepInvocationId())
                .createdTime(now)
                .lastUpdated(now)
                .datasetType(DatasetType.STAGING_DATASET);
    }

    private NotFoundFailure<String> notFoundForMetadata(
            final String schemaName,
            final DatasetProperties datasetProperties) {
        try {
            String message = String.format("schema: %s, metadata: %s",
                    schemaName, MAPPER.writeValueAsString(datasetProperties));

            return CRUDFailure.notFoundIndices("dataset", Dataset.class.getSimpleName(), singletonList(message));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private Long getNextRunKey(final String name, final String entityCode, final LocalDate referenceDate) {
        return Option.ofOptional(datasetRepository.findLatestDataset(name, entityCode, referenceDate))
                .map(Dataset::getRunKey)
                .map(runKey -> runKey + 1)
                .getOrElse(1L);
    }
}
