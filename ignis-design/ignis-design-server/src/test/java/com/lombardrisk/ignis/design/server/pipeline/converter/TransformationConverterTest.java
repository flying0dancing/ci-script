package com.lombardrisk.ignis.design.server.pipeline.converter;

import com.lombardrisk.ignis.client.external.pipeline.export.step.JoinType;
import com.lombardrisk.ignis.common.assertions.VavrAssert;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.design.field.DesignField;
import com.lombardrisk.ignis.design.server.fixtures.Design;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineJoinStep;
import com.lombardrisk.ignis.design.server.pipeline.model.join.Join;
import com.lombardrisk.ignis.design.server.pipeline.model.join.JoinField;
import com.lombardrisk.ignis.design.server.productconfig.schema.Schema;
import com.lombardrisk.ignis.pipeline.step.api.SelectColumn;
import com.lombardrisk.ignis.pipeline.step.common.Transformation;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;

import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static org.assertj.core.api.Assertions.assertThat;

public class TransformationConverterTest {

    @Test
    public void convertJoinStep_Valid_ReturnsTransformation() {

        LinkedHashSet<SelectColumn> selectColumns = newLinkedHashSet(Arrays.asList(
                SelectColumn.builder().select("ID1").as("IDENTITY1").build(),
                SelectColumn.builder().select("ID2").as("IDENTITY2").build()));

        Schema left = Design.Populated.schema("LEFT").id(1L)
                .fields(newHashSet(
                        DesignField.Populated.intField("ID1").id(101L).build(),
                        DesignField.Populated.intField("ID2").id(102L).build()))
                .build();
        Schema right = Design.Populated.schema("RIGHT").id(2L)
                .fields(newHashSet(
                        DesignField.Populated.intField("ID1").id(201L).build(),
                        DesignField.Populated.intField("ID2").id(202L).build()))
                .build();

        PipelineJoinStep pipelineJoinStep = Design.Populated.pipelineJoinStep()
                .joins(newHashSet(Join.builder()
                        .leftSchemaId(left.getId())
                        .rightSchemaId(right.getId())
                        .joinType(JoinType.FULL_OUTER)
                        .joinFields(newHashSet(
                                JoinField.builder()
                                        .leftJoinFieldId(101L)
                                        .rightJoinFieldId(201L)
                                        .build(),
                                JoinField.builder()
                                        .leftJoinFieldId(102L)
                                        .rightJoinFieldId(202L)
                                        .build()))
                        .build()))
                .build();
        Transformation joinTransformation = VavrAssert.assertValid(
                TransformationConverter.convertJoinStep(pipelineJoinStep, selectColumns, Arrays.asList(left, right)))
                .getResult();

        assertThat(joinTransformation.toSparkSql())
                .isEqualTo(
                        "SELECT ID1 AS IDENTITY1, ID2 AS IDENTITY2, "
                                + "LEFT.ROW_KEY AS FCR_SYS__LEFT__ROW_KEY, RIGHT.ROW_KEY AS FCR_SYS__RIGHT__ROW_KEY "
                                + "FROM LEFT "
                                + "FULL OUTER JOIN RIGHT ON LEFT.ID1 = RIGHT.ID1 AND LEFT.ID2 = RIGHT.ID2");
    }

    @Test
    public void convertJoinStep_JoinFieldNotFound_ReturnsError() {

        LinkedHashSet<SelectColumn> selectColumns = newLinkedHashSet(Arrays.asList(
                SelectColumn.builder().select("ID1").as("IDENTITY1").build(),
                SelectColumn.builder().select("ID2").as("IDENTITY2").build()));

        Schema left = Design.Populated.schema("LEFT").id(1L)
                .fields(newHashSet(
                        DesignField.Populated.intField("ID1").id(101L).build()))
                .build();
        Schema right = Design.Populated.schema("RIGHT").id(2L)
                .fields(newHashSet(
                        DesignField.Populated.intField("ID1").id(201L).build()))
                .build();

        PipelineJoinStep pipelineJoinStep = Design.Populated.pipelineJoinStep()
                .joins(newHashSet(Join.builder()
                        .leftSchemaId(left.getId())
                        .rightSchemaId(right.getId())
                        .joinType(JoinType.FULL_OUTER)
                        .joinFields(newHashSet(
                                JoinField.builder()
                                        .leftJoinFieldId(101L)
                                        .rightJoinFieldId(201L)
                                        .build(),
                                JoinField.builder()
                                        .leftJoinFieldId(102L)
                                        .rightJoinFieldId(202L)
                                        .build()))
                        .build()))
                .build();
        VavrAssert.assertCollectionFailure(
                TransformationConverter.convertJoinStep(pipelineJoinStep, selectColumns, Arrays.asList(left, right)))
                .withFailure(ErrorResponse.valueOf(
                        "Cannot find Field with Schema 'LEFT', with Id '102'",
                        "NOT_FOUND"));
    }

    @Test
    public void convertJoinStep_SchemaNotFound_ReturnsError() {

        LinkedHashSet<SelectColumn> selectColumns = newLinkedHashSet(Arrays.asList(
                SelectColumn.builder().select("ID1").as("IDENTITY1").build(),
                SelectColumn.builder().select("ID2").as("IDENTITY2").build()));

        Schema left = Design.Populated.schema("LEFT").id(1L)
                .fields(newHashSet(
                        DesignField.Populated.intField("ID1").id(101L).build()))
                .build();

        PipelineJoinStep pipelineJoinStep = Design.Populated.pipelineJoinStep()
                .joins(newHashSet(Join.builder()
                        .leftSchemaId(left.getId())
                        .rightSchemaId(-100L)
                        .joinType(JoinType.FULL_OUTER)
                        .joinFields(newHashSet(
                                JoinField.builder()
                                        .leftJoinFieldId(101L)
                                        .rightJoinFieldId(201L)
                                        .build(),
                                JoinField.builder()
                                        .leftJoinFieldId(102L)
                                        .rightJoinFieldId(202L)
                                        .build()))
                        .build()))
                .build();

        VavrAssert.assertCollectionFailure(
                TransformationConverter.convertJoinStep(pipelineJoinStep, selectColumns,
                        Collections.singletonList(left)))
                .withFailure(ErrorResponse.valueOf(
                        "Could not find Schema for ids [-100]",
                        "NOT_FOUND"));
    }
}
