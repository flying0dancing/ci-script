package com.lombardrisk.ignis.design.server.pipeline.converter;

import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.design.field.model.Field;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineAggregationStep;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineJoinStep;
import com.lombardrisk.ignis.design.server.pipeline.model.join.Join;
import com.lombardrisk.ignis.design.server.pipeline.model.join.JoinField;
import com.lombardrisk.ignis.design.server.productconfig.schema.Schema;
import com.lombardrisk.ignis.pipeline.step.api.JoinAppConfig;
import com.lombardrisk.ignis.pipeline.step.api.JoinFieldConfig;
import com.lombardrisk.ignis.pipeline.step.api.SelectColumn;
import com.lombardrisk.ignis.pipeline.step.api.UnionSpec;
import com.lombardrisk.ignis.pipeline.step.common.AggregateTransformation;
import com.lombardrisk.ignis.pipeline.step.common.JoinTransformation;
import com.lombardrisk.ignis.pipeline.step.common.Transformation;
import io.vavr.control.Validation;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static com.lombardrisk.ignis.design.field.model.Field.FIELD_NAME;
import static java.util.stream.Collectors.toMap;

@UtilityClass
public class TransformationConverter {

    public static Transformation convertAggregationStep(
            final PipelineAggregationStep step,
            final Set<SelectColumn> selectColumns,
            final Schema inputSchema) {

        return AggregateTransformation.builder()
                .datasetName(inputSchema.getPhysicalTableName())
                .selects(selectColumns)
                .filters(step.getFilters())
                .groupings(step.getGroupings())
                .build();
    }

    public static UnionSpec convertUnionSpec(final String schemaName, final Long unionSchemaId) {
        return UnionSpec.builder()
                .schemaId(unionSchemaId)
                .schemaInPhysicalName(schemaName)
                .build();
    }

    public static Validation<List<ErrorResponse>, Transformation> convertJoinStep(
            final PipelineJoinStep step,
            final Set<SelectColumn> selectColumns,
            final List<Schema> inputSchemas) {

        Validation<List<ErrorResponse>, Set<JoinAppConfig>> joinValidation =
                validateJoinFields(step, inputSchemas);
        if (joinValidation.isInvalid()) {
            return Validation.invalid(joinValidation.getError());
        }

        return Validation.valid(JoinTransformation.builder()
                .joins(joinValidation.get())
                .selects(selectColumns)
                .build());
    }

    private static Validation<List<ErrorResponse>, Set<JoinAppConfig>> validateJoinFields(
            final PipelineJoinStep step, final List<Schema> inputSchemas) {

        List<ErrorResponse> errors = new ArrayList<>();

        Map<Long, Schema> inputSchemaIdToFields = inputSchemas.stream()
                .collect(toMap(Schema::getId, Function.identity()));

        Set<JoinAppConfig> joinDtos = new LinkedHashSet<>();

        for (Join join : step.getJoins()) {
            Schema leftSchema = inputSchemaIdToFields.get(join.getLeftSchemaId());
            if (leftSchema == null) {
                return Validation.invalid(Collections.singletonList(
                        CRUDFailure.notFoundIds("Schema", join.getLeftSchemaId())
                        .toErrorResponse()));
            }
            Schema rightSchema = inputSchemaIdToFields.get(join.getRightSchemaId());
            if (rightSchema == null) {
                return Validation.invalid(Collections.singletonList(
                        CRUDFailure.notFoundIds("Schema", join.getRightSchemaId())
                                .toErrorResponse()));
            }

            List<JoinFieldConfig> joinFields = new ArrayList<>();

            for (JoinField joinField : join.getJoinFields()) {
                Optional<Field> leftJoinField = leftSchema.getFields().stream()
                        .filter(field -> field.getId().equals(joinField.getLeftJoinFieldId()))
                        .findFirst();

                if (!leftJoinField.isPresent()) {
                    errors.add(CRUDFailure.cannotFind(FIELD_NAME)
                            .with(Schema.ENTITY_NAME, leftSchema.getDisplayName())
                            .with("Id", joinField.getLeftJoinFieldId())
                            .asFailure().toErrorResponse());
                }

                Optional<Field> rightJoinField = rightSchema.getFields().stream()
                        .filter(field -> field.getId().equals(joinField.getRightJoinFieldId()))
                        .findFirst();

                if (!rightJoinField.isPresent()) {
                    errors.add(CRUDFailure.cannotFind(FIELD_NAME)
                            .with(Schema.ENTITY_NAME, rightSchema.getDisplayName())
                            .with("Id", joinField.getRightJoinFieldId())
                            .asFailure().toErrorResponse());
                }
                if (rightJoinField.isPresent() && leftJoinField.isPresent()) {
                    joinFields.add(JoinFieldConfig.builder()
                            .leftJoinField(leftJoinField.get().getName())
                            .rightJoinField(rightJoinField.get().getName())
                            .build());
                }
            }

            joinDtos.add(JoinAppConfig.builder()
                    .joinType(JoinAppConfig.JoinType.valueOf(join.getJoinType().name()))
                    .leftSchemaName(leftSchema.getPhysicalTableName())
                    .rightSchemaName(rightSchema.getPhysicalTableName())
                    .joinFields(joinFields)
                    .build());
        }

        return errors.isEmpty()
                ? Validation.valid(joinDtos)
                : Validation.invalid(errors);
    }
}
