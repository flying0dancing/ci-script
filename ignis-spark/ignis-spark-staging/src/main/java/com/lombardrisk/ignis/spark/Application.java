package com.lombardrisk.ignis.spark;

import com.lombardrisk.ignis.spark.api.staging.StagingAppConfig;
import com.lombardrisk.ignis.common.annotation.ExcludeFromTest;
import com.lombardrisk.ignis.spark.config.ApplicationConfig;
import com.lombardrisk.ignis.spark.config.initializer.CorrelationIdInitializer;
import com.lombardrisk.ignis.spark.config.FeatureFlagConfiguration;
import com.lombardrisk.ignis.spark.config.IgnisClientConfig;
import com.lombardrisk.ignis.spark.config.initializer.JobRequestInitializer;
import com.lombardrisk.ignis.spark.config.PhoenixConfiguration;
import com.lombardrisk.ignis.spark.staging.StagingJobOperator;
import com.lombardrisk.ignis.spark.staging.config.StagingJobConfiguration;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootConfiguration
@ExcludeFromTest
public class Application {

    public static void main(final String[] args) {
        ConfigurableApplicationContext applicationContext =
                new SpringApplicationBuilder(Application.class)
                        .web(WebApplicationType.NONE)
                        .initializers(new JobRequestInitializer<>(args[0], StagingAppConfig.class))
                        .initializers(new CorrelationIdInitializer(args[1]))
                        .sources(
                                ApplicationConfig.class,
                                FeatureFlagConfiguration.class,
                                IgnisClientConfig.class,
                                PhoenixConfiguration.class,
                                StagingJobConfiguration.class)
                        .run();

        StagingJobOperator stagingJobOperator = applicationContext.getBean(StagingJobOperator.class);
        stagingJobOperator.runJob();
    }
}
