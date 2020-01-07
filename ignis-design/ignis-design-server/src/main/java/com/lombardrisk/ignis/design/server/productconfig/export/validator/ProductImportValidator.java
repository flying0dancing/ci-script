package com.lombardrisk.ignis.design.server.productconfig.export.validator;

import com.google.common.collect.ImmutableList;
import com.lombardrisk.ignis.client.external.pipeline.export.PipelineExport;
import com.lombardrisk.ignis.client.external.productconfig.export.SchemaExport;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.design.server.pipeline.validator.PipelineConstraintsValidator;
import com.lombardrisk.ignis.design.server.productconfig.ProductConfig;
import com.lombardrisk.ignis.design.server.productconfig.api.ProductConfigRepository;
import com.lombardrisk.ignis.design.server.productconfig.export.ImportProductRequest;
import com.lombardrisk.ignis.design.server.productconfig.export.converter.SchemaExportConverter;
import com.lombardrisk.ignis.design.server.productconfig.schema.model.SchemaConstraints;
import com.lombardrisk.ignis.design.server.productconfig.schema.validation.SchemaConstraintsValidator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

public class ProductImportValidator {

    private final ProductConfigRepository productConfigRepository;
    private final SchemaConstraintsValidator createValidator;
    private final SchemaExportConverter schemaExportConverter;
    private final PipelineConstraintsValidator pipelineConstraintsValidator;

    public ProductImportValidator(
            final ProductConfigRepository productConfigRepository,
            final SchemaConstraintsValidator createValidator,
            final SchemaExportConverter schemaExportConverter,
            final PipelineConstraintsValidator pipelineConstraintsValidator) {
        this.productConfigRepository = productConfigRepository;
        this.createValidator = createValidator;
        this.schemaExportConverter = schemaExportConverter;
        this.pipelineConstraintsValidator = pipelineConstraintsValidator;
    }

    public List<CRUDFailure> validate(final ImportProductRequest importProductDto) {
        List<CRUDFailure> failures = new ArrayList<>();
        String productConfigName = importProductDto.getNewProductConfigRequest().getName();
        Optional<ProductConfig> findByName = productConfigRepository.findByName(productConfigName);

        if (findByName.isPresent()) {
            failures.add(
                    CRUDFailure.constraintFailure("Product already exists with name " + productConfigName));
        }

        List<CRUDFailure> schemaFailures = importProductDto.getSchemaExports()
                .stream()
                .map(this::toConstraints)
                .map(createValidator::validateCreate)
                .flatMap(Collection::stream)
                .collect(toList());

        List<CRUDFailure> pipelineFailures = this.validatePipelineExports(importProductDto);

        failures.addAll(schemaFailures);
        failures.addAll(pipelineFailures);

        return failures;
    }

    private SchemaConstraints toConstraints(final SchemaExport schemaExport) {
        return SchemaConstraints.builder()
                .physicalTableName(schemaExport.getPhysicalTableName())
                .displayName(schemaExport.getDisplayName())
                .majorVersion(schemaExport.getVersion())
                .startDate(schemaExportConverter.findStartDate(schemaExport))
                .endDate(schemaExportConverter.findEndDate(schemaExport))
                .build();
    }

    private List<CRUDFailure> validatePipelineExports(final ImportProductRequest importProductRequest) {
        List<String> pipelineNames = importProductRequest.getPipelineExports().stream()
                .map(PipelineExport::getName)
                .collect(toList());

        List<CRUDFailure> existingPipelineFailures = pipelineNames.stream()
                .map(pipelineConstraintsValidator::validateNewPipeline)
                .flatMap(Collection::stream)
                .collect(toList());

        List<CRUDFailure> duplicatePipelineFailures =
                pipelineConstraintsValidator.validateDuplicatePipelineNames(pipelineNames);

        List<CRUDFailure> invalidPipelineSteps = importProductRequest.getPipelineExports().stream()
                .flatMap(pipelineExport -> pipelineExport.getSteps().stream())
                .flatMap(pipelineStepExport -> pipelineConstraintsValidator.validatePipelineStep(
                        pipelineStepExport, importProductRequest.getSchemaExports()).stream())
                .collect(toList());

        return ImmutableList.<CRUDFailure>builder()
                .addAll(existingPipelineFailures)
                .addAll(duplicatePipelineFailures)
                .addAll(invalidPipelineSteps)
                .build();
    }
}
