package com.lombardrisk.ignis.server.controller.staging;

import com.lombardrisk.ignis.api.dataset.DatasetState;
import com.lombardrisk.ignis.client.external.job.staging.StagingItemView;
import com.lombardrisk.ignis.client.external.path.api;
import com.lombardrisk.ignis.common.MapperUtils;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.server.config.swagger.ApiErrorResponses;
import com.lombardrisk.ignis.server.job.staging.StagingDatasetService;
import com.lombardrisk.ignis.server.job.staging.exception.DatasetStateChangeException;
import com.lombardrisk.ignis.server.job.staging.file.ErrorFileLinkOrStream;
import com.lombardrisk.ignis.server.job.staging.model.StagingDataset;
import com.lombardrisk.ignis.web.common.response.BadRequestErrorResponse;
import com.lombardrisk.ignis.web.common.response.FcrResponse;
import io.micrometer.core.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequestMapping(produces = APPLICATION_JSON_VALUE)
@ApiErrorResponses
@Api(value = "staging",
        description = "API to track the progress of staging data in FCR, each item is started by using the Jobs API",
        tags = "Staging API")
public class StagingController {

    private static final MediaType TEXT_CSV = MediaType.valueOf("text/csv");
    private final StagingDatasetConverter stagingDatasetConverter;
    private final StagingDatasetService stagingDatasetService;

    public StagingController(
            final StagingDatasetService stagingDatasetService,
            final StagingDatasetConverter stagingDatasetConverter) {
        this.stagingDatasetService = stagingDatasetService;
        this.stagingDatasetConverter = stagingDatasetConverter;
    }

    @GetMapping(api.external.v1.staging.ById)
    @Timed("ignis.find_staging_item")
    @ApiResponses({ @ApiResponse(code = 200, message = "OK", response = StagingItemView.class) })
    public FcrResponse<StagingItemView> findStagingItem(@PathVariable(api.Params.ID) final Long itemId) {
        log.info("Find staging dataset [{}]", itemId);

        return stagingDatasetService.findWithValidation(itemId)
                .map(stagingDatasetConverter)
                .fold(FcrResponse::crudFailure, FcrResponse::okResponse);
    }

    @GetMapping(api.external.v1.Staging)
    @Timed("ignis.find_staging_items")
    public FcrResponse<List<StagingItemView>> findStagingItems(
            @RequestParam(name = api.Params.JOB_ID, required = false) final Long jobExecutionId,
            @RequestParam(name = api.Params.DATASET_ID, required = false) final Long datasetId,
            @RequestParam(name = api.Params.ITEM_NAME, required = false) final String itemName) {

        return stagingDatasetService.findStagingDatasets(jobExecutionId, datasetId, itemName)
                .map(datasets -> MapperUtils.map(datasets, stagingDatasetConverter))
                .fold(FcrResponse::crudFailure, FcrResponse::okResponse);
    }

    @GetMapping(value = api.external.v1.staging.byID.ValidationErrorFile, produces = { "text/csv", "application/json" })
    @Timed("ignis.download_validation_error_file")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK", response = MultipartFile.class),
            @ApiResponse(code = 302, message = "FOUND", responseHeaders = {
                    @ResponseHeader(name = "Location", response = String.class, description = "Download forward url")
            })
    })
    public ResponseEntity downloadValidationErrorFile(
            @PathVariable(api.Params.ID) final long datasetId) throws IOException {

        return stagingDatasetService.downloadStagingErrorFile(datasetId)
                .fold(StagingController::createDatasetNotFoundError, StagingController::handleValidationFile);
    }

    @PutMapping(api.external.v1.staging.ById)
    @Timed("ignis.update_dataset_state")
    @ApiIgnore
    public StagingItemView updateDataSetState(
            @PathVariable(api.Params.ID) final long datasetId,
            @RequestParam(name = api.Params.STATE) final DatasetState state) throws DatasetStateChangeException {

        log.info("Update staging dataset [{}] to state [{}]", datasetId, state);

        StagingDataset stagingDataset = stagingDatasetService.updateStagingDatasetState(datasetId, state);
        return stagingDatasetConverter.apply(stagingDataset);
    }

    private static ResponseEntity handleValidationFile(final ErrorFileLinkOrStream validationFileLinkOrStream) {
        return validationFileLinkOrStream
                .fold(StagingController::forwardLink, StagingController::toInputStreamResource);
    }

    private static ResponseEntity forwardLink(final URL url) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", url.toString());
        return new ResponseEntity<String>(headers, HttpStatus.FOUND);
    }

    private static ResponseEntity toInputStreamResource(final String fileName, final InputStream inputStream) {
        InputStreamResource errorsFileResource = new InputStreamResource(inputStream);

        return ResponseEntity.ok()
                .contentType(TEXT_CSV)
                .header(CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .body(errorsFileResource);
    }

    private static ResponseEntity<List<ErrorResponse>> createDatasetNotFoundError(final CRUDFailure crudFailure) {
        return BadRequestErrorResponse.valueOf(
                crudFailure.getErrorMessage(),
                StagingErrorCodes.DATASET_ERRORS_FILE_NOT_FOUND);
    }
}
