package com.lombardrisk.ignis.server.config.swagger;

import com.lombardrisk.ignis.common.log.CorrelationId;
import com.lombardrisk.ignis.data.common.error.ErrorResponse;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses({
        @ApiResponse(code = 400, message = "BadRequest", response = ErrorResponse.class, responseContainer = "List",
                responseHeaders = {
                        @ResponseHeader(name = CorrelationId.CORRELATION_ID_HEADER, response = String.class)
                }),
        @ApiResponse(code = 500, message = "InternalServerError", response = ErrorResponse.class,
                responseContainer = "List",
                responseHeaders = {
                        @ResponseHeader(name = CorrelationId.CORRELATION_ID_HEADER, response = String.class)
                })
})
public @interface ApiErrorResponses {

}
