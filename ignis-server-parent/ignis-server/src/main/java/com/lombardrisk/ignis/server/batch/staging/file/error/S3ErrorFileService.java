package com.lombardrisk.ignis.server.batch.staging.file.error;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.lombardrisk.ignis.common.time.TimeSource;
import com.lombardrisk.ignis.server.job.staging.file.ErrorFileLinkOrStream;
import com.lombardrisk.ignis.server.job.staging.file.ErrorFileService;
import com.lombardrisk.ignis.server.job.staging.model.StagingDataset;
import com.lombardrisk.ignis.spark.api.staging.StagingErrorOutput;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.net.URL;
import java.util.Date;

@AllArgsConstructor
public class S3ErrorFileService implements ErrorFileService {

    private static final int DOWNLOAD_LINK_EXPIRY_SECONDS = 1_000;

    private final AmazonS3 s3Client;
    private final String s3Protocol;
    private final String errorFileS3Bucket;
    private final String errorFileS3Prefix;
    private final TimeSource timeSource;

    @Override
    public String errorFileRelativePath(final long serviceRequestId, final String physicalTableName) {
        return prefix() + "stagingJob/" + serviceRequestId + "/" + "E_" + physicalTableName + ".csv";
    }

    @Override
    public StagingErrorOutput errorOutput(final StagingDataset stagingDataset) {
        String fileSystemUri = errorFileSystemURI();
        String s3ErrorFilePath = fileSystemUri + "/" + stagingDataset.getValidationErrorFile();

        return StagingErrorOutput.builder()
                .errorFilePath(s3ErrorFilePath)
                .errorFileSystemUri(fileSystemUri)
                .temporaryFilePath(temporaryFilePath(stagingDataset))
                .build();
    }

    @Override
    public ErrorFileLinkOrStream downloadValidationErrorFile(final StagingDataset stagingDataset) {
        Date expiry = DateUtils.addSeconds(timeSource.nowAsDate(), DOWNLOAD_LINK_EXPIRY_SECONDS);

        GeneratePresignedUrlRequest presignedUrlRequest =
                new GeneratePresignedUrlRequest(
                        errorFileS3Bucket, stagingDataset.getValidationErrorFile())
                        .withExpiration(expiry);

        URL url = s3Client.generatePresignedUrl(presignedUrlRequest);

        return ErrorFileLinkOrStream.fileLink(url);
    }

    private String temporaryFilePath(final StagingDataset stagingDataset) {
        return errorFileSystemURI() + "/" + prefix() + "temp/" + stagingDataset.getId() + "/";
    }

    private String prefix() {
        return StringUtils.isBlank(errorFileS3Prefix)
                ? ""
                : errorFileS3Prefix + "/";
    }

    private String errorFileSystemURI() {
        return s3Protocol + "://" + errorFileS3Bucket;
    }
}
