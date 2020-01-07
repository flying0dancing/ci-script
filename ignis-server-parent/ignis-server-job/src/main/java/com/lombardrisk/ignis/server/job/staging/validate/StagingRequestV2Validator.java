package com.lombardrisk.ignis.server.job.staging.validate;

import com.google.common.collect.ImmutableSet;
import com.lombardrisk.ignis.client.external.job.staging.request.v2.StagingItemRequestV2;
import com.lombardrisk.ignis.client.external.job.staging.request.v2.StagingRequestV2;
import com.lombardrisk.ignis.common.stream.CollectorUtils;
import com.lombardrisk.ignis.common.stream.SeqUtils;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.feature.FeatureNotActiveException;
import com.lombardrisk.ignis.feature.IgnisFeature;
import com.lombardrisk.ignis.server.dataset.DatasetService;
import com.lombardrisk.ignis.server.dataset.model.Dataset;
import com.lombardrisk.ignis.server.job.staging.model.DownstreamPipelineInstruction;
import com.lombardrisk.ignis.server.job.staging.model.StagingDatasetInstruction;
import com.lombardrisk.ignis.server.job.staging.model.StagingInstructions;
import com.lombardrisk.ignis.server.job.util.ReferenceDateConverter;
import com.lombardrisk.ignis.server.product.pipeline.PipelineService;
import com.lombardrisk.ignis.server.product.table.model.Table;
import io.vavr.collection.Seq;
import io.vavr.control.Validation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.togglz.core.manager.FeatureManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

@Slf4j
@AllArgsConstructor
public class StagingRequestV2Validator {

    private final StagingRequestCommonValidator commonValidator;
    private final FeatureManager featureManager;
    private final DatasetService datasetService;
    private final PipelineService pipelineService;

    @Transactional
    public Validation<List<CRUDFailure>, StagingInstructions> validate(final StagingRequestV2 request) {
        Validation<CRUDFailure, LocalDate> referenceDate =
                ReferenceDateConverter.convertReferenceDate(request.getMetadata().getReferenceDate());

        if (referenceDate.isInvalid()) {
            return Validation.invalid(singletonList(referenceDate.getError()));
        }

        Validation<List<CRUDFailure>, Set<StagingDatasetInstruction>> stagingItemsValidation =
                validateStagingItems(request, referenceDate.get());

        Validation<List<CRUDFailure>, Set<DownstreamPipelineInstruction>> downstreamPipelinesValidation =
                validateDownstreamPipelines(request, referenceDate.get());

        return Validation.combine(stagingItemsValidation, downstreamPipelinesValidation)
                .ap((stagingDatasets, downstreamPipelines) -> StagingInstructions.builder()
                        .jobName(request.getName())
                        .stagingDatasetInstructions(stagingDatasets)
                        .downstreamPipelineInstructions(downstreamPipelines)
                        .build())
                .mapError(SeqUtils::flatMapToList);
    }

    private Validation<List<CRUDFailure>, Set<StagingDatasetInstruction>> validateStagingItems(
            final StagingRequestV2 request, final LocalDate referenceDate) {

        Validation<CRUDFailure, Set<String>> schemasValidation =
                commonValidator.validateDuplicateStagingSchemas(request.getItems(), StagingItemRequestV2::getSchema);

        if (schemasValidation.isInvalid()) {
            return Validation.invalid(singletonList(schemasValidation.getError()));
        }

        String entityCode = request.getMetadata().getEntityCode();

        return request.getItems().stream()
                .map(item -> validateStagingItem(item, entityCode, referenceDate))
                .collect(CollectorUtils.groupCollectionValidations())
                .map(ImmutableSet::copyOf);
    }

    private Validation<List<CRUDFailure>, StagingDatasetInstruction> validateStagingItem(
            final StagingItemRequestV2 itemRequest,
            final String entityCode,
            final LocalDate referenceDate) {

        Validation<CRUDFailure, Table> schemaValidation =
                commonValidator.validateSchemaDisplayName(itemRequest.getSchema(), referenceDate);

        Validation<CRUDFailure, Dataset> appendToDatasetValidation =
                validateDatasetToAppendTo(itemRequest, entityCode, referenceDate);

        return Validation.combine(schemaValidation, appendToDatasetValidation)
                .ap((schema, appendToDataset) -> StagingDatasetInstruction.builder()
                        .schema(schema)
                        .entityCode(entityCode)
                        .referenceDate(referenceDate)
                        .appendToDataset(appendToDataset)
                        .filePath(itemRequest.getSource().getFilePath())
                        .header(itemRequest.getSource().isHeader())
                        .autoValidate(itemRequest.isAutoValidate())
                        .build())
                .mapError(Seq::asJava);
    }

    private Validation<CRUDFailure, Dataset> validateDatasetToAppendTo(
            final StagingItemRequestV2 stagingItemRequest, final String entityCode, final LocalDate referenceDate) {

        Long appendToDatasetId = stagingItemRequest.getAppendToDatasetId();

        if (appendToDatasetId == null) {
            return Validation.valid(null);
        }

        if (!featureManager.isActive(IgnisFeature.APPEND_DATASETS)) {
            log.warn(
                    "Attempted to append datasets in staging but {} feature not active",
                    IgnisFeature.APPEND_DATASETS.name());
            throw new FeatureNotActiveException(IgnisFeature.APPEND_DATASETS);
        }

        return datasetService.findWithValidation(appendToDatasetId)
                .flatMap(dataset -> validateDataset(dataset, stagingItemRequest, entityCode, referenceDate));
    }

    private Validation<CRUDFailure, Dataset> validateDataset(
            final Dataset dataset,
            final StagingItemRequestV2 stagingItemRequest,
            final String entityCode,
            final LocalDate referenceDate) {

        String stagingSchemaName = stagingItemRequest.getSchema();

        boolean entityCodeMatches = dataset.getEntityCode().equals(entityCode);
        boolean referenceDateMatches = dataset.getReferenceDate().equals(referenceDate);
        boolean schemaMatches = dataset.getSchema().getDisplayName().equals(stagingSchemaName);

        if (entityCodeMatches && referenceDateMatches && schemaMatches) {
            return Validation.valid(dataset);
        }

        return Validation.invalid(CRUDFailure.invalidParameters()
                .paramError("entityCode", entityCode)
                .paramError("referenceDate", referenceDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .paramError("schema", stagingSchemaName)
                .asFailure());
    }

    private Validation<List<CRUDFailure>, Set<DownstreamPipelineInstruction>> validateDownstreamPipelines(
            final StagingRequestV2 request, final LocalDate referenceDate) {

        if (isEmpty(request.getDownstreamPipelines())) {
            return Validation.valid(emptySet());
        }

        String entityCode = request.getMetadata().getEntityCode();

        return request.getDownstreamPipelines().stream()
                .map(pipelineName -> validateDownstreamPipeline(pipelineName, entityCode, referenceDate))
                .collect(CollectorUtils.groupValidations())
                .map(ImmutableSet::copyOf);
    }

    private Validation<CRUDFailure, DownstreamPipelineInstruction> validateDownstreamPipeline(
            final String pipelineName, final String entityCode, final LocalDate referenceDate) {

        return pipelineService.findByName(pipelineName)
                .map(pipeline -> DownstreamPipelineInstruction.builder()
                        .pipeline(pipeline)
                        .entityCode(entityCode)
                        .referenceDate(referenceDate)
                        .build());
    }
}
