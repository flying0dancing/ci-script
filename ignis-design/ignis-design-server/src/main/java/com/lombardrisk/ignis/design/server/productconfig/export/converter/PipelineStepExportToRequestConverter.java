package com.lombardrisk.ignis.design.server.productconfig.export.converter;

import com.lombardrisk.ignis.client.design.pipeline.PipelineStepRequest;
import com.lombardrisk.ignis.client.design.pipeline.aggregation.PipelineAggregationStepRequest;
import com.lombardrisk.ignis.client.design.pipeline.join.JoinFieldRequest;
import com.lombardrisk.ignis.client.design.pipeline.join.JoinRequest;
import com.lombardrisk.ignis.client.design.pipeline.join.PipelineJoinStepRequest;
import com.lombardrisk.ignis.client.design.pipeline.map.PipelineMapStepRequest;
import com.lombardrisk.ignis.client.design.pipeline.select.OrderDirection;
import com.lombardrisk.ignis.client.design.pipeline.select.OrderRequest;
import com.lombardrisk.ignis.client.design.pipeline.select.SelectRequest;
import com.lombardrisk.ignis.client.design.pipeline.select.UnionRequest;
import com.lombardrisk.ignis.client.design.pipeline.select.WindowRequest;
import com.lombardrisk.ignis.client.design.pipeline.union.PipelineUnionStepRequest;
import com.lombardrisk.ignis.client.design.pipeline.window.PipelineWindowStepRequest;
import com.lombardrisk.ignis.client.external.pipeline.export.SchemaReference;
import com.lombardrisk.ignis.client.external.pipeline.export.step.JoinExport;
import com.lombardrisk.ignis.client.external.pipeline.export.step.JoinFieldExport;
import com.lombardrisk.ignis.client.external.pipeline.export.step.PipelineAggregationStepExport;
import com.lombardrisk.ignis.client.external.pipeline.export.step.PipelineJoinStepExport;
import com.lombardrisk.ignis.client.external.pipeline.export.step.PipelineMapStepExport;
import com.lombardrisk.ignis.client.external.pipeline.export.step.PipelineStepExport;
import com.lombardrisk.ignis.client.external.pipeline.export.step.PipelineUnionStepExport;
import com.lombardrisk.ignis.client.external.pipeline.export.step.PipelineWindowStepExport;
import com.lombardrisk.ignis.client.external.pipeline.export.step.UnionExport;
import com.lombardrisk.ignis.client.external.pipeline.export.step.select.SelectExport;
import com.lombardrisk.ignis.client.external.pipeline.export.step.select.WindowExport;
import com.lombardrisk.ignis.design.field.model.Field;
import com.lombardrisk.ignis.design.server.productconfig.ProductConfig;
import com.lombardrisk.ignis.design.server.productconfig.schema.Schema;
import io.vavr.Function1;
import io.vavr.Tuple;
import io.vavr.Tuple2;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.lombardrisk.ignis.common.MapperUtils.map;
import static com.lombardrisk.ignis.common.MapperUtils.mapCollectionOrEmpty;
import static com.lombardrisk.ignis.common.MapperUtils.mapOrEmpty;

public class PipelineStepExportToRequestConverter
        implements BiFunction<PipelineStepExport, ProductConfig, PipelineStepRequest> {

    @Override
    public PipelineStepRequest apply(
            final PipelineStepExport stepExport, final ProductConfig productConfig) {

        Map<SchemaReference, Schema> schemaReferenceToSchema = schemaReferenceToSchema(productConfig);

        switch (stepExport.getType()) {
            case MAP:
                return toMapStepRequest((PipelineMapStepExport) stepExport, schemaReferenceToSchema);

            case AGGREGATION:
                return toAggregationStepRequest((PipelineAggregationStepExport) stepExport, schemaReferenceToSchema);

            case JOIN:
                return toJoinStepRequest((PipelineJoinStepExport) stepExport, schemaReferenceToSchema);

            case WINDOW:
                return toWindowStepRequest((PipelineWindowStepExport) stepExport, schemaReferenceToSchema);

            case UNION:
                return toUnionStepRequest((PipelineUnionStepExport) stepExport, schemaReferenceToSchema);

            default:
                throw new IllegalArgumentException("Unknown transformation type: " + stepExport.getType());
        }
    }

    private Map<SchemaReference, Schema> schemaReferenceToSchema(final ProductConfig productConfig) {
        return productConfig.getTables().stream()
                .collect(Collectors.toMap(this::toSchemaReference, Function.identity()));
    }

    private SchemaReference toSchemaReference(final Schema schema) {
        return SchemaReference.builder()
                .displayName(schema.getDisplayName())
                .physicalTableName(schema.getPhysicalTableName())
                .version(schema.getMajorVersion())
                .build();
    }

    private PipelineMapStepRequest toMapStepRequest(
            final PipelineMapStepExport stepExport, final Map<SchemaReference, Schema> schemas) {

        Schema inputSchema = schemas.get(stepExport.getSchemaIn());
        Schema outputSchema = schemas.get(stepExport.getSchemaOut());

        return PipelineMapStepRequest.builder()
                .name(stepExport.getName())
                .description(stepExport.getDescription())
                .schemaInId(inputSchema.getId())
                .schemaOutId(outputSchema.getId())
                .selects(mapCollectionOrEmpty(stepExport.getSelects(),
                        select -> toSelectRequest(outputSchema, select), LinkedHashSet::new))
                .filters(mapOrEmpty(stepExport.getFilters(), Function1.identity()))
                .build();
    }

    private PipelineAggregationStepRequest toAggregationStepRequest(
            final PipelineAggregationStepExport stepExport, final Map<SchemaReference, Schema> schemas) {

        Schema inputSchema = schemas.get(stepExport.getSchemaIn());
        Schema outputSchema = schemas.get(stepExport.getSchemaOut());

        return PipelineAggregationStepRequest.builder()
                .name(stepExport.getName())
                .description(stepExport.getDescription())
                .schemaInId(inputSchema.getId())
                .schemaOutId(outputSchema.getId())
                .selects(mapCollectionOrEmpty(stepExport.getSelects(),
                        select -> toSelectRequest(outputSchema, select), LinkedHashSet::new))
                .filters(mapOrEmpty(stepExport.getFilters(), Function1.identity()))
                .groupings(mapOrEmpty(stepExport.getGroupings(), Function1.identity()))
                .build();
    }

    private PipelineJoinStepRequest toJoinStepRequest(
            final PipelineJoinStepExport stepExport, final Map<SchemaReference, Schema> schemas) {

        Schema outputSchema = schemas.get(stepExport.getSchemaOut());

        return PipelineJoinStepRequest.builder()
                .name(stepExport.getName())
                .description(stepExport.getDescription())
                .schemaOutId(outputSchema.getId())
                .selects(mapCollectionOrEmpty(stepExport.getSelects(),
                        select -> toSelectRequest(outputSchema, select), LinkedHashSet::new))
                .joins(mapCollectionOrEmpty(stepExport.getJoins(),
                        joinExport -> toJoinRequest(joinExport, schemas), LinkedHashSet::new))
                .build();
    }

    private PipelineWindowStepRequest toWindowStepRequest(
            final PipelineWindowStepExport stepExport, final Map<SchemaReference, Schema> schemas) {

        Schema inputSchema = schemas.get(stepExport.getSchemaIn());
        Schema outputSchema = schemas.get(stepExport.getSchemaOut());

        return PipelineWindowStepRequest.builder()
                .name(stepExport.getName())
                .description(stepExport.getDescription())
                .schemaInId(inputSchema.getId())
                .schemaOutId(outputSchema.getId())
                .selects(mapCollectionOrEmpty(stepExport.getSelects(),
                        select -> toSelectRequest(outputSchema, select), LinkedHashSet::new))
                .build();
    }

    private PipelineUnionStepRequest toUnionStepRequest(
            final PipelineUnionStepExport stepExport, final Map<SchemaReference, Schema> schemas) {

        Schema outputSchema = schemas.get(stepExport.getSchemaOut());

        return PipelineUnionStepRequest.builder()
                .name(stepExport.getName())
                .description(stepExport.getDescription())
                .schemaOutId(outputSchema.getId())
                .unionSchemas(stepExport.getUnions().stream()
                        .map(union -> toUnionRequest(outputSchema, union, schemas))
                        .collect(Collectors.toMap(Tuple2::_1, Tuple2::_2)))
                .build();
    }

    private Tuple2<Long, UnionRequest> toUnionRequest(
            final Schema outputSchema, final UnionExport unionExport, final Map<SchemaReference, Schema> schemas) {

        Schema unionInSchema = schemas.get(unionExport.getUnionInSchema());

        return Tuple.of(unionInSchema.getId(), UnionRequest.builder()
                .selects(map(unionExport.getSelects(), select -> toSelectRequest(outputSchema, select)))
                .filters(mapOrEmpty(unionExport.getFilters(), Function1.identity()))
                .build());
    }

    private SelectRequest toSelectRequest(final Schema outputSchema, final SelectExport selectExport) {
        return SelectRequest.builder()
                .select(selectExport.getSelect())
                .outputFieldId(getField(outputSchema, selectExport.getOutputFieldName()).getId())
                .order(selectExport.getOrder())
                .intermediateResult(selectExport.isIntermediateResult())
                .hasWindow(selectExport.getIsWindow())
                .window(toWindowRequest(selectExport.getWindow()))
                .build();
    }

    private WindowRequest toWindowRequest(final WindowExport windowExport) {
        if (windowExport == null) {
            return null;
        }

        return WindowRequest.builder()
                .partitionBy(windowExport.getPartitionBy())
                .orderBy(windowExport.getOrderBy().stream()
                        .map(export -> OrderRequest.builder()
                                .fieldName(export.getFieldName())
                                .direction(OrderDirection.valueOf(export.getDirection()))
                                .priority(export.getPriority())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }

    private JoinRequest toJoinRequest(final JoinExport join, final Map<SchemaReference, Schema> schemas) {
        Schema leftSchema = schemas.get(join.getLeft());
        Schema rightSchema = schemas.get(join.getRight());

        return JoinRequest.builder()
                .joinType(join.getType())
                .leftSchemaId(leftSchema.getId())
                .rightSchemaId(rightSchema.getId())
                .joinFields(mapOrEmpty(join.getJoinFields(),
                        joinField -> toJoinFieldRequest(leftSchema, rightSchema, joinField)))
                .build();
    }

    private JoinFieldRequest toJoinFieldRequest(
            final Schema leftSchema, final Schema rightSchema, final JoinFieldExport joinFieldExport) {

        return JoinFieldRequest.builder()
                .leftFieldId(getField(leftSchema, joinFieldExport.getLeftColumn()).getId())
                .rightFieldId(getField(rightSchema, joinFieldExport.getRightColumn()).getId())
                .build();
    }

    private Field getField(final Schema schema, final String fieldName) {
        return schema.getFields().stream()
                .filter(field -> field.getName().equals(fieldName))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(String.format("Cannot find field [%s] on schema [%s]",
                        fieldName, schema.getPhysicalTableName())));
    }
}
