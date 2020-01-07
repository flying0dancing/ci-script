package com.lombardrisk.ignis.server.batch.staging.file;

import com.lombardrisk.ignis.server.batch.staging.HdfsDatasetConf;
import com.lombardrisk.ignis.server.job.staging.file.DataSourceService;
import com.lombardrisk.ignis.server.job.staging.model.StagingDataset;
import com.lombardrisk.ignis.spark.api.staging.datasource.DataSource;
import com.lombardrisk.ignis.spark.api.staging.datasource.HdfsCsvDataSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class LocalFileService implements DataSourceService {

    private final HdfsDatasetConf datasetConf;

    public LocalFileService(final HdfsDatasetConf datasetConf) {
        this.datasetConf = datasetConf;
    }

    @Override
    public Stream<String> findFileNames() throws IOException {
        return Files.list(Paths.get(datasetConf.getLocalPath()))
                .filter(Files::isRegularFile)
                .map(Path::getFileName)
                .map(Path::toString)
                .sorted();
    }

    @Override
    public DataSource createSparkDataSource(final StagingDataset stagingDataset, final boolean header) {
        return HdfsCsvDataSource.builder()
                .header(header)
                .localPath(stagingDataset.getStagingFile())
                .hdfsPath(stagingDataset.getStagingFileCopy())
                .build();
    }

    @Override
    public String stagingFileCopyLocation(final long serviceRequestId, final String physicalTableName) {
        return datasetConf.getRemotePath() + "/" + serviceRequestId + "/" + physicalTableName + ".csv";
    }
}
