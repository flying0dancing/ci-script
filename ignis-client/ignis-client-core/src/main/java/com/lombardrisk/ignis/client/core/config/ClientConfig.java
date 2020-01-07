package com.lombardrisk.ignis.client.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.lombardrisk.ignis.client.core.BasicAuthInterceptor;
import com.lombardrisk.ignis.client.core.LoggerInterceptor;
import lombok.Getter;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Getter
public class ClientConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClientConfig.class);
    private static final ClientContext DEFAULT_CONTEXT = ClientContext.builder()
            .requestTimeout(600)
            .requestTimeout(600)
            .build();

    private final ClientContext clientProperties;
    private final X509TrustManager trustManager;
    private final ObjectMapper objectMapper;

    public ClientConfig(
            final ClientContext clientProperties,
            final X509TrustManager trustManager,
            final ObjectMapper objectMapper) {
        this.clientProperties = clientProperties;
        this.trustManager = trustManager;
        this.objectMapper = objectMapper;
    }

    public ClientConfig(final X509TrustManager trustManager, final ObjectMapper objectMapper) {
        this(DEFAULT_CONTEXT, trustManager, objectMapper);
    }

    public Retrofit retrofit(final ConnectionProperties serverConfig) {
        try {
            return createRetrofit(serverConfig);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new IllegalStateException(e);
        }
    }

    private Retrofit createRetrofit(final ConnectionProperties connectionProperties)
            throws NoSuchAlgorithmException, KeyManagementException {

        OkHttpClient okHttpClient = createOkHttpClient(connectionProperties);

        LOGGER.info("Configure Retrofit with base URL [{}]", connectionProperties.getUrl());

        objectMapper.setDateFormat(new StdDateFormat());

        return new Retrofit.Builder()
                .addConverterFactory(JacksonConverterFactory.create(objectMapper))
                .client(okHttpClient)
                .baseUrl(connectionProperties.getUrl().toString())
                .build();
    }

    private OkHttpClient createOkHttpClient(final ConnectionProperties connectionProperties) throws NoSuchAlgorithmException, KeyManagementException {

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, new TrustManager[]{ trustManager }, null);

        SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

        long timeout = clientProperties.getRequestTimeout();
        long connectionTimeout = clientProperties.getConnectionTimeout();

        return new OkHttpClient.Builder()
                .readTimeout(timeout, MILLISECONDS)
                .writeTimeout(timeout, MILLISECONDS)
                .connectTimeout(connectionTimeout, MILLISECONDS)
                .addInterceptor(authInterceptor(connectionProperties))
                .addInterceptor(new LoggerInterceptor())
                .sslSocketFactory(sslSocketFactory, trustManager)
                .hostnameVerifier((hostname, session) -> true)
                .followRedirects(true)
                .build();
    }

    private BasicAuthInterceptor authInterceptor(final ConnectionProperties serverConfig) {
        return new BasicAuthInterceptor(serverConfig.getBasicAuthUsername(), serverConfig.getBasicAuthPassword());
    }
}
