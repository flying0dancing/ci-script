package com.lombardrisk.ignis.spark.staging;

import com.lombardrisk.ignis.spark.api.staging.DatasetProperties;
import com.lombardrisk.ignis.spark.api.staging.StagingAppConfig;
import com.lombardrisk.ignis.spark.api.staging.StagingDatasetConfig;
import com.lombardrisk.ignis.spark.api.staging.StagingErrorOutput;
import com.lombardrisk.ignis.spark.api.staging.StagingSchemaValidation;
import com.lombardrisk.ignis.client.external.job.StagingClient;
import com.lombardrisk.ignis.spark.TestStagingApplication;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;
import retrofit2.Call;

import java.io.File;
import java.io.IOException;

import static com.google.common.collect.Sets.newHashSet;
import static com.lombardrisk.ignis.api.dataset.DatasetState.VALIDATION_FAILED;
import static org.apache.commons.io.FileUtils.copyDirectory;
import static org.apache.commons.io.FileUtils.deleteQuietly;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { TestStagingApplication.class, StagingJobOperatorIT.TestConfig.class })
public class StagingJobOperatorIT {

    @MockBean
    private StagingClient stagingClient;
    @Mock
    private Call<Void> voidCall;

    @Autowired
    private StagingJobOperator jobOperator;

    @Configuration
    public static class TestConfig {

        @Bean
        public StagingAppConfig stagingJobRequest() {
            return StagingAppConfig.builder()
                    .name("test staging")
                    .serviceRequestId(1)
                    .items(newHashSet(
                            StagingDatasetConfig.builder()
                                    .id(1912)
                                    .source(new TestDataSource(
                                            "target/datasets/employee/staging/1/INVALID_EMPLOYEE",
                                            false))
                                    .datasetProperties(DatasetProperties.builder()
                                            .build())
                                    .stagingErrorOutput(StagingErrorOutput.builder()
                                            .errorFileSystemUri("file:///")
                                            .temporaryFilePath(
                                                    "target/datasets/employee/staging/1/E_INVALID_EMPLOYEE_tmp")
                                            .errorFilePath("target/datasets/employee/staging/1/E_INVALID_EMPLOYEE.csv")
                                            .build())
                                    .stagingSchemaValidation(StagingSchemaValidation.builder()
                                            .physicalTableName("INVALID_EMPLOYEE")
                                            .fields(newHashSet())
                                            .build())
                                    .build()))
                    .build();
        }
    }

    @BeforeClass
    public static void setupDatasets() throws IOException {
        File datasetsDir = new File("target/datasets").getAbsoluteFile();

        deleteQuietly(datasetsDir);
        copyDirectory(new File("src/test/resources/datasets"), datasetsDir);
    }

    @Test
    public void runJob_RunsJob_AndFailsValidation() {
        when(stagingClient.updateDataSetState(anyLong(), anyString()))
                .thenReturn(voidCall);

        jobOperator.runJob();

        verify(stagingClient)
                .updateDataSetState(eq(1912L), eq(VALIDATION_FAILED.name()));
    }
}
