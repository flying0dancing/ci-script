package com.lombardrisk.ignis.spark.config;

import com.lombardrisk.ignis.client.core.BasicAuthInterceptor;
import com.lombardrisk.ignis.client.core.LoggerInterceptor;
import com.lombardrisk.ignis.client.external.job.StagingClient;
import com.lombardrisk.ignis.client.internal.InternalDatasetClient;
import com.lombardrisk.ignis.client.internal.PipelineStatusClient;
import com.lombardrisk.ignis.spark.config.interceptor.HeaderInterceptor;
import com.lombardrisk.ignis.spark.core.JobOperatorException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import static com.lombardrisk.ignis.common.log.CorrelationId.CORRELATION_ID_HEADER;
import static com.lombardrisk.ignis.config.VariableConstants.IGNIS_HOST;
import static com.lombardrisk.ignis.config.VariableConstants.IGNIS_HTTPS_PORT;
import static com.lombardrisk.ignis.config.VariableConstants.IGNIS_SERVER_CONTEXT_PATH;
import static com.lombardrisk.ignis.config.VariableConstants.JOB_USER_NAME;
import static com.lombardrisk.ignis.config.VariableConstants.JOB_USER_PASSWORD;
import static java.lang.Integer.parseInt;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Slf4j
@Configuration
public class IgnisClientConfig {

    private static final int DEFAULT_TIMEOUT = 60;

    private final Environment environment;
    private final X509TrustManager trustManager;
    private final String correlationId;

    public IgnisClientConfig(
            final Environment environment,
            final X509TrustManager trustManager,
            final String correlationId) {
        this.environment = environment;
        this.trustManager = trustManager;
        this.correlationId = correlationId;
    }

    @Bean
    public Retrofit retrofit() {
        String scheme = "https";
        String httpsPort = environment.getProperty(IGNIS_HTTPS_PORT);
        int port = isNotBlank(httpsPort) ? parseInt(httpsPort) : HttpUrl.defaultPort(scheme);

        String baseUrl =
                new HttpUrl.Builder()
                        .scheme(scheme)
                        .host(environment.getRequiredProperty(IGNIS_HOST))
                        .port(port)
                        .addPathSegment(environment.getRequiredProperty(IGNIS_SERVER_CONTEXT_PATH))
                        .build().toString() + "/";

        return createRetrofit(baseUrl, trustManager);
    }

    private Retrofit createRetrofit(final String baseUrl, final X509TrustManager customTm) {
        OkHttpClient okHttpClient = createOkHttpClient(customTm);

        log.info("Configure Retrofit with base URL [{}]", baseUrl);

        return new Retrofit.Builder()
                .addConverterFactory(JacksonConverterFactory.create())
                .client(okHttpClient)
                .baseUrl(baseUrl)
                .build();
    }

    private OkHttpClient createOkHttpClient(final X509TrustManager customTm) {
        BasicAuthInterceptor basicAuthInterceptor =
                new BasicAuthInterceptor(
                        environment.getProperty(JOB_USER_NAME),
                        environment.getProperty(JOB_USER_PASSWORD));

        try {
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, new TrustManager[]{ customTm }, null);

            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                    .readTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                    .connectTimeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)

                    .addInterceptor(basicAuthInterceptor)
                    .addInterceptor(new LoggerInterceptor())

                    .sslSocketFactory(sslSocketFactory, customTm)
                    .hostnameVerifier((hostname, session) -> hostname.equalsIgnoreCase(session.getPeerHost()));

            if (correlationId != null) {
                clientBuilder.addInterceptor(HeaderInterceptor.oneHeader(CORRELATION_ID_HEADER, correlationId));
            }

            return clientBuilder.build();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new JobOperatorException(e);
        }
    }

    @Bean
    public InternalDatasetClient datasetClient() {
        return retrofit().create(InternalDatasetClient.class);
    }

    @Bean
    public StagingClient stagingClient() {
        return retrofit().create(StagingClient.class);
    }

    @Bean
    public PipelineStatusClient pipelineStatusClient() {
        return retrofit().create(PipelineStatusClient.class);
    }
}
