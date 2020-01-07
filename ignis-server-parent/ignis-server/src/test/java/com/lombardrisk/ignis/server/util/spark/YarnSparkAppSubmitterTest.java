package com.lombardrisk.ignis.server.util.spark;

import com.lombardrisk.ignis.server.fixtures.Populated;
import com.lombardrisk.ignis.server.job.fixture.JobPopulated;
import com.lombardrisk.ignis.server.util.TempDirectory;
import com.lombardrisk.ignis.spark.api.JobRequest;
import com.lombardrisk.ignis.spark.api.staging.StagingAppConfig;
import com.lombardrisk.ignis.web.common.config.TogglzConfiguration;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.spark.deploy.yarn.Client;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;

import java.io.File;
import java.nio.file.Paths;

import static com.lombardrisk.ignis.common.json.MapperWrapper.MAPPER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class YarnSparkAppSubmitterTest {

    @Mock
    private Client client;
    @Mock
    private TogglzConfiguration togglzConfiguration;
    @Mock
    private Environment environment;
    @Mock
    private TempDirectory tempDirectory;

    private YarnSparkAppSubmitter appSubmitter;

    @Before
    public void setUp() {
        when(tempDirectory.resolvePath(any()))
                .thenReturn(Paths.get("target"));

        JobRequestFileWriter fileWriter = new JobRequestFileWriter(MAPPER, tempDirectory);
        appSubmitter = new YarnSparkAppSubmitter(client,
                MAPPER, fileWriter, togglzConfiguration, environment);
    }

    @Test
    public void submitApplication() throws Exception {
        when(client.submitApplication())
                .thenReturn(ApplicationId.newInstance(12L, 2));

        AppSession appSession = appSubmitter.submit(Populated.sparkSubmitOption()
                .job(JobPopulated.stagingAppConfig().serviceRequestId(1234).build())
                .build());

        assertThat(appSession.getAppId().getId())
                .isEqualTo(2);
        assertThat(appSession.getAppId().getClusterTimestamp())
                .isEqualTo(12L);
    }

    @Test
    public void submitApplication_WritesJobRequestToFile() throws Exception {
        when(client.submitApplication())
                .thenReturn(ApplicationId.newInstance(12L, 2));

        JobRequest jobConfig = JobPopulated.stagingAppConfig().serviceRequestId(1234).build();

        appSubmitter.submit(Populated.sparkSubmitOption()
                .job(jobConfig)
                .build());

        File jobFile = new File("target/job_1234.json");

        assertThat(jobFile)
                .exists();

        JobRequest savedJobConfig = MAPPER.readValue(jobFile, StagingAppConfig.class);

        assertThat(savedJobConfig)
                .isEqualTo(jobConfig);
    }
}
