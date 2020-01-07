package com.lombardrisk.ignis.server.batch.staging.file;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.lombardrisk.ignis.server.job.fixture.JobPopulated;
import com.lombardrisk.ignis.server.job.staging.model.StagingDataset;
import com.lombardrisk.ignis.spark.api.staging.datasource.DataSource;
import com.lombardrisk.ignis.spark.api.staging.datasource.S3CsvDataSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.lombardrisk.ignis.server.util.S3UtilTest.createSummary;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class S3FileServiceTest {

    @Mock
    private AmazonS3 amazonS3;

    @Mock
    private ListObjectsV2Result result;

    @InjectMocks
    private S3FileService s3FileService;

    @Before
    public void setUp() {
        when(amazonS3.listObjectsV2(any(ListObjectsV2Request.class)))
                .thenReturn(result);

        when(result.isTruncated())
                .thenReturn(false);

        when(result.getObjectSummaries())
                .thenReturn(Arrays.asList(
                        createSummary("1"),
                        createSummary("2")));
    }

    @Test
    public void findFilenames_returnsKeysAndFiltersOutFolders() {
        when(result.getObjectSummaries())
                .thenReturn(Arrays.asList(
                        createSummary("toplevel.txt"),
                        createSummary("folder/"),
                        createSummary("folder/file.txt"),
                        createSummary("folder/subfolder/"),
                        createSummary("folder/subfolder/file2.txt")));

        List<String> keys = s3FileService.findFileNames()
                .collect(Collectors.toList());

        assertThat(keys)
                .containsExactly("toplevel.txt", "folder/file.txt", "folder/subfolder/file2.txt");
    }

    @Test
    public void createSparkDataSource_ReturnsS3DataSource() {
        S3FileService s3FileService = new S3FileService(null, "s3a", "le_bucket", null);

        StagingDataset stagingDataset = JobPopulated.stagingDataset().stagingFile("folder/key").build();

        DataSource dataSource = s3FileService.createSparkDataSource(stagingDataset, false);

        assertThat(dataSource).isEqualTo(S3CsvDataSource.builder()
                .header(false)
                .s3Protocol("s3a")
                .s3Key("folder/key")
                .s3Bucket("le_bucket")
                .build());
    }
}
