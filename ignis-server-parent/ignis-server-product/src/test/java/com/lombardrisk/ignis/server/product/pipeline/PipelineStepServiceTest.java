package com.lombardrisk.ignis.server.product.pipeline;

import com.lombardrisk.ignis.pipeline.step.api.DrillbackColumnLink;
import com.lombardrisk.ignis.server.product.fixture.ProductPopulated;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineAggregationStep;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineStep;
import com.lombardrisk.ignis.server.product.pipeline.model.PipelineWindowStep;
import com.lombardrisk.ignis.server.product.pipeline.select.Window;
import com.lombardrisk.ignis.server.product.productconfig.model.ProductImportDiff;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PipelineStepServiceTest {

    @Mock
    private SelectJpaRepository selectJpaRepository;

    @InjectMocks
    private PipelineStepService pipelineStepService;

    @Test
    public void getDrillbackColumnLinks_MapStep_ReturnsRowKeyColumn() {
        PipelineStep mapStep = ProductPopulated.mapPipelineStep()
                .id(123L)
                .schemaInId(88L)
                .schemaIn(ProductPopulated.schemaDetails().physicalTableName("INPUT").build())
                .schemaOutId(99L)
                .schemaOut(ProductPopulated.schemaDetails().physicalTableName("OUTPUT").build())
                .build();

        Set<DrillbackColumnLink> drillbackColumnLinks = pipelineStepService.getDrillbackColumnLinks(mapStep);

        assertThat(drillbackColumnLinks)
                .containsExactly(DrillbackColumnLink.builder()
                        .inputSchema("INPUT")
                        .outputSchema("OUTPUT")
                        .inputColumn("ROW_KEY")
                        .inputColumnSqlType("BIGINT")
                        .build());
    }

    @Test
    public void getDrillbackColumnLinks_AggregationStep_ReturnsGroupingColumn() {
        PipelineStep aggregationStep = ProductPopulated.aggregatePipelineStep()
                .id(123L)
                .schemaInId(88L)
                .schemaIn(ProductPopulated.schemaDetails().physicalTableName("INPUT").build())
                .schemaOut(ProductPopulated.schemaDetails().physicalTableName("OUTPUT").build())
                .groupings(newHashSet("A", "B", "C", "D"))
                .schemaOutId(99L)
                .build();

        Set<DrillbackColumnLink> drillbackColumnLinks = pipelineStepService.getDrillbackColumnLinks(aggregationStep);

        assertThat(drillbackColumnLinks)
                .containsExactlyInAnyOrder(
                        DrillbackColumnLink.builder()
                                .inputSchema("INPUT").outputSchema("OUTPUT").inputColumn("A").build(),
                        DrillbackColumnLink.builder()
                                .inputSchema("INPUT").outputSchema("OUTPUT").inputColumn("B").build(),
                        DrillbackColumnLink.builder()
                                .inputSchema("INPUT").outputSchema("OUTPUT").inputColumn("C").build(),
                        DrillbackColumnLink.builder()
                                .inputSchema("INPUT").outputSchema("OUTPUT").inputColumn("D").build());
    }

    @Test
    public void getDrillbackColumnLinks_WindowStepWithPartitions_ReturnsPartitionColumns() {
        PipelineStep windowStep = ProductPopulated.windowPipelineStep()
                .id(123L)
                .schemaInId(88L).schemaIn(ProductPopulated.schemaDetails().physicalTableName("INPUT").build())
                .schemaOut(ProductPopulated.schemaDetails().physicalTableName("OUTPUT").build()).schemaOutId(99L)
                .build();

        when(selectJpaRepository.findAllWithWindowByPipelineStep(windowStep))
                .thenReturn(asList(
                        ProductPopulated.select().select("A").outputFieldId(991L).build(),
                        ProductPopulated.select().select("B").outputFieldId(992L).build(),
                        ProductPopulated.select()
                                .select("window()")
                                .outputFieldId(993L)
                                .isWindow(true)
                                .window(Window.builder().partitions(singleton("B")).build())
                                .build(),
                        ProductPopulated.select()
                                .select("window()")
                                .outputFieldId(994L)
                                .isWindow(true)
                                .window(Window.builder().partitions(newHashSet("C", "D")).build())
                                .build()));

        Set<DrillbackColumnLink> drillbackColumnLinks = pipelineStepService.getDrillbackColumnLinks(windowStep);

        assertThat(drillbackColumnLinks)
                .containsExactlyInAnyOrder(
                        DrillbackColumnLink.builder()
                                .inputSchema("INPUT").outputSchema("OUTPUT").inputColumn("B").build(),
                        DrillbackColumnLink.builder()
                                .inputSchema("INPUT").outputSchema("OUTPUT").inputColumn("C").build(),
                        DrillbackColumnLink.builder()
                                .inputSchema("INPUT").outputSchema("OUTPUT").inputColumn("D").build());
    }

    @Test
    public void getDrillbackColumnLinks_WindowStepWithNoPartitions_ReturnsRowKeyColumn() {
        PipelineStep windowStep = ProductPopulated.windowPipelineStep()
                .id(123L)
                .schemaInId(88L)
                .schemaIn(ProductPopulated.schemaDetails().physicalTableName("INPUT").build())
                .schemaOut(ProductPopulated.schemaDetails().physicalTableName("OUTPUT").build())
                .schemaOutId(99L)
                .selects(newHashSet(
                        ProductPopulated.select().select("A").outputFieldId(991L).build(),
                        ProductPopulated.select().select("B").outputFieldId(992L).build(),
                        ProductPopulated.select().select("C").outputFieldId(993L).build(),
                        ProductPopulated.select().select("D").outputFieldId(994L).build()))
                .build();

        when(selectJpaRepository.findAllWithWindowByPipelineStep(windowStep))
                .thenReturn(emptyList());

        Set<DrillbackColumnLink> drillbackColumnLinks = pipelineStepService.getDrillbackColumnLinks(windowStep);

        assertThat(drillbackColumnLinks)
                .containsExactly(DrillbackColumnLink.builder()
                        .inputSchema("INPUT")
                        .outputSchema("OUTPUT")
                        .inputColumn("ROW_KEY")
                        .inputColumnSqlType("BIGINT")
                        .build());
    }

    @Test
    public void getDrillbackColumnLinks_JoinStep_ReturnsMultipleRowKeyColumns() {
        PipelineStep joinStep = ProductPopulated.joinPipelineStep()
                .id(123L)
                .joins(newHashSet(
                        ProductPopulated.join()
                                .leftSchemaId(101L)
                                .leftSchema(ProductPopulated.schemaDetails()
                                        .physicalTableName("A")
                                        .build())
                                .rightSchemaId(102L)
                                .rightSchema(ProductPopulated.schemaDetails()
                                        .physicalTableName("B")
                                        .build())
                                .build(),
                        ProductPopulated.join()
                                .leftSchemaId(102L)
                                .leftSchema(ProductPopulated.schemaDetails()
                                        .physicalTableName("B")
                                        .build())
                                .rightSchemaId(103L)
                                .rightSchema(ProductPopulated.schemaDetails()
                                        .physicalTableName("C")
                                        .build())
                                .build()))
                .schemaOutId(99L)
                .schemaOut(ProductPopulated.schemaDetails().physicalTableName("OUTPUT").build())
                .build();

        Set<DrillbackColumnLink> drillbackColumnLinks = pipelineStepService.getDrillbackColumnLinks(joinStep);

        assertThat(drillbackColumnLinks)
                .containsExactlyInAnyOrder(
                        DrillbackColumnLink.builder()
                                .inputSchema("A")
                                .outputSchema("OUTPUT")
                                .inputColumn("ROW_KEY")
                                .inputColumnSqlType("BIGINT")
                                .build(),
                        DrillbackColumnLink.builder()
                                .inputSchema("B")
                                .outputSchema("OUTPUT")
                                .inputColumn("ROW_KEY")
                                .inputColumnSqlType("BIGINT")
                                .build(),
                        DrillbackColumnLink.builder()
                                .inputSchema("C")
                                .outputSchema("OUTPUT")
                                .inputColumn("ROW_KEY")
                                .inputColumnSqlType("BIGINT")
                                .build());
    }

    @Test
    public void getDrillbackColumnLinks_UnionStep_ReturnsMultipleRowKeyColumns() {
        PipelineStep unionStep = ProductPopulated.unionPipelineStep()
                .id(123L)
                .schemaInIds(newHashSet(88L, 89L))
                .schemasIn(newHashSet(
                        ProductPopulated.schemaDetails().physicalTableName("INPUT_A").build(),
                        ProductPopulated.schemaDetails().physicalTableName("INPUT_B").build()))
                .schemaOutId(99L)
                .schemaOut(ProductPopulated.schemaDetails().physicalTableName("OUTPUT").build())
                .build();

        Set<DrillbackColumnLink> drillbackColumnLinks = pipelineStepService.getDrillbackColumnLinks(unionStep);

        assertThat(drillbackColumnLinks)
                .containsExactlyInAnyOrder(
                        DrillbackColumnLink.builder()
                                .inputSchema("INPUT_A")
                                .outputSchema("OUTPUT")
                                .inputColumn("ROW_KEY")
                                .inputColumnSqlType("BIGINT")
                                .build(),
                        DrillbackColumnLink.builder()
                                .inputSchema("INPUT_B")
                                .outputSchema("OUTPUT")
                                .inputColumn("ROW_KEY")
                                .inputColumnSqlType("BIGINT")
                                .build());
    }

    @Test
    public void findAdditionalPhoenixDrillbackColumns_GroupingNotFoundForAggregation_ThrowsException() {
        ProductImportDiff productDiff = ProductPopulated.productImportDiff()
                .existingSchemas(singleton(ProductPopulated.table()
                        .id(1L)
                        .physicalTableName("SCHEMA_OUT")
                        .build()))
                .newSchemas(singleton(ProductPopulated.table()
                        .id(2L)
                        .physicalTableName("SCHEMA_IN")
                        .fields(newHashSet(
                                ProductPopulated.stringField("A").maxLength(100).build(),
                                ProductPopulated.decimalField("B").scale(10).precision(2).build(),
                                ProductPopulated.timestampField("C").build()))
                        .build()))
                .build();

        PipelineAggregationStep aggregationStep = ProductPopulated.aggregatePipelineStep()
                .schemaIn(ProductPopulated.schemaDetails()
                        .physicalTableName("SCHEMA_IN")
                        .build())
                .schemaOut(ProductPopulated.schemaDetails()
                        .physicalTableName("SCHEMA_OUT")
                        .build())
                .groupings(newLinkedHashSet(asList("A", "I_DONT_EXIST")))
                .build();

        assertThatThrownBy(() -> pipelineStepService
                .findAdditionalPhoenixDrillbackColumns(aggregationStep, productDiff))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Could not find grouping field I_DONT_EXIST in schema SCHEMA_IN");
    }

    @Test
    public void findAdditionalPhoenixDrillbackColumns_PartitionNotFoundForWindow_ThrowsException() {
        ProductImportDiff productDiff = ProductPopulated.productImportDiff()
                .newSchemas(newHashSet(
                        ProductPopulated.table()
                                .id(2L)
                                .physicalTableName("EMPLOYEES")
                                .fields(newHashSet(
                                        ProductPopulated.stringField("Name").maxLength(100).build(),
                                        ProductPopulated.decimalField("Salary").scale(10).build()))
                                .build(),
                        ProductPopulated.table()
                                .id(1L)
                                .physicalTableName("EMPLOYEES_RANKED")
                                .build()))
                .build();

        PipelineWindowStep windowStep = ProductPopulated.windowPipelineStep()
                .schemaIn(ProductPopulated.schemaDetails()
                        .physicalTableName("EMPLOYEES")
                        .build())
                .schemaOut(ProductPopulated.schemaDetails()
                        .physicalTableName("EMPLOYEES_RANKED")
                        .build())
                .selects(null)
                .build();

        when(selectJpaRepository.findAllWithWindowByPipelineStep(windowStep))
                .thenReturn(asList(
                        ProductPopulated.select()
                                .select("Name")
                                .build(),
                        ProductPopulated.select()
                                .select("dense_rank()")
                                .isWindow(true)
                                .window(Window.builder()
                                        .partitions(singleton("Department"))
                                        .build())
                                .build()));

        assertThatThrownBy(() -> pipelineStepService
                .findAdditionalPhoenixDrillbackColumns(windowStep, productDiff))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Could not find grouping field Department in schema EMPLOYEES");
    }
}