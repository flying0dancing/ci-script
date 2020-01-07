package com.lombardrisk.ignis.server.controller.external;

import com.lombardrisk.ignis.client.external.dataset.model.Dataset;
import com.lombardrisk.ignis.client.external.dataset.model.PagedDataset;
import com.lombardrisk.ignis.client.external.path.api;
import com.lombardrisk.ignis.client.external.rule.ValidationResultsDetailView;
import com.lombardrisk.ignis.server.dataset.DatasetService;
import com.lombardrisk.ignis.server.dataset.model.DatasetOnly;
import com.lombardrisk.ignis.server.dataset.model.DatasetQuery;
import com.lombardrisk.ignis.server.dataset.rule.ValidationResultDetailService;
import com.lombardrisk.ignis.server.dataset.rule.ValidationResultsSummaryService;
import com.lombardrisk.ignis.server.dataset.rule.view.ValidationResultsSummaryConverter;
import com.lombardrisk.ignis.server.dataset.view.DatasetConverter;
import com.lombardrisk.ignis.server.product.util.PageConverter;
import com.lombardrisk.ignis.web.common.response.FcrResponse;
import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

import static com.lombardrisk.ignis.client.external.path.api.Params;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.ObjectUtils.anyNotNull;

@RestController
@Slf4j
public class ExternalDatasetController {

    private final DatasetService datasetService;
    private final DatasetConverter datasetViewConverter;
    private final ValidationResultsSummaryService validationRuleSummaryService;
    private final ValidationResultsSummaryConverter validationResultsSummaryConverter;
    private final ValidationResultDetailService validationResultDetailService;
    private final PageConverter pageConverter;

    public ExternalDatasetController(
            final DatasetService datasetService,
            final DatasetConverter datasetViewConverter,
            final ValidationResultsSummaryService validationRuleSummaryService,
            final ValidationResultsSummaryConverter validationResultsSummaryConverter,
            final ValidationResultDetailService validationResultDetailService,
            final PageConverter pageConverter) {
        this.datasetService = datasetService;
        this.datasetViewConverter = datasetViewConverter;
        this.validationRuleSummaryService = validationRuleSummaryService;
        this.validationResultsSummaryConverter = validationResultsSummaryConverter;
        this.validationResultDetailService = validationResultDetailService;
        this.pageConverter = pageConverter;
    }

    @GetMapping(value = api.external.v1.Datasets)
    @Timed("ignis.get_all_datasets")
    public FcrResponse<PagedDataset> getAllDatasets(
            @RequestParam(value = Params.DATASET_NAME, required = false) final String datasetName,
            @RequestParam(value = Params.DATASET_SCHEMA, required = false) final String datasetSchema,
            @RequestParam(value = Params.REFERENCE_DATE, required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) final LocalDate referenceDate,
            @RequestParam(value = Params.ENTITY_CODE, required = false) final String entityCode,
            @PageableDefault(size = Integer.MAX_VALUE) final Pageable pageable)
            throws MissingServletRequestParameterException {

        log.trace("Find all datasets with name [{}] reference date [{}] entity code [{}] page [{}]",
                datasetName, referenceDate, entityCode, pageable);

        Page<Dataset> pagedDatasets;
        if (checkFilterQuery(datasetName, datasetSchema, referenceDate, entityCode)) {
            DatasetQuery datasetQuery = DatasetQuery.builder()
                    .name(datasetName)
                    .schema(datasetSchema)
                    .entityCode(entityCode)
                    .referenceDate(referenceDate)
                    .build();

            pagedDatasets = datasetService.findAllDatasets(datasetQuery, pageable)
                    .map(this::toDatasetView);
        } else {
            pagedDatasets = datasetService.findAllDatasets(pageable)
                    .map(this::toDatasetView);
        }

        PagedDataset pagedDataset = new PagedDataset(
                new PagedDataset.Embedded(pagedDatasets.getContent()),
                pageConverter.apply(pagedDatasets));

        return FcrResponse.okResponse(pagedDataset);
    }

    private Dataset toDatasetView(final DatasetOnly datasetOnly) {
        return datasetViewConverter.apply(datasetOnly);
    }

    @GetMapping(path = api.external.v1.datasets.byID.ValidationResultsSummaries)
    @Timed("ignis.get_rule_summaries")
    public ResponseEntity getRuleSummaries(
            @PathVariable(api.Params.ID) final long datasetId) {
        return ResponseEntity.ok()
                .body(validationRuleSummaryService.findByDatasetId(datasetId)
                        .stream()
                        .map(validationResultsSummaryConverter)
                        .collect(toList()));
    }

    @GetMapping(path = api.external.v1.datasets.byID.ValidationResultsDetails)
    @Timed("ignis.get_rule_details")
    public FcrResponse<ValidationResultsDetailView> getRuleDetails(
            @PathVariable(Params.ID) final long datasetId,
            @RequestParam(value = Params.RULE_ID, required = false) final Long ruleId,
            @PageableDefault(size = Integer.MAX_VALUE) final Pageable pageable) {

        if (ruleId == null) {
            return validationResultDetailService.findValidationDetails(datasetId, pageable)
                    .fold(FcrResponse::crudFailure, FcrResponse::okResponse);
        }
        return validationResultDetailService.findValidationDetails(datasetId, ruleId, pageable)
                .fold(FcrResponse::crudFailure, FcrResponse::okResponse);
    }

    private boolean checkFilterQuery(
            final String datasetName,
            final String datasetSchema,
            final LocalDate referenceDate,
            final String entityCode) throws MissingServletRequestParameterException {

        boolean filterQueryPresent = anyNotNull(datasetName, datasetSchema, referenceDate, entityCode);

        if (datasetName == null && datasetSchema == null && filterQueryPresent) {
            throw new MissingServletRequestParameterException(
                    Params.DATASET_NAME + "|" + Params.DATASET_SCHEMA, "String");
        }
        if (entityCode == null && filterQueryPresent) {
            throw new MissingServletRequestParameterException(Params.ENTITY_CODE, "String");
        }
        if (referenceDate == null && filterQueryPresent) {
            throw new MissingServletRequestParameterException(Params.REFERENCE_DATE, "Date");
        }
        return filterQueryPresent;
    }
}
