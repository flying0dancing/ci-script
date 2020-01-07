package com.lombardrisk.ignis.server.batch.staging.file.error;

import com.lombardrisk.ignis.fs.FileSystemTemplate;
import com.lombardrisk.ignis.server.batch.staging.HdfsDatasetConf;
import com.lombardrisk.ignis.server.job.staging.file.ErrorFileLinkOrStream;
import com.lombardrisk.ignis.server.job.staging.file.ErrorFileService;
import com.lombardrisk.ignis.server.job.staging.model.StagingDataset;
import com.lombardrisk.ignis.spark.api.staging.StagingErrorOutput;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class HDFSErrorFileService implements ErrorFileService {

    private final HdfsDatasetConf hdfsDatasetConf;
    private final FileSystemTemplate hdfsFileSystem;

    public HDFSErrorFileService(final HdfsDatasetConf hdfsDatasetConf, final FileSystemTemplate hdfsFileSystem) {
        this.hdfsDatasetConf = hdfsDatasetConf;
        this.hdfsFileSystem = hdfsFileSystem;
    }

    @Override
    public String errorFileRelativePath(final long serviceRequestId, final String physicalTableName) {
        return hdfsDatasetConf.getRemotePath() + "/" + serviceRequestId + "/" + "E_" + physicalTableName + ".csv";
    }

    @Override
    public StagingErrorOutput errorOutput(final StagingDataset stagingDataset) {
        String fileSystemURI = hdfsDatasetConf.getRemotePath();

        return StagingErrorOutput.builder()
                .errorFilePath(stagingDataset.getValidationErrorFile())
                .errorFileSystemUri(fileSystemURI)
                .temporaryFilePath(fileSystemURI + "/tmp-error-dataset/" + stagingDataset.getId())
                .build();
    }

    @Override
    public ErrorFileLinkOrStream downloadValidationErrorFile(final StagingDataset stagingDataset) throws IOException {
        @SuppressWarnings("squid:S2095")
        InputStream inputStream = hdfsFileSystem.open(stagingDataset.getValidationErrorFile());

        return ErrorFileLinkOrStream.fileStream(
                new File(stagingDataset.getValidationErrorFile()).getName(),
                inputStream);
    }
}
