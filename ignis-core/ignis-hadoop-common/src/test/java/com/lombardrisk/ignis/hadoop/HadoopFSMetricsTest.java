package com.lombardrisk.ignis.hadoop;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HadoopFSMetricsTest {

    @Test
    public void getCapacityTotalMB() {
        HadoopFSMetrics metrics = new HadoopFSMetrics();

        metrics.setCapacityTotal(FileUtils.ONE_MB);

        assertThat(metrics.getCapacityTotalMB()).isEqualTo(1);
    }

    @Test
    public void getCapacityUsedMB() {
        HadoopFSMetrics metrics = new HadoopFSMetrics();

        metrics.setCapacityUsed(FileUtils.ONE_MB);

        assertThat(metrics.getCapacityUsedMB()).isEqualTo(1);
    }

    @Test
    public void getCapacityRemainingMB() {
        HadoopFSMetrics metrics = new HadoopFSMetrics();

        metrics.setCapacityRemaining(FileUtils.ONE_MB);

        assertThat(metrics.getCapacityRemainingMB()).isEqualTo(1);
    }
}