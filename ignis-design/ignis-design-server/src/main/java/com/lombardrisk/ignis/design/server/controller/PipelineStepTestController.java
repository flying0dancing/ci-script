package com.lombardrisk.ignis.design.server.controller;

import com.lombardrisk.ignis.client.design.path.design;
import com.lombardrisk.ignis.client.design.pipeline.test.request.CreatePipelineStepDataRowRequest;
import com.lombardrisk.ignis.client.design.pipeline.test.request.CreateStepTestRequest;
import com.lombardrisk.ignis.client.design.pipeline.test.request.RowCellDataRequest;
import com.lombardrisk.ignis.client.design.pipeline.test.request.UpdateStepTestRequest;
import com.lombardrisk.ignis.client.design.pipeline.test.view.StepRowInputDataView;
import com.lombardrisk.ignis.client.design.pipeline.test.view.StepRowOutputDataView;
import com.lombardrisk.ignis.client.design.pipeline.test.view.StepTestRowView;
import com.lombardrisk.ignis.client.design.pipeline.test.view.StepTestView;
import com.lombardrisk.ignis.client.design.pipeline.test.view.TestRunResultView;
import com.lombardrisk.ignis.client.external.path.api;
import com.lombardrisk.ignis.data.common.Identifiable;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.data.common.utils.CsvUtils;
import com.lombardrisk.ignis.design.server.pipeline.test.PipelineStepTestExecuteService;
import com.lombardrisk.ignis.design.server.pipeline.test.PipelineStepTestRowService;
import com.lombardrisk.ignis.design.server.pipeline.test.PipelineStepTestService;
import com.lombardrisk.ignis.web.common.response.FcrResponse;
import com.lombardrisk.ignis.web.common.response.ResponseUtils;
import io.vavr.control.Try;
import io.vavr.control.Validation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.lombardrisk.ignis.web.common.exception.GlobalExceptionHandler.UNEXPECTED_ERROR_RESPONSE;
import static java.util.Collections.singletonList;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@AllArgsConstructor
@Slf4j
@RestController
public class PipelineStepTestController {

    private final PipelineStepTestService pipelineStepTestService;
    private final PipelineStepTestExecuteService pipelineStepTestExecuteService;
    private final PipelineStepTestRowService pipelineStepTestRowService;

    @PostMapping(path = design.api.v1.PipelineStepTests)
    public FcrResponse<Identifiable> createPipelineStepTest(
            @RequestBody final CreateStepTestRequest request) {
        log.info("Create pipeline step test {}", request.getName());

        return pipelineStepTestService.create(request)
                .map(stepTestView -> (Identifiable) stepTestView::getId)
                .fold(FcrResponse::crudFailure, FcrResponse::okResponse);
    }

    @GetMapping(path = design.api.v1.PipelineStepTests)
    public List<StepTestView> getPipelineStepTests(
            @RequestParam(value = "pipelineId") final Long pipelineId) {
        log.info("Find all step tests for pipeline {}", pipelineId);

        return pipelineStepTestService.findAllTestsByPipelineId(pipelineId);
    }

    @GetMapping(path = design.api.v1.pipelineStepTests.ById)
    public FcrResponse<StepTestView> getPipelineStepTest(
            @PathVariable(design.api.Params.TEST_ID) final Long testId) {
        log.info("Get step test by id {}", testId);

        return pipelineStepTestService.findById(testId)
                .fold(FcrResponse::crudFailure, FcrResponse::okResponse);
    }

    @PatchMapping(path = design.api.v1.pipelineStepTests.ById)
    public FcrResponse<StepTestView> updatePipelineStepTest(
            @PathVariable(design.api.Params.TEST_ID) final Long testId,
            @RequestBody final UpdateStepTestRequest request) {

        log.info("Updating test [{}]", testId);

        return pipelineStepTestService.update(testId, request)
                .fold(FcrResponse::crudFailure, FcrResponse::okResponse);
    }

    @DeleteMapping(path = design.api.v1.pipelineStepTests.ById)
    public FcrResponse<Identifiable> deletePipelineStepTest(
            @PathVariable(design.api.Params.TEST_ID) final Long testId) {

        log.info("Deleting test [{}]", testId);

        return pipelineStepTestService.deleteById(testId)
                .fold(FcrResponse::crudFailure, FcrResponse::okResponse);
    }

    @PostMapping(path = design.api.v1.pipelineStepTests.run)
    public FcrResponse<TestRunResultView> runTest(
            @PathVariable(design.api.Params.TEST_ID) final Long testId) {
        log.info("Running step test [{}]", testId);

        return pipelineStepTestService.runTest(testId)
                .fold(FcrResponse::badRequest, FcrResponse::okResponse);
    }

    @GetMapping(path = design.api.v1.pipelineStepTests.inputDataRow)
    public FcrResponse<Map<Long, List<StepRowInputDataView>>> getTestInputRows(
            @PathVariable(design.api.Params.TEST_ID) final Long testId) {

        log.info("Get input rows for test [{}]", testId);
        return pipelineStepTestService.getTestInputRows(testId)
                .fold(FcrResponse::crudFailure, FcrResponse::okResponse);
    }

    @GetMapping(path = design.api.v1.pipelineStepTests.outputDataRow)
    public FcrResponse<List<StepRowOutputDataView>> getTestOutputRows(
            @PathVariable(design.api.Params.TEST_ID) final Long testId) {

        log.info("Get output rows for test [{}]", testId);
        List<StepRowOutputDataView> outputRows = pipelineStepTestService.getTestOutputRows(testId);
        return FcrResponse.okResponse(outputRows);
    }

    @PostMapping(path = design.api.v1.pipelineStepTests.inputDataRow)
    public FcrResponse<StepTestRowView> createPipelineTestInputDataRow(
            @PathVariable(design.api.Params.TEST_ID) final Long testId,
            @RequestBody final CreatePipelineStepDataRowRequest request) {
        log.info("Create input row for test [{}]", testId);

        return pipelineStepTestRowService.createInputDataRow(testId, request.getSchemaId())
                .fold(FcrResponse::crudFailure, FcrResponse::okResponse);
    }

    @PostMapping(
            path = design.api.v1.pipelineStepTests.importCsv, params = "type=input",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public FcrResponse<List<StepTestRowView>> createPipelineTestImportCsvInputDataRow(
            @PathVariable(design.api.Params.TEST_ID) final Long testId,
            @PathVariable(design.api.Params.SCHEMA_ID) final Long schemaId,
            @RequestParam(api.Params.FILE) final MultipartFile file) throws IOException {

        log.info("Upload test rows file [{}] for test [{}] with input data row", file.getOriginalFilename(), testId);

        return pipelineStepTestRowService.importCsvInputDataRow(
                testId,
                schemaId,
                file.getInputStream())
                .fold(FcrResponse::crudFailure, FcrResponse::okResponse);
    }

    @PostMapping(
            path = design.api.v1.pipelineStepTests.importCsv, params = "type=expected",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public FcrResponse<List<StepTestRowView>> createPipelineTestImportCsvExpectedDataRow(
            @PathVariable(design.api.Params.TEST_ID) final Long testId,
            @PathVariable(design.api.Params.SCHEMA_ID) final Long schemaId,
            @RequestParam(api.Params.FILE) final MultipartFile file) throws IOException {

        log.info("Upload test rows file [{}] for test [{}] with expected data row", file.getOriginalFilename(), testId);

        return pipelineStepTestRowService.importCsvExpectedDataRow(
                testId,
                schemaId,
                file.getInputStream())
                .fold(FcrResponse::crudFailure, FcrResponse::okResponse);
    }

    @PostMapping(path = design.api.v1.pipelineStepTests.expectedDataRow)
    public FcrResponse<StepTestRowView> createPipelineTestExpectedDataRow(
            @PathVariable(design.api.Params.TEST_ID) final Long testId,
            @RequestBody final CreatePipelineStepDataRowRequest request) {
        log.info("Create expected row for test [{}]", testId);

        return pipelineStepTestRowService.createExpectedDataRow(testId, request.getSchemaId())
                .fold(FcrResponse::crudFailure, FcrResponse::okResponse);
    }

    @PatchMapping(path = design.api.v1.pipelineStepTests.rowCellDataByRowCellDataId)
    public FcrResponse<StepTestRowView> patchRowCellData(
            @PathVariable(design.api.Params.TEST_ID) final Long testId,
            @PathVariable(design.api.Params.ROW_ID) final Long rowId,
            @PathVariable(design.api.Params.ROW_CELL_DATA_ID) final Long rowCellDataId,
            @RequestBody final RowCellDataRequest request) {
        log.info("Update cell data for cell [{}], row [{}], test[{}]", rowCellDataId, rowId, testId);

        return pipelineStepTestRowService.updateCellValue(testId, rowId, rowCellDataId, request.getInputDataValue())
                .fold(FcrResponse::crudFailure, FcrResponse::okResponse);
    }

    @DeleteMapping(path = design.api.v1.pipelineStepTests.inputDataRowById)
    public FcrResponse<Identifiable> deleteInputDataRow(
            @PathVariable(design.api.Params.TEST_ID) final Long testId,
            @PathVariable(design.api.Params.ROW_ID) final Long rowId) {

        log.info("Deleting input row [{}] for test [{}]", rowId, testId);

        return pipelineStepTestRowService.deleteInput(rowId)
                .fold(FcrResponse::crudFailure, FcrResponse::okResponse);
    }

    @DeleteMapping(path = design.api.v1.pipelineStepTests.expectedDataRowById)
    public FcrResponse<Identifiable> deleteExpectedDataRow(
            @PathVariable(design.api.Params.TEST_ID) final Long testId,
            @PathVariable(design.api.Params.ROW_ID) final Long rowId) {

        log.info("Deleting expected row [{}] for test [{}]", rowId, testId);

        return pipelineStepTestRowService.deleteExpected(rowId)
                .fold(FcrResponse::crudFailure, FcrResponse::okResponse);
    }

    @GetMapping(path = design.api.v1.pipelineStepTests.exportCsv, params = "type=input",
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity exportPipelineTestCsvInputDataRow(
            @PathVariable(design.api.Params.TEST_ID) final Long testId,
            @PathVariable(design.api.Params.SCHEMA_ID) final Long schemaId) {

        log.info("Export csv file [{}] for test [{}] and schema [{}] input data row", testId, schemaId);

        return Try.withResources(ByteArrayOutputStream::new)
                .of(outputStream -> pipelineStepTestRowService.exportCsvInputDataRow(testId, schemaId, outputStream))
                .onFailure(throwable -> log.error("Failed to export csv", throwable))
                .mapTry(this::mapSchemaCsvDownloadValid)
                .getOrElse(failedExportResponse());
    }

    @GetMapping(path = design.api.v1.pipelineStepTests.exportCsv, params = "type=expected",
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity exportPipelineTestCsvExpectedDataRow(
            @PathVariable(design.api.Params.TEST_ID) final Long testId,
            @PathVariable(design.api.Params.SCHEMA_ID) final Long schemaId) {

        log.info("Export csv file [{}] for test [{}] and schema [{}] expected data row", testId, schemaId);

        return Try.withResources(ByteArrayOutputStream::new)
                .of(outputStream -> pipelineStepTestRowService.exportCsvExpectedDataRow(testId, schemaId, outputStream))
                .onFailure(throwable -> log.error("Failed to export csv", throwable))
                .mapTry(this::mapSchemaCsvDownloadValid)
                .getOrElse(failedExportResponse());
    }

    @GetMapping(path = design.api.v1.pipelineStepTests.exportCsv, params = "type=actual",
            produces = APPLICATION_JSON_VALUE)
    public ResponseEntity exportPipelineTestCsvActualDataRow(
            @PathVariable(design.api.Params.TEST_ID) final Long testId,
            @PathVariable(design.api.Params.SCHEMA_ID) final Long schemaId) {

        log.info("Export csv file [{}] for test [{}] and schema [{}] expected data row", testId, schemaId);

        return Try.withResources(ByteArrayOutputStream::new)
                .of(outputStream -> pipelineStepTestRowService.exportCsvActualDataRow(testId, schemaId, outputStream))
                .onFailure(throwable -> log.error("Failed to export csv", throwable))
                .mapTry(this::mapSchemaCsvDownloadValid)
                .getOrElse(failedExportResponse());
    }

    private ResponseEntity mapSchemaCsvDownloadValid(
            final Validation<CRUDFailure, CsvUtils.CsvOutputStream<ByteArrayOutputStream>> validation) {

        if (validation.isInvalid()) {
            return ResponseEntity.badRequest()
                    .body(
                            singletonList(
                                    validation.getError()));
        }

        return ResponseUtils.toDownloadResponseEntity(
                validation.get().getOutputStream(), validation.get().getFilename(), "text/csv");
    }

    private static ResponseEntity failedExportResponse() {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(singletonList(UNEXPECTED_ERROR_RESPONSE));
    }
}
