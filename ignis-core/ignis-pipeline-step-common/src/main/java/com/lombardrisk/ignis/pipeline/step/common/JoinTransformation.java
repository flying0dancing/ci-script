package com.lombardrisk.ignis.pipeline.step.common;

import com.google.common.collect.ImmutableList;
import com.lombardrisk.ignis.pipeline.step.api.DatasetFieldName;
import com.lombardrisk.ignis.pipeline.step.api.DrillbackColumnLink;
import com.lombardrisk.ignis.pipeline.step.api.JoinAppConfig;
import com.lombardrisk.ignis.pipeline.step.api.JoinFieldConfig;
import com.lombardrisk.ignis.pipeline.step.api.SelectColumn;
import io.vavr.control.Validation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.jgrapht.GraphTests;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.traverse.ClosestFirstIterator;
import org.jgrapht.traverse.DepthFirstIterator;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * Transformation that takes one row in the input to one row in the output.
 * Drillback is achieved by carrying over the row key from the input dataset to the output dataset.
 */
@Builder
@AllArgsConstructor
@Slf4j
public class JoinTransformation implements Transformation {

    private static final String JOIN = " JOIN ";

    private final Set<SelectColumn> selects;
    private final Set<JoinAppConfig> joins;
    private final String outputSchema;

    /**
     * Uses JGraphT library to check all the joined datasets are interconnected.
     * Uses closest first iteration to iterate over all edges
     *
     * Throws IllegalArgumentException if not connected.
     *
     * @return SQL as String
     */
    @Override
    public String toSparkSql() {
        DefaultDirectedGraph<String, JoinAppConfig> joinGraph = new DefaultDirectedGraph<>(JoinAppConfig.class);
        for (JoinAppConfig join : joins) {
            joinGraph.addVertex(join.getLeftSchemaName());
            joinGraph.addVertex(join.getRightSchemaName());
            joinGraph.addEdge(join.getLeftSchemaName(), join.getRightSchemaName(), join);
        }

        if (!GraphTests.isConnected(joinGraph)) {
            throw new IllegalArgumentException("Graph is not fully connected");
        }

        String joinSql = generateJoinSql(joinGraph);

        Validation<List<String>, String> sparkSql = selectStrings(selects)
                .map(s -> "SELECT " + String.join(", ", s) + ", " + drillbackColumnsSql() + " FROM " + joinSql);

        if (sparkSql.isInvalid()) {
            throw new IllegalArgumentException(String.join(", ", sparkSql.getError()));
        }

        return sparkSql.get();
    }

    private String drillbackColumnsSql() {
        Set<DrillbackColumnLink> drillbackLinks = new LinkedHashSet<>();

        for (JoinAppConfig join : joins) {
            drillbackLinks.add(DrillbackColumnLink.builder()
                    .outputSchema(outputSchema)
                    .inputSchema(join.getLeftSchemaName())
                    .inputColumn(DatasetFieldName.ROW_KEY.name())
                    .build());

            drillbackLinks.add(DrillbackColumnLink.builder()
                    .outputSchema(outputSchema)
                    .inputSchema(join.getRightSchemaName())
                    .inputColumn(DatasetFieldName.ROW_KEY.name())
                    .build());
        }

        return drillbackLinks.stream()
                .map(link -> link.getInputSchema()
                        + "."
                        + link.getInputColumn()
                        + " AS "
                        + link.toDrillbackColumn())
                .collect(Collectors.joining(", "));
    }

    private String generateJoinSql(final DefaultDirectedGraph<String, JoinAppConfig> joinGraph) {
        Set<String> usedSchemaNames = new HashSet<>();
        StringBuilder joinBuilder = new StringBuilder();

        List<String> startNodes = joinGraph.vertexSet().stream()
                .filter(vertex -> joinGraph.incomingEdgesOf(vertex).isEmpty())
                .collect(toList());

        if (startNodes.size() > 1) {
            throw new IllegalArgumentException("Cannot run a join with multiple start nodes " + startNodes);
        }

        Set<String> selfJoiningAppConfigs = joinGraph.edgeSet().stream()
                .filter(joinAppConfig -> joinAppConfig.getLeftSchemaName().equals(joinAppConfig.getRightSchemaName()))
                .map(joinAppConfig -> String.format(
                        "%s -> %s",
                        joinAppConfig.getLeftSchemaName(),
                        joinAppConfig.getRightSchemaName()))
                .collect(Collectors.toSet());

        if (!selfJoiningAppConfigs.isEmpty()) {
            throw new IllegalArgumentException("Self-join not supported for " + selfJoiningAppConfigs);
        }

        List<String> schemas = ImmutableList.copyOf(
                startNodes.isEmpty()
                        ? new DepthFirstIterator<>(joinGraph)
                        : new ClosestFirstIterator<>(joinGraph, startNodes.get(0))
        );

        for (String schemaToJoin : schemas) {
            Set<JoinAppConfig> edges = joinGraph.outgoingEdgesOf(schemaToJoin);
            for (JoinAppConfig join : edges) {

                //generate from statement and first join
                if (usedSchemaNames.isEmpty()) {

                    joinBuilder.append(firstJoinSqlStatement(join));
                    usedSchemaNames.add(join.getLeftSchemaName());
                    usedSchemaNames.add(join.getRightSchemaName());
                } else {
                    joinBuilder.append(joinToRight(join));
                }
            }
        }

        return joinBuilder.toString();
    }

    private String firstJoinSqlStatement(final JoinAppConfig joinAppConfig) {
        String joinOnFields = joinOnFields(joinAppConfig);

        return joinAppConfig.getLeftSchemaName() + " " + joinAppConfig.getJoinType().getSqlSyntax()
                + JOIN + joinAppConfig.getRightSchemaName()
                + joinOnFields;
    }

    private String joinToRight(final JoinAppConfig joinAppConfig) {
        String joinOnFields = joinOnFields(joinAppConfig);

        return " " + joinAppConfig.getJoinType().getSqlSyntax()
                + JOIN + joinAppConfig.getRightSchemaName() + joinOnFields;
    }

    private String joinOnFields(final JoinAppConfig joinAppConfig) {
        String joinOnFields = joinAppConfig.getJoinFields().stream()
                .map(joinField -> leftSideFieldSql(joinAppConfig, joinField)
                        + " = "
                        + rightSideFieldSql(joinAppConfig, joinField))
                .collect(Collectors.joining(" AND "));

        return " ON " + joinOnFields;
    }

    private String rightSideFieldSql(final JoinAppConfig joinAppConfig, final JoinFieldConfig joinField) {
        return joinAppConfig.getRightSchemaName() + "." + joinField.getRightJoinField();
    }

    private String leftSideFieldSql(final JoinAppConfig joinAppConfig, final JoinFieldConfig joinField) {
        return joinAppConfig.getLeftSchemaName() + "." + joinField.getLeftJoinField();
    }
}
