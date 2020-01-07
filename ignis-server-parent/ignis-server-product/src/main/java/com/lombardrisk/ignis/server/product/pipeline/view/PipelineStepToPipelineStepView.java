package com.lombardrisk.ignis.server.product.pipeline.view;

import com.lombardrisk.ignis.client.external.pipeline.export.TransformationType;
import com.lombardrisk.ignis.client.external.pipeline.view.JoinView;
import com.lombardrisk.ignis.client.external.pipeline.view.PipelineStepView;
import com.lombardrisk.ignis.client.external.pipeline.view.select.OrderDirection;
import com.lombardrisk.ignis.client.external.pipeline.view.select.OrderView;
import com.lombardrisk.ignis.client.external.pipeline.view.select.WindowView;
import com.lombardrisk.ignis.common.MapperUtils;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineAggregationStep;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineJoinStep;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineMapStep;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineScriptletStep;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineStep;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineUnionStep;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineWindowStep;
import com.lombardrisk.ignis.server.product.pipeline.select.Order;
import com.lombardrisk.ignis.server.product.pipeline.select.Window;
import com.lombardrisk.ignis.server.product.pipeline.transformation.Join;
import com.lombardrisk.ignis.server.product.pipeline.transformation.JoinField;
import com.lombardrisk.ignis.server.product.table.view.FieldToFieldView;

import java.util.ArrayList;
import java.util.function.Function;

public class PipelineStepToPipelineStepView implements Function<PipelineStep, PipelineStepView> {

    private final FieldToFieldView fieldToFieldView = new FieldToFieldView();
    private final SchemaDetailsToSchemaDetailsView toSchemaView = new SchemaDetailsToSchemaDetailsView();

    public PipelineStepView apply(final PipelineStep pipelineStep) {
        switch (pipelineStep.getType()) {
            case MAP:
                return mapPipelineStep((PipelineMapStep) pipelineStep);
            case AGGREGATION:
                return aggregationPipelineStep((PipelineAggregationStep) pipelineStep);
            case JOIN:
                return joinPipelineStep((PipelineJoinStep) pipelineStep);
            case WINDOW:
                return windowPipelineStep((PipelineWindowStep) pipelineStep);
            case UNION:
                return unionPipelineStep((PipelineUnionStep) pipelineStep);
            case SCRIPTLET:
                return scriptletUnionStep((PipelineScriptletStep) pipelineStep);
            default:
                throw new IllegalStateException("Transformation type not supported: " + pipelineStep.getType());
        }
    }

    private PipelineStepView mapPipelineStep(final PipelineMapStep pipelineMapStep) {
        return PipelineStepView.builder()
                .id(pipelineMapStep.getId())
                .name(pipelineMapStep.getName())
                .schemaIn(toSchemaView.apply(pipelineMapStep.getSchemaIn()))
                .schemaOut(toSchemaView.apply(pipelineMapStep.getSchemaOut()))
                .type(TransformationType.MAP)
                .description(pipelineMapStep.getDescription())
                .build();
    }

    private PipelineStepView aggregationPipelineStep(final PipelineAggregationStep pipelineAggregationStep) {
        return PipelineStepView.builder()
                .id(pipelineAggregationStep.getId())
                .name(pipelineAggregationStep.getName())
                .schemaIn(toSchemaView.apply(pipelineAggregationStep.getSchemaIn()))
                .schemaOut(toSchemaView.apply(pipelineAggregationStep.getSchemaOut()))
                .groupings(new ArrayList<>(pipelineAggregationStep.getGroupings()))
                .type(TransformationType.AGGREGATION)
                .description(pipelineAggregationStep.getDescription())
                .build();
    }

    private PipelineStepView joinPipelineStep(final PipelineJoinStep pipelineJoinStep) {
        return PipelineStepView.builder()
                .id(pipelineJoinStep.getId())
                .name(pipelineJoinStep.getName())
                .schemaOut(toSchemaView.apply(pipelineJoinStep.getSchemaOut()))
                .type(TransformationType.JOIN)
                .description(pipelineJoinStep.getDescription())
                .joins(MapperUtils.map(pipelineJoinStep.getJoins(), this::toJoinView))
                .build();
    }

    private PipelineStepView windowPipelineStep(final PipelineWindowStep pipelineStep) {
        return PipelineStepView.builder()
                .id(pipelineStep.getId())
                .description(pipelineStep.getDescription())
                .name(pipelineStep.getName())
                .schemaIn(toSchemaView.apply(pipelineStep.getSchemaIn()))
                .schemaOut(toSchemaView.apply(pipelineStep.getSchemaOut()))
                .type(TransformationType.WINDOW)
                .build();
    }

    private PipelineStepView unionPipelineStep(final PipelineUnionStep pipelineStep) {
        return PipelineStepView.builder()
                .id(pipelineStep.getId())
                .name(pipelineStep.getName())
                .description(pipelineStep.getDescription())
                .type(TransformationType.UNION)
                .schemaOut(toSchemaView.apply(pipelineStep.getSchemaOut()))
                .unionInputSchemas(MapperUtils.mapCollectionOrEmpty(
                        pipelineStep.getInputs(),
                        toSchemaView::apply,
                        ArrayList::new))
                .build();
    }

    private PipelineStepView scriptletUnionStep(final PipelineScriptletStep pipelineStep) {
        return PipelineStepView.builder()
                .id(pipelineStep.getId())
                .name(pipelineStep.getName())
                .description(pipelineStep.getDescription())
                .type(TransformationType.SCRIPTLET)
                .jarFile(pipelineStep.getJarFile())
                .className(pipelineStep.getClassName())
                .schemaOut(toSchemaView.apply(pipelineStep.getSchemaOut()))
                .build();
    }

    private WindowView toWindowView(final Window window) {
        return WindowView.builder()
                .partitionBy(window.getPartitions())
                .orderBy(MapperUtils.map(window.getOrders(), this::toOrderView))
                .build();
    }

    private OrderView toOrderView(final Order order) {
        return OrderView.builder()
                .fieldName(order.getFieldName())
                .direction(OrderDirection.valueOf(order.getDirection().toString()))
                .priority(order.getPriority())
                .build();
    }

    private JoinView toJoinView(final Join join) {
        JoinField joinField = join.getJoinFields().iterator().next();
        return JoinView.builder()
                .id(join.getId())
                .left(toSchemaView.apply(join.getLeftSchema()))
                .leftField(fieldToFieldView.apply(joinField.getLeftJoinField()))
                .right(toSchemaView.apply(join.getRightSchema()))
                .rightField(fieldToFieldView.apply(joinField.getRightJoinField()))
                .build();
    }
}
