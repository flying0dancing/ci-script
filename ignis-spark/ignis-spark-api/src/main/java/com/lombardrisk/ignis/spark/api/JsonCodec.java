package com.lombardrisk.ignis.spark.api;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public final class JsonCodec {

    private final ObjectMapper objectMapper;
    private final Charset charset;

    public JsonCodec(final ObjectMapper objectMapper) {
        this(objectMapper, StandardCharsets.UTF_8);
    }

    public JsonCodec(final ObjectMapper objectMapper, final Charset charset) {
        this.objectMapper = objectMapper;
        this.charset = charset;
    }

    public String encode(final Object object) throws IOException {
        return encode(objectMapper.writeValueAsString(object));
    }

    public <T> T decode(final String json, final Class<T> tClass) throws IOException {
        return objectMapper.readValue(decode(json), tClass);
    }

    public String encode(final String json) {
        try {
            return URLEncoder.encode(json, charset.name());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public String decode(final String json) {
        try {
            return URLDecoder.decode(json, charset.name());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
