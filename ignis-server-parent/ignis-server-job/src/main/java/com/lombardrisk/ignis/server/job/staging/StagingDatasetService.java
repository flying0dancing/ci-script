package com.lombardrisk.ignis.server.job.staging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.lombardrisk.ignis.api.dataset.DatasetState;
import com.lombardrisk.ignis.client.external.job.staging.request.v1.StagingRequest;
import com.lombardrisk.ignis.client.external.job.staging.request.v2.StagingRequestV2;
import com.lombardrisk.ignis.common.time.TimeSource;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.data.common.service.JpaCRUDService;
import com.lombardrisk.ignis.server.job.exception.JobStartException;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequest;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequestRepository;
import com.lombardrisk.ignis.server.job.staging.exception.DatasetStateChangeException;
import com.lombardrisk.ignis.server.job.staging.file.DataSourceService;
import com.lombardrisk.ignis.server.job.staging.file.ErrorFileLinkOrStream;
import com.lombardrisk.ignis.server.job.staging.file.ErrorFileService;
import com.lombardrisk.ignis.server.job.staging.model.StagingDataset;
import com.lombardrisk.ignis.server.job.staging.model.StagingDatasetInstruction;
import com.lombardrisk.ignis.server.job.staging.model.StagingInstructions;
import com.lombardrisk.ignis.server.job.staging.validate.StagingRequestV1Validator;
import com.lombardrisk.ignis.server.job.staging.validate.StagingRequestV2Validator;
import com.lombardrisk.ignis.server.product.table.model.Table;
import com.lombardrisk.ignis.spark.api.staging.DownstreamPipeline;
import com.lombardrisk.ignis.spark.api.staging.StagingAppConfig;
import com.lombardrisk.ignis.spark.api.staging.StagingDatasetConfig;
import io.vavr.control.Validation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import static com.lombardrisk.ignis.api.dataset.DatasetState.REGISTERED;
import static com.lombardrisk.ignis.api.dataset.DatasetState.REGISTRATION_FAILED;
import static com.lombardrisk.ignis.api.dataset.DatasetState.VALIDATION_FAILED;
import static com.lombardrisk.ignis.common.json.MapperWrapper.MAPPER;
import static com.lombardrisk.ignis.server.job.servicerequest.ServiceRequestType.STAGING;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Slf4j
@Service
public class StagingDatasetService implements JpaCRUDService<StagingDataset> {

    private final StagingRequestV1Validator stagingRequestV1Validator;
    private final StagingRequestV2Validator stagingRequestV2Validator;
    private final StagingDatasetRepository stagingDatasetRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final JobStarter jobStarter;
    private final StagingSparkConfigService stagingSparkConfigService;
    private final TimeSource timeSource;
    private final DataSourceService dataSourceService;
    private final ErrorFileService errorFileService;

    public StagingDatasetService(
            final StagingRequestV1Validator stagingRequestV1Validator,
            final StagingRequestV2Validator stagingRequestV2Validator,
            final StagingDatasetRepository stagingDatasetRepository,
            final ServiceRequestRepository serviceRequestRepository,
            final JobStarter jobStarter,
            final StagingSparkConfigService stagingSparkConfigService,
            final TimeSource timeSource,
            final DataSourceService dataSourceService,
            final ErrorFileService errorFileService) {
        this.stagingRequestV1Validator = stagingRequestV1Validator;
        this.stagingRequestV2Validator = stagingRequestV2Validator;
        this.stagingDatasetRepository = stagingDatasetRepository;
        this.serviceRequestRepository = serviceRequestRepository;
        this.jobStarter = jobStarter;
        this.stagingSparkConfigService = stagingSparkConfigService;
        this.timeSource = timeSource;
        this.dataSourceService = dataSourceService;
        this.errorFileService = errorFileService;
    }

    @Override
    public String entityName() {
        return StagingDataset.class.getSimpleName();
    }

    @Override
    public JpaRepository<StagingDataset, Long> repository() {
        return stagingDatasetRepository;
    }

    @Deprecated
    @Transactional
    public Validation<List<CRUDFailure>, Long> start(
            final StagingRequest jobRequest, final String userName) throws JobStartException {

        Validation<List<CRUDFailure>, StagingInstructions> stagingConfigValidation =
                stagingRequestV1Validator.validate(jobRequest);

        if (stagingConfigValidation.isInvalid()) {
            return Validation.invalid(stagingConfigValidation.getError());
        }

        long serviceRequestId = startStagingJob(userName, stagingConfigValidation.get());

        return Validation.valid(serviceRequestId);
    }

    @Transactional
    public Validation<List<CRUDFailure>, Long> start(
            final StagingRequestV2 jobRequest, final String userName) throws JobStartException {

        Validation<List<CRUDFailure>, StagingInstructions> stagingConfigValidation =
                stagingRequestV2Validator.validate(jobRequest);

        if (stagingConfigValidation.isInvalid()) {
            return Validation.invalid(stagingConfigValidation.getError());
        }

        long serviceRequestId = startStagingJob(userName, stagingConfigValidation.get());

        return Validation.valid(serviceRequestId);
    }

    public Validation<CRUDFailure, List<StagingDataset>> findStagingDatasets(
            final Long serviceRequestId, final Long datasetId, final String datasetName) {

        if (serviceRequestId == null && datasetId == null) {
            return Validation.invalid(CRUDFailure.invalidParameters()
                    .paramError("jobId", "not provided").paramError("datasetId", "not provided").asFailure());
        }

        if (datasetId != null) {
            return Validation.valid(stagingDatasetRepository.findByDatasetId(datasetId));
        }

        if (isNotEmpty(datasetName)) {
            return Validation.valid(stagingDatasetRepository
                    .findByServiceRequestIdAndDatasetName(serviceRequestId, datasetName));
        }

        return Validation.valid(stagingDatasetRepository.findByServiceRequestId(serviceRequestId));
    }

    public Validation<CRUDFailure, ErrorFileLinkOrStream> downloadStagingErrorFile(final long stagingDatasetId) throws IOException {
        Validation<CRUDFailure, StagingDataset> stagingDatasetResult = findWithValidation(stagingDatasetId);
        if (stagingDatasetResult.isInvalid()) {
            return Validation.invalid(stagingDatasetResult.getError());
        }

        return Validation.valid(errorFileService.downloadValidationErrorFile(stagingDatasetResult.get()));
    }

    @Transactional
    public StagingDataset updateStagingDatasetState(
            final long stagingDatasetId,
            final DatasetState state)
            throws DatasetStateChangeException {

        Validation<CRUDFailure, StagingDataset> stagingDatasetOptional = findWithValidation(stagingDatasetId);
        if (stagingDatasetOptional.isInvalid()) {
            throw new DatasetStateChangeException(
                    String.format("staging dataset can't be found with stagingDatasetId %d", stagingDatasetId));
        }
        StagingDataset stagingDataset = stagingDatasetOptional.get();
        if (!stagingDataset.getStatus().canTransitionTo(state)) {
            throw new DatasetStateChangeException(
                    String.format(
                            "Can't transit dataset state from %s to %s with stagingDatasetId:%d",
                            stagingDataset.getStatus(), state, stagingDatasetId));
        }

        if (state == VALIDATION_FAILED
                || state == REGISTRATION_FAILED
                || state == REGISTERED) {
            stagingDataset.setEndTime(timeSource.nowAsDate());
        }

        stagingDataset.setLastUpdateTime(timeSource.nowAsDate());
        stagingDataset.setStatus(state);

        return stagingDatasetRepository.save(stagingDataset);
    }

    private long startStagingJob(
            final String userName, final StagingInstructions stagingInstructions) throws JobStartException {

        ServiceRequest serviceRequest = createAndSaveEmptyServiceRequest(stagingInstructions.getJobName(), userName);

        Set<StagingDatasetConfig> stagingDatasetConfigs = createStagingDatasets(
                stagingInstructions.getStagingDatasetInstructions(), serviceRequest);

        Set<DownstreamPipeline> downstreamPipelines = createDownstreamPipelines(stagingInstructions);

        String requestMessage = createStagingRequestMessage(
                stagingInstructions.getJobName(), stagingDatasetConfigs, downstreamPipelines, serviceRequest);

        serviceRequest.setRequestMessage(requestMessage);
        serviceRequestRepository.saveAndFlush(serviceRequest);

        return jobStarter.startJob(STAGING.name(), serviceRequest, new Properties());
    }

    private String createStagingRequestMessage(
            final String jobName,
            final Set<StagingDatasetConfig> stagingDatasetConfigs,
            final Set<DownstreamPipeline> downstreamPipelines,
            final ServiceRequest serviceRequest) {

        StagingAppConfig newConfig = StagingAppConfig.builder()
                .name(jobName)
                .serviceRequestId(serviceRequest.getId())
                .items(stagingDatasetConfigs)
                .downstreamPipelines(downstreamPipelines)
                .build();
        try {
            return MAPPER.writeValueAsString(newConfig);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to set service request message for " + newConfig, e);
        }
    }

    private ServiceRequest createAndSaveEmptyServiceRequest(final String jobName, final String userName) {
        return serviceRequestRepository.save(
                ServiceRequest.builder()
                        .name(jobName)
                        .serviceRequestType(STAGING)
                        .createdBy(userName)
                        .build());
    }

    private Set<StagingDatasetConfig> createStagingDatasets(
            final Set<StagingDatasetInstruction> stagingDatasetInstructions,
            final ServiceRequest serviceRequest) {

        Set<StagingDatasetConfig> datasetConfigs = new HashSet<>();
        for (StagingDatasetInstruction datasetInstruction : stagingDatasetInstructions) {

            StagingDataset stagingDataset = createStagingDataset(datasetInstruction, serviceRequest.getId());
            StagingDatasetConfig stagingDatasetConfig = stagingSparkConfigService.createDatasetAppConfig(
                    stagingDataset, datasetInstruction);

            datasetConfigs.add(stagingDatasetConfig);
        }

        return datasetConfigs;
    }

    private StagingDataset createStagingDataset(
            final StagingDatasetInstruction stagingDatasetInstruction, final Long serviceRequestId) {

        Table schema = stagingDatasetInstruction.getSchema();
        String physicalTableName = schema.getPhysicalTableName();

        String stagingFileCopyLocation = dataSourceService.stagingFileCopyLocation(serviceRequestId, physicalTableName);

        StagingDataset stagingDataset = StagingDataset.builder()
                .table(schema.getDisplayName() + " v." + schema.getVersion())
                .status(DatasetState.QUEUED)
                .serviceRequestId(serviceRequestId)
                .startTime(timeSource.nowAsDate())
                .lastUpdateTime(timeSource.nowAsDate())
                .datasetName(physicalTableName)
                .entityCode(stagingDatasetInstruction.getEntityCode())
                .referenceDate(stagingDatasetInstruction.getReferenceDate())
                .stagingFile(stagingDatasetInstruction.getFilePath())
                .stagingFileCopy(stagingFileCopyLocation)
                .validationErrorFile(errorFileService.errorFileRelativePath(serviceRequestId, physicalTableName))
                .build();

        StagingDataset createdStagingDataset = stagingDatasetRepository.save(stagingDataset);

        log.debug("Created staging dataset [{}] for job [{}] schema [{}]",
                createdStagingDataset.getId(), serviceRequestId, schema.getVersionedName());

        return createdStagingDataset;
    }

    private Set<DownstreamPipeline> createDownstreamPipelines(final StagingInstructions stagingInstructions) {
        return stagingInstructions.getDownstreamPipelineInstructions().stream()
                .map(instruction -> DownstreamPipeline.builder()
                        .pipelineId(instruction.getPipeline().getId())
                        .pipelineName(instruction.getPipeline().getName())
                        .entityCode(instruction.getEntityCode())
                        .referenceDate(instruction.getReferenceDate())
                        .build())
                .collect(toSet());
    }
}
