package com.lombardrisk.ignis.server.controller.exception.handler;

import com.google.common.collect.Sets;
import com.lombardrisk.ignis.client.core.view.IdView;
import com.lombardrisk.ignis.server.config.IntegrationTestConfig;
import com.lombardrisk.ignis.server.product.table.TableService;
import com.lombardrisk.ignis.web.common.response.FcrResponse;
import com.machinezoo.noexception.throwing.ThrowingSupplier;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import java.util.Set;

import static com.lombardrisk.ignis.test.config.AdminUser.BASIC_AUTH;
import static java.util.Collections.singletonList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@IntegrationTestConfig
public class GlobalExceptionHandlerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TableService tableService;

    @MockBean
    private ThrowingSupplier<Boolean> throwingSupplier;

    @Test
    public void handle_AnyException_ReturnsGenericErrorResponse() throws Exception {
        when(throwingSupplier.get())
                .thenThrow(new IllegalArgumentException("should not be returned"));

        mockMvc.perform(get("/test"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.[0].errorCode", nullValue()))
                .andExpect(
                        jsonPath(
                                "$.[0].errorMessage",
                                equalTo("An unexpected error occurred. If it persists please contact your FCR administrator")));
    }

    @Test
    public void handle_ConstraintViolationException_ReturnsErrorResponses() throws Exception {
        ConstraintViolation violation1 = mock(ConstraintViolation.class);
        ConstraintViolation violation2 = mock(ConstraintViolation.class);

        doReturn("first violation message")
                .when(violation1).getMessage();
        doReturn(PathImpl.createPathFromString("firstViolation"))
                .when(violation1).getPropertyPath();

        doReturn("second violation message")
                .when(violation2).getMessage();
        doReturn(PathImpl.createPathFromString("secondViolation"))
                .when(violation2).getPropertyPath();

        Set<ConstraintViolation<?>> constraintViolations = Sets.newHashSet(
                violation1,
                violation2
        );

        ConstraintViolationException constraintViolationException = new ConstraintViolationException(
                "the message",
                constraintViolations
        );

        when(throwingSupplier.get())
                .thenThrow(constraintViolationException);

        mockMvc.perform(get("/test"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.[0].errorCode", equalTo("firstViolation")))
                .andExpect(jsonPath("$.[0].errorMessage", equalTo("first violation message")))
                .andExpect(jsonPath("$.[1].errorCode", equalTo("secondViolation")))
                .andExpect(jsonPath("$.[1].errorMessage", equalTo("second violation message")));
    }

    @Test
    public void handle_MethodArgumentNotValidException_ReturnsErrorResponses() throws Exception {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("the object name", "the field name", "the default message");

        doReturn(singletonList(fieldError))
                .when(bindingResult).getFieldErrors();

        MethodArgumentNotValidException methodArgumentNotValidException
                = new MethodArgumentNotValidException(mock(MethodParameter.class), bindingResult);

        when(throwingSupplier.get())
                .thenThrow(methodArgumentNotValidException);

        mockMvc.perform(get("/test"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.[0].errorCode", equalTo("the field name")))
                .andExpect(jsonPath("$.[0].errorMessage", equalTo("the default message")));
    }

    @Test
    public void handle_MissingServletRequestParam_ReturnsBadRequestResponse() throws Exception {
        MissingServletRequestParameterException parameterException = new MissingServletRequestParameterException(
                "myName",
                "myType");

        when(throwingSupplier.get())
                .thenThrow(parameterException);

        mockMvc.perform(get("/test"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.[0].errorCode", equalTo("myName")))
                .andExpect(jsonPath(
                        "$.[0].errorMessage",
                        equalTo("Required myType parameter 'myName' is not present")));
    }

    @Test
    public void handle_MaxUploadSizeExceededException_ReturnsErrorResponses() throws Exception {
        when(throwingSupplier.get())
                .thenThrow(new MaxUploadSizeExceededException(1000));

        mockMvc.perform(get("/test"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.[0].errorCode", equalTo("Maximum file size exceeded")))
                .andExpect(jsonPath(
                        "$.[0].errorMessage",
                        equalTo("Cannot upload files with size greater than [1000 bytes]")));
    }

    @Test
    public void handle_JsonMappingExceptionForUnrecognizedField_ReturnsErrorResponses() throws Exception {
        mockMvc.perform(
                post("/test")
                        .contentType(APPLICATION_JSON_VALUE)
                        .content("{ \"name\" : \"STRING\" }")
                        .with(BASIC_AUTH))

                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.[0].errorCode", equalTo("JSON parsing error")))
                .andExpect(jsonPath(
                        "$.[0].errorMessage",
                        startsWith("Cannot parse JSON at line 1, column 12")));
    }

    @Test
    public void handle_MethodArgumentTypeMismatchException_ReturnsErrorResponses() throws Exception {
        mockMvc.perform(get("/test-param?number=boom"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.[0].errorCode", equalTo("PARAM_TYPE_MISMATCH")))
                .andExpect(
                        jsonPath(
                                "$.[0].errorMessage",
                                equalTo("Incorrect type for param: number")));
    }

    @Configuration
    public static class TestConfiguration {

        @Bean
        public ThrowingSupplier<Boolean> mockSupplier() {
            return () -> true;
        }
    }

    @RestController
    public static class TestController {

        @Autowired
        private ThrowingSupplier<Boolean> mockSupplier;

        @GetMapping(path = "/test")
        public boolean test() throws Exception {
            return mockSupplier.get();
        }

        @GetMapping(path = "/test-param")
        public boolean wrongParamType(@RequestParam(value = "number") final Integer number) {
            return number > 0;
        }

        @PostMapping(path = "/test", consumes = APPLICATION_JSON_VALUE)
        public FcrResponse<IdView> post(
                @RequestBody @Valid final Request request) {
            return FcrResponse.okResponse(new IdView(1L));
        }
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class Request {

        private int name;
    }
}
