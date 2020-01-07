package com.lombardrisk.ignis.server.dataset.drillback;

import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.data.common.search.FilterExpression;
import com.lombardrisk.ignis.server.dataset.DatasetService;
import com.lombardrisk.ignis.server.dataset.model.Dataset;
import com.lombardrisk.ignis.server.dataset.result.DatasetResultRepository;
import com.lombardrisk.ignis.server.dataset.result.DatasetRowData;
import com.lombardrisk.ignis.server.product.pipeline.PipelineService;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineStep;
import com.lombardrisk.ignis.server.product.table.model.Field;
import io.vavr.control.Validation;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class DatasetExportService {

    private final Integer exportBatchSize;
    private final Integer exportSizeLimit;
    private final DatasetService datasetService;
    private final PipelineService pipelineService;
    private final DatasetResultRepository datasetResultRepository;

    public DatasetExportService(
            final Integer exportBatchSize,
            final Integer exportSizeLimit,
            final DatasetService datasetService,
            final PipelineService pipelineService,
            final DatasetResultRepository datasetResultRepository) {
        this.exportBatchSize = exportBatchSize;
        this.exportSizeLimit = exportSizeLimit;
        this.datasetService = datasetService;
        this.pipelineService = pipelineService;
        this.datasetResultRepository = datasetResultRepository;
    }

    @Transactional
    public <T extends OutputStream> Validation<CRUDFailure, DatasetOutputStream<T>> exportOnlyDillBackDataset(
            final Long datasetId,
            final Long pipelineId,
            final Long pipelineStepId,
            final Long outputTableRowKey,
            final Sort sort,
            final FilterExpression filter,
            final T outputStream) throws IOException {

        Validation<CRUDFailure, Dataset> withValidation = datasetService.findWithValidation(datasetId);
        if (withValidation.isInvalid()) {
            return Validation.invalid(withValidation.getError());
        }

        Dataset dataset = withValidation.get();
        Set<Field> fields = dataset.getSchema().getFields();

        Validation<CRUDFailure, PipelineStep> pipelineStep = pipelineService.findStepById(
                pipelineId, pipelineStepId);

        if (pipelineStep.isInvalid()) {
            return Validation.invalid(pipelineStep.getError());
        }

        DatasetRowData firstPageDatasetRowData =
                datasetResultRepository.findDrillbackInputDatasetRowData(dataset, pipelineStep.get(),
                        PageRequest.of(0, exportBatchSize, sort == null ? Sort.unsorted() : sort),
                        true, outputTableRowKey, filter);

        long totalElements = firstPageDatasetRowData.getResultData().getTotalElements();
        if (totalElements > exportSizeLimit) {
            log.error("Max export size [{}] exceeded with [{}]", exportSizeLimit, totalElements);
            return Validation.invalid(CRUDFailure.constraintFailure("Dataset export would contain more records ["
                    + totalElements
                    + "] than max export size allows [" + exportSizeLimit + "]"));
        }

        return paginateExport(sort, filter, outputStream, dataset, fields, firstPageDatasetRowData);
    }

    @Transactional
    public <T extends OutputStream> Validation<CRUDFailure, DatasetOutputStream<T>> exportDataset(
            final Long id,
            final Sort sort,
            final FilterExpression filter,
            final T outputStream) throws IOException {

        Validation<CRUDFailure, Dataset> withValidation = datasetService.findWithValidation(id);
        if (withValidation.isInvalid()) {
            return Validation.invalid(withValidation.getError());
        }

        Dataset dataset = withValidation.get();
        Set<Field> fields = dataset.getSchema().getFields();

        DatasetRowData firstPageDatasetRowData = datasetResultRepository.queryDataset(
                dataset, PageRequest.of(0, exportBatchSize, sort == null ? Sort.unsorted() : sort), filter);

        long totalElements = firstPageDatasetRowData.getResultData().getTotalElements();
        if (totalElements > exportSizeLimit) {
            log.error("Max export size [{}] exceeded with [{}]", exportSizeLimit, totalElements);
            return Validation.invalid(CRUDFailure.constraintFailure("Dataset export would contain more records ["
                    + totalElements
                    + "] than max export size allows [" + exportSizeLimit + "]"));
        }

        return paginateExport(sort, filter, outputStream, dataset, fields, firstPageDatasetRowData);
    }

    private <T extends OutputStream> Validation<CRUDFailure, DatasetOutputStream<T>> checkExportSizeLimit(
            final DatasetRowData firstPageDatasetRowData) {
        long totalElements = firstPageDatasetRowData.getResultData().getTotalElements();
        if (totalElements > exportSizeLimit) {
            log.error("Max export size [{}] exceeded with [{}]", exportSizeLimit, totalElements);
            return Validation.invalid(CRUDFailure.constraintFailure("Dataset export would contain more records ["
                    + totalElements
                    + "] than max export size allows [" + exportSizeLimit + "]"));
        }
        return null;
    }

    private <T extends OutputStream> Validation<CRUDFailure, DatasetOutputStream<T>> paginateExport(
            final Sort sort,
            final FilterExpression filter,
            final T outputStream,
            final Dataset dataset,
            final Set<Field> fields,
            final DatasetRowData firstPageDatasetRowData) throws IOException {

        ObjectWriter withHeaderWriter = new CsvMapper().writer()
                .with(generateCsvSchema(fields).withHeader());

        writePage(outputStream, withHeaderWriter, fields, firstPageDatasetRowData);

        int totalPages = firstPageDatasetRowData.getResultData().getTotalPages();
        if (totalPages == 1) {
            return Validation.valid(
                    new DatasetOutputStream(dataset.getSchema().getDisplayName(), outputStream));
        }

        writeRemainingPages(outputStream, dataset, fields, sort, filter, totalPages);

        return Validation.valid(
                new DatasetOutputStream<>(dataset.getSchema().getDisplayName(), outputStream));
    }

    private <T extends OutputStream> void writeRemainingPages(
            final T outputStream,
            final Dataset dataset,
            final Set<Field> fields,
            final Sort sort,
            final FilterExpression filter,
            final int totalPages) throws IOException {

        ObjectWriter withoutHeaderWriter = new CsvMapper().writer()
                .with(generateCsvSchema(fields).withoutHeader());
        for (int i = 1; i < totalPages; i++) {
            DatasetRowData datasetRowData = datasetResultRepository.queryDataset(
                    dataset,
                    PageRequest.of(i, exportBatchSize, sort == null ? Sort.unsorted() : sort),
                    filter);

            writePage(outputStream, withoutHeaderWriter, fields, datasetRowData);
        }
    }

    private <T extends OutputStream> void writePage(
            final T outputStream,
            final ObjectWriter objectWriter,
            final Set<Field> fields,
            final DatasetRowData datasetRowData) throws IOException {

        List<List<Object>> rows = datasetRowData.getResultData().stream()
                .map(resultDatum -> fields.stream()
                        .map(field -> resultDatum.get(field.getName()))
                        .collect(Collectors.toList())).collect(Collectors.toList());

        objectWriter.writeValues(outputStream)
                .write(rows);
    }

    private CsvSchema generateCsvSchema(final Set<Field> fields) {
        CsvSchema.Builder csvSchemaBuilder = CsvSchema.builder();
        for (Field field : fields) {
            csvSchemaBuilder.addColumn(field.getName());
        }
        return csvSchemaBuilder.build();
    }

    @Data
    public static class DatasetOutputStream<T extends OutputStream> {

        private final String fileName;
        private final T outputStream;

        public DatasetOutputStream(final String datasetName, final T t) {
            this.fileName = datasetName + ".csv";
            this.outputStream = t;
        }
    }
}
