package com.lombardrisk.ignis.design.server.productconfig.export.converter;

import com.google.common.collect.ImmutableSet;
import com.lombardrisk.ignis.client.external.pipeline.export.PipelineExport;
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
import com.lombardrisk.ignis.client.external.pipeline.export.step.select.OrderExport;
import com.lombardrisk.ignis.client.external.pipeline.export.step.select.SelectExport;
import com.lombardrisk.ignis.client.external.pipeline.export.step.select.WindowExport;
import com.lombardrisk.ignis.common.MapperUtils;
import com.lombardrisk.ignis.common.stream.CollectorUtils;
import com.lombardrisk.ignis.design.field.model.Field;
import com.lombardrisk.ignis.design.server.pipeline.model.Pipeline;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineAggregationStep;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineJoinStep;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineMapStep;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineStep;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineUnionStep;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineWindowStep;
import com.lombardrisk.ignis.design.server.pipeline.model.join.Join;
import com.lombardrisk.ignis.design.server.pipeline.model.select.Order;
import com.lombardrisk.ignis.design.server.pipeline.model.select.PipelineFilter;
import com.lombardrisk.ignis.design.server.pipeline.model.select.Select;
import com.lombardrisk.ignis.design.server.pipeline.model.select.Window;
import com.lombardrisk.ignis.design.server.productconfig.ProductConfig;
import com.lombardrisk.ignis.design.server.productconfig.schema.Schema;
import io.vavr.Function1;
import io.vavr.control.Option;
import org.apache.commons.lang3.NotImplementedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.lombardrisk.ignis.common.MapperUtils.mapOrEmpty;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;

public class PipelineExportConverter implements BiFunction<Pipeline, ProductConfig, PipelineExport> {

    @Override
    public PipelineExport apply(final Pipeline pipeline, final ProductConfig productConfig) {
        Set<Schema> tables = Option.of(productConfig.getTables())
                .getOrElse(ImmutableSet.of());

        Map<Long, Schema> schemaIdsToMap = tables.stream()
                .collect(Collectors.toMap(Schema::getId, Function.identity()));

        return PipelineExport.builder()
                .name(pipeline.getName())
                .steps(MapperUtils.map(pipeline.getSteps(), step -> toPipelineStepExport(step, schemaIdsToMap)))
                .build();
    }

    private PipelineStepExport toPipelineStepExport(
            final PipelineStep pipelineStep,
            final Map<Long, Schema> schemaIdsToMap) {
        switch (pipelineStep.getType()) {
            case MAP:
                return toPipelineMapStepExport((PipelineMapStep) pipelineStep, schemaIdsToMap);
            case AGGREGATION:
                return toPipelineAggregationStepExport((PipelineAggregationStep) pipelineStep, schemaIdsToMap);
            case JOIN:
                return toPipelineJoinStepExport((PipelineJoinStep) pipelineStep, schemaIdsToMap);
            case WINDOW:
                return toPipelineWindowStepExport((PipelineWindowStep) pipelineStep, schemaIdsToMap);
            case UNION:
                return toPipelineUnionStepExport((PipelineUnionStep) pipelineStep, schemaIdsToMap);
            default:
                throw new NotImplementedException(pipelineStep.getType() + " is not implemented yet");
        }
    }

    private PipelineStepExport toPipelineUnionStepExport(
            final PipelineUnionStep pipelineStep,
            final Map<Long, Schema> schemaIdsToMap) {

        Schema schemaOut = schemaIdsToMap.get(pipelineStep.getSchemaOutId());

        return PipelineUnionStepExport.builder()
                .name(pipelineStep.getName())
                .description(pipelineStep.getDescription())
                .schemaOut(toSchemaReference(schemaOut))
                .unions(toUnionExport(pipelineStep, schemaIdsToMap))
                .build();
    }

    private List<UnionExport> toUnionExport(
            final PipelineUnionStep pipelineUnionStep,
            final Map<Long, Schema> schemaIdsToMap) {
        Schema schemaOut = schemaIdsToMap.get(pipelineUnionStep.getSchemaOutId());

        Map<Long, List<SelectExport>> schemaIdToSelect = pipelineUnionStep.getSelects().stream()
                .collect(CollectorUtils.toMultimapList(
                        select -> select.getUnion().getUnionSchemaId(),
                        select -> toSelectExport(select, schemaOut)));

        Map<Long, List<String>> unionFilterMap = pipelineUnionStep.getPipelineFilters() == null
                ? emptyMap()
                : pipelineUnionStep.getPipelineFilters().stream()
                        .collect(CollectorUtils.toMultimapList(
                                PipelineFilter::getUnionSchemaId,
                                PipelineFilter::getFilter));

        return schemaIdToSelect.entrySet().stream()
                .map(entry -> UnionExport.builder()
                        .selects(entry.getValue())
                        .filters(unionFilterMap.get(entry.getKey()))
                        .unionInSchema(toSchemaReference(schemaIdsToMap.get(entry.getKey())))
                        .build())
                .collect(toList());
    }

    private PipelineMapStepExport toPipelineMapStepExport(
            final PipelineMapStep pipelineStep,
            final Map<Long, Schema> schemaIdsToMap) {
        Schema schemaIn = schemaIdsToMap.get(pipelineStep.getSchemaInId());
        Schema schemaOut = schemaIdsToMap.get(pipelineStep.getSchemaOutId());

        return PipelineMapStepExport.builder()
                .name(pipelineStep.getName())
                .description(pipelineStep.getDescription())
                .selects(MapperUtils.mapCollectionOrEmpty(
                        pipelineStep.getSelects(),
                        select -> toSelectExport(select, schemaOut),
                        ArrayList::new))
                .filters(mapOrEmpty(pipelineStep.getFilters(), Function1.identity()))
                .schemaIn(toSchemaReference(schemaIn))
                .schemaOut(toSchemaReference(schemaOut))
                .build();
    }

    private PipelineAggregationStepExport toPipelineAggregationStepExport(
            final PipelineAggregationStep pipelineStep,
            final Map<Long, Schema> schemaIdsToMap) {

        Schema schemaIn = schemaIdsToMap.get(pipelineStep.getSchemaInId());
        Schema schemaOut = schemaIdsToMap.get(pipelineStep.getSchemaOutId());

        return PipelineAggregationStepExport.builder()
                .name(pipelineStep.getName())
                .description(pipelineStep.getDescription())
                .selects(MapperUtils.mapCollectionOrEmpty(
                        pipelineStep.getSelects(),
                        select -> toSelectExport(select, schemaOut),
                        ArrayList::new))
                .filters(mapOrEmpty(pipelineStep.getFilters(), Function1.identity()))
                .groupings(mapOrEmpty(pipelineStep.getGroupings(), Function1.identity()))
                .schemaIn(toSchemaReference(schemaIn))
                .schemaOut(toSchemaReference(schemaOut))
                .build();
    }

    private PipelineJoinStepExport toPipelineJoinStepExport(
            final PipelineJoinStep pipelineStep,
            final Map<Long, Schema> schemaIdsToMap) {

        Schema schemaOut = schemaIdsToMap.get(pipelineStep.getSchemaOutId());

        return PipelineJoinStepExport.builder()
                .name(pipelineStep.getName())
                .description(pipelineStep.getDescription())
                .selects(MapperUtils.mapCollectionOrEmpty(
                        pipelineStep.getSelects(),
                        select -> toSelectExport(select, schemaOut),
                        ArrayList::new))
                .schemaOut(toSchemaReference(schemaOut))
                .joins(MapperUtils.mapCollectionOrEmpty(
                        pipelineStep.getJoins(),
                        join -> convertJoinExport(join, schemaIdsToMap),
                        ArrayList::new))
                .build();
    }

    private PipelineStepExport toPipelineWindowStepExport(
            final PipelineWindowStep pipelineStep,
            final Map<Long, Schema> schemaIdsToMap) {

        Schema schemaIn = schemaIdsToMap.get(pipelineStep.getSchemaInId());
        Schema schemaOut = schemaIdsToMap.get(pipelineStep.getSchemaOutId());

        return PipelineWindowStepExport.builder()
                .name(pipelineStep.getName())
                .description(pipelineStep.getDescription())
                .schemaIn(toSchemaReference(schemaIn))
                .schemaOut(toSchemaReference(schemaOut))
                .filters(new ArrayList<>(pipelineStep.getFilters()))
                .selects(MapperUtils.mapCollectionOrEmpty(
                        pipelineStep.getSelects(),
                        select -> toSelectExport(select, schemaOut),
                        ArrayList::new))
                .build();
    }

    private SchemaReference toSchemaReference(final Schema schema) {
        return SchemaReference.builder()
                .physicalTableName(schema.getPhysicalTableName())
                .displayName(schema.getDisplayName())
                .version(schema.getMajorVersion())
                .build();
    }

    private SelectExport toSelectExport(final Select select, final Schema schemaOut) {
        return SelectExport.builder()
                .select(select.getSelect())
                .outputFieldName(findFieldName(schemaOut, select.getOutputFieldId()))
                .order(select.getOrder())
                .intermediateResult(select.isIntermediate())
                .isWindow(select.isWindow())
                .window(select.isWindow() ? toWindowExport(select.getWindow()) : null)
                .build();
    }

    private WindowExport toWindowExport(final Window window) {
        return WindowExport.builder()
                .partitionBy(window.getPartitions())
                .orderBy(MapperUtils.mapCollectionOrEmpty(window.getOrders(), this::toOrderExport, ArrayList::new))
                .build();
    }

    private OrderExport toOrderExport(final Order order) {
        return OrderExport.builder()
                .fieldName(order.getFieldName())
                .direction(order.getDirection().toString())
                .priority(order.getPriority())
                .build();
    }

    private JoinExport convertJoinExport(final Join join, final Map<Long, Schema> schemaIdsToMap) {
        Schema rightSchema = schemaIdsToMap.get(join.getRightSchemaId());
        Schema leftSchema = schemaIdsToMap.get(join.getLeftSchemaId());
        return JoinExport.builder()
                .right(toSchemaReference(rightSchema))
                .left(toSchemaReference(leftSchema))
                .type(join.getJoinType())
                .joinFields(MapperUtils.mapCollectionOrEmpty(
                        join.getJoinFields(),
                        joinField -> JoinFieldExport.builder()
                                .leftColumn(findFieldName(leftSchema, joinField.getLeftJoinFieldId()))
                                .rightColumn(findFieldName(rightSchema, joinField.getRightJoinFieldId()))
                                .build(),
                        ArrayList::new))
                .build();
    }

    private String findFieldName(final Schema schema, final Long fieldId) {
        return schema.getFields().stream()
                .filter(field -> field.getId().equals(fieldId))
                .map(Field::getName)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Could not find field on schema " + schema.getDisplayName() + " with id " + fieldId));
    }
}
