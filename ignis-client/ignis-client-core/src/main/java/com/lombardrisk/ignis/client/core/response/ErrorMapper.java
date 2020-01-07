package com.lombardrisk.ignis.client.core.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.util.List;

@Slf4j
public class ErrorMapper {

    private final ObjectMapper objectMapper;

    public ErrorMapper(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<ApiErrorCode> convertToErrorCodes(final ResponseBody responseBody) throws IOException {
        TypeFactory typeFactory = objectMapper.getTypeFactory();
        return objectMapper.readValue(
                responseBody.string(), typeFactory.constructCollectionType(List.class, ApiErrorCode.class));
    }
}
