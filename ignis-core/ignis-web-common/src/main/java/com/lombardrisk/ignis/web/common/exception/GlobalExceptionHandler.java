package com.lombardrisk.ignis.web.common.exception;

import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonMappingException.Reference;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.lombardrisk.ignis.feature.FeatureNotActiveException;
import com.lombardrisk.ignis.web.common.response.BadRequestErrorResponse;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Comparator;
import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    public static final ErrorResponse UNEXPECTED_ERROR_RESPONSE =
            ErrorResponse.valueOf(
                    "An unexpected error occurred. If it persists please contact your FCR administrator",
                    null);
    private static final String PARAM_TYPE_MISMATCH_ERROR_CODE = "PARAM_TYPE_MISMATCH";

    private static final Comparator<ErrorResponse> ERROR_COMPARATOR = Comparator.comparing(ErrorResponse::getErrorCode);

    private static final int LAST_FIELD_NAME_INDEX = 2;

    private static final String AT_POS_TEMPLATE = " at line %d, column %d";
    private static final String JSON_MAPPING_TEMPLATE = "%s in attribute with name '%s' (" + AT_POS_TEMPLATE + ")";
    private static final String MAX_UPLOAD_SIZE_TEMPLATE = "Cannot upload files with size greater than [%s]";
    private static final String UNRECOGNIZED_FIELD_TEMPLATE = "Unrecognized field '%s'" + AT_POS_TEMPLATE;
    private static final String GENERIC_JSON_PARSE_TEMPLATE = "Cannot parse JSON" + AT_POS_TEMPLATE;
    private static final String JSON_PARSING_ERROR_CODE = "JSON parsing error";
    private static final String MAX_UPLOAD_SIZE_CODE = "Maximum file size exceeded";

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseBody
    BadRequestErrorResponse handleConstraintViolationException(final ConstraintViolationException violationException) {
        List<ErrorResponse> messages =
                violationException.getConstraintViolations().stream()
                        .map(GlobalExceptionHandler::createErrorResponse)
                        .sorted(ERROR_COMPARATOR)
                        .collect(toList());

        return BadRequestErrorResponse.valueOf(messages);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    BadRequestErrorResponse handleMethodArgumentNotValidException(final MethodArgumentNotValidException methodArgException) {
        List<ErrorResponse> errorResponses =
                methodArgException.getBindingResult().getFieldErrors().stream()
                        .map(GlobalExceptionHandler::createErrorResponse)
                        .sorted(ERROR_COMPARATOR)
                        .collect(toList());
        return BadRequestErrorResponse.valueOf(errorResponses);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseBody
    BadRequestErrorResponse handleMethodArgumentTypeMismatchException(
            final MethodArgumentTypeMismatchException methodArgException) {
        return BadRequestErrorResponse.valueOf(singletonList(
                ErrorResponse.valueOf(
                        "Incorrect type for param: " + methodArgException.getName(),
                        PARAM_TYPE_MISMATCH_ERROR_CODE)));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseBody
    BadRequestErrorResponse handleMissingServletRequestParameterException(final MissingServletRequestParameterException exception) {
        return BadRequestErrorResponse.valueOf(singletonList(ErrorResponse.valueOf(
                exception.getMessage(),
                exception.getParameterName())));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseBody
    BadRequestErrorResponse handleMaxUploadSizeExceededException(final MaxUploadSizeExceededException exception) {
        log.error(exception.getMessage());

        return BadRequestErrorResponse.valueOf(singletonList(ErrorResponse.valueOf(
                createMaxUploadSizeExceededMessage(exception),
                MAX_UPLOAD_SIZE_CODE)));
    }

    @ExceptionHandler({ JsonMappingException.class, HttpMessageNotReadableException.class })
    @ResponseBody
    BadRequestErrorResponse handleJsonMappingException(final JsonMappingException jsonException) {
        log.error(jsonException.getMessage());

        String errorMessage = createJsonMappingMessage(jsonException);

        return BadRequestErrorResponse.valueOf(
                singletonList(ErrorResponse.valueOf(errorMessage, JSON_PARSING_ERROR_CODE)));
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    protected ResponseEntity<List<ErrorResponse>> handleAnyException(final Exception exception) {
        log.error(exception.getMessage(), exception);

        return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                .body(singletonList(UNEXPECTED_ERROR_RESPONSE));
    }

    @ExceptionHandler(FeatureNotActiveException.class)
    @ResponseBody
    protected ResponseEntity<List<ErrorResponse>> handleFeatureNotActiveException(final FeatureNotActiveException exception) {
        log.error(exception.getMessage(), exception);

        return ResponseEntity.status(INTERNAL_SERVER_ERROR)
                .body(singletonList(ErrorResponse.valueOf(exception.getMessage(), exception.getFeature().toString())));
    }

    private static String createMaxUploadSizeExceededMessage(final MaxUploadSizeExceededException exception) {
        return String.format(MAX_UPLOAD_SIZE_TEMPLATE, FileUtils.byteCountToDisplaySize(exception.getMaxUploadSize()));
    }

    private static String createJsonMappingMessage(final JsonMappingException jsonException) {
        String message = jsonException.getMessage();
        JsonLocation location = jsonException.getLocation();

        if (message.contains("missing property")) {
            return createMissingPropMessage(jsonException);
        }
        if (message.contains("into a subtype of [simple type, class com.lombardrisk.ignis.api.table.Field")) {
            return createMissingFieldTypeMessage(jsonException);
        }
        if (jsonException instanceof UnrecognizedPropertyException) {
            return createUnrecognizedFieldMessage((UnrecognizedPropertyException) jsonException);
        }
        return String.format(GENERIC_JSON_PARSE_TEMPLATE, location.getLineNr(), location.getColumnNr());
    }

    private static String createMessageFromPart(final JsonMappingException jsonException, final String messagePart) {
        String message = jsonException.getMessage();
        int missingPropStart = message.indexOf(messagePart);
        int missingPropEnd = message.lastIndexOf('\'') + 1;

        String missingProp = message.substring(missingPropStart, missingPropEnd);

        List<Reference> path = jsonException.getPath();
        String lastFieldName = path.get(path.size() - LAST_FIELD_NAME_INDEX).getFieldName();

        JsonLocation location = jsonException.getLocation();
        return String.format(
                JSON_MAPPING_TEMPLATE,
                missingProp, lastFieldName, location.getLineNr(), location.getColumnNr());
    }

    private static String createMissingPropMessage(final JsonMappingException e) {
        return createMessageFromPart(e, "missing property");
    }

    private static String createUnrecognizedFieldMessage(final UnrecognizedPropertyException jsonException) {
        JsonLocation location = jsonException.getLocation();

        return String.format(
                UNRECOGNIZED_FIELD_TEMPLATE,
                jsonException.getPropertyName(), location.getLineNr(), location.getColumnNr());
    }

    private static String createMissingFieldTypeMessage(final JsonMappingException jsonException) {
        return createMessageFromPart(jsonException, "Could not resolve type id");
    }

    private static ErrorResponse createErrorResponse(final FieldError fieldErr) {
        return ErrorResponse.valueOf(fieldErr.getDefaultMessage(), fieldErr.getField());
    }

    private static ErrorResponse createErrorResponse(final ConstraintViolation<?> violation) {
        return ErrorResponse.valueOf(violation.getMessage(), violation.getPropertyPath().toString());
    }
}
