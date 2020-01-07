package com.lombardrisk.ignis.server.config;

import com.lombardrisk.ignis.server.config.job.HadoopConfiguration;
import com.lombardrisk.ignis.server.config.job.HadoopSiteProperties;
import org.apache.hadoop.conf.Configuration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HadoopConfigurationTest {

    @Mock
    private HadoopSiteProperties hadoopSiteProperties;

    private HadoopConfiguration hadoopConfiguration;

    @Before
    public void setup() {
        hadoopConfiguration =
                new HadoopConfiguration(null, null, false, true, null, hadoopSiteProperties);
    }

    @Test
    public void hdfsSiteConfiguration_ReturnsHdfsConfiguration() {
        when(hadoopSiteProperties.toConfiguration())
                .thenReturn(new Configuration());

        hadoopConfiguration.hadoopSiteConfiguration();

        verify(hadoopSiteProperties).toConfiguration();
    }
}