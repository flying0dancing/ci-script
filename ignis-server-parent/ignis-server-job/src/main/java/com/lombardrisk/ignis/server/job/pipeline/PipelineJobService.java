package com.lombardrisk.ignis.server.job.pipeline;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lombardrisk.ignis.api.calendar.HolidayCalendar;
import com.lombardrisk.ignis.client.external.job.pipeline.PipelineRequest;
import com.lombardrisk.ignis.common.time.TimeSource;
import com.lombardrisk.ignis.data.common.Identifiable;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.server.config.calendar.CalendarService;
import com.lombardrisk.ignis.server.dataset.model.Dataset;
import com.lombardrisk.ignis.server.dataset.pipeline.invocation.PipelineInvocationService;
import com.lombardrisk.ignis.server.dataset.pipeline.invocation.model.PipelineInvocation;
import com.lombardrisk.ignis.server.dataset.pipeline.invocation.model.PipelineStepInvocation;
import com.lombardrisk.ignis.server.dataset.pipeline.invocation.model.PipelineStepInvocationDataset;
import com.lombardrisk.ignis.server.dataset.pipeline.invocation.model.PipelineStepStatus;
import com.lombardrisk.ignis.server.job.exception.JobStartException;
import com.lombardrisk.ignis.server.job.pipeline.model.PipelineAggregationInput;
import com.lombardrisk.ignis.server.job.pipeline.model.PipelineInput;
import com.lombardrisk.ignis.server.job.pipeline.model.PipelineInputValidator;
import com.lombardrisk.ignis.server.job.pipeline.model.PipelineJoinInput;
import com.lombardrisk.ignis.server.job.pipeline.model.PipelineMapInput;
import com.lombardrisk.ignis.server.job.pipeline.model.PipelineScriptletInput;
import com.lombardrisk.ignis.server.job.pipeline.model.PipelineStepDatasetInput;
import com.lombardrisk.ignis.server.job.pipeline.model.PipelineStepInput;
import com.lombardrisk.ignis.server.job.pipeline.model.PipelineUnionInput;
import com.lombardrisk.ignis.server.job.pipeline.model.PipelineWindowInput;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequest;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequestRepository;
import com.lombardrisk.ignis.server.job.staging.JobStarter;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineStep;
import com.lombardrisk.ignis.server.product.pipeline.model.TransformationType;
import com.lombardrisk.ignis.spark.api.pipeline.PipelineAppConfig;
import com.lombardrisk.ignis.spark.api.pipeline.spark.SparkFunctionConfig;
import io.vavr.control.Either;
import io.vavr.control.Validation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.collect.Sets.newLinkedHashSet;
import static com.lombardrisk.ignis.server.job.JobFailure.JOB_COULD_NOT_START;
import static com.lombardrisk.ignis.server.job.servicerequest.ServiceRequestType.PIPELINE;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;

@AllArgsConstructor
@Slf4j
public class PipelineJobService {

    private static final int CALENDAR_YEAR_RANGE = 2;
    private final PipelineInvocationService pipelineInvocationService;
    private final JobStarter jobStarter;
    private final ServiceRequestRepository serviceRequestRepository;
    private final PipelineInputValidator pipelineInputValidator;
    private final CalendarService calendarService;
    private final TimeSource timeSource;
    private final ObjectMapper objectMapper;

    public Validation<List<ErrorResponse>, Identifiable> startJob(
            final PipelineRequest pipelineRequest, final String currentUser) {

        try {
            Validation<List<ErrorResponse>, PipelineInput> pipelineInput = pipelineInputValidator
                    .validatePipelineRequest(pipelineRequest);

            if (pipelineInput.isInvalid()) {
                return Validation.invalid(pipelineInput.getError());
            }

            ServiceRequest serviceRequest = createEmptyServiceRequest(pipelineRequest, currentUser);

            ServiceRequest updatedServiceRequest = registerPipelineInvocation(
                    pipelineRequest, serviceRequest, pipelineInput.get());

            long serviceRequestId = jobStarter.startJob(PIPELINE.name(), updatedServiceRequest, new Properties());
            return Validation.valid(() -> serviceRequestId);
        } catch (final JsonProcessingException e) {
            log.warn("Could not process pipeline request,", e);
            return Validation.invalid(singletonList(
                    ErrorResponse.valueOf("", "Invalid Request")));
        } catch (final JobStartException e) {
            log.error("Could not start job", e);
            return Validation.invalid(singletonList(
                    ErrorResponse.valueOf("Could not start Pipeline job", JOB_COULD_NOT_START.getErrorCode())));
        }
    }

    private ServiceRequest registerPipelineInvocation(
            final PipelineRequest pipelineRequest,
            final ServiceRequest serviceRequest,
            final PipelineInput pipelineInputsList) throws JsonProcessingException {

        PipelineAppConfig appConfig = createPipelineInvocation(
                pipelineRequest.getName(), serviceRequest,
                pipelineInputsList);

        String requestMessage = objectMapper.writeValueAsString(appConfig);
        serviceRequest.setRequestMessage(requestMessage);
        return serviceRequestRepository.save(serviceRequest);
    }

    private PipelineAppConfig createPipelineInvocation(
            final String jobName, final ServiceRequest serviceRequest,
            final PipelineInput pipelineInput) {

        PipelineInvocation pipelineInvocation =
                pipelineInvocationService.create(buildInvocation(
                        jobName,
                        serviceRequest,
                        pipelineInput));

        LocalDate referenceDate = pipelineInput.getReferenceDate();

        Long productId = pipelineInput.getPipeline().getProductId();
        HolidayCalendar holidayCalendar = calendarService.createHolidayCalendar(
                productId,
                referenceDate.minusYears(CALENDAR_YEAR_RANGE),
                referenceDate.plusYears(CALENDAR_YEAR_RANGE))
                .getOrElseThrow(() -> new IllegalStateException(
                        "Product [" + productId + "] does not exist for pipeline ["
                                + pipelineInput.getPipeline().getId()
                                + "]"));

        return PipelineAppConfig.builder()
                .name(pipelineInput.getJobName())
                .serviceRequestId(serviceRequest.getId())
                .pipelineInvocationId(pipelineInvocation.getId())
                .pipelineSteps(pipelineInput.getPipelineStepAppConfigs(pipelineInvocation))
                .sparkFunctionConfig(SparkFunctionConfig.builder()
                        .holidayCalendar(holidayCalendar)
                        .referenceDate(referenceDate)
                        .build())
                .build();
    }

    private PipelineInvocation buildInvocation(
            final String jobName,
            final ServiceRequest serviceRequest,
            final PipelineInput pipelineInput) {

        List<PipelineStepInvocation> pipelineStepInvocations = pipelineInput.getPipelineSteps().stream()
                .map(this::buildStepInvocation)
                .collect(Collectors.toList());

        return PipelineInvocation.builder()
                .name(jobName)
                .serviceRequestId(serviceRequest.getId())
                .pipelineId(pipelineInput.getPipeline().getId())
                .entityCode(pipelineInput.getEntityCode())
                .referenceDate(pipelineInput.getReferenceDate())
                .createdTime(timeSource.nowAsLocalDateTime())
                .steps(newLinkedHashSet(pipelineStepInvocations))
                .build();
    }

    private PipelineStepInvocation buildStepInvocation(final PipelineStepInput pipelineStepInput) {
        TransformationType type = pipelineStepInput.getPipelineStep().getType();
        switch (type) {
            case AGGREGATION:
                return buildAggregationStepInvocation((PipelineAggregationInput) pipelineStepInput);
            case MAP:
                return buildMapStepInvocation((PipelineMapInput) pipelineStepInput);
            case JOIN:
                return buildJoinStepInvocation((PipelineJoinInput) pipelineStepInput);
            case WINDOW:
                return buildWindowStepInvocation((PipelineWindowInput) pipelineStepInput);
            case UNION:
                return buildUnionStepInvocation((PipelineUnionInput) pipelineStepInput);
            case SCRIPTLET:
                return buildScriptletStepInvocation((PipelineScriptletInput) pipelineStepInput);
            default:
                throw new IllegalArgumentException(type.name() + ": not supported");
        }
    }

    private PipelineStepInvocation buildMapStepInvocation(final PipelineMapInput pipelineStepInput) {
        return buildPipelineStepInvocation(pipelineStepInput, singleton(pipelineStepInput.getDatasetInput()));
    }

    private PipelineStepInvocation buildAggregationStepInvocation(final PipelineAggregationInput pipelineStepInput) {
        return buildPipelineStepInvocation(pipelineStepInput, singleton(pipelineStepInput.getDatasetInput()));
    }

    private PipelineStepInvocation buildJoinStepInvocation(final PipelineJoinInput pipelineStepInput) {
        return buildPipelineStepInvocation(pipelineStepInput, pipelineStepInput.getDatasetInputs());
    }

    private PipelineStepInvocation buildWindowStepInvocation(final PipelineWindowInput pipelineStepInput) {
        return buildPipelineStepInvocation(pipelineStepInput, singleton(pipelineStepInput.getDatasetInput()));
    }

    private PipelineStepInvocation buildUnionStepInvocation(final PipelineUnionInput pipelineStepInput) {
        return buildPipelineStepInvocation(pipelineStepInput, pipelineStepInput.getDatasetInputs());
    }

    private PipelineStepInvocation buildScriptletStepInvocation(final PipelineScriptletInput pipelineStepInput) {
        return buildPipelineStepInvocation(pipelineStepInput, pipelineStepInput.getDatasetInputs());
    }

    private PipelineStepInvocation buildPipelineStepInvocation(
            final PipelineStepInput pipelineStepInput,
            final Set<PipelineStepDatasetInput> pipelineStepDatasetInputs) {

        Set<PipelineStepInvocationDataset> datasets = new HashSet<>();
        Set<Long> pipelineStepIds = new HashSet<>();

        for (PipelineStepDatasetInput pipelineStepDatasetInput : pipelineStepDatasetInputs) {
            Either<Dataset, PipelineStep> datasetInput = pipelineStepDatasetInput.getDatasetInput();

            if (datasetInput.isLeft()) {
                datasets.add(PipelineStepInvocationDataset.builder()
                        .datasetId(datasetInput.getLeft().getId())
                        .datasetRunKey(datasetInput.getLeft().getRunKey())
                        .build());
            } else {
                pipelineStepIds.add(datasetInput.get().getId());
            }
        }

        return PipelineStepInvocation.builder()
                .pipelineStepId(pipelineStepInput.getPipelineStep().getId())
                .inputDatasets(datasets)
                .inputPipelineStepIds(pipelineStepIds)
                .status(pipelineStepInput.isSkipped() ? PipelineStepStatus.SKIPPED : PipelineStepStatus.PENDING)
                .build();
    }

    private ServiceRequest createEmptyServiceRequest(
            final PipelineRequest pipelineRequest,
            final String currentUser) {

        return serviceRequestRepository.save(ServiceRequest.builder()
                .name(pipelineRequest.getName())
                .serviceRequestType(PIPELINE)
                .createdBy(currentUser)
                .build());
    }
}
