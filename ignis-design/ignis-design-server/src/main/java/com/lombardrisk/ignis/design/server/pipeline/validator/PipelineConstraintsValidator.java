package com.lombardrisk.ignis.design.server.pipeline.validator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.lombardrisk.ignis.client.external.pipeline.export.SchemaReference;
import com.lombardrisk.ignis.client.external.pipeline.export.TransformationType;
import com.lombardrisk.ignis.client.external.pipeline.export.step.JoinExport;
import com.lombardrisk.ignis.client.external.pipeline.export.step.JoinFieldExport;
import com.lombardrisk.ignis.client.external.pipeline.export.step.PipelineJoinStepExport;
import com.lombardrisk.ignis.client.external.pipeline.export.step.PipelineStepExport;
import com.lombardrisk.ignis.client.external.pipeline.export.step.select.SelectExport;
import com.lombardrisk.ignis.client.external.productconfig.export.FieldExport;
import com.lombardrisk.ignis.client.external.productconfig.export.SchemaExport;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.design.server.pipeline.api.PipelineRepository;
import com.lombardrisk.ignis.design.server.pipeline.model.Pipeline;
import io.vavr.Tuple;
import io.vavr.Tuple0;
import io.vavr.control.Option;
import io.vavr.control.Validation;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static com.lombardrisk.ignis.common.MapperUtils.mapSet;
import static com.lombardrisk.ignis.data.common.failure.CRUDFailure.constraintFailure;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;

@AllArgsConstructor
public class PipelineConstraintsValidator {

    private final PipelineRepository pipelineRepository;

    public List<CRUDFailure> validateNewPipeline(final String pipelineName) {
        Optional<Pipeline> existingPipeline = pipelineRepository.findByName(pipelineName);
        if (existingPipeline.isPresent()) {
            return singletonList(
                    constraintFailure("Pipeline '" + pipelineName + "' already exists"));
        }
        return emptyList();
    }

    public List<CRUDFailure> validateDuplicatePipelineNames(final List<String> pipelineNames) {
        List<CRUDFailure> duplicatePipelineNames = new ArrayList<>();
        Set<String> uniqueNames = new HashSet<>();

        for (String pipelineName : pipelineNames) {
            if (!uniqueNames.add(pipelineName)) {
                duplicatePipelineNames.add(
                        constraintFailure(String.format("Pipelines with same name [%s]", pipelineName)));
            }
        }

        return duplicatePipelineNames;
    }

    public List<CRUDFailure> validatePipelineStep(
            final PipelineStepExport stepExport, final List<SchemaExport> schemaExports) {

        List<CRUDFailure> stepInputFailures = validatePipelineStepInputSchemas(stepExport, schemaExports)
                .flatMap(noop -> validateJoinFields(stepExport, schemaExports))
                .fold(Collections::singletonList, tuple -> emptyList());

        List<CRUDFailure> stepOutputFailures = validatePipelineStepOutputSchema(stepExport, schemaExports)
                .fold(Collections::singletonList, tuple0 -> emptyList());

        return ImmutableList.<CRUDFailure>builder()
                .addAll(stepInputFailures)
                .addAll(stepOutputFailures)
                .build();
    }

    private Validation<CRUDFailure, Tuple0> validatePipelineStepInputSchemas(
            final PipelineStepExport pipelineStepExport, final List<SchemaExport> schemaExports) {

        Set<SchemaReference> missingStepInputSchemas = pipelineStepExport.getInputSchemas().stream()
                .filter(inputSchema -> schemaExports.stream()
                        .noneMatch(schemaExport -> matchesSchemaReference(schemaExport, inputSchema)))
                .collect(toSet());

        if (missingStepInputSchemas.isEmpty()) {
            return Validation.valid(Tuple.empty());
        }

        String missingInputSchemaNames = missingStepInputSchemas.stream()
                .map(SchemaReference::getPhysicalTableName)
                .collect(joining(", "));

        return Validation.invalid(constraintFailure(
                String.format("Pipeline step [%s] references invalid input schemas [%s]",
                        pipelineStepExport.getName(), missingInputSchemaNames)));
    }

    private Validation<CRUDFailure, Tuple0> validatePipelineStepOutputSchema(
            final PipelineStepExport pipelineStepExport, final List<SchemaExport> schemaExports) {

        SchemaReference stepOutputSchema = pipelineStepExport.getOutputSchema();

        Optional<SchemaExport> outputSchema = schemaExports.stream()
                .filter(schemaExport -> matchesSchemaReference(schemaExport, stepOutputSchema))
                .findFirst();

        Validation<CRUDFailure, SchemaExport> outputSchemaValidation = Option.ofOptional(outputSchema)
                .toValid(constraintFailure(
                        String.format("Pipeline step [%s] references invalid output schema [%s]",
                                pipelineStepExport.getName(), stepOutputSchema.getPhysicalTableName())));

        return outputSchemaValidation.flatMap(schema -> validatePipelineStepOutputFields(pipelineStepExport, schema));
    }

    private Validation<CRUDFailure, Tuple0> validatePipelineStepOutputFields(
            final PipelineStepExport stepExport, final SchemaExport stepOutputSchema) {

        Set<String> outputSchemaFieldNames =
                mapSet(stepOutputSchema.getFields(), FieldExport::getName);
        Set<String> pipelineStepOutputFieldNames =
                mapSet(stepExport.getOutputFieldSelects(), SelectExport::getOutputFieldName);

        Sets.SetView<String> invalidPipelineStepOutputFieldNames =
                Sets.difference(pipelineStepOutputFieldNames, outputSchemaFieldNames);

        if (invalidPipelineStepOutputFieldNames.isEmpty()) {
            return Validation.valid(Tuple.empty());
        }

        Set<String> fieldNames = invalidPipelineStepOutputFieldNames.stream()
                .map(fieldName -> String.format("%s.%s", stepOutputSchema.getPhysicalTableName(), fieldName))
                .collect(toSet());

        return Validation.invalid(invalidFieldsFailure(stepExport, fieldNames));
    }

    private Validation<CRUDFailure, Tuple0> validateJoinFields(
            final PipelineStepExport stepExport, final List<SchemaExport> schemaExports) {

        if (stepExport.getType() != TransformationType.JOIN) {
            return Validation.valid(Tuple.empty());
        }

        PipelineJoinStepExport joinStepExport = (PipelineJoinStepExport) stepExport;

        Set<String> invalidJoinFields = new HashSet<>();
        for (JoinExport join : joinStepExport.getJoins()) {
            SchemaExport leftSchema = findMatchingSchemaByReference(schemaExports, join.getLeft())
                    .getOrElseThrow(() -> new IllegalStateException("Left schema not found"));
            SchemaExport rightSchema = findMatchingSchemaByReference(schemaExports, join.getRight())
                    .getOrElseThrow(() -> new IllegalStateException("Right schema not found"));

            Set<String> invalidLeftFields = findMissingJoinFields(
                    leftSchema, join.getJoinFields(), JoinFieldExport::getLeftColumn);
            Set<String> invalidRightFields = findMissingJoinFields(
                    rightSchema, join.getJoinFields(), JoinFieldExport::getRightColumn);

            invalidJoinFields.addAll(invalidLeftFields);
            invalidJoinFields.addAll(invalidRightFields);
        }

        if (invalidJoinFields.isEmpty()) {
            return Validation.valid(Tuple.empty());
        }

        return Validation.invalid(invalidFieldsFailure(joinStepExport, invalidJoinFields));
    }

    private Set<String> findMissingJoinFields(
            final SchemaExport schemaExport,
            final List<JoinFieldExport> joinFieldExports,
            final Function<JoinFieldExport, String> joinFieldToString) {

        Set<String> schemaFieldNames = mapSet(schemaExport.getFields(), FieldExport::getName);
        Set<String> joinFieldNames = mapSet(joinFieldExports, joinFieldToString::apply);

        return Sets.difference(joinFieldNames, schemaFieldNames).stream()
                .map(field -> String.format("%s.%s", schemaExport.getPhysicalTableName(), field))
                .collect(toSet());
    }

    private Option<SchemaExport> findMatchingSchemaByReference(
            final List<SchemaExport> schemaExports, final SchemaReference schemaReference) {

        return Option.ofOptional(schemaExports.stream()
                .filter(schemaExport -> matchesSchemaReference(schemaExport, schemaReference))
                .findFirst());
    }

    private boolean matchesSchemaReference(final SchemaExport schemaExport, final SchemaReference schemaReference) {
        return schemaExport.getPhysicalTableName().equals(schemaReference.getPhysicalTableName())
                && schemaExport.getDisplayName().equals(schemaReference.getDisplayName())
                && schemaExport.getVersion().equals(schemaReference.getVersion());
    }

    private static CRUDFailure invalidFieldsFailure(
            final PipelineStepExport stepExport, final Set<String> invalidFields) {

        return constraintFailure(String.format("Pipeline step [%s] references invalid fields [%s]",
                stepExport.getName(), String.join(", ", invalidFields)));
    }
}
