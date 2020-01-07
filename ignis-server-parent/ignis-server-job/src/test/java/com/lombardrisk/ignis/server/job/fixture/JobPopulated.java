package com.lombardrisk.ignis.server.job.fixture;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.lombardrisk.ignis.api.dataset.DatasetState;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequest;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequestType;
import com.lombardrisk.ignis.server.job.staging.model.StagingDataset;
import com.lombardrisk.ignis.server.job.staging.model.StagingDatasetInstruction;
import com.lombardrisk.ignis.server.job.staging.model.StagingInstructions;
import com.lombardrisk.ignis.server.product.fixture.ProductPopulated;
import com.lombardrisk.ignis.spark.api.staging.StagingAppConfig;
import com.lombardrisk.ignis.spark.api.staging.StagingDatasetConfig;
import com.machinezoo.noexception.Exceptions;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.test.util.JsonPathExpectationsHelper;

import java.time.LocalDate;

import static com.google.common.collect.Sets.newHashSet;
import static com.lombardrisk.ignis.common.fixtures.PopulatedDates.toDate;
import static com.lombardrisk.ignis.common.json.MapperWrapper.MAPPER;
import static com.lombardrisk.ignis.server.job.servicerequest.ServiceRequestType.IMPORT_PRODUCT;
import static com.lombardrisk.ignis.server.job.servicerequest.ServiceRequestType.STAGING;
import static org.junit.Assert.fail;

@Slf4j
@UtilityClass
public class JobPopulated {

    public static StagingAppConfig.StagingAppConfigBuilder stagingAppConfig() {
        return com.lombardrisk.ignis.spark.api.fixture.Populated.stagingAppConfig();
    }

    public static ServiceRequest.ServiceRequestBuilder stagingJobServiceRequest() {
        return ServiceRequest.builder()
                .name("validation job")
                .createdBy("matt")
                .jobExecutionId(100L)
                .serviceRequestType(STAGING)
                .requestMessage(Exceptions.sneak()
                        .supplier(() -> MAPPER.writeValueAsString(stagingAppConfig().build()))
                        .get());
    }

    public static ServiceRequest.ServiceRequestBuilder validationJobServiceRequest() {
        return stagingJobServiceRequest()
                .serviceRequestType(ServiceRequestType.VALIDATION)
                .requestMessage("1");
    }

    public static ServiceRequest.ServiceRequestBuilder importProductJobServiceRequest() {
        String importProductContextMessage = "{}";
        try {
            importProductContextMessage = MAPPER.writeValueAsString(ProductPopulated.productImportContext().build());
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
        return ServiceRequest.builder()
                .name("import product job")
                .createdBy("matt")
                .jobExecutionId(100L)
                .serviceRequestType(IMPORT_PRODUCT)
                .requestMessage(importProductContextMessage);
    }

    public static ServiceRequest.ServiceRequestBuilder pipelineJobServiceRequest() {
        String requestMessage = "{}";
        try {
            requestMessage = MAPPER.writeValueAsString(
                    com.lombardrisk.ignis.spark.api.fixture.Populated.pipelineAppConfig().build());
        } catch (JsonProcessingException e) {
            fail("Failed to parse pipeline app config as JSON " + e.getMessage());
        }

        return ServiceRequest.builder()
                .name("pipeline job")
                .createdBy("admin")
                .jobExecutionId(100L)
                .serviceRequestType(ServiceRequestType.PIPELINE)
                .requestMessage(requestMessage);
    }

    public static StagingDataset.StagingDatasetBuilder stagingDataset() {
        return StagingDataset.builder()
                .serviceRequestId(0L)
                .table("table v.1")
                .datasetName("a_table")
                .entityCode("Entity")
                .referenceDate(LocalDate.of(2000, 1, 1))
                .startTime(toDate("2020-01-01"))
                .lastUpdateTime(toDate("2020-01-01"))
                .endTime(toDate("2020-01-01"))
                .status(DatasetState.VALIDATION_FAILED)
                .validationErrorFile("errorfile.csv")
                .message("msg");
    }

    public static StagingInstructions.StagingInstructionsBuilder stagingInstructions() {
        return StagingInstructions.builder()
                .jobName("job1")
                .stagingDatasetInstructions(newHashSet(stagingDatasetInstruction().build()));
    }

    public static StagingDatasetInstruction.StagingDatasetInstructionBuilder stagingDatasetInstruction() {
        return StagingDatasetInstruction.builder()
                .schema(ProductPopulated.table().build())
                .entityCode("entityCode")
                .referenceDate(LocalDate.of(1991, 1, 1))
                .autoValidate(true)
                .filePath("path/to/file")
                .header(true);
    }

    public static StagingDatasetConfig.StagingDatasetConfigBuilder stagingDatasetConfig() {
        return com.lombardrisk.ignis.spark.api.fixture.Populated.stagingDatasetConfig();
    }

    public static JsonPathExpectationsHelper usingJsonPath(final String expression) {
        return new JsonPathExpectationsHelper(expression);
    }
}
