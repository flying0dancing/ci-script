package com.lombardrisk.ignis.design.server.pipeline.converter;

import com.lombardrisk.ignis.client.design.pipeline.aggregation.PipelineAggregationStepView;
import com.lombardrisk.ignis.client.design.pipeline.join.JoinFieldView;
import com.lombardrisk.ignis.client.design.pipeline.join.JoinView;
import com.lombardrisk.ignis.client.design.pipeline.join.PipelineJoinStepView;
import com.lombardrisk.ignis.client.design.pipeline.map.PipelineMapStepView;
import com.lombardrisk.ignis.client.design.pipeline.scriptlet.PipelineScriptletStepView;
import com.lombardrisk.ignis.client.design.pipeline.scriptlet.ScriptletInputView;
import com.lombardrisk.ignis.client.design.pipeline.select.OrderDirection;
import com.lombardrisk.ignis.client.design.pipeline.select.OrderView;
import com.lombardrisk.ignis.client.design.pipeline.select.SelectView;
import com.lombardrisk.ignis.client.design.pipeline.select.WindowView;
import com.lombardrisk.ignis.client.external.pipeline.export.TransformationType;
import com.lombardrisk.ignis.client.external.pipeline.export.step.JoinType;
import com.lombardrisk.ignis.design.server.fixtures.Design;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineScriptletStep;
import com.lombardrisk.ignis.design.server.pipeline.model.PipelineStep;
import com.lombardrisk.ignis.design.server.pipeline.model.ScriptletInput;
import com.lombardrisk.ignis.design.server.pipeline.model.select.Order;
import com.lombardrisk.ignis.design.server.pipeline.model.select.Window;
import org.junit.Test;

import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;

public class PipelineStepConverterTest {

    private PipelineStepConverter converter = new PipelineStepConverter();

    @Test
    public void apply_ConvertsId() {
        PipelineStep pipelineStep = Design.Populated.pipelineMapStep()
                .id(12345L)
                .build();

        PipelineMapStepView view = (PipelineMapStepView) converter.apply(pipelineStep);

        assertThat(view.getId()).isEqualTo(12345L);
    }

    @Test
    public void apply_ConvertsName() {
        PipelineStep pipelineStep = Design.Populated.pipelineMapStep()
                .name("a pipeline step name")
                .build();

        PipelineMapStepView view = (PipelineMapStepView) converter.apply(pipelineStep);

        assertThat(view.getName()).isEqualTo("a pipeline step name");
    }

    @Test
    public void apply_ConvertsDescription() {
        PipelineStep pipelineStep = Design.Populated.pipelineMapStep()
                .description("a pipeline step description")
                .build();

        PipelineMapStepView view = (PipelineMapStepView) converter.apply(pipelineStep);

        assertThat(view.getDescription()).isEqualTo("a pipeline step description");
    }

    @Test
    public void apply_ConvertsSelects() {
        PipelineStep pipelineStep = Design.Populated.pipelineMapStep()
                .selects(newLinkedHashSet(asList(
                        Design.Populated.select().id(1L).select("A").outputFieldId(123L).build(),
                        Design.Populated.select()
                                .id(2L)
                                .select("B")
                                .outputFieldId(456L)
                                .isWindow(true)
                                .window(Window.builder()
                                        .partitions(newHashSet("D", "E"))
                                        .orders(newLinkedHashSet(asList(
                                                Order.builder()
                                                        .fieldName("F")
                                                        .direction(Order.Direction.ASC)
                                                        .priority(0)
                                                        .build(),
                                                Order.builder()
                                                        .fieldName("G")
                                                        .direction(Order.Direction.DESC)
                                                        .priority(1)
                                                        .build()
                                        )))
                                        .build())
                                .build(),
                        Design.Populated.select().id(3L).select("C").outputFieldId(789L).build())))
                .build();

        PipelineMapStepView view = (PipelineMapStepView) converter.apply(pipelineStep);

        assertThat(view.getSelects()).containsExactly(
                SelectView.builder()
                        .select("A")
                        .outputFieldId(123L)
                        .hasWindow(false)
                        .build(),
                SelectView.builder()
                        .select("B")
                        .outputFieldId(456L)
                        .hasWindow(true)
                        .window(WindowView.builder()
                                .partitionBy(newHashSet("D", "E"))
                                .orderBy(asList(
                                        OrderView.builder()
                                                .fieldName("F")
                                                .direction(OrderDirection.ASC)
                                                .priority(0)
                                                .build(),
                                        OrderView.builder()
                                                .fieldName("G")
                                                .direction(OrderDirection.DESC)
                                                .priority(1)
                                                .build()))
                                .build())
                        .build(),
                SelectView.builder()
                        .select("C")
                        .outputFieldId(789L)
                        .hasWindow(false)
                        .build());
    }

    @Test
    public void apply_SelectsNull_ReturnsEmptyList() {
        PipelineStep pipelineStep = Design.Populated.pipelineMapStep()
                .selects(null)
                .build();

        PipelineMapStepView view = (PipelineMapStepView) converter.apply(pipelineStep);

        assertThat(view.getSelects()).isEmpty();
    }

    @Test
    public void apply_ConvertsFilters() {
        PipelineStep pipelineStep = Design.Populated.pipelineMapStep()
                .filters(newHashSet("A", "B", "C"))
                .build();

        PipelineMapStepView view = (PipelineMapStepView) converter.apply(pipelineStep);

        assertThat(view.getFilters()).containsExactly("A", "B", "C");
    }

    @Test
    public void apply_FiltersNull_ReturnsEmptyList() {
        PipelineStep pipelineStep = Design.Populated.pipelineMapStep()
                .filters(null)
                .build();

        PipelineMapStepView view = (PipelineMapStepView) converter.apply(pipelineStep);

        assertThat(view.getFilters()).isEmpty();
    }

    @Test
    public void apply_ConvertsGroupings() {
        PipelineStep pipelineStep = Design.Populated.pipelineAggregationStep()
                .groupings(newHashSet("A", "B", "C"))
                .build();

        PipelineAggregationStepView view = (PipelineAggregationStepView) converter.apply(pipelineStep);

        assertThat(view.getGroupings()).containsExactly("A", "B", "C");
    }

    @Test
    public void apply_GroupingsNull_ReturnsEmptyList() {
        PipelineStep pipelineStep = Design.Populated.pipelineAggregationStep()
                .groupings(null)
                .build();

        PipelineAggregationStepView view = (PipelineAggregationStepView) converter.apply(pipelineStep);

        assertThat(view.getGroupings()).isEmpty();
    }

    @Test
    public void apply_ConvertsSchemaInId() {
        PipelineStep pipelineStep = Design.Populated.pipelineMapStep()
                .schemaInId(7777L)
                .build();

        PipelineMapStepView view = (PipelineMapStepView) converter.apply(pipelineStep);

        assertThat(view.getSchemaInId()).isEqualTo(7777L);
    }

    @Test
    public void apply_ConvertsSchemaOutId() {
        PipelineStep pipelineStep = Design.Populated.pipelineMapStep()
                .schemaOutId(9999L)
                .build();

        PipelineMapStepView view = (PipelineMapStepView) converter.apply(pipelineStep);

        assertThat(view.getSchemaOutId()).isEqualTo(9999L);
    }

    @Test
    public void apply_Aggregation_ConvertsTransformationType() {
        PipelineStep pipelineStep = Design.Populated.pipelineAggregationStep()
                .build();

        PipelineAggregationStepView view = (PipelineAggregationStepView) converter.apply(pipelineStep);

        assertThat(view.getType()).isEqualTo(TransformationType.AGGREGATION);
    }

    @Test
    public void apply_Map_ConvertsTransformationType() {
        PipelineStep pipelineStep = Design.Populated.pipelineMapStep()
                .build();

        PipelineMapStepView view = (PipelineMapStepView) converter.apply(pipelineStep);

        assertThat(view.getType()).isEqualTo(TransformationType.MAP);
    }

    @Test
    public void apply_ConvertsJoins() {
        PipelineStep pipelineStep = Design.Populated.pipelineJoinStepWithJoinFields()
                .build();

        PipelineJoinStepView view = (PipelineJoinStepView) converter.apply(pipelineStep);

        assertThat(view.getJoins()).containsExactly(
                JoinView.builder()
                        .leftSchemaId(123L)
                        .rightSchemaId(456L)
                        .joinType(JoinType.INNER)
                        .joinFields(asList(
                                JoinFieldView.builder()
                                        .leftFieldId(678L)
                                        .rightFieldId(910L)
                                        .build(),
                                JoinFieldView.builder()
                                        .leftFieldId(112L)
                                        .rightFieldId(134L)
                                        .build()))
                        .build()
        );
    }

    @Test
    public void apply_ConvertsPipelineScriptletStep() {
        PipelineScriptletStep pipelineStep = Design.Populated.scriptletPipelineStep()
                .build();

        ScriptletInput scriptletInput = Design.Populated.scriptletInput()
                .inputName("InputName")
                .schemaInId(25L)
                .build();

        pipelineStep.setName("Step name");
        pipelineStep.setDescription("Step description");
        pipelineStep.setJarFile("myFile.jar");
        pipelineStep.setClassName("className");
        pipelineStep.setSchemaIns(singleton(scriptletInput));
        pipelineStep.setSchemaOutId(24L);

        PipelineScriptletStepView view = (PipelineScriptletStepView) converter.apply(pipelineStep);
        assertThat(view.getName()).isEqualTo("Step name");
        assertThat(view.getDescription()).isEqualTo("Step description");
        assertThat(view.getJarFile()).isEqualTo("myFile.jar");
        assertThat(view.getJavaClass()).isEqualTo("className");
        assertThat(view.getSchemaOutId()).isEqualTo(24L);

        assertThat(view.getScriptletInputs().size()).isEqualTo(1);
        assertThat(view.getScriptletInputs()).containsExactly(ScriptletInputView.builder()
                .metadataInput("InputName")
                .schemaInId(25L)
                .build());
    }
}
