package com.lombardrisk.ignis.pipeline.step.common;

import com.google.common.collect.ImmutableList;
import com.lombardrisk.ignis.pipeline.step.api.DrillbackColumnLink;
import com.lombardrisk.ignis.pipeline.step.api.SelectColumn;
import com.lombardrisk.ignis.pipeline.step.api.UnionSpec;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import scala.collection.JavaConversions;
import scala.collection.Seq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.lombardrisk.ignis.pipeline.step.api.DatasetFieldName.ROW_KEY;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

@Slf4j
@AllArgsConstructor
public class UnionStepExecutor {

    public Dataset<Row> runUnion(
            final Map<UnionSpec, Set<SelectColumn>> selectMap,
            final Map<UnionSpec, Set<String>> filterMap,
            final Map<String, Dataset<Row>> datasetLookup) {

        List<Dataset<Row>> toBeUnioned = new ArrayList<>();

        for (UnionSpec unionSpec : selectMap.keySet()) {
            Set<SelectColumn> selects = selectMap.get(unionSpec);
            Set<String> filters = filterMap.get(unionSpec);

            Dataset<Row> inputDataset = datasetLookup.get(unionSpec.getSchemaInPhysicalName());

            if (filters != null && !isEmpty(filters)) {
                inputDataset = inputDataset.filter(String.join(" AND ", filters));
            }

            List<SelectColumn> selectColumns = ImmutableList.<SelectColumn>builder()
                    .addAll(selects)
                    .addAll(drillbackColumns(selectMap, unionSpec))
                    .build();

            Dataset<Row> unionDataset = new DependentOutputFieldsStepExecutor().transform(selectColumns, inputDataset);
            Dataset<Row> unionDatasetWithOrderedColumns = reorderColumnsByName(unionDataset);

            toBeUnioned.add(unionDatasetWithOrderedColumns);
        }

        return toBeUnioned.stream()
                .reduce(Dataset::union)
                .orElseThrow(() -> new IllegalStateException("No datasets to union"));
    }

    private Dataset<Row> reorderColumnsByName(final Dataset<Row> dataset) {
        List<Column> columnsSortedByName = Arrays.stream(dataset.columns())
                .sorted()
                .map(dataset::col)
                .collect(Collectors.toList());

        Seq<Column> columnSeq = JavaConversions.asScalaIterator(columnsSortedByName.iterator()).toSeq();

        return dataset.select(columnSeq);
    }

    private List<SelectColumn> drillbackColumns(
            final Map<UnionSpec, Set<SelectColumn>> selectColumns,
            final UnionSpec inputSchema) {
        return selectColumns.keySet().stream()
                .map(schema -> toDrillbackColumn(inputSchema, schema)).collect(Collectors.toList());
    }

    private SelectColumn toDrillbackColumn(final UnionSpec inputSchema, final UnionSpec otherSchema) {
        DrillbackColumnLink inputDrillback = DrillbackColumnLink.builder()
                .inputColumn(ROW_KEY.name())
                .inputSchema(inputSchema.getSchemaInPhysicalName())
                .build();

        DrillbackColumnLink otherDrillback = DrillbackColumnLink.builder()
                .inputColumn(ROW_KEY.name())
                .inputSchema(otherSchema.getSchemaInPhysicalName())
                .build();

        if (inputSchema.equals(otherSchema)) {
            return SelectColumn.builder()
                    .select(inputDrillback.getInputColumn())
                    .as(inputDrillback.toDrillbackColumn())
                    .build();
        }

        return SelectColumn.builder()
                .select("NULL")
                .as(otherDrillback.toDrillbackColumn())
                .build();
    }
}
