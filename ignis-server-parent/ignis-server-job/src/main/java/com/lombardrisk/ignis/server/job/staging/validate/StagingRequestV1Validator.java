package com.lombardrisk.ignis.server.job.staging.validate;

import com.google.common.collect.ImmutableSet;
import com.lombardrisk.ignis.client.external.job.staging.request.v1.StagingItemRequest;
import com.lombardrisk.ignis.client.external.job.staging.request.v1.StagingRequest;
import com.lombardrisk.ignis.common.stream.CollectorUtils;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.server.job.staging.model.StagingDatasetInstruction;
import com.lombardrisk.ignis.server.job.staging.model.StagingInstructions;
import com.lombardrisk.ignis.server.product.table.model.Table;
import io.vavr.control.Validation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static com.lombardrisk.ignis.server.job.util.ReferenceDateConverter.convertReferenceDate;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;

@Slf4j
@AllArgsConstructor
@Deprecated
@SuppressWarnings("DeprecatedIsStillUsed")
public class StagingRequestV1Validator {

    private final StagingRequestCommonValidator commonValidator;

    @Transactional
    public Validation<List<CRUDFailure>, StagingInstructions> validate(final StagingRequest stagingRequest) {
        Validation<CRUDFailure, Set<String>> schemaValidation = commonValidator.validateDuplicateStagingSchemas(
                stagingRequest.getItems(), StagingItemRequest::getSchema);

        if (schemaValidation.isInvalid()) {
            return Validation.invalid(singletonList(schemaValidation.getError()));
        }

        Validation<List<CRUDFailure>, Set<StagingDatasetInstruction>> stagingInstructions =
                stagingRequest.getItems().stream()
                        .map(this::toStagingConfig)
                        .collect(CollectorUtils.groupValidations())
                        .map(ImmutableSet::copyOf);

        return stagingInstructions
                .map(stagingItems -> StagingInstructions.builder()
                        .jobName(stagingRequest.getName())
                        .stagingDatasetInstructions(stagingItems)
                        .downstreamPipelineInstructions(emptySet())
                        .build());
    }

    private Validation<CRUDFailure, StagingDatasetInstruction> toStagingConfig(
            final StagingItemRequest stagingItemRequest) {

        Validation<CRUDFailure, LocalDate> referenceDateValidation =
                convertReferenceDate(stagingItemRequest.getDataset().getReferenceDate());
        if (referenceDateValidation.isInvalid()) {
            return Validation.invalid(referenceDateValidation.getError());
        }

        Validation<CRUDFailure, Table> schemaValidation =
                commonValidator.validateSchemaDisplayName(stagingItemRequest.getSchema(), referenceDateValidation.get());

        if (schemaValidation.isInvalid()) {
            return Validation.invalid(schemaValidation.getError());
        }

        schemaValidation.get().getFields();
        StagingDatasetInstruction datasetInstruction = StagingDatasetInstruction.builder()
                .schema(schemaValidation.get())
                .entityCode(stagingItemRequest.getDataset().getEntityCode())
                .referenceDate(referenceDateValidation.get())
                .filePath(stagingItemRequest.getSource().getFilePath())
                .header(stagingItemRequest.getSource().isHeader())
                .autoValidate(stagingItemRequest.isAutoValidate())
                .build();

        return Validation.valid(datasetInstruction);
    }
}


