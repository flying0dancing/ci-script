package com.lombardrisk.ignis.spark.config;

import com.lombardrisk.ignis.client.core.BasicAuthInterceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.env.Environment;
import retrofit2.converter.jackson.JacksonConverterFactory;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.security.KeyStore;

import static com.lombardrisk.ignis.config.VariableConstants.IGNIS_HOST;
import static com.lombardrisk.ignis.config.VariableConstants.IGNIS_HTTPS_PORT;
import static com.lombardrisk.ignis.config.VariableConstants.IGNIS_SERVER_CONTEXT_PATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IgnisClientConfigTest {

    @Mock
    private Environment environment;
    private X509TrustManager defaultTm;

    private IgnisClientConfig client;

    @Before
    public void init() throws Exception {
        when(environment.getRequiredProperty(IGNIS_HOST))
                .thenReturn("host");
        when(environment.getProperty(IGNIS_HTTPS_PORT))
                .thenReturn("444");
        when(environment.getRequiredProperty(IGNIS_SERVER_CONTEXT_PATH))
                .thenReturn("path");

        TrustManagerFactory tmf = TrustManagerFactory
                .getInstance(TrustManagerFactory.getDefaultAlgorithm());
        // Using null here initialises the TMF with the default trust store.
        tmf.init((KeyStore) null);

        for (final TrustManager tm : tmf.getTrustManagers()) {
            if (tm instanceof X509TrustManager) {
                defaultTm = (X509TrustManager) tm;
                break;
            }
        }
        client = new IgnisClientConfig(environment, defaultTm, "correlationId");
    }

    @Test
    public void retrofit_CreatesRetrofitWithBaseURL() {
        when(environment.getRequiredProperty(IGNIS_HOST))
                .thenReturn("myhost");
        when(environment.getProperty(IGNIS_HTTPS_PORT))
                .thenReturn("4443");
        when(environment.getRequiredProperty(IGNIS_SERVER_CONTEXT_PATH))
                .thenReturn("ignis-path");

        assertThat(
                client.retrofit()
                        .baseUrl().toString())
                .isEqualTo("https://myhost:4443/ignis-path/");
    }

    @Test
    public void retrofit_MissingHost_ThrowsException() {
        when(environment.getRequiredProperty(IGNIS_HOST))
                .thenReturn(null);

        assertThatThrownBy(
                () -> client.retrofit())
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void retrofit_MissingPort_DefaultPortToHttpsScheme() {
        when(environment.getProperty(IGNIS_HTTPS_PORT))
                .thenReturn(null);

        assertThat(
                client.retrofit().baseUrl().port())
                .isEqualTo(443);
    }

    @Test
    public void retrofit_MissingContextPath_ThrowsException() {
        when(environment.getRequiredProperty(IGNIS_SERVER_CONTEXT_PATH))
                .thenReturn(null);

        assertThatThrownBy(
                () -> client.retrofit())
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void retrofit_CreatesRetrofitJacksonFactory() {
        assertThat(
                client.retrofit()
                        .converterFactories())
                .filteredOn(JacksonConverterFactory.class::isInstance)
                .isNotNull();
    }

    @Test
    public void retrofit_CreatesOkHttpClientWithBasicAuthInterceptor() {
        assertThat(
                client.retrofit().callFactory())
                .isInstanceOfSatisfying(
                        OkHttpClient.class,
                        okHttpClient -> assertThat(okHttpClient.interceptors())
                                .filteredOn(BasicAuthInterceptor.class::isInstance)
                                .isNotNull());
    }

    @Test
    public void stagingClient_WithContextPathUrl_CreatesRequestWithContextPath() {
        when(environment.getRequiredProperty(IGNIS_HOST))
                .thenReturn("fcr");
        when(environment.getProperty(IGNIS_HTTPS_PORT))
                .thenReturn("888");
        when(environment.getRequiredProperty(IGNIS_SERVER_CONTEXT_PATH))
                .thenReturn("engine");

        Request dataQualityRequest =
                client.stagingClient()
                        .updateDataSetState(12L, "BoxOfficeHit")
                        .request();

        assertThat(dataQualityRequest.url().toString())
                .isEqualTo("https://fcr:888/engine/api/v1/stagingItems/12?state=BoxOfficeHit");
    }
}