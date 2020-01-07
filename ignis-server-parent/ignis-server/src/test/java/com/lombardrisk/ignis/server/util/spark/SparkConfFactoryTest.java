package com.lombardrisk.ignis.server.util.spark;

import com.google.common.collect.ImmutableMap;
import com.lombardrisk.ignis.web.common.config.TogglzConfiguration;
import org.apache.spark.SparkConf;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;

import java.io.File;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SparkConfFactoryTest {

    @InjectMocks
    private SparkConfFactory sparkConfFactory;
    @Mock
    private Environment environment;
    @Mock
    private TogglzConfiguration togglzConfiguration;

    @Before
    public void setUp() {
        sparkConfFactory.setConf(emptyMap());

        when(environment.getRequiredProperty(any(), eq(File.class)))
                .thenReturn(new File("something"));

        when(togglzConfiguration.getFeaturePropertiesFile())
                .thenReturn(new File("something"));
    }

    @Test
    public void create_ReturnsSparkConfWithProperties() {
        sparkConfFactory.setConf(ImmutableMap.of(
                "spark.conf.1", "a",
                "spark.conf.2", "aa"));

        SparkConf sparkConf = sparkConfFactory.create();
        assertThat(sparkConf.get("spark.conf.1")).isEqualTo("a");
        assertThat(sparkConf.get("spark.conf.2"))
                .isEqualTo("aa");
    }

    @Test
    public void create_SetsKeystoreFile() {
        when(environment.getRequiredProperty("server.ssl.key-store", File.class))
                .thenReturn(new File("path/to/some/keystorefile"));

        SparkConf sparkConf = sparkConfFactory.create();

        assertThat(sparkConf.get("keystore.file")).isEqualTo("keystorefile");
    }

    @Test
    public void create_SetsFeaturesFile() {
        when(togglzConfiguration.getFeaturePropertiesFile())
                .thenReturn(new File("path/to/the/feature.file"));

        SparkConf sparkConf = sparkConfFactory.create();

        assertThat(sparkConf.get("job.features.file")).isEqualTo("feature.file");
    }
}