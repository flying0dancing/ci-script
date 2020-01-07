package com.lombardrisk.ignis.pipeline.step.common;

import com.google.common.collect.ImmutableList;
import com.lombardrisk.ignis.pipeline.step.api.DrillbackColumnLink;
import com.lombardrisk.ignis.pipeline.step.api.SelectColumn;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import java.util.List;
import java.util.Set;

import static com.lombardrisk.ignis.pipeline.step.api.DatasetFieldName.ROW_KEY;
import static org.apache.commons.collections.CollectionUtils.isEmpty;

@Slf4j
@AllArgsConstructor
public class MapStepExecutor {

    public Dataset<Row> runMap(
            final Iterable<SelectColumn> selects,
            final Set<String> filters,
            final String inputDatasetName,
            Dataset<Row> inputDataset) {

        if (filters != null && !isEmpty(filters)) {
            inputDataset = inputDataset.filter(String.join(" AND ", filters));
        }

        List<SelectColumn> selectColumns = ImmutableList.<SelectColumn>builder()
                .addAll(selects)
                .add(rowKeyDrillbackColumn(inputDatasetName))
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
}
