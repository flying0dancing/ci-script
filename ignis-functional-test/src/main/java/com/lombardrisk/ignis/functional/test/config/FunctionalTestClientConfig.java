package com.lombardrisk.ignis.functional.test.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lombardrisk.ignis.client.core.config.ClientConfig;
import com.lombardrisk.ignis.client.core.config.ClientContext;
import com.lombardrisk.ignis.client.design.RuleSetClient;
import com.lombardrisk.ignis.client.external.dataset.DatasetClient;
import com.lombardrisk.ignis.client.external.drillback.DrillbackClient;
import com.lombardrisk.ignis.client.external.job.JobsClient;
import com.lombardrisk.ignis.client.external.job.StagingClient;
import com.lombardrisk.ignis.client.external.pipeline.PipelineClient;
import com.lombardrisk.ignis.client.external.productconfig.ProductConfigClient;
import com.lombardrisk.ignis.client.internal.FeaturesClient;
import com.lombardrisk.ignis.client.internal.InternalDatasetClient;
import com.lombardrisk.ignis.functional.test.config.properties.ClientProperties;
import com.lombardrisk.ignis.functional.test.config.properties.IgnisServerProperties;
import com.lombardrisk.ignis.functional.test.config.properties.Timeouts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;

import javax.net.ssl.X509TrustManager;

@Configuration
@EnableConfigurationProperties({ IgnisServerProperties.class, ClientProperties.class, Timeouts.class })
public class FunctionalTestClientConfig {

    private static final X509TrustManager IGNORE_SSL_TRUST_MANAGER = new IgnoreSslTrustManager();

    private final IgnisServerProperties serverConfig;
    private final ClientProperties clientProperties;

    @Autowired
    public FunctionalTestClientConfig(
            final IgnisServerProperties serverConfig,
            final ClientProperties clientProperties) {
        this.serverConfig = serverConfig;
        this.clientProperties = clientProperties;
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public ClientConfig clientConfig() {
        ClientContext context = ClientContext.builder()
                .connectionTimeout(clientProperties.getConnectionTimeout())
                .requestTimeout(clientProperties.getRequestTimeout())
                .build();

        return new ClientConfig(context, IGNORE_SSL_TRUST_MANAGER, objectMapper());
    }

    @Bean
    public Retrofit ignisServerRetrofit() {
        return clientConfig().retrofit(serverConfig.toConnectionProperties());
    }

    @Bean
    public StagingClient stagingClient() {
        return ignisServerRetrofit().create(StagingClient.class);
    }

    @Bean
    public PipelineClient pipelineClient() {
        return ignisServerRetrofit().create(PipelineClient.class);
    }

    @Bean
    public DrillbackClient drillbackClient() {
        return ignisServerRetrofit().create(DrillbackClient.class);
    }

    @Bean
    public InternalDatasetClient datasetClient() {
        return ignisServerRetrofit().create(InternalDatasetClient.class);
    }

    @Bean
    public RuleSetClient ruleSetClient() {
        return ignisServerRetrofit().create(RuleSetClient.class);
    }

    @Bean
    public JobsClient jobsClient() {
        return ignisServerRetrofit().create(JobsClient.class);
    }

    @Bean
    public DatasetClient externalDatasetClient() {
        return ignisServerRetrofit().create(DatasetClient.class);
    }

    @Bean
    public ProductConfigClient productConfigClient() {
        return ignisServerRetrofit().create(ProductConfigClient.class);
    }

    @Bean
    public FeaturesClient featuresClient() {
        return ignisServerRetrofit().create(FeaturesClient.class);
    }
}
