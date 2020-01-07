package com.lombardrisk.ignis.server.util.spark;

import com.google.common.annotations.VisibleForTesting;
import com.lombardrisk.ignis.web.common.config.TogglzConfiguration;
import org.apache.spark.SparkConf;
import org.springframework.core.env.Environment;

import java.io.File;
import java.util.Map;

import static com.lombardrisk.ignis.config.VariableConstants.KEYSTORE_FILE;

public class SparkConfFactory {
    private Map<String, String> conf;
    private final Environment environment;
    private final TogglzConfiguration togglzConfiguration;

    public SparkConfFactory(
            final Map<String, String> conf,
            final Environment environment,
            final TogglzConfiguration togglzConfiguration) {

        this.conf = conf;
        this.environment = environment;
        this.togglzConfiguration = togglzConfiguration;
    }

    public SparkConf create() {
        SparkConf sparkConf = new SparkConf();

        conf.forEach(sparkConf::set);

        sparkConf.set("job.features.file", togglzConfiguration.getFeaturePropertiesFile().getName());
        sparkConf.set(KEYSTORE_FILE, environment.getRequiredProperty("server.ssl.key-store", File.class).getName());

        return sparkConf;
    }

    @VisibleForTesting
    public void setConf(final Map<String, String> conf) {
        this.conf = conf;
    }
}
