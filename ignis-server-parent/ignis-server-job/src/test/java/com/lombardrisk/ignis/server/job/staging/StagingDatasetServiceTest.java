package com.lombardrisk.ignis.server.job.staging;

import com.google.common.collect.ImmutableList;
import com.lombardrisk.ignis.api.dataset.DatasetState;
import com.lombardrisk.ignis.common.assertions.VavrAssert;
import com.lombardrisk.ignis.common.time.TimeSource;
import com.lombardrisk.ignis.data.common.failure.CRUDFailure;
import com.lombardrisk.ignis.feature.IgnisFeature;
import com.lombardrisk.ignis.server.job.fixture.JobPopulated;
import com.lombardrisk.ignis.server.job.servicerequest.ServiceRequestRepository;
import com.lombardrisk.ignis.server.job.staging.file.DataSourceService;
import com.lombardrisk.ignis.server.job.staging.model.StagingDataset;
import com.lombardrisk.ignis.server.job.staging.validate.StagingRequestV1Validator;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.togglz.junit.TogglzRule;

import java.util.List;
import java.util.Optional;

import static com.lombardrisk.ignis.common.fixtures.PopulatedDates.toDate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StagingDatasetServiceTest {

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Rule
    public TogglzRule togglzRule = TogglzRule.allDisabled(IgnisFeature.class);

    @Mock
    private StagingRequestV1Validator stagingRequestV1Validator;
    @Mock
    private ServiceRequestRepository serviceRequestRepository;
    @Mock
    private StagingDatasetRepository stagingDatasetRepository;
    @Mock
    private TimeSource timeSource;
    @Mock
    private DataSourceService dataSourceService;
    @Mock
    private StagingSparkConfigService stagingSparkConfigService;

    @InjectMocks
    private StagingDatasetService stagingDatasetService;

    @Before
    public void before() {
        when(timeSource.nowAsDate())
                .thenReturn(toDate("2018-01-01"));

        when(stagingDatasetRepository.save(any()))
                .thenReturn(JobPopulated.stagingDataset().build());

        when(stagingDatasetRepository.save(any()))
                .thenReturn(JobPopulated.stagingDataset()
                        .id(1234L)
                        .build());
    }

    @Test
    public void findStagingDataset_CallsStagingDatasetRepository() {
        stagingDatasetService.findById(11L);

        verify(stagingDatasetRepository).findById(11L);
    }

    @Test
    public void findStagingDatasets_ByJobExecutionIdAndName_ReturnsStagingDatasets() {
        when(stagingDatasetRepository.findByServiceRequestIdAndDatasetName(1L, "employee"))
                .thenReturn(ImmutableList.of(new StagingDataset()));

        when(stagingDatasetRepository.findByServiceRequestId(1L))
                .thenReturn(ImmutableList.of(new StagingDataset(), new StagingDataset()));

        List<StagingDataset> datasetsByJobIdAndName = VavrAssert
                .assertValid(stagingDatasetService.findStagingDatasets(1L, null, "employee"))
                .getResult();

        List<StagingDataset> datasetsByNameJobIdOnly = VavrAssert
                .assertValid(stagingDatasetService.findStagingDatasets(1L, null, null))
                .getResult();

        assertThat(datasetsByJobIdAndName.size())
                .isEqualTo(1);

        assertThat(datasetsByNameJobIdOnly.size())
                .isEqualTo(2);
    }

    @Test
    public void findStagingDatasets_ByDatasetId_ReturnsStagingDatasets() {
        StagingDataset stagingDataset = JobPopulated.stagingDataset().datasetId(34124L).build();

        when(stagingDatasetRepository.findByDatasetId(34124L))
                .thenReturn(ImmutableList.of(stagingDataset));

        List<StagingDataset> stagingDatasets = VavrAssert
                .assertValid(stagingDatasetService.findStagingDatasets(null, 34124L, null))
                .getResult();

        assertThat(stagingDatasets)
                .extracting(StagingDataset::getDatasetId)
                .containsExactly(34124L);
    }

    @Test
    public void findStagingDatasets_JobExecutionIdAndDatasetIdNull_ReturnsError() {
        VavrAssert
                .assertFailed(stagingDatasetService.findStagingDatasets(null, null, null))
                .withFailure(CRUDFailure.invalidParameters()
                        .paramError("jobId", "not provided")
                        .paramError("datasetId", "not provided")
                        .asFailure());

        verifyZeroInteractions(stagingDatasetRepository);
    }

    @Test
    public void updateDatasetState() throws Exception {
        StagingDataset stagingDataset = JobPopulated.stagingDataset()
                .status(DatasetState.VALIDATING)
                .build();
        when(stagingDatasetRepository.findById(1L))
                .thenReturn(Optional.of(stagingDataset));
        when(stagingDatasetRepository.save(stagingDataset))
                .thenReturn(stagingDataset);

        stagingDatasetService.updateStagingDatasetState(1L, DatasetState.VALIDATED);

        verify(stagingDatasetRepository).findById(1L);
        verify(stagingDatasetRepository).save(stagingDataset);
    }
}
