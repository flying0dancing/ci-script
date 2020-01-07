package com.lombardrisk.ignis.server.health;

import com.lombardrisk.ignis.fs.FileSystemTemplate;
import lombok.experimental.UtilityClass;
import org.apache.hadoop.yarn.api.records.YarnClusterMetrics;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.springframework.boot.actuate.health.Health;

import java.time.Duration;

@UtilityClass
public class HadoopHealthChecks {

    public static MemoizedHealthCheck yarnHealthCheck(final YarnClient yarnClient, final Duration healthRefreshRate) {
        return MemoizedHealthCheck.create(() -> isConnectedToYarn(yarnClient), healthRefreshRate);
    }

    public static MemoizedHealthCheck hdfsHealthCheck(
            final FileSystemTemplate fileSystemTemplate,
            final Duration healthRefreshRate) {

        return MemoizedHealthCheck.create(() -> isConnectedToHdfs(fileSystemTemplate), healthRefreshRate);
    }

    private static Health isConnectedToYarn(final YarnClient yarnClient) {
        try {
            YarnClusterMetrics yarnClusterMetrics = yarnClient.getYarnClusterMetrics();

            return Health.up()
                    .withDetail("nodeManagers", yarnClusterMetrics.getNumNodeManagers())
                    .build();
        } catch (Exception e) {
            return Health.down(e)
                    .build();
        }
    }

    private static Health isConnectedToHdfs(final FileSystemTemplate fileSystemTemplate) {
        try {
            boolean exists = fileSystemTemplate.exists("/");

            return Health.up()
                    .withDetail("rootPathExists", exists)
                    .build();
        } catch (Exception e) {
            return Health.down(e)
                    .build();
        }
    }
}
