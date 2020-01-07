package com.lombardrisk.ignis.pipeline.step.common;

import com.lombardrisk.ignis.pipeline.step.api.OrderSpec;
import com.lombardrisk.ignis.pipeline.step.api.SelectColumn;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.expressions.Window;
import org.apache.spark.sql.expressions.WindowSpec;
import org.apache.spark.sql.functions;
import scala.collection.JavaConversions;
import scala.collection.mutable.ArrayBuffer;
import scala.collection.mutable.Buffer;

import java.util.List;
import java.util.stream.Collectors;

import static com.lombardrisk.ignis.common.stream.CollectionUtils.orEmpty;

@Slf4j
@AllArgsConstructor
public class DependentOutputFieldsStepExecutor {

    public Dataset<Row> transform(
            final List<SelectColumn> selectColumns,
            final Dataset<Row> input) {

        Dataset<Row> output = input;
        SQLContext sqlContext = output.sqlContext();


        List<SelectColumn> intermediateResults = selectColumns.stream()
                .filter(SelectColumn::isIntermediateResult)
                .collect(Collectors.toList());

        for (SelectColumn selectColumn : intermediateResults) {
            if (selectColumn.getOver() == null) {
                output = output.withColumn(selectColumn.getAs(), functions.expr(selectColumn.getSelect()));
            } else {
                output = output.withColumn(selectColumn.getAs(), functions.expr(selectColumn.getSelect())
                        .over(toWindowSpec(selectColumn)));
            }
        }

        Buffer<Column> outputFieldSelect = JavaConversions.asScalaBuffer(selectColumns
                .stream()
                .map(this::createColumnDefinition)
                .collect(Collectors.toList()));

        sqlContext.registerDataFrameAsTable(output, "IntermediateOutput");

        List<SelectColumn> adjustedSelects = selectColumns.stream().peek(select -> {
            if (select.isIntermediateResult()) {
                select.setSelect(select.getAs());
            }
        }).collect(Collectors.toList());

        return sqlContext.sql(
                MapWindowTransformation.builder()
                        .datasetName("IntermediateOutput")
                        .selects(adjustedSelects)
                        .build()
                        .toSparkSql());
    }

    private Column createColumnDefinition(final SelectColumn selectColumn) {
        if (selectColumn.isIntermediateResult()) {
            return new Column(selectColumn.getAs());
        }

        if (selectColumn.getOver() != null) {
            return functions.expr(selectColumn.getSelect())
                    .over(toWindowSpec(selectColumn))
                    .as(selectColumn.getAs());
        }

        return functions.expr(selectColumn.getSelect()).as(selectColumn.getAs());
    }

    private WindowSpec toWindowSpec(final SelectColumn selectColumn) {
        Buffer<Column> partitions =
                JavaConversions.asScalaBuffer(orEmpty(selectColumn.getOver().getPartitionBy())
                        .stream()
                        .map(Column::new)
                        .collect(Collectors.toList()));

        Buffer<Column> orderBys = findOrderBys(selectColumn);

        return Window.orderBy(orderBys)
                .partitionBy(partitions);
    }

    private Buffer<Column> findOrderBys(final SelectColumn selectColumn) {
        if (selectColumn.getOver().getOrderBy() == null) {
            return new ArrayBuffer<>();
        }
        return JavaConversions.asScalaBuffer(selectColumn.getOver().getOrderBy()
                .stream()
                .map(this::getDirection)
                .collect(Collectors.toList()));
    }

    private Column getDirection(final OrderSpec orderSpec) {
        return orderSpec.getDirection() == OrderSpec.Direction.ASC
                ? new Column(orderSpec.getColumn()).asc()
                : new Column(orderSpec.getColumn()).desc();
    }
}
