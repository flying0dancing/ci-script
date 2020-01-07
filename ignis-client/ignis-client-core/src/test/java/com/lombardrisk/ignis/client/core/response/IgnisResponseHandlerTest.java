package com.lombardrisk.ignis.client.core.response;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import retrofit2.Response;

import java.io.IOException;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IgnisResponseHandlerTest {

    private static final Response<List<String>> ERROR_RESPONSE =
            Response.error(400, ResponseBody.create(MediaType.parse("application/json"), ""));

    @Mock
    private ErrorMapper errorMapper;

    @InjectMocks
    private IgnisResponseHandler ignisResponseMappingService;

    @Test
    public void handleResponse_responseSuccessful_ReturnsResponse() throws IOException {
        IgnisResponse<Integer> response = ignisResponseMappingService.handleResponse(Response.success(100));

        assertThat(response.getValue())
                .isEqualTo(100);
    }

    @Test
    public void handleResponse_responseSuccessfulAndNull_ReturnsResponse() throws IOException {
        IgnisResponse<Integer> response = ignisResponseMappingService.handleResponse(Response.success(null));

        assertThat(response.getValue())
                .isNull();
    }

    @Test
    public void handleResponse_errorMapperReturnsErrorCodes_ReturnsApiErrorCodes() throws IOException {

        ApiErrorCode apiErrorCode = ApiErrorCode.builder()
                .errorMessage("Dataset not found")
                .errorCode("NOT FOUND")
                .build();

        when(errorMapper.convertToErrorCodes(any()))
                .thenReturn(singletonList(apiErrorCode));

        IgnisResponse<List<String>> response = ignisResponseMappingService.handleResponse(ERROR_RESPONSE);

        assertThat(response.getErrorCodes())
                .contains(apiErrorCode);
    }

    @Test
    public void handleResponse_ErrorCodesCannotBeMapped_ReturnsErrorMessage() throws IOException {
        when(errorMapper.convertToErrorCodes(any()))
                .thenThrow(new IOException("Oops"));

        assertThatThrownBy(() -> ignisResponseMappingService.handleResponse(ERROR_RESPONSE))
                .isInstanceOf(IOException.class)
                .hasMessage("Oops");
    }
}