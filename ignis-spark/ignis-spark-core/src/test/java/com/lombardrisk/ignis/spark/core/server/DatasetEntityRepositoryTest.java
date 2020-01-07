package com.lombardrisk.ignis.spark.core.server;

import com.lombardrisk.ignis.client.core.view.IdView;
import com.lombardrisk.ignis.client.internal.CreateDatasetCall;
import com.lombardrisk.ignis.client.internal.InternalDatasetClient;
import com.lombardrisk.ignis.client.internal.UpdateDatasetRunCall;
import com.lombardrisk.ignis.spark.api.JobRequest;
import com.lombardrisk.ignis.spark.api.fixture.Populated;
import com.lombardrisk.ignis.spark.api.staging.DatasetProperties;
import com.lombardrisk.ignis.spark.api.staging.StagingSchemaValidation;
import com.lombardrisk.ignis.spark.core.JobOperatorException;
import com.lombardrisk.ignis.spark.core.mock.RetrofitCall;
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
import retrofit2.Call;

import java.io.IOException;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DatasetEntityRepositoryTest {

    @Mock
    private JobRequest jobRequest;
    @Mock
    private InternalDatasetClient datasetClient;

    @InjectMocks
    private DatasetEntityRepository datasetEntityRepository;

    @Mock
    private Call<IdView> createDatasetCall;

    @Mock
    private Call<IdView> updateDatasetRunCall;

    @Captor
    private ArgumentCaptor<CreateDatasetCall> datasetCallCaptor;

    @Captor
    private ArgumentCaptor<UpdateDatasetRunCall> updateDatasetCallCaptor;

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Before
    public void setUp() {
        when(datasetClient.createDataset(any(CreateDatasetCall.class)))
                .thenReturn(new RetrofitCall<>(new IdView(123456L)));

        when(datasetClient.updateDatasetRun(anyLong(), any()))
                .thenReturn(new RetrofitCall<>(new IdView(123456L)));
    }

    @Test
    public void createStagingDataset_CreatesDataset() {
        when(jobRequest.getServiceRequestId())
                .thenReturn(10L);

        datasetEntityRepository.createStagingDataset(
                21,
                1L,
                "ROW_KEY >= 42949672960 and ROW_KEY <= 47244640255",
                Populated.stagingDatasetConfig()
                        .datasetProperties(DatasetProperties.builder()
                                .entityCode("EE")
                                .referenceDate(LocalDate.of(1999, 9, 9))
                                .build())
                        .stagingSchemaValidation(StagingSchemaValidation.builder()
                                .schemaId(123L)
                                .build())
                        .autoValidate(true)
                        .build());

        verify(datasetClient).createDataset(datasetCallCaptor.capture());

        CreateDatasetCall datasetCall = datasetCallCaptor.getValue();

        soft.assertThat(datasetCall.getStagingJobId())
                .isEqualTo(10L);
        soft.assertThat(datasetCall.getRecordsCount())
                .isEqualTo(21);

        soft.assertThat(datasetCall.getEntityCode())
                .isEqualTo("EE");
        soft.assertThat(datasetCall.getReferenceDate())
                .isEqualTo(LocalDate.of(1999, 9, 9));
        soft.assertThat(datasetCall.getPredicate())
                .isEqualTo("ROW_KEY >= 42949672960 and ROW_KEY <= 47244640255");

        soft.assertThat(datasetCall.getSchemaId())
                .isEqualTo(123L);
        soft.assertThat(datasetCall.isAutoValidate())
                .isTrue();
    }

    @Test
    public void createStagingDataset_ThrowsException() throws Exception {
        IOException exception = new IOException("server went away");

        when(datasetClient.createDataset(any(CreateDatasetCall.class)))
                .thenReturn(createDatasetCall);

        when(createDatasetCall.execute())
                .thenThrow(exception);

        assertThatThrownBy(() -> datasetEntityRepository.createStagingDataset(
                0, 1L, "ROW_KEY >= 0 AND ROW_KEY <= 1", Populated.stagingDatasetConfig().build()
        ))
                .isInstanceOf(JobOperatorException.class)
                .hasMessageStartingWith("Request failed for job")
                .hasCause(exception);
    }

    @Test
    public void updateStagingDataset_UpdatesDataset() {
        when(jobRequest.getServiceRequestId())
                .thenReturn(234234L);

        datasetEntityRepository.updateStagingDataset(12345L, 3474324L, 100L);

        verify(datasetClient).updateDatasetRun(eq(12345L), updateDatasetCallCaptor.capture());

        soft.assertThat(updateDatasetCallCaptor.getValue().getStagingDatasetId())
                .isEqualTo(3474324L);

        soft.assertThat(updateDatasetCallCaptor.getValue().getStagingJobId())
                .isEqualTo(234234L);

        soft.assertThat(updateDatasetCallCaptor.getValue().getRecordsCount())
                .isEqualTo(100L);
    }

    @Test
    public void updateStagingDataset_ThrowsException() throws IOException {
        when(jobRequest.getServiceRequestId())
                .thenReturn(234234L);

        IOException ioException = new IOException("uh oh!");

        when(updateDatasetRunCall.execute())
                .thenThrow(ioException);

        when(datasetClient.updateDatasetRun(anyLong(), any()))
                .thenReturn(updateDatasetRunCall);

        assertThatThrownBy(() -> datasetEntityRepository.updateStagingDataset(12345L, 32432432L, 100L))
                .isInstanceOf(JobOperatorException.class)
                .hasCause(ioException);
    }
}