package com.lombardrisk.ignis.spark.pipeline.transformation;

import com.lombardrisk.ignis.pipeline.step.api.JoinAppConfig;
import com.lombardrisk.ignis.pipeline.step.api.JoinFieldConfig;
import com.lombardrisk.ignis.pipeline.step.api.SelectColumn;
import com.lombardrisk.ignis.pipeline.step.common.JoinTransformation;
import org.junit.Test;

import java.util.Arrays;

import static com.google.common.collect.Sets.newLinkedHashSet;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class JoinTransformationTest {

    @Test
    public void toSparkSql_ManyJoinColumns_RendersCorrectSql() {
        JoinTransformation joinTransformation = JoinTransformation.builder()
                .selects(newLinkedHashSet(asList(
                        SelectColumn.builder().select("LEFT.NAME").build(),
                        SelectColumn.builder().select("LEFT.JOB").build(),
                        SelectColumn.builder().select("RIGHT.SALARY").build(),
                        SelectColumn.builder().select("OTHER.TEST").build())))
                .joins(newLinkedHashSet(singletonList(
                        JoinAppConfig.builder()
                                .leftSchemaName("LEFT")
                                .rightSchemaName("RIGHT")
                                .joinType(JoinAppConfig.JoinType.INNER)
                                .joinFields(singletonList(JoinFieldConfig.builder()
                                        .leftJoinField("L_ID")
                                        .rightJoinField("R_ID")
                                        .build()))
                                .build())))
                .build();

        assertThat(
                joinTransformation.toSparkSql())
                .isEqualTo("SELECT LEFT.NAME, LEFT.JOB, RIGHT.SALARY, OTHER.TEST, "
                        + "LEFT.ROW_KEY AS FCR_SYS__LEFT__ROW_KEY, RIGHT.ROW_KEY AS FCR_SYS__RIGHT__ROW_KEY "
                        + "FROM LEFT "
                        + "INNER JOIN RIGHT ON LEFT.L_ID = RIGHT.R_ID");
    }

    @Test
    public void toSparkSql_GraphNotConnected_Errors() {
        JoinTransformation joinTransformation = JoinTransformation.builder()
                .selects(newLinkedHashSet(asList(
                        SelectColumn.builder().select("LEFT.NAME").build(),
                        SelectColumn.builder().select("LEFT.JOB").build(),
                        SelectColumn.builder().select("RIGHT.SALARY").build(),
                        SelectColumn.builder().select("OTHER.TEST").build(),
                        SelectColumn.builder().select("ANOTHER.TEST").build())))
                .joins(newLinkedHashSet(asList(
                        JoinAppConfig.builder()
                                .leftSchemaName("LEFT")
                                .rightSchemaName("RIGHT")
                                .joinType(JoinAppConfig.JoinType.INNER)
                                .joinFields(singletonList(JoinFieldConfig.builder()
                                        .leftJoinField("L_ID")
                                        .rightJoinField("R_ID")
                                        .build()))
                                .build(),
                        JoinAppConfig.builder()
                                .leftSchemaName("OTHER")
                                .rightSchemaName("ANOTHER")
                                .joinType(JoinAppConfig.JoinType.FULL_OUTER)
                                .joinFields(singletonList(JoinFieldConfig.builder()
                                        .leftJoinField("O_NAME")
                                        .rightJoinField("A_NAME")
                                        .build()))
                                .build()
                )))
                .build();

        assertThatThrownBy(joinTransformation::toSparkSql)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Graph is not fully connected");
    }

    @Test
    public void toSparkSql_ComplexJoin_RendersCorrectSql() {
        JoinTransformation joinTransformation = JoinTransformation.builder()
                .selects(newLinkedHashSet(asList(
                        SelectColumn.builder().select("EMPLOYEES.NAME").build(),
                        SelectColumn.builder().select("EMPLOYEES.JOB").build(),
                        SelectColumn.builder().select("RECORDS.SALARY").build(),
                        SelectColumn.builder().select("HR_PERSONAL.ADDRESS").build(),
                        SelectColumn.builder().select("COMMISSION.MONEY").build())))
                .joins(newLinkedHashSet(asList(
                        JoinAppConfig.builder()
                                .leftSchemaName("EMPLOYEES")
                                .rightSchemaName("RECORDS")
                                .joinType(JoinAppConfig.JoinType.INNER)
                                .joinFields(singletonList(JoinFieldConfig.builder()
                                        .leftJoinField("L_ID")
                                        .rightJoinField("R_ID")
                                        .build()))
                                .build(),
                        JoinAppConfig.builder()
                                .leftSchemaName("HR_PERSONAL")
                                .rightSchemaName("COMMISSION")
                                .joinType(JoinAppConfig.JoinType.FULL_OUTER)
                                .joinFields(singletonList(JoinFieldConfig.builder()
                                        .leftJoinField("COMM_ID")
                                        .rightJoinField("ID")
                                        .build()))
                                .build(),
                        JoinAppConfig.builder()
                                .leftSchemaName("RECORDS")
                                .rightSchemaName("HR_PERSONAL")
                                .joinType(JoinAppConfig.JoinType.LEFT)
                                .joinFields(singletonList(JoinFieldConfig.builder()
                                        .leftJoinField("HR_ID")
                                        .rightJoinField("ID")
                                        .build()))
                                .build()
                )))
                .build();

        String sqlResult = joinTransformation.toSparkSql();

        assertThat(sqlResult)
                .startsWith(
                        "SELECT EMPLOYEES.NAME, EMPLOYEES.JOB, RECORDS.SALARY, HR_PERSONAL.ADDRESS, COMMISSION.MONEY");
        assertThat(sqlResult)
                .contains("LEFT JOIN", "FULL OUTER JOIN", "INNER JOIN");
        assertThat(sqlResult)
                .contains(" EMPLOYEES ", " RECORDS ", " HR_PERSONAL ", " COMMISSION ");
        assertThat(sqlResult)
                .contains(
                        "LEFT JOIN HR_PERSONAL ON RECORDS.HR_ID = HR_PERSONAL.ID",
                        "FULL OUTER JOIN COMMISSION ON HR_PERSONAL.COMM_ID = COMMISSION.ID",
                        "INNER JOIN RECORDS ON EMPLOYEES.L_ID = RECORDS.R_ID");
    }

    @Test
    public void toSparkSql_ComplexJoinWithMultipleOnFields_RendersCorrectSql() {
        JoinTransformation joinTransformation = JoinTransformation.builder()
                .selects(newLinkedHashSet(asList(
                        SelectColumn.builder().select("EMPLOYEES.NAME").build(),
                        SelectColumn.builder().select("EMPLOYEES.JOB").build(),
                        SelectColumn.builder().select("RECORDS.SALARY").build(),
                        SelectColumn.builder().select("HR_PERSONAL.ADDRESS").build(),
                        SelectColumn.builder().select("COMMISSION.MONEY").build())))
                .joins(newLinkedHashSet(asList(
                        JoinAppConfig.builder()
                                .leftSchemaName("EMPLOYEES")
                                .rightSchemaName("RECORDS")
                                .joinType(JoinAppConfig.JoinType.INNER)
                                .joinFields(singletonList(JoinFieldConfig.builder()
                                        .leftJoinField("L_ID")
                                        .rightJoinField("R_ID")
                                        .build()))
                                .build(),
                        JoinAppConfig.builder()
                                .leftSchemaName("HR_PERSONAL")
                                .rightSchemaName("COMMISSION")
                                .joinType(JoinAppConfig.JoinType.FULL_OUTER)
                                .joinFields(Arrays.asList(
                                        JoinFieldConfig.builder()
                                                .leftJoinField("COMM_ID")
                                                .rightJoinField("ID")
                                                .build(),
                                        JoinFieldConfig.builder()
                                                .leftJoinField("COMM_STR")
                                                .rightJoinField("STR")
                                                .build()))
                                .build(),
                        JoinAppConfig.builder()
                                .leftSchemaName("RECORDS")
                                .rightSchemaName("HR_PERSONAL")
                                .joinType(JoinAppConfig.JoinType.LEFT)
                                .joinFields(Arrays.asList(
                                        JoinFieldConfig.builder()
                                                .leftJoinField("HR_ID")
                                                .rightJoinField("ID")
                                                .build(),
                                        JoinFieldConfig.builder()
                                                .leftJoinField("HS_ID")
                                                .rightJoinField("ID")
                                                .build()))
                                .build())))
                .build();

        String sqlResult = joinTransformation.toSparkSql();

        assertThat(sqlResult)
                .startsWith(
                        "SELECT EMPLOYEES.NAME, EMPLOYEES.JOB, RECORDS.SALARY, HR_PERSONAL.ADDRESS, COMMISSION.MONEY");
        assertThat(sqlResult)
                .contains("LEFT JOIN", "FULL OUTER JOIN", "INNER JOIN");
        assertThat(sqlResult)
                .contains(" EMPLOYEES ", " RECORDS ", " HR_PERSONAL ", " COMMISSION ");
        assertThat(sqlResult)
                .contains(
                        "LEFT JOIN HR_PERSONAL ON RECORDS.HR_ID = HR_PERSONAL.ID AND RECORDS.HS_ID = HR_PERSONAL.ID",
                        "FULL OUTER JOIN COMMISSION ON HR_PERSONAL.COMM_ID = COMMISSION.ID AND HR_PERSONAL.COMM_STR = COMMISSION.STR",
                        "INNER JOIN RECORDS ON EMPLOYEES.L_ID = RECORDS.R_ID");
    }

    @Test
    public void toSparkSql_JoinsHasTwoStartNodes_ThrowsException() {
        JoinTransformation joinTransformation = JoinTransformation.builder()
                .selects(newLinkedHashSet(asList(
                        SelectColumn.builder().select("A.NAME").build(),
                        SelectColumn.builder().select("B.NAME").build(),
                        SelectColumn.builder().select("C.NAME").build(),
                        SelectColumn.builder().select("D.NAME").build())))
                .joins(newLinkedHashSet(asList(
                        JoinAppConfig.builder()
                                .leftSchemaName("A")
                                .rightSchemaName("B")
                                .joinType(JoinAppConfig.JoinType.INNER)
                                .joinFields(singletonList(JoinFieldConfig.builder()
                                        .leftJoinField("A_ID")
                                        .rightJoinField("B_ID")
                                        .build()))
                                .build(),
                        JoinAppConfig.builder()
                                .leftSchemaName("C")
                                .rightSchemaName("D")
                                .joinType(JoinAppConfig.JoinType.FULL_OUTER)
                                .joinFields(singletonList(JoinFieldConfig.builder()
                                        .leftJoinField("C_ID")
                                        .rightJoinField("D_ID")
                                        .build()))
                                .build(),
                        JoinAppConfig.builder()
                                .leftSchemaName("C")
                                .rightSchemaName("B")
                                .joinType(JoinAppConfig.JoinType.LEFT)
                                .joinFields(singletonList(JoinFieldConfig.builder()
                                        .leftJoinField("CID")
                                        .rightJoinField("B_ID")
                                        .build()))
                                .build(),
                        JoinAppConfig.builder()
                                .leftSchemaName("A")
                                .rightSchemaName("D")
                                .joinType(JoinAppConfig.JoinType.LEFT)
                                .joinFields(singletonList(JoinFieldConfig.builder()
                                        .leftJoinField("A_ID")
                                        .rightJoinField("D_ID")
                                        .build()))
                                .build())))
                .build();

        assertThatThrownBy(joinTransformation::toSparkSql)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot run a join with multiple start nodes [A, C]");
    }

    @Test
    public void toSparkSql_SourceSchemaIsLastJoin_StartsWithNodeWithNoIncomingEdges() {
        JoinTransformation joinTransformation = JoinTransformation.builder()
                .selects(newLinkedHashSet(asList(
                        SelectColumn.builder().select("A.NAME").build(),
                        SelectColumn.builder().select("B.NAME").build(),
                        SelectColumn.builder().select("C.NAME").build(),
                        SelectColumn.builder().select("D.NAME").build(),
                        SelectColumn.builder().select("E.NAME").build())))
                .joins(newLinkedHashSet(asList(
                        JoinAppConfig.builder()
                                .leftSchemaName("D")
                                .rightSchemaName("E")
                                .joinType(JoinAppConfig.JoinType.LEFT)
                                .joinFields(singletonList(JoinFieldConfig.builder()
                                        .leftJoinField("D_ID")
                                        .rightJoinField("E_ID")
                                        .build()))
                                .build(),
                        JoinAppConfig.builder()
                                .leftSchemaName("B")
                                .rightSchemaName("C")
                                .joinType(JoinAppConfig.JoinType.LEFT)
                                .joinFields(singletonList(JoinFieldConfig.builder()
                                        .leftJoinField("B_ID")
                                        .rightJoinField("C_ID")
                                        .build()))
                                .build(),
                        JoinAppConfig.builder()
                                .leftSchemaName("E")
                                .rightSchemaName("B")
                                .joinType(JoinAppConfig.JoinType.LEFT)
                                .joinFields(singletonList(JoinFieldConfig.builder()
                                        .leftJoinField("E_ID")
                                        .rightJoinField("B_ID")
                                        .build()))
                                .build(),
                        JoinAppConfig.builder()
                                .leftSchemaName("C")
                                .rightSchemaName("D")
                                .joinType(JoinAppConfig.JoinType.LEFT)
                                .joinFields(singletonList(JoinFieldConfig.builder()
                                        .leftJoinField("C_ID")
                                        .rightJoinField("D_ID")
                                        .build()))
                                .build(),
                        JoinAppConfig.builder()
                                .leftSchemaName("A")
                                .rightSchemaName("B")
                                .joinType(JoinAppConfig.JoinType.LEFT)
                                .joinFields(singletonList(JoinFieldConfig.builder()
                                        .leftJoinField("A_ID")
                                        .rightJoinField("B_ID")
                                        .build()))
                                .build())))
                .build();

        assertThat(joinTransformation.toSparkSql())
                .startsWith("SELECT A.NAME, B.NAME, C.NAME, D.NAME, E.NAME, ");
        assertThat(joinTransformation.toSparkSql())
                .contains(
                        "C.ROW_KEY AS FCR_SYS__C__ROW_KEY",
                        "E.ROW_KEY AS FCR_SYS__E__ROW_KEY",
                        "B.ROW_KEY AS FCR_SYS__B__ROW_KEY",
                        "D.ROW_KEY AS FCR_SYS__D__ROW_KEY",
                        "A.ROW_KEY AS FCR_SYS__A__ROW_KEY");

        assertThat(joinTransformation.toSparkSql())
                .endsWith("FROM A LEFT JOIN B ON A.A_ID = B.B_ID "
                        + "LEFT JOIN C ON B.B_ID = C.C_ID "
                        + "LEFT JOIN D ON C.C_ID = D.D_ID "
                        + "LEFT JOIN E ON D.D_ID = E.E_ID "
                        + "LEFT JOIN B ON E.E_ID = B.B_ID");
    }

    @Test
    public void toSparkSql_SelfJoin_ThrowsException() {
        JoinTransformation joinTransformation = JoinTransformation.builder()
                .selects(newLinkedHashSet(singletonList(
                        SelectColumn.builder().select("A.NAME").build())))
                .joins(newLinkedHashSet(singletonList(
                        JoinAppConfig.builder()
                                .leftSchemaName("A")
                                .rightSchemaName("A")
                                .joinType(JoinAppConfig.JoinType.LEFT)
                                .joinFields(singletonList(JoinFieldConfig.builder()
                                        .leftJoinField("A.ID")
                                        .rightJoinField("A.ID")
                                        .build()))
                                .build())))
                .build();

        assertThatThrownBy(joinTransformation::toSparkSql)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Self-join not supported for [A -> A]");
    }

    @Test
    public void toSparkSql_ManyJoinColumnsAndFields_RendersCorrectSql() {
        JoinTransformation joinTransformation = JoinTransformation.builder()
                .selects(newLinkedHashSet(asList(
                        SelectColumn.builder().select("LEFT.NAME").build(),
                        SelectColumn.builder().select("LEFT.JOB").build(),
                        SelectColumn.builder().select("RIGHT.SALARY").build(),
                        SelectColumn.builder().select("OTHER.TEST").build())))
                .joins(newLinkedHashSet(singletonList(
                        JoinAppConfig.builder()
                                .leftSchemaName("LEFT")
                                .rightSchemaName("RIGHT")
                                .joinType(JoinAppConfig.JoinType.INNER)
                                .joinFields(asList(
                                        JoinFieldConfig.builder()
                                                .leftJoinField("L_ID")
                                                .rightJoinField("R_ID")
                                                .build(),
                                        JoinFieldConfig.builder()
                                                .leftJoinField("L_NAME")
                                                .rightJoinField("R_NAME")
                                                .build()))
                                .build())))
                .build();

        assertThat(
                joinTransformation.toSparkSql())
                .isEqualTo("SELECT LEFT.NAME, LEFT.JOB, RIGHT.SALARY, OTHER.TEST, "
                        + "LEFT.ROW_KEY AS FCR_SYS__LEFT__ROW_KEY, RIGHT.ROW_KEY AS FCR_SYS__RIGHT__ROW_KEY "
                        + "FROM LEFT "
                        + "INNER JOIN RIGHT ON LEFT.L_ID = RIGHT.R_ID AND LEFT.L_NAME = RIGHT.R_NAME");
    }

    @Test
    public void toSparkSql_ManyJoinsWithManyJoinsFields_RendersCorrectSql() {
        JoinTransformation joinTransformation = JoinTransformation.builder()
                .selects(newLinkedHashSet(asList(
                        SelectColumn.builder().select("THE_LEFT.NAME").as("NAME").build(),
                        SelectColumn.builder().select("THE_MIDDLE.JOB").as("JOB").build(),
                        SelectColumn.builder().select("THE_RIGHT.SALARY").as("SALARY").build())))
                .joins(newLinkedHashSet(asList(
                        JoinAppConfig.builder()
                                .leftSchemaName("THE_LEFT")
                                .rightSchemaName("THE_MIDDLE")
                                .joinType(JoinAppConfig.JoinType.LEFT)
                                .joinFields(asList(
                                        JoinFieldConfig.builder()
                                                .leftJoinField("L_ID")
                                                .rightJoinField("M_ID")
                                                .build(),
                                        JoinFieldConfig.builder()
                                                .leftJoinField("X")
                                                .rightJoinField("X")
                                                .build()))
                                .build(),
                        JoinAppConfig.builder()
                                .leftSchemaName("THE_MIDDLE")
                                .rightSchemaName("THE_RIGHT")
                                .joinType(JoinAppConfig.JoinType.INNER)
                                .joinFields(asList(
                                        JoinFieldConfig.builder()
                                                .leftJoinField("M_ID")
                                                .rightJoinField("R_ID")
                                                .build(),
                                        JoinFieldConfig.builder()
                                                .leftJoinField("X")
                                                .rightJoinField("X")
                                                .build()))
                                .build())))
                .build();

        assertThat(joinTransformation.toSparkSql())
                .isEqualTo("SELECT "
                        + "THE_LEFT.NAME AS NAME, "
                        + "THE_MIDDLE.JOB AS JOB, "
                        + "THE_RIGHT.SALARY AS SALARY, "
                        + "THE_LEFT.ROW_KEY AS FCR_SYS__THE_LEFT__ROW_KEY, "
                        + "THE_MIDDLE.ROW_KEY AS FCR_SYS__THE_MIDDLE__ROW_KEY, "
                        + "THE_RIGHT.ROW_KEY AS FCR_SYS__THE_RIGHT__ROW_KEY "
                        + "FROM THE_LEFT LEFT JOIN THE_MIDDLE ON THE_LEFT.L_ID = THE_MIDDLE.M_ID AND THE_LEFT.X = THE_MIDDLE.X "
                        + "INNER JOIN THE_RIGHT ON THE_MIDDLE.M_ID = THE_RIGHT.R_ID AND THE_MIDDLE.X = THE_RIGHT.X");
    }
}
