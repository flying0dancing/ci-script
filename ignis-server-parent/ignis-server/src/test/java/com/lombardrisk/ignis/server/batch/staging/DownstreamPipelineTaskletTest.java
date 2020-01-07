package com.lombardrisk.ignis.server.batch.staging;

import com.lombardrisk.ignis.api.calendar.HolidayCalendarModule;
import com.lombardrisk.ignis.client.external.job.pipeline.PipelineRequest;
import com.lombardrisk.ignis.server.batch.SparkJobExecutor;
import com.lombardrisk.ignis.server.job.fixture.JobPopulated;
import com.lombardrisk.ignis.server.job.pipeline.PipelineJobService;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequest;
import com.lombardrisk.ignis.spark.api.staging.DownstreamPipeline;
import com.lombardrisk.ignis.spark.api.staging.StagingAppConfig;
import io.vavr.control.Validation;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;

import java.time.LocalDate;

import static com.google.common.collect.Sets.newHashSet;
import static com.lombardrisk.ignis.common.json.MapperWrapper.MAPPER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DownstreamPipelineTaskletTest {

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Mock
    private SparkJobExecutor sparkJobExecutor;
    @Mock
    private PipelineJobService pipelineJobService;
    @Mock
    private ChunkContext chunkContext;
    @Mock
    private StepContribution stepContribution;

    @InjectMocks
    private DownstreamPipelineTasklet tasklet;

    @Captor
    private ArgumentCaptor<PipelineRequest> requestCaptor;
    @Captor
    private ArgumentCaptor<String> stringCaptor;

    @Before
    public void setUp() {
        when(pipelineJobService.startJob(any(), any()))
                .thenReturn(Validation.valid(() -> 12345L));
        MAPPER.registerModule(new HolidayCalendarModule());
        tasklet = new DownstreamPipelineTasklet(sparkJobExecutor, pipelineJobService, MAPPER);
    }

    @Test
    public void execute_DownstreamPipelinesNotSet_DoesNotStartPipelineJob() throws Exception {
        StagingAppConfig appConfig = JobPopulated.stagingAppConfig().downstreamPipelines(null).build();

        ServiceRequest serviceRequest = JobPopulated.stagingJobServiceRequest()
                .id(1L)
                .requestMessage(MAPPER.writeValueAsString(appConfig))
                .build();

        when(sparkJobExecutor.setupRequest(any()))
                .thenReturn(serviceRequest);

        RepeatStatus status = tasklet.execute(stepContribution, chunkContext);

        verifyZeroInteractions(pipelineJobService);

        assertThat(status).isEqualTo(RepeatStatus.FINISHED);
    }

    @Test
    public void execute_DownstreamPipelinesSet_StartsDownstreamPipelineJob() throws Exception {
        DownstreamPipeline downstreamPipeline1 = DownstreamPipeline.builder()
                .pipelineId(1L)
                .pipelineName("p1")
                .entityCode("entity 1")
                .referenceDate(LocalDate.now())
                .build();

        DownstreamPipeline downstreamPipeline2 = DownstreamPipeline.builder()
                .pipelineId(2L)
                .pipelineName("p2")
                .entityCode("entity 2")
                .referenceDate(LocalDate.now().plusDays(1))
                .build();

        StagingAppConfig appConfig = JobPopulated.stagingAppConfig()
                .downstreamPipelines(newHashSet(downstreamPipeline1, downstreamPipeline2))
                .build();

        ServiceRequest serviceRequest = JobPopulated.stagingJobServiceRequest()
                .id(1L)
                .createdBy("system admin")
                .requestMessage(MAPPER.writeValueAsString(appConfig))
                .build();

        when(sparkJobExecutor.setupRequest(any()))
                .thenReturn(serviceRequest);

        RepeatStatus status = tasklet.execute(stepContribution, chunkContext);

        verify(pipelineJobService, times(2)).startJob(requestCaptor.capture(), stringCaptor.capture());

        soft.assertThat(requestCaptor.getAllValues())
                .containsExactlyInAnyOrder(
                        PipelineRequest.builder()
                                .pipelineId(1L)
                                .name("p1")
                                .entityCode("entity 1")
                                .referenceDate(LocalDate.now())
                                .build(),
                        PipelineRequest.builder()
                                .pipelineId(2L)
                                .name("p2")
                                .entityCode("entity 2")
                                .referenceDate(LocalDate.now().plusDays(1))
                                .build());

        soft.assertThat(stringCaptor.getAllValues())
                .containsOnly("system admin");

        soft.assertThat(status).isEqualTo(RepeatStatus.FINISHED);
    }
}