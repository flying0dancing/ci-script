package com.lombardrisk.ignis.server.controller.drillback;

import com.lombardrisk.ignis.client.external.drillback.DatasetRowDataView;
import com.lombardrisk.ignis.client.external.drillback.DrillBackStepDetails;
import com.lombardrisk.ignis.client.external.path.api;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.data.common.search.FilterExpression;
import com.lombardrisk.ignis.server.dataset.DatasetService;
import com.lombardrisk.ignis.server.dataset.drillback.DatasetExportService;
import com.lombardrisk.ignis.server.dataset.drillback.DatasetExportService.DatasetOutputStream;
import com.lombardrisk.ignis.server.dataset.drillback.DrillBackService;
import com.lombardrisk.ignis.server.dataset.model.Dataset;
import com.lombardrisk.ignis.server.util.converter.StringToFilterConverter;
import com.lombardrisk.ignis.web.common.response.FcrResponse;
import com.lombardrisk.ignis.web.common.response.ResponseUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.vavr.control.Try;
import io.vavr.control.Validation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.time.LocalDate;

import static com.lombardrisk.ignis.web.common.exception.GlobalExceptionHandler.UNEXPECTED_ERROR_RESPONSE;
import static java.util.Collections.singletonList;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@AllArgsConstructor
@Api(value = "drillback",
        description = "API to handle drillback operations, including redirect",
        tags = "Drillback API")
public class DrillBackController {

    private final RedirectService redirectService;
    private final DrillBackService drillBackService;
    private final DatasetExportService datasetExportService;
    private final DatasetService datasetService;

    @ApiIgnore
    @GetMapping(path = api.external.v1.drillBack.pipeline.step.byId, produces = APPLICATION_JSON_VALUE)
    public FcrResponse<DrillBackStepDetails> getDrillBackStepDetails(
            @PathVariable(api.Params.ID) final Long pipelineId,
            @PathVariable(api.Params.STEP_ID) final Long stepId) {

        log.debug("Get drillback detail for pipeline {} and step {} ", pipelineId, stepId);

        return drillBackService.findDrillBackStepDetails(pipelineId, stepId)
                .fold(FcrResponse::crudFailure, FcrResponse::okResponse);
    }

    @ApiIgnore
    @GetMapping(path = api.external.v1.drillBack.dataset.exportOnlyDrillback, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity exportOnlyDillBackDataset(
            @PathVariable(api.Params.DATASET_ID) final Long datasetId,
            @RequestParam(api.Params.PIPELINE_ID) final Long pipelineId,
            @RequestParam(api.Params.PIPELINE_STEP_ID) final Long pipelineStepId,
            @RequestParam(api.Params.OUTPUT_TABLE_ROWKEY) final Long outputTableRowKey,
            @SortDefault final Sort sort,
            @RequestParam(value = "search", defaultValue = "") final String search) {
        log.debug("Export dataset [{}] to csv", datasetId);
        FilterExpression filter = StringToFilterConverter.toFilter(search);

        return Try.withResources(ByteArrayOutputStream::new)
                .of(outputStream -> datasetExportService.exportOnlyDillBackDataset(
                        datasetId,
                        pipelineId,
                        pipelineStepId,
                        outputTableRowKey,
                        sort,
                        filter,
                        outputStream))
                .onFailure(throwable -> log.error("Failed to write dataset to csv", throwable))
                .mapTry(this::mapDatasetExportResult)
                .getOrElse(failedExportResponse());
    }

    @ApiIgnore
    @GetMapping(path = api.external.v1.drillBack.dataset.export, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity exportDataset(
            @PathVariable(api.Params.DATASET_ID) final Long datasetId,
            @SortDefault final Sort sort,
            @RequestParam(value = "search", defaultValue = "") final String search) {
        log.debug("Export dataset [{}] to csv", datasetId);
        FilterExpression filter = StringToFilterConverter.toFilter(search);

        return Try.withResources(ByteArrayOutputStream::new)
                .of(outputStream -> datasetExportService.exportDataset(datasetId, sort, filter, outputStream))
                .onFailure(throwable -> log.error("Failed to write dataset to csv", throwable))
                .mapTry(this::mapDatasetExportResult)
                .getOrElse(failedExportResponse());
    }

    @ApiResponses({ @ApiResponse(code = 301, message = "Redirect") })
    @GetMapping(path = api.external.v1.drillBack.Metadata, produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> redirectForPipelineDataset(
            @RequestParam(value = api.Params.ENTITY_CODE) final String entityCode,
            @RequestParam(value = api.Params.REFERENCE_DATE)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate referenceDate,
            @RequestParam(value = api.Params.RUN_KEY) final Long runKey,
            @RequestParam(value = api.Params.DATASET_NAME) final String datasetName) throws UnsupportedEncodingException {

        log.debug(
                "Redirect to drillback for dataset [{}], entityCode [{}], referenceDate [{}], runKey [{}]",
                datasetName,
                entityCode,
                referenceDate,
                runKey);

        Validation<CRUDFailure, Dataset> pipelineDataset =
                datasetService.findPipelineDataset(datasetName, runKey, referenceDate, entityCode);

        String newUrl;

        if (pipelineDataset.isValid()) {
            Dataset dataset = pipelineDataset.get();
            newUrl = redirectService.redirectToDrillback(dataset);
        } else {
            newUrl = redirectService.redirectToErrorPage(pipelineDataset.getError().toErrorResponse());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(newUrl));
        return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
    }

    @ApiIgnore
    @GetMapping(path = api.external.v1.drillBack.dataset.byId, produces = APPLICATION_JSON_VALUE, params = "type=output")
    public FcrResponse<DatasetRowDataView> getOutputDrillBackRowData(
            @PathVariable(api.Params.DATASET_ID) final Long datasetId,
            @PageableDefault(size = Integer.MAX_VALUE) final Pageable pageable,
            @RequestParam(value = "search", defaultValue = "") final String search) {

        log.debug("Get dataset row data for dataset {} and page {}", datasetId, pageable);

        FilterExpression filter = StringToFilterConverter.toFilter(search);
        return drillBackService.findDrillBackOutputDatasetRows(datasetId, pageable, filter)
                .fold(FcrResponse::crudFailure, FcrResponse::okResponse);
    }

    @ApiIgnore
    @GetMapping(path = api.external.v1.drillBack.dataset.byId, produces = APPLICATION_JSON_VALUE, params = "type=input")
    public FcrResponse<DatasetRowDataView> getInputDrillBackRowData(
            @PathVariable(api.Params.DATASET_ID) final Long datasetId,
            @RequestParam(api.Params.PIPELINE_ID) final Long pipelineId,
            @RequestParam(api.Params.PIPELINE_STEP_ID) final Long pipelineStepId,
            @RequestParam(value = "showOnlyDrillbackRows", defaultValue = "false") final Boolean showOnlyDrillbackRows,
            @RequestParam(value = "outputTableRowKey", required = false) final Long outputTableRowKey,
            @PageableDefault(size = Integer.MAX_VALUE) final Pageable pageable,
            @RequestParam(value = "search", defaultValue = "") final String search) {

        log.debug("Get dataset row data for dataset {} and page {}", datasetId, pageable);

        FilterExpression filter = StringToFilterConverter.toFilter(search);
        return drillBackService.findDrillBackInputDatasetRows(
                datasetId, pipelineId, pipelineStepId, pageable, showOnlyDrillbackRows, outputTableRowKey, filter)
                .fold(FcrResponse::crudFailure, FcrResponse::okResponse);
    }

    private ResponseEntity mapDatasetExportResult(
            final Validation<CRUDFailure, DatasetExportService.DatasetOutputStream<ByteArrayOutputStream>> validation) {

        if (validation.isInvalid()) {
            log.error("Could not export dataset {}", validation.getError().getErrorMessage());
            return ResponseEntity.badRequest()
                    .body(
                            singletonList(
                                    validation.getError().toErrorResponse()));
        }

        DatasetOutputStream<ByteArrayOutputStream> datasetOutputStream =
                validation.get();
        ByteArrayOutputStream productStream = datasetOutputStream.getOutputStream();

        return ResponseUtils.toDownloadResponseEntity(productStream, datasetOutputStream.getFileName(), "text/csv");
    }

    private static ResponseEntity failedExportResponse() {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(singletonList(UNEXPECTED_ERROR_RESPONSE));
    }
}

