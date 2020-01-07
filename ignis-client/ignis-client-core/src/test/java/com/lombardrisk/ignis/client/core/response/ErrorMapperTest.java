package com.lombardrisk.ignis.client.core.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.Okio;
import okio.Source;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ErrorMapperTest {

    private final ErrorMapper errorMapper = new ErrorMapper(new ObjectMapper());

    @Mock
    private Source source;

    @Test
    public void convertToErrorCodes_stringMethodThrowsException_ThrowsIoException() throws Exception {
        when(source.read(any(), anyLong()))
                .thenThrow(new IOException("AYE, OH!"));

        ResponseBody responseBody = ResponseBody.create(MediaType.parse("application/json"), 0L, Okio.buffer(source));

        assertThatThrownBy(() -> errorMapper.convertToErrorCodes(responseBody))
                .isInstanceOf(IOException.class);
    }

    @Test
    public void convertToErrorCodes_CannotMarshalJson_ThrowsIoException() {
        ResponseBody responseBody = ResponseBody.create(MediaType.parse("application/json"), "THIS IS NOT JSON");

        assertThatThrownBy(() -> errorMapper.convertToErrorCodes(responseBody))
                .isInstanceOf(IOException.class)
                .hasMessage("Unrecognized token 'THIS': was expecting ('true', 'false' or 'null')\n"
                        + " at [Source: (String)\"THIS IS NOT JSON\"; line: 1, column: 5]");
    }

    @Test
    public void convertToErrorCodes_JsonWellFormed_ReturnsApiErrorCode() throws IOException {
        ResponseBody responseBody = ResponseBody.create(
                MediaType.parse("application/json"),
                "[{\"errorMessage\" : \"Datasets not found for name\", \"errorCode\" : \"NOT_FOUND\"}]");

        List<ApiErrorCode> apiErrorCodes = errorMapper.convertToErrorCodes(responseBody);

        Assertions.assertThat(apiErrorCodes)
                .contains(ApiErrorCode.builder()
                        .errorCode("NOT_FOUND")
                        .errorMessage("Datasets not found for name")
                        .build());
    }
}