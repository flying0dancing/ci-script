package com.lombardrisk.ignis.server.controller.jobs;

import com.lombardrisk.ignis.client.core.view.IdView;
import com.lombardrisk.ignis.client.core.view.PagedView;
import com.lombardrisk.ignis.client.external.job.JobExecutionView;
import com.lombardrisk.ignis.client.external.job.pipeline.PipelineRequest;
import com.lombardrisk.ignis.client.external.job.staging.request.DatasetMetadata;
import com.lombardrisk.ignis.client.external.job.staging.request.v1.StagingItemRequest;
import com.lombardrisk.ignis.client.external.job.staging.request.v1.StagingRequest;
import com.lombardrisk.ignis.client.external.job.staging.request.v2.StagingRequestV2;
import com.lombardrisk.ignis.client.external.job.validation.ValidationJobRequest;
import com.lombardrisk.ignis.client.external.path.api;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.server.batch.JobOperatorImpl;
import com.lombardrisk.ignis.server.batch.JobService;
import com.lombardrisk.ignis.server.config.swagger.ApiErrorResponses;
import com.lombardrisk.ignis.server.job.JobFailure;
import com.lombardrisk.ignis.server.job.exception.JobStartException;
import com.lombardrisk.ignis.server.job.pipeline.PipelineJobService;
import com.lombardrisk.ignis.server.job.staging.StagingDatasetService;
import com.lombardrisk.ignis.server.product.util.PageToViewConverter;
import com.lombardrisk.ignis.web.common.response.FcrResponse;
import io.micrometer.core.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.vavr.control.Either;
import io.vavr.control.Validation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.List;

import static com.lombardrisk.ignis.web.common.exception.GlobalExceptionHandler.UNEXPECTED_ERROR_RESPONSE;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;
import static org.apache.http.HttpStatus.SC_OK;
import static org.mortbay.jetty.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequestMapping(produces = APPLICATION_JSON_VALUE)
@Api(value = "jobs", description = "API to start, stop and track the status of jobs in FCR", tags = "Jobs API")
@ApiErrorResponses
public class JobsController {

    private final JobService jobService;
    private final PipelineJobService pipelineJobService;
    private final PageToViewConverter pageToViewConverter;
    private final StagingDatasetService stagingDatasetService;
    private final JobOperatorImpl jobOperator;

    public JobsController(
            final JobService jobService,
            final PipelineJobService pipelineJobService,
            final PageToViewConverter pageToViewConverter,
            final StagingDatasetService stagingDatasetService,
            final JobOperatorImpl jobOperator) {
        this.jobService = jobService;
        this.pipelineJobService = pipelineJobService;
        this.pageToViewConverter = pageToViewConverter;
        this.stagingDatasetService = stagingDatasetService;
        this.jobOperator = jobOperator;
    }

    @PostMapping(path = api.external.v1.Jobs, consumes = APPLICATION_JSON_VALUE, params = "type=validation")
    @Timed("ignis.start_validation_job")
    @ApiIgnore
    public FcrResponse<IdView> startJob(
            @RequestBody @Valid final ValidationJobRequest validationJobRequest) {
        log.info(
                "Start validation job [{}] for dataset [{}]",
                validationJobRequest.getName(),
                validationJobRequest.getDatasetId());

        String userName = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        Either<JobFailure, IdView> jobStartResult =
                jobService.startValidationJob(
                        validationJobRequest.getDatasetId(), validationJobRequest.getName(), userName)
                        .map(IdView::new);

        return jobStartResult
                .peek(id -> log.info("Run validation job [{}] by user [{}] ", id, userName))
                .fold(error -> mapFailure(error, validationJobRequest), FcrResponse::okResponse);
    }

    @Timed("ignis.start_staging_job")
    @PostMapping(path = api.external.v1.Jobs, consumes = APPLICATION_JSON_VALUE, params = "type=staging")
    @ApiResponses({ @ApiResponse(response = IdView.class, code = SC_OK, message = OK) })
    @Deprecated
    public FcrResponse<IdView> startJob(
            @RequestBody @Valid final StagingRequest request) throws JobStartException {
        logStagingRequest(request);

        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        Validation<List<CRUDFailure>, Long> result = stagingDatasetService.start(request, userName);

        return toIdView(result, userName);
    }

    @Timed("ignis.start_staging_job_v2")
    @PostMapping(path = api.external.v2.Jobs, consumes = APPLICATION_JSON_VALUE, params = "type=staging")
    @ApiResponses({ @ApiResponse(response = IdView.class, code = SC_OK, message = OK) })
    public FcrResponse<IdView> startJobV2(
            @RequestBody @Valid final StagingRequestV2 request) throws JobStartException {

        log.info("Start staging job [{}]", request.getName());

        String userName = SecurityContextHolder.getContext().getAuthentication().getName();
        Validation<List<CRUDFailure>, Long> result = stagingDatasetService.start(request, userName);

        return toIdView(result, userName);
    }

    private FcrResponse<IdView> toIdView(
            final Validation<List<CRUDFailure>, Long> serviceRequestId, final String userName) {

        return serviceRequestId
                .peek(id -> log.info("Run staging job [{}] by user [{}] ", id, userName))
                .map(IdView::new)
                .fold(FcrResponse::crudFailure, FcrResponse::okResponse);
    }

    @PostMapping(path = api.external.v1.Jobs, consumes = APPLICATION_JSON_VALUE, params = "type=pipeline")
    @ApiIgnore
    public FcrResponse<IdView> startJob(
            @RequestBody @Valid final PipelineRequest pipelineRequest) {

        String userName = SecurityContextHolder.getContext().getAuthentication().getName();

        return pipelineJobService.startJob(pipelineRequest, userName)
                .map(identifiable -> new IdView(identifiable.getId()))
                .fold(FcrResponse::badRequest, FcrResponse::okResponse);
    }

    @GetMapping(path = api.external.v1.Jobs)
    @Timed("ignis.get_all_jobs")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", defaultValue = "0", dataType = "int", paramType = "query"),
            @ApiImplicitParam(name = "size", dataType = "int", paramType = "query")
    })
    public PagedView<JobExecutionView> getAllJobs(
            @ApiIgnore @PageableDefault(size = Integer.MAX_VALUE) final Pageable pageable,
            @ApiIgnore @RequestParam(value = "search", required = false) final String search) {

        if (search != null) {
            return pageToViewConverter.apply(jobService.searchJobs(pageable, search));
        }
        return pageToViewConverter.apply(jobService.getAllJobs(pageable));
    }

    @GetMapping(api.external.v1.jobs.ById)
    @Timed("ignis.get_job")
    @ApiResponses({ @ApiResponse(code = SC_OK, response = JobExecutionView.class, message = OK) })
    public FcrResponse<JobExecutionView> getJob(
            @PathVariable(api.Params.ID) final long jobExecutionId) {

        return jobService.getJob(jobExecutionId)
                .fold(FcrResponse::crudFailure, FcrResponse::okResponse);
    }

    @PutMapping(value = api.external.v1.jobs.byID.Stop,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Timed("ignis.terminate_job")
    @ResponseBody
    @ApiResponses({ @ApiResponse(code = SC_OK, response = Boolean.class, message = OK) })
    public FcrResponse<Boolean> terminateJob(
            @PathVariable(api.Params.ID) final long jobExecutionId) {
        log.info("Stop job [{}]", jobExecutionId);

        return jobOperator.stop(jobExecutionId)
                .fold(FcrResponse::badRequest, FcrResponse::okResponse);
    }

    private <T> FcrResponse<T> mapFailure(
            final JobFailure error, final ValidationJobRequest request) {

        switch (error) {
            case JOB_COULD_NOT_START:
                ErrorResponse errorResponse = ErrorResponse.valueOf(
                        "An unexpected error occurred, could not start job", error.getErrorCode());
                return FcrResponse.internalServerError(singletonList(errorResponse));

            case DATASET_NOT_FOUND:
                ErrorResponse datasetNotFound = ErrorResponse.valueOf(
                        "Dataset not found for id " + request.getDatasetId(), error.getErrorCode());
                return FcrResponse.badRequest(singletonList(datasetNotFound));

            default:
                return FcrResponse.internalServerError(singletonList(UNEXPECTED_ERROR_RESPONSE));
        }
    }

    private static void logStagingRequest(final @Valid StagingRequest request) {
        log.info("Start staging job [{}]", request.getName());

        if (log.isDebugEnabled()) {
            String stagingItemsDetails =
                    request.getItems().stream()
                            .map(JobsController::toStagingItemDetails)
                            .collect(joining("\n"));

            log.debug("Staging items:\n{}", stagingItemsDetails);
        }
    }

    private static String toStagingItemDetails(final StagingItemRequest item) {
        DatasetMetadata dataset = item.getDataset();
        return String.format(
                "  entity [%s] reference date [%s] and schema [%s]",
                dataset.getEntityCode(),
                dataset.getReferenceDate(),
                item.getSchema());
    }
}
