package com.lombardrisk.ignis.design.server.pipeline.model;

import com.lombardrisk.ignis.design.server.pipeline.validator.transformation.dataframe.SparkTransformUtils;
import com.lombardrisk.ignis.design.server.productconfig.schema.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Singular;
import org.apache.commons.lang3.RandomUtils;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import scala.collection.JavaConversions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ExecuteStepContext {

    @Getter
    @Singular("inputSchema")
    private final List<Schema> inputSchemas;
    @Getter
    private final Schema outputSchema;

    private final Map<Schema, List<Row>> executionInputData;

    private final boolean runWithMockData;

    public static ExecuteStepContext mockData(final List<Schema> inputSchemas, final Schema outputSchema) {
        return new ExecuteStepContext(inputSchemas, outputSchema, null, true);
    }

    public static ExecuteStepContext realData(
            final List<Schema> inputSchemas,
            final Schema outputSchema,
            final Map<Schema, List<Row>> executionInputData) {
        return new ExecuteStepContext(inputSchemas, outputSchema, executionInputData, false);
    }

    public Map<Schema, List<Row>> getExecutionInputData() {
        if (runWithMockData) {
            return inputSchemas.stream()
                    .collect(Collectors.toMap(
                            Function.identity(),
                            schema -> singletonList(SparkTransformUtils.generateDummyRow(schema.getFields()))));
        }
        return executionInputData.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, this::addRowKey));
    }

    private List<Row> addRowKey(final Map.Entry<Schema, List<Row>> entry) {
        List<Row> adjustedRows = new ArrayList<>();
        for (Row row : entry.getValue()) {
            ArrayList<Object> rowValues = new ArrayList<>();
            rowValues.add(RandomUtils.nextLong());
            rowValues.addAll(JavaConversions.seqAsJavaList(row.toSeq()));
            adjustedRows.add(RowFactory.create(rowValues.toArray()));
        }
        return adjustedRows;
    }
}
