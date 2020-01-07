package com.lombardrisk.ignis.server.controller;

import com.google.common.collect.ImmutableList;
import com.lombardrisk.ignis.api.dataset.DatasetState;
import com.lombardrisk.ignis.common.fixtures.PopulatedDates;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.server.batch.JobOperatorImpl;
import com.lombardrisk.ignis.server.config.IntegrationTestConfig;
import com.lombardrisk.ignis.server.job.fixture.JobPopulated;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequestRepository;
import com.lombardrisk.ignis.server.job.staging.StagingDatasetService;
import com.lombardrisk.ignis.server.job.staging.file.ErrorFileLinkOrStream;
import com.lombardrisk.ignis.server.job.staging.model.StagingDataset;
import io.vavr.control.Validation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Date;

import static com.lombardrisk.ignis.api.dataset.DatasetState.UPLOADING;
import static com.lombardrisk.ignis.test.config.AdminUser.BASIC_AUTH;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@IntegrationTestConfig
public class StagingControllerIT {

    @Autowired
    protected MockMvc mvc;

    @MockBean
    protected JobOperatorImpl jobOperator;

    @MockBean
    private ServiceRequestRepository serviceRequestRepository;

    @MockBean
    private StagingDatasetService stagingDatasetService;

    @MockBean
    private JobExplorer jobExplorer;

    @Test
    public void findStagingDataset_DatasetExists_ReturnsStagingDataset() throws Exception {
        Date startTime = PopulatedDates.toDateTime("2000-01-01T00:00:01");
        Date endTime = PopulatedDates.toDateTime("2000-02-01T00:00:01");
        Date lastUpdateTime = PopulatedDates.toDateTime("2000-02-01T00:10:01");

        StagingDataset stagingDataset = StagingDataset.builder()
                .id(1L)
                .status(DatasetState.UPLOADING)
                .validationErrorFile("derp derp")
                .stagingFile("pred pred")
                .message("hello")
                .startTime(startTime)
                .endTime(endTime)
                .lastUpdateTime(lastUpdateTime)
                .build();

        when(stagingDatasetService.findWithValidation(1)).thenReturn(Validation.valid(
                stagingDataset));

        mvc.perform(
                get("/api/v1/stagingItems/1")
                        .with(BASIC_AUTH)
                        .contentType(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("UPLOADING"))
                .andExpect(jsonPath("$.startTime").value(startTime))
                .andExpect(jsonPath("$.endTime").value(endTime))
                .andExpect(jsonPath("$.lastUpdateTime").value(lastUpdateTime))
                .andExpect(jsonPath("$.message").value("hello"))
                .andExpect(jsonPath("$.stagingFile").value("pred pred"))
                .andExpect(jsonPath("$.validationErrorFile").value("derp derp"))
                .andExpect(jsonPath("$.validationErrorFileUrl").value(
                        "http://localhost/fcrengine/api/v1/stagingItems/1/validationError"));
    }

    @Test
    public void findStagingDataset_DatasetNotFound_ReturnsError() throws Exception {
        when(stagingDatasetService.findWithValidation(anyLong()))
                .thenReturn(Validation.invalid(CRUDFailure.notFoundIds("StagingDataset", singletonList(1L))));

        mvc.perform(
                get("/api/v1/stagingItems/1")
                        .with(BASIC_AUTH)
                        .contentType(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].errorCode").value("NOT_FOUND"))
                .andExpect(jsonPath("$[0].errorMessage").value("Could not find StagingDataset for ids [1]"));
    }

    @Test
    public void findStagingDatasets_MultipleFound_ReturnsList() throws Exception {

        Date startTime = PopulatedDates.toDateTime("2000-01-01T00:00:01");
        Date endTime = PopulatedDates.toDateTime("2000-02-01T00:00:01");
        Date lastUpdateTime = PopulatedDates.toDateTime("2000-02-01T00:10:01");

        StagingDataset stagingDataset1 = StagingDataset.builder()
                .id(5L)
                .status(DatasetState.UPLOADING)
                .validationErrorFile("derp derp")
                .stagingFile("pred pred")
                .message("hello1")
                .startTime(startTime)
                .endTime(endTime)
                .lastUpdateTime(lastUpdateTime)
                .build();

        StagingDataset stagingDataset2 = StagingDataset.builder()
                .id(6L)
                .status(DatasetState.REGISTRATION_FAILED)
                .validationErrorFile("errorFile")
                .stagingFile("stagingFile")
                .message("hello2")
                .startTime(startTime)
                .endTime(endTime)
                .lastUpdateTime(lastUpdateTime)
                .build();

        when(stagingDatasetService.findStagingDatasets(5L, null, "employee"))
                .thenReturn(Validation.valid(ImmutableList.of(stagingDataset1, stagingDataset2)));

        mvc.perform(
                get("/api/v1/stagingItems?jobId=5&itemName=employee")
                        .with(BASIC_AUTH)
                        .contentType(APPLICATION_JSON))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5))
                .andExpect(jsonPath("$[0].status").value("UPLOADING"))
                .andExpect(jsonPath("$[0].validationErrorFile").value("derp derp"))
                .andExpect(jsonPath("$[0].stagingFile").value("pred pred"))
                .andExpect(jsonPath("$[0].message").value("hello1"))
                .andExpect(jsonPath("$[0].startTime").value(startTime))
                .andExpect(jsonPath("$[0].endTime").value(endTime))
                .andExpect(jsonPath("$[0].lastUpdateTime").value(lastUpdateTime))
                .andExpect(jsonPath("$[1].id").value(6))
                .andExpect(jsonPath("$[1].status").value("REGISTRATION_FAILED"))
                .andExpect(jsonPath("$[1].validationErrorFile").value("errorFile"))
                .andExpect(jsonPath("$[1].stagingFile").value("stagingFile"))
                .andExpect(jsonPath("$[1].message").value("hello2"))
                .andExpect(jsonPath("$[1].startTime").value(startTime))
                .andExpect(jsonPath("$[1].endTime").value(endTime))
                .andExpect(jsonPath("$[1].lastUpdateTime").value(lastUpdateTime));
    }

    @Test
    public void findStagingDatasets_CRUDFailureReturned_ReturnsBadRequest() throws Exception {
        when(stagingDatasetService.findStagingDatasets(any(), any(), any()))
                .thenReturn(Validation.invalid(CRUDFailure.invalidRequestParameter("invalid", "parameters")));

        mvc.perform(
                get("/api/v1/stagingItems?jobId=5&itemName=employee")
                        .with(BASIC_AUTH)
                        .contentType(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].errorCode").value("INVALID_REQUEST_PARAMETERS"));
    }

    @Test
    public void downloadValidationErrorFile_ValidationFileExists_ReturnsFileAndStatusNoContent() throws Exception {
        when(stagingDatasetService.downloadStagingErrorFile(31))
                .thenReturn(Validation.valid(
                        ErrorFileLinkOrStream.fileStream(
                                "employee",
                                new ByteArrayInputStream("CSV_CONTENT".getBytes()))));

        mvc.perform(
                get("/api/v1/stagingItems/31/validationError")
                        .with(BASIC_AUTH)
                        .contentType(APPLICATION_JSON))

                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv"))
                .andExpect(content().bytes("CSV_CONTENT".getBytes()));
    }

    @Test
    public void downloadValidationErrorFile_ValidationFileForwardLink_ReturnsRedirectHeaders() throws Exception {
        when(stagingDatasetService.downloadStagingErrorFile(31))
                .thenReturn(Validation.valid(
                        ErrorFileLinkOrStream.fileLink(new URL("https://s3.com/file/myFile"))));

        mvc.perform(
                get("/api/v1/stagingItems/31/validationError")
                        .with(BASIC_AUTH)
                        .contentType(APPLICATION_JSON))

                .andExpect(status().isFound())
                .andExpect(header().stringValues("Location", "https://s3.com/file/myFile"));
    }

    @Test
    public void downloadValidationErrorFile_NonExistentValidationFile_ReturnsBadRequest() throws Exception {
        when(stagingDatasetService.downloadStagingErrorFile(3456))
                .thenReturn(Validation.invalid(CRUDFailure.notFoundIds("StagingDataset", 3456L)));

        mvc.perform(
                get("/api/v1/stagingItems/3456/validationError")
                        .with(BASIC_AUTH)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].errorCode").value("DATASET_ERRORS_FILE_NOT_FOUND"))
                .andExpect(jsonPath("$[0].errorMessage").value("Could not find StagingDataset for ids [3456]"));
    }

    @Test
    public void updateDataSetState_CallsStagingDatasetService() throws Exception {
        when(stagingDatasetService.updateStagingDatasetState(anyLong(), any()))
                .thenReturn(
                        JobPopulated.stagingDataset()
                                .id(333L)
                                .build());

        mvc.perform(
                put("/api/v1/stagingItems/333?state=UPLOADING&predicate=&recordsCount=0")
                        .with(BASIC_AUTH)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(stagingDatasetService).updateStagingDatasetState(333L, UPLOADING);
    }
}
