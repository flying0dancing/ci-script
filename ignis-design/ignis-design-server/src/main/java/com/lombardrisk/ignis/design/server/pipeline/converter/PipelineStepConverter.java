package com.lombardrisk.ignis.design.server.pipeline.converter;

import com.lombardrisk.ignis.client.design.pipeline.PipelineStepView;
import com.lombardrisk.ignis.client.design.pipeline.aggregation.PipelineAggregationStepView;
import com.lombardrisk.ignis.client.design.pipeline.join.JoinFieldView;
import com.lombardrisk.ignis.client.design.pipeline.join.JoinView;
import com.lombardrisk.ignis.client.design.pipeline.join.PipelineJoinStepView;
import com.lombardrisk.ignis.client.design.pipeline.map.PipelineMapStepView;
import com.lombardrisk.ignis.client.design.pipeline.scriptlet.PipelineScriptletStepView;
import com.lombardrisk.ignis.client.design.pipeline.scriptlet.ScriptletInputView;
import com.lombardrisk.ignis.client.design.pipeline.select.OrderDirection;
import com.lombardrisk.ignis.client.design.pipeline.select.OrderView;
import com.lombardrisk.ignis.client.design.pipeline.select.SelectView;
import com.lombardrisk.ignis.client.design.pipeline.select.WindowView;
import com.lombardrisk.ignis.client.design.pipeline.union.PipelineUnionStepView;
import com.lombardrisk.ignis.client.design.pipeline.union.UnionView;
import com.lombardrisk.ignis.client.design.pipeline.window.PipelineWindowStepView;
import com.lombardrisk.ignis.common.MapperUtils;
import com.lombardrisk.ignis.common.stream.CollectorUtils;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineAggregationStep;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineJoinStep;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineMapStep;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineScriptletStep;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineStep;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineUnionStep;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineWindowStep;
import com.lombardrisk.ignis.design.server.pipeline.model.join.Join;
import com.lombardrisk.ignis.design.server.pipeline.model.join.JoinField;
import com.lombardrisk.ignis.design.server.pipeline.model.select.Order;
import com.lombardrisk.ignis.design.server.pipeline.model.select.PipelineFilter;
import com.lombardrisk.ignis.design.server.pipeline.model.select.Select;
import io.vavr.Function1;
import org.apache.commons.lang3.NotImplementedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.lombardrisk.ignis.common.MapperUtils.mapOrEmpty;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toSet;

public class PipelineStepConverter implements Function1<PipelineStep, PipelineStepView> {

    private static final long serialVersionUID = -3635343417152884290L;

    @Override
    public PipelineStepView apply(final PipelineStep pipelineStep) {
        switch (pipelineStep.getType()) {
            case MAP:
                return convertMapStep((PipelineMapStep) pipelineStep);
            case AGGREGATION:
                return convertAggregationStep((PipelineAggregationStep) pipelineStep);
            case JOIN:
                return convertJoinStep((PipelineJoinStep) pipelineStep);
            case WINDOW:
                return convertWindowStep((PipelineWindowStep) pipelineStep);
            case UNION:
                return convertUnionStep((PipelineUnionStep) pipelineStep);
            case SCRIPTLET:
                return convertScriptletStep((PipelineScriptletStep) pipelineStep);
            default:
                throw new NotImplementedException(pipelineStep.getType() + " not supported yet");
        }
    }

    private PipelineStepView convertMapStep(final PipelineMapStep pipelineStep) {
        return PipelineMapStepView.builder()
                .id(pipelineStep.getId())
                .name(pipelineStep.getName())
                .description(pipelineStep.getDescription())
                .selects(mapOrEmpty(pipelineStep.getSelects(), this::convertSelect))
                .filters(mapOrEmpty(pipelineStep.getFilters(), Function1.identity()))
                .schemaInId(pipelineStep.getSchemaInId())
                .schemaOutId(pipelineStep.getSchemaOutId())
                .build();
    }

    private PipelineUnionStepView convertUnionStep(final PipelineUnionStep pipelineStep) {
        return PipelineUnionStepView.builder()
                .id(pipelineStep.getId())
                .name(pipelineStep.getName())
                .description(pipelineStep.getDescription())
                .unions(toUnionView(pipelineStep))
                .schemaOutId(pipelineStep.getSchemaOutId())
                .build();
    }

    private Map<Long, UnionView> toUnionView(final PipelineUnionStep pipelineUnionStep) {
        Set<Select> selects = pipelineUnionStep.getSelects();
        Map<Long, List<SelectView>> unionSelectsMap = new HashMap<>();

        for (Select select : selects) {
            Long unionSchemaId = select.getUnion().getUnionSchemaId();
            if (!unionSelectsMap.containsKey(unionSchemaId)) {
                unionSelectsMap.put(unionSchemaId, new ArrayList<>());
            }

            unionSelectsMap.get(unionSchemaId).add(convertSelect(select));
        }

        Map<Long, List<String>> unionFilterMap = pipelineUnionStep.getPipelineFilters() == null
                ? emptyMap()
                : pipelineUnionStep.getPipelineFilters().stream()
                        .collect(CollectorUtils.toMultimapList(
                                PipelineFilter::getUnionSchemaId,
                                PipelineFilter::getFilter));

        return unionSelectsMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> UnionView.builder()
                                .selects(entry.getValue())
                                .filters(unionFilterMap.get(entry.getKey()))
                                .build()));
    }

    private PipelineStepView convertAggregationStep(final PipelineAggregationStep pipelineStep) {
        return PipelineAggregationStepView.builder()
                .id(pipelineStep.getId())
                .name(pipelineStep.getName())
                .description(pipelineStep.getDescription())
                .selects(mapOrEmpty(pipelineStep.getSelects(), this::convertSelect))
                .filters(mapOrEmpty(pipelineStep.getFilters(), Function1.identity()))
                .groupings(mapOrEmpty(pipelineStep.getGroupings(), Function1.identity()))
                .schemaInId(pipelineStep.getSchemaInId())
                .schemaOutId(pipelineStep.getSchemaOutId())
                .build();
    }

    private PipelineStepView convertJoinStep(final PipelineJoinStep pipelineStep) {
        return PipelineJoinStepView.builder()
                .id(pipelineStep.getId())
                .name(pipelineStep.getName())
                .description(pipelineStep.getDescription())
                .selects(mapOrEmpty(pipelineStep.getSelects(), this::convertSelect))
                .schemaOutId(pipelineStep.getSchemaOutId())
                .joins(MapperUtils.map(pipelineStep.getJoins(), this::toJoinView))
                .build();
    }

    private JoinView toJoinView(final Join join) {
        return JoinView.builder()
                .id(join.getId())
                .joinType(join.getJoinType())
                .leftSchemaId(join.getLeftSchemaId())
                .rightSchemaId(join.getRightSchemaId())
                .joinFields(MapperUtils.mapCollectionOrEmpty(join.getJoinFields(),
                        this::toJoinFieldView,
                        ArrayList::new))
                .build();
    }

    private JoinFieldView toJoinFieldView(final JoinField joinFields) {
        return JoinFieldView.builder()
                .id(joinFields.getId())
                .leftFieldId(joinFields.getLeftJoinFieldId())
                .rightFieldId(joinFields.getRightJoinFieldId())
                .build();
    }

    private PipelineStepView convertWindowStep(final PipelineWindowStep pipelineStep) {
        return PipelineWindowStepView.builder()
                .id(pipelineStep.getId())
                .name(pipelineStep.getName())
                .description(pipelineStep.getDescription())
                .schemaInId(pipelineStep.getSchemaInId())
                .schemaOutId(pipelineStep.getSchemaOutId())
                .selects(mapOrEmpty(pipelineStep.getSelects(), this::convertSelect))
                .filters(new ArrayList<>(pipelineStep.getFilters()))
                .build();
    }

    private SelectView convertSelect(final Select select) {
        return SelectView.builder()
                .outputFieldId(select.getOutputFieldId())
                .select(select.getSelect())
                .intermediateResult(select.isIntermediate())
                .hasWindow(select.isWindow())
                .order(select.isIntermediate() ? select.getOrder() : null)
                .window(select.isWindow() ? convertWindow(select) : null)
                .build();
    }

    private WindowView convertWindow(final Select select) {
        return WindowView.builder()
                .partitionBy(select.getWindow().getPartitions())
                .orderBy(MapperUtils.mapCollection(select.getWindow().getOrders(), this::convertOrder, ArrayList::new))
                .build();
    }

    private OrderView convertOrder(final Order order) {
        return OrderView.builder()
                .fieldName(order.getFieldName())
                .direction(OrderDirection.valueOf(order.getDirection().toString()))
                .priority(order.getPriority())
                .build();
    }

    private PipelineStepView convertScriptletStep(final PipelineScriptletStep pipelineStep) {
        Set<ScriptletInputView> scriptletInput = pipelineStep.getSchemaIns().stream()
                .map(input -> ScriptletInputView.builder()
                        .metadataInput(input.getInputName())
                        .schemaInId(input.getSchemaInId())
                        .build())
                .collect(toSet());

        return PipelineScriptletStepView.builder()
                .id(pipelineStep.getId())
                .name(pipelineStep.getName())
                .description(pipelineStep.getDescription())
                .jarFile(pipelineStep.getJarFile())
                .javaClass(pipelineStep.getClassName())
                .scriptletInputs(scriptletInput)
                .schemaOutId(pipelineStep.getSchemaOutId())
                .build();
    }
}
