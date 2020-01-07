package com.lombardrisk.ignis.server.product.pipeline;

import com.google.common.collect.ImmutableSet;
import com.lombardrisk.ignis.pipeline.step.api.DrillbackColumnLink;
import com.lombardrisk.ignis.server.product.pipeline.details.SchemaDetails;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineAggregationStep;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineJoinStep;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineMapStep;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineStep;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineUnionStep;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineWindowStep;
import com.lombardrisk.ignis.server.product.pipeline.select.Select;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductImportDiff;
import com.lombardrisk.ignis.server.product.table.model.Field;
import lombok.AllArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static com.lombardrisk.ignis.pipeline.step.api.DatasetFieldName.ROW_KEY;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toSet;

@AllArgsConstructor
public class PipelineStepService {

    private static final String BIG_INT_DATA_TYPE = "BIGINT";

    private final SelectJpaRepository selectJpaRepository;

    @Transactional
    public List<Select> getSelects(final PipelineStep step) {
        return selectJpaRepository.findAllByPipelineStepOrderByOrder(step);
    }

    @Transactional
    public Set<DrillbackColumnLink> getDrillbackColumnLinks(final PipelineStep step) {
        switch (step.getType()) {
            case MAP:
                return mapDrillbackColumns((PipelineMapStep) step);
            case AGGREGATION:
                return aggregationDrillbackColumns((PipelineAggregationStep) step);
            case JOIN:
                return joinDrillbackColumns((PipelineJoinStep) step);
            case WINDOW:
                return windowDrillbackColumns((PipelineWindowStep) step);
            case UNION:
                return unionDrillbackColumns((PipelineUnionStep) step);
            default:
                return emptySet();
        }
    }

    @Transactional
    public Set<DrillbackColumnLink> findAdditionalPhoenixDrillbackColumns(
            final PipelineStep pipelineStep, final ProductImportDiff productImportDiff) {

        Map<String, Map<String, Field>> physicalTableToFields = productImportDiff.physicalTableNameToFieldNameToField();

        switch (pipelineStep.getType()) {
            case MAP:
                PipelineMapStep pipelineMapStep = (PipelineMapStep) pipelineStep;
                return mapDrillbackColumns(pipelineMapStep);
            case JOIN:
                PipelineJoinStep pipelineJoinStep = (PipelineJoinStep) pipelineStep;
                return joinDrillbackColumns(pipelineJoinStep);
            case UNION:
                PipelineUnionStep pipelineUnionStep = (PipelineUnionStep) pipelineStep;
                return unionDrillbackColumns(pipelineUnionStep);
            case AGGREGATION:
                PipelineAggregationStep pipelineAggregationStep = (PipelineAggregationStep) pipelineStep;
                return aggregationDrillbackColumnDefinitions(pipelineAggregationStep, physicalTableToFields);
            case WINDOW:
                PipelineWindowStep pipelineWindowStep = (PipelineWindowStep) pipelineStep;
                return windowDrillbackColumnsDefinitions(pipelineWindowStep, physicalTableToFields);
            default:
                return emptySet();
        }
    }

    private Set<DrillbackColumnLink> aggregationDrillbackColumnDefinitions(
            final PipelineAggregationStep step, Map<String, Map<String, Field>> physicalTableToFields) {

        SchemaDetails schemaOut = step.getSchemaOut();
        SchemaDetails schemaIn = step.getSchemaIn();

        return step.getGroupings().stream()
                .map(grouping -> DrillbackColumnLink.builder()
                        .outputSchema(schemaOut.getPhysicalTableName())
                        .inputSchema(schemaIn.getPhysicalTableName())
                        .inputColumn(grouping)
                        .inputColumnSqlType(findGroupingField(physicalTableToFields, schemaIn, grouping)
                                .toColumnType())
                        .build())
                .collect(toSet());
    }

    private Set<DrillbackColumnLink> windowDrillbackColumnsDefinitions(
            final PipelineWindowStep step, final Map<String, Map<String, Field>> physicalTableToFields) {

        SchemaDetails schemaIn = step.getSchemaIn();
        SchemaDetails schemaOut = step.getSchemaOut();

        DrillbackColumnLink rowKeyDrillbackColumn = DrillbackColumnLink.builder()
                .outputSchema(schemaOut.getPhysicalTableName())
                .inputSchema(schemaIn.getPhysicalTableName())
                .inputColumn(ROW_KEY.name())
                .inputColumnSqlType(BIG_INT_DATA_TYPE)
                .build();

        List<Select> selects = selectJpaRepository.findAllWithWindowByPipelineStep(step);

        Set<DrillbackColumnLink> partitionDrillbackColumns = selects.stream()
                .filter(Select::isWindow)
                .flatMap(select -> select.getWindow().getPartitions().stream())
                .map(partition -> DrillbackColumnLink.builder()
                        .inputColumn(partition)
                        .inputSchema(schemaIn.getPhysicalTableName())
                        .outputSchema(schemaOut.getPhysicalTableName())
                        .inputColumnSqlType(
                                findGroupingField(physicalTableToFields, schemaIn, partition).toColumnType())
                        .build())
                .collect(toSet());

        return ImmutableSet.<DrillbackColumnLink>builder()
                .add(rowKeyDrillbackColumn)
                .addAll(partitionDrillbackColumns)
                .build();
    }

    private Field findGroupingField(
            final Map<String, Map<String, Field>> physicalTableToFields,
            final SchemaDetails schemaIn, final String grouping) {
        String physicalTableName = schemaIn.getPhysicalTableName();
        Map<String, Field> schemaFields = physicalTableToFields.get(physicalTableName);

        if (schemaFields == null) {
            throw new IllegalStateException("Could not find schema "
                    + physicalTableName
                    + " in product when looking for drillback grouping field");
        }

        Field field = schemaFields.get(grouping);
        if (field == null) {
            throw new IllegalArgumentException(
                    "Could not find grouping field " + grouping + " in schema " + physicalTableName);
        }
        return field;
    }

    private Set<DrillbackColumnLink> mapDrillbackColumns(final PipelineMapStep mapStep) {
        DrillbackColumnLink rowKeyDrillBack = rowKeyColumnLink()
                .outputSchema(mapStep.getSchemaOut().getPhysicalTableName())
                .inputSchema(mapStep.getSchemaIn().getPhysicalTableName())
                .build();

        return singleton(rowKeyDrillBack);
    }

    private Set<DrillbackColumnLink> aggregationDrillbackColumns(final PipelineAggregationStep aggregationStep) {
        return aggregationStep.getGroupings().stream()
                .map(grouping -> DrillbackColumnLink.builder()
                        .inputColumn(grouping)
                        .inputSchema(aggregationStep.getSchemaIn().getPhysicalTableName())
                        .outputSchema(aggregationStep.getSchemaOut().getPhysicalTableName())
                        .build())
                .collect(toSet());
    }

    private Set<DrillbackColumnLink> joinDrillbackColumns(final PipelineJoinStep joinStep) {
        return joinStep.getJoins().stream()
                .flatMap(join -> Stream.of(
                        rowKeyColumnLink()
                                .outputSchema(joinStep.getSchemaOut().getPhysicalTableName())
                                .inputSchema(join.getLeftSchema().getPhysicalTableName())
                                .build(),
                        rowKeyColumnLink()
                                .outputSchema(joinStep.getSchemaOut().getPhysicalTableName())
                                .inputSchema(join.getRightSchema().getPhysicalTableName())
                                .build()))
                .collect(toSet());
    }

    private Set<DrillbackColumnLink> windowDrillbackColumns(final PipelineWindowStep windowStep) {
        List<Select> windowSelects = selectJpaRepository.findAllWithWindowByPipelineStep(windowStep);

        Set<DrillbackColumnLink> partitionColumnsDrillback = windowSelects.stream()
                .filter(Select::isWindow)
                .flatMap(select -> select.getWindow().getPartitions().stream())
                .map(partition -> DrillbackColumnLink.builder()
                        .inputColumn(partition)
                        .inputSchema(windowStep.getSchemaIn().getPhysicalTableName())
                        .outputSchema(windowStep.getSchemaOut().getPhysicalTableName())
                        .build())
                .collect(toSet());

        if (partitionColumnsDrillback.isEmpty()) {
            DrillbackColumnLink rowKeyDrillBack = rowKeyColumnLink()
                    .outputSchema(windowStep.getSchemaOut().getPhysicalTableName())
                    .inputSchema(windowStep.getSchemaIn().getPhysicalTableName())
                    .build();

            return singleton(rowKeyDrillBack);
        }

        return partitionColumnsDrillback;
    }

    private Set<DrillbackColumnLink> unionDrillbackColumns(final PipelineUnionStep unionStep) {
        return unionStep.getInputs().stream()
                .map(schema -> rowKeyColumnLink()
                        .outputSchema(unionStep.getSchemaOut().getPhysicalTableName())
                        .inputSchema(schema.getPhysicalTableName())
                        .build())
                .collect(toSet());
    }

    private static DrillbackColumnLink.DrillbackColumnLinkBuilder rowKeyColumnLink() {
        return DrillbackColumnLink.builder()
                .inputColumn(ROW_KEY.name())
                .inputColumnSqlType(BIG_INT_DATA_TYPE);
    }
}
