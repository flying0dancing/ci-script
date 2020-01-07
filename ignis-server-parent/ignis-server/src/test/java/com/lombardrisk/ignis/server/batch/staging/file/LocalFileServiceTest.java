package com.lombardrisk.ignis.server.batch.staging.file;

import com.lombardrisk.ignis.server.batch.staging.HdfsDatasetConf;
import com.lombardrisk.ignis.server.job.fixture.JobPopulated;
import com.lombardrisk.ignis.server.job.staging.model.StagingDataset;
import com.lombardrisk.ignis.spark.api.staging.datasource.DataSource;
import com.lombardrisk.ignis.spark.api.staging.datasource.HdfsCsvDataSource;
import org.assertj.core.util.Files;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

public class LocalFileServiceTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private File rootFolder;
    private LocalFileService localFileService;

    @Before
    public void setUp() throws Exception {
        rootFolder = folder.newFolder("root");
        HdfsDatasetConf datasetConf = new HdfsDatasetConf();
        datasetConf.setLocalPath(rootFolder.getPath());
        localFileService = new LocalFileService(datasetConf);
    }

    @Test
    public void findFilenames_filtersOutFolders() throws Exception {
        Files.newFile(rootFolder.getPath() + "/file_1.csv");
        Files.newFolder(rootFolder.getPath() + "/subFolder");
        Files.newFolder(rootFolder.getPath() + "/subFolder/file_2.csv");

        List<String> retrievedFilenames = localFileService.findFileNames().collect(toList());

        assertThat(retrievedFilenames).containsExactly("file_1.csv");
    }

    @Test
    public void findFilenames_sortsByFileName() throws Exception {
        Files.newFile(rootFolder.getPath() + "/amex.csv");
        Files.newFile(rootFolder.getPath() + "/bank_of_america.csv");
        Files.newFile(rootFolder.getPath() + "/zanzibar_bank.csv");

        List<String> retrievedFilenames = localFileService.findFileNames()
                .collect(toList());

        assertThat(retrievedFilenames)
                .containsExactly("amex.csv", "bank_of_america.csv", "zanzibar_bank.csv");
    }

    @Test
    public void createSparkDataSource_ReturnsHdfsDataSource() {
        HdfsDatasetConf datasetConf = new HdfsDatasetConf();
        datasetConf.setRemotePath("/the/remote/path");
        LocalFileService localFileService = new LocalFileService(datasetConf);

        StagingDataset stagingDataset = JobPopulated.stagingDataset()
                .stagingFile("my/local/path")
                .stagingFileCopy("/the/remote/path")
                .build();

        DataSource dataSource = localFileService.createSparkDataSource(stagingDataset, false);

        assertThat(dataSource).isEqualTo(HdfsCsvDataSource.builder()
                .header(false)
                .localPath("my/local/path")
                .hdfsPath("/the/remote/path")
                .build());
    }
}
