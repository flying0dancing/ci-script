package com.lombardrisk.ignis.server.config.datasource;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.auth.InstanceProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.lombardrisk.ignis.common.time.TimeSource;
import com.lombardrisk.ignis.server.batch.staging.file.S3FileService;
import com.lombardrisk.ignis.server.batch.staging.file.error.S3ErrorFileService;
import com.lombardrisk.ignis.server.health.MemoizedHealthCheck;
import com.lombardrisk.ignis.server.health.S3HealthChecks;
import com.lombardrisk.ignis.server.job.staging.file.DataSourceService;
import com.lombardrisk.ignis.server.job.staging.file.ErrorFileService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.function.Supplier;

@Slf4j
@Configuration
@ConditionalOnProperty(name = "dataset.source.location.type", havingValue = "S3")
public class S3DataSourceConfiguration implements DatasourceFileConfiguration {

    private static final Duration S3_HEALTH_CHECK_REFRESH_RATE = Duration.ofMinutes(2);

    @Value("${dataset.source.location.s3.credentialsSource}")
    private CredentialsSource credentialsSource;

    @Value("${dataset.source.location.s3.region}")
    private String s3Region;

    @Value("${dataset.source.location.s3.bucket}")
    private String s3Bucket;

    @Value("${dataset.source.location.s3.prefix}")
    private String s3Prefix;

    @Value("${dataset.error.location.s3.bucket}")
    private String s3BucketForErrorFiles;

    @Value("${dataset.error.location.s3.prefix}")
    private String s3ErrorPrefix;

    @Value("${s3.protocol}")
    private String s3Protocol;

    private final TimeSource timeSource;

    @Autowired
    public S3DataSourceConfiguration(final TimeSource timeSource) {
        this.timeSource = timeSource;
    }

    @AllArgsConstructor
    public enum CredentialsSource {
        ENVIRONMENT(EnvironmentVariableCredentialsProvider::new),
        INSTANCE(InstanceProfileCredentialsProvider::getInstance);

        private final Supplier<AWSCredentialsProvider> credentialsProvider;
    }

    @Override
    public Source getDatasetFileSource() {
        return Source.S3;
    }

    @Bean
    public AmazonS3 amazonS3() {
        return AmazonS3ClientBuilder.standard()
                .withRegion(Regions.fromName(s3Region))
                .withCredentials(credentialsSource.credentialsProvider.get())
                .build();
    }

    @Bean
    @Override
    public DataSourceService dataSourceService() {
        return new S3FileService(amazonS3(), s3Protocol, s3Bucket, s3Prefix);
    }

    @Bean
    @Override
    public ErrorFileService errorFileService() {
        return new S3ErrorFileService(amazonS3(), s3Protocol, s3BucketForErrorFiles, s3ErrorPrefix, timeSource);
    }

    @Bean
    public MemoizedHealthCheck s3HealthCheck() {
        return S3HealthChecks.s3HealthCheck(
                amazonS3(), s3Bucket, s3BucketForErrorFiles, S3_HEALTH_CHECK_REFRESH_RATE);
    }
}
