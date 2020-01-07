package com.lombardrisk.ignis.server.health;

import com.amazonaws.services.s3.AmazonS3;
import com.google.common.collect.ImmutableMap;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class S3HealthCheckTest {

    @Mock
    private AmazonS3 amazonS3;

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Before
    public void setUp() {
        when(amazonS3.doesBucketExistV2(any()))
                .thenReturn(true);
    }

    @Test
    public void health_BucketDoesNotExist_ReturnsDown() {
        when(amazonS3.doesBucketExistV2("source"))
                .thenReturn(false);

        MemoizedHealthCheck s3HealthCheck = S3HealthChecks.s3HealthCheck(
                amazonS3, "source", "error", Duration.ofSeconds(1));

        Health health = s3HealthCheck.health();

        soft.assertThat(health.getStatus())
                .isEqualTo(Status.DOWN);
        soft.assertThat(health.getDetails())
                .isEqualTo(
                        ImmutableMap.of("SourceBucket: source", "INACCESSIBLE",
                                "ErrorBucket: error", "CONNECTED"));
    }

    @Test
    public void health_S3ClientThrowsException_ReturnsDown() {
        when(amazonS3.doesBucketExistV2("source"))
                .thenThrow(new RuntimeException("S3PO R5D2"));

        MemoizedHealthCheck s3HealthCheck = S3HealthChecks.s3HealthCheck(
                amazonS3, "source", "error", Duration.ofSeconds(1));

        Health health = s3HealthCheck.health();

        soft.assertThat(health.getStatus())
                .isEqualTo(Status.DOWN);
        soft.assertThat(health.getDetails())
                .isEqualTo(
                        ImmutableMap.of("error", "java.lang.RuntimeException: S3PO R5D2"));
    }

    @Test
    public void health_BothBucketsExist_ReturnsUp() {
        MemoizedHealthCheck s3HealthCheck = S3HealthChecks.s3HealthCheck(
                amazonS3, "source", "error", Duration.ofSeconds(1));

        assertThat(s3HealthCheck.health().getStatus())
                .isEqualTo(Status.UP);
    }

    @Test
    public void health_BucketsExist_ResultIsMemoized() {
        MemoizedHealthCheck s3HealthCheck = S3HealthChecks.s3HealthCheck(
                amazonS3, "source", "error", Duration.ofSeconds(1));

        s3HealthCheck.health();
        s3HealthCheck.health();
        s3HealthCheck.health();
        s3HealthCheck.health();

        verify(amazonS3, times(1)).doesBucketExistV2("source");
        verify(amazonS3, times(1)).doesBucketExistV2("error");
    }
}