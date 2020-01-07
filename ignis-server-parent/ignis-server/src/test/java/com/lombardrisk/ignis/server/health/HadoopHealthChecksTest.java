package com.lombardrisk.ignis.server.health;

import com.google.common.collect.ImmutableMap;
import com.lombardrisk.ignis.hadoop.HdfsTemplate;
import org.apache.hadoop.yarn.api.records.YarnClusterMetrics;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.assertj.core.api.JUnitSoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HadoopHealthChecksTest {

    @Mock
    private YarnClient yarnClient;

    @Mock
    private HdfsTemplate fileSystemTemplate;

    @Rule
    public JUnitSoftAssertions soft = new JUnitSoftAssertions();

    @Test
    public void yarnHealthCheck_Successful_ReturnsUpWithNumberOfNodes() throws Exception {
        when(yarnClient.getYarnClusterMetrics())
                .thenReturn(YarnClusterMetrics.newInstance(4));

        MemoizedHealthCheck memoizedHealthCheck =
                HadoopHealthChecks.yarnHealthCheck(yarnClient, Duration.ofSeconds(10));

        Health health = memoizedHealthCheck.health();

        soft.assertThat(health.getStatus())
                .isEqualTo(Status.UP);
        soft.assertThat(health.getDetails())
                .isEqualTo(ImmutableMap.of("nodeManagers", 4));
    }

    @Test
    public void yarnHealthCheck_ThrowsException_ReturnsDownHealth() throws Exception {
        when(yarnClient.getYarnClusterMetrics())
                .thenThrow(new RuntimeException("Yarnt you glad I didnt say banana"));

        MemoizedHealthCheck memoizedHealthCheck =
                HadoopHealthChecks.yarnHealthCheck(yarnClient, Duration.ofSeconds(10));

        Health health = memoizedHealthCheck.health();

        soft.assertThat(health.getStatus())
                .isEqualTo(Status.DOWN);
        soft.assertThat(health.getDetails())
                .isEqualTo(ImmutableMap.of(
                        "error",
                        "java.lang.RuntimeException: Yarnt you glad I didnt say banana"));
    }

    @Test
    public void hdfsHealthCheck_PathExists_ReturnsUpHealth() throws Exception {
        when(fileSystemTemplate.exists(anyString()))
                .thenReturn(true);

        MemoizedHealthCheck healthCheck =
                HadoopHealthChecks.hdfsHealthCheck(fileSystemTemplate, Duration.ofSeconds(1));

        Health health = healthCheck.health();
        soft.assertThat(health.getStatus())
                .isEqualTo(Status.UP);
        soft.assertThat(health.getDetails())
                .isEqualTo(ImmutableMap.of("rootPathExists", true));
    }

    @Test
    public void hdfsHealthCheck_CannotConnectToHdfs_ReturnsDownHealthAndMessage() throws Exception {
        when(fileSystemTemplate.exists(anyString()))
                .thenThrow(new RuntimeException("MR HDFS NO HERE"));

        MemoizedHealthCheck healthCheck =
                HadoopHealthChecks.hdfsHealthCheck(fileSystemTemplate, Duration.ofSeconds(1));

        Health health = healthCheck.health();

        soft.assertThat(health.getStatus())
                .isEqualTo(Status.DOWN);
        soft.assertThat(health.getDetails())
                .isEqualTo(ImmutableMap.of("error", "java.lang.RuntimeException: MR HDFS NO HERE"));
    }
}