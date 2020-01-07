package com.lombardrisk.ignis.server.dataset.drillback;

import com.google.common.collect.ImmutableMap;
import com.lombardrisk.ignis.common.assertions.VavrAssert;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.server.dataset.DatasetService;
import com.lombardrisk.ignis.server.dataset.fixture.DatasetPopulated;
import com.lombardrisk.ignis.server.dataset.model.Dataset;
import com.lombardrisk.ignis.server.dataset.result.DatasetResultRepository;
import com.lombardrisk.ignis.server.dataset.result.DatasetRowData;
import com.lombardrisk.ignis.server.product.fixture.ProductPopulated;
import com.lombardrisk.ignis.server.product.pipeline.PipelineService;
import io.vavr.control.Validation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.google.common.collect.Sets.newLinkedHashSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DatasetExportServiceTest {

    @Mock
    private DatasetService datasetService;
    @Mock
    private DatasetResultRepository datasetResultRepository;
    @Mock
    private PipelineService pipelineService;

    private DatasetExportService datasetExportService;

    @Before
    public void setUp() {
        this.datasetExportService =
                new DatasetExportService(2, 100, datasetService, pipelineService, datasetResultRepository);
    }

    @Test
    public void exportDataset_DatasetExists_ExportsContentWithHeader() throws IOException {
        Dataset dataset = DatasetPopulated.dataset()
                .recordsCount(1L)
                .schema(ProductPopulated.table()
                        .fields(newLinkedHashSet(Arrays.asList(
                                ProductPopulated.stringField("NAME").build(),
                                ProductPopulated.stringField("DESCRIPTION").build(),
                                ProductPopulated.longField("AGE").build())))
                        .build())
                .build();
        when(datasetService.findWithValidation(anyLong()))
                .thenReturn(Validation.valid(dataset));

        when(datasetResultRepository.queryDataset(any(), any(), any()))
                .thenReturn(DatasetRowData.builder()
                        .dataset(dataset)
                        .resultData(new PageImpl<>(Collections.singletonList(
                                ImmutableMap.of(
                                        "NAME", "Beetle",
                                        "DESCRIPTION", "A gross thing",
                                        "AGE", 1))))
                        .build());

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream result =
                VavrAssert.assertValid(datasetExportService.exportDataset(10L, null, null, byteArrayOutputStream))
                        .getResult().getOutputStream();

        String output = new String(result.toByteArray());
        assertThat(output)
                .isEqualTo("NAME,DESCRIPTION,AGE\n"
                        + "Beetle,\"A gross thing\",1\n");
    }

    @Test
    public void exportDataset_DatasetExceedsMaxExportSize_ReturnsError() throws IOException {
        this.datasetExportService =
                new DatasetExportService(2, 7000, datasetService, pipelineService, datasetResultRepository);

        Dataset dataset = DatasetPopulated.dataset()
                .recordsCount(10000L)
                .schema(ProductPopulated.table()
                        .fields(newLinkedHashSet(Arrays.asList(
                                ProductPopulated.stringField("NAME").build(),
                                ProductPopulated.stringField("DESCRIPTION").build(),
                                ProductPopulated.longField("AGE").build())))
                        .build())
                .build();
        when(datasetService.findWithValidation(anyLong()))
                .thenReturn(Validation.valid(dataset));

        when(datasetResultRepository.queryDataset(any(), any(), any()))
                .thenReturn(DatasetRowData.builder()
                        .dataset(dataset)
                        .resultData(new PageImpl<>(Collections.emptyList(), Pageable.unpaged(), 10000))
                        .build());

        VavrAssert.assertFailed(datasetExportService.exportDataset(10L, null, null, null))
                .withFailure(CRUDFailure.constraintFailure(
                        "Dataset export would contain more records [10000] than max export size allows [7000]"));
    }

    @Test
    public void exportDataset_DatasetExists_ReturnsOutputStreamWithName() throws IOException {
        Dataset dataset = DatasetPopulated.dataset()
                .recordsCount(1L)
                .schema(ProductPopulated.table()
                        .displayName("A nice dataset")
                        .fields(newLinkedHashSet(Arrays.asList(
                                ProductPopulated.stringField("NAME").build(),
                                ProductPopulated.stringField("DESCRIPTION").build(),
                                ProductPopulated.longField("AGE").build())))
                        .build())
                .build();
        when(datasetService.findWithValidation(anyLong()))
                .thenReturn(Validation.valid(dataset));

        when(datasetResultRepository.queryDataset(any(), any(), any()))
                .thenReturn(DatasetRowData.builder()
                        .dataset(dataset)
                        .resultData(new PageImpl<>(Collections.singletonList(ImmutableMap.of())))
                        .build());

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        VavrAssert.assertValid(datasetExportService.exportDataset(10L, null, null, byteArrayOutputStream))
                .extracting(DatasetExportService.DatasetOutputStream::getFileName)
                .withResult("A nice dataset.csv");
    }

    @Test
    public void exportDataset_DatasetHasManyRecords_PagesResultSet() throws IOException {
        this.datasetExportService =
                new DatasetExportService(1, 100, datasetService, pipelineService, datasetResultRepository);

        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        Dataset dataset = DatasetPopulated.dataset()
                .recordsCount(10L)
                .schema(ProductPopulated.table()
                        .fields(newLinkedHashSet(Collections.singletonList(
                                ProductPopulated.longField("NUMBER").build())))
                        .build())
                .build();
        when(datasetService.findWithValidation(anyLong()))
                .thenReturn(Validation.valid(dataset));

        when(datasetResultRepository.queryDataset(any(), any(), any()))
                .then(inv -> {
                    Pageable argument = inv.getArgument(1);
                    return DatasetRowData.builder()
                            .dataset(dataset)
                            .resultData(new PageImpl<>(Collections.singletonList(
                                    ImmutableMap.of("NUMBER", numbers.get(argument.getPageNumber()))),
                                    argument, 10))
                            .build();
                });

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream result =
                VavrAssert.assertValid(datasetExportService.exportDataset(10L, null, null, byteArrayOutputStream))
                        .getResult()
                        .getOutputStream();

        String output = new String(result.toByteArray());
        assertThat(output)
                .isEqualTo("NUMBER\n"
                        + "1\n"
                        + "2\n"
                        + "3\n"
                        + "4\n"
                        + "5\n"
                        + "6\n"
                        + "7\n"
                        + "8\n"
                        + "9\n"
                        + "10\n"
                );
    }

    @Test
    public void exportOnlyDillBackDataset_DatasetExists_ExportContentWithHeader() throws IOException {
        Dataset dataset = DatasetPopulated.dataset()
                .recordsCount(1L)
                .schema(ProductPopulated.table()
                        .fields(newLinkedHashSet(Arrays.asList(
                                ProductPopulated.stringField("NAME").build(),
                                ProductPopulated.stringField("DESCRIPTION").build(),
                                ProductPopulated.longField("AGE").build())))
                        .build())
                .build();
        when(datasetService.findWithValidation(anyLong()))
                .thenReturn(Validation.valid(dataset));

        when(pipelineService.findStepById(any(), any()))
                .thenReturn(Validation.valid(ProductPopulated.mapPipelineStep().build()));

        when(datasetResultRepository.findDrillbackInputDatasetRowData(any(), any(), any(),
                anyBoolean(), anyLong(), any()))
                .thenReturn(DatasetRowData.builder()
                        .dataset(dataset)
                        .resultData(new PageImpl<>(Collections.singletonList(
                                ImmutableMap.of(
                                        "NAME", "Beetle",
                                        "DESCRIPTION", "A gross thing",
                                        "AGE", 1))))
                        .build());

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream result =
                VavrAssert.assertValid(datasetExportService.exportOnlyDillBackDataset(
                        10L,
                        15L,
                        20L,
                        30L,
                        null,
                        null,
                        byteArrayOutputStream))
                        .getResult().getOutputStream();

        String output = new String(result.toByteArray());
        assertThat(output)
                .isEqualTo("NAME,DESCRIPTION,AGE\n"
                        + "Beetle,\"A gross thing\",1\n");
    }

    @Test
    public void exportOnlyDillBackDataset_DatasetExceedsMaxExportSize_ReturnsError() throws IOException {
        this.datasetExportService =
                new DatasetExportService(2, 7000, datasetService, pipelineService, datasetResultRepository);

        Dataset dataset = DatasetPopulated.dataset()
                .recordsCount(10000L)
                .schema(ProductPopulated.table()
                        .fields(newLinkedHashSet(Arrays.asList(
                                ProductPopulated.stringField("NAME").build(),
                                ProductPopulated.stringField("DESCRIPTION").build(),
                                ProductPopulated.longField("AGE").build())))
                        .build())
                .build();
        when(datasetService.findWithValidation(anyLong()))
                .thenReturn(Validation.valid(dataset));

        when(pipelineService.findStepById(any(), any()))
                .thenReturn(Validation.valid(ProductPopulated.mapPipelineStep().build()));

        when(datasetResultRepository.findDrillbackInputDatasetRowData(any(), any(), any(),
                anyBoolean(), anyLong(), any()))
                .thenReturn(DatasetRowData.builder()
                        .dataset(dataset)
                        .resultData(new PageImpl<>(Collections.emptyList(), Pageable.unpaged(), 10000))
                        .build());

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        VavrAssert.assertFailed(datasetExportService.exportOnlyDillBackDataset(
                10L,
                15L,
                20L,
                30L,
                null,
                null,
                byteArrayOutputStream))
                .withFailure(CRUDFailure.constraintFailure(
                        "Dataset export would contain more records [10000] than max export size allows [7000]"));
    }

    @Test
    public void exportOnlyDillBackDataset_DatasetExists_ReturnsOutputStreamWithName() throws IOException {
        Dataset dataset = DatasetPopulated.dataset()
                .recordsCount(1L)
                .schema(ProductPopulated.table()
                        .displayName("A nice dataset")
                        .fields(newLinkedHashSet(Arrays.asList(
                                ProductPopulated.stringField("NAME").build(),
                                ProductPopulated.stringField("DESCRIPTION").build(),
                                ProductPopulated.longField("AGE").build())))
                        .build())
                .build();
        when(datasetService.findWithValidation(anyLong()))
                .thenReturn(Validation.valid(dataset));

        when(pipelineService.findStepById(any(), any()))
                .thenReturn(Validation.valid(ProductPopulated.mapPipelineStep().build()));

        when(datasetResultRepository.findDrillbackInputDatasetRowData(any(), any(), any(),
                anyBoolean(), anyLong(), any()))
                .thenReturn(DatasetRowData.builder()
                        .dataset(dataset)
                        .resultData(new PageImpl<>(Collections.singletonList(ImmutableMap.of())))
                        .build());

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        VavrAssert.assertValid(datasetExportService.exportOnlyDillBackDataset(
                10L,
                15L,
                20L,
                30L,
                null,
                null,
                byteArrayOutputStream))
                .extracting(DatasetExportService.DatasetOutputStream::getFileName)
                .withResult("A nice dataset.csv");
    }
}
