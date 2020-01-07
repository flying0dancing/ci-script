package com.lombardrisk.ignis.spark;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lombardrisk.ignis.api.calendar.HolidayCalendarModule;
import com.lombardrisk.ignis.common.annotation.ExcludeFromTest;
import com.lombardrisk.ignis.spark.api.pipeline.PipelineAppConfig;
import com.lombardrisk.ignis.spark.config.ApplicationConfig;
import com.lombardrisk.ignis.spark.config.FeatureFlagConfiguration;
import com.lombardrisk.ignis.spark.config.IgnisClientConfig;
import com.lombardrisk.ignis.spark.config.PhoenixConfiguration;
import com.lombardrisk.ignis.spark.config.initializer.CorrelationIdInitializer;
import com.lombardrisk.ignis.spark.config.initializer.JobRequestInitializer;
import com.lombardrisk.ignis.spark.pipeline.config.PipelineJobConfiguration;
import com.lombardrisk.ignis.spark.pipeline.job.PipelineJobOperator;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import static com.lombardrisk.ignis.common.json.MapperWrapper.MAPPER;

@SpringBootConfiguration
@ExcludeFromTest
public class Application {

    public static void main(final String[] args) {
        ConfigurableApplicationContext applicationContext =
                new SpringApplicationBuilder(Application.class)
                        .web(WebApplicationType.NONE)
                        .sources(
                                ApplicationConfig.class,
                                FeatureFlagConfiguration.class,
                                IgnisClientConfig.class,
                                PhoenixConfiguration.class,
                                PipelineJobConfiguration.class)
                        .initializers(new JobRequestInitializer<>(args[0], objectMapper(), PipelineAppConfig.class))
                        .initializers(new CorrelationIdInitializer(args[1]))
                        .run();

        PipelineJobOperator pipelineJobOperator = applicationContext.getBean(PipelineJobOperator.class);
        PipelineAppConfig pipelineAppConfig = applicationContext.getBean(PipelineAppConfig.class);
        pipelineJobOperator.runJob(pipelineAppConfig);
    }

    private static ObjectMapper objectMapper() {
        ObjectMapper objectMapper = MAPPER;
        objectMapper.registerModule(new HolidayCalendarModule());
        return objectMapper;
    }
}
