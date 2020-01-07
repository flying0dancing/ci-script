package com.lombardrisk.ignis.design.server.productconfig.export.converter;

import com.lombardrisk.ignis.client.external.fixture.ExternalClient;
import com.lombardrisk.ignis.client.external.pipeline.export.PipelineExport;
import com.lombardrisk.ignis.client.external.pipeline.export.SchemaReference;
import com.lombardrisk.ignis.client.external.pipeline.export.TransformationType;
import com.lombardrisk.ignis.client.external.pipeline.export.step.JoinExport;
import com.lombardrisk.ignis.client.external.pipeline.export.step.JoinType;
import com.lombardrisk.ignis.client.external.pipeline.export.step.PipelineAggregationStepExport;
import com.lombardrisk.ignis.client.external.pipeline.export.step.PipelineJoinStepExport;
import com.lombardrisk.ignis.client.external.pipeline.export.step.PipelineMapStepExport;
import com.lombardrisk.ignis.client.external.pipeline.export.step.PipelineStepExport;
import com.lombardrisk.ignis.client.external.pipeline.export.step.PipelineWindowStepExport;
import com.lombardrisk.ignis.client.external.pipeline.export.step.select.OrderExport;
import com.lombardrisk.ignis.client.external.pipeline.export.step.select.WindowExport;
import com.lombardrisk.ignis.design.field.DesignField;
import com.lombardrisk.ignis.design.server.fixtures.Design;
import com.lombardrisk.ignis.design.server.pipeline.model.Pipeline;
import com.lombardrisk.ignis.design.server.pipeline.model.join.Join;
import com.lombardrisk.ignis.design.server.pipeline.model.join.JoinField;
import com.lombardrisk.ignis.design.server.pipeline.model.select.Order;
import com.lombardrisk.ignis.design.server.pipeline.model.select.Window;
import com.lombardrisk.ignis.design.server.productconfig.ProductConfig;
import org.junit.Test;

import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

public class PipelineExportConverterTest {

    private PipelineExportConverter converter = new PipelineExportConverter();

    private ProductConfig productConfig = Design.Populated.productConfig()
            .tables(newHashSet(
                    Design.Populated.schema().id(1234L)
                            .physicalTableName("SCHEMA_IN_A")
                            .displayName("schema in display name in A")
                            .majorVersion(1)
                            .fields(newHashSet(
                                    DesignField.Populated.stringField("A").id(1L).build(),
                                    DesignField.Populated.intField("B").id(2L).build()))
                            .build(),
                    Design.Populated.schema().id(910L)
                            .physicalTableName("SCHEMA_IN_B")
                            .displayName("schema in display name in B")
                            .majorVersion(1)
                            .fields(newHashSet(
                                    DesignField.Populated.stringField("C").id(6L).build(),
                                    DesignField.Populated.intField("D").id(7L).build()))
                            .build(),
                    Design.Populated.schema().id(5678L)
                            .physicalTableName("SCHEMA_OUT")
                            .displayName("schema out display name out")
                            .majorVersion(99)
                            .fields(newHashSet(
                                    DesignField.Populated.stringField("X").id(3L).build(),
                                    DesignField.Populated.intField("Y").id(4L).build(),
                                    DesignField.Populated.intField("Z").id(5L).build()))
                            .build()))
            .build();

    @Test
    public void apply_ConvertsPipelineName() {
        Pipeline pipeline = Design.Populated.pipeline()
                .name("the pipeline name")
                .steps(newHashSet(
                        Design.Populated.pipelineMapStep()
                                .schemaInId(1234L)
                                .schemaOutId(5678L)
                                .build()))
                .build();

        PipelineExport result = converter.apply(pipeline, productConfig);

        assertThat(result.getName())
                .isEqualTo("the pipeline name");
    }

    @Test
    public void apply_ConvertsPipelineStepName() {
        Pipeline pipeline = Design.Populated.pipeline()
                .name("the pipeline name")
                .steps(newHashSet(
                        Design.Populated.pipelineMapStep()
                                .name("the pipeline step name")
                                .schemaInId(1234L)
                                .schemaOutId(5678L)
                                .build()))
                .build();

        PipelineExport result = converter.apply(pipeline, productConfig);

        assertThat(result.getSteps())
                .extracting(PipelineStepExport::getName)
                .containsExactly("the pipeline step name");
    }

    @Test
    public void apply_ConvertsPipelineStepDescription() {
        Pipeline pipeline = Design.Populated.pipeline()
                .name("the pipeline name")
                .steps(newHashSet(
                        Design.Populated.pipelineMapStep()
                                .description("the pipeline step description")
                                .schemaInId(1234L)
                                .schemaOutId(5678L)
                                .build()))
                .build();

        PipelineExport result = converter.apply(pipeline, productConfig);

        assertThat(result.getSteps())
                .extracting(PipelineStepExport::getDescription)
                .containsExactly("the pipeline step description");
    }

    @Test
    public void apply_ConvertsPipelineStepType() {
        Pipeline pipeline = Design.Populated.pipeline()
                .name("the pipeline name")
                .steps(newHashSet(
                        Design.Populated.pipelineMapStep()
                                .schemaInId(1234L)
                                .schemaOutId(5678L)
                                .build()))
                .build();

        PipelineExport result = converter.apply(pipeline, productConfig);

        assertThat(result.getSteps())
                .extracting(PipelineStepExport::getType)
                .containsExactly(TransformationType.MAP);
    }

    @Test
    public void apply_ConvertsPipelineStepWindowFilters() {
        Pipeline pipeline = Design.Populated.pipeline()
                .name("the pipeline name")
                .steps(newHashSet(
                        Design.Populated.pipelineWindowStep()
                                .schemaInId(1234L)
                                .schemaOutId(5678L)
                                .filters(singleton("NO HOMERS"))
                                .build()))
                .build();

        PipelineExport result = converter.apply(pipeline, productConfig);

        assertThat(result.getSteps())
                .extracting(pipelineStepExport -> ((PipelineWindowStepExport) pipelineStepExport).getFilters())
                .containsExactly(singletonList("NO HOMERS"));
    }

    @Test
    public void apply_ConvertsPipelineStepSelects() {
        Pipeline pipeline = Design.Populated.pipeline()
                .name("the pipeline name")
                .steps(newHashSet(Design.Populated.pipelineWindowStep()
                        .selects(newHashSet(
                                Design.Populated.select().id(1111L).select("A").outputFieldId(3L).build(),
                                Design.Populated.select().id(2222L).select("B").outputFieldId(4L).build(),
                                Design.Populated.select()
                                        .id(3333L)
                                        .select("rank()")
                                        .outputFieldId(5L)
                                        .isWindow(true)
                                        .window(Window.builder()
                                                .partitions(singleton("B"))
                                                .orders(newLinkedHashSet(asList(
                                                        Order.builder()
                                                                .fieldName("B")
                                                                .direction(Order.Direction.DESC)
                                                                .priority(0)
                                                                .build(),
                                                        Order.builder()
                                                                .fieldName("A")
                                                                .direction(Order.Direction.ASC)
                                                                .priority(1)
                                                                .build())))
                                                .build())
                                        .build()))
                        .schemaInId(1234L)
                        .schemaOutId(5678L)
                        .build()))
                .build();

        PipelineExport result = converter.apply(pipeline, productConfig);
        PipelineStepExport pipelineStepExport = result.getSteps().get(0);

        assertThat(pipelineStepExport)
                .isInstanceOf(PipelineWindowStepExport.class);

        assertThat(((PipelineWindowStepExport) pipelineStepExport).getSelects())
                .containsExactlyInAnyOrder(
                        ExternalClient.Populated.selectExport()
                                .select("A")
                                .outputFieldName("X")
                                .window(null)
                                .build(),
                        ExternalClient.Populated.selectExport()
                                .select("B")
                                .outputFieldName("Y")
                                .window(null)
                                .build(),
                        ExternalClient.Populated.selectExport()
                                .select("rank()")
                                .outputFieldName("Z")
                                .isWindow(true)
                                .window(WindowExport.builder()
                                        .partitionBy(singleton("B"))
                                        .orderBy(asList(
                                                OrderExport.builder()
                                                        .fieldName("B").direction("DESC").priority(0).build(),
                                                OrderExport.builder()
                                                        .fieldName("A").direction("ASC").priority(1).build()))
                                        .build())
                                .build());
    }

    @Test
    public void apply_SelectsNull_ReturnsEmptyList() {
        Pipeline pipeline = Design.Populated.pipeline()
                .name("the pipeline name")
                .steps(newHashSet(
                        Design.Populated.pipelineMapStep()
                                .selects(null)
                                .schemaInId(1234L)
                                .schemaOutId(5678L)
                                .build()))
                .build();

        PipelineExport result = converter.apply(pipeline, productConfig);

        PipelineMapStepExport stepExport = (PipelineMapStepExport) result.getSteps().get(0);
        assertThat(stepExport.getSelects())
                .isEmpty();
    }

    @Test
    public void apply_ConvertsPipelineStepFilters() {
        Pipeline pipeline = Design.Populated.pipeline()
                .name("the pipeline name")
                .steps(newHashSet(
                        Design.Populated.pipelineMapStep()
                                .filters(newHashSet("A", "B", "C"))
                                .schemaInId(1234L)
                                .schemaOutId(5678L)
                                .build()))
                .build();

        PipelineExport result = converter.apply(pipeline, productConfig);

        PipelineMapStepExport pipelineStepExport = (PipelineMapStepExport) result.getSteps().get(0);
        assertThat(pipelineStepExport.getFilters())
                .containsExactly("A", "B", "C");
    }

    @Test
    public void apply_FiltersNull_ReturnsEmptyList() {
        Pipeline pipeline = Design.Populated.pipeline()
                .name("the pipeline name")
                .steps(newHashSet(
                        Design.Populated.pipelineMapStep()
                                .filters(null)
                                .schemaInId(1234L)
                                .schemaOutId(5678L)
                                .build()))
                .build();

        PipelineExport result = converter.apply(pipeline, productConfig);

        PipelineMapStepExport pipelineStepExport = (PipelineMapStepExport) result.getSteps().get(0);
        assertThat(pipelineStepExport.getFilters())
                .isEmpty();
    }

    @Test
    public void apply_ConvertsPipelineStepGroupings() {
        Pipeline pipeline = Design.Populated.pipeline()
                .name("the pipeline name")
                .steps(newHashSet(
                        Design.Populated.pipelineAggregationStep()
                                .groupings(newHashSet("A", "B", "C"))
                                .schemaInId(1234L)
                                .schemaOutId(5678L)
                                .build()))
                .build();

        PipelineExport result = converter.apply(pipeline, productConfig);

        PipelineAggregationStepExport pipelineStepExport = (PipelineAggregationStepExport) result.getSteps().get(0);
        assertThat(pipelineStepExport.getGroupings())
                .containsExactly("A", "B", "C");
    }

    @Test
    public void apply_GroupingsNull_ReturnsEmptyList() {
        Pipeline pipeline = Design.Populated.pipeline()
                .name("the pipeline name")
                .steps(newHashSet(
                        Design.Populated.pipelineAggregationStep()
                                .schemaInId(1234L)
                                .schemaOutId(5678L)
                                .groupings(null)
                                .build()))
                .build();

        PipelineExport result = converter.apply(pipeline, productConfig);
        PipelineAggregationStepExport pipelineStepExport = (PipelineAggregationStepExport) result.getSteps().get(0);

        assertThat(pipelineStepExport.getGroupings())
                .isEmpty();
    }

    @Test
    public void apply_ConvertsSchemaIn() {
        Pipeline pipeline = Design.Populated.pipeline()
                .name("the pipeline name")
                .steps(newHashSet(
                        Design.Populated.pipelineAggregationStep()
                                .schemaInId(1234L)
                                .schemaOutId(5678L)
                                .build()))
                .build();

        PipelineExport result = converter.apply(pipeline, productConfig);

        assertThat(result.getSteps())
                .extracting(PipelineAggregationStepExport.class::cast)
                .extracting(PipelineAggregationStepExport::getSchemaIn)
                .containsExactly(SchemaReference.builder()
                        .physicalTableName("SCHEMA_IN_A")
                        .displayName("schema in display name in A")
                        .version(1)
                        .build());
    }

    @Test
    public void apply_ConvertsSchemaOut() {
        Pipeline pipeline = Design.Populated.pipeline()
                .name("the pipeline name")
                .steps(newHashSet(
                        Design.Populated.pipelineAggregationStep()
                                .schemaInId(1234L)
                                .schemaOutId(5678L)
                                .build()))
                .build();

        PipelineExport result = converter.apply(pipeline, productConfig);

        assertThat(result.getSteps())
                .extracting(PipelineAggregationStepExport.class::cast)
                .extracting(PipelineAggregationStepExport::getSchemaOut)
                .containsExactly(SchemaReference.builder()
                        .physicalTableName("SCHEMA_OUT")
                        .displayName("schema out display name out")
                        .version(99)
                        .build());
    }

    @Test
    public void apply_ConvertsPipelineStepJoin() {
        Pipeline pipeline = Design.Populated.pipeline()
                .name("the pipeline name")
                .steps(newHashSet(
                        Design.Populated.pipelineJoinStep()
                                .name("the pipeline step name")
                                .schemaOutId(5678L)
                                .joins(newHashSet(Join.builder()
                                        .leftSchemaId(1234L)
                                        .rightSchemaId(910L)
                                        .joinType(JoinType.INNER)
                                        .joinFields(newHashSet(
                                                JoinField.builder()
                                                        .leftJoinFieldId(1L)
                                                        .rightJoinFieldId(6L)
                                                        .build(),
                                                JoinField.builder()
                                                        .leftJoinFieldId(2L)
                                                        .rightJoinFieldId(7L)
                                                        .build()))
                                        .build()))
                                .build()))
                .build();

        PipelineExport result = converter.apply(pipeline, productConfig);

        PipelineJoinStepExport pipelineJoinStepExport = (PipelineJoinStepExport) result.getSteps().get(0);
        JoinExport joinExport = pipelineJoinStepExport.getJoins().get(0);
        assertThat(joinExport.getLeft().getPhysicalTableName())
                .isEqualTo("SCHEMA_IN_A");
        assertThat(joinExport.getRight().getPhysicalTableName())
                .isEqualTo("SCHEMA_IN_B");
        assertThat(joinExport.getType())
                .isEqualTo(JoinType.INNER);
        assertThat(joinExport.getJoinFields())
                .extracting("leftColumn")
                .containsExactlyInAnyOrder("A", "B");
        assertThat(joinExport.getJoinFields())
                .extracting("rightColumn")
                .containsExactlyInAnyOrder("C", "D");
    }
}
