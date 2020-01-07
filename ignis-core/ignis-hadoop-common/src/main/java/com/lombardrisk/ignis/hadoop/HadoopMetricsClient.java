package com.lombardrisk.ignis.hadoop;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Try;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class HadoopMetricsClient {

    private static final String FS_BEAN_NAME = "Hadoop:service=NameNode,name=FSNamesystem";

    private final OkHttpClient client;
    private final HttpUrl baseUrl;
    private final ObjectMapper objectMapper;

    public HadoopMetricsClient(
            final OkHttpClient client,
            final HttpUrl baseUrl,
            final ObjectMapper objectMapper) {
        this.client = client;
        this.baseUrl = baseUrl;
        this.objectMapper = objectMapper;
    }

    public Try<HadoopFSMetrics> getHadoopFSMetrics() {
        HttpUrl httpUrl = baseUrl.newBuilder()
                .addPathSegment("jmx")
                .addQueryParameter("qry", FS_BEAN_NAME)
                .build();

        Request request = new Request.Builder().url(httpUrl).build();

        Call call = client.newCall(request);

        return Try.withResources(call::execute).of(this::parseResponse);
    }

    private HadoopFSMetrics parseResponse(final Response response) throws IOException {
        String json = response.body().string();

        if (isBlank(json)) {
            throw new IllegalStateException("Got empty metrics response from Hadoop NameNode");
        }

        JsonNode beans = objectMapper.readTree(json).get("beans");
        return objectMapper.treeToValue(beans.get(0), HadoopFSMetrics.class);
    }
}
