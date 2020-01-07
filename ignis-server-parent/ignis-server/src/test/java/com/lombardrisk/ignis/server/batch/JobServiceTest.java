package com.lombardrisk.ignis.server.batch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lombardrisk.ignis.client.external.job.ExternalExitStatus;
import com.lombardrisk.ignis.client.external.job.JobExecutionView;
import com.lombardrisk.ignis.client.external.job.JobStatus;
import com.lombardrisk.ignis.client.external.job.JobType;
import com.lombardrisk.ignis.common.assertions.VavrAssert;
import com.lombardrisk.ignis.common.log.CorrelationId;
import com.lombardrisk.ignis.common.time.TimeSource;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.data.common.search.Filter;
import com.lombardrisk.ignis.data.common.search.FilterExpression;
import com.lombardrisk.ignis.data.common.search.FilterOption;
import com.lombardrisk.ignis.server.dataset.DatasetJpaRepository;
import com.lombardrisk.ignis.server.dataset.fixture.DatasetPopulated;
import com.lombardrisk.ignis.server.fixtures.Populated;
import com.lombardrisk.ignis.server.job.JobFailure;
import com.lombardrisk.ignis.server.job.exception.JobStartException;
import com.lombardrisk.ignis.server.job.fixture.JobPopulated;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequest;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequestRepository;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequestSpecificationBuilder;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequestType;
import com.lombardrisk.ignis.server.job.staging.JobStarter;
import io.vavr.control.Either;
import io.vavr.control.Option;
import io.vavr.control.Validation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.Properties;

import static com.lombardrisk.ignis.client.external.job.JobType.STAGING;
import static com.lombardrisk.ignis.common.fixtures.PopulatedDates.toDate;
import static com.lombardrisk.ignis.server.job.JobFailure.DATASET_NOT_FOUND;
import static com.lombardrisk.ignis.server.job.JobFailure.JOB_COULD_NOT_START;
import static com.lombardrisk.ignis.server.job.servicerequest.ServiceRequestType.VALIDATION;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class JobServiceTest {

    @Mock
    private ServiceRequestRepository serviceRequestRepository;
    @Mock
    private ServiceRequestSpecificationBuilder specificationBuilder;
    @Mock
    private JobExplorer jobExplorer;
    @Mock
    private JobStarter jobStarter;
    @Mock
    private DatasetJpaRepository datasetRepository;
    @Mock
    private TimeSource timeSource;
    @InjectMocks
    private JobService jobService;

    @Captor
    private ArgumentCaptor<ServiceRequest> serviceRequestCaptor;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUp() {
        when(serviceRequestRepository.save(any(ServiceRequest.class)))
                .thenAnswer(invocation -> invocation.getArguments()[0]);

        when(datasetRepository.findById(anyLong()))
                .thenReturn(Optional.of(
                        DatasetPopulated.dataset().build()));

        ServiceRequest serviceRequest = JobPopulated.stagingJobServiceRequest().id(1231L).build();

        Page<ServiceRequest> serviceRequestPage = new PageImpl<>(singletonList(serviceRequest));

        when(serviceRequestRepository.findAll(any(Pageable.class)))
                .thenReturn(serviceRequestPage);

        when(specificationBuilder.build(any()))
                .thenReturn(Validation.invalid(
                        singletonList(CRUDFailure.invalidRequestParameter("search", "invalid"))));

        JobExecution jobExecution = new JobExecution(121L);
        jobExecution.setExitStatus(ExitStatus.COMPLETED);
        jobExecution.setStatus(BatchStatus.COMPLETED);
        jobExecution.setStartTime(new Date());
        jobExecution.setEndTime(new Date());

        when(jobExplorer.getJobExecution(any()))
                .thenReturn(jobExecution);
    }

    @Test
    public void findById_ServiceRequestFound_ReturnsServiceRequest() {
        ServiceRequest serviceRequest = JobPopulated.stagingJobServiceRequest().id(12345L).build();

        when(serviceRequestRepository.findById(anyLong()))
                .thenReturn(Optional.of(serviceRequest));

        Option<ServiceRequest> found = jobService.findById(12345L);

        assertThat(found)
                .isNotEmpty();

        assertThat(found.get())
                .isSameAs(serviceRequest);
    }

    @Test
    public void findById_CallsServiceRepository() {
        ServiceRequest serviceRequest = JobPopulated.stagingJobServiceRequest().id(12345L).build();

        when(serviceRequestRepository.findById(anyLong()))
                .thenReturn(Optional.of(serviceRequest));

        jobService.findById(12345L);

        verify(serviceRequestRepository).findById(12345L);
    }

    @Test
    public void startJob_DatasetNotFound_ReturnsError() {
        when(datasetRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        Either<JobFailure, Long> jobResult = jobService.startValidationJob(123L, "validate dataset 123", "user");

        assertThat(jobResult.getLeft())
                .isEqualTo(DATASET_NOT_FOUND);
    }

    @Test
    public void startJob_DatasetId_CallsServiceRequestRepository() {
        CorrelationId.setCorrelationId("correlation");

        jobService.startValidationJob(123L, "validate dataset 123", "user");

        verify(serviceRequestRepository, times(1)).save(serviceRequestCaptor.capture());

        assertThat(serviceRequestCaptor.getValue())
                .extracting(
                        ServiceRequest::getName,
                        ServiceRequest::getRequestMessage,
                        ServiceRequest::getCreatedBy,
                        ServiceRequest::getServiceRequestType)
                .containsExactly("validate dataset 123", "123", "user", VALIDATION);
    }

    @Test
    public void startJob_returnsServiceRequestId() throws JobStartException {
        CorrelationId.setCorrelationId("correlation");

        when(serviceRequestRepository.save(any(ServiceRequest.class)))
                .thenReturn(JobPopulated.stagingJobServiceRequest()
                        .id(100L)
                        .build());

        when(jobStarter.startJob(any(), any(), any()))
                .thenReturn(100L);

        Either<JobFailure, Long> jobId = jobService.startValidationJob(0L, null, null);

        assertThat(jobId.right().get()).isEqualTo(100L);
    }

    @Test
    public void startJob_CallsJobOperatorWithNameAndProperties() throws Exception {
        CorrelationId.setCorrelationId("correlation");

        ServiceRequest serviceRequest = JobPopulated.stagingJobServiceRequest()
                .id(456L)
                .name("validate dataset 456")
                .build();

        when(serviceRequestRepository.save(any(ServiceRequest.class)))
                .thenReturn(serviceRequest);

        jobService.startValidationJob(456L, "validate dataset 456", "user");

        verify(jobStarter).startJob(VALIDATION.name(), serviceRequest, new Properties());
    }

    @Test
    public void startJob_JobOperatorThrowsException_ReturnsFailure() throws Exception {
        CorrelationId.setCorrelationId("correlation");

        when(serviceRequestRepository.save(any(ServiceRequest.class)))
                .thenReturn(JobPopulated.stagingJobServiceRequest()
                        .build());

        when(jobStarter.startJob(any(), any(), any())).
                thenThrow(new JobStartException("Whoa nelly"));

        Either<JobFailure, Long> jobResult = jobService.startValidationJob(0L, null, null);

        assertThat(jobResult.getLeft())
                .isEqualTo(JOB_COULD_NOT_START);
    }

    @Test
    public void getJob_ServiceRequestNotFound_ReturnsCrudFailure() {
        when(serviceRequestRepository.findById(any()))
                .thenReturn(Optional.empty());

        Validation<CRUDFailure, JobExecutionView> jobExecution = jobService.getJob(100L);

        assertThat(jobExecution.getError())
                .isEqualTo(CRUDFailure.notFoundIds("ServiceRequest", 100L));
    }

    @Test
    public void getJob_JobExecutionNotFound_ReturnsCrudFailure() {
        when(serviceRequestRepository.findById(any()))
                .thenReturn(Optional.of(JobPopulated.stagingJobServiceRequest()
                        .id(12345L)
                        .jobExecutionId(100L)
                        .build()));

        when(jobExplorer.getJobExecution(any()))
                .thenReturn(null);

        Validation<CRUDFailure, JobExecutionView> jobExecution = jobService.getJob(12345L);

        assertThat(jobExecution.getError())
                .isEqualTo(CRUDFailure.notFoundIds("ServiceRequest", 12345L));
    }

    @Test
    public void getJob_JobExecutionFound_ReturnsExecution() {
        when(serviceRequestRepository.findById(any()))
                .thenReturn(Optional.of(JobPopulated.validationJobServiceRequest()
                        .id(100L)
                        .name("staging job 1")
                        .createdBy("admin")
                        .requestMessage("request")
                        .trackingUrl("https://spinning.the.yarn")
                        .startTime(toDate("1997-03-04"))
                        .endTime(toDate("1997-03-05"))
                        .status(JobStatus.COMPLETED)
                        .exitCode(ExternalExitStatus.COMPLETED)
                        .build()));

        ZonedDateTime startTime = ZonedDateTime.now();
        ZonedDateTime endTime = startTime.plusMinutes(15);

        when(timeSource.toZonedDateTime(any(Date.class)))
                .thenReturn(startTime, endTime);

        JobExecutionView jobExecution = VavrAssert.assertValid(jobService.getJob(100L)).getResult();

        assertThat(jobExecution)
                .isEqualTo(JobExecutionView.builder()
                        .id(100)
                        .name("staging job 1")
                        .createUser("admin")
                        .requestMessage("request")
                        .yarnApplicationTrackingUrl("https://spinning.the.yarn")
                        .serviceRequestType(JobType.VALIDATION)
                        .startTime(startTime)
                        .endTime(endTime)
                        .status(JobStatus.COMPLETED)
                        .exitCode(ExternalExitStatus.COMPLETED)
                        .errors(emptyList())
                        .build());
    }

    @Test
    public void getJob_JobExecutionWithMessages_ReturnsExecution() {
        JobExecution jobExecution = Populated.jobExecution();
        StepExecution stepExecution = jobExecution.createStepExecution("import product");
        ExitStatus error = new ExitStatus(
                null,
                "org.springframework.batch.core.JobExecutionException: Cannot create new table [MY_SECOND_SCHEMA], because it already exists\n"
                        + "\tat com.lombardrisk.ignis.server.service.staging.impl.job.MigrationTasklet.execute(MigrationTasklet.java:78)\n"
                        + "\tat com.lombardrisk.ignis.server.service.common.StoppableServiceRequestJobTasklet.execute(StoppableServiceRequestJobTasklet.java:36)\n"
                        + "\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)");

        stepExecution.setExitStatus(error);

        when(jobExplorer.getJobExecution(any()))
                .thenReturn(jobExecution);

        when(serviceRequestRepository.findById(any()))
                .thenReturn(Optional.of(JobPopulated.validationJobServiceRequest()
                        .id(100L)
                        .build()));

        JobExecutionView jobExecutionView = VavrAssert.assertValid(jobService.getJob(100L)).getResult();

        assertThat(jobExecutionView.getErrors())
                .containsExactly("Cannot create new table [MY_SECOND_SCHEMA], because it already exists");
    }

    @Test
    public void getJob_NotAValidationJob_ReturnsExecutionWithoutRequestMessage() {
        when(serviceRequestRepository.findById(any()))
                .thenReturn(Optional.of(JobPopulated.stagingJobServiceRequest()
                        .id(12345678L)
                        .requestMessage("this should be ignored")
                        .build()));

        JobExecutionView jobExecution = VavrAssert.assertValid(jobService.getJob(100L)).getResult();

        assertThat(jobExecution)
                .extracting(JobExecutionView::getRequestMessage)
                .isNull();
    }

    @Test
    public void getAllJobs_DelegatesToServiceRequestRepository() {
        Pageable pageable = mock(Pageable.class);

        jobService.getAllJobs(pageable);

        verify(serviceRequestRepository).findAll(pageable);
    }

    @Test
    public void getAllJobs_ReturnsJobExecution() {
        Date startTime = new Date();
        Date endTime = new Date();

        ServiceRequest serviceRequest = ServiceRequest.builder()
                .id(12111L)
                .trackingUrl("url")
                .name("job name")
                .serviceRequestType(ServiceRequestType.STAGING)
                .status(JobStatus.COMPLETED)
                .exitCode(ExternalExitStatus.COMPLETED)
                .startTime(startTime)
                .endTime(endTime)
                .createdBy("user1234")
                .build();

        ZonedDateTime zonedStartTime = ZonedDateTime.now();
        ZonedDateTime zonedEndTime = zonedStartTime.plusMinutes(15);

        when(timeSource.toZonedDateTime(any(Date.class)))
                .thenReturn(zonedStartTime, zonedEndTime);

        when(serviceRequestRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(singletonList(serviceRequest)));

        Page<JobExecutionView> jobs = jobService.getAllJobs(mock(Pageable.class));

        JobExecutionView expectedJobExecution =
                JobExecutionView.builder()
                        .id(12111L)
                        .name("job name")
                        .serviceRequestType(STAGING)
                        .status(JobStatus.COMPLETED)
                        .exitCode(ExternalExitStatus.COMPLETED)
                        .startTime(zonedStartTime)
                        .endTime(zonedEndTime)
                        .yarnApplicationTrackingUrl("url")
                        .createUser("user1234")
                        .errors(emptyList())
                        .build();

        assertThat(jobs.getContent()).containsExactly(expectedJobExecution);
    }

    @Test
    public void getAllJobs_StartEndDateNull_ReturnsNullDates() {
        ServiceRequest serviceRequest = JobPopulated.stagingJobServiceRequest()
                .id(12111L)
                .startTime(null)
                .endTime(null)
                .build();

        when(serviceRequestRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(singletonList(serviceRequest)));

        Page<JobExecutionView> jobs = jobService.getAllJobs(mock(Pageable.class));

        assertThat(jobs.getContent())
                .extracting(JobExecutionView::getStartTime, JobExecutionView::getEndTime)
                .containsExactly(tuple(null, null));

        verifyZeroInteractions(timeSource);
    }

    @Test
    public void getAllJobs_RequestTypeValidation_ReturnsJobExecutionWithRequestMessage() {
        ServiceRequest request = JobPopulated.stagingJobServiceRequest()
                .id(121L)
                .serviceRequestType(ServiceRequestType.VALIDATION)
                .requestMessage("my message")
                .build();

        when(serviceRequestRepository.findAll(Mockito.<Pageable>any()))
                .thenReturn(new PageImpl<>(singletonList(request)));

        Page<JobExecutionView> jobs = jobService.getAllJobs(PageRequest.of(0, 1));

        assertThat(jobs.getContent().get(0).getRequestMessage())
                .isEqualTo("my message");
    }

    @Test
    public void getAllJobs_RequestTypeNotValidation_ReturnsJobExecutionWithNoRequestMessage() {
        ServiceRequest request = JobPopulated.stagingJobServiceRequest()
                .id(121L)
                .serviceRequestType(ServiceRequestType.STAGING)
                .requestMessage("my message")
                .build();

        when(serviceRequestRepository.findAll(Mockito.<Pageable>any()))
                .thenReturn(new PageImpl<>(singletonList(request)));

        Page<JobExecutionView> jobs = jobService.getAllJobs(PageRequest.of(0, 1));

        assertThat(jobs.getContent().get(0).getRequestMessage())
                .isNull();
    }

    @Test
    public void searchJobs_InvalidFilterSpecification_ReturnsEmptyPage() throws JsonProcessingException {
        FilterExpression filter = Filter.builder()
                .columnName("invalidProperty")
                .type(FilterOption.EQUALS)
                .filter("job1")
                .build();

        Page<JobExecutionView> page = jobService.searchJobs(
                Pageable.unpaged(), objectMapper.writeValueAsString(filter));

        assertThat(page)
                .isEmpty();

        assertThat(page.hasContent())
                .isFalse();

        verifyZeroInteractions(serviceRequestRepository);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void searchJobs_ValidFilter_ReturnsJobs() throws JsonProcessingException {
        FilterExpression filter = Filter.builder()
                .columnName("name")
                .type(FilterOption.EQUALS)
                .filter("job1")
                .build();

        ServiceRequest serviceRequest = JobPopulated.stagingJobServiceRequest().id(12345L).name("job1").build();

        Specification<ServiceRequest> mockSpecification = mock(Specification.class);

        when(specificationBuilder.build(any()))
                .thenReturn(Validation.valid(mockSpecification));

        when(serviceRequestRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(singletonList(serviceRequest)));

        Page<JobExecutionView> page = jobService.searchJobs(
                Pageable.unpaged(), objectMapper.writeValueAsString(filter));

        assertThat(page)
                .extracting(JobExecutionView::getId, JobExecutionView::getName)
                .containsExactly(tuple(12345L, "job1"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void searchJobs_ValidFilter_QueriesRepository() throws JsonProcessingException {
        Specification<ServiceRequest> mockSpecification = mock(Specification.class);

        when(specificationBuilder.build(any()))
                .thenReturn(Validation.valid(mockSpecification));

        when(serviceRequestRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(singletonList(JobPopulated.stagingJobServiceRequest().id(11L).build())));

        PageRequest pageRequest = PageRequest.of(19, 32424);

        FilterExpression filter = Filter.builder()
                .columnName("name")
                .type(FilterOption.EQUALS)
                .filter("job1")
                .build();

        jobService.searchJobs(pageRequest, objectMapper.writeValueAsString(filter));

        verify(specificationBuilder).build(filter);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        verify(serviceRequestRepository).findAll(same(mockSpecification), pageableCaptor.capture());
        verifyNoMoreInteractions(serviceRequestRepository);

        assertThat(pageableCaptor.getValue())
                .isSameAs(pageRequest);
    }
}
