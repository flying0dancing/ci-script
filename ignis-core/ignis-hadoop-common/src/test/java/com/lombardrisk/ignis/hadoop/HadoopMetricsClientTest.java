package com.lombardrisk.ignis.hadoop;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Try;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.ConnectException;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.assertj.core.api.Assertions.assertThat;

public class HadoopMetricsClientTest {
    private HadoopMetricsClient hadoopMetricsClient;
    private MockWebServer server;
    private OkHttpClient okHttpClient;
    private ObjectMapper objectMapper;

    @Before
    public void setUp() throws IOException {
        okHttpClient = new OkHttpClient.Builder().build();
        objectMapper = new ObjectMapper();

        server = new MockWebServer();
        server.start();

        HttpUrl httpUrl = server.url(EMPTY);
        hadoopMetricsClient = new HadoopMetricsClient(okHttpClient, httpUrl, objectMapper);
    }

    @After
    public void tearDown() throws Exception {
        server.shutdown();
    }

    @Test
    public void getHadoopFSMetrics_OkResponse_ReturnsMetrics() {
        server.enqueue(new MockResponse().setResponseCode(200).setBody(getHadoopResponse()));

        Try<HadoopFSMetrics> metrics = hadoopMetricsClient.getHadoopFSMetrics();

        assertThat(metrics.get().getCapacityUsed()).isEqualTo(241852198L);
        assertThat(metrics.get().getCapacityRemaining()).isEqualTo(83441348788L);
        assertThat(metrics.get().getCapacityTotal()).isEqualTo(107361579008L);
    }

    @Test
    public void getHadoopFSMetrics_FailToConnect_ReturnsError() {
        HttpUrl invalidUrl = new HttpUrl.Builder().scheme("http").host("localhost").port(1).build();

        hadoopMetricsClient = new HadoopMetricsClient(okHttpClient, invalidUrl, objectMapper);
        Try<HadoopFSMetrics> metrics = hadoopMetricsClient.getHadoopFSMetrics();

        assertThat(metrics.isFailure()).isTrue();
        assertThat(metrics.getCause()).isInstanceOf(ConnectException.class);
    }

    @Test
    public void getHadoopFSMetrics_InvalidJson_ReturnsError() {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("invalid json response"));

        Try<HadoopFSMetrics> metrics = hadoopMetricsClient.getHadoopFSMetrics();

        assertThat(metrics.isFailure()).isTrue();
        assertThat(metrics.getCause()).isInstanceOf(JsonParseException.class);
    }

    @Test
    public void getHadoopFSMetrics_OkResponseWithoutBody_ReturnsError() {
        server.enqueue(new MockResponse().setResponseCode(200));

        Try<HadoopFSMetrics> metrics = hadoopMetricsClient.getHadoopFSMetrics();

        assertThat(metrics.isFailure()).isTrue();
        assertThat(metrics.getCause()).isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void getHadoopFSMetrics_CallsHadoopNameNodeForMetrics() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(200).setBody(getHadoopResponse()));

        hadoopMetricsClient.getHadoopFSMetrics();

        RecordedRequest recordedRequest = server.takeRequest();

        assertThat(recordedRequest.getPath()).isEqualTo("/jmx?qry=Hadoop:service%3DNameNode,name%3DFSNamesystem");
        assertThat(recordedRequest.getMethod()).isEqualTo("GET");
        assertThat(server.getRequestCount()).isEqualTo(1);
    }

    private static String getHadoopResponse() {
        return "{"
                + "  \"beans\": ["
                + "    {"
                + "      \"name\": \"Hadoop:service=NameNode,name=FSNamesystem\","
                + "      \"CapacityTotal\": 107361579008,"
                + "      \"CapacityTotalGB\": 100.0,"
                + "      \"CapacityUsed\": 241852198,"
                + "      \"CapacityUsedGB\": 0.0,"
                + "      \"CapacityRemaining\": 83441348788,"
                + "      \"CapacityRemainingGB\": 78.0,"
                + "      \"CapacityUsedNonDFS\": 23678378022"
                + "    }\n"
                + "  ]\n"
                + "}";
    }
}