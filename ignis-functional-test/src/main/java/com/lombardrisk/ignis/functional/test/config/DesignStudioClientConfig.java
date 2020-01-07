package com.lombardrisk.ignis.functional.test.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.lombardrisk.ignis.client.core.LoggerInterceptor;
import com.lombardrisk.ignis.client.design.RuleSetClient;
import com.lombardrisk.ignis.client.design.pipeline.PipelineClient;
import com.lombardrisk.ignis.client.design.productconfig.ProductConfigClient;
import com.lombardrisk.ignis.client.design.schema.SchemaClient;
import com.lombardrisk.ignis.functional.test.config.properties.ClientProperties;
import com.lombardrisk.ignis.functional.test.config.properties.DesignStudioProperties;
import com.lombardrisk.ignis.functional.test.steps.service.PipelineService;
import com.lombardrisk.ignis.functional.test.steps.service.RuleService;
import com.lombardrisk.ignis.functional.test.steps.service.TableService;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.net.URL;

@Configuration
@EnableConfigurationProperties({ DesignStudioProperties.class, ClientProperties.class })
public class DesignStudioClientConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(DesignStudioClientConfig.class);
    private final DesignStudioProperties designStudioProperties;
    private final ClientProperties clientProperties;

    @Autowired
    public DesignStudioClientConfig(
            final DesignStudioProperties designStudioProperties,
            final ClientProperties clientProperties) {
        this.designStudioProperties = designStudioProperties;
        this.clientProperties = clientProperties;
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new GuavaModule());
        return objectMapper;
    }

    @Bean
    public Retrofit designStudioRetrofit() {
        URL designStudioUrl = designStudioProperties.getUrl();

        LOGGER.info("Configure Design Studio Retrofit with base URL [{}]", designStudioUrl);

        return new Retrofit.Builder()
                .baseUrl(designStudioUrl.toString())
                .addConverterFactory(JacksonConverterFactory.create(objectMapper()))
                .client(new OkHttpClient.Builder()
                        .addInterceptor(new LoggerInterceptor())
                        .build())
                .build();
    }

    @Bean
    public ProductConfigClient designProductConfigClient() {
        return designStudioRetrofit().create(ProductConfigClient.class);
    }

    @Bean
    public SchemaClient schemaClient() {
        return designStudioRetrofit().create(SchemaClient.class);
    }

    @Bean
    public PipelineClient designPipelineClient() {
        return designStudioRetrofit().create(PipelineClient.class);
    }

    @Bean
    public TableService designTableService() {
        return new TableService(
                objectMapper(),
                clientProperties);
    }

    @Bean
    public RuleService ruleService() {
        return new RuleService(
                designStudioRetrofit().create(RuleSetClient.class),
                objectMapper(),
                clientProperties);
    }

    @Bean
    public PipelineService pipelineService() {
        return new PipelineService(
                objectMapper(),
                clientProperties);
    }
}
