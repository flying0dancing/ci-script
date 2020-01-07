package com.lombardrisk.ignis.server.jpa;

import com.lombardrisk.ignis.api.dataset.DatasetState;
import com.lombardrisk.ignis.common.fixtures.PopulatedDates;
import com.lombardrisk.ignis.server.config.IntegrationTestConfig;
import com.lombardrisk.ignis.server.job.fixture.JobPopulated;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequest;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequestRepository;
import com.lombardrisk.ignis.server.job.staging.StagingDatasetRepository;
import com.lombardrisk.ignis.server.job.staging.model.StagingDataset;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;

@RunWith(SpringRunner.class)
@IntegrationTestConfig
public class StagingDatasetRepositoryIT {

    @Autowired
    private StagingDatasetRepository stagingDatasetRepository;
    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Test
    public void createRetrieve() {

        ServiceRequest serviceRequest = serviceRequestRepository.save(
                JobPopulated.stagingJobServiceRequest().build());

        StagingDataset created = stagingDatasetRepository.save(JobPopulated.stagingDataset()
                .serviceRequestId(serviceRequest.getId())
                .status(DatasetState.QUEUED)
                .stagingFile("test.csv")
                .stagingFileCopy("/user/ubuntu/datasets/100/test.csv")
                .validationErrorFile("/user/ubuntu/datasets/100/E_TABLE.csv")
                .message("IllegalStateException: Could not upload")
                .datasetName("TABLE")
                .table("TABLE")
                .entityCode("FCR")
                .referenceDate(LocalDate.of(2001, 1, 1))
                .startTime(PopulatedDates.toDate("2000-12-1"))
                .endTime(PopulatedDates.toDate("2000-12-2"))
                .build());

        soft.assertThat(created.getServiceRequestId())
                .isEqualTo(serviceRequest.getId());
        soft.assertThat(created.getStatus())
                .isEqualTo(DatasetState.QUEUED);
        soft.assertThat(created.getStagingFile())
                .isEqualTo("test.csv");
        soft.assertThat(created.getStagingFileCopy())
                .isEqualTo("/user/ubuntu/datasets/100/test.csv");
        soft.assertThat(created.getValidationErrorFile())
                .isEqualTo("/user/ubuntu/datasets/100/E_TABLE.csv");
        soft.assertThat(created.getMessage())
                .isEqualTo("IllegalStateException: Could not upload");
        soft.assertThat(created.getDatasetName())
                .isEqualTo("TABLE");
        soft.assertThat(created.getTable())
                .isEqualTo("TABLE");
        soft.assertThat(created.getEntityCode())
                .isEqualTo("FCR");
        soft.assertThat(created.getReferenceDate())
                .isEqualTo(LocalDate.of(2001, 1, 1));
        soft.assertThat(created.getStartTime())
                .isEqualTo(PopulatedDates.toDate("2000-12-1"));
        soft.assertThat(created.getEndTime())
                .isEqualTo(PopulatedDates.toDate("2000-12-2"));
    }
}
