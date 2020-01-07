package com.lombardrisk.ignis.server.batch.staging.file;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.lombardrisk.ignis.server.job.staging.file.DataSourceService;
import com.lombardrisk.ignis.server.job.staging.model.StagingDataset;
import com.lombardrisk.ignis.server.util.S3Utils;
import com.lombardrisk.ignis.spark.api.staging.datasource.DataSource;
import com.lombardrisk.ignis.spark.api.staging.datasource.S3CsvDataSource;

import java.util.stream.Stream;

public class S3FileService implements DataSourceService {

    private final AmazonS3 s3Client;
    private final String s3Protocol;
    private final String bucketName;
    private final String prefix;

    public S3FileService(
            final AmazonS3 s3Client,
            final String s3Protocol,
            final String bucketName,
            final String prefix) {
        this.s3Client = s3Client;
        this.s3Protocol = s3Protocol;
        this.bucketName = bucketName;
        this.prefix = prefix;
    }

    @Override
    public Stream<String> findFileNames() {
        return S3Utils.streamBucketContent(s3Client, bucketName, prefix)
                .map(S3ObjectSummary::getKey)
                .filter(key -> !key.endsWith("/"));
    }


    @Override
    public DataSource createSparkDataSource(final StagingDataset stagingDataset, final boolean header) {
        return S3CsvDataSource.builder()
                .s3Protocol(s3Protocol)
                .s3Bucket(bucketName)
                .s3Key(stagingDataset.getStagingFile())
                .header(header)
                .build();
    }

    @Override
    public String stagingFileCopyLocation(final long serviceRequestId, final String physicalTableName) {
        return null;
    }
}
