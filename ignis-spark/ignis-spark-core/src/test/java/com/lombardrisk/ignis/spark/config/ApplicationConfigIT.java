package com.lombardrisk.ignis.spark.config;

import org.apache.commons.lang3.SystemUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

public class ApplicationConfigIT {

    @BeforeClass
    public static void setupHadoop() {
        if (SystemUtils.IS_OS_WINDOWS) {
            System.setProperty(
                    "hadoop.home.dir",
                    Paths.get("../../ignis-core/ignis-common/src/test/resources/hadoop").toAbsolutePath().toString());
        }
    }

    @Test
    public void applicationConfig_WithSparkSessions_CreatesSparkSessionsAndJavaSparkContext() {
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setDebugMode(true);

        assertThat(applicationConfig.sparkSession())
                .isNotNull();
        assertThat(applicationConfig.javaSparkContext())
                .isNotNull();
    }
}