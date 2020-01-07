package com.lombardrisk.ignis.server.health;

import com.amazonaws.services.s3.AmazonS3;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;

import java.time.Duration;

@UtilityClass
@Slf4j
public final class S3HealthChecks {

    private static final String UP_MESSAGE = "CONNECTED";
    private static final String DOWN_MESSAGE = "INACCESSIBLE";

    public static MemoizedHealthCheck s3HealthCheck(
            final AmazonS3 amazonS3,
            final String sourceBucket,
            final String errorBucket,
            final Duration healthRefreshRate) {

        return MemoizedHealthCheck.create(
                () -> checkS3Connection(amazonS3, sourceBucket, errorBucket),
                healthRefreshRate);
    }

    private static Health checkS3Connection(
            final AmazonS3 amazonS3, final String sourceBucket, final String errorBucket) {

        try {
            return bucketsExist(amazonS3, sourceBucket, errorBucket);
        } catch (Exception e) {
            return Health.down(e)
                    .build();
        }
    }

    private static Health bucketsExist(final AmazonS3 amazonS3, final String sourceBucket, final String errorBucket) {
        log.debug("Checking s3 bucket {}", sourceBucket);
        boolean canConnectToSourceBucket = amazonS3.doesBucketExistV2(sourceBucket);

        log.debug("Checking s3 bucket {}", errorBucket);
        boolean canConnectToErrorBucket = amazonS3.doesBucketExistV2(errorBucket);

        if (canConnectToSourceBucket && canConnectToErrorBucket) {
            return Health.up().build();
        }

        return Health.down()
                .withDetail("SourceBucket: " + sourceBucket, canConnectToSourceBucket ? UP_MESSAGE : DOWN_MESSAGE)
                .withDetail("ErrorBucket: " + errorBucket, canConnectToErrorBucket ? UP_MESSAGE : DOWN_MESSAGE)
                .build();
    }
}
