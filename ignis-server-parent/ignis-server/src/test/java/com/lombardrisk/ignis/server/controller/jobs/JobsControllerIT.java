package com.lombardrisk.ignis.server.controller.jobs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import com.lombardrisk.ignis.client.external.fixture.ExternalClient;
import com.lombardrisk.ignis.client.external.job.ExternalExitStatus;
import com.lombardrisk.ignis.client.external.job.JobExecutionView;
import com.lombardrisk.ignis.client.external.job.JobStatus;
import com.lombardrisk.ignis.client.external.job.JobType;
import com.lombardrisk.ignis.client.external.job.pipeline.PipelineRequest;
import com.lombardrisk.ignis.client.external.job.staging.request.v1.StagingRequest;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.data.common.failure.NotFoundFailure;
import com.lombardrisk.ignis.server.batch.JobOperatorImpl;
import com.lombardrisk.ignis.server.batch.JobService;
import com.lombardrisk.ignis.server.config.IntegrationTestConfig;
import com.lombardrisk.ignis.server.job.JobFailure;
import com.lombardrisk.ignis.server.job.exception.JobStartException;
import com.lombardrisk.ignis.server.job.pipeline.PipelineJobService;
import com.lombardrisk.ignis.server.job.staging.StagingDatasetService;
import io.vavr.control.Either;
import io.vavr.control.Validation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.togglz.core.manager.FeatureManager;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;

import static com.lombardrisk.ignis.common.json.MapperWrapper.MAPPER;
import static com.lombardrisk.ignis.test.config.AdminUser.BASIC_AUTH;
import static com.lombardrisk.ignis.test.config.AdminUser.USERNAME;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@IntegrationTestConfig
public class JobsControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StagingDatasetService stagingDatasetService;
    @MockBean
    private JobService jobService;
    @MockBean
    private PipelineJobService pipelineJobService;
    @MockBean
    private JobOperatorImpl jobOperator;

    @MockBean
    private FeatureManager featureManager;

    @Captor
    private ArgumentCaptor<Pageable> pageableArgumentCaptor;

    @Before
    public void setUp() {
        when(pipelineJobService.startJob(any(PipelineRequest.class), anyString()))
                .thenReturn(Validation.valid(() -> 123L));
    }

    @Test
    public void startStagingJob_StagingRequest_CallsStagingDatasetServiceWithRequestAndUsername() throws Exception {
        when(stagingDatasetService.start(any(StagingRequest.class), anyString()))
                .thenReturn(Validation.valid(1L));

        StagingRequest stagingRequest = ExternalClient.Populated.stagingRequest().build();
        mockMvc.perform(
                post("/api/v1/jobs?type=staging")
                        .with(BASIC_AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(stagingRequest)))
                .andExpect(status().isOk());

        verify(stagingDatasetService).start(stagingRequest, "admin");
    }

    @Test
    public void startStagingJob_StagingRequest_ReturnsNewJobId() throws Exception {
        when(stagingDatasetService.start(any(StagingRequest.class), anyString()))
                .thenReturn(Validation.valid(1829L));

        StagingRequest stagingRequest = ExternalClient.Populated.stagingRequest().build();
        mockMvc.perform(
                post("/api/v1/jobs?type=staging")
                        .with(BASIC_AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(stagingRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1829));
    }

    @Test
    public void startStagingJob_CRUDFailure_ReturnsErrorList() throws Exception {
        NotFoundFailure<String> notFoundFailure = CRUDFailure.notFoundIndices(
                "name", "Table", ImmutableSet.of("table1", "table2"));

        when(stagingDatasetService.start(any(StagingRequest.class), anyString()))
                .thenReturn(Validation.invalid(singletonList(notFoundFailure)));

        StagingRequest stagingRequest = ExternalClient.Populated.stagingRequest().build();
        mockMvc.perform(
                post("/api/v1/jobs?type=staging")
                        .with(BASIC_AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(stagingRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].errorCode").value("NOT_FOUND"))
                .andExpect(jsonPath("$[0].errorMessage").value(
                        "Could not find Table for names [table1, table2]"));
    }

    @Test
    public void startStagingJob_ServiceThrowsJobException_ReturnsErrorObject() throws Exception {
        when(stagingDatasetService.start(any(StagingRequest.class), anyString()))
                .thenThrow(new JobStartException("not today my friend"));

        StagingRequest stagingRequest = ExternalClient.Populated.stagingRequest().build();
        mockMvc.perform(
                post("/api/v1/jobs?type=staging")
                        .with(BASIC_AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(stagingRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$[0].errorCode").value(nullValue()))
                .andExpect(jsonPath("$[0].errorMessage").value(
                        "An unexpected error occurred. If it persists please contact your FCR administrator"));
    }

    @Test
    public void startJobValidation_CallsJobService() throws Exception {
        String validationJobRequestJson =
                objectMapper.createObjectNode()
                        .put("datasetId", 2345L)
                        .put("name", "source view name")
                        .toString();

        mockMvc.perform(
                post("/api/v1/jobs?type=validation")
                        .contentType(APPLICATION_JSON_UTF8)
                        .content(validationJobRequestJson)
                        .with(BASIC_AUTH));

        verify(jobService).startValidationJob(2345L, "source view name", USERNAME);
    }

    @Test
    public void startJobValidation_Request_ReturnsJobResource() throws Exception {
        when(jobService.startValidationJob(any(), any(), anyString())).thenReturn(Either.right(567L));

        String validationJobRequestJson =
                objectMapper.createObjectNode()
                        .put("datasetId", 23)
                        .put("name", "myFirstDQ")
                        .toString();
        mockMvc.perform(
                post("/api/v1/jobs?type=validation")
                        .contentType(APPLICATION_JSON_UTF8)
                        .content(validationJobRequestJson)
                        .with(BASIC_AUTH))

                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(567));
    }

    @Test
    public void startJobValidation_JobFailsToStart_ReturnsErrorMessage() throws Exception {
        when(jobService.startValidationJob(
                any(),
                any(),
                anyString())).thenReturn(Either.left(JobFailure.JOB_COULD_NOT_START));

        String validationJobRequestJson =
                objectMapper.createObjectNode()
                        .put("datasetId", 23)
                        .put("name", "myFirstDQ")
                        .toString();

        mockMvc.perform(
                post("/api/v1/jobs?type=validation")
                        .contentType(APPLICATION_JSON_UTF8)
                        .content(validationJobRequestJson)
                        .with(BASIC_AUTH))

                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$[0].errorMessage").value("An unexpected error occurred, could not start job"))
                .andExpect(jsonPath("$[0].errorCode").value("JOB_COULD_NOT_START"));
    }

    @Test
    public void startJobValidation_DatasetNotFound_ReturnsBadRequest() throws Exception {
        when(jobService.startValidationJob(
                any(),
                any(),
                anyString())).thenReturn(Either.left(JobFailure.DATASET_NOT_FOUND));

        String validationJobRequestJson =
                objectMapper.createObjectNode()
                        .put("datasetId", 23)
                        .put("name", "myFirstDQ")
                        .toString();

        mockMvc.perform(
                post("/api/v1/jobs?type=validation")
                        .contentType(APPLICATION_JSON_UTF8)
                        .content(validationJobRequestJson)
                        .with(BASIC_AUTH))

                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$[0].errorMessage").value("Dataset not found for id 23"))
                .andExpect(jsonPath("$[0].errorCode").value("DATASET_NOT_FOUND"));
    }

    @Test
    public void startJobValidation_InvalidRequest_ReturnsErrors() throws Exception {
        String validationJobRequestJson =
                objectMapper.createObjectNode()
                        .put("name", "myFirstDQ")
                        .toString();

        mockMvc.perform(
                post("/api/v1/jobs?type=validation")
                        .contentType(APPLICATION_JSON_UTF8)
                        .content(validationJobRequestJson)
                        .with(BASIC_AUTH))

                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$[0].errorMessage").value("must not be null"))
                .andExpect(jsonPath("$[0].errorCode").value("datasetId"));
    }

    @Test
    public void startPipelineJob_StartsSuccessfully_ReturnsId() throws Exception {
        when(pipelineJobService.startJob(any(PipelineRequest.class), anyString()))
                .thenReturn(Validation.valid(() -> 100L));

        PipelineRequest pipelineRequest = ExternalClient.Populated.pipelineItemRequest().build();

        mockMvc.perform(
                post("/api/v1/jobs?type=pipeline")
                        .with(BASIC_AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(pipelineRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L));
    }

    @Test
    public void startPipelineJob_PipelineRequestWithStepIdStartsSuccessfully_ReturnsId() throws Exception {
        when(pipelineJobService.startJob(any(PipelineRequest.class), anyString()))
                .thenReturn(Validation.valid(() -> 100L));

        PipelineRequest pipelineRequest = ExternalClient.Populated.pipelineItemRequest()
                .stepId(125L)
                .build();

        mockMvc.perform(
                post("/api/v1/jobs?type=pipeline")
                        .with(BASIC_AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(pipelineRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100L));
    }

    @Test
    public void startPipelineJob_StartsSuccessfully_PassesUserNameToService() throws Exception {
        PipelineRequest pipelineRequest = ExternalClient.Populated.pipelineItemRequest().build();

        mockMvc.perform(
                post("/api/v1/jobs?type=pipeline")
                        .with(BASIC_AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(pipelineRequest)))
                .andExpect(status().isOk());

        verify(pipelineJobService).startJob(pipelineRequest, "admin");
    }

    @Test
    public void startPipelineJob_JobDoesNotStart_ReturnsError() throws Exception {
        when(pipelineJobService.startJob(any(PipelineRequest.class), anyString()))
                .thenReturn(Validation.invalid(singletonList(
                        ErrorResponse.valueOf("oops", "NAH"))));

        PipelineRequest pipelineRequest = ExternalClient.Populated.pipelineItemRequest().build();

        mockMvc.perform(
                post("/api/v1/jobs?type=pipeline")
                        .with(BASIC_AUTH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(MAPPER.writeValueAsString(pipelineRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].errorMessage").value("oops"))
                .andExpect(jsonPath("$[0].errorCode").value("NAH"));
    }

    @Test
    public void getAllJobs_ReturnsListOfJobs() throws Exception {
        ZonedDateTime startTime = ZonedDateTime.of(2019, 8, 1, 20, 26, 0, 0, ZoneId.of("UTC"));
        ZonedDateTime endTime = startTime.plusMinutes(10);

        JobExecutionView jobExecution1 = JobExecutionView.builder()
                .id(101L)
                .name("my job 1")
                .serviceRequestType(JobType.STAGING)
                .status(JobStatus.COMPLETED)
                .exitCode(ExternalExitStatus.COMPLETED)
                .startTime(startTime)
                .endTime(endTime)
                .createUser("my job creator 1")
                .yarnApplicationTrackingUrl("my yarn tracking url 1")
                .build();
        JobExecutionView jobExecution2 = JobExecutionView.builder()
                .id(102L)
                .name("my job 2")
                .serviceRequestType(JobType.VALIDATION)
                .status(JobStatus.ABANDONED)
                .exitCode(ExternalExitStatus.NOOP)
                .startTime(startTime)
                .endTime(endTime)
                .createUser("my job creator 2")
                .yarnApplicationTrackingUrl("my yarn tracking url 2")
                .build();

        PageImpl<JobExecutionView> page =
                new PageImpl<>(Arrays.asList(jobExecution1, jobExecution2), PageRequest.of(0, 4), 4);

        when(jobService.getAllJobs(any()))
                .thenReturn(page);

        mockMvc.perform(
                get("/api/v1/jobs")
                        .with(BASIC_AUTH))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(101))
                .andExpect(jsonPath("$.data[0].name").value("my job 1"))
                .andExpect(jsonPath("$.data[0].serviceRequestType").value("STAGING"))
                .andExpect(jsonPath("$.data[0].status").value("COMPLETED"))
                .andExpect(jsonPath("$.data[0].exitCode").value("COMPLETED"))
                .andExpect(jsonPath("$.data[0].startTime").value("2019-08-01T20:26:00+0000"))
                .andExpect(jsonPath("$.data[0].endTime").value("2019-08-01T20:36:00+0000"))
                .andExpect(jsonPath("$.data[0].createUser").value("my job creator 1"))
                .andExpect(jsonPath("$.data[0].yarnApplicationTrackingUrl").value("my yarn tracking url 1"))
                .andExpect(jsonPath("$.data[1].id").value(102))
                .andExpect(jsonPath("$.data[1].name").value("my job 2"))
                .andExpect(jsonPath("$.data[1].serviceRequestType").value("VALIDATION"))
                .andExpect(jsonPath("$.data[1].status").value("ABANDONED"))
                .andExpect(jsonPath("$.data[1].exitCode").value("NOOP"))
                .andExpect(jsonPath("$.data[1].startTime").value("2019-08-01T20:26:00+0000"))
                .andExpect(jsonPath("$.data[1].endTime").value("2019-08-01T20:36:00+0000"))
                .andExpect(jsonPath("$.data[1].createUser").value("my job creator 2"))
                .andExpect(jsonPath("$.data[1].yarnApplicationTrackingUrl").value("my yarn tracking url 2"));
    }

    @Test
    public void getAllJobs_ReturnsPageData() throws Exception {
        JobExecutionView jobExecution1 = JobExecutionView.builder()
                .id(101L)
                .build();

        PageImpl<JobExecutionView> page =
                new PageImpl<>(singletonList(jobExecution1), PageRequest.of(1, 4), 8);

        when(jobService.getAllJobs(any()))
                .thenReturn(page);

        mockMvc.perform(
                get("/api/v1/jobs")
                        .with(BASIC_AUTH))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.size").value(4))
                .andExpect(jsonPath("$.page.number").value(1))
                .andExpect(jsonPath("$.page.totalElements").value(8))
                .andExpect(jsonPath("$.page.totalPages").value(2));
    }

    @Test
    public void getAllJobs_PaginatedRequest_CallsServiceWithPage() throws Exception {
        JobExecutionView jobExecution1 = JobExecutionView.builder()
                .id(101L)
                .build();

        PageImpl<JobExecutionView> page =
                new PageImpl<>(singletonList(jobExecution1), PageRequest.of(1, 4), 8);

        when(jobService.getAllJobs(any()))
                .thenReturn(page);

        mockMvc.perform(
                get("/api/v1/jobs?size=10&page=1")
                        .with(BASIC_AUTH))
                .andDo(print())
                .andExpect(status().isOk());

        verify(jobService).getAllJobs(pageableArgumentCaptor.capture());

        Pageable pageable = pageableArgumentCaptor.getValue();
        assertThat(pageable.getPageSize()).isEqualTo(10);
        assertThat(pageable.getPageNumber()).isEqualTo(1);
    }

    @Test
    public void getAllJobs_SearchParamProvided_ReturnsPageData() throws Exception {
        JobExecutionView jobExecution1 = JobExecutionView.builder()
                .id(101L)
                .build();

        PageImpl<JobExecutionView> page =
                new PageImpl<>(singletonList(jobExecution1), PageRequest.of(1, 4), 8);

        when(jobService.searchJobs(any(), any()))
                .thenReturn(page);

        mockMvc.perform(
                get("/api/v1/jobs?search=searchthis")
                        .with(BASIC_AUTH))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.size").value(4))
                .andExpect(jsonPath("$.page.number").value(1))
                .andExpect(jsonPath("$.page.totalElements").value(8))
                .andExpect(jsonPath("$.page.totalPages").value(2));
    }

    @Test
    public void getAllJobs_SearchParamProvided_CallsService() throws Exception {
        JobExecutionView jobExecution1 = JobExecutionView.builder()
                .id(101L)
                .build();

        PageImpl<JobExecutionView> page =
                new PageImpl<>(singletonList(jobExecution1), PageRequest.of(1, 4), 8);

        when(jobService.searchJobs(any(), any()))
                .thenReturn(page);

        mockMvc.perform(
                get("/api/v1/jobs?size=10&page=1&search=searchthis")
                        .with(BASIC_AUTH))
                .andDo(print())
                .andExpect(status().isOk());

        verify(jobService).searchJobs(pageableArgumentCaptor.capture(), eq("searchthis"));
        verifyNoMoreInteractions(jobService);

        Pageable pageable = pageableArgumentCaptor.getValue();
        assertThat(pageable.getPageSize()).isEqualTo(10);
        assertThat(pageable.getPageNumber()).isEqualTo(1);
    }

    @Test
    public void getJob_ShouldPassAndReturnHttp200() throws Exception {
        ZonedDateTime startTime = ZonedDateTime.of(2019, 8, 1, 20, 26, 0, 0, ZoneId.of("UTC"));
        ZonedDateTime endTime = startTime.plusMinutes(10);

        JobExecutionView execution =
                JobExecutionView.builder()
                        .id(100)
                        .exitCode(ExternalExitStatus.FAILED)
                        .name("JOB_NAME")
                        .status(JobStatus.COMPLETED)
                        .yarnApplicationTrackingUrl("urlIRL")
                        .startTime(startTime)
                        .endTime(endTime)
                        .serviceRequestType(JobType.VALIDATION)
                        .createUser("admin")
                        .requestMessage("Please stop doing that")
                        .build();

        when(jobService.getJob(anyLong())).thenReturn(Validation.valid(execution));

        mockMvc.perform(
                get("/api/v1/jobs/1")
                        .with(BASIC_AUTH))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.exitCode").value("FAILED"))
                .andExpect(jsonPath("$.name").value("JOB_NAME"))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.yarnApplicationTrackingUrl").value("urlIRL"))
                .andExpect(jsonPath("$.startTime").value("2019-08-01T20:26:00+0000"))
                .andExpect(jsonPath("$.endTime").value("2019-08-01T20:36:00+0000"))
                .andExpect(jsonPath("$.serviceRequestType").value("VALIDATION"))
                .andExpect(jsonPath("$.createUser").value("admin"))
                .andExpect(jsonPath("$.requestMessage").value("Please stop doing that"));
    }

    @Test
    public void getJob_JobNotFound_ShouldReturnErrorResponse() throws Exception {
        when(jobService.getJob(anyLong()))
                .thenReturn(Validation.invalid(CRUDFailure.notFoundIds("ServiceRequest", 1L)));

        mockMvc.perform(
                get("/api/v1/jobs/1")
                        .with(BASIC_AUTH))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].errorCode").value("NOT_FOUND"))
                .andExpect(jsonPath("$[0].errorMessage").value("Could not find ServiceRequest for ids [1]"));
    }

    @Test
    public void terminateJob_ShouldPassAndReturnHttp200() throws Exception {
        when(jobOperator.stop(anyLong())).thenReturn(Validation.valid(true));

        mockMvc.perform(
                put("/api/v1/jobs/1/stop")
                        .with(BASIC_AUTH)
                        .with(BASIC_AUTH)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().bytes("true".getBytes()));
    }

    @Test
    public void terminateJob_JobNotFound_ReturnsErrorResponse() throws Exception {
        when(jobOperator.stop(anyLong()))
                .thenReturn(Validation.invalid(singletonList(
                        ErrorResponse.valueOf("It failed", "OOPS_1"))));

        mockMvc.perform(
                put("/api/v1/jobs/1/stop")
                        .with(BASIC_AUTH)
                        .with(BASIC_AUTH)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].errorCode").value("OOPS_1"))
                .andExpect(jsonPath("$[0].errorMessage").value("It failed"));
    }
}
