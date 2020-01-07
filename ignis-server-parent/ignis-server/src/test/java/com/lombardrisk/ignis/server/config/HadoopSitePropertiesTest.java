package com.lombardrisk.ignis.server.config;

import com.google.common.collect.ImmutableMap;
import com.lombardrisk.ignis.server.config.job.HadoopSiteProperties;
import org.apache.hadoop.conf.Configuration;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class HadoopSitePropertiesTest {

    private HadoopSiteProperties siteProperties = new HadoopSiteProperties();

    @Test
    public void toConfiguration_ReturnsHadoopConfiguration() {
        siteProperties.setConf(ImmutableMap.of(
                "fs.defaultFS", "hdfs://url:9",
                "hadoop.tmp.dir", "hdfs://url:9/tmp"));

        Configuration config = siteProperties.toConfiguration();
        assertThat(config.get("fs.defaultFS")).isEqualTo("hdfs://url:9");
        assertThat(config.get("hadoop.tmp.dir")).isEqualTo("hdfs://url:9/tmp");
    }
}