package com.lombardrisk.ignis.pipeline.step.common;

import com.google.common.collect.ImmutableList;
import com.lombardrisk.ignis.pipeline.step.api.DrillbackColumnLink;
import com.lombardrisk.ignis.pipeline.step.api.SelectColumn;
import com.lombardrisk.ignis.pipeline.step.api.WindowSpec;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.lombardrisk.ignis.pipeline.step.api.DatasetFieldName.ROW_KEY;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

@Slf4j
@AllArgsConstructor
public class WindowStepExecutor {

    public Dataset<Row> runWindow(
            final Collection<SelectColumn> selects,
            final Set<String> filters,
            final String datasetName,
            Dataset<Row> inputDataset) {

        if (filters != null && !isEmpty(filters)) {
            inputDataset = inputDataset.filter(String.join(" AND ", filters));
        }
        List<SelectColumn> selectColumns = ImmutableList.<SelectColumn>builder()
                .addAll(selects)
                .add(rowKeyDrillbackColumn(datasetName))
                .addAll(partitionDrillbackColumns(selects, datasetName))
                .build();

        return new DependentOutputFieldsStepExecutor()
                .transform(selectColumns, inputDataset);
    }

    private SelectColumn rowKeyDrillbackColumn(final String inputDatasetName) {
        DrillbackColumnLink rowKeyDrillbackColumn = DrillbackColumnLink.builder()
                .inputColumn(ROW_KEY.name())
                .inputSchema(inputDatasetName)
                .build();

        return SelectColumn.builder()
                .select(rowKeyDrillbackColumn.getInputColumn())
                .as(rowKeyDrillbackColumn.toDrillbackColumn())
                .build();
    }

    private List<SelectColumn> partitionDrillbackColumns(
            final Collection<SelectColumn> selects,
            final String inputDatasetName) {
        return selects.stream()
                .map(SelectColumn::getOver)
                .flatMap(this::toPartitionStream)
                .map(partition -> toDrillBackColumnLink(partition, inputDatasetName))
                .map(link -> SelectColumn.builder()
                        .select(link.getInputColumn())
                        .as(link.toDrillbackColumn())
                        .build())
                .distinct()
                .collect(toList());
    }

    private Stream<String> toPartitionStream(final WindowSpec windowSpec) {
        if (windowSpec == null || windowSpec.getPartitionBy() == null) {
            return Stream.of();
        }

        return windowSpec.getPartitionBy().stream();
    }

    private DrillbackColumnLink toDrillBackColumnLink(final String partition, final String datasetName) {
        return DrillbackColumnLink.builder()
                .inputColumn(partition)
                .inputSchema(datasetName)
                .build();
    }
}
