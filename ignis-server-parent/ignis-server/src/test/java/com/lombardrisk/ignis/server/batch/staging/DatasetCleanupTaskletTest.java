package com.lombardrisk.ignis.server.batch.staging;

import com.lombardrisk.ignis.api.calendar.HolidayCalendarModule;
import com.lombardrisk.ignis.hadoop.HdfsTemplate;
import com.lombardrisk.ignis.server.batch.SparkJobExecutor;
import com.lombardrisk.ignis.server.fixtures.Populated;
import com.lombardrisk.ignis.server.job.fixture.JobPopulated;
import com.lombardrisk.ignis.spark.api.staging.StagingAppConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.google.common.collect.Sets.newHashSet;
import static com.lombardrisk.ignis.common.json.MapperWrapper.MAPPER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DatasetCleanupTaskletTest {

    @Mock
    private HdfsTemplate fileSystemTemplate;
    @Mock
    private SparkJobExecutor sparkJobExecutor;

    private DatasetCleanupTasklet tasklet;

    @Before
    public void setUp() throws Exception {
        when(fileSystemTemplate.exists(anyString()))
                .thenReturn(true);

        MAPPER.registerModule(new HolidayCalendarModule());
        tasklet = new DatasetCleanupTasklet(sparkJobExecutor, fileSystemTemplate, MAPPER);
    }

    @Test
    public void execute_ManyDatasetConfigs_DeletesStagingFile() throws Exception {
        StagingAppConfig stagingAppConfig = JobPopulated.stagingAppConfig()
                .items(newHashSet(
                        JobPopulated.stagingDatasetConfig()
                                .source(Populated.hdfsCsvDataSource()
                                        .hdfsPath("hdfs://root/12/TABLE")
                                        .build())
                                .build(),
                        JobPopulated.stagingDatasetConfig()
                                .source(Populated.hdfsCsvDataSource()
                                        .hdfsPath("hdfs://root/12/CHAIR")
                                        .build())
                                .build()))
                .build();

        when(sparkJobExecutor.setupRequest(any()))
                .thenReturn(JobPopulated.stagingJobServiceRequest()
                        .id(12L)
                        .requestMessage(MAPPER.writeValueAsString(stagingAppConfig))
                        .build());

        tasklet.execute(null, null);

        verify(fileSystemTemplate).delete("hdfs://root/12/TABLE");
        verify(fileSystemTemplate).delete("hdfs://root/12/CHAIR");
    }

    @Test
    public void execute_FileDoesNotExist_DoesNotAttemptDelete() throws Exception {
        StagingAppConfig stagingAppConfig = JobPopulated.stagingAppConfig()
                .items(newHashSet(JobPopulated.stagingDatasetConfig().build()))
                .build();

        when(sparkJobExecutor.setupRequest(any()))
                .thenReturn(JobPopulated.stagingJobServiceRequest()
                        .id(100L)
                        .requestMessage(MAPPER.writeValueAsString(stagingAppConfig))
                        .build());

        when(fileSystemTemplate.exists(anyString()))
                .thenReturn(false);

        tasklet.execute(null, null);

        verify(fileSystemTemplate, never()).delete(anyString());
    }
}
