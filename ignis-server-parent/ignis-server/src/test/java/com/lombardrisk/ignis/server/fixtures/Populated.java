package com.lombardrisk.ignis.server.fixtures;

import com.lombardrisk.ignis.client.external.fixture.ExternalClient;
import com.lombardrisk.ignis.client.external.job.staging.request.DatasetMetadata;
import com.lombardrisk.ignis.client.external.job.staging.request.v1.StagingItemRequest;
import com.lombardrisk.ignis.client.external.job.validation.ValidationJobRequest;
import com.lombardrisk.ignis.server.user.model.SecUser;
import com.lombardrisk.ignis.server.util.spark.SparkSubmitOption;
import com.lombardrisk.ignis.server.util.spark.YarnAppSession;
import com.lombardrisk.ignis.spark.api.JobType;
import com.lombardrisk.ignis.spark.api.staging.StagingDatasetConfig;
import com.lombardrisk.ignis.spark.api.staging.StagingSchemaValidation;
import com.lombardrisk.ignis.spark.api.staging.datasource.HdfsCsvDataSource;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.spark.SparkConf;
import org.apache.spark.deploy.yarn.Client;
import org.apache.spark.deploy.yarn.ClientArguments;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;

import static com.lombardrisk.ignis.common.fixtures.PopulatedDates.toDate;
import static org.apache.commons.lang3.ArrayUtils.EMPTY_STRING_ARRAY;

@SuppressWarnings("WeakerAccess")
@Slf4j
@UtilityClass
public class Populated {

    public static org.springframework.batch.core.JobExecution jobExecution() {
        JobExecution jobExecution = new JobExecution(544L);
        jobExecution.setExitStatus(ExitStatus.COMPLETED);
        jobExecution.setStatus(BatchStatus.COMPLETED);
        jobExecution.setStartTime(toDate("1997-03-04"));
        jobExecution.setEndTime(toDate("1997-03-05"));
        return jobExecution;
    }

    public static ValidationJobRequest.ValidationJobRequestBuilder validationJobRequest() {
        return ExternalClient.Populated.validationJobRequest();
    }

    public static HdfsCsvDataSource.HdfsCsvDataSourceBuilder hdfsCsvDataSource() {
        return com.lombardrisk.ignis.spark.api.fixture.Populated.csvDataSource();
    }

    public static StagingItemRequest.StagingItemRequestBuilder stagingItemRequest() {
        return ExternalClient.Populated.stagingItemRequest();
    }

    public static StagingDatasetConfig.StagingDatasetConfigBuilder stagingDatasetConfigRequest() {
        return com.lombardrisk.ignis.spark.api.fixture.Populated.stagingDatasetConfig();
    }

    public static StagingSchemaValidation.StagingSchemaValidationBuilder stagingSchemaValidation() {
        return com.lombardrisk.ignis.spark.api.fixture.Populated.stagingSchemaValidation();
    }

    public static DatasetMetadata.DatasetMetadataBuilder datasetMetadata() {
        return DatasetMetadata.builder()
                .entityCode("Entity")
                .referenceDate("01/01/2000");
    }

    public static YarnAppSession.YarnApplicationSessionBuilder appSession() {
        return YarnAppSession.builder()
                .appId(0L, 0)
                .client(new Client(new ClientArguments(EMPTY_STRING_ARRAY), new SparkConf(true)));
    }

    public static SparkSubmitOption.SparkSubmitOptionBuilder sparkSubmitOption() {
        return SparkSubmitOption.builder()
                .jobExecutionId(0L)
                .jobName("spark")
                .job(() -> 0L)
                .jobType(JobType.STAGING_DATASET)
                .clazz("App.class")
                .driverJar("spark.jar")
                .sparkConf(new SparkConf())
                .hdfsUser("user");
    }

    public static SecUser.SecUserBuilder user() {
        return SecUser.builder()
                .username("username")
                .password("userPassword");
    }
}
