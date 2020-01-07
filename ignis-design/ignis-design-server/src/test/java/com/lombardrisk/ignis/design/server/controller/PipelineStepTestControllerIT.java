package com.lombardrisk.ignis.design.server.controller;

import com.lombardrisk.ignis.client.design.pipeline.test.StepTestStatus;
import com.lombardrisk.ignis.client.design.pipeline.test.request.CreatePipelineStepDataRowRequest;
import com.lombardrisk.ignis.client.design.pipeline.test.request.CreateStepTestRequest;
import com.lombardrisk.ignis.client.design.pipeline.test.request.RowCellDataRequest;
import com.lombardrisk.ignis.client.design.pipeline.test.view.StepRowInputDataView;
import com.lombardrisk.ignis.client.design.pipeline.test.view.StepRowOutputDataView;
import com.lombardrisk.ignis.client.design.pipeline.test.view.StepTestCellView;
import com.lombardrisk.ignis.client.design.pipeline.test.view.StepTestRowView;
import com.lombardrisk.ignis.client.design.pipeline.test.view.StepTestView;
import com.lombardrisk.ignis.client.design.pipeline.test.view.TestRunResultView;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.data.common.utils.CsvUtils;
import com.lombardrisk.ignis.design.server.DesignStudioTestConfig;
import com.lombardrisk.ignis.design.server.fixtures.Design;
import com.lombardrisk.ignis.design.server.pipeline.test.PipelineStepTestExecuteService;
import com.lombardrisk.ignis.design.server.pipeline.test.PipelineStepTestRowService;
import com.lombardrisk.ignis.design.server.pipeline.test.PipelineStepTestService;
import io.vavr.control.Validation;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.lombardrisk.ignis.common.json.MapperWrapper.MAPPER;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@DesignStudioTestConfig
public class PipelineStepTestControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PipelineStepTestService pipelineStepTestService;

    @MockBean
    private PipelineStepTestExecuteService pipelineStepTestExecuteService;

    @MockBean
    private PipelineStepTestRowService pipelineStepTestRowService;

    @Test
    public void createPipelineTest_ReturnsOkResponseWithEntity() throws Exception {
        when(pipelineStepTestService.create(any()))
                .thenReturn(Validation.valid(() -> 1L));

        mockMvc.perform(
                post("/api/v1/pipelineStepTests")
                        .contentType(APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(CreateStepTestRequest.builder().build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    public void getPipelineStepTestByPipelineId_ReturnsOkResponseWithEntity() throws Exception {
        when(pipelineStepTestService.findAllTestsByPipelineId(1L))
                .thenReturn(Collections.singletonList(StepTestView
                        .builder()
                        .id(1L)
                        .name("StepTestView")
                        .description("StepTestView")
                        .testReferenceDate(LocalDate.of(2019, 1, 1))
                        .build()));

        mockMvc.perform(
                get("/api/v1/pipelineStepTests?pipelineId=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("StepTestView"))
                .andExpect(jsonPath("$[0].description").value("StepTestView"))
                .andExpect(jsonPath("$[0].testReferenceDate").value("2019-01-01"));
    }

    @Test
    public void createPipelineTestInputDataRow_ReturnsOkResponseWithEntity() throws Exception {
        when(pipelineStepTestRowService.createInputDataRow(anyLong(), any()))
                .thenReturn(Validation.valid(StepTestRowView.builder()
                        .id(1L)
                        .cells(Collections.singletonList(StepTestRowView.CellIdAndField.builder()
                                .id(2L)
                                .fieldId(3L)
                                .build()))
                        .build()));

        mockMvc.perform(
                post("/api/v1/pipelineStepTests/1/inputDataRows")
                        .contentType(APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(CreatePipelineStepDataRowRequest.builder().build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.cells[0].id").value(2))
                .andExpect(jsonPath("$.cells[0].fieldId").value(3));
    }

    @Test
    public void createPipelineTestExpectedDataRow_ReturnsOkResponseWithEntity() throws Exception {
        when(pipelineStepTestRowService.createExpectedDataRow(anyLong(), any()))
                .thenReturn(Validation.valid(StepTestRowView.builder()
                        .id(1L)
                        .cells(Collections.singletonList(StepTestRowView.CellIdAndField.builder()
                                .id(2L)
                                .fieldId(3L)
                                .build()))
                        .build()));

        mockMvc.perform(
                post("/api/v1/pipelineStepTests/1/expectedDataRows")
                        .contentType(APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(CreatePipelineStepDataRowRequest.builder().build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.cells[0].id").value(2))
                .andExpect(jsonPath("$.cells[0].fieldId").value(3));
    }

    @Test
    public void createPipelineTestImportCsvInputDataRow_delegatesStepIdToPipelineStepTestRowService() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "application/csv", new byte[]{});

        mockMvc.perform(multipart("/api/v1/pipelineStepTests/1/schemas/2/import?type=input")
                .file(file));

        verify(pipelineStepTestRowService).importCsvInputDataRow(eq(1L), anyLong(), any());
    }

    @Test
    public void createPipelineTestImportCsvInputDataRow_delegatesSchemaIdToPipelineStepTestRowService() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "application/csv", new byte[]{});

        mockMvc.perform(multipart("/api/v1/pipelineStepTests/1/schemas/2/import?type=input")
                .file(file));

        verify(pipelineStepTestRowService).importCsvInputDataRow(anyLong(), eq(2L), any());
    }

    @Test
    public void createPipelineTestImportCsvInputDataRow_delegatesFileInputStreamToPipelineStepTestRowService() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.csv",
                "application/csv", "content".getBytes());

        mockMvc.perform(multipart("/api/v1/pipelineStepTests/1/schemas/2/import?type=input")
                .file(file));

        ArgumentCaptor<InputStream> inputStreamCapture = ArgumentCaptor.forClass(InputStream.class);
        verify(pipelineStepTestRowService).importCsvInputDataRow(anyLong(), anyLong(), inputStreamCapture.capture());

        InputStream inputStream = inputStreamCapture.getValue();
        final String csvContent = IOUtils.readLines(inputStream, "utf-8")
                .get(0);
        assertThat(csvContent).isEqualTo("content");
    }

    @Test
    public void createPipelineTestImportCsvInputDataRow_returnsOkResponseWithFieldCellData() throws Exception {
        List<StepTestRowView> createdSteps = asList(StepTestRowView.builder()
                .id(1L)
                .cells(Collections.singletonList(StepTestRowView.CellIdAndField.builder()
                        .id(2L)
                        .cellData("Cell data")
                        .fieldId(3L)
                        .build()))
                .build());

        when(pipelineStepTestRowService.importCsvInputDataRow(anyLong(), anyLong(), any()))
                .thenReturn(Validation.valid(createdSteps));

        MockMultipartFile file = new MockMultipartFile("file", "test.csv",
                "application/csv", "content".getBytes());

        mockMvc.perform(
                multipart("/api/v1/pipelineStepTests/1/schemas/2/import?type=input")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cells[0].cellData").value("Cell data"));
    }

    @Test
    public void updateCellValue_ReturnsOkResponseWithEntity() throws Exception {
        when(pipelineStepTestRowService.updateCellValue(200L, 3L, 1L, "NEW_CELL"))
                .thenReturn(Validation.valid(StepTestRowView.builder()
                        .id(3L)
                        .cells(Collections.singletonList(
                                StepTestRowView.CellIdAndField.builder().id(1L).fieldId(999L).build()))
                        .build()));

        mockMvc.perform(
                patch("/api/v1/pipelineStepTests/200/rows/3/rowCells/1")
                        .contentType(APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(RowCellDataRequest.builder()
                                .inputDataValue("NEW_CELL")
                                .build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.cells[0].id").value(1))
                .andExpect(jsonPath("$.cells[0].fieldId").value(999));
    }

    @Test
    public void getPipelineStepTestById_ReturnsOkResponseWithEntity() throws Exception {
        when(pipelineStepTestService.findById(any()))
                .thenReturn(Validation.valid(StepTestView
                        .builder()
                        .id(1L)
                        .name("StepTestView")
                        .description("StepTestView")
                        .testReferenceDate(LocalDate.of(2019, 1, 1))
                        .status(StepTestStatus.FAIL)
                        .build()));

        mockMvc.perform(
                get("/api/v1/pipelineStepTests/123"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("StepTestView"))
                .andExpect(jsonPath("$.description").value("StepTestView"))
                .andExpect(jsonPath("$.testReferenceDate").value("2019-01-01"))
                .andExpect(jsonPath("$.status").value("FAIL"));
    }

    @Test
    public void getPipelineStepTestById_NotFound_ReturnsBadRequest() throws Exception {
        when(pipelineStepTestService.findById(any()))
                .thenReturn(Validation.invalid(CRUDFailure.notFoundIds("StepTest", 1L)));

        mockMvc.perform(
                get("/api/v1/pipelineStepTests/123"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].errorCode").value("NOT_FOUND"))
                .andExpect(jsonPath("$[0].errorMessage").value("Could not find StepTest for ids [1]"));
    }

    @Test
    public void deletePipelineStepTest_StepExists_ReturnsId() throws Exception {
        when(pipelineStepTestService.deleteById(any()))
                .thenReturn(Validation.valid(() -> 1L));

        mockMvc.perform(
                delete("/api/v1/pipelineStepTests/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath(".id").value(1));
    }

    @Test
    public void deletePipelineStepTest_NotFound_ReturnsBadRequest() throws Exception {
        when(pipelineStepTestService.deleteById(any()))
                .thenReturn(Validation.invalid(CRUDFailure.notFoundIds("StepTest", 1L)));

        mockMvc.perform(
                delete("/api/v1/pipelineStepTests/123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].errorCode").value("NOT_FOUND"))
                .andExpect(jsonPath("$[0].errorMessage").value("Could not find StepTest for ids [1]"));
    }

    @Test
    public void runTest_ReturnsOkResponseWithEntity() throws Exception {
        when(pipelineStepTestService.runTest(anyLong()))
                .thenReturn(Validation.valid(TestRunResultView.builder()
                        .id(1L)
                        .status(StepTestStatus.FAIL)
                        .build()));

        mockMvc.perform(
                post("/api/v1/pipelineStepTests/123/run"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.status").value("FAIL"));
    }

    @Test
    public void deleteInputRow_ReturnsOkResponseWithEntity() throws Exception {
        when(pipelineStepTestRowService.deleteInput(anyLong()))
                .thenReturn(Validation.valid(Design.Populated.inputDataRow().id(2342L).build()));

        mockMvc.perform(
                delete("/api/v1/pipelineStepTests/1234/inputDataRows/2342"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("2342"));
    }

    @Test
    public void deleteExpectedRow_ReturnsOkResponseWithEntity() throws Exception {
        when(pipelineStepTestRowService.deleteExpected(anyLong()))
                .thenReturn(Validation.valid(Design.Populated.expectedDataRow().id(2342L).build()));

        mockMvc.perform(
                delete("/api/v1/pipelineStepTests/1234/expectedDataRows/2342"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("2342"));
    }

    @Test
    public void updatePipelineTest_ReturnsOkResponseWithEntity() throws Exception {
        when(pipelineStepTestService.update(anyLong(), any()))
                .thenReturn(Validation.valid(StepTestView
                        .builder()
                        .id(1L)
                        .name("StepTestView")
                        .description("StepTestView")
                        .testReferenceDate(LocalDate.of(2019, 1, 1))
                        .build()));

        mockMvc.perform(
                patch("/api/v1/pipelineStepTests/1")
                        .contentType(APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(CreateStepTestRequest.builder().build())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("StepTestView"))
                .andExpect(jsonPath("$.description").value("StepTestView"))
                .andExpect(jsonPath("$.testReferenceDate").value("2019-01-01"));
    }

    @Test
    public void getTestInputRows_RowsFound_ReturnsOkResponse() throws Exception {
        Map<Long, List<StepRowInputDataView>> inputRows = Collections.singletonMap(
                1001L,
                Collections.singletonList(
                        StepRowInputDataView.builder()
                                .id(101L)
                                .run(true)
                                .schemaId(1001L)
                                .cells(Collections.singletonMap(801L, StepTestCellView.builder()
                                        .id(929L)
                                        .fieldId(801L)
                                        .data("HELLO")
                                        .build()))
                                .build()));

        when(pipelineStepTestService.getTestInputRows(anyLong()))
                .thenReturn(Validation.valid(inputRows));

        mockMvc.perform(
                get("/api/v1/pipelineStepTests/123/inputDataRows"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.1001[0].id").value(101))
                .andExpect(jsonPath("$.1001[0].run").value(true))
                .andExpect(jsonPath("$.1001[0].cells['801'].id").value(929))
                .andExpect(jsonPath("$.1001[0].cells['801'].fieldId").value(801))
                .andExpect(jsonPath("$.1001[0].cells['801'].data").value("HELLO"));
    }

    @Test
    public void getTestInputRows_CrudFailure_ReturnsBadRequestResponse() throws Exception {
        when(pipelineStepTestService.getTestInputRows(anyLong()))
                .thenReturn(Validation.invalid(CRUDFailure.notFoundIds("StepTest", 123L)));

        mockMvc.perform(
                get("/api/v1/pipelineStepTests/123/inputDataRows"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].errorCode").value("NOT_FOUND"))
                .andExpect(jsonPath("$[0].errorMessage").value("Could not find StepTest for ids [123]"));
    }

    @Test
    public void getTestOutputRows_ReturnsOkResponse() throws Exception {
        when(pipelineStepTestService.getTestOutputRows(anyLong()))
                .thenReturn(Collections.singletonList(
                        StepRowOutputDataView.builder()
                                .id(103L)
                                .run(true)
                                .schemaId(1001L)
                                .cells(Collections.singletonMap(801L, StepTestCellView.builder()
                                        .id(928L)
                                        .fieldId(801L)
                                        .data("HELLO THERE")
                                        .build()))
                                .build()));

        mockMvc.perform(
                get("/api/v1/pipelineStepTests/123/outputDataRows"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id").value(103))
                .andExpect(jsonPath("$.[0].run").value(true))
                .andExpect(jsonPath("$.[0].cells['801'].id").value(928))
                .andExpect(jsonPath("$.[0].cells['801'].fieldId").value(801))
                .andExpect(jsonPath("$.[0].cells['801'].data").value("HELLO THERE"));
    }

    @Test
    public void createPipelineTestImportCsvExpectedDataRow_delegatesSchemaIdToPipelineStepTestRowService() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.csv", "application/csv", new byte[]{});

        mockMvc.perform(multipart("/api/v1/pipelineStepTests/1/schemas/2/import?type=expected")
                .file(file));

        verify(pipelineStepTestRowService).importCsvExpectedDataRow(anyLong(), eq(2L), any());
    }

    @Test
    public void createPipelineTestImportCsvExpectedDataRow_delegatesFileInputStreamToPipelineStepTestRowService() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.csv",
                "application/csv", "content".getBytes());

        mockMvc.perform(multipart("/api/v1/pipelineStepTests/1/schemas/2/import?type=expected")
                .file(file));

        ArgumentCaptor<InputStream> inputStreamCapture = ArgumentCaptor.forClass(InputStream.class);
        verify(pipelineStepTestRowService).importCsvExpectedDataRow(anyLong(), anyLong(), inputStreamCapture.capture());

        InputStream inputStream = inputStreamCapture.getValue();
        final String csvContent = IOUtils.readLines(inputStream, "utf-8")
                .get(0);
        assertThat(csvContent).isEqualTo("content");
    }

    @Test
    public void createPipelineTestImportCsvExpectedDataRow_returnsOkResponseWithFieldCellData() throws Exception {
        List<StepTestRowView> createdSteps = asList(StepTestRowView.builder()
                .id(1L)
                .cells(Collections.singletonList(StepTestRowView.CellIdAndField.builder()
                        .id(2L)
                        .cellData("Cell data")
                        .fieldId(3L)
                        .build()))
                .build());

        when(pipelineStepTestRowService.importCsvExpectedDataRow(anyLong(), anyLong(), any()))
                .thenReturn(Validation.valid(createdSteps));

        MockMultipartFile file = new MockMultipartFile("file", "test.csv",
                "application/csv", "content".getBytes());

        mockMvc.perform(
                multipart("/api/v1/pipelineStepTests/1/schemas/2/import?type=expected")
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cells[0].cellData").value("Cell data"));
    }

    @Test
    public void exportPipelineTestCsvInputDataRow_FileServiceReturnsNoErrorMessage_ReturnsOkResponse() throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write("abcd".getBytes());

        when(pipelineStepTestRowService.exportCsvInputDataRow(anyLong(), anyLong(), any()))
                .thenReturn(Validation.valid(
                        new CsvUtils.CsvOutputStream<>("schemaName", byteArrayOutputStream)));

        MvcResult mvcResult = mockMvc.perform(
                get("/api/v1/pipelineStepTests/123/schemas/456/export?type=input"))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();

        assertThat(response.getHeader(HttpHeaders.CONTENT_DISPOSITION))
                .isEqualTo("attachment; filename=\"schemaName.csv\"");
        assertThat(response.getHeader("Cache-Control"))
                .isEqualTo("no-cache, no-store, must-revalidate");
        assertThat(response.getContentType())
                .isEqualTo("text/csv");
        assertThat(response.getContentLength())
                .isEqualTo(4);
        assertThat(response.getContentAsString())
                .isEqualTo("abcd");
    }

    @Test
    public void exportPipelineTestCsvExpectedDataRow_FileServiceReturnsNoErrorMessage_ReturnsOkResponse() throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write("abcd".getBytes());

        when(pipelineStepTestRowService.exportCsvExpectedDataRow(anyLong(), anyLong(), any()))
                .thenReturn(Validation.valid(
                        new CsvUtils.CsvOutputStream<>("schemaName", byteArrayOutputStream)));

        MvcResult mvcResult = mockMvc.perform(
                get("/api/v1/pipelineStepTests/123/schemas/456/export?type=expected"))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();

        assertThat(response.getHeader(HttpHeaders.CONTENT_DISPOSITION))
                .isEqualTo("attachment; filename=\"schemaName.csv\"");
        assertThat(response.getHeader("Cache-Control"))
                .isEqualTo("no-cache, no-store, must-revalidate");
        assertThat(response.getContentType())
                .isEqualTo("text/csv");
        assertThat(response.getContentLength())
                .isEqualTo(4);
        assertThat(response.getContentAsString())
                .isEqualTo("abcd");
    }

    @Test
    public void exportPipelineTestCsvActualDataRow_FileServiceReturnsNoErrorMessage_ReturnsOkResponse() throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write("abcd".getBytes());

        when(pipelineStepTestRowService.exportCsvActualDataRow(anyLong(), anyLong(), any()))
                .thenReturn(Validation.valid(
                        new CsvUtils.CsvOutputStream<>("schemaName", byteArrayOutputStream)));

        MvcResult mvcResult = mockMvc.perform(
                get("/api/v1/pipelineStepTests/123/schemas/456/export?type=actual"))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();

        MockHttpServletResponse response = mvcResult.getResponse();

        assertThat(response.getHeader(HttpHeaders.CONTENT_DISPOSITION))
                .isEqualTo("attachment; filename=\"schemaName.csv\"");
        assertThat(response.getHeader("Cache-Control"))
                .isEqualTo("no-cache, no-store, must-revalidate");
        assertThat(response.getContentType())
                .isEqualTo("text/csv");
        assertThat(response.getContentLength())
                .isEqualTo(4);
        assertThat(response.getContentAsString())
                .isEqualTo("abcd");
    }
}
