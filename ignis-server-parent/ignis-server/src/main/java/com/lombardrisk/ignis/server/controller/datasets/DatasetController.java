package com.lombardrisk.ignis.server.controller.datasets;

import com.lombardrisk.ignis.client.internal.CreateDatasetCall;
import com.lombardrisk.ignis.client.internal.RuleSummaryRequest;
import com.lombardrisk.ignis.client.internal.UpdateDatasetRunCall;
import com.lombardrisk.ignis.client.internal.path.api;
import com.lombardrisk.ignis.data.common.Identifiable;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.server.dataset.DatasetService;
import com.lombardrisk.ignis.server.dataset.model.Dataset;
import com.lombardrisk.ignis.server.dataset.rule.ValidationResultsSummaryService;
import com.lombardrisk.ignis.server.dataset.rule.model.ValidationResultsSummary;
import com.lombardrisk.ignis.server.job.util.ReferenceDateConverter;
import com.lombardrisk.ignis.spark.api.staging.DatasetProperties;
import com.lombardrisk.ignis.web.common.response.FcrResponse;
import io.vavr.control.Option;
import io.vavr.control.Validation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@Slf4j
public class DatasetController {

    private final DatasetService datasetService;
    private final ValidationResultsSummaryService validationRuleSummaryService;

    public DatasetController(
            final DatasetService datasetService,
            final ValidationResultsSummaryService validationRuleSummaryService) {
        this.datasetService = datasetService;
        this.validationRuleSummaryService = validationRuleSummaryService;
    }

    @ResponseBody
    @PostMapping(
            value = api.internal.Datasets,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public FcrResponse<Identifiable> createDataset(@RequestBody final CreateDatasetCall datasetCall) {
        log.info(
                "Create dataset as part of staging job [{}] for schema [{}] entity code [{}] reference date [{}]",
                datasetCall.getStagingJobId(),
                datasetCall.getSchemaId(),
                datasetCall.getEntityCode(),
                datasetCall.getReferenceDate());

        return datasetService.createDataset(datasetCall)
                .map(Identifiable::toIdentifiable)
                .fold(FcrResponse::crudFailure, FcrResponse::okResponse);
    }

    @ResponseBody
    @PatchMapping(
            value = api.internal.datasets.byId,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public FcrResponse<Identifiable> updateDatasetRun(
            @PathVariable(api.Params.ID) final Long datasetId,
            @RequestBody final UpdateDatasetRunCall datasetCall) {

        log.info(
                "Update dataset [{}] as part of staging job [{}] with records count [{}]",
                datasetId, datasetCall.getStagingJobId(), datasetCall.getRecordsCount());

        return datasetService.updateDatasetRun(datasetId, datasetCall)
                .map(Identifiable::toIdentifiable)
                .fold(FcrResponse::crudFailure, FcrResponse::okResponse);
    }

    /**
     * Deprecated api for getting latest dataset in AgileReporter versions 1.15.10 and under
     *
     * @param datasetName        Name of dataset
     * @param entityCodeField    Name of the entity code field in the metadata
     * @param entityCode         Entity code value
     * @param referenceDateField Name of the reference date field in the dataset's metadata
     * @param referenceDate      Reference date value
     * @return One dataset or throws exception if not found
     * @deprecated in favor of ExternalDatasetController getDatasets
     */
    @Deprecated
    @ResponseBody
    @RequestMapping(
            path = "api/datasets/{datasetName}/lastVersionForAR",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public FcrResponse<Dataset> getLastVersionOfDataset4AR(
            @PathVariable final String datasetName,
            @RequestParam("entityCodeField") final String entityCodeField,
            @RequestParam("entityCode") final String entityCode,
            @RequestParam("referenceDateField") final String referenceDateField,
            @RequestParam("referenceDate") final String referenceDate) {

        Validation<CRUDFailure, LocalDate> referenceDateResult =
                ReferenceDateConverter.convertReferenceDate(referenceDate);
        if (referenceDateResult.isInvalid()) {
            return FcrResponse.crudFailure(referenceDateResult.getError());
        }

        DatasetProperties datasetProperties = DatasetProperties.builder()
                .entityCode(entityCode)
                .referenceDate(referenceDateResult.get())
                .build();

        return datasetService.findLatestDataset(datasetName, datasetProperties)
                .fold(FcrResponse::crudFailure, FcrResponse::okResponse);
    }

    @PostMapping(path = api.internal.datasets.byID.ValidationResultsSummaries, consumes = APPLICATION_JSON_VALUE)
    public FcrResponse<List<Identifiable>> createRuleSummaries(
            @PathVariable(api.Params.ID) final long datasetId,
            @RequestBody final List<RuleSummaryRequest> rules) {
        Option<Dataset> datasetOptional = datasetService.findById(datasetId);
        if (!datasetOptional.isDefined()) {
            return FcrResponse.badRequest(datasetNotFound(datasetId));
        }

        return validationRuleSummaryService.create(datasetOptional.get(), rules)
                .map(this::handleValidationRuleSuccess)
                .fold(FcrResponse::crudFailure, FcrResponse::okResponse);
    }

    private List<Identifiable> handleValidationRuleSuccess(final List<ValidationResultsSummary> validationRuleSummaries) {
        return validationRuleSummaries.stream()
                        .map(Identifiable::toIdentifiable)
                        .collect(toList());
    }

    private ErrorResponse datasetNotFound(final long datasetId) {
        return ErrorResponse.valueOf("Dataset not found for id " + datasetId, "DATASET");
    }
}
