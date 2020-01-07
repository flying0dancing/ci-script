package com.lombardrisk.ignis.server.controller.drillback;

import com.google.common.collect.ImmutableMap;
import com.lombardrisk.ignis.client.external.fixture.ExternalClient;
import com.lombardrisk.ignis.client.external.productconfig.view.FieldView;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.data.common.search.Filter;
import com.lombardrisk.ignis.data.common.search.FilterExpression;
import com.lombardrisk.ignis.data.common.search.FilterOption;
import com.lombardrisk.ignis.data.common.search.FilterType;
import com.lombardrisk.ignis.server.config.IntegrationTestConfig;
import com.lombardrisk.ignis.server.dataset.DatasetService;
import com.lombardrisk.ignis.server.dataset.drillback.DatasetExportService;
import com.lombardrisk.ignis.server.dataset.drillback.DrillBackService;
import com.lombardrisk.ignis.server.dataset.fixture.DatasetPopulated;
import io.vavr.control.Validation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.time.LocalDate;
import java.util.Arrays;

import static com.lombardrisk.ignis.test.config.AdminUser.BASIC_AUTH;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@IntegrationTestConfig
public class DrillBackControllerIT {

    @MockBean
    private DrillBackService drillBackService;

    @MockBean
    private DatasetService datasetService;

    @MockBean
    private DatasetExportService datasetExportService;

    @Autowired
    private MockMvc mockMvc;

    @Before
    public void setUp() {
        when(drillBackService.findDrillBackOutputDatasetRows(any(), any(), any()))
                .thenReturn(Validation.valid(ExternalClient.Populated.datasetRowDataView().build()));
        when(drillBackService.findDrillBackInputDatasetRows(
                any(), anyLong(), anyLong(), any(), any(), any(), any()))
                .thenReturn(Validation.valid(ExternalClient.Populated.datasetRowDataView().build()));
    }

    @Test
    public void getInputDrillBackRowData_CallsServiceWithPageRequest() throws Exception {
        mockMvc.perform(
                get("/api/v1/drillback/datasets/10031?size=100&page=1&type=input&pipelineId=920&pipelineStepId=921")
                        .with(BASIC_AUTH))
                .andExpect(status().isOk());

        verify(drillBackService).findDrillBackInputDatasetRows(10031L, 920L, 921L,
                PageRequest.of(1, 100), false, null, null);
    }

    @Test
    public void getOutputDrillBackRowData_TransformationFeatureActive_CallsServiceWithPageRequest() throws Exception {
        mockMvc.perform(
                get("/api/v1/drillback/datasets/10031?size=100&page=1&type=output")
                        .with(BASIC_AUTH))
                .andExpect(status().isOk());

        verify(drillBackService).findDrillBackOutputDatasetRows(10031L, PageRequest.of(1, 100), null);
    }

    @Test
    public void getOutputDrillBackRowData_DatasetFound_DatasetRowDataReturned() throws Exception {
        when(drillBackService.findDrillBackOutputDatasetRows(any(), any(), any()))
                .thenReturn(Validation.valid(ExternalClient.Populated.datasetRowDataView()
                        .datasetId(7272L)
                        .data(singletonList(ImmutableMap.of("ROW_KEY", 218123L, "NAME", "HUGH")))
                        .build()));

        mockMvc.perform(
                get("/api/v1/drillback/datasets/10031?size=100&page=1&type=output")
                        .with(BASIC_AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datasetId").value(7272L))
                .andExpect(jsonPath("$.data[0].ROW_KEY").value(218123L))
                .andExpect(jsonPath("$.data[0].NAME").value("HUGH"));
    }

    @Test
    public void getOutputDrillBackRowData_DatasetFound_PageDataReturned() throws Exception {
        when(drillBackService.findDrillBackOutputDatasetRows(any(), any(), any()))
                .thenReturn(Validation.valid(ExternalClient.Populated.datasetRowDataView()
                        .page(ExternalClient.Populated.page()
                                .number(1)
                                .totalElements(100)
                                .size(10)
                                .totalPages(10)
                                .build())
                        .build()));

        mockMvc.perform(
                get("/api/v1/drillback/datasets/10031?size=10&page=1&type=output")
                        .with(BASIC_AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.number").value(1))
                .andExpect(jsonPath("$.page.totalElements").value(100))
                .andExpect(jsonPath("$.page.size").value(10))
                .andExpect(jsonPath("$.page.totalPages").value(10));
    }

    @Test
    public void getInputDrillBackRowData_DatasetFound_DatasetRowDataReturned() throws Exception {
        when(drillBackService.findDrillBackInputDatasetRows(
                any(),
                anyLong(),
                anyLong(),
                any(),
                any(),
                any(),
                any()))
                .thenReturn(Validation.valid(ExternalClient.Populated.datasetRowDataView()
                        .datasetId(7272L)
                        .data(singletonList(ImmutableMap.of("ROW_KEY", 218123L, "NAME", "HUGH")))
                        .build()));

        mockMvc.perform(
                get("/api/v1/drillback/datasets/10031?size=100&page=1&pipelineStepId=921&pipelineId=920&type=input")
                        .with(BASIC_AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datasetId").value(7272L))
                .andExpect(jsonPath("$.data[0].ROW_KEY").value(218123L))
                .andExpect(jsonPath("$.data[0].NAME").value("HUGH"));
    }

    @Test
    public void getInputDrillBackRowData_DatasetFound_PageDataReturned() throws Exception {
        when(drillBackService.findDrillBackInputDatasetRows(any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Validation.valid(ExternalClient.Populated.datasetRowDataView()
                        .page(ExternalClient.Populated.page()
                                .number(1)
                                .totalElements(100)
                                .size(10)
                                .totalPages(10)
                                .build())
                        .build()));

        mockMvc.perform(
                get("/api/v1/drillback/datasets/10031?size=100&page=1&type=input&pipelineId=920&pipelineStepId=921")
                        .with(BASIC_AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.number").value(1))
                .andExpect(jsonPath("$.page.totalElements").value(100))
                .andExpect(jsonPath("$.page.size").value(10))
                .andExpect(jsonPath("$.page.totalPages").value(10));
    }

    @Test
    public void getDrillBackStepDetails_TransformationActive_ReturnsPipelineDetails() throws Exception {
        when(drillBackService.findDrillBackStepDetails(anyLong(), anyLong()))
                .thenReturn(Validation.valid(ExternalClient.Populated.drillBackStepDetails()
                        .schemasIn(singletonMap(
                                1L,
                                Arrays.asList(
                                        ExternalClient.Populated.fieldView()
                                                .id(1L)
                                                .name("firstName")
                                                .fieldType(FieldView.Type.STRING)
                                                .build(),
                                        ExternalClient.Populated.fieldView()
                                                .id(2L)
                                                .name("lastName")
                                                .fieldType(FieldView.Type.STRING)
                                                .build())))
                        .schemasOut(singletonMap(
                                2L,
                                singletonList(ExternalClient.Populated.fieldView()
                                        .id(3L)
                                        .name("fullName")
                                        .fieldType(FieldView.Type.STRING)
                                        .build())))
                        .build()));

        mockMvc.perform(
                get("/api/v1/drillback/pipelines/1/steps/2")
                        .with(BASIC_AUTH))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.schemasIn.1[0].id").value(1))
                .andExpect(jsonPath("$.schemasIn.1[0].name").value("firstName"))
                .andExpect(jsonPath("$.schemasIn.1[0].fieldType").value("STRING"))
                .andExpect(jsonPath("$.schemasIn.1[1].id").value(2))
                .andExpect(jsonPath("$.schemasIn.1[1].name").value("lastName"))
                .andExpect(jsonPath("$.schemasIn.1[1].fieldType").value("STRING"))
                .andExpect(jsonPath("$.schemasOut.2[0].id").value(3))
                .andExpect(jsonPath("$.schemasOut.2[0].name").value("fullName"));

        verify(drillBackService).findDrillBackStepDetails(1, 2);
    }

    @Test
    public void getDrillBackStepDetails_DetailsNotFound_ReturnsError() throws Exception {
        when(drillBackService.findDrillBackStepDetails(anyLong(), anyLong()))
                .thenReturn(Validation.invalid(CRUDFailure.notFoundIds("PipelineStep", 2L)));

        mockMvc.perform(
                get("/api/v1/drillback/pipelines/1/steps/2")
                        .with(BASIC_AUTH))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].errorCode").value("NOT_FOUND"))
                .andExpect(jsonPath("$[0].errorMessage").value("Could not find PipelineStep for ids [2]"));
    }

    @Test
    public void getInputDrillBackRowData_SelectedRowKeyDatasetFound_DatasetRowDataReturned() throws Exception {
        when(drillBackService.findDrillBackInputDatasetRows(
                any(),
                anyLong(),
                anyLong(),
                any(),
                any(),
                any(),
                any()))
                .thenReturn(Validation.valid(ExternalClient.Populated.datasetRowDataView()
                        .datasetId(10031L)
                        .data(singletonList(ImmutableMap.of("ROW_KEY", 218123L, "NAME", "HUGH")))
                        .build()));

        mockMvc.perform(
                get("/api/v1/drillback/datasets/10031?size=100&page=1&pipelineStepId=921&pipelineId=920" +
                        "&type=input&showOnlyDrillbackRows=true&outputTableRowKey=123456789")
                        .with(BASIC_AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datasetId").value(10031L))
                .andExpect(jsonPath("$.data[0].ROW_KEY").value(218123L))
                .andExpect(jsonPath("$.data[0].NAME").value("HUGH"));

        verify(drillBackService)
                .findDrillBackInputDatasetRows(10031L, 920L, 921L, PageRequest.of(1, 100), true, 123456789L, null);
    }

    @Test
    public void getInputDrillBackRowData_SearchDatasetFound_DatasetRowDataReturned() throws Exception {
        when(drillBackService.findDrillBackInputDatasetRows(
                any(),
                anyLong(),
                anyLong(),
                any(),
                any(),
                any(),
                any()))
                .thenReturn(Validation.valid(ExternalClient.Populated.datasetRowDataView()
                        .datasetId(10031L)
                        .data(singletonList(ImmutableMap.of("ROW_KEY", 218123L, "NAME", "HUGH")))
                        .build()));

        mockMvc.perform(
                get(new URI("/api/v1/drillback/datasets/10031?size=100&page=1&pipelineStepId=921&pipelineId=920&" +
                        "type=input&search=%7B%22expressionType%22:%22simple%22,%22type%22:%22contains%22,%22filter%22:%22UG%22,%22filterType%22:%22text%22,%22columnName%22:%22NAME%22%7D"))
                        .with(BASIC_AUTH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.datasetId").value(10031L))
                .andExpect(jsonPath("$.data[0].ROW_KEY").value(218123L))
                .andExpect(jsonPath("$.data[0].NAME").value("HUGH"));

        ArgumentCaptor<FilterExpression> filterCaptor = ArgumentCaptor.forClass(FilterExpression.class);

        verify(drillBackService).findDrillBackInputDatasetRows(eq(10031L), eq(920L), eq(921L),
                eq(PageRequest.of(1, 100)), eq(false), eq(null), filterCaptor.capture());

        FilterExpression filter = filterCaptor.getValue();
        assertThat(filter).isInstanceOf(Filter.class);
        assertThat(filter).extracting("type").containsExactly(FilterOption.CONTAINS);
        assertThat(filter).extracting("filter").containsExactly("UG");
        assertThat(filter).extracting("filterType").containsExactly(FilterType.TEXT);
        assertThat(filter).extracting("columnName").containsExactly("NAME");
    }

    @Test
    public void redirectForPipelineDataset_DatasetExists_ReturnsDrillbackUrl() throws Exception {
        when(datasetService.findPipelineDataset(any(), anyLong(), any(), any()))
                .thenReturn(Validation.valid(DatasetPopulated.dataset()
                        .pipelineInvocationId(1002L)
                        .pipelineStepInvocationId(1003L)
                        .build()));

        mockMvc.perform(
                get(new URI(
                        "/api/v1/drillback/metadata?entityCode=123&referenceDate=2019-01-01&runKey=1&datasetName=LIQUIDITY"))
                        .with(BASIC_AUTH))
                .andExpect(redirectedUrl("/fcrengine/drillback?pipelineInvocationId=1002&pipelineStepInvocationId=1003"))
                .andExpect(status().is3xxRedirection());

        verify(datasetService).findPipelineDataset("LIQUIDITY", 1L, LocalDate.of(2019, 1, 1), "123");
    }

    @Test
    public void redirectForPipelineDataset_DatasetNotFound_ReturnsBadRequestUrl() throws Exception {
        when(datasetService.findPipelineDataset(any(), anyLong(), any(), any()))
                .thenReturn(Validation.invalid(CRUDFailure.cannotFind("Dataset")
                        .with("entityCode", "123")
                        .with("referenceDate", LocalDate.of(2019, 1, 1))
                        .with("runKey", 1L)
                        .with("name", "LIQUIDITY")
                        .asFailure()));

        mockMvc.perform(
                get(new URI(
                        "/api/v1/drillback/metadata?entityCode=123&referenceDate=2019-01-01&runKey=1&datasetName=LIQUIDITY"))
                        .with(BASIC_AUTH))
                .andExpect(redirectedUrl(
                        "/fcrengine/error?errorMessage=Cannot+find+Dataset+with+entityCode+%27123%27%2C+"
                                + "with+referenceDate+%272019-01-01%27%2C+with+runKey+%271%27%2C+"
                                + "with+name+%27LIQUIDITY%27&errorTitle=NOT_FOUND"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    public void exportDataset_DatasetNotFound_ReturnsError() throws Exception {
        when(datasetExportService.exportDataset(anyLong(), any(), any(), any()))
                .thenReturn(Validation.invalid(CRUDFailure.notFoundIds("Dataset", 1L)));

        mockMvc.perform(
                get("/api/v1/drillback/datasets/1/export")
                        .with(BASIC_AUTH))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].errorCode").value("NOT_FOUND"))
                .andExpect(jsonPath("$[0].errorMessage").value("Could not find Dataset for ids [1]"));
    }

    @Test
    public void exportDataset_DatasetExported_ReturnsCsvData() throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write("CSV DATA".getBytes());

        when(datasetExportService.exportDataset(anyLong(), any(), any(), any()))
                .thenReturn(Validation.valid
                        (new DatasetExportService.DatasetOutputStream<>("test", byteArrayOutputStream)));

        mockMvc.perform(
                get("/api/v1/drillback/datasets/1/export")
                        .with(BASIC_AUTH))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv"))
                .andExpect(content().string("CSV DATA"));
    }

    @Test
    public void exportDataset_ExportWithFilterAndSort_DelegatesToService() throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write("CSV DATA".getBytes());

        when(datasetExportService.exportDataset(anyLong(), any(), any(), any()))
                .thenReturn(Validation.valid
                        (new DatasetExportService.DatasetOutputStream<>("test", byteArrayOutputStream)));

        mockMvc.perform(
                get(new URI("/api/v1/drillback/datasets/1/export?sort=name,asc"
                        + "&search=%7B%22expressionType%22:%22simple%22,%22type%22:%22contains%22,%22filter%22:%22UG%22,%22filterType%22:%22text%22,%22columnName%22:%22NAME%22%7D"))
                        .with(BASIC_AUTH))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv"))
                .andExpect(content().string("CSV DATA"));

        ArgumentCaptor<FilterExpression> filterCaptor = ArgumentCaptor.forClass(FilterExpression.class);
        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        verify(datasetExportService).exportDataset(eq(1L), sortCaptor.capture(), filterCaptor.capture(), any());

        assertThat(filterCaptor.getValue())
                .isEqualTo(Filter.builder()
                        .columnName("NAME")
                        .filter("UG")
                        .filterType(FilterType.TEXT)
                        .type(FilterOption.CONTAINS)
                        .build());

        assertThat(sortCaptor.getValue())
                .isEqualTo(Sort.by("name").ascending());
    }

    @Test
    public void exportOnlyDillBackDataset_DatasetNotFound_ReturnsError() throws Exception {
        when(datasetExportService.exportOnlyDillBackDataset(
                anyLong(),
                anyLong(),
                anyLong(),
                anyLong(),
                any(),
                any(),
                any()))
                .thenReturn(Validation.invalid(CRUDFailure.notFoundIds("Dataset", 1L)));

        mockMvc.perform(
                get("/api/v1/drillback/datasets/1/exportOnlyDrillback?pipelineId=2&pipelineStepId=3&outputTableRowKey=1000")
                        .with(BASIC_AUTH))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].errorCode").value("NOT_FOUND"))
                .andExpect(jsonPath("$[0].errorMessage").value("Could not find Dataset for ids [1]"));
    }

    @Test
    public void exportOnlyDillBackDataset_DatasetExported_ReturnsCsvData() throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write("CSV DATA".getBytes());

        when(datasetExportService.exportOnlyDillBackDataset(
                anyLong(),
                anyLong(),
                anyLong(),
                anyLong(),
                any(),
                any(),
                any()))
                .thenReturn(Validation.valid
                        (new DatasetExportService.DatasetOutputStream<>("test", byteArrayOutputStream)));

        mockMvc.perform(
                get("/api/v1/drillback/datasets/1/exportOnlyDrillback?pipelineId=2&pipelineStepId=3&outputTableRowKey=1000")
                        .with(BASIC_AUTH))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv"))
                .andExpect(content().string("CSV DATA"));
    }

    @Test
    public void exportOnlyDillBackDataset_delegatesToService() throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write("CSV DATA".getBytes());

        when(datasetExportService.exportOnlyDillBackDataset(
                anyLong(),
                anyLong(),
                anyLong(),
                anyLong(),
                any(),
                any(),
                any()))
                .thenReturn(Validation.valid
                        (new DatasetExportService.DatasetOutputStream<>("test", byteArrayOutputStream)));

        mockMvc.perform(
                get(new URI(
                        "/api/v1/drillback/datasets/1/exportOnlyDrillback?pipelineId=2&pipelineStepId=3&"
                                + "outputTableRowKey=1000&"
                                + "sort=name,asc&"
                                + "search=%7B%22expressionType%22:%22simple%22,%22type%22:%22contains%22,%22filter%22:%22UG%22,%22filterType%22:%22text%22,%22columnName%22:%22NAME%22%7D"))
                        .with(BASIC_AUTH))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv"))
                .andExpect(content().string("CSV DATA"));

        ArgumentCaptor<Sort> sortCaptor = ArgumentCaptor.forClass(Sort.class);
        ArgumentCaptor<FilterExpression> filterCaptor = ArgumentCaptor.forClass(FilterExpression.class);
        verify(datasetExportService).exportOnlyDillBackDataset(
                eq(1L),
                eq(2L),
                eq(3L),
                eq(1000L),
                sortCaptor.capture(),
                filterCaptor.capture(),
                any());

        assertThat(filterCaptor.getValue())
                .isEqualTo(Filter.builder()
                        .columnName("NAME")
                        .filter("UG")
                        .filterType(FilterType.TEXT)
                        .type(FilterOption.CONTAINS)
                        .build());

        assertThat(sortCaptor.getValue())
                .isEqualTo(Sort.by("name").ascending());

        assertThat(filterCaptor.getValue())
                .isEqualTo(Filter.builder()
                        .columnName("NAME")
                        .filter("UG")
                        .filterType(FilterType.TEXT)
                        .type(FilterOption.CONTAINS)
                        .build());

        assertThat(sortCaptor.getValue())
                .isEqualTo(Sort.by("name").ascending());
    }
}

